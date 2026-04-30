import React, {useEffect} from 'react';
import {Form, Input, InputNumber, Modal, Switch} from 'antd';
import type {DictItemRequest} from '../../../apis';

/**
 * 字典项弹窗组件属性
 */
interface DictItemModalProps {
    // 是否可见
    open: boolean;
    // 弹窗标题
    title: string;
    // 当前编辑的字典项数据
    initialValues?: DictItemRequest;
    // 确认回调
    onOk: (values: DictItemRequest) => void;
    // 取消回调
    onCancel: () => void;
}

/**
 * 字典项编辑弹窗组件
 * 用于新增或编辑字典项，包含值、含义、排序号、状态、描述字段
 *
 * @param open 是否可见
 * @param title 弹窗标题
 * @param initialValues 当前编辑的字典项数据
 * @param onOk 确认回调
 * @param onCancel 取消回调
 * @returns 弹窗组件
 * @author zhn
 */
const DictItemModal: React.FC<DictItemModalProps> = ({open, title, initialValues, onOk, onCancel}) => {
    const [form] = Form.useForm();

    // 弹窗打开时重置表单数据
    useEffect(() => {
        if (open) {
            form.setFieldsValue({
                itemValue: initialValues?.itemValue || '',
                itemLabel: initialValues?.itemLabel || '',
                sortOrder: initialValues?.sortOrder ?? 0,
                status: initialValues?.status === 0,
                description: initialValues?.description || ''
            });
        }
    }, [open, initialValues, form]);

    /**
     * 确认按钮回调
     * 校验表单并提交数据
     */
    const handleOk = async () => {
        try {
            const values = await form.validateFields();
            onOk({
                ...initialValues,
                ...values,
                status: values.status ? 0 : 1
            });
            form.resetFields();
        } catch {
            // 校验失败，不执行操作
        }
    };

    /**
     * 取消按钮回调
     * 重置表单并关闭弹窗
     */
    const handleCancel = () => {
        form.resetFields();
        onCancel();
    };

    return (
        <Modal
            title={title}
            open={open}
            onOk={handleOk}
            onCancel={handleCancel}
            destroyOnHidden
            width={480}
        >
            <Form
                form={form}
                layout="horizontal"
                labelCol={{span: 5}}
                wrapperCol={{span: 19}}
                autoComplete="off"
            >
                <Form.Item
                    label="值"
                    name="itemValue"
                    rules={[{required: true, message: '请输入值'}]}
                >
                    <Input placeholder="请输入值"/>
                </Form.Item>
                <Form.Item
                    label="含义"
                    name="itemLabel"
                    rules={[{required: true, message: '请输入含义'}]}
                >
                    <Input placeholder="请输入含义"/>
                </Form.Item>
                <Form.Item
                    label="排序号"
                    name="sortOrder"
                    rules={[{required: true, message: '请输入排序号'}]}
                >
                    <InputNumber style={{width: '100%'}} placeholder="请输入排序号" min={0}/>
                </Form.Item>
                <Form.Item
                    label="状态"
                    name="status"
                    valuePropName="checked"
                    initialValue={true}
                >
                    <Switch checkedChildren="启用" unCheckedChildren="禁用"/>
                </Form.Item>
                <Form.Item
                    label="描述"
                    name="description"
                >
                    <Input.TextArea rows={2} placeholder="请输入描述"/>
                </Form.Item>
            </Form>
        </Modal>
    );
};

export default DictItemModal;
