import React from 'react';
import {Button, Card, Input} from 'antd';
import {SearchOutlined} from '@ant-design/icons';
import DictSelect from '../../../components/DictSelect';
import type {UserPageQuery} from '../../../apis';
import globalStyles from '../../global.module.scss';

interface UserSearchBarProps {
    query: UserPageQuery;
    onQueryChange: (query: UserPageQuery) => void;
    onSearch: () => void;
    onReset: () => void;
}

const UserSearchBar: React.FC<UserSearchBarProps> = ({query, onQueryChange, onSearch, onReset}) => {
    return (
        <Card className={globalStyles.searchBar}>
            <div className={globalStyles.searchLeft}>
                <Input
                    placeholder="用户名"
                    value={query.username || ''}
                    onChange={(e) => onQueryChange({...query, username: e.target.value})}
                    onPressEnter={onSearch}
                    prefix={<SearchOutlined/>}
                    style={{width: 150}}
                    size="small"
                    allowClear
                />
                <Input
                    placeholder="昵称"
                    value={query.nickname || ''}
                    onChange={(e) => onQueryChange({...query, nickname: e.target.value})}
                    onPressEnter={onSearch}
                    prefix={<SearchOutlined/>}
                    style={{width: 150}}
                    size="small"
                    allowClear
                />
                <DictSelect
                    dictCode="ENABLE_STATUS"
                    placeholder="用户状态"
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
            </div>
        </Card>
    );
};

export default UserSearchBar;
