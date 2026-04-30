import request from '../request'
import type {ResponseData} from './types'

/**
 * 获取一言（随机句子）
 */
export function getHitokoto(): Promise<ResponseData<HitokotoResponse>> {
    return request.get('/third-party/hitokoto')
}

/**
 * 获取 IP 信息
 */
export function getIpInfo(ip: string): Promise<ResponseData<IpInfoResponse>> {
    return request.get(`/third-party/ip-info/${ip}`)
}

// ============================================
// 类型定义
// ============================================

/**
 * 一言响应
 */
export interface HitokotoResponse {
    hitokoto: string
    from: string
}

/**
 * IP 信息响应
 */
export interface IpInfoResponse {
    ip: string
    city: string
}
