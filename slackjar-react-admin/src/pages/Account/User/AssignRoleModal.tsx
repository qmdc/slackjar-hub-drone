import React, {useEffect, useState} from 'react';
import {Checkbox, message, Modal} from 'antd';
import type {Role} from '../../../apis';
import {getUserRoles, pageQueryRoles, assignRoles} from '../../../apis';
import type {PageResult} from '../../../apis';

interface AssignRoleModalProps {
    open: boolean;
    userId: number | null;
    onCancel: () => void;
    onSuccess: () => void;
}

const AssignRoleModal: React.FC<AssignRoleModalProps> = ({open, userId, onCancel, onSuccess}) => {
    const [allRoles, setAllRoles] = useState<Role[]>([]);
    const [checkedRoleIds, setCheckedRoleIds] = useState<number[]>([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        if (open && userId) {
            setLoading(true);
            Promise.all([
                pageQueryRoles({pageNo: 1, pageSize: 200, sortOrder: 'sortOrder', sortBy: 'asc'}),
                getUserRoles(userId)
            ])
                .then(([rolesRes, userRolesRes]) => {
                    if (rolesRes.code === 200 && rolesRes.data) {
                        const pageResult = rolesRes.data as PageResult<any>;
                        setAllRoles(pageResult.list || []);
                    }
                    if (userRolesRes.code === 200 && userRolesRes.data) {
                        setCheckedRoleIds(userRolesRes.data.map((r: Role) => r.id));
                    }
                })
                .finally(() => setLoading(false));
        }
    }, [open, userId]);

    const handleOk = async () => {
        if (!userId) return;
        setSaving(true);
        try {
            const res = await assignRoles(userId, checkedRoleIds);
            if (res.code === 200) {
                message.success('分配角色成功');
                onSuccess();
            } else {
                message.error(res.message || '分配角色失败');
            }
        } catch {
            message.error('分配角色失败');
        } finally {
            setSaving(false);
        }
    };

    return (
        <Modal
            title="分配角色"
            open={open}
            onOk={handleOk}
            onCancel={onCancel}
            confirmLoading={saving}
            loading={loading}
            width={480}
        >
            <Checkbox.Group
                value={checkedRoleIds}
                onChange={(values) => setCheckedRoleIds(values as number[])}
                style={{width: '100%'}}
            >
                <div style={{display: 'flex', flexDirection: 'column', gap: 12, padding: '12px 0'}}>
                    {allRoles.map((role) => (
                        <Checkbox key={role.id} value={role.id}>
                            <span style={{fontWeight: 500}}>{role.roleName}</span>
                            <span style={{color: '#8c8c8c', marginLeft: 8, fontSize: 12}}>{role.roleCode}</span>
                            {role.description && (
                                <span style={{color: '#bfbfbf', marginLeft: 8, fontSize: 12}}>{role.description}</span>
                            )}
                        </Checkbox>
                    ))}
                </div>
            </Checkbox.Group>
        </Modal>
    );
};

export default AssignRoleModal;
