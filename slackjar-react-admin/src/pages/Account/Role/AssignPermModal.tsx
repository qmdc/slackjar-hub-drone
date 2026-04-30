import React, {useEffect, useState} from 'react';
import {message, Modal, Tree} from 'antd';
import type {PermissionResponse} from '../../../apis';
import {getPermissionTree, assignPermissions, getRolePermissionsBatchFromRole} from '../../../apis';
import styles from './index.module.scss';

interface AssignPermModalProps {
    open: boolean;
    roleId: number | null;
    onCancel: () => void;
    onSuccess: () => void;
}

const AssignPermModal: React.FC<AssignPermModalProps> = ({open, roleId, onCancel, onSuccess}) => {
    const [treeData, setTreeData] = useState<PermissionResponse[]>([]);
    const [checkedKeys, setCheckedKeys] = useState<React.Key[]>([]);
    const [loading, setLoading] = useState(false);
    const [saving, setSaving] = useState(false);

    useEffect(() => {
        if (open && roleId) {
            setLoading(true);
            Promise.all([
                getPermissionTree(),
                getRolePermissionsBatchFromRole([roleId])
            ])
                .then(([treeRes, permRes]) => {
                    if (treeRes.code === 200 && treeRes.data) {
                        setTreeData(treeRes.data);
                    }
                    if (permRes.code === 200 && permRes.data) {
                        const checkedKeys = permRes.data.map((item) => item.id);
                        setCheckedKeys(checkedKeys);
                    }
                })
                .finally(() => setLoading(false));
        } else {
            setCheckedKeys([]);
            setTreeData([]);
        }
    }, [open, roleId]);

    const buildTreeData = (data: PermissionResponse[]): any[] => {
        return data.map((item) => ({
            key: item.id,
            title: (
                <span>
                    <span style={{fontWeight: 500}}>{item.permissionName}</span>
                    <span style={{color: '#8c8c8c', marginLeft: 8, fontSize: 12}}>{item.permissionCode}</span>
                </span>
            ),
            children: item.children && item.children.length > 0 ? buildTreeData(item.children) : [],
        }));
    };

    const handleOk = async () => {
        if (!roleId) return;
        setSaving(true);
        try {
            const res = await assignPermissions(roleId, checkedKeys as number[]);
            if (res.code === 200) {
                message.success('分配权限成功');
                onSuccess();
            } else {
                message.error(res.message || '分配权限失败');
            }
        } catch {
            message.error('分配权限失败');
        } finally {
            setSaving(false);
        }
    };

    return (
        <Modal
            title="分配权限"
            open={open}
            onOk={handleOk}
            onCancel={onCancel}
            confirmLoading={saving}
            loading={loading}
            width={560}
        >
            <div className={styles.permTreeWrap}>
                <Tree
                    checkable
                    checkStrictly
                    defaultExpandAll
                    checkedKeys={checkedKeys}
                    onCheck={(keys) => setCheckedKeys((keys as {checked: React.Key[]}).checked)}
                    treeData={buildTreeData(treeData)}
                />
            </div>
        </Modal>
    );
};

export default AssignPermModal;
