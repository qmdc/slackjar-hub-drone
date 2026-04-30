import React, {useState, useEffect} from 'react';
import {Button, Upload, InputNumber, Select, Card, message, Tag, Statistic, Row, Col, Progress, Space} from 'antd';
import {UploadOutlined, PlayCircleOutlined, RestOutlined, UpCircleOutlined} from '@ant-design/icons';
import type {UploadProps} from 'antd';
import {listModels, detectVideo, type ModelInfo, type DetectionResult} from '../../../apis/modules/detection';

interface ChannelState {
    uploadedFile: string;
    originalUrl: string;
    resultUrl: string;
    selectedModel: string;
    confThreshold: number;
    iouThreshold: number;
    detectionResult: DetectionResult | null;
    loading: boolean;
    progress: number;
}

const MultiVideoDetection: React.FC = () => {
    const [models, setModels] = useState<ModelInfo[]>([]);
    const [channels, setChannels] = useState<ChannelState[]>([
        {uploadedFile: '', originalUrl: '', resultUrl: '', selectedModel: '', confThreshold: 0.25, iouThreshold: 0.45, detectionResult: null, loading: false, progress: 0},
        {uploadedFile: '', originalUrl: '', resultUrl: '', selectedModel: '', confThreshold: 0.25, iouThreshold: 0.45, detectionResult: null, loading: false, progress: 0},
        {uploadedFile: '', originalUrl: '', resultUrl: '', selectedModel: '', confThreshold: 0.25, iouThreshold: 0.45, detectionResult: null, loading: false, progress: 0},
        {uploadedFile: '', originalUrl: '', resultUrl: '', selectedModel: '', confThreshold: 0.25, iouThreshold: 0.45, detectionResult: null, loading: false, progress: 0},
    ]);

    useEffect(() => {
        loadModels();
    }, []);

    const loadModels = async () => {
        try {
            const res = await listModels();
            if (res.code === 200 && res.data) {
                const data = res.data;
                setModels(data);
                setChannels(prev => prev.map(ch => ({...ch, selectedModel: data[0]?.name || ''})));
            }
        } catch (e) {
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
                    resultUrl: '',
                    detectionResult: null
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

        setChannels(prev => prev.map((ch, i) => i === index ? {...ch, loading: true, progress: 0} : ch));

        const progressInterval = setInterval(() => {
            setChannels(prev => prev.map((ch, i) => i === index ? {...ch, progress: Math.min(ch.progress + 5, 95)} : ch));
        }, 300);

        try {
            const res = await detectVideo({
                filePath: channel.uploadedFile,
                modelName: channel.selectedModel,
                confThreshold: channel.confThreshold,
                iouThreshold: channel.iouThreshold,
            });

            clearInterval(progressInterval);
            setChannels(prev => prev.map((ch, i) => i === index ? {...ch, progress: 100, loading: false} : ch));

            if (res.code === 200 && res.data) {
                const result = res.data;
                setChannels(prev => prev.map((ch, i) => i === index ? {
                    ...ch,
                    detectionResult: result,
                    resultUrl: result.success && result.outputPath ? `/api/sys-file/local/download?filePath=${result.outputPath}` : ''
                } : ch));
                if (!result.success) {
                    message.error(`通道${index + 1}检测失败: ${result.message}`);
                }
            }
        } catch (e) {
            clearInterval(progressInterval);
            setChannels(prev => prev.map((ch, i) => i === index ? {...ch, loading: false} : ch));
            message.error(`通道${index + 1}检测失败`);
        }
    };

    const handleDetectAll = async () => {
        const validChannels = channels.filter(ch => ch.uploadedFile && ch.selectedModel);
        if (validChannels.length === 0) {
            message.warning('请至少配置一个通道');
            return;
        }

        for (let i = 0; i < channels.length; i++) {
            if (channels[i].uploadedFile && channels[i].selectedModel) {
                await handleDetectChannel(i);
            }
        }
    };

    const resetChannel = (index: number) => {
        setChannels(prev => prev.map((ch, i) => i === index ? {
            ...ch,
            uploadedFile: '',
            originalUrl: '',
            resultUrl: '',
            detectionResult: null,
            progress: 0
        } : ch));
    };

    const getAllStats = () => {
        let totalDetections = 0;
        let totalFrames = 0;
        const classCounts: Record<string, number> = {};

        channels.forEach(ch => {
            if (ch.detectionResult?.success && ch.detectionResult.detections) {
                totalDetections += ch.detectionResult.detections.length;
                totalFrames += ch.detectionResult.totalFrames || 0;
                ch.detectionResult.detections.forEach(d => {
                    classCounts[d.className] = (classCounts[d.className] || 0) + 1;
                });
            }
        });

        return {totalDetections, totalFrames, classCounts};
    };

    const stats = getAllStats();

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
                            <div style={{marginBottom: 12}}>
                                <Upload
                                    name="file"
                                    action="/api/sys-file/upload?bizType=video"
                                    onChange={handleFileUpload(index)}
                                    accept="video/*"
                                    showUploadList={false}
                                >
                                    <Button icon={<UploadOutlined/>}>上传视频</Button>
                                </Upload>
                                <Button
                                    icon={<UpCircleOutlined/>}
                                    onClick={() => resetChannel(index)}
                                    style={{marginLeft: 8}}
                                    disabled={!channel.uploadedFile}
                                >
                                    重置
                                </Button>
                            </div>

                            <div style={{marginBottom: 12}}>
                                <Select
                                    value={channel.selectedModel}
                                    onChange={(value) => setChannels(prev => prev.map((ch, i) => i === index ? {...ch, selectedModel: value} : ch))}
                                    style={{width: '100%'}}
                                    options={models.map(m => ({label: m.name, value: m.name}))}
                                />
                            </div>

                            <Row gutter={8} style={{marginBottom: 12}}>
                                <Col span={11}>
                                    <InputNumber
                                        value={channel.confThreshold}
                                        onChange={(value) => setChannels(prev => prev.map((ch, i) => i === index ? {...ch, confThreshold: value || 0.25} : ch))}
                                        min={0.01}
                                        max={1}
                                        step={0.01}
                                        style={{width: '100%'}}
                                        placeholder="Conf"
                                    />
                                </Col>
                                <Col span={11}>
                                    <InputNumber
                                        value={channel.iouThreshold}
                                        onChange={(value) => setChannels(prev => prev.map((ch, i) => i === index ? {...ch, iouThreshold: value || 0.45} : ch))}
                                        min={0.01}
                                        max={1}
                                        step={0.01}
                                        style={{width: '100%'}}
                                        placeholder="IoU"
                                    />
                                </Col>
                            </Row>

                            <Button
                                type="primary"
                                icon={channel.loading ? <RestOutlined spin/> : <PlayCircleOutlined/>}
                                onClick={() => handleDetectChannel(index)}
                                loading={channel.loading}
                                disabled={!channel.uploadedFile || !channel.selectedModel}
                            >
                                开始检测
                            </Button>

                            {channel.loading && (
                                <Progress percent={channel.progress} status="active" style={{marginTop: 12}}/>
                            )}

                            {channel.originalUrl && (
                                <div style={{marginTop: 12}}>
                                    <video
                                        src={channel.originalUrl}
                                        controls
                                        style={{maxWidth: '100%', maxHeight: 150}}
                                    />
                                </div>
                            )}

                            {channel.resultUrl && (
                                <div style={{marginTop: 12}}>
                                    <video
                                        src={channel.resultUrl}
                                        controls
                                        style={{maxWidth: '100%', maxHeight: 150}}
                                    />
                                </div>
                            )}

                            {channel.detectionResult && (
                                <div style={{marginTop: 12}}>
                                    <Tag color={channel.detectionResult.success ? 'green' : 'red'}>
                                        {channel.detectionResult.success ? '检测完成' : '检测失败'}
                                    </Tag>
                                    <span style={{marginLeft: 8}}>
                                        检测目标: {channel.detectionResult.detections?.length || 0}个
                                    </span>
                                </div>
                            )}
                        </Card>
                    ))}
                </div>
            </Card>

            <Card title="统计摘要">
                <Row gutter={16}>
                    <Col span={6}>
                        <Statistic title="总检测目标数" value={stats.totalDetections}/>
                    </Col>
                    <Col span={6}>
                        <Statistic title="总处理帧数" value={stats.totalFrames}/>
                    </Col>
                    <Col span={6}>
                        <Statistic title="活跃通道" value={channels.filter(ch => ch.detectionResult?.success).length}/>
                    </Col>
                    <Col span={6}>
                        <Statistic title="检测类别数" value={Object.keys(stats.classCounts).length}/>
                    </Col>
                </Row>

                <div style={{marginTop: 24}}>
                    <label style={{display: 'block', marginBottom: 12, fontWeight: 'bold'}}>目标类别分布</label>
                    <div style={{display: 'flex', flexWrap: 'wrap', gap: 12}}>
                        {Object.entries(stats.classCounts).map(([name, count]) => (
                            <Tag key={name} color="purple">
                                {name}: {count}个
                            </Tag>
                        ))}
                    </div>
                </div>
            </Card>
        </div>
    );
};

export default MultiVideoDetection;