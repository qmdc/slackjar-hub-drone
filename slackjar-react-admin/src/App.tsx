import {RouterProvider} from "react-router";
import {router} from "./routers/router";

import dayjs from 'dayjs';
import 'dayjs/locale/zh-cn';
import React, {useEffect} from 'react';
import {App as AntdApp, ConfigProvider, notification} from "antd";
import {useAppGlobalStore} from "./store/appGlobalStore";
import {useAuthStore} from "./store/authStore";
import {socketManager, initSocketMessageHandlers} from "./socketio";

// 设置 dayjs 默认语言为中文
dayjs.locale('zh');

// 创建全局 notification API（用于非组件环境）
let globalNotificationApi: any = null;
export const getNotificationApi = () => globalNotificationApi;

function App() {
    // 从 Zustand store 中获取语言配置
    const {locale} = useAppGlobalStore();
    // 从 authStore 中获取登录状态
    const {isLogin} = useAuthStore();
    // 初始化 notification API
    const [notificationApi, notificationContextHolder] = notification.useNotification();
    
    // 将 notification API 保存到全局变量
    useEffect(() => {
        globalNotificationApi = notificationApi;
        console.log('[App] Notification API 已初始化');
    }, [notificationApi]);

    // 初始化 Socket 连接（处理页面刷新和重新打开浏览器的情况）
    useEffect(() => {
        // 如果用户已登录且 Socket 未连接，则初始化连接
        if (isLogin && !socketManager.isConnected()) {
            const socketUrl = import.meta.env.VITE_SOCKET_URL || 'http://127.0.0.1:9092';
            // 从 authStore 获取用户信息
            const {userInfo} = useAuthStore.getState();
            if (userInfo) {
                socketManager.init(socketUrl, userInfo.id.toString());
                // 只在首次连接时注册 handlers（socketManager 内部会防重复）
                initSocketMessageHandlers();
                console.log('[App] 检测到已登录状态，已初始化 Socket 连接');
            }
        }
    }, [isLogin]);

    return (
        // Ant Design 配置提供者，设置语言包
        <ConfigProvider locale={locale}>
            {/* Ant Design 应用容器，提供 message、notification 等静态方法 */}
            <AntdApp>
                {/* Notification Context Holder */}
                {notificationContextHolder}
                {/* 路由提供者 */}
                <RouterProvider router={router}/>
            </AntdApp>
        </ConfigProvider>
    )
}

export default App
