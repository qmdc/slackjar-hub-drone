import React, {useState, useEffect, useCallback} from 'react';
import {Button, Upload, InputNumber, Select, Card, message, Tag, Statistic, Row, Col, Progress} from 'antd';
import {UploadOutlined, PlayCircleOutlined, RestOutlined, UpCircleOutlined} from '@ant-design/icons';
import type {UploadProps} from 'antd';
import {listModels, detectMultiVideo, type ModelInfo} from '../../../apis/modules/detection';
import {useAuthStore} from '../../../store/authStore';
import {socketManager} from '../../../socketio';
import type {SocketMessageDTO} from '../../../socketio/types';

interface FrameData {
    type: string
    taskId: string
    frameIndex?: number
    totalFrames?: number
    frameUrl?: string
    detectionCount?: number
    processTime?: number
    message?: string
}

interface ChannelState {
    uploadedFile: string;
    originalUrl: string;
    currentFrameUrl: string;
    selectedModel: string;
    confThreshold: number;
    iouThreshold: number;
    loading: boolean;
    progress: number;
    currentFrame: number;
    totalFrames: number;
    totalDetections: number;
    processTime: number;
    completed: boolean;
    taskId: string;
}

const initialChannel = (modelName: string): ChannelState => ({
    uploadedFile: '', originalUrl: '', currentFrameUrl: '',
    selectedModel: modelName, confThreshold: 0.25, iouThreshold: 0.45,
    loading: false, progress: 0, currentFrame: 0, totalFrames: 0,
    totalDetections: 0, processTime: 0, completed: false, taskId: '',
});

