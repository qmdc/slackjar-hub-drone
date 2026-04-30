import request from '../request'
import type {PageResult, ResponseData} from './types'

/**
 * 分页条件查询角色列表
 */
export function pageQueryRoles(data: RolePageQuery): Promise<ResponseData<PageResult<RoleItem>>> {
    return request.post('/role/pageQuery', data)
}

/**
 * 保存角色（新增、修改）
 */
export function saveRole(data: RoleRequest): Promise<ResponseData<boolean>> {
    return request.post('/role/save', data)
}

/**
 * 删除角色
 */
export function deleteRole(id: number): Promise<ResponseData<boolean>> {
    return request.delete(`/role/delete/${id}`)
}

/**
 * 为角色分配权限
 */
export function assignPermissions(roleId: number, permissionIds: number[]): Promise<ResponseData<boolean>> {
    return request.post(`/role/assignPermissions/${roleId}`, permissionIds)
}

/**
 * 批量获取角色的权限列表
 */
export function getRolePermissionsBatch(roleIds: number[]): Promise<ResponseData<PermissionItem[]>> {
    return request.post('/role/getPermissions/batch', roleIds)
}

/**
 * 为角色分配用户
 */
export function assignUsersToRole(roleId: number, userIds: number[]): Promise<ResponseData<boolean>> {
    return request.post(`/role/assignUsers/${roleId}`, userIds)
}

/**
 * 获取角色的用户列表
 */
export function getUsersByRoleId(roleId: number): Promise<ResponseData<SysUser[]>> {
    return request.get(`/role/getUsers/${roleId}`)
}

/**
 * 分页查询角色的用户列表
 */
export function pageQueryRoleUsers(roleId: number, data: RoleUserPageQuery): Promise<ResponseData<PageResult<SysUser>>> {
    return request.post(`/role/getUsers/${roleId}/pageQuery`, data)
}

// ============================================
// 类型定义
// ============================================

/**
 * 角色分页查询请求参数
 */
export interface RolePageQuery {
    pageNo?: number
    pageSize?: number
    roleCode?: string
    roleName?: string
    roleType?: number
    status?: number
    sortOrder?: string
    sortBy?: string
}

/**
 * 角色请求参数
 */
export interface RoleRequest {
    id?: number
    roleName: string
    roleCode: string
    description?: string
    roleType?: number
    status?: number
    sortOrder?: number
    permissionIds?: number[]
}

/**
 * 角色项（用于列表展示）
 */
export interface RoleItem {
    id: number
    roleName: string
    roleCode: string
    description: string
    roleType: number
    status: number
    sortOrder: number
}

/**
 * 权限项
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
 * 系统用户
 */
export interface SysUser {
    id: number
    username: string
    nickname: string
    email: string
    phone: string
    status: number
    createTime: number
    updateTime: number
}

/**
 * 角色用户分页查询请求参数
 */
export interface RoleUserPageQuery {
    pageNo?: number
    pageSize?: number
    username?: string
    nickname?: string
    status?: number
}
