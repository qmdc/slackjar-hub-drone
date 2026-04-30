import React, {useEffect} from 'react';
import {Form, Input, InputNumber, Modal, Select} from 'antd';
import type {RoleItem, RoleRequest} from '../../../apis';

interface RoleFormModalProps {
    open: boolean;
    editData: RoleItem | null;
    onCancel: () => void;
    onOk: (values: RoleRequest) => void;
    confirmLoading: boolean;
}

const RoleFormModal: React.FC<RoleFormModalProps> = ({open, editData, onCancel, onOk, confirmLoading}) => {
    const [form] = Form.useForm<RoleRequest>();
    const isEdit = !!editData;

    useEffect(() => {
        if (open) {
            if (editData) {
                form.setFieldsValue({
                    id: editData.id,
                    roleName: editData.roleName,
                    roleCode: editData.roleCode,
                    description: editData.description,
                    roleType: editData.roleType,
                    status: editData.status,
                    sortOrder: editData.sortOrder,
                });
            } else {
                form.resetFields();
            }
        }
    }, [open, editData, form]);

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
            title={isEdit ? '编辑角色' : '新建角色'}
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
                initialValues={{status: 0, roleType: 2, sortOrder: 0}}
            >
                <Form.Item name="id" hidden>
                    <Input/>
                </Form.Item>
                <Form.Item
                    label="角色名称"
                    name="roleName"
                    rules={[{required: true, message: '请输入角色名称'}]}
                >
                    <Input placeholder="请输入角色名称" maxLength={50}/>
                </Form.Item>
                <Form.Item
                    label="角色编码"
                    name="roleCode"
                    rules={[{required: true, message: '请输入角色编码'}]}
                >
                    <Input placeholder="请输入角色编码" maxLength={50} disabled={isEdit}/>
                </Form.Item>
                <Form.Item label="角色描述" name="description">
                    <Input.TextArea placeholder="请输入角色描述" maxLength={200} rows={3}/>
                </Form.Item>
                <div style={{display: 'flex', gap: 16}}>
                    <Form.Item label="角色类型" name="roleType" style={{flex: 1}}>
                        <Select placeholder="请选择角色类型">
                            <Select.Option value={1}>系统角色</Select.Option>
                            <Select.Option value={2}>自定义角色</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="状态" name="status" style={{flex: 1}}>
                        <Select placeholder="请选择状态">
                            <Select.Option value={0}>启用</Select.Option>
                            <Select.Option value={1}>禁用</Select.Option>
                        </Select>
                    </Form.Item>
                    <Form.Item label="排序号" name="sortOrder" style={{flex: 1}}>
                        <InputNumber placeholder="排序号" min={0} style={{width: '100%'}}/>
                    </Form.Item>
                </div>
            </Form>
        </Modal>
    );
};

export default RoleFormModal;
