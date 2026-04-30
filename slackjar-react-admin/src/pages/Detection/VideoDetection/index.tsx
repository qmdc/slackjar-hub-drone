import React, {useState, useEffect} from 'react';
import {Button, Upload, InputNumber, Select, Card, message, Tag, Statistic, Row, Col, Progress} from 'antd';
import {UploadOutlined, PlayCircleOutlined, RestOutlined, VideoCameraOutlined} from '@ant-design/icons';
import type {UploadProps} from 'antd';
import {listModels, detectVideo, type ModelInfo, type DetectionResult} from '../../../apis/modules/detection';
import {useAuthStore} from '../../../store/authStore';

const VideoDetection: React.FC = () => {
    const [models, setModels] = useState<ModelInfo[]>([]);
    const [selectedModel, setSelectedModel] = useState('');
    const [confThreshold, setConfThreshold] = useState(0.25);
    const [iouThreshold, setIouThreshold] = useState(0.45);
    const [uploadedFile, setUploadedFile] = useState<string>('');
    const [originalVideoUrl, setOriginalVideoUrl] = useState<string>('');
    const [resultVideoUrl, setResultVideoUrl] = useState<string>('');
    const [detectionResult, setDetectionResult] = useState<DetectionResult | null>(null);
    const [loading, setLoading] = useState(false);
    const [progress, setProgress] = useState(0);

    useEffect(() => {
        loadModels();
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

    const handleFileUpload: UploadProps['onChange'] = async (info) => {
        if (info.file.status === 'done') {
            const res = info.file.response as { code: number; data: { fileUrl: string; filePath: string } };
            if (res.code === 200) {
                setUploadedFile(res.data.filePath);
                setOriginalVideoUrl(res.data.fileUrl);
                setResultVideoUrl('');
                setDetectionResult(null);
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
        
        const progressInterval = setInterval(() => {
            setProgress(prev => Math.min(prev + 5, 95));
        }, 500);

        try {
            const res = await detectVideo({
                filePath: uploadedFile,
                modelName: selectedModel,
                confThreshold,
                iouThreshold,
            });

            clearInterval(progressInterval);
            setProgress(100);

            if (res.code === 200 && res.data) {
                const result = res.data;
                setDetectionResult(result);
                if (result.success && result.outputPath) {
                    setResultVideoUrl(`/api/sys-file/local/download?filePath=${result.outputPath}`);
                }
                if (!result.success) {
                    message.error(result.message);
                }
            }
        } catch (e) {
            clearInterval(progressInterval);
            message.error('检测失败');
        } finally {
            setLoading(false);
        }
    };

    const getClassDistribution = () => {
        if (!detectionResult?.detections) return [];
        const counts: Record<string, number> = {};
        detectionResult.detections.forEach(d => {
            counts[d.className] = (counts[d.className] || 0) + 1;
        });
        return Object.entries(counts).map(([name, count]) => ({name, count}));
    };

    const uploadHeaders = {
        token: useAuthStore.getState().jwt || '',
    };

    return (
        <div style={{padding: 24}}>
            <Card title="视频目标检测" style={{marginBottom: 24}}>
                <Row gutter={16}>
                    <Col span={8}>
                        <div style={{marginBottom: 16}}>
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
                        </div>

                        <div style={{marginBottom: 16}}>
                            <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>选择模型</label>
                            <Select
                                value={selectedModel}
                                onChange={setSelectedModel}
                                style={{width: '100%'}}
                                options={models.map(m => ({label: m.name, value: m.name}))}
                            />
                        </div>

                        <div style={{marginBottom: 16}}>
                            <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>参数配置</label>
                            <Row gutter={8}>
                                <Col span={11}>
                                    <label style={{display: 'block', marginBottom: 4}}>置信度阈值</label>
                                    <InputNumber
                                        value={confThreshold}
                                        onChange={(value) => setConfThreshold(value || 0.25)}
                                        min={0.01}
                                        max={1}
                                        step={0.01}
                                        style={{width: '100%'}}
                                    />
                                </Col>
                                <Col span={11}>
                                    <label style={{display: 'block', marginBottom: 4}}>IoU阈值</label>
                                    <InputNumber
                                        value={iouThreshold}
                                        onChange={(value) => setIouThreshold(value || 0.45)}
                                        min={0.01}
                                        max={1}
                                        step={0.01}
                                        style={{width: '100%'}}
                                    />
                                </Col>
                            </Row>
                        </div>

                        <Button
                            type="primary"
                            icon={loading ? <RestOutlined spin/> : <VideoCameraOutlined/>}
                            onClick={handleDetect}
                            loading={loading}
                            disabled={loading}
                        >
                            开始检测
                        </Button>

                        {loading && (
                            <div style={{marginTop: 16}}>
                                <Progress percent={progress} status="active"/>
                            </div>
                        )}
                    </Col>

                    <Col span={8}>
                        {originalVideoUrl && (
                            <div style={{marginBottom: 16}}>
                                <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>原始视频</label>
                                <video
                                    src={originalVideoUrl}
                                    controls
                                    style={{maxWidth: '100%', maxHeight: 250}}
                                />
                            </div>
                        )}
                    </Col>

                    <Col span={8}>
                        {resultVideoUrl && (
                            <div>
                                <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>检测结果</label>
                                <video
                                    src={resultVideoUrl}
                                    controls
                                    style={{maxWidth: '100%', maxHeight: 250}}
                                />
                            </div>
                        )}
                    </Col>
                </Row>
            </Card>

            {detectionResult && (
                <Card title="检测统计">
                    <Row gutter={16}>
                        <Col span={6}>
                            <Statistic title="检测目标数" value={detectionResult.detections?.length || 0}/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="处理帧数" value={detectionResult.totalFrames || 0}/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="平均置信度" value={detectionResult.detections?.reduce((sum, d) => sum + d.confidence, 0) / (detectionResult.detections?.length || 1) || 0} precision={2}/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="最高置信度" value={Math.max(...(detectionResult.detections?.map(d => d.confidence) || [0]))} precision={2}/>
                        </Col>
                    </Row>

                    <div style={{marginTop: 24}}>
                        <label style={{display: 'block', marginBottom: 12, fontWeight: 'bold'}}>目标类别分布</label>
                        <div style={{display: 'flex', flexWrap: 'wrap', gap: 12}}>
                            {getClassDistribution().map(item => (
                                <Tag key={item.name} color="green">
                                    {item.name}: {item.count}个
                                </Tag>
                            ))}
                        </div>
                    </div>
                </Card>
            )}
        </div>
    );
};

export default VideoDetection;
