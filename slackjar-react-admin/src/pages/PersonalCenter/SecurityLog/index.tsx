import React, {useCallback, useEffect, useState} from 'react';
import {Button, message, Modal, Pagination, Spin, Table, Tag, Typography} from 'antd';
import {
    CheckCircleOutlined,
    CloseCircleOutlined,
    DesktopOutlined,
    EnvironmentOutlined,
    MobileOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import {forceLogoutDevice, getUserDevices, pageQueryUserDevices, UserDevice, UserDevicePageQuery} from '../../../apis';
import type {PageResult} from '../../../apis';
import styles from './index.module.scss';
import globalStyles from '../../global.module.scss';

const {Text} = Typography;

const SecurityLog: React.FC = () => {
    const [devices, setDevices] = useState<UserDevice[]>([]);
    const [loadingDevices, setLoadingDevices] = useState(false);

    const [securityRecords, setSecurityRecords] = useState<UserDevice[]>([]);
    const [loadingRecords, setLoadingRecords] = useState(false);
    const [pagination, setPagination] = useState({
        current: 1,
        pageSize: 5,
        total: 0
    });

    const getDeviceIcon = (device: string) => {
        if (device === 'MOBILE') {
            return <MobileOutlined style={{fontSize: 24, color: '#8c8c8c'}}/>;
        }
        return <DesktopOutlined style={{fontSize: 24, color: '#8c8c8c'}}/>;
    };

    const getDeviceText = (device: string) => {
        const deviceMap: Record<string, string> = {
            PC: 'Windows',
            MAC: 'Mac',
            MOBILE: '手机'
        };
        return deviceMap[device] || device;
    };

    const getStatusText = (status: number) => {
        return status === 0 ? '有效' : '无效';
    };

    const fetchDevices = useCallback(async () => {
        setLoadingDevices(true);
        try {
            const result = await getUserDevices();
            if (result.code === 200 && result.data) {
                setDevices(result.data);
            } else {
                message.error(result.message || '获取设备列表失败');
            }
        } catch {
            message.error('获取设备列表失败');
        } finally {
            setLoadingDevices(false);
        }
    }, []);

    const fetchSecurityRecords = useCallback(async (pageNo: number, pageSize: number) => {
        setLoadingRecords(true);
        try {
            const query: UserDevicePageQuery = {
                pageNo,
                pageSize,
                sortBy: 'loginTime',
                sortOrder: 'desc'
            };
            const result = await pageQueryUserDevices(query);
            if (result.code === 200 && result.data) {
                const pageResult = result.data as PageResult<UserDevice>;
                setSecurityRecords(pageResult.list || []);
                setPagination({
                    current: pageResult.pageNo || pageNo,
                    pageSize: pageResult.pageSize || pageSize,
                    total: pageResult.total || 0
                });
            } else {
                message.error(result.message || '获取安全记录失败');
            }
        } catch {
            message.error('获取安全记录失败');
        } finally {
            setLoadingRecords(false);
        }
    }, []);

    const handleForceLogout = useCallback(async (deviceId: number, isCurrentDevice: boolean) => {
        if (isCurrentDevice) {
            message.warning('无法下线当前设备');
            return;
        }

        Modal.confirm({
            title: '确认下线',
            content: '下线该设备后，在该设备登录需要进行身份验证，是否继续？',
            onOk: async () => {
                try {
                    const result = await forceLogoutDevice(deviceId);
                    if (result.code === 200) {
                        message.success('下线成功');
                        await fetchDevices();
                        await fetchSecurityRecords(pagination.current, pagination.pageSize);
                    } else {
                        message.error(result.message || '下线失败');
                    }
                } catch {
                    message.error('下线失败');
                }
            }
        });
    }, [fetchDevices, fetchSecurityRecords, pagination]);

    const handlePageChange = useCallback((page: number, pageSize: number) => {
        fetchSecurityRecords(page, pageSize).then();
    }, [fetchSecurityRecords]);

    const handlePageSizeChange = useCallback((_current: number, size: number) => {
        fetchSecurityRecords(1, size).then();
    }, [fetchSecurityRecords]);

    useEffect(() => {
        fetchDevices().then();
        fetchSecurityRecords(1, 5).then();
    }, [fetchDevices, fetchSecurityRecords]);

    const recordColumns = [
        {
            title: '详情',
            dataIndex: 'device',
            key: 'device',
            render: (_: string, record: UserDevice) => (
                <div className={styles.recordDetail}>
                    <div className={styles.recordDevice}>
                        {getDeviceIcon(record.device)}
                        <Text strong>{getDeviceText(record.device)}</Text>
                    </div>
                    <Text type="secondary" className={styles.recordBrowser}>
                        {record.browser} on {record.os}
                    </Text>
                </div>
            )
        },
        {
            title: 'IP 地址',
            dataIndex: 'ipAddr',
            key: 'ipAddr',
            render: (ip: string) => ip || '-'
        },
        {
            title: '登录地址',
            dataIndex: 'location',
            key: 'location',
            render: (location: string) => location || '-'
        },
        {
            title: '登录时间',
            dataIndex: 'loginTime',
            key: 'loginTime',
            render: (time: string) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
        },
        {
            title: '登录状态',
            dataIndex: 'status',
            key: 'status',
            width: 100,
            render: (status: number) => (
                <Tag icon={status === 0 ? <CheckCircleOutlined/> : <CloseCircleOutlined/>}
                     color={status === 0 ? 'green' : 'default'}>
                    {getStatusText(status)}
                </Tag>
            )
        }
    ];

    return (
        <div className={`${globalStyles.scrollbar}`} style={{height: '100%', overflowY: 'auto', paddingBottom: 24}}>
            <div className={styles.securityLogContainer}>
                <div className={styles.section}>
                    <div className={styles.sectionHeader}>
                        <Text strong className={styles.sectionTitle}>登录设备</Text>
                    </div>
                    <Text type="secondary" className={styles.sectionDesc}>
                        下线列表中的设备后，在该设备需重新进行身份验证
                    </Text>

                    <Spin spinning={loadingDevices}>
                        {devices.length > 0 ? (
                            <div className={styles.deviceList}>
                                {devices.map((device) => (
                                    <div key={device.id} className={styles.deviceItem}>
                                        <div className={styles.deviceIcon}>
                                            {getDeviceIcon(device.device)}
                                        </div>
                                        <div className={styles.deviceInfo}>
                                            <div className={styles.deviceTop}>
                                                <span
                                                    className={styles.deviceLocation}>{device.location || '未知地区'}</span>
                                                <span className={styles.deviceIp}>{device.ipAddr}</span>
                                                {device.currentDevice && (
                                                    <Tag color="success" icon={<CheckCircleOutlined/>}
                                                         className={styles.currentTag}>
                                                        当前设备
                                                    </Tag>
                                                )}
                                            </div>
                                            <div className={styles.deviceBottom}>
                                                <Text type="secondary">
                                                    {device.browser} on {device.os} &nbsp;登录时间 {dayjs(device.loginTime).format('YYYY-MM-DD HH:mm:ss')}
                                                </Text>
                                            </div>
                                        </div>
                                        <div className={styles.deviceAction}>
                                            <Button
                                                size="small"
                                                disabled={device.currentDevice || device.status === 1}
                                                onClick={() => handleForceLogout(device.id, device.currentDevice)}
                                            >
                                                下线
                                            </Button>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className={styles.emptyTip}>暂无登录设备</div>
                        )}
                    </Spin>
                </div>

                <div className={styles.section}>
                    <div className={styles.sectionHeader}>
                        <Text strong className={styles.sectionTitle}>登录记录</Text>
                    </div>
                    <Table
                        columns={recordColumns}
                        dataSource={securityRecords}
                        rowKey="id"
                        loading={loadingRecords}
                        pagination={false}
                        locale={{emptyText: '暂无登录记录'}}
                    />
                    <div className={styles.paginationWrapper}>
                        <Pagination
                            current={pagination.current}
                            pageSize={pagination.pageSize}
                            total={pagination.total}
                            onChange={handlePageChange}
                            onShowSizeChange={handlePageSizeChange}
                            showSizeChanger
                            // showQuickJumper
                            showTotal={(total) => `共 ${total} 条`}
                            pageSizeOptions={['5', '10', '20']}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SecurityLog;
