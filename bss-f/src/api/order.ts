import { http } from "./http"

// ====== 类型 ======
export type OrderItem = {
    planCode: string
    planName: string
    planType: 'month' | 'year' | 'forever' | string
    qty: number
    unitPrice: number
    discount: number
    itemPrice: number
    description?: string
    ip?: string
    endTime?: string
}

export type OrderRecord = {
    id: number | string
    status: string
    createdAt: string
    installAddress: string
    price: number
    items: OrderItem[]
    engineerFullName?: string
    engineerPhone?: string
    engineerEmail?: string
}

export type PageResp<T> = {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}
export interface code<T> {
    code: string
    message: string
    data: T
}

interface Data {
    current: number; size: number; username: string; keyword?: string; status?: string
}

// ====== API ======
export async function getMyOrders(data: Data) {
    const res = await http.post<code<PageResp<OrderRecord>>>('/api/order/get', data)
    return res.data.data
}

// 提交评价
export async function submitOrderReview(payload: {
    orderId: number
    score: number         // 1-5

    comment?: string
}) {
    // 你后端真实地址按需调整
    const res = await http.post('/api/rating', payload)
    return res.data
}