const MultiVideoDetection: React.FC = () => {
    const [models, setModels] = useState<ModelInfo[]>([]);
    const [channels, setChannels] = useState<ChannelState[]>([]);

    useEffect(() => {
        loadModels();
    }, []);

    const loadModels = async () => {
        try {
            const res = await listModels();
            if (res.code === 200 && res.data) {
                const data = res.data;
                setModels(data);
                setChannels([
                    initialChannel(data[0]?.name || ''),
                    initialChannel(data[0]?.name || ''),
                    initialChannel(data[0]?.name || ''),
                    initialChannel(data[0]?.name || ''),
                ]);
            }
        } catch (e) {
            message.error('加载模型列表失败');
        }
    };

    const handleFrameMessage = useCallback((msg: SocketMessageDTO) => {
        const data = msg.content as FrameData;
        if (!data || !data.taskId) return;

        setChannels(prev => prev.map(ch => {
            if (ch.taskId !== data.taskId) return ch;

            switch (data.type) {
                case 'start':
                    return {...ch, totalFrames: data.totalFrames || 0, progress: 0, currentFrame: 0, totalDetections: 0, completed: false};
                case 'frame':
                    return {
                        ...ch,
                        currentFrameUrl: data.frameUrl ? data.frameUrl + '&_t=' + Date.now() : ch.currentFrameUrl,
                        currentFrame: data.frameIndex !== undefined ? data.frameIndex + 1 : ch.currentFrame,
                        progress: data.frameIndex !== undefined && data.totalFrames ? Math.round(((data.frameIndex + 1) / data.totalFrames) * 100) : ch.progress,
                        totalDetections: ch.totalDetections + (data.detectionCount || 0),
                    };
                case 'complete':
                    return {...ch, progress: 100, loading: false, completed: true, processTime: data.processTime || 0};
                case 'error':
                    return {...ch, loading: false, completed: false};
                default:
                    return ch;
            }
        }));
    }, []);

    useEffect(() => {
        socketManager.registerHandler('VIDEO_DETECTION_FRAME', handleFrameMessage);
    }, [handleFrameMessage]);

    const handleFileUpload = (index: number): UploadProps['onChange'] => async (info) => {
        if (info.file.status === 'done') {
            const res = info.file.response as { code: number; data: { fileUrl: string; filePath: string } };
            if (res.code === 200) {
                setChannels(prev => prev.map((ch, i) => i === index ? {
                    ...ch,
                    uploadedFile: res.data.filePath,
                    originalUrl: res.data.fileUrl,
                    currentFrameUrl: '',
                    completed: false,
                    progress: 0,
                    totalDetections: 0,
                } : ch));
                message.success(`通道${index + 1}文件上传成功`);
            } else {
                message.error(`通道${index + 1}文件上传失败`);
            }
        } else if (info.file.status === 'error') {
            message.error(`通道${index + 1}文件上传失败`);
        }
    };

    const handleDetectChannel = async (index: number) => {
        const channel = channels[index];
        if (!channel.uploadedFile || !channel.selectedModel) {
            message.warning(`通道${index + 1}请上传视频并选择模型`);
            return;
        }

        setChannels(prev => prev.map((ch, i) => i === index ? {...ch, loading: true, progress: 0, currentFrame: 0, totalDetections: 0, completed: false, currentFrameUrl: ''} : ch));

        try {
            const res = await detectMultiVideo([{
                filePath: channel.uploadedFile,
                modelName: channel.selectedModel,
                confThreshold: channel.confThreshold,
                iouThreshold: channel.iouThreshold,
            }]);

            if (res.code === 200 && res.data && res.data[0]?.taskId) {
                setChannels(prev => prev.map((ch, i) => i === index ? {...ch, taskId: res.data![0].taskId} : ch));
                message.info(`通道${index + 1}检测已启动`);
            } else {
                setChannels(prev => prev.map((ch, i) => i === index ? {...ch, loading: false} : ch));
                message.error(`通道${index + 1}启动检测失败`);
            }
        } catch (e) {
            setChannels(prev => prev.map((ch, i) => i === index ? {...ch, loading: false} : ch));
            message.error(`通道${index + 1}启动检测失败`);
        }
    };

    const handleDetectAll = async () => {
        for (let i = 0; i < channels.length; i++) {
            if (channels[i].uploadedFile && channels[i].selectedModel) {
                await handleDetectChannel(i);
            }
        }
    };

    const resetChannel = (index: number) => {
        setChannels(prev => prev.map((ch, i) => i === index ? {
            ...ch,
            uploadedFile: '', originalUrl: '', currentFrameUrl: '',
            progress: 0, totalDetections: 0, completed: false, taskId: '',
        } : ch));
    };

    const uploadHeaders = {
        token: useAuthStore.getState().jwt || '',
    };

    const totalAllDetections = channels.reduce((sum, ch) => sum + ch.totalDetections, 0);
    const activeChannels = channels.filter(ch => ch.completed).length;

    return (
        <div style={{padding: 24}}>
            <Card title="四路视频协同检测" style={{marginBottom: 24}}>
                <div style={{display: 'flex', gap: 12, marginBottom: 16}}>
                    <Button type="primary" icon={<PlayCircleOutlined/>} onClick={handleDetectAll}>
                        全部开始检测
                    </Button>
                </div>

                <div style={{display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: 16}}>
                    {channels.map((channel, index) => (
                        <Card key={index} title={`通道 ${index + 1}`} bordered={false}>
                            <div style={{marginBottom: 12, display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap'}}>
                                <Upload
                                    name="file"
                                    action="/api/sys-file/upload?bizType=video"
                                    headers={uploadHeaders}
                                    onChange={handleFileUpload(index)}
                                    accept="video/*"
                                    showUploadList={false}
                                >
                                    <Button icon={<UploadOutlined/>}>上传</Button>
                                </Upload>
                                <Select
                                    value={channel.selectedModel}
                                    onChange={(value) => setChannels(prev => prev.map((ch, i) => i === index ? {...ch, selectedModel: value} : ch))}
                                    style={{width: 160}}
                                    options={models.map(m => ({label: m.name, value: m.name}))}
                                />
                                <InputNumber
                                    value={channel.confThreshold}
                                    onChange={(value) => setChannels(prev => prev.map((ch, i) => i === index ? {...ch, confThreshold: value || 0.25} : ch))}
                                    min={0.01} max={1} step={0.01} style={{width: 80}} placeholder="Conf"
                                />
                                <InputNumber
                                    value={channel.iouThreshold}
                                    onChange={(value) => setChannels(prev => prev.map((ch, i) => i === index ? {...ch, iouThreshold: value || 0.45} : ch))}
                                    min={0.01} max={1} step={0.01} style={{width: 80}} placeholder="IoU"
                                />
                                <Button
                                    type="primary"
                                    icon={channel.loading ? <RestOutlined spin/> : <PlayCircleOutlined/>}
                                    onClick={() => handleDetectChannel(index)}
                                    loading={channel.loading}
                                    disabled={!channel.uploadedFile || !channel.selectedModel}
                                    size="small"
                                >
                                    检测
                                </Button>
                                <Button
                                    icon={<UpCircleOutlined/>}
                                    onClick={() => resetChannel(index)}
                                    disabled={!channel.uploadedFile}
                                    size="small"
                                >
                                    重置
                                </Button>
                            </div>

                            {channel.loading && (
                                <Progress
                                    percent={channel.progress}
                                    status="active"
                                    size="small"
                                    format={() => `${channel.currentFrame}/${channel.totalFrames}`}
                                />
                            )}

                            <div style={{display: 'flex', gap: 8, minHeight: 150}}>
                                <div style={{flex: 1, border: channel.originalUrl ? 'none' : '1px dashed #d9d9d9', borderRadius: 4, display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden'}}>
                                    {channel.originalUrl ? (
                                        <video src={channel.originalUrl} controls style={{width: '100%', maxHeight: 150, objectFit: 'contain'}}/>
                                    ) : (
                                        <span style={{color: '#999', fontSize: 12}}>原始视频</span>
                                    )}
                                </div>
                                <div style={{flex: 1, border: channel.currentFrameUrl ? 'none' : '1px dashed #d9d9d9', borderRadius: 4, display: 'flex', alignItems: 'center', justifyContent: 'center', overflow: 'hidden'}}>
                                    {channel.currentFrameUrl ? (
                                        <img src={channel.currentFrameUrl} alt="检测" style={{width: '100%', maxHeight: 150, objectFit: 'contain'}}/>
                                    ) : (
                                        <span style={{color: '#999', fontSize: 12}}>{channel.loading ? '检测中...' : '检测结果'}</span>
                                    )}
                                </div>
                            </div>

                            {channel.completed && (
                                <div style={{marginTop: 8, display: 'flex', gap: 8}}>
                                    <Tag color="green">完成</Tag>
                                    <Tag>{channel.totalDetections}个目标</Tag>
                                    <Tag>{channel.processTime}ms</Tag>
                                </div>
                            )}
                        </Card>
                    ))}
                </div>
            </Card>

            <Card title="统计摘要">
                <Row gutter={16}>
                    <Col span={6}>
                        <Statistic title="总检测目标数" value={totalAllDetections}/>
                    </Col>
                    <Col span={6}>
                        <Statistic title="活跃通道" value={activeChannels}/>
                    </Col>
                    <Col span={6}>
                        <Statistic title="总处理帧数" value={channels.reduce((sum, ch) => sum + ch.currentFrame, 0)}/>
                    </Col>
                    <Col span={6}>
                        <Statistic title="总耗时" value={channels.reduce((sum, ch) => sum + ch.processTime, 0)} suffix="ms"/>
                    </Col>
                </Row>
            </Card>
        </div>
    );
};

export default MultiVideoDetection;
