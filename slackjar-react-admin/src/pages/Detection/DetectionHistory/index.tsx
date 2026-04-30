import React, {useState, useEffect} from 'react';
import {Button, Table, Card, message, Tag, Modal, Statistic, Row, Col} from 'antd';
import {DeleteOutlined, EyeOutlined, DownloadOutlined, RestOutlined} from '@ant-design/icons';
import {getDetectionHistory, deleteHistory, getDetectionStats, type DetectionHistory, type DetectionStats} from '../../../apis/modules/detection';

const DetectionHistory: React.FC = () => {
    const [historyList, setHistoryList] = useState<DetectionHistory[]>([]);
    const [loading, setLoading] = useState(false);
    const [selectedHistory, setSelectedHistory] = useState<DetectionHistory | null>(null);
    const [stats, setStats] = useState<DetectionStats | null>(null);
    const [modalVisible, setModalVisible] = useState(false);

    useEffect(() => {
        loadHistory();
    }, []);

    const loadHistory = async () => {
        setLoading(true);
        try {
            const res = await getDetectionHistory();
            if (res.code === 200 && res.data) {
                setHistoryList(res.data);
            }
        } catch (e) {
            message.error('加载检测历史失败');
        } finally {
            setLoading(false);
        }
    };

    const handleDelete = async (id: number) => {
        try {
            const res = await deleteHistory(id);
            if (res.code === 200) {
                message.success('删除成功');
                loadHistory();
            } else {
                message.error(res.message);
            }
        } catch (e) {
            message.error('删除失败');
        }
    };

    const handleView = async (history: DetectionHistory) => {
        setSelectedHistory(history);
        try {
            const res = await getDetectionStats(history.id);
            if (res.code === 200 && res.data) {
                setStats(res.data);
            }
        } catch (e) {
            message.error('获取统计数据失败');
        }
        setModalVisible(true);
    };

    const handleExport = (history: DetectionHistory) => {
        const data = {
            id: history.id,
            taskName: history.taskName,
            taskType: history.taskType,
            modelName: history.modelName,
            status: history.status,
            totalDetections: history.totalDetections,
            avgConfidence: history.avgConfidence,
            minConfidence: history.minConfidence,
            maxConfidence: history.maxConfidence,
            confThreshold: history.confThreshold,
            iouThreshold: history.iouThreshold,
            processTime: history.processTime,
            createTime: history.createTime,
            classDistribution: history.classDistribution,
            detectionResult: history.detectionResult
        };

        const blob = new Blob([JSON.stringify(data, null, 2)], {type: 'application/json'});
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `detection_result_${history.id}.json`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
        message.success('导出成功');
    };

    const columns = [
        {title: '任务名称', dataIndex: 'taskName', key: 'taskName'},
        {title: '任务类型', dataIndex: 'taskType', key: 'taskType', render: (text: string) => <Tag color={text === 'IMAGE' ? 'blue' : 'green'}>{text === 'IMAGE' ? '图片检测' : '视频检测'}</Tag>},
        {title: '模型名称', dataIndex: 'modelName', key: 'modelName'},
        {title: '检测目标数', dataIndex: 'totalDetections', key: 'totalDetections'},
        {title: '平均置信度', dataIndex: 'avgConfidence', key: 'avgConfidence', render: (val: number) => val?.toFixed(2)},
        {title: '状态', dataIndex: 'status', key: 'status', render: (text: string) => <Tag color={text === 'SUCCESS' ? 'green' : 'red'}>{text === 'SUCCESS' ? '成功' : '失败'}</Tag>},
        {title: '处理时间(ms)', dataIndex: 'processTime', key: 'processTime'},
        {title: '创建时间', dataIndex: 'createTime', key: 'createTime'},
        {
            title: '操作',
            key: 'action',
            render: (_: unknown, record: DetectionHistory) => (
                <div style={{display: 'flex', gap: 8}}>
                    <Button icon={<EyeOutlined/>} onClick={() => handleView(record)}>查看</Button>
                    <Button icon={<DownloadOutlined/>} onClick={() => handleExport(record)}>导出</Button>
                    <Button icon={<DeleteOutlined/>} onClick={() => handleDelete(record.id)} danger>删除</Button>
                </div>
            ),
        },
    ];

    return (
        <div style={{padding: 24}}>
            <Card title="检测历史记录" extra={
                <Button icon={<RestOutlined/>} onClick={loadHistory} loading={loading}>
                    刷新
                </Button>
            }>
                <Table
                    dataSource={historyList}
                    columns={columns}
                    rowKey="id"
                    loading={loading}
                    pagination={{pageSize: 10}}
                />
            </Card>

            <Modal
                title="检测详情"
                visible={modalVisible}
                onCancel={() => setModalVisible(false)}
                width={800}
            >
                {selectedHistory && (
                    <div>
                        <Row gutter={16} style={{marginBottom: 24}}>
                            <Col span={6}>
                                <Statistic title="检测目标数" value={selectedHistory.totalDetections}/>
                            </Col>
                            <Col span={6}>
                                <Statistic title="平均置信度" value={selectedHistory.avgConfidence || 0} precision={2}/>
                            </Col>
                            <Col span={6}>
                                <Statistic title="最低置信度" value={selectedHistory.minConfidence || 0} precision={2}/>
                            </Col>
                            <Col span={6}>
                                <Statistic title="最高置信度" value={selectedHistory.maxConfidence || 0} precision={2}/>
                            </Col>
                        </Row>

                        <div style={{marginBottom: 16}}>
                            <label style={{fontWeight: 'bold', marginRight: 8}}>任务类型:</label>
                            <Tag color={selectedHistory.taskType === 'IMAGE' ? 'blue' : 'green'}>
                                {selectedHistory.taskType === 'IMAGE' ? '图片检测' : '视频检测'}
                            </Tag>
                        </div>

                        <div style={{marginBottom: 16}}>
                            <label style={{fontWeight: 'bold', marginRight: 8}}>模型名称:</label>
                            {selectedHistory.modelName}
                        </div>

                        <div style={{marginBottom: 16}}>
                            <label style={{fontWeight: 'bold', marginRight: 8}}>参数配置:</label>
                            Conf={selectedHistory.confThreshold}, IoU={selectedHistory.iouThreshold}
                        </div>

                        {stats?.classCounts && (
                            <div style={{marginBottom: 16}}>
                                <label style={{fontWeight: 'bold', display: 'block', marginBottom: 8}}>类别分布:</label>
                                <div style={{display: 'flex', flexWrap: 'wrap', gap: 8}}>
                                    {Object.entries(stats.classCounts).map(([name, count]) => (
                                        <Tag key={name} color="blue">
                                            {name}: {count}
                                        </Tag>
                                    ))}
                                </div>
                            </div>
                        )}

                        {selectedHistory.outputPath && (
                            <div>
                                <label style={{fontWeight: 'bold', display: 'block', marginBottom: 8}}>结果文件:</label>
                                <a href={`/api/sys-file/local/download?filePath=${selectedHistory.outputPath}`} target="_blank">
                                    下载结果文件
                                </a>
                            </div>
                        )}
                    </div>
                )}
            </Modal>
        </div>
    );
};

export default DetectionHistory;