import React, {useCallback, useEffect, useState} from 'react';
import {message} from 'antd';
import type {PermissionPageQuery, PermissionRequest, PermissionResponse} from '../../../apis';
import {getPermissionTree, savePermission} from '../../../apis';
import PermSearchBar from './PermSearchBar';
import PermTable from './PermTable';
import PermFormModal from './PermFormModal';
import AssignRoleModal from './AssignRoleModal';
import globalStyles from '../../global.module.scss';
import styles from './index.module.scss';

const filterTree = (
    data: PermissionResponse[],
    query: PermissionPageQuery
): PermissionResponse[] => {
    const {permissionName, permissionCode, permissionType} = query;

    if (!permissionName && !permissionCode && !permissionType) {
        return data;
    }

    const filterNode = (node: PermissionResponse): PermissionResponse | null => {
        let match = true;

        if (permissionName && !node.permissionName.toLowerCase().includes(permissionName.toLowerCase())) {
            match = false;
        }
        if (permissionCode && !node.permissionCode.toLowerCase().includes(permissionCode.toLowerCase())) {
            match = false;
        }
        if (permissionType !== undefined && permissionType !== null && node.permissionType !== Number(permissionType)) {
            match = false;
        }
        const filteredChildren = node.children
            ? node.children
                .map((child) => filterNode(child))
                .filter((child): child is PermissionResponse => child !== null)
            : [];

        if (match) {
            return {...node, children: filteredChildren.length > 0 ? filteredChildren : node.children};
        }

        if (filteredChildren.length > 0) {
            return {...node, children: filteredChildren};
        }

        return null;
    };

    return data
        .map((node) => filterNode(node))
        .filter((node): node is PermissionResponse => node !== null);
};

const PermissionManage: React.FC = () => {
    const [query, setQuery] = useState<PermissionPageQuery>({pageNo: 1, pageSize: 10});
    const [allPermissions, setAllPermissions] = useState<PermissionResponse[]>([]);
    const [filteredPermissions, setFilteredPermissions] = useState<PermissionResponse[]>([]);
    const [loading, setLoading] = useState(false);

    const [formModalOpen, setFormModalOpen] = useState(false);
    const [editData, setEditData] = useState<PermissionRequest | null>(null);
    const [formLoading, setFormLoading] = useState(false);

    const [assignRoleModalOpen, setAssignRoleModalOpen] = useState(false);
    const [assignPermId, setAssignPermId] = useState<number | null>(null);

    const fetchPermissions = useCallback(() => {
        setLoading(true);
        getPermissionTree()
            .then((res) => {
                if (res.code === 200 && res.data) {
                    setAllPermissions(res.data);
                    setFilteredPermissions(filterTree(res.data, query));
                }
            })
            .finally(() => setLoading(false));
    }, [query]);

    useEffect(() => {
        fetchPermissions();
    }, []);

    const handleSearch = () => {
        setFilteredPermissions(filterTree(allPermissions, query));
    };

    const handleReset = () => {
        const resetQuery: PermissionPageQuery = {
            pageNo: 1,
            pageSize: 10,
            permissionName: undefined,
            permissionCode: undefined,
            permissionType: undefined,
        };
        setQuery(resetQuery);
        setFilteredPermissions(allPermissions);
    };

    const handleAdd = () => {
        setEditData(null);
        setFormModalOpen(true);
    };

    const handleEdit = (record: PermissionResponse) => {
        setEditData({
            id: record.id,
            permissionName: record.permissionName,
            permissionCode: record.permissionCode,
            description: record.description,
            permissionType: record.permissionType,
            parentId: record.parentId,
            sortOrder: record.sortOrder,
        });
        setFormModalOpen(true);
    };

    const handleFormOk = async (values: PermissionRequest) => {
        setFormLoading(true);
        try {
            const res = await savePermission(values);
            if (res.code === 200) {
                message.success(values.id ? '修改成功' : '创建成功');
                setFormModalOpen(false);
                fetchPermissions();
            } else {
                message.error(res.message || '操作失败');
            }
        } catch {
            message.error('操作失败');
        } finally {
            setFormLoading(false);
        }
    };

    const handleAssignRole = (permissionId: number) => {
        setAssignPermId(permissionId);
        setAssignRoleModalOpen(true);
    };

    return (
        <div className={`${globalStyles.pageContainer} ${styles.permPage}`}>
            <PermSearchBar
                query={query}
                onQueryChange={setQuery}
                onSearch={handleSearch}
                onReset={handleReset}
                onAdd={handleAdd}
            />
            <PermTable
                dataSource={filteredPermissions}
                loading={loading}
                onEdit={handleEdit}
                onAssignRole={handleAssignRole}
                onRefresh={() => fetchPermissions()}
            />
            <PermFormModal
                open={formModalOpen}
                editData={editData}
                onCancel={() => setFormModalOpen(false)}
                onOk={handleFormOk}
                confirmLoading={formLoading}
            />
            <AssignRoleModal
                open={assignRoleModalOpen}
                permissionId={assignPermId}
                onCancel={() => setAssignRoleModalOpen(false)}
                onSuccess={() => {
                    setAssignRoleModalOpen(false);
                    fetchPermissions();
                }}
            />
        </div>
    );
};

export default PermissionManage;
