import React, {useEffect, useState} from 'react';
import {Form, Input, InputNumber, Modal, Select, TreeSelect} from 'antd';
import type {PermissionRequest, PermissionResponse} from '../../../apis';
import {getPermissionTree} from '../../../apis';

interface PermFormModalProps {
    open: boolean;
    editData: PermissionRequest | null;
    onCancel: () => void;
    onOk: (values: PermissionRequest) => void;
    confirmLoading: boolean;
}

const PermFormModal: React.FC<PermFormModalProps> = ({open, editData, onCancel, onOk, confirmLoading}) => {
    const [form] = Form.useForm<PermissionRequest>();
    const isEdit = !!editData?.id;
    const [treeData, setTreeData] = useState<PermissionResponse[]>([]);

    useEffect(() => {
        if (open) {
            getPermissionTree().then((res) => {
                if (res.code === 200 && res.data) {
                    setTreeData(res.data);
                }
            });
            if (editData) {
                form.setFieldsValue(editData);
            } else {
                form.resetFields();
            }
        }
    }, [open, editData, form]);

    const buildTreeSelectData = (data: PermissionResponse[]): any[] => {
        return data.map((item) => ({
            value: item.id,
            title: item.permissionName,
            children: item.children && item.children.length > 0 ? buildTreeSelectData(item.children) : [],
        }));
    };

    const handleOk = async () => {
        try {
            const values = await form.validateFields();
            onOk(values);
        } catch {
            // form validation failed
        }
    };

    return (
        <Modal
            title={isEdit ? '编辑权限' : '新建权限'}
            open={open}
            onOk={handleOk}
            onCancel={onCancel}
            confirmLoading={confirmLoading}
            width={520}
            destroyOnClose
        >
            <Form
                form={form}
                layout="vertical"
                style={{paddingTop: 16}}
                initialValues={{permissionType: 1, sortOrder: 0}}
            >
                <Form.Item name="id" hidden>
                    <Input/>
                </Form.Item>
                <Form.Item
                    label="权限名称"
                    name="permissionName"
                    rules={[{required: true, message: '请输入权限名称'}]}
                >
                    <Input placeholder="请输入权限名称" maxLength={50}/>
                </Form.Item>
                <Form.Item
                    label="权限编码"
                    name="permissionCode"
                    rules={[{required: true, message: '请输入权限编码'}]}
                >
                    <Input placeholder="请输入权限编码" maxLength={50} disabled={isEdit}/>
                </Form.Item>
                <Form.Item label="权限描述" name="description">
                    <Input.TextArea placeholder="请输入权限描述" maxLength={200} rows={3}/>
                </Form.Item>
                <div style={{display: 'flex', gap: 16}}>
                    <Form.Item
                        label="权限类型"
                        name="permissionType"
                        rules={[{required: true, message: '请选择权限类型'}]}
                        style={{flex: 1}}
                    >
                        <Select placeholder="请选择权限类型">
                            <Select.Option value={1}>菜单权限</Select.Option>
                            <Select.Option value={2}>按钮权限</Select.Option>
                            <Select.Option value={3}>接口权限</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="排序号" name="sortOrder" style={{flex: 1}}>
                        <InputNumber placeholder="排序号" min={0} style={{width: '100%'}}/>
                    </Form.Item>
                </div>
                <Form.Item label="父权限" name="parentId">
                    <TreeSelect
                        placeholder="无（顶级权限）"
                        treeData={buildTreeSelectData(treeData)}
                        allowClear
                        treeDefaultExpandAll
                        style={{width: '100%'}}
                        popupStyle={{minWidth: 300}}
                        showSearch
                        treeNodeFilterProp="title"
                    />
                </Form.Item>
            </Form>
        </Modal>
    );
};

export default PermFormModal;
