import { http } from './http'

/**
 * 登录
 */
export interface LoginReq {
    username: string
    password: string
}
export interface LoginResp {
    token: string
    roleName: 'user' | 'admin' | '客户' | '平台管理员' | '客服坐席' | '装维工程师'
    username: string
}
export interface code<T> {
    code: string
    message: string
    data: T
}

export async function loginApi(data: LoginReq): Promise<code<LoginResp>> {
    try {
        const res = await http.post<code<LoginResp>>('/auth/login', data)
        return res.data
    } catch (e: any) {
        const status = e?.response?.status
        let message = '登录失败'
        if (status === 401) {
            message = '账号或密码错误'
        } else if (status === 500) {
            message = '服务器异常，请稍后再试'
        } else if (e?.response?.data?.message) {
            message = e.response.data.message
        }
        throw new Error(message)
    }
}

/**
 * 注册
 */

export interface RegisterReq {
    username: string
    password: string
    fullName: string
    phone: string
    email: string
    roleName: string    // 例如: "客户" 或 "管理员"（按你后端期望）
}

export async function registerApi(data: RegisterReq): Promise<void> {
    await http.post('/auth/register', data)
}