import {create} from 'zustand'
import {persist} from 'zustand/middleware'
import {Role, UserInfo, PermissionItem} from "../apis";
import {socketManager} from "../socketio";

export interface AuthState {
    isLogin: boolean
    jwt: string | null,
    userInfo: UserInfo | null,
    roles: Role[],
    permissions: PermissionItem[],

    login: (jwt: string, userInfo: UserInfo, roles: Role[], permissions: PermissionItem[]) => void
    loggedOut: () => void
    setUserInfo: (userInfo: UserInfo | null) => void
    setRoles: (roles: Role[]) => void
    setPermissions: (permissions: PermissionItem[]) => void
}

export const useAuthStore = create<AuthState>()(
    persist((set) => ({
            isLogin: false,
            jwt: null,
            userInfo: null,
            roles: [],
            permissions: [],
            login: (jwt: string, userInfo: UserInfo, roles: Role[], permissions: PermissionItem[]) => {
                set({isLogin: true, jwt, userInfo, roles, permissions});
            },
            loggedOut: () => {
                set({isLogin: false, jwt: null, userInfo: null, roles: [], permissions: []});
                // 退出登录时断开 Socket 连接（会自动清理 handlers）
                socketManager.disconnect();
                console.log('[Auth] 退出登录，已断开 Socket 连接');
            },
            setUserInfo: (userInfo: UserInfo | null) => {
                set({userInfo})
            },
            setRoles: (roles: Role[]) => {
                set({roles})
            },
            setPermissions: (permissions: PermissionItem[]) => {
                set({permissions})
            }
        }),
        {
            name: 'auth-storage',
            partialize: (state) => ({
                isLogin: state.isLogin,
                jwt: state.jwt,
                userInfo: state.userInfo,
                roles: state.roles,
                permissions: state.permissions
            })
        }
    )
)
