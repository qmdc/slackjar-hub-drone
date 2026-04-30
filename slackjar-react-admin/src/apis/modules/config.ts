import request from '../request'
import type {PageResult, ResponseData} from './types'

/**
 * 按分类保存配置（创建、修改、删除）
 */
export function saveConfig(data: SysConfigRequest): Promise<ResponseData<boolean>> {
    return request.post('/sys-config/save', data)
}

/**
 * 根据分类查询配置
 */
export function getConfigByCategory(category: string): Promise<ResponseData<SysConfigResponse>> {
    return request.get(`/sys-config/query/category/${category}`)
}

/**
 * 保存或修改单个配置
 */
export function saveConfigEntity(data: SysConfigSaveRequest): Promise<ResponseData<boolean>> {
    return request.post('/sys-config/saveEntity', data)
}

/**
 * 根据 ID 删除配置
 */
export function deleteConfigById(id: number): Promise<ResponseData<boolean>> {
    return request.delete(`/sys-config/delete/${id}`)
}

/**
 * 分页条件查询配置列表
 */
export function pageQueryConfigs(data: SysConfigPageQuery): Promise<ResponseData<PageResult<SysConfigItemResponse>>> {
    return request.post('/sys-config/pageQuery', data)
}

// ============================================
// 类型定义
// ============================================

/**
 * 保存配置请求参数（批量）
 */
export interface SysConfigRequest {
    category: string
    configItems?: ConfigItem[]
}

/**
 * 配置项
 */
export interface ConfigItem {
    configKey?: string
    configValue?: string
    description?: string
    status?: number
}

/**
 * 查询配置响应
 */
export interface SysConfigResponse {
    category: string
    configItems?: ConfigItem[] | null
}

/**
 * 保存单个配置请求参数
 */
export interface SysConfigSaveRequest {
    id?: number
    category: string
    configKey: string
    configValue?: string
    description?: string
    status?: string
}

/**
 * 分页查询请求参数
 */
export interface SysConfigPageQuery {
    pageNo?: number
    pageSize?: number
    category?: string
    configKey?: string
    status?: number
    description?: string
}

/**
 * 配置分页项响应
 */
export interface SysConfigItemResponse {
    id: number
    configKey: string
    configValue: string
    category: string
    description: string
    status: number
    createTime: number
    updateTime: number
}
