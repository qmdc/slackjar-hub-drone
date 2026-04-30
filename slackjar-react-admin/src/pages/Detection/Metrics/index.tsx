import React, {useState, useEffect} from 'react';
import {Card, Statistic, Row, Col, Tag, Progress} from 'antd';
import {getDetectionHistory, type DetectionHistory} from '../../../apis/modules/detection';

const Metrics: React.FC = () => {
    const [history, setHistory] = useState<DetectionHistory[]>([]);

    useEffect(() => {
        loadHistory();
    }, []);

    const loadHistory = async () => {
        try {
            const res = await getDetectionHistory();
            if (res.code === 200 && res.data) {
                setHistory(res.data);
            }
        } catch (e) {
            console.error('加载历史失败');
        }
    };

    const stats = {
        totalTasks: history.length,
        successTasks: history.filter(h => h.status === 'SUCCESS').length,
        failedTasks: history.filter(h => h.status === 'FAILED').length,
        totalDetections: history.reduce((sum, h) => sum + (h.totalDetections || 0), 0),
        avgConfidence: history.length > 0 
            ? history.reduce((sum, h) => sum + (h.avgConfidence || 0), 0) / history.length 
            : 0,
        avgProcessTime: history.length > 0
            ? history.reduce((sum, h) => sum + (h.processTime || 0), 0) / history.length
            : 0,
        imageTasks: history.filter(h => h.taskType === 'IMAGE').length,
        videoTasks: history.filter(h => h.taskType === 'VIDEO').length,
    };

    const getClassDistribution = () => {
        const counts: Record<string, number> = {};
        history.forEach(h => {
            if (h.classDistribution) {
                try {
                    const dist = JSON.parse(h.classDistribution) as Record<string, number>;
                    Object.entries(dist).forEach(([name, count]) => {
                        counts[name] = (counts[name] || 0) + count;
                    });
                } catch (e) {}
            }
        });
        return Object.entries(counts).sort((a, b) => b[1] - a[1]);
    };

    const classDistribution = getClassDistribution();
    const maxClassCount = Math.max(...classDistribution.map(([, count]) => count), 1);

    const getConfidenceDistribution = () => {
        const ranges = {
            '0.0-0.2': 0,
            '0.2-0.4': 0,
            '0.4-0.6': 0,
            '0.6-0.8': 0,
            '0.8-1.0': 0,
        };

        history.forEach(h => {
            if (h.avgConfidence !== null && h.avgConfidence !== undefined) {
                const conf = h.avgConfidence;
                if (conf < 0.2) ranges['0.0-0.2']++;
                else if (conf < 0.4) ranges['0.2-0.4']++;
                else if (conf < 0.6) ranges['0.4-0.6']++;
                else if (conf < 0.8) ranges['0.6-0.8']++;
                else ranges['0.8-1.0']++;
            }
        });

        return Object.entries(ranges);
    };

    const confidenceDistribution = getConfidenceDistribution();
    const maxConfCount = Math.max(...confidenceDistribution.map(([, count]) => count), 1);

    return (
        <div style={{padding: 24}}>
            <Row gutter={16} style={{marginBottom: 24}}>
                <Col span={6}>
                    <Card>
                        <Statistic title="总任务数" value={stats.totalTasks}/>
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic title="成功任务" value={stats.successTasks} prefix={<Tag color="green">✓</Tag>}/>
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic title="失败任务" value={stats.failedTasks} prefix={<Tag color="red">✗</Tag>}/>
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic title="成功率" value={stats.totalTasks > 0 ? ((stats.successTasks / stats.totalTasks) * 100).toFixed(1) : '0'} suffix="%"/>
                    </Card>
                </Col>
            </Row>

            <Row gutter={16} style={{marginBottom: 24}}>
                <Col span={6}>
                    <Card>
                        <Statistic title="累计检测目标" value={stats.totalDetections}/>
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic title="平均置信度" value={stats.avgConfidence.toFixed(3)}/>
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic title="平均处理时间" value={stats.avgProcessTime.toFixed(0)} suffix="ms"/>
                    </Card>
                </Col>
                <Col span={6}>
                    <Card>
                        <Statistic title="图片检测" value={stats.imageTasks}/>
                    </Card>
                </Col>
            </Row>

            <Card title="目标类别分布" style={{marginBottom: 24}}>
                <div style={{display: 'flex', flexDirection: 'column', gap: 12}}>
                    {classDistribution.map(([name, count]) => (
                        <div key={name} style={{display: 'flex', alignItems: 'center', gap: 12}}>
                            <span style={{width: 100}}>{name}</span>
                            <div style={{flex: 1, height: 24, backgroundColor: '#f0f0f0', borderRadius: 4, overflow: 'hidden'}}>
                                <div 
                                    style={{
                                        height: '100%', 
                                        backgroundColor: '#1890ff', 
                                        width: `${(count / maxClassCount) * 100}%`,
                                        display: 'flex',
                                        alignItems: 'center',
                                        paddingLeft: 8
                                    }}
                                >
                                    <span style={{color: '#fff', fontSize: 12}}>{count}</span>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </Card>

            <Card title="置信度分布">
                <div style={{display: 'flex', flexDirection: 'column', gap: 12}}>
                    {confidenceDistribution.map(([range, count]) => (
                        <div key={range} style={{display: 'flex', alignItems: 'center', gap: 12}}>
                            <span style={{width: 80}}>{range}</span>
                            <div style={{flex: 1}}>
                                <Progress percent={(count / maxConfCount) * 100} showInfo={false}/>
                            </div>
                            <span style={{width: 30, textAlign: 'right'}}>{count}</span>
                        </div>
                    ))}
                </div>
            </Card>
        </div>
    );
};

export default Metrics;