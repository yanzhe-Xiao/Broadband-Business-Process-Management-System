// src/api/user.ts
import { http } from '../api/http'

/** ------- 通用类型 ------- */
export type ID = string

export type UserStatus = 'active' | 'disabled' | 'pending'
export type Profile = {
    username: string
    fullName: string
    roleName: string
    status: UserStatus | string
    email?: string
    phone?: string
    avatar?: string // 可选：后端返回头像URL或Base64
    password?: string
}

// 拉取个人资料
export async function getProfile(): Promise<Profile> {
    const res = await http.get<Profile>('/api/user')
    return res.data
}

// 更新基本信息（不含密码）
export async function updateProfile(data: Partial<Pick<Profile, 'fullName' | 'phone' | 'email' | 'avatar'>>): Promise<Profile> {
    const res = await http.put<Profile>('/user/me', data)
    return res.data
}
// 更新基本信息
export async function updateAllProfile(data: Profile): Promise<Profile> {
    const res = await http.put<Profile>('/api/user', data)
    return res.data
}

// 修改密码（可选：单独接口）
export async function changeMyPassword(oldPassword: string, newPassword: string): Promise<void> {
    await http.post('/user/change-password', { oldPassword, newPassword })
}
// /api/user/reset-password

// 修改密码
export async function changePassword(username: string, newPassword: string): Promise<void> {
    await http.post('/api/user/reset-password', { username, newPassword })
}


// 密码强度
export function calcStrength(pwd: string): number {
    if (!pwd) return 0
    let score = 0
    if (pwd.length >= 6) score++
    if (/[A-Z]/.test(pwd) || /[a-z]/.test(pwd)) score++
    if (/\d/.test(pwd)) score++
    if (/[^A-Za-z0-9]/.test(pwd)) score++
    if (pwd.length >= 10) score++
    // 返回等级 0 ~ 3
    if (score >= 4) return 3
    if (score >= 2) return 2
    return 1
}


// =============管理员 人员管理===============
/** ------- 错误信息提取 ------- */
function pickErrMsg(e: any, fallback = '请求失败') {
    return (
        e?.response?.data?.message ||
        e?.message ||
        fallback
    )
}


export interface PageReq {
    current?: number
    size?: number
    keyword?: string
    status?: string
    roleName?: string
}

export interface PageResp<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}
/** 可选：上传头像（表单上传），返回新 avatar URL */
export async function uploadMyAvatar(file: File): Promise<{ avatar: string }> {
    try {
        const form = new FormData()
        form.append('file', file)
        const res = await http.post<{ avatar: string }>('/api/users/me/avatar', form, {
            headers: { 'Content-Type': 'multipart/form-data' },
        })
        return res.data
    } catch (e: any) {
        throw new Error(pickErrMsg(e, '上传头像失败'))
    }
}
/** 分页查询用户 */
export interface UserRow extends Profile {
    id: ID
}
export async function listUsers(params: PageReq): Promise<PageResp<UserRow>> {
    try {
        const res = await http.get<PageResp<UserRow>>('/api/users', { params })
        const p = res.data
        // 兜底转换，避免 undefined 影响渲染
        return {
            records: p.records ?? [],
            total: Number(p.total ?? 0),
            size: Number(p.size ?? params.size ?? 10),
            current: Number(p.current ?? params.current ?? 1),
            pages: Number(p.pages ?? 0),
        }
    } catch (e: any) {
        throw new Error(pickErrMsg(e, '获取用户列表失败'))
    }
}

/** 创建用户（管理员） */
export interface CreateUserReq {
    username: string
    password: string
    fullName: string
    roleName: string
    email?: string
    phone?: string
    status?: UserStatus
    avatar?: string
}
export async function createUser(data: CreateUserReq): Promise<UserRow> {
    try {
        const res = await http.post<UserRow>('/api/users', data)
        return res.data
    } catch (e: any) {
        throw new Error(pickErrMsg(e, '创建用户失败'))
    }
}

/** 更新用户（管理员，可改任意字段；不含密码时可不传） */
export type UpdateUserReq = Partial<Omit<UserRow, 'id' | 'username'>> & { password?: string }
export async function updateUser(id: ID, data: UpdateUserReq): Promise<UserRow> {
    try {
        const res = await http.put<UserRow>(`/api/users/${encodeURIComponent(id)}`, data)
        return res.data
    } catch (e: any) {
        throw new Error(pickErrMsg(e, '更新用户失败'))
    }
}

/** 删除用户（管理员） */
export async function deleteUser(id: ID): Promise<void> {
    try {
        await http.delete(`/api/users/${encodeURIComponent(id)}`)
    } catch (e: any) {
        throw new Error(pickErrMsg(e, '删除用户失败'))
    }
}

/** 设置用户状态（启用/禁用/待审核） */
export async function setUserStatus(id: ID, status: UserStatus): Promise<void> {
    try {
        await http.patch(`/api/users/${encodeURIComponent(id)}/status`, { status })
    } catch (e: any) {
        throw new Error(pickErrMsg(e, '修改用户状态失败'))
    }
}

/** 管理员重置用户密码 */
export async function adminResetPassword(username: string, newPassword: string): Promise<void> {
    try {
        await http.post('/api/users/reset-password', { username, newPassword })
    } catch (e: any) {
        throw new Error(pickErrMsg(e, '重置密码失败'))
    }
}

/** （可选）批量删除 */
export async function batchDeleteUsers(ids: ID[]): Promise<void> {
    try {
        await http.post('/api/users/batch-delete', { ids })
    } catch (e: any) {
        throw new Error(pickErrMsg(e, '批量删除失败'))
    }
}