import { http } from './http'

export type PlanType = 'month' | 'year' | 'forever'

export interface AddCartReq {
    planCode: string
    qty: number
    status: string         // 例如：'在购物车中'
    username: string | null
    planType: PlanType     // 'month' | 'year' | 'forever'
}

//添加购物车
export async function addToCartApi(body: AddCartReq): Promise<void> {
    await http.post('/cart/add', body)
}



export interface CartItem {
    id: string            // 购物车项ID（后端生成）
    planCode: string
    name: string
    qty: number           // 数量
    planType: PlanType    // 'month' | 'year' | 'forever'
    status: string        // "在购物车中"
    price: number         // 单价
    period: string
    imageUrl: string
    cover?: string
    username?: string
    // 可选的展示字段
    monthlyFree?: number | null
    yearlyFree?: number | null
    foreverFree?: number | null
    deviceName?: string
    model?: string
}

export interface PageResp<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

/** 分页拉取购物车 */
export async function getCart(params: { username: string | null; current?: number; size?: number }) {
    const { username, current = 1, size = 10 } = params
    const res = await http.get<PageResp<CartItem>>('/api/order-item/list', { params: { username, current, size } })
    const p = res.data
    return {
        records: p.records ?? [],
        total: Number(p.total ?? 0),
        size: Number(p.size ?? size),
        current: Number(p.current ?? current),
        pages: Number(p.pages ?? 0),
    }
}


/** 更新数量 */
export async function updateCartQty(body: { id: string; qty: number }) {
    await http.post('/cart/updateQty', body)
}

/** 删除单条 */
export async function removeCartItem(id: string) {
    await http.post('/cart/remove', { id })
}

/** 批量删除 */
export async function removeCartBatch(ids: string[]) {
    await http.post('/cart/removeBatch', { ids })
}

/** 清空当前用户购物车 */
export async function clearCart(username: string | null) {
    await http.post('/cart/clear', { username })
}

/** 结算（示例） */
export async function checkoutApi(payload: {
    username: string | null
    itemIds: string[]
}) {
    // 实际根据你的后端改
    await http.post('/cart/checkout', payload)
}
