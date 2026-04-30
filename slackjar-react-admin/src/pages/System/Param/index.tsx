import React from 'react';
import {Tabs, TabsProps} from 'antd';
import SystemParams from "./SystemParams";
import ServerParams from "./ServerParams";
import AliOssParams from "./AliOssParams";
import AiDoubao1_6Params from "./AiDoubao1_6Params";
import CustomizeParams from "./CustomizeParams";
import globalStyles from '../../global.module.scss'


const Param: React.FC = () => {

    const items: TabsProps['items'] = [
        {
            key: '1',
            label: '系统参数',
            children: <SystemParams/>,
        },
        {
            key: '2',
            label: '服务器参数',
            children: <ServerParams/>,
        },
        {
            key: '3',
            label: '阿里云OSS',
            children: <AliOssParams/>,
        },
        {
            key: '4',
            label: '豆包SDK 1.6',
            children: <AiDoubao1_6Params/>,
        },
        {
            key: '5',
            label: '自定义参数',
            children: <CustomizeParams/>,
        },
    ];

    const onChange = (key: string) => {
        console.log('参数配置', key);
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

export default Param;
