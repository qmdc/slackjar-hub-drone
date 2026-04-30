import React from 'react';
import {Button, Card, message, Popconfirm, Table, Tag, Tooltip} from 'antd';
import type {ColumnsType} from 'antd/es/table';
import type {RoleItem} from '../../../apis';
import {deleteRole} from '../../../apis';
import globalStyles from '../../global.module.scss';
import styles from './index.module.scss';

interface RoleTableProps {
    dataSource: RoleItem[];
    loading: boolean;
    pagination: {
        current: number;
        pageSize: number;
        total: number;
    };
    onPageChange: (page: number, pageSize: number) => void;
    onEdit: (record: RoleItem) => void;
    onAssignPerm: (roleId: number) => void;
    onAuthorizeUser: (roleId: number) => void;
    onRefresh: () => void;
}

const RoleTable: React.FC<RoleTableProps> = ({
                                                 dataSource,
                                                 loading,
                                                 pagination,
                                                 onPageChange,
                                                 onEdit,
                                                 onAssignPerm,
                                                 onAuthorizeUser,
                                                 onRefresh,
                                             }) => {
    const handleDelete = async (id: number) => {
        try {
            const res = await deleteRole(id);
            if (res.code === 200) {
                message.success('删除成功');
                onRefresh();
            } else {
                message.error(res.message || '删除失败');
            }
        } catch {
            message.error('删除失败');
        }
    };

    const columns: ColumnsType<RoleItem> = [
        {
            title: '角色名称',
            dataIndex: 'roleName',
            key: 'roleName',
            width: 120,
        },
        {
            title: '角色编码',
            dataIndex: 'roleCode',
            key: 'roleCode',
            width: 160,
        },
        {
            title: '描述',
            dataIndex: 'description',
            key: 'description',
            width: 200,
            ellipsis: {
                showTitle: false,
            },
            render: (text: string) => (
                <Tooltip title={text || ''} placement="topLeft" autoAdjustOverflow>
                    <span style={{cursor: 'pointer'}}>{text || '-'}</span>
                </Tooltip>
            ),
        },
        {
            title: '类型',
            dataIndex: 'roleType',
            key: 'roleType',
            width: 80,
            render: (type: number) => (
                <Tag color={type === 1 ? 'blue' : 'default'}>
                    {type === 1 ? '系统角色' : '自定义角色'}
                </Tag>
            ),
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 60,
            render: (status: number) => (
                <Tag color={status === 0 ? 'green' : 'default'}>
                    {status === 0 ? '启用' : '禁用'}
                </Tag>
            ),
        },
        {
            title: '排序',
            dataIndex: 'sortOrder',
            key: 'sortOrder',
            width: 50,
        },
        {
            title: '操作',
            key: 'action',
            width: 170,
            render: (_, record) => (
                <div style={{display: 'flex', gap: 4}}>
                    <Button
                        type="link"
                        size="small"
                        className={styles.actionBtn}
                        onClick={() => onEdit(record)}
                    >
                        编辑
                    </Button>
                    <Button
                        type="link"
                        size="small"
                        className={styles.actionBtn}
                        onClick={() => onAssignPerm(record.id)}
                    >
                        分配权限
                    </Button>
                    <Button
                        type="link"
                        size="small"
                        className={styles.actionBtn}
                        onClick={() => onAuthorizeUser(record.id)}
                    >
                        授权用户
                    </Button>
                    <Popconfirm
                        title="确定删除该角色？"
                        onConfirm={() => handleDelete(record.id)}
                        okText="确定"
                        cancelText="取消"
                    >
                        <Button
                            type="link"
                            size="small"
                            danger
                            className={styles.actionBtn}
                        >
                            删除
                        </Button>
                    </Popconfirm>
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
            <Table<RoleItem>
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

export default RoleTable;
