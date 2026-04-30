import React, {useCallback, useEffect, useState} from 'react';
import {Button, Modal, Table, Tag} from 'antd';
import type {ColumnsType} from 'antd/es/table';
import type {SysUser, RoleUserPageQuery} from '../../../apis';
import {pageQueryRoleUsers} from '../../../apis';
import type {PageResult} from '../../../apis';

interface AuthorizeUserModalProps {
    open: boolean;
    roleId: number | null;
    onCancel: () => void;
}

const AuthorizeUserModal: React.FC<AuthorizeUserModalProps> = ({open, roleId, onCancel}) => {
    const [users, setUsers] = useState<SysUser[]>([]);
    const [loading, setLoading] = useState(false);
    const [pagination, setPagination] = useState({current: 1, pageSize: 10, total: 0});

    const fetchUsers = useCallback((pageNo?: number, pageSize?: number) => {
        if (!roleId) return;
        setLoading(true);
        const query: RoleUserPageQuery = {
            pageNo: pageNo || pagination.current,
            pageSize: pageSize || pagination.pageSize,
        };
        pageQueryRoleUsers(roleId, query)
            .then((res) => {
                if (res.code === 200 && res.data) {
                    const pageResult = res.data as PageResult<SysUser>;
                    setUsers(pageResult.list || []);
                    setPagination({
                        current: pageResult.pageNo || query.pageNo || 1,
                        pageSize: pageResult.pageSize || query.pageSize || 10,
                        total: pageResult.total || 0,
                    });
                }
            })
            .finally(() => setLoading(false));
    }, [roleId, pagination.current, pagination.pageSize]);

    useEffect(() => {
        if (open && roleId) {
            setPagination({current: 1, pageSize: 10, total: 0});
            fetchUsers(1, 10);
        } else {
            setUsers([]);
        }
    }, [open, roleId]);

    const handlePageChange = (page: number, size: number) => {
        fetchUsers(page, size);
    };

    const columns: ColumnsType<SysUser> = [
        {
            title: '用户名',
            dataIndex: 'username',
            key: 'username',
            width: 100,
        },
        {
            title: '昵称',
            dataIndex: 'nickname',
            key: 'nickname',
            width: 130,
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
            width: 110,
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
    ];

    return (
        <Modal
            title="授权用户列表"
            open={open}
            onCancel={onCancel}
            footer={[
                <Button key="close" onClick={onCancel}>
                    关闭
                </Button>,
            ]}
            width={800}
            destroyOnClose
        >
            <Table<SysUser>
                columns={columns}
                dataSource={users}
                rowKey="id"
                loading={loading}
                pagination={{
                    current: pagination.current,
                    pageSize: pagination.pageSize,
                    total: pagination.total,
                    onChange: handlePageChange,
                    showSizeChanger: true,
                    showTotal: (total) => `共 ${total} 条`,
                    pageSizeOptions: ['10', '20', '50'],
                }}
            />
        </Modal>
    );
};

export default AuthorizeUserModal;
