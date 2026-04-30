import React from 'react';
import {Layout, theme} from 'antd';
import {Navigate, Outlet, useLocation} from "react-router";
import Breadcrumbs from "../Breadcrumbs";
import MyHeader from "../MyHeader";
import MySider from "../MySider";
import CheckPerms from "../../components/CheckPerms";
import styles from "./portal.module.scss";


const Portal: React.FC = () => {
    const {token: {colorBgContainer},} = theme.useToken();
    const {pathname} = useLocation();

    // 如果访问根路径，重定向到 /index
    if (pathname === '/') {
        return <Navigate to="/index" replace/>;
    }

    return (
        <Layout className={styles.mainLayout}>
            <MyHeader/>
            <Layout className={styles.contentLayout}>
                <MySider/>
                <Layout className={styles.rightLayout}>
                    {
                        pathname !== '/index' &&
                        <div className={styles.breadcrumbsWrapper}>
                            <Breadcrumbs/>
                        </div>
                    }
                    <Layout.Content
                        className={styles.scrollableContent}
                        style={{
                            padding: 12,
                            marginTop: pathname === '/index' ? '5px' : 0,
                            background: colorBgContainer,
                        }}
                    >
                        <CheckPerms>
                            <Outlet/>
                        </CheckPerms>
                    </Layout.Content>
                </Layout>
            </Layout>
        </Layout>
    );
};

export default Portal;
