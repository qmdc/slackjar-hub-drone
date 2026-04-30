import React from 'react';
import {Button, Card, Input} from 'antd';
import {PlusOutlined, SearchOutlined} from '@ant-design/icons';
import DictSelect from '../../../components/DictSelect';
import type {RolePageQuery} from '../../../apis';
import globalStyles from '../../global.module.scss';

interface RoleSearchBarProps {
    query: RolePageQuery;
    onQueryChange: (query: RolePageQuery) => void;
    onSearch: () => void;
    onReset: () => void;
    onAdd: () => void;
}

const RoleSearchBar: React.FC<RoleSearchBarProps> = ({query, onQueryChange, onSearch, onReset, onAdd}) => {
    return (
        <Card className={globalStyles.searchBar}>
            <div className={globalStyles.searchLeft}>
                <Input
                    placeholder="角色名称"
                    value={query.roleName || ''}
                    onChange={(e) => onQueryChange({...query, roleName: e.target.value})}
                    onPressEnter={onSearch}
                    prefix={<SearchOutlined/>}
                    style={{width: 150}}
                    size="small"
                    allowClear
                />
                <Input
                    placeholder="角色编码"
                    value={query.roleCode || ''}
                    onChange={(e) => onQueryChange({...query, roleCode: e.target.value})}
                    onPressEnter={onSearch}
                    prefix={<SearchOutlined/>}
                    style={{width: 150}}
                    size="small"
                    allowClear
                />
                <DictSelect
                    dictCode="ENABLE_STATUS"
                    placeholder="角色状态"
                    style={{width: 120}}
                    value={query.status}
                    onChange={(value) => onQueryChange({...query, status: value})}
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
                    新建角色
                </Button>
            </div>
        </Card>
    );
};

export default RoleSearchBar;
