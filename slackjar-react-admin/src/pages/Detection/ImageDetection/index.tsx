import React, {useState, useEffect} from 'react';
import {Button, Upload, InputNumber, Select, Card, message, Image, Tag, Statistic, Row, Col} from 'antd';
import {UploadOutlined, PlayCircleOutlined, RestOutlined} from '@ant-design/icons';
import type {UploadProps} from 'antd';
import {listModels, detectImage, type ModelInfo, type DetectionResult} from '../../../apis/modules/detection';
import {useAuthStore} from '../../../store/authStore';

const ImageDetection: React.FC = () => {
    const [models, setModels] = useState<ModelInfo[]>([]);
    const [selectedModel, setSelectedModel] = useState('');
    const [confThreshold, setConfThreshold] = useState(0.25);
    const [iouThreshold, setIouThreshold] = useState(0.45);
    const [uploadedFile, setUploadedFile] = useState<string>('');
    const [originalImageUrl, setOriginalImageUrl] = useState<string>('');
    const [resultImageUrl, setResultImageUrl] = useState<string>('');
    const [detectionResult, setDetectionResult] = useState<DetectionResult | null>(null);
    const [loading, setLoading] = useState(false);

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
            const res = info.file.response as { code: number; data: { fileUrl: string } };
            if (res.code === 200) {
                setUploadedFile(res.data.fileUrl);
                console.log('文件上传 filePath:', res.data.fileUrl)
                setOriginalImageUrl(res.data.fileUrl);
                setResultImageUrl('');
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
            console.log('请上传图片并选择模型', uploadedFile, selectedModel)
            message.warning('请上传图片并选择模型');
            return;
        }

        setLoading(true);
        try {
            const res = await detectImage({
                filePath: uploadedFile,
                modelName: selectedModel,
                confThreshold,
                iouThreshold,
            });

            if (res.code === 200 && res.data) {
                const result = res.data;
                setDetectionResult(result);
                if (result.success && result.outputPath) {
                    setResultImageUrl(`/api/sys-file/local/download?filePath=${result.outputPath}`);
                }
                if (!result.success) {
                    message.error(result.message);
                }
            }
        } catch (e) {
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
            <Card title="图片目标检测" style={{marginBottom: 24}}>
                <Row gutter={16}>
                    <Col span={12}>
                        <div style={{marginBottom: 16}}>
                            <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>上传图片</label>
                            <Upload
                                name="file"
                                action="/api/sys-file/upload?bizType=image"
                                headers={uploadHeaders}
                                onChange={handleFileUpload}
                                accept="image/*"
                                showUploadList={false}
                            >
                                <Button icon={<UploadOutlined/>}>选择图片</Button>
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
                            <Row gutter={16}>
                                <Col span={11}>
                                    <label style={{display: 'block', marginBottom: 4}}>置信度阈值 (Conf)</label>
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
                                    <label style={{display: 'block', marginBottom: 4}}>IoU阈值 (IoU)</label>
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
                            icon={loading ? <RestOutlined spin/> : <PlayCircleOutlined/>}
                            onClick={handleDetect}
                            loading={loading}
                        >
                            开始检测
                        </Button>
                    </Col>

                    <Col span={12}>
                        {originalImageUrl && (
                            <div style={{marginBottom: 16}}>
                                <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>原始图片</label>
                                <Image
                                    src={originalImageUrl}
                                    style={{maxWidth: '100%', maxHeight: 300, objectFit: 'contain'}}
                                />
                            </div>
                        )}
                        {resultImageUrl && (
                            <div>
                                <label style={{display: 'block', marginBottom: 8, fontWeight: 'bold'}}>检测结果</label>
                                <Image
                                    src={resultImageUrl}
                                    style={{maxWidth: '100%', maxHeight: 300, objectFit: 'contain'}}
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
                            <Statistic title="平均置信度" value={detectionResult.detections?.reduce((sum, d) => sum + d.confidence, 0) / (detectionResult.detections?.length || 1) || 0} precision={2}/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="最高置信度" value={Math.max(...(detectionResult.detections?.map(d => d.confidence) || [0]))} precision={2}/>
                        </Col>
                        <Col span={6}>
                            <Statistic title="最低置信度" value={Math.min(...(detectionResult.detections?.map(d => d.confidence) || [0]))} precision={2}/>
                        </Col>
                    </Row>

                    <div style={{marginTop: 24}}>
                        <label style={{display: 'block', marginBottom: 12, fontWeight: 'bold'}}>目标类别分布</label>
                        <div style={{display: 'flex', flexWrap: 'wrap', gap: 12}}>
                            {getClassDistribution().map(item => (
                                <Tag key={item.name} color="blue">
                                    {item.name}: {item.count}个
                                </Tag>
                            ))}
                        </div>
                    </div>

                    <div style={{marginTop: 24}}>
                        <label style={{display: 'block', marginBottom: 12, fontWeight: 'bold'}}>检测详情</label>
                        <div style={{maxHeight: 300, overflowY: 'auto'}}>
                            {detectionResult.detections?.map((d, idx) => (
                                <div key={idx} style={{padding: 8, borderBottom: '1px solid #f0f0f0'}}>
                                    <span style={{marginRight: 12}}>类别: {d.className}</span>
                                    <span style={{marginRight: 12}}>置信度: {d.confidence.toFixed(2)}</span>
                                    <span>位置: ({Math.round(d.x1)}, {Math.round(d.y1)}) - ({Math.round(d.x2)}, {Math.round(d.y2)})</span>
                                </div>
                            ))}
                        </div>
                    </div>
                </Card>
            )}
        </div>
    );
};

export default ImageDetection;
