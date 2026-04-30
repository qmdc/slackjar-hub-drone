import React from 'react';
import {Button, Card, Input, InputNumber, Space, Switch, Tooltip} from 'antd';
import {SaveOutlined} from '@ant-design/icons';
import type {SysDictRequest} from '../../../apis';
import styles from './index.module.scss';

/**
 * 字典详情表单组件属性
 */
interface DictDetailFormProps {
    // 字典表单数据
    form: SysDictRequest;
    // 是否为编辑模式（true=编辑已有字典，false=新增字典）
    isEdit: boolean;
    // 表单数据变更回调
    onChange: (form: SysDictRequest) => void;
    // 保存字典回调
    onSave: () => void;
    // 保存加载状态
    saveLoading: boolean;
}

/**
 * 字典详情编辑组件
 * 展示并编辑字典基本信息，包含编码、名称、排序号、状态、描述
 *
 * @param form 字典表单数据
 * @param isEdit 是否为编辑模式（true=编辑已有字典，false=新增字典）
 * @param onChange 表单数据变更回调
 * @param onSave 保存字典回调
 * @param saveLoading 保存加载状态
 * @returns 字典详情表单组件
 * @author zhn
 */
const DictDetailForm: React.FC<DictDetailFormProps> = ({
                                                           form,
                                                           isEdit,
                                                           onChange,
                                                           onSave,
                                                           saveLoading
                                                       }) => {
    return (
        <Card
            className={styles.detailFormCard}
            styles={{
                body: {
                    padding: '2px'
                }
            }}
        >
            <div className={styles.detailFormWrap}>
                <div className={styles.detailFormHeader}>
                    <span className={styles.detailFormTitle}>
                        字典详情
                    </span>
                    <Space size={4}>
                        <span className={styles.detailFormLabel}>启用</span>
                        <Switch
                            size="medium"
                            className={styles.detailSwitch}
                            style={{marginRight: '8px'}}
                            checked={form.status === 0}
                            onChange={(checked) => onChange({...form, status: checked ? 0 : 1})}
                        />
                        <Button type="primary" size="small" className={styles.detailSaveBtn} icon={<SaveOutlined/>} loading={saveLoading} onClick={onSave}>
                            保存
                        </Button>
                    </Space>
                </div>
                <div className={styles.detailFormBody}>
                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={styles.formLabelRequired}>字典编码</label>
                            {isEdit ? (
                                <Tooltip title={form.dictCode} mouseEnterDelay={0.3} placement="top" autoAdjustOverflow>
                                    <span className={styles.formReadonly}>{form.dictCode}</span>
                                </Tooltip>
                            ) : (
                                <Input
                                    value={form.dictCode}
                                    onChange={(e) => onChange({...form, dictCode: e.target.value})}
                                    placeholder="请输入字典编码"
                                    size="small"
                                    className={styles.formInputFlex}
                                />
                            )}
                        </div>
                        <div className={styles.formGroup}>
                            <label className={styles.formLabelRequired}>字典名称</label>
                            <Input
                                value={form.dictName}
                                onChange={(e) => onChange({...form, dictName: e.target.value})}
                                placeholder="请输入字典名称"
                                size="small"
                                className={styles.formInputFlex}
                            />
                        </div>
                    </div>
                    <div className={styles.formRow}>
                        <div className={styles.formGroup}>
                            <label className={styles.formLabel}>排序号</label>
                            <InputNumber
                                value={form.sortOrder}
                                onChange={(val) => onChange({...form, sortOrder: val || 0})}
                                placeholder="排序号"
                                min={0}
                                size="small"
                                className={styles.formInputFlex}
                            />
                        </div>
                        <div className={styles.formGroup}>
                            <label className={styles.formLabel}>描述</label>
                            <Input
                                value={form.description}
                                onChange={(e) => onChange({...form, description: e.target.value})}
                                placeholder="请输入描述"
                                size="small"
                                className={styles.formInputFlex}
                            />
                        </div>
                    </div>
                </div>
            </div>
        </Card>
    );
};

export default DictDetailForm;
