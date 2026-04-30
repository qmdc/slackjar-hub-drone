import request from '../request'
import type {PageResult, ResponseData} from './types'

/**
 * 用户登录（RSA加密密码）
 */
export function login(data: LoginParams): Promise<ResponseData<LoginResult>> {
    return request.post('/sys-user/login/encrypted', data)
}

/**
 * 退出登录
 */
export function logout(): Promise<ResponseData<void>> {
    return request.get('/sys-user/logout')
}

/**
 * 用户注册
 */
export function register(data: RegisterParams): Promise<ResponseData<void>> {
    return request.post('/sys-user/register', data)
}

/**
 * 获取用户信息
 */
export function getUserInfo(userId: number): Promise<ResponseData<UserInfo>> {
    return request.get(`/sys-user/info/${userId}`)
}

/**
 * 推送用户 IP 对应的地级市信息
 */
export function pushIpCityInfo(): Promise<ResponseData<void>> {
    return request.post('/sys-user/pushIpCityInfo')
}

/**
 * 修改用户信息（昵称、头像、背景图）
 */
export function updateUserInfo(data: UpdateUserInfoParams): Promise<ResponseData<UserInfo>> {
    return request.put('/sys-user/updateInfo', data)
}

/**
 * 获取用户的角色列表
 */
export function getUserRoles(userId: number): Promise<ResponseData<Role[]>> {
    return request.get(`/sys-user/${userId}/roles`)
}

/**
 * 分页条件查询用户列表
 */
export function pageQueryUsers(data: UserPageQuery): Promise<ResponseData<PageResult<UserInfo>>> {
    return request.post('/sys-user/pageQuery', data)
}

/**
 * 修改用户状态（启用、禁用）
 */
export function updateUserStatus(userId: number, status: number): Promise<ResponseData<boolean>> {
    return request.put(`/sys-user/updateStatus/${userId}/${status}`)
}

/**
 * 为用户分配角色
 */
export function assignRoles(userId: number, roleIds: number[]): Promise<ResponseData<boolean>> {
    return request.post(`/sys-user/${userId}/roles`, roleIds)
}

/**
 * 获取当前用户登录状态有效的设备列表
 */
export function getUserDevices(): Promise<ResponseData<UserDevice[]>> {
    return request.get('/sys-user/devices')
}

/**
 * 强制下线指定设备
 */
export function forceLogoutDevice(deviceId: number): Promise<ResponseData<boolean>> {
    return request.post(`/sys-user/devices/${deviceId}/logout`)
}

/**
 * 分页查询用户设备登录记录
 */
export function pageQueryUserDevices(data: UserDevicePageQuery): Promise<ResponseData<PageResult<UserDevice>>> {
    return request.post('/sys-user/devices/pageQuery', data)
}

/**
 * 修改密码
 */
export function changePassword(data: ChangePasswordParams): Promise<ResponseData<void>> {
    return request.put('/sys-user/changePassword', data)
}

/**
 * 修改手机号
 */
export function changePhone(phone: string): Promise<ResponseData<void>> {
    return request.put('/sys-user/changePhone', null, {params: {phone}})
}

/**
 * 修改邮箱
 */
export function changeEmail(email: string): Promise<ResponseData<void>> {
    return request.put('/sys-user/changeEmail', null, {params: {email}})
}

// ============================================
// 类型定义
// ============================================

/**
 * 登录请求参数
 */
export interface LoginParams {
    username: string
    encryptedPassword: string
}

/**
 * 登录响应结果
 */
export interface LoginResult {
    tokenName: string
    tokenValue: string
    isLogin: boolean
    loginId: any
    loginType: string
    tokenTimeout: number
    sessionTimeout: number
    tokenSessionTimeout: number
    tokenActivityTimeout: number
    loginDevice: string
    tag: string
}

/**
 * 注册请求参数
 */
export interface RegisterParams {
    username: string
    password: string
    confirmPassword: string
}

/**
 * 用户信息
 */
export interface UserInfo {
    id: number
    username: string
    nickname: string
    email: string
    phone: string
    status: number
    avatarUrl: string
    avatarId: number
    backgroundUrl: string
    backgroundId: number
    createTime: number
    lastLoginTime: number
    ip: string
    city: string
}

/**
 * 修改用户信息请求参数
 */
export interface UpdateUserInfoParams {
    nickname?: string
    avatarId?: number
    backgroundId?: number
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

/**
 * 用户分页查询请求参数
 */
export interface UserPageQuery {
    pageNo?: number
    pageSize?: number
    username?: string
    nickname?: string
    status?: number
}

/**
 * 用户设备信息
 */
export interface UserDevice {
    id: number
    device: string
    ipAddr: string
    location: string
    browser: string
    os: string
    loginTime: string
    expireTime: string
    currentDevice: boolean
    status: number
}

/**
 * 用户设备分页查询请求参数
 */
export interface UserDevicePageQuery {
    pageNo?: number
    pageSize?: number
    device?: string
    browser?: string
    os?: string
    status?: number
    sortBy?: string
    sortOrder?: string
}

/**
 * 修改密码请求参数
 */
export interface ChangePasswordParams {
    oldPassword: string
    newPassword: string
    confirmPassword: string
}
