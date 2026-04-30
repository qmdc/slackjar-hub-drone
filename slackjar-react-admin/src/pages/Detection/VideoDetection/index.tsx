import React, {useState, useEffect, useRef, useCallback} from 'react';
import {Button, Upload, InputNumber, Select, Card, message, Tag, Statistic, Row, Col, Progress} from 'antd';
import {UploadOutlined, PlayCircleOutlined, RestOutlined, VideoCameraOutlined} from '@ant-design/icons';
import type {UploadProps} from 'antd';
import {listModels, detectVideo, type ModelInfo} from '../../../apis/modules/detection';
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
    fps?: number
    width?: number
    height?: number
}

const VideoDetection: React.FC = () => {
    const [models, setModels] = useState<ModelInfo[]>([]);
    const [selectedModel, setSelectedModel] = useState('');
    const [confThreshold, setConfThreshold] = useState(0.25);
    const [iouThreshold, setIouThreshold] = useState(0.45);
    const [uploadedFile, setUploadedFile] = useState<string>('');
    const [originalVideoUrl, setOriginalVideoUrl] = useState<string>('');
    const [currentFrameUrl, setCurrentFrameUrl] = useState<string>('');
    const [loading, setLoading] = useState(false);
    const [progress, setProgress] = useState(0);
    const [totalFrames, setTotalFrames] = useState(0);
    const [currentFrame, setCurrentFrame] = useState(0);
    const [totalDetections, setTotalDetections] = useState(0);
    const [processTime, setProcessTime] = useState(0);
    const [completed, setCompleted] = useState(false);
    const frameHandlerRef = useRef<((msg: SocketMessageDTO) => void) | null>(null);

    useEffect(() => {
        loadModels();
        return () => {
            if (frameHandlerRef.current) {
                socketManager.registerHandler('VIDEO_DETECTION_FRAME', frameHandlerRef.current);
            }
        };
    }, []);

    const loadModels = async () => {
        try {
            const res = await listModels();
            if (res.code === 200 && res.data) {
                setModels(res.data);
                if (res.data.length > 0) {
                    setSelectedModel(res.data[0].name);
                }
            }
        } catch (e) {
            message.error('加载模型列表失败');
        }
    };

    const handleFrameMessage = useCallback((msg: SocketMessageDTO) => {
        const data = msg.content as FrameData;
        if (!data) return;

        switch (data.type) {
            case 'start':
                setTotalFrames(data.totalFrames || 0);
                setProgress(0);
                setCurrentFrame(0);
                setTotalDetections(0);
                setCompleted(false);
                break;
            case 'frame':
                if (data.frameUrl) {
                    setCurrentFrameUrl(data.frameUrl + '&_t=' + Date.now());
                }
                if (data.frameIndex !== undefined && data.totalFrames) {
                    setCurrentFrame(data.frameIndex + 1);
                    setProgress(Math.round(((data.frameIndex + 1) / data.totalFrames) * 100));
                }
                if (data.detectionCount !== undefined) {
                    setTotalDetections(prev => prev + data.detectionCount!);
                }
                break;
            case 'complete':
                setProgress(100);
                setLoading(false);
                setCompleted(true);
                if (data.processTime) {
                    setProcessTime(data.processTime);
                }
                message.success('视频检测完成');
                break;
            case 'error':
                setLoading(false);
                setCompleted(false);
                message.error(data.message || '检测失败');
                break;
        }
    }, []);

    useEffect(() => {
        frameHandlerRef.current = handleFrameMessage;
        socketManager.registerHandler('VIDEO_DETECTION_FRAME', handleFrameMessage);
    }, [handleFrameMessage]);

    const handleFileUpload: UploadProps['onChange'] = async (info) => {
        if (info.file.status === 'done') {
            const res = info.file.response as { code: number; data: { fileUrl: string; filePath: string } };
            if (res.code === 200) {
                setUploadedFile(res.data.filePath);
                setOriginalVideoUrl(res.data.fileUrl);
                setCurrentFrameUrl('');
                setCompleted(false);
                setProgress(0);
                setTotalDetections(0);
                message.success('文件上传成功');
            } else {
                message.error('文件上传失败');
            }
        } else if (info.file.status === 'error') {
            message.error('文件上传失败');
        }
    };

    const handleDetect = async () => {
        if (!uploadedFile || !selectedModel) {
            message.warning('请上传视频并选择模型');
            return;
        }

        setLoading(true);
        setProgress(0);
        setCurrentFrame(0);
        setTotalDetections(0);
        setCompleted(false);
        setCurrentFrameUrl('');

        try {
            const res = await detectVideo({
                filePath: uploadedFile,
                modelName: selectedModel,
                confThreshold,
                iouThreshold,
            });

            if (res.code === 200 && res.data?.taskId) {
                message.info('检测已启动，实时接收结果...');
            } else {
                setLoading(false);
                message.error('启动检测失败');
            }
        } catch (e) {
            setLoading(false);
            message.error('启动检测失败');
        }
    };

    const uploadHeaders = {
        token: useAuthStore.getState().jwt || '',
    };

    return (
        <div style={{padding: 24}}>
            <Card title="视频目标检测" style={{marginBottom: 24}}>
                <div style={{marginBottom: 24}}>
                    <Row gutter={16}>
                        <Col span={6}>
                            <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>上传视频</label>
                            <Upload
                                name="file"
                                action="/api/sys-file/upload?bizType=video"
                                headers={uploadHeaders}
                                onChange={handleFileUpload}
                                accept="video/*"
                                showUploadList={false}
                            >
                                <Button icon={<UploadOutlined/>}>选择视频</Button>
                            </Upload>
                        </Col>
                        <Col span={6}>
                            <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>选择模型</label>
                            <Select
                                value={selectedModel}
                                onChange={setSelectedModel}
                                style={{width: '100%'}}
                                options={models.map(m => ({label: m.name, value: m.name}))}
                            />
                        </Col>
                        <Col span={5}>
                            <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>置信度阈值</label>
                            <InputNumber
                                value={confThreshold}
                                onChange={(value) => setConfThreshold(value || 0.25)}
                                min={0.01}
                                max={1}
                                step={0.01}
                                style={{width: '100%'}}
                            />
                        </Col>
                        <Col span={5}>
                            <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>IoU阈值</label>
                            <InputNumber
                                value={iouThreshold}
                                onChange={(value) => setIouThreshold(value || 0.45)}
                                min={0.01}
                                max={1}
                                step={0.01}
                                style={{width: '100%'}}
                            />
                        </Col>
                        <Col span={2} style={{display: 'flex', alignItems: 'flex-end'}}>
                            <Button
                                type="primary"
                                icon={loading ? <RestOutlined spin/> : <VideoCameraOutlined/>}
                                onClick={handleDetect}
                                loading={loading}
                                disabled={loading}
                            >
                                检测
                            </Button>
                        </Col>
                    </Row>
                    {loading && (
                        <Progress
                            percent={progress}
                            status="active"
                            style={{marginTop: 16}}
                            format={() => `${currentFrame}/${totalFrames} 帧`}
                        />
                    )}
                </div>

                <Row gutter={16}>
                    <Col span={12}>
                        <div style={{
                            border: originalVideoUrl ? 'none' : '1px dashed #d9d9d9',
                            borderRadius: 6,
                            padding: originalVideoUrl ? 0 : 48,
                            textAlign: 'center',
                            minHeight: 320,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}>
                            {originalVideoUrl ? (
                                <video
                                    src={originalVideoUrl}
                                    controls
                                    style={{width: '100%', height: '100%', objectFit: 'contain'}}
                                />
                            ) : (
                                <span style={{color: '#999'}}>请先上传视频</span>
                            )}
                        </div>
                    </Col>
                    <Col span={12}>
                        <div style={{
                            border: currentFrameUrl ? 'none' : '1px dashed #d9d9d9',
                            borderRadius: 6,
                            padding: currentFrameUrl ? 0 : 48,
                            textAlign: 'center',
                            minHeight: 320,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}>
                            {currentFrameUrl ? (
                                <img
                                    src={currentFrameUrl}
                                    alt="检测结果"
                                    style={{width: '100%', height: '100%', objectFit: 'contain'}}
                                />
                            ) : (
                                <span style={{color: '#999'}}>{loading ? '检测中...' : '暂无检测结果'}</span>
                            )}
                        </div>
                    </Col>
                </Row>
            </Card>

            {completed && (
                <Card title="检测统计">
                    <Row gutter={16}>
                        <Col span={6}>
                            <Statistic title="总帧数" value={currentFrame}/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="检测目标数" value={totalDetections}/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="处理耗时" value={processTime} suffix="ms"/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="平均每帧耗时"
                                       value={currentFrame > 0 ? Math.round(processTime / currentFrame) : 0}
                                       suffix="ms"/>
                        </Col>
                    </Row>
                </Card>
            )}
        </div>
    );
};

export default VideoDetection;
