import React from 'react';
import {Tabs, TabsProps} from 'antd';
import AccountInfo from "./AccountInfo";
import SecurityLog from "./SecurityLog";
import AccountManagement from "./AccountManagement";
import globalStyles from "../global.module.scss";


const UserInfo: React.FC = () => {

    const items: TabsProps['items'] = [
        {
            key: '1',
            label: '账号信息',
            children: <AccountInfo/>,
        },
        {
            key: '2',
            label: '账户管理',
            children: <AccountManagement/>,
        },
        {
            key: '3',
            label: '安全日志',
            children: <SecurityLog/>,
        },
    ];

    const onChange = (key: string) => {
        console.log('个人中心', key);
    };

    return (
        <div className={globalStyles.tabsContainer}>
            <Tabs
                defaultActiveKey="1"
                items={items}
                onChange={onChange}
                className={globalStyles.tabsComponent}
            />
        </div>
    );
};

export default UserInfo;
