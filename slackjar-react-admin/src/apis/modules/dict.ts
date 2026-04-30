import request from '../request'
import type {PageResult, ResponseData} from './types'

/**
 * 保存字典（新增、修改）
 */
export function saveDict(data: SysDictRequest): Promise<ResponseData<boolean>> {
    return request.post('/sys-dict/save', data)
}

/**
 * 删除字典
 */
export function deleteDict(id: number): Promise<ResponseData<boolean>> {
    return request.post(`/sys-dict/delete/${id}`)
}

/**
 * 根据ID查询字典
 */
export function getDictById(id: number): Promise<ResponseData<SysDictResponse>> {
    return request.get(`/sys-dict/query/${id}`)
}

/**
 * 根据字典编码查询字典
 */
export function getDictByCode(dictCode: string): Promise<ResponseData<SysDictResponse>> {
    return request.get(`/sys-dict/query/code/${dictCode}`)
}

/**
 * 分页条件查询字典列表
 */
export function pageQueryDicts(data: SysDictPageQuery): Promise<ResponseData<PageResult<DictItem>>> {
    return request.post('/sys-dict/pageQuery', data)
}

// ============================================
// 类型定义
// ============================================

/**
 * 字典请求参数
 */
export interface SysDictRequest {
    id?: number
    dictName: string
    dictCode: string
    description?: string
    status?: number
    sortOrder?: number
    dictItems?: DictItemRequest[]
}

/**
 * 字典项请求
 */
export interface DictItemRequest {
    id?: number
    itemLabel: string
    itemValue: string
    description?: string
    status?: number
    sortOrder?: number
}

/**
 * 字典响应
 */
export interface SysDictResponse {
    id: number
    dictName: string
    dictCode: string
    description: string
    status: number
    sortOrder: number
    dictItems: DictItemResponse[]
}

/**
 * 字典项响应
 */
export interface DictItemResponse {
    id: number
    dictId: number
    itemLabel: string
    itemValue: string
    description: string
    status: number
    sortOrder: number
}

/**
 * 字典项（用于列表展示）
 */
export interface DictItem {
    id: number
    dictName: string
    dictCode: string
    description: string
    status: number
    sortOrder: number
    createTime: number
    updateTime: number
}

/**
 * 字典分页查询请求参数
 */
export interface SysDictPageQuery {
    pageNo?: number
    pageSize?: number
    dictName?: string
    dictCode?: string
    status?: number
}
