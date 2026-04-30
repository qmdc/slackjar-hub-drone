import React, {useEffect, useMemo, useState} from 'react';
import {Select, Spin} from 'antd';
import type {SelectProps} from 'antd/es/select';
import {getDictByCode} from '../../apis';
import type {DictItemResponse} from '../../apis';

/**
 * DictSelect 组件属性
 */
interface DictSelectProps extends Omit<SelectProps, 'options' | 'loading'> {
    // 字典编码，用于从后端加载字典项
    dictCode: string;
}

/**
 * 字典选择组件
 * 根据字典编码自动从后端加载字典项，并渲染为 Select 下拉选项
 *
 * @example
 * // 性别选择
 * <DictSelect dictCode="gender" placeholder="请选择性别" />
 *
 * @example
 * // 状态选择，支持所有 Select 属性
 * <DictSelect dictCode="status" allowClear style={{ width: 120 }} />
 *
 * @author zhn
 */
const DictSelect: React.FC<DictSelectProps> = ({dictCode, ...restProps}) => {

    // 字典项列表
    const [dictItems, setDictItems] = useState<DictItemResponse[]>([]);
    // 加载状态
    const [loading, setLoading] = useState(false);

    /**
     * 加载字典数据
     */
    useEffect(() => {
        if (!dictCode) {
            return;
        }
        setLoading(true);
        getDictByCode(dictCode)
            .then((res) => {
                if (res.code === 200 && res.data) {
                    setDictItems(res.data.dictItems || []);
                }
            })
            .catch(() => {
                setDictItems([]);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [dictCode]);

    /**
     * 转换为 Select options
     */
    const options = useMemo(() => {
        return dictItems.map((item) => ({
            label: item.itemLabel,
            value: item.itemValue
        }));
    }, [dictItems]);

    return (
        <Select
            {...restProps}
            allowClear
            loading={loading}
            options={options}
            notFoundContent={loading ? <Spin size="small"/> : '暂无数据'}
        />
    );
};

export default DictSelect;
