import React, {useCallback, useEffect, useState} from 'react';
import {message} from 'antd';
import type {RoleItem, RolePageQuery, RoleRequest} from '../../../apis';
import {pageQueryRoles, saveRole} from '../../../apis';
import type {PageResult} from '../../../apis';
import RoleSearchBar from './RoleSearchBar';
import RoleTable from './RoleTable';
import RoleFormModal from './RoleFormModal';
import AssignPermModal from './AssignPermModal';
import AuthorizeUserModal from './AuthorizeUserModal';
import globalStyles from '../../global.module.scss';
import styles from './index.module.scss';

const RoleManage: React.FC = () => {
    const [query, setQuery] = useState<RolePageQuery>({pageNo: 1, pageSize: 10, sortOrder: 'sortOrder', sortBy: 'asc'});
    const [roles, setRoles] = useState<RoleItem[]>([]);
    const [loading, setLoading] = useState(false);
    const [pagination, setPagination] = useState({current: 1, pageSize: 10, total: 0});

    const [formModalOpen, setFormModalOpen] = useState(false);
    const [editData, setEditData] = useState<RoleItem | null>(null);
    const [formLoading, setFormLoading] = useState(false);

    const [assignPermModalOpen, setAssignPermModalOpen] = useState(false);
    const [assignPermRoleId, setAssignPermRoleId] = useState<number | null>(null);

    const [authorizeUserModalOpen, setAuthorizeUserModalOpen] = useState(false);
    const [authorizeUserRoleId, setAuthorizeUserRoleId] = useState<number | null>(null);

    const fetchRoles = useCallback((searchQuery?: RolePageQuery) => {
        const q = searchQuery || query;
        setLoading(true);
        pageQueryRoles(q)
            .then((res) => {
                if (res.code === 200 && res.data) {
                    const pageResult = res.data as PageResult<RoleItem>;
                    setRoles(pageResult.list || []);
                    setPagination({
                        current: pageResult.pageNo || q.pageNo || 1,
                        pageSize: pageResult.pageSize || q.pageSize || 10,
                        total: pageResult.total || 0,
                    });
                }
            })
            .finally(() => setLoading(false));
    }, [query]);

    useEffect(() => {
        fetchRoles();
    }, []);

    const handleSearch = () => {
        const newQuery = {...query, pageNo: 1};
        setQuery(newQuery);
        fetchRoles(newQuery);
    };

    const handleReset = () => {
        const resetQuery = {
            pageNo: 1,
            pageSize: 10,
            sortOrder: 'sortOrder',
            sortBy: 'asc',
            roleName: undefined,
            roleCode: undefined,
            status: undefined
        };
        setQuery(resetQuery);
        fetchRoles(resetQuery);
    };

    const handlePageChange = (page: number, pageSize: number) => {
        const newQuery = {...query, pageNo: page, pageSize};
        setQuery(newQuery);
        fetchRoles(newQuery);
    };

    const handleAdd = () => {
        setEditData(null);
        setFormModalOpen(true);
    };

    const handleEdit = (record: RoleItem) => {
        setEditData(record);
        setFormModalOpen(true);
    };

    const handleFormOk = async (values: RoleRequest) => {
        setFormLoading(true);
        try {
            const res = await saveRole(values);
            if (res.code === 200) {
                message.success(values.id ? '修改成功' : '创建成功');
                setFormModalOpen(false);
                fetchRoles();
            } else {
                message.error(res.message || '操作失败');
            }
        } catch {
            message.error('操作失败');
        } finally {
            setFormLoading(false);
        }
    };

    const handleAssignPerm = (roleId: number) => {
        setAssignPermRoleId(roleId);
        setAssignPermModalOpen(true);
    };

    const handleAuthorizeUser = (roleId: number) => {
        setAuthorizeUserRoleId(roleId);
        setAuthorizeUserModalOpen(true);
    };

    return (
        <div className={`${globalStyles.pageContainer} ${styles.rolePage}`}>
            <RoleSearchBar
                query={query}
                onQueryChange={setQuery}
                onSearch={handleSearch}
                onReset={handleReset}
                onAdd={handleAdd}
            />
            <RoleTable
                dataSource={roles}
                loading={loading}
                pagination={pagination}
                onPageChange={handlePageChange}
                onEdit={handleEdit}
                onAssignPerm={handleAssignPerm}
                onAuthorizeUser={handleAuthorizeUser}
                onRefresh={() => fetchRoles()}
            />
            <RoleFormModal
                open={formModalOpen}
                editData={editData}
                onCancel={() => setFormModalOpen(false)}
                onOk={handleFormOk}
                confirmLoading={formLoading}
            />
            <AssignPermModal
                open={assignPermModalOpen}
                roleId={assignPermRoleId}
                onCancel={() => setAssignPermModalOpen(false)}
                onSuccess={() => {
                    setAssignPermModalOpen(false);
                    fetchRoles();
                }}
            />
            <AuthorizeUserModal
                open={authorizeUserModalOpen}
                roleId={authorizeUserRoleId}
                onCancel={() => setAuthorizeUserModalOpen(false)}
            />
        </div>
    );
};

export default RoleManage;
