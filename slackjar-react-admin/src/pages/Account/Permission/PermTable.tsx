import React from 'react';
import {Button, Card, message, Popconfirm, Table, Tag, Tooltip} from 'antd';
import type {ColumnsType} from 'antd/es/table';
import type {PermissionResponse} from '../../../apis';
import {deletePermission} from '../../../apis';
import globalStyles from '../../global.module.scss';
import styles from './index.module.scss';

interface PermTableProps {
    dataSource: PermissionResponse[];
    loading: boolean;
    onEdit: (record: PermissionResponse) => void;
    onAssignRole: (permissionId: number) => void;
    onRefresh: () => void;
}

const permTypeMap: Record<number, { label: string; color: string }> = {
    1: {label: '菜单', color: 'blue'},
    2: {label: '按钮', color: 'orange'},
    3: {label: '接口', color: 'purple'},
};

const PermTable: React.FC<PermTableProps> = ({
                                                 dataSource,
                                                 loading,
                                                 onEdit,
                                                 onAssignRole,
                                                 onRefresh,
                                             }) => {
    const handleDelete = async (id: number) => {
        try {
            const res = await deletePermission(id);
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

    const columns: ColumnsType<PermissionResponse> = [
        {
            title: '权限名称',
            dataIndex: 'permissionName',
            key: 'permissionName',
            width: 200,
        },
        {
            title: '权限编码',
            dataIndex: 'permissionCode',
            key: 'permissionCode',
            width: 200,
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
            dataIndex: 'permissionType',
            key: 'permissionType',
            width: 80,
            render: (type: number) => {
                const info = permTypeMap[type] || {label: '未知', color: 'default'};
                return <Tag color={info.color}>{info.label}</Tag>;
            },
        },
        {
            title: '排序',
            dataIndex: 'sortOrder',
            key: 'sortOrder',
            width: 70,
        },
        {
            title: '操作',
            key: 'action',
            width: 180,
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
                        onClick={() => onAssignRole(record.id)}
                    >
                        分配角色
                    </Button>
                    <Popconfirm
                        title="确定删除该权限？"
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
            <Table<PermissionResponse>
                className={globalStyles.table}
                columns={columns}
                dataSource={dataSource}
                rowKey="id"
                loading={loading}
                pagination={false}
                scroll={{y: 'calc(100vh - 200px)'}}
                defaultExpandAllRows
            />
        </Card>
    );
};

export default PermTable;
