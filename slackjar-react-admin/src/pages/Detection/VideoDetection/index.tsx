import React, {useState, useEffect, useRef, useCallback} from 'react';
import {Button, Upload, InputNumber, Select, Card, message, Statistic, Row, Col, Progress} from 'antd';
import {UploadOutlined, PlayCircleOutlined, PauseCircleOutlined, VideoCameraOutlined} from '@ant-design/icons';
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

const VideoDetection: React.FC = () => {
    const [models, setModels] = useState<ModelInfo[]>([]);
    const [selectedModel, setSelectedModel] = useState('');
    const [confThreshold, setConfThreshold] = useState(0.25);
    const [iouThreshold, setIouThreshold] = useState(0.45);
    const [uploadedFile, setUploadedFile] = useState<string>('');
    const [originalVideoUrl, setOriginalVideoUrl] = useState<string>('');
    const [streamUrl, setStreamUrl] = useState<string>('');
    const [loading, setLoading] = useState(false);
    const [paused, setPaused] = useState(false);
    const [progress, setProgress] = useState(0);
    const [totalFrames, setTotalFrames] = useState(0);
    const [currentFrame, setCurrentFrame] = useState(0);
    const [totalDetections, setTotalDetections] = useState(0);
    const [processTime, setProcessTime] = useState(0);
    const [completed, setCompleted] = useState(false);
    const [taskId, setTaskId] = useState<string>('');

    const socketHandlerRef = useRef<((msg: SocketMessageDTO) => void) | null>(null);

    useEffect(() => {
        loadModels();
        return () => {
            unregisterSocketHandler();
        };
    }, []);

    const registerSocketHandler = useCallback((tid: string) => {
        unregisterSocketHandler();

        const handler = (messageDTO: SocketMessageDTO) => {
            const data = messageDTO.content;
            if (!data || data.taskId !== tid) return;

            const type = data.type;

            if (type === 'start') {
                setTotalFrames(data.totalFrames || 0);
            } else if (type === 'frame') {
                setCurrentFrame(data.currentFrame || 0);
                setTotalFrames(data.totalFrames || 0);
                setTotalDetections(data.totalDetections || 0);
                if (data.totalFrames > 0) {
                    setProgress(Math.round((data.currentFrame * 100) / data.totalFrames));
                }
            } else if (type === 'complete') {
                setLoading(false);
                setCompleted(true);
                setCurrentFrame(data.currentFrame || 0);
                setTotalFrames(data.totalFrames || 0);
                setTotalDetections(data.totalDetections || 0);
                setProcessTime(data.processTime || 0);
                setProgress(100);
                message.success('视频检测完成');
                unregisterSocketHandler();
            } else if (type === 'error') {
                setLoading(false);
                setCompleted(false);
                setStreamUrl('');
                message.error('检测失败: ' + (data.message || '未知错误'));
                unregisterSocketHandler();
            }
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
                setModels(res.data);
                if (res.data.length > 0) {
                    setSelectedModel(res.data[0].name);
                }
            }
        } catch {
            message.error('加载模型列表失败');
        }
    };

    const handleFileUpload: UploadProps['onChange'] = async (info) => {
        if (info.file.status === 'done') {
            const res = info.file.response as { code: number; data: { fileUrl: string; filePath: string } };
            if (res.code === 200) {
                setUploadedFile(res.data.filePath);
                setOriginalVideoUrl(res.data.fileUrl);
                setStreamUrl('');
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
        setTotalFrames(0);
        setTotalDetections(0);
        setCompleted(false);
        setPaused(false);
        setStreamUrl('');

        try {
            const res = await detectVideo({
                filePath: uploadedFile,
                modelName: selectedModel,
                confThreshold,
                iouThreshold,
            });

            if (res.code === 200 && res.data?.taskId) {
                const tid = res.data.taskId;
                setTaskId(tid);
                setStreamUrl(`/api/yolo/stream/${tid}`);
                registerSocketHandler(tid);
                message.info('检测已启动，实时接收结果...');
            } else {
                setLoading(false);
                message.error('启动检测失败');
            }
        } catch {
            setLoading(false);
            message.error('启动检测失败');
        }
    };

    const handlePause = async () => {
        if (!taskId) return;
        try {
            const res = await pauseDetection(taskId);
            if (res.code === 200) {
                setPaused(true);
                message.info('检测已暂停');
            }
        } catch {
            message.error('暂停失败');
        }
    };

    const handleResume = async () => {
        if (!taskId) return;
        try {
            const res = await resumeDetection(taskId);
            if (res.code === 200) {
                setPaused(false);
                message.info('检测已恢复');
            }
        } catch {
            message.error('恢复失败');
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
                        <Col span={2} style={{display: 'flex', alignItems: 'flex-end', gap: 4}}>
                            <Button
                                type="primary"
                                icon={<VideoCameraOutlined/>}
                                onClick={handleDetect}
                                disabled={loading}
                            >
                                检测
                            </Button>
                        </Col>
                    </Row>
                    {loading && (
                        <div style={{marginTop: 16, display: 'flex', alignItems: 'center', gap: 12}}>
                            <Progress
                                percent={progress}
                                status={paused ? 'normal' : 'active'}
                                style={{flex: 1}}
                                format={() => `${currentFrame}/${totalFrames} 帧`}
                            />
                            <Button
                                icon={paused ? <PlayCircleOutlined/> : <PauseCircleOutlined/>}
                                onClick={paused ? handleResume : handlePause}
                                size="small"
                            >
                                {paused ? '恢复' : '暂停'}
                            </Button>
                        </div>
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
                            border: streamUrl ? 'none' : '1px dashed #d9d9d9',
                            borderRadius: 6,
                            padding: streamUrl ? 0 : 48,
                            textAlign: 'center',
                            minHeight: 320,
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center'
                        }}>
                            {streamUrl ? (
                                <img
                                    src={streamUrl}
                                    alt="实时检测"
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
