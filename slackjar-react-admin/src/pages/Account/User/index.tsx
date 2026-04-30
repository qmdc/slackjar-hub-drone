import React, {useCallback, useEffect, useState} from 'react';
import type {UserInfo, UserPageQuery} from '../../../apis';
import {pageQueryUsers} from '../../../apis';
import type {PageResult} from '../../../apis';
import UserSearchBar from './UserSearchBar';
import UserTable from './UserTable';
import AssignRoleModal from './AssignRoleModal';
import styles from './index.module.scss';
import globalStyles from '../../global.module.scss';


const UserManage: React.FC = () => {
    const [query, setQuery] = useState<UserPageQuery>({pageNo: 1, pageSize: 10});
    const [users, setUsers] = useState<UserInfo[]>([]);
    const [loading, setLoading] = useState(false);
    const [pagination, setPagination] = useState({current: 1, pageSize: 10, total: 0});

    const [assignModalOpen, setAssignModalOpen] = useState(false);
    const [assignUserId, setAssignUserId] = useState<number | null>(null);

    const fetchUsers = useCallback((searchQuery?: UserPageQuery) => {
        const q = searchQuery || query;
        setLoading(true);
        pageQueryUsers(q)
            .then((res) => {
                if (res.code === 200 && res.data) {
                    const pageResult = res.data as PageResult<UserInfo>;
                    setUsers(pageResult.list || []);
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
        fetchUsers();
    }, []);

    const handleSearch = () => {
        const newQuery = {...query, pageNo: 1};
        setQuery(newQuery);
        fetchUsers(newQuery);
    };

    const handlePageChange = (page: number, pageSize: number) => {
        const newQuery = {...query, pageNo: page, pageSize};
        setQuery(newQuery);
        fetchUsers(newQuery);
    };

    const handleAssignRole = (userId: number) => {
        setAssignUserId(userId);
        setAssignModalOpen(true);
    };

    return (
        <div className={`${globalStyles.pageContainer}`}>
            <UserSearchBar
                query={query}
                onQueryChange={setQuery}
                onSearch={handleSearch}
                onReset={() => {
                    const resetQuery = {pageNo: 1, pageSize: 10, username: undefined, nickname: undefined, status: undefined};
                    setQuery(resetQuery);
                    fetchUsers(resetQuery);
                }}
            />
            <UserTable
                dataSource={users}
                loading={loading}
                pagination={pagination}
                onPageChange={handlePageChange}
                onAssignRole={handleAssignRole}
                onRefresh={() => fetchUsers()}
            />
            <AssignRoleModal
                open={assignModalOpen}
                userId={assignUserId}
                onCancel={() => setAssignModalOpen(false)}
                onSuccess={() => {
                    setAssignModalOpen(false);
                    fetchUsers();
                }}
            />
        </div>
    );
};

export default UserManage;
