import React, {useState} from 'react';
import {Button, Card, message, Popconfirm, Switch, Table, Tag, Tooltip} from 'antd';
import type {ColumnsType} from 'antd/es/table';
import dayjs from 'dayjs';
import type {UserInfo} from '../../../apis';
import {updateUserStatus} from '../../../apis';
import globalStyles from '../../global.module.scss';
import styles from './index.module.scss';

interface UserTableProps {
    dataSource: UserInfo[];
    loading: boolean;
    pagination: {
        current: number;
        pageSize: number;
        total: number;
    };
    onPageChange: (page: number, pageSize: number) => void;
    onAssignRole: (userId: number) => void;
    onRefresh: () => void;
}

const UserTable: React.FC<UserTableProps> = ({
                                                 dataSource,
                                                 loading,
                                                 pagination,
                                                 onPageChange,
                                                 onAssignRole,
                                                 onRefresh
                                             }) => {
    const [changingStatus, setChangingStatus] = useState<number | null>(null);
    const handleStatusChange = async (userId: number, checked: boolean) => {
        setChangingStatus(userId);
        const status = checked ? 0 : 1;
        const action = checked ? '启用' : '禁用';
        try {
            const res = await updateUserStatus(userId, status);
            if (res.code === 200) {
                message.success(`${action}成功`);
                onRefresh();
            } else {
                message.error(res.message || '操作失败');
            }
        } catch {
            message.error('操作失败');
        } finally {
            setChangingStatus(null);
        }
    };

    const columns: ColumnsType<UserInfo> = [
        {
            title: '用户名',
            dataIndex: 'username',
            key: 'username',
            width: 120,
        },
        {
            title: '昵称',
            dataIndex: 'nickname',
            key: 'nickname',
            width: 120,
        },
        {
            title: '邮箱',
            dataIndex: 'email',
            key: 'email',
            width: 140,
            render: (email: string) => email || '-',
        },
        {
            title: '手机号',
            dataIndex: 'phone',
            key: 'phone',
            width: 100,
            render: (phone: string) => phone || '-',
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 60,
            render: (status: number) => (
                <Tag color={status === 0 ? 'green' : 'default'}>
                    {status === 0 ? '正常' : '禁用'}
                </Tag>
            ),
        },
        {
            title: '创建时间',
            dataIndex: 'createTime',
            key: 'createTime',
            width: 140,
            render: (time: number) => time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-',
        },
        {
            title: '操作',
            key: 'action',
            width: 100,
            render: (_, record) => (
                <div style={{display: 'flex', alignItems: 'center', gap: 8}}>
                    <Tooltip title={record.status === 0 ? '点击禁用' : '点击启用'} placement="top" autoAdjustOverflow>
                        <Switch
                            size="small"
                            checked={record.status === 0}
                            onChange={(checked) => handleStatusChange(record.id, checked)}
                            loading={changingStatus === record.id}
                        />
                    </Tooltip>
                    <Button
                        type="link"
                        size="small"
                        className={styles.actionBtn}
                        onClick={() => onAssignRole(record.id)}
                    >
                        分配角色
                    </Button>
                </div>
            ),
        },
    ];

    return (
        <Card
            className={globalStyles.tableCard}
            styles={{
                body: {
                    padding: '0'
                }
            }}
        >
            <Table<UserInfo>
                className={globalStyles.table}
                columns={columns}
                dataSource={dataSource}
                rowKey="id"
                loading={loading}
                pagination={{
                    current: pagination.current,
                    pageSize: pagination.pageSize,
                    total: pagination.total,
                    onChange: onPageChange,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                    pageSizeOptions: ['10', '20', '50'],
                }}
                scroll={{y: 'calc(100vh - 250px)'}}
            />
        </Card>
    );
};

export default UserTable;
