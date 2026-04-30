import { useMemo } from "react"
import { useAuthStore } from "../store/authStore"

/**
 * 权限检测 Hook - 检查当前用户是否有某个权限
 * @param permissionCode - 权限代码（如 "user:view"）
 * @returns 是否有权限
 */
export const useHasPermission = (permissionCode: string | string[]): boolean => {
    // 从 Store 读取权限列表（精确订阅）
    const permissions = useAuthStore((state) => state.permissions)

    // 使用 useMemo 优化性能，只在权限或 permissionCode 变化时重新计算
    return useMemo(() => {
        if (!permissions || permissions.length === 0) {
            return false
        }

        // 提取权限代码数组
        const permissionCodes = permissions.filter(p => p.roleStatus === 0).map(p => p.permissionCode)

        // 支持单个权限或多个权限检查
        if (Array.isArray(permissionCode)) {
            // 需要拥有所有权限
            return permissionCode.every(code => permissionCodes.includes(code))
        }
        
        // 检查单个权限
        return permissionCodes.includes(permissionCode)
    }, [permissions, permissionCode])
}

/**
 * 权限检测 Hook - 检查当前用户是否有任意一个权限
 * @param permissionCodes - 权限代码数组
 * @returns 是否有任意一个权限
 */
export const useHasAnyPermission = (permissionCodes: string[]): boolean => {
    const permissions = useAuthStore((state) => state.permissions)

    return useMemo(() => {
        if (!permissions || permissions.length === 0) {
            return false
        }

        const userPermissionCodes = permissions.map(p => p.permissionCode)
        
        // 只要有一个权限匹配就返回 true
        return permissionCodes.some(code => userPermissionCodes.includes(code))
    }, [permissions, permissionCodes])
}