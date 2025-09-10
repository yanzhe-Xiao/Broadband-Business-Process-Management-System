

// --- 新增：工程师施工步骤 API ---
// src/api/engineer.ts
import { http } from './http'

export type WorkStatus = 'pending' | 'accepted' | 'onroute' | 'arrived' | 'working' | 'done' | 'canceled'
export type Priority = 'low' | 'medium' | 'high'

export type FlowStepKey =
    | 'survey'        // 现场勘察
    | 'cabling'       // 布线/熔纤
    | 'install'       // 设备安装与上电
    | 'opticalTest'   // 光功率测试
    | 'speedTest'     // 上网测速
    | 'signoff'       // 用户签字确认

export interface FlowStep {
    key: FlowStepKey
    title: string
    status: 'wait' | 'process' | 'finish' | 'error'
    /** ↓↓↓ 新增：本地/后端备注 */
    remark?: string
    /** ↓↓↓ 新增：凭证图片 URL 列表 */
    proofs?: string[]
}

export interface EngineerOrder {
    id: string
    orderNo: string
    customerName: string
    customerPhone: string
    address: string
    planName?: string
    createdAt: string
    priority: Priority
    status: WorkStatus                // 工单总体状态
    currentKey: FlowStepKey          // 当前进行到的步骤
    steps: FlowStep[]                // 全部步骤（含状态）
}

export interface PageResp<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

// 提交单个步骤
export async function submitStep(data: {
    orderId: string
    stepKey: FlowStepKey
    remark: string
    files?: string[]   // 前端 File，上传可能需要 FormData
}) {
    return await http.post('/api/workorder/submitStep', {
        orderNo: data.orderId,
        stepKey: data.stepKey,
        remark: data.remark || '',
        files: data.files || []
    })
    // {
    //     headers: { 'Content-Type': 'multipart/form-data' }
    // }
}



/** 分页查询工程师自己的工单（含流程进度） */
export async function listEngineerOrders(params: {
    username: string
    current?: number
    size?: number
    keyword?: string
    status?: WorkStatus | 'all'
}) {
    const { username, current = 1, size = 10, ...rest } = params
    const res = await http.get<PageResp<EngineerOrder>>('/engineer/my-orders', {
        params: { username, current, size, ...rest }
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

/** （可选）继续施工：后端可用于把状态推进到 working 或维持当前，仅返回确认 */
export async function ensureWorking(orderId: string) {
    await http.post('/engineer/orders/ensure-working', { orderId })
}


// 任意 util.ts，或直接写在组件里
export function fileToDataUrl(file: File): Promise<string> {
    return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => resolve(reader.result as string) // "data:image/...;base64,xxx"
        reader.onerror = reject
        reader.readAsDataURL(file)
    })
}
