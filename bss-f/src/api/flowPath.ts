

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
    status: 'wait' | 'process' | 'finish' | 'error' | string
    /** ↓↓↓ 新增：本地/后端备注 */
    remark?: string | null
    /** ↓↓↓ 新增：凭证图片 URL 列表 */
    proofs?: string[]
}
const TEMPLATE: Array<{ key: FlowStepKey; title: string }> = [
    { key: 'survey', title: '现场勘察' },
    { key: 'cabling', title: '布线/熔纤' },
    { key: 'install', title: '设备安装与上电' },
    { key: 'opticalTest', title: '光功率测试' },
    { key: 'speedTest', title: '上网测速' },
    { key: 'signoff', title: '用户签字确认' },
];
export interface EngineerOrder<T> {
    id: string | number
    orderId: string | number
    engineerFullName: string
    engineerPhone: string
    installAddress: string
    createdAt: string
    updatedAt?: string
    dispatchedAt?: string
    planName?: string
    note?: string
    priority?: Priority | string
    status?: WorkStatus                // 工单总体状态
    currentKey?: FlowStepKey | ''        // 当前进行到的步骤
    steps: T[]                // 全部步骤（含状态）
}

export interface TempStep {
    eventCode: string
    happenedAt: string
    note: string
    imageUrls: string[]
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
    orderId?: string | number
    stepKey?: FlowStepKey
    remark?: string
    note: string
    eventCode: string
    ticketId: string | number

    files?: string[]   // 前端 File，上传可能需要 FormData
}) {
    return await http.post('/api/ticket/flow', {
        ticketId: data.ticketId,
        note: data.note,
        eventCode: data.eventCode || '',
        base64: data.files || []
    })
    // {
    //     headers: { 'Content-Type': 'multipart/form-data' }
    // }
}



export interface code<T> {
    code: string
    message: string
    data: T
}



/** 分页查询工程师自己的工单（含流程进度） */
export async function listEngineerOrders(params: {
    username?: string
    current?: number
    size?: number
    keyword?: string
    status?: WorkStatus | 'all'
}) {/*: Promise<PageResp<EngineerOrder<FlowStep>>>*/
    const { username, current = 1, size = 10, ...rest } = params
    const res = await http.get<code<PageResp<EngineerOrder<TempStep>>>>('/api/ticket/page', {
        params: { username, current, size, ...rest }
    })
    const p = res.data.data

    console.log(111, p.records);
    for (let i = 0; i < p.records.length; i++) {

    }
    const back = p.records.map((order, idx) => {
        const len = order.steps.length;
        console.log('len', idx, len);

        const steps: FlowStep[] = TEMPLATE.map((tpl, i) => {
            const temp: TempStep | undefined = order.steps[i];
            const status: FlowStep['status'] = i < len ? 'finish' : i === len ? 'process' : 'wait';
            console.log('status', idx, i, status);
            return {
                key: tpl.key,
                title: tpl.title,
                status,
                remark: temp?.note ?? undefined,
                proofs: temp?.imageUrls,
            };
        });

        // 注意：不要在这里 log back
        return {
            ...order,
            steps,
            currentKey: len === 0 ? 'survey' : len === 1 ? 'cabling' : len === 2 ? 'install'
                : len === 3 ? 'opticalTest' : len === 4 ? 'speedTest' : len === 5 ? 'signoff' : "" as FlowStepKey
        };
    });
    // export type FlowStepKey =
    //     | 'survey'        // 现场勘察
    //     | 'cabling'       // 布线/熔纤
    //     | 'install'       // 设备安装与上电
    //     | 'opticalTest'   // 光功率测试
    //     | 'speedTest'     // 上网测速
    //     | 'signoff'       // 用户签字确认
    // ✅ 现在 back 已经初始化完成，可以安全打印
    console.log('back', back);

    return {
        records: back ?? [],
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
