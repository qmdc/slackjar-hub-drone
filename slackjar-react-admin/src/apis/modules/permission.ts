import request from '../request'
import type {PageResult, ResponseData} from './types'

/**
 * 分页条件查询权限列表
 */
export function pageQueryPermissions(data: PermissionPageQuery): Promise<ResponseData<PageResult<PermissionItem>>> {
    return request.post('/permission/pageQuery', data)
}

/**
 * 保存权限（新增、修改）
 */
export function savePermission(data: PermissionRequest): Promise<ResponseData<boolean>> {
    return request.post('/permission/save', data)
}

/**
 * 删除权限
 */
export function deletePermission(id: number): Promise<ResponseData<boolean>> {
    return request.delete(`/permission/delete/${id}`)
}

/**
 * 获取权限详情（包含分配的角色列表）
 */
export function getPermissionDetail(id: number): Promise<ResponseData<PermissionDetail>> {
    return request.get(`/permission/detail/${id}`)
}

/**
 * 将权限分配给角色
 */
export function assignRolesToPermission(permissionId: number, roleIds: number[]): Promise<ResponseData<boolean>> {
    return request.post(`/permission/assignRoles/${permissionId}`, roleIds)
}

/**
 * 获取权限树
 */
export function getPermissionTree(): Promise<ResponseData<PermissionResponse[]>> {
    return request.get('/permission/getTree')
}

// ============================================
// 类型定义
// ============================================

/**
 * 权限分页查询请求参数
 */
export interface PermissionPageQuery {
    pageNo?: number
    pageSize?: number
    permissionCode?: string
    permissionName?: string
    permissionType?: number
}

/**
 * 权限请求参数
 */
export interface PermissionRequest {
    id?: number
    permissionName: string
    permissionCode: string
    description?: string
    permissionType: number
    parentId?: number
    sortOrder?: number
}

/**
 * 权限项（用于列表展示）
 */
export interface PermissionItem {
    id: number
    permissionName: string
    permissionCode: string
    description: string
    permissionType: number
    parentId: number
    sortOrder: number
    roleCode: string
    roleId: string
    roleStatus: number
}

/**
 * 权限响应（用于树形结构）
 */
export interface PermissionResponse {
    id: number
    permissionName: string
    permissionCode: string
    description: string
    permissionType: number
    parentId: number
    sortOrder: number
    children?: PermissionResponse[]
}

/**
 * 权限详情（包含分配的角色列表）
 */
export interface PermissionDetail {
    id: number
    permissionName: string
    permissionCode: string
    description: string
    permissionType: number
    parentId: number
    sortOrder: number
    roles: Role[]
}

/**
 * 角色信息
 */
export interface Role {
    id: number
    roleName: string
    roleCode: string
    description: string
    roleType: number
    status: number
    sortOrder: number
    createTime: number
    updateTime: number
    deleted: number
    version: number
}
