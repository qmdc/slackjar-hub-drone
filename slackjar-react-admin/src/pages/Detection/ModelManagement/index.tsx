import React, {useState, useEffect} from 'react';
import {Button, Card, Table, message, Tag, Statistic, Row, Col} from 'antd';
import {RestOutlined, EyeOutlined} from '@ant-design/icons';
import {listModels, type ModelInfo} from '../../../apis/modules/detection';
import {getDetectionHistory, type DetectionHistory} from '../../../apis/modules/detection';

const ModelManagement: React.FC = () => {
    const [models, setModels] = useState<ModelInfo[]>([]);
    const [history, setHistory] = useState<DetectionHistory[]>([]);
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        loadData();
    }, []);

    const loadData = async () => {
        setLoading(true);
        try {
            const [modelsRes, historyRes] = await Promise.all([listModels(), getDetectionHistory()]);
            
            if (modelsRes.code === 200 && modelsRes.data) {
                setModels(modelsRes.data);
            }
            
            if (historyRes.code === 200 && historyRes.data) {
                setHistory(historyRes.data);
            }
        } catch (e) {
            message.error('加载数据失败');
        } finally {
            setLoading(false);
        }
    };

    const getModelUsage = (modelName: string) => {
        const usage = history.filter(h => h.modelName === modelName);
        const successCount = usage.filter(h => h.status === 'SUCCESS').length;
        const totalDetections = usage.reduce((sum, h) => sum + (h.totalDetections || 0), 0);
        return {count: usage.length, successCount, totalDetections};
    };

    const columns = [
        {title: '模型名称', dataIndex: 'name', key: 'name'},
        {title: '模型类型', dataIndex: 'type', key: 'type', render: (text: string) => <Tag color="blue">{text}</Tag>},
        {title: '文件路径', dataIndex: 'path', key: 'path', render: (text: string) => <span style={{maxWidth: 300, overflow: 'hidden', textOverflow: 'ellipsis', display: 'inline-block'}}>{text}</span>},
        {
            title: '使用次数',
            key: 'usage',
            render: (_: unknown, record: ModelInfo) => {
                const usage = getModelUsage(record.name);
                return (
                    <div>
                        <div>总次数: {usage.count}</div>
                        <div>成功: {usage.successCount}</div>
                        <div>检测目标: {usage.totalDetections}</div>
                    </div>
                );
            }
        },
        {
            title: '操作',
            key: 'action',
            render: (_: unknown, record: ModelInfo) => (
                <Button icon={<EyeOutlined/>} onClick={() => message.info(`模型: ${record.name}\n路径: ${record.path}`)}>
                    详情
                </Button>
            ),
        },
    ];

    const totalStats = {
        totalModels: models.length,
        totalUsage: history.length,
        totalDetections: history.reduce((sum, h) => sum + (h.totalDetections || 0), 0),
        successRate: history.length > 0 ? (history.filter(h => h.status === 'SUCCESS').length / history.length * 100).toFixed(1) : '0'
    };

    return (
        <div style={{padding: 24}}>
            <Card title="模型管理" extra={
                <Button icon={<RestOutlined/>} onClick={loadData} loading={loading}>
                    刷新
                </Button>
            } style={{marginBottom: 24}}>
                <Row gutter={16} style={{marginBottom: 24}}>
                    <Col span={6}>
                        <Statistic title="模型总数" value={totalStats.totalModels}/>
                    </Col>
                    <Col span={6}>
                        <Statistic title="检测任务总数" value={totalStats.totalUsage}/>
                    </Col>
                    <Col span={6}>
                        <Statistic title="累计检测目标" value={totalStats.totalDetections}/>
                    </Col>
                    <Col span={6}>
                        <Statistic title="成功率" value={totalStats.successRate} suffix="%"/>
                    </Col>
                </Row>

                <Table
                    dataSource={models}
                    columns={columns}
                    rowKey="name"
                    loading={loading}
                    pagination={false}
                />
            </Card>

            <Card title="模型使用排行">
                <div style={{display: 'flex', gap: 16, flexWrap: 'wrap'}}>
                    {models.map(model => {
                        const usage = getModelUsage(model.name);
                        return (
                            <Card key={model.name} bordered={false} style={{width: 240}}>
                                <div style={{fontWeight: 'bold', marginBottom: 8}}>{model.name}</div>
                                <div>使用次数: {usage.count}</div>
                                <div>成功次数: {usage.successCount}</div>
                                <div>检测目标: {usage.totalDetections}</div>
                                <div style={{marginTop: 8}}>
                                    <div style={{height: 8, backgroundColor: '#f0f0f0', borderRadius: 4}}>
                                        <div 
                                            style={{
                                                height: '100%', 
                                                backgroundColor: '#1890ff', 
                                                borderRadius: 4,
                                                width: `${usage.count > 0 ? (usage.successCount / usage.count * 100) : 0}%`
                                            }}
                                        />
                                    </div>
                                    <div style={{textAlign: 'right', fontSize: 12, color: '#666', marginTop: 4}}>
                                        成功率: {usage.count > 0 ? ((usage.successCount / usage.count) * 100).toFixed(0) : 0}%
                                    </div>
                                </div>
                            </Card>
                        );
                    })}
                </div>
            </Card>
        </div>
    );
};

export default ModelManagement;