import React from 'react';
import {Button, Card, Input, Select} from 'antd';
import {PlusOutlined, SearchOutlined} from '@ant-design/icons';
import type {PermissionPageQuery} from '../../../apis';
import globalStyles from '../../global.module.scss';
import DictSelect from "../../../components/DictSelect";

interface PermSearchBarProps {
    query: PermissionPageQuery;
    onQueryChange: (query: PermissionPageQuery) => void;
    onSearch: () => void;
    onReset: () => void;
    onAdd: () => void;
}

const PermSearchBar: React.FC<PermSearchBarProps> = ({query, onQueryChange, onSearch, onReset, onAdd}) => {
    return (
        <Card className={globalStyles.searchBar}>
            <div className={globalStyles.searchLeft}>
                <Input
                    placeholder="权限名称"
                    value={query.permissionName || ''}
                    onChange={(e) => onQueryChange({...query, permissionName: e.target.value})}
                    onPressEnter={onSearch}
                    prefix={<SearchOutlined/>}
                    style={{width: 150}}
                    size="small"
                    allowClear
                />
                <Input
                    placeholder="权限编码"
                    value={query.permissionCode || ''}
                    onChange={(e) => onQueryChange({...query, permissionCode: e.target.value})}
                    onPressEnter={onSearch}
                    prefix={<SearchOutlined/>}
                    style={{width: 150}}
                    size="small"
                    allowClear
                />
                <DictSelect
                    dictCode="PERM_TYPE"
                    placeholder="权限类型"
                    style={{width: 120}}
                    value={query.permissionType != null ? String(query.permissionType) : undefined}
                    onChange={(value) => onQueryChange({...query, permissionType: value ? Number(value) : undefined})}
                    size="small"
                    allowClear
                />
            </div>
            <div className={globalStyles.searchRight}>
                <Button
                    type="link"
                    icon={<SearchOutlined/>}
                    onClick={onSearch}
                    style={{padding: '4px 8px'}}
                >
                    查询
                </Button>
                <Button
                    type="link"
                    icon={<SearchOutlined/>}
                    onClick={onReset}
                    style={{padding: '4px 8px'}}
                >
                    重置
                </Button>
                <Button
                    type="link"
                    icon={<PlusOutlined/>}
                    onClick={onAdd}
                    style={{padding: '4px 8px'}}
                >
                    新建权限
                </Button>
            </div>
        </Card>
    );
};

export default PermSearchBar;
