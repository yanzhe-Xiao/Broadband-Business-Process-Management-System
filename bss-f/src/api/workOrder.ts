// src/api/workorder.ts
import { http } from './http'

export type WorkStatus = 'pending' | 'accepted' | 'onroute' | 'arrived' | 'working' | 'done' | 'canceled'
export type Priority = 'low' | 'medium' | 'high'

export interface WorkOrderItem {
    id: string
    orderNo: string
    customerName: string
    customerPhone: string
    address: string
    planName?: string
    createdAt: string
    deadline?: string
    status: WorkStatus
    priority: Priority
    remarks?: string
    // 可选：地图坐标
    lat?: number
    lng?: number
}

export interface PageResp<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

/** 分页查询工单列表 */
export async function listWorkOrders(params: {
    current?: number
    size?: number
    keyword?: string
    status?: WorkStatus | 'all'
    dateFrom?: string
    dateTo?: string
}) {
    const { current = 1, size = 10, ...rest } = params || {}
    const res = await http.get<PageResp<WorkOrderItem>>('/engineer/workorders', {
        params: { current, size, ...rest }
    })
    const p = res.data
    return {
        records: p.records ?? [],
        total: Number(p.total ?? 0),
        size: Number(p.size ?? size),
        current: Number(p.current ?? current),
        pages: Number(p.pages ?? 0),
    }
}

/** 领取/接单、出发、到场、开工、完工、取消 等状态流转 */
export async function updateWorkStatus(payload: {
    id: string
    status: WorkStatus
    remark?: string
}) {
    await http.post('/engineer/workorders/status', payload)
}

/** 新增备注 */
export async function addWorkRemark(payload: { id: string; remark: string }) {
    await http.post('/engineer/workorders/remark', payload)
}

/** 上传完工凭证(返回文件URL) */
export async function uploadProof(data: FormData): Promise<{ url: string }> {
    const res = await http.post<{ url: string }>('/engineer/workorders/proof', data, {
        headers: { 'Content-Type': 'multipart/form-data' },
    })
    return res.data
}



