import axios from 'axios';
import {nanoid} from 'nanoid';
import {useAuthStore} from '../store/authStore';
import {message} from 'antd';

/**
 * 创建 axios 实例
 */
const request = axios.create({
    baseURL: '/api',
    timeout: 15000,
    headers: {
        'Content-Type': 'application/json'
    }
})

/**
 * 请求拦截器
 */
request.interceptors.request.use(
    (config) => {
        // 注入认证 token
        const jwt = useAuthStore.getState().jwt
        if (jwt) {
            config.headers.token = jwt
        }

        // 注入请求追踪ID
        config.headers['X-Trace-Id'] = nanoid()

        // 调试日志
        console.log('>>>>> axios request:', config.method?.toUpperCase(), `${config.baseURL}${config.url || ''}`, config.headers)
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

/**
 * 响应拦截器
 */
request.interceptors.response.use(
    (response) => {
        console.log('<<<<< axios success response:', response, response.data)
        const customData = response.data;
        if (customData.code === 401) {
            message.error('登录已失效，请重新登录').then()
            useAuthStore.getState().loggedOut()
            setTimeout(() => {
                window.location.href = '/login'
            }, 800)
            return;
        } else if (customData.code === 400) {
            message.error('参数错误').then()
        } else if (customData.code === 403) {
            message.error('暂无操作权限').then()
        } else if (customData.code === 404) {
            message.error('资源不存在').then()
        } else if (customData.code === 429) {
            message.warning('系统繁忙，请稍后再试').then()
        } else if (customData.code === 500) {
            message.error('服务端异常').then()
        } else if (customData.code === 503) {
            message.warning('服务暂不可用，请稍后再试').then()
        }
        return customData;
    },
    (error) => {
        if (error.response) {
            console.log('<<<<< axios error response:', error.response)
            switch (error.response.status) {
                case 401:
                    message.error('登录已失效，请重新登录').then()
                    useAuthStore.getState().loggedOut()
                    setTimeout(() => {
                        window.location.href = '/login'
                    }, 800)
                    break
                case 403:
                    window.location.href = '/error/403'
                    break
                case 500:
                    window.location.href = '/error/500'
                    break
                default:
                    console.error('请求失败:', error.response.data?.message || error.message)
            }
        }
        return Promise.reject(error)
    }
)

export default request
