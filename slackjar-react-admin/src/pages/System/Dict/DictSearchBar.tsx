import React from 'react';
import {Button, Card, Input} from 'antd';
import {PlusOutlined, SearchOutlined} from '@ant-design/icons';
import type {SysDictPageQuery} from '../../../apis';
import globalStyles from '../../global.module.scss';
import DictSelect from "../../../components/DictSelect";

/**
 * 字典搜索栏组件属性
 */
interface DictSearchBarProps {
    // 查询参数
    query: SysDictPageQuery;
    // 查询参数变更回调
    onQueryChange: (query: SysDictPageQuery) => void;
    // 搜索回调（可传入自定义查询参数）
    onSearch: (overrideQuery?: SysDictPageQuery) => void;
    // 新增字典回调
    onAddDict: () => void;
}

/**
 * 顶部检索条件组件
 * 提供搜索框、查询、重置、新增字典功能
 *
 * @param query 查询参数
 * @param onQueryChange 查询参数变更回调
 * @param onSearch 搜索回调
 * @param onAddDict 新增字典回调
 * @returns 搜索栏组件
 * @author zhn
 */
const DictSearchBar: React.FC<DictSearchBarProps> = ({query, onQueryChange, onSearch, onAddDict}) => {
    return (
        <Card
            className={globalStyles.searchBar}
        >
            {/* 左侧：搜索框 */}
            <div className={globalStyles.searchLeft}>
                <Input
                    placeholder="字典编码"
                    value={query.dictCode || ''}
                    onChange={(e) => onQueryChange({...query, dictCode: e.target.value})}
                    onPressEnter={() => onSearch()}
                    prefix={<SearchOutlined/>}
                    style={{width: 150}}
                    size="small"
                    allowClear
                />
                <Input
                    placeholder="字典名称"
                    value={query.dictName || ''}
                    onChange={(e) => onQueryChange({...query, dictName: e.target.value})}
                    onPressEnter={() => onSearch()}
                    prefix={<SearchOutlined/>}
                    style={{width: 150}}
                    size="small"
                    allowClear
                />
                <DictSelect
                    dictCode="ENABLE_STATUS"
                    placeholder="值集状态"
                    style={{width: 120}}
                    value={query.status}
                    onChange={(value) => onQueryChange({...query, status: value})}
                    size="small"
                    suffixIcon={<SearchOutlined/>}
                />
            </div>

            {/* 右侧：操作按钮 */}
            <div className={globalStyles.searchRight}>
                <Button
                    type="link"
                    icon={<SearchOutlined/>}
                    onClick={() => onSearch()}
                    style={{padding: '4px 8px'}}
                >
                    查询
                </Button>
                <Button
                    type="link"
                    icon={<SearchOutlined/>}
                    onClick={() => {
                        const resetQuery = {
                            pageNo: 1,
                            pageSize: 10,
                            dictName: undefined,
                            dictCode: undefined,
                            status: undefined
                        };
                        onQueryChange(resetQuery);
                        onSearch(resetQuery);
                    }}
                    style={{padding: '4px 8px'}}
                >
                    重置
                </Button>
                <Button
                    type="link"
                    icon={<PlusOutlined/>}
                    onClick={onAddDict}
                    style={{padding: '4px 8px'}}
                >
                    新建字典
                </Button>
            </div>
        </Card>
    );
};

export default DictSearchBar;
