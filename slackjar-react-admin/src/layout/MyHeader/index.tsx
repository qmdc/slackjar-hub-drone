import React from 'react';
import {useLocation, useNavigate} from "react-router";
import {useTranslation} from "react-i18next";
import {Avatar, Badge, Dropdown, Layout, Menu, MenuProps, message, Space, Tabs} from 'antd';
import {
    BellOutlined, ExportOutlined,
    SettingOutlined, TranslationOutlined, UserOutlined
} from "@ant-design/icons";
import {getFirstChildPathByParent, useMenuItems} from "../../routers/router";
import styles from "../Portal/portal.module.scss";

import enUS from 'antd/locale/en_US';
import zhCN from 'antd/locale/zh_CN';
import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import {useAppGlobalStore} from "../../store/appGlobalStore";
import {useAuthStore} from "../../store/authStore";
import {logout} from "../../apis";

// Gitee 图标组件
const GiteeOutlined = ({style}: { style?: React.CSSProperties }) => (
    <svg
        viewBox="0 0 1024 1024"
        width="1em"
        height="1em"
        fill="currentColor"
        style={style}
    >
        <path
            d="M512 1024C229.222 1024 0 794.778 0 512S229.222 0 512 0s512 229.222 512 512-229.222 512-512 512z m259.149-568.883h-290.74a25.293 25.293 0 0 0-25.292 25.293l-0.026 63.206c0 13.952 11.315 25.293 25.267 25.293h177.024c13.978 0 25.293 11.315 25.293 25.267v12.646a75.853 75.853 0 0 1-75.853 75.853h-240.23a25.293 25.293 0 0 1-25.267-25.293V417.203a75.853 75.853 0 0 1 75.827-75.853h353.946a25.293 25.293 0 0 0 25.267-25.292l0.077-63.207a25.293 25.293 0 0 0-25.268-25.293H417.152a189.62 189.62 0 0 0-189.62 189.645V771.15c0 13.977 11.316 25.293 25.294 25.293h372.94a170.65 170.65 0 0 0 170.65-170.65V480.384a25.293 25.293 0 0 0-25.293-25.267z"/>
    </svg>
);

// 通知标签页类型定义
interface Tab {
    key: string;              // 标签页唯一标识
    label: React.ReactNode;   // 标签页标题
    children: React.ReactNode; // 标签页内容
}

/**
 * 顶部导航栏组件
 * 包含：顶部菜单、搜索框、语言切换、通知、用户头像等功能
 */
