import React, {useState, useEffect, useRef, useCallback} from 'react';
import {Button, Upload, InputNumber, Select, Card, message, Tag, Statistic, Row, Col, Progress} from 'antd';
import {UploadOutlined, PlayCircleOutlined, PauseCircleOutlined} from '@ant-design/icons';
import type {UploadProps} from 'antd';
import {
    listModels,
    detectVideo,
    pauseDetection,
    resumeDetection,
    type ModelInfo
} from '../../../apis/modules/detection';
import {socketManager} from '../../../socketio';
import {PushWithBackendEnum, type SocketMessageDTO} from '../../../socketio/types';
import {useAuthStore} from '../../../store/authStore';

interface ChannelState {
    uploadedFile: string;
    originalUrl: string;
    streamUrl: string;
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
    paused: boolean;
    taskId: string;
}

const initialChannel = (modelName: string): ChannelState => ({
    uploadedFile: '', originalUrl: '', streamUrl: '',
    selectedModel: modelName, confThreshold: 0.25, iouThreshold: 0.45,
    loading: false, progress: 0, currentFrame: 0, totalFrames: 0,
    totalDetections: 0, processTime: 0, completed: false, paused: false, taskId: '',
});

const MultiVideoDetection: React.FC = () => {
    const [models, setModels] = useState<ModelInfo[]>([]);
    const [channels, setChannels] = useState<ChannelState[]>([]);
    const socketHandlerRef = useRef<((msg: SocketMessageDTO) => void) | null>(null);

    useEffect(() => {
        loadModels();
        return () => {
            unregisterSocketHandler();
        };
    }, []);

    const registerSocketHandler = useCallback(() => {
        unregisterSocketHandler();

        const handler = (messageDTO: SocketMessageDTO) => {
            const data = messageDTO.content;
            if (!data || !data.taskId) return;

            const tid = data.taskId;
            const type = data.type;

            setChannels(prev => prev.map(ch => {
                if (ch.taskId !== tid) return ch;
                const updated = {...ch};

                if (type === 'start') {
                    updated.totalFrames = data.totalFrames || 0;
                } else if (type === 'frame') {
                    updated.currentFrame = data.currentFrame || 0;
                    updated.totalFrames = data.totalFrames || 0;
                    updated.totalDetections = data.totalDetections || 0;
                    if (data.totalFrames > 0) {
                        updated.progress = Math.round((data.currentFrame * 100) / data.totalFrames);
                    }
                } else if (type === 'complete') {
                    updated.loading = false;
                    updated.completed = true;
                    updated.currentFrame = data.currentFrame || 0;
                    updated.totalFrames = data.totalFrames || 0;
                    updated.totalDetections = data.totalDetections || 0;
                    updated.processTime = data.processTime || 0;
                    updated.progress = 100;
                } else if (type === 'error') {
                    updated.loading = false;
                    updated.completed = false;
                    updated.streamUrl = '';
                }

                return updated;
            }));
        };

        socketHandlerRef.current = handler;
        socketManager.registerHandler(PushWithBackendEnum.VIDEO_DETECTION_FRAME, handler);
    }, []);

    const unregisterSocketHandler = () => {
        if (socketHandlerRef.current) {
            socketManager.unregisterHandler(PushWithBackendEnum.VIDEO_DETECTION_FRAME, socketHandlerRef.current);
            socketHandlerRef.current = null;
        }
    };

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
        } catch {
            message.error('加载模型列表失败');
        }
    };

    const handleFileUpload = (index: number): UploadProps['onChange'] => async (info) => {
        if (info.file.status === 'done') {
            const res = info.file.response as { code: number; data: { fileUrl: string; filePath: string } };
            if (res.code === 200) {
                setChannels(prev => prev.map((ch, i) => i === index ? {
                    ...ch,
                    uploadedFile: res.data.filePath,
                    originalUrl: res.data.fileUrl,
                    streamUrl: '',
                    completed: false,
                    progress: 0,
                    totalDetections: 0,
                } : ch));
                message.success(`通道${index + 1}上传成功`);
            } else {
                message.error(`通道${index + 1}上传失败`);
            }
        } else if (info.file.status === 'error') {
            message.error(`通道${index + 1}上传失败`);
        }
    };

    const handleDetectChannel = async (index: number) => {
        const channel = channels[index];
        if (!channel.uploadedFile || !channel.selectedModel) {
            message.warning(`通道${index + 1}请上传视频并选择模型`);
            return;
        }

        setChannels(prev => prev.map((ch, i) => i === index ? {
            ...ch, loading: true, progress: 0, currentFrame: 0, totalFrames: 0,
            totalDetections: 0, completed: false, paused: false, streamUrl: ''
        } : ch));

        try {
            const res = await detectVideo({
                filePath: channel.uploadedFile,
                modelName: channel.selectedModel,
                confThreshold: channel.confThreshold,
                iouThreshold: channel.iouThreshold,
            });

            if (res.code === 200 && res.data?.taskId) {
                const tid = res.data.taskId;
                setChannels(prev => prev.map((ch, i) => i === index ? {
                    ...ch, taskId: tid, streamUrl: `/api/yolo/stream/${tid}`
                } : ch));
                registerSocketHandler();
                message.info(`通道${index + 1}检测已启动`);
            } else {
                setChannels(prev => prev.map((ch, i) => i === index ? {...ch, loading: false} : ch));
                message.error(`通道${index + 1}启动检测失败`);
            }
        } catch {
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

    const handlePauseChannel = async (index: number) => {
        const channel = channels[index];
        if (!channel.taskId) return;
        try {
            const res = await pauseDetection(channel.taskId);
            if (res.code === 200) {
                setChannels(prev => prev.map((ch, i) => i === index ? {...ch, paused: true} : ch));
            }
        } catch {
            message.error(`通道${index + 1}暂停失败`);
        }
    };

    const handleResumeChannel = async (index: number) => {
        const channel = channels[index];
        if (!channel.taskId) return;
        try {
            const res = await resumeDetection(channel.taskId);
            if (res.code === 200) {
                setChannels(prev => prev.map((ch, i) => i === index ? {...ch, paused: false} : ch));
            }
        } catch {
            message.error(`通道${index + 1}恢复失败`);
        }
    };

    const uploadHeaders = {
        token: useAuthStore.getState().jwt || '',
    };

    const totalAllDetections = channels.reduce((sum, ch) => sum + ch.totalDetections, 0);
    const completedChannels = channels.filter(ch => ch.completed).length;

    return (
        <div style={{padding: 24}}>
            <Card title="四路视频协同检测" extra={
                <Button type="primary" icon={<PlayCircleOutlined/>} onClick={handleDetectAll}>
                    全部开始
                </Button>
            } style={{marginBottom: 16}}>
                <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12}}>
                    {channels.map((channel, index) => (
                        <div key={index} style={{background: '#fafafa', borderRadius: 6, padding: 10}}>
                            <div style={{
                                display: 'flex', gap: 6, alignItems: 'center', marginBottom: 6,
                                flexWrap: 'wrap', fontSize: 12
                            }}>
                                <span style={{fontWeight: 'bold', marginRight: 4}}>CH{index + 1}</span>
                                <Upload
                                    name="file" action="/api/sys-file/upload?bizType=video"
                                    headers={uploadHeaders} onChange={handleFileUpload(index)}
                                    accept="video/*" showUploadList={false}
                                >
                                    <Button icon={<UploadOutlined/>} size="small">上传</Button>
                                </Upload>
                                <Select
                                    value={channel.selectedModel}
                                    onChange={(v) => setChannels(prev => prev.map((ch, i) => i === index ? {...ch, selectedModel: v} : ch))}
                                    style={{width: 120}} size="small"
                                    options={models.map(m => ({label: m.name, value: m.name}))}
                                />
                                <InputNumber
                                    value={channel.confThreshold}
                                    onChange={(v) => setChannels(prev => prev.map((ch, i) => i === index ? {...ch, confThreshold: v || 0.25} : ch))}
                                    min={0.01} max={1} step={0.01} style={{width: 70}} size="small" placeholder="Conf"
                                />
                                <InputNumber
                                    value={channel.iouThreshold}
                                    onChange={(v) => setChannels(prev => prev.map((ch, i) => i === index ? {...ch, iouThreshold: v || 0.45} : ch))}
                                    min={0.01} max={1} step={0.01} style={{width: 70}} size="small" placeholder="IoU"
                                />
                                <Button type="primary" size="small"
                                        icon={<PlayCircleOutlined/>}
                                        onClick={() => handleDetectChannel(index)}
                                        disabled={!channel.uploadedFile || !channel.selectedModel || channel.loading}
                                >检测</Button>
                                {channel.loading && (
                                    <Button size="small"
                                            icon={channel.paused ? <PlayCircleOutlined/> : <PauseCircleOutlined/>}
                                            onClick={() => channel.paused ? handleResumeChannel(index) : handlePauseChannel(index)}
                                    >{channel.paused ? '恢复' : '暂停'}</Button>
                                )}
                                {channel.completed && <Tag color="green" style={{margin: 0}}>完成</Tag>}
                            </div>

                            {channel.loading && (
                                <Progress
                                    percent={channel.progress}
                                    status={channel.paused ? 'normal' : 'active'}
                                    size="small"
                                    style={{marginBottom: 6}}
                                    format={() => `${channel.currentFrame}/${channel.totalFrames}`}
                                />
                            )}

                            <div style={{display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 4, height: 180}}>
                                <div style={{
                                    background: '#000', borderRadius: 4, overflow: 'hidden',
                                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                                }}>
                                    {channel.originalUrl ? (
                                        <video src={channel.originalUrl} muted
                                               style={{width: '100%', height: '100%', objectFit: 'contain'}}/>
                                    ) : (
                                        <span style={{color: '#555', fontSize: 11}}>原始视频</span>
                                    )}
                                </div>
                                <div style={{
                                    background: '#000', borderRadius: 4, overflow: 'hidden',
                                    display: 'flex', alignItems: 'center', justifyContent: 'center'
                                }}>
                                    {channel.streamUrl ? (
                                        <img src={channel.streamUrl} alt="检测"
                                             style={{width: '100%', height: '100%', objectFit: 'contain'}}/>
                                    ) : (
                                        <span style={{color: '#555', fontSize: 11}}>
                                            {channel.loading ? '检测中...' : '检测结果'}
                                        </span>
                                    )}
                                </div>
                            </div>

                            {channel.completed && (
                                <div style={{marginTop: 4, fontSize: 11, color: '#888'}}>
                                    目标{channel.totalDetections}个 | {channel.processTime}ms
                                </div>
                            )}
                        </div>
                    ))}
                </div>
            </Card>

            {completedChannels > 0 && (
                <Card title="统计摘要" size="small">
                    <Row gutter={16}>
                        <Col span={6}>
                            <Statistic title="总检测目标" value={totalAllDetections}/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="完成通道" value={completedChannels} suffix="/4"/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="总处理帧"
                                       value={channels.reduce((sum, ch) => sum + ch.currentFrame, 0)}/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="总耗时"
                                       value={channels.reduce((sum, ch) => sum + ch.processTime, 0)} suffix="ms"/>
                        </Col>
                    </Row>
                </Card>
            )}
        </div>
    );
};

export default MultiVideoDetection;