const MyHeader: React.FC = () => {

    // ========== 路由相关 ==========
    const navigate = useNavigate();                    // 路由跳转
    const {pathname} = useLocation();                  // 当前路由路径

    // 获取顶级菜单项（true 表示只获取一级菜单）
    const topMenus = useMenuItems(true);

    // 根据当前路径高亮对应的顶级菜单
    let menuKey: string = '';
    for (let topMenu of topMenus) {
        if (pathname.startsWith(topMenu!.key + "/") || pathname === topMenu!.key) {
            menuKey = topMenu!.key as string;
        }
    }

    // 处理顶部菜单点击事件
    function handlerTopItemClick(item: { key: string }) {
        // 获取该菜单下的第一个子路由路径
        const firstChildPath = getFirstChildPathByParent(item.key);
        // 如果有子路由则跳转到第一个子路由，否则跳转到当前菜单路径
        navigate(firstChildPath ? firstChildPath : item.key);
    }

    // ========== 国际化相关 ==========
    const {t, i18n} = useTranslation();

    // 使用 Zustand store 管理语言设置
    const {setLocale} = useAppGlobalStore();

    // 处理语言切换
    const handlerChangeLang = (e: { key: string }) => {
        const language = e.key as 'zh' | 'en';
        const locale = language === 'en' ? enUS : zhCN;

        i18n.changeLanguage(language).then(r => {
        });     // 切换 i18next 业务文本语言
        dayjs.locale(language);            // 切换 dayjs 日期库语言
        setLocale(locale, language);       // 切换 Ant Design 组件语言包并持久化到 localStorage
    }

    // ========== 语言切换下拉菜单 ==========
    const langItems: MenuProps['items'] = [
        {
            key: 'zh',
            label: "简体中文",
            onClick: handlerChangeLang
        },
        {
            key: 'en',
            label: "English",
            onClick: handlerChangeLang
        },
    ];

    // ========== 用户菜单相关 ==========
    const userInfo = useAuthStore((state) => state.userInfo);
    const loggedOut = useAuthStore((state) => state.loggedOut);
    const [messageApi, contextHolder] = message.useMessage();

    // 处理用户菜单点击事件
    const handlerUserMenus = (item: { key: string }) => {
        if (item.key === "loggedOut") {
            messageApi.success("已退出").then(r => {
            })
            logout().then(r => {
                if (r.code === 200) {
                    loggedOut()           // 清除登录状态
                    navigate("/login")    // 跳转到登录页
                }
            })
        } else if (item.key === "personalCenter") {
            navigate("/other/personal-center")
        }
    }

    // 用户头像下拉菜单项
    const avatarItems: MenuProps['items'] = [
        {
            key: 'personalCenter',
            label: <><UserOutlined/> {t("menu.personal center")}</>,
            onClick: handlerUserMenus
        },
        {
            type: 'divider',      // 分隔线
        },
        {
            key: 'loggedOut',
            label: <>  <ExportOutlined/> {t("menu.logged out")}</>,
            onClick: handlerUserMenus
        },
    ];

    // ========== 通知相关 ==========
    // 通知标签页数据（待办事项、系统通知、我的消息）
    const notifyItems: Tab[] = [
        {
            label: "系统通知",
            key: "12",
            children: <>
                <p>这里自定义 系统通知 的内容</p>
                <p>这里自定义 系统通知 的内容</p>
                <p>这里自定义 系统通知 的内容</p>
                <p>这里自定义 系统通知 的内容</p>
            </>,
        },
        {
            label: "我的消息",
            key: "23",
            children: <>
                <p>这里自定义 我的消息 的内容</p>
                <p>这里自定义 我的消息 的内容</p>
                <p>这里自定义 我的消息 的内容</p>
                <p>这里自定义 我的消息 的内容</p>
            </>,
        }
    ]


    return (
        <Layout.Header className={styles.header}>
            {contextHolder}
            <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                width: '100%',
                height: '100%'
            }}>
                <div className={styles.logo} style={{flexShrink: 0}}>
                    <img src="/favicon-sailboat.png" alt="logo" style={{width: 24, height: 24, marginRight: 5}}/>
                    {t("title")}
                </div>

                {/* ========== 顶部导航菜单 ========== */}
                <Menu style={{background: "transparent", minWidth: "500px", flex: 1}} mode="horizontal"
                      defaultSelectedKeys={[menuKey]}
                      items={topMenus}
                      onClick={handlerTopItemClick}
                />

                {/* ========== 右侧功能区域 ========== */}
                <Space align={"center"} size={"large"}
                       style={{textAlign: "right", color: "#333", whiteSpace: 'nowrap', marginRight: 36}}>
                    {/* Gitee 仓库链接 */}
                    <a href="https://gitee.com/notre/slack-hub" target="_blank"
                       style={{color: "#333", display: "none", alignItems: "center"}}>
                        <GiteeOutlined style={{fontSize: "18px"}}/>
                    </a>

                    {/* 通知中心（带标签页）- 暂不开放，隐藏 */}
                    <Dropdown placement="bottom" popupRender={
                        () => (
                            <div style={{
                                width: 220,
                                background: "#fff",
                                padding: "5px 10px",
                                borderRadius: "5px",
                                boxShadow: "0 6px 16px 0 rgba(0, 0, 0, 0.08), 0 3px 6px -4px rgba(0, 0, 0, 0.12), 0 9px 28px 8px rgba(0, 0, 0, 0.05)"
                            }}>
                                <Tabs
                                    defaultActiveKey="1"
                                    centered
                                    items={notifyItems}
                                />
                            </div>
                        )
                    }>
                        <div style={{display: 'none'}}>
                            <Badge count={0} size={"small"}>
                                <BellOutlined style={{fontSize: "16px", color: "#333"}}/>
                            </Badge>
                        </div>
                    </Dropdown>

                    {/* 语言切换下拉菜单 */}
                    <Dropdown menu={{items: langItems}} placement="bottom">
                        <div style={{cursor: 'pointer'}}>
                            <TranslationOutlined style={{fontSize: "16px"}}/>
                        </div>
                    </Dropdown>

                    {/* 用户头像下拉菜单 */}
                    <Dropdown menu={{items: avatarItems}} placement="bottomRight">
                        <div style={{fontSize: "14px", cursor: 'pointer'}}>
                            <Avatar src={userInfo!.avatarUrl}/> {userInfo!.nickname}
                        </div>
                    </Dropdown>
                </Space>
            </div>
        </Layout.Header>
    );
};

export default MyHeader;
