import { http } from './http'

export interface AdminPlanItem {
    picture: string
    planCode: string
    name: string
    price: number | string
    // status?: 'onSale' | 'offSale'
    maxBandwidth?: number
    availableBandwidth?: number
    deviceName?: string
    model?: string
    monthlyFee?: number;
    yearlyFee?: number;
    foreverFee?: number;
    planPeriod?: number;
    discount?: number;
    qty?: number;
    status?: string;
    isIp?: number;
    rating?: number;
    deviceSN?: string;
    bandwidth?: number;
    description?: string;
    deviceQty?: number;
    imageBase64?: string;
    requiredDeviceQty?: string | null | number

    [k: string]: any
}

export interface PageResp<T> {
    records: T[]
    total: number
    size: number
    current: number
    pages: number
}

export interface AdminPlanQuery {
    current: number
    size: number
    keyword?: string
    status?: string
    minPrice?: number
    maxPrice?: number
    sortField?: string
    sortOrder?: 'asc' | 'desc'
    onlyInStock?: boolean
}
export interface code<T> {
    code: string
    message: string
    data: T
}

/** 分页获取套餐 */
export async function getPlanPage(params: AdminPlanQuery): Promise<PageResp<AdminPlanItem>> {

    const res = await http.post<code<PageResp<AdminPlanItem>>>('/api/admin/menu', params)
    const p = res.data.data
    return {
        records: p.records ?? [],
        total: Number(p.total ?? 0),
        size: Number(p.size ?? params.size),
        current: Number(p.current ?? params.current),
        pages: Number(p.pages ?? 0),
    }
}

export type PLAN_STATUS = "ACTIVE" | "INACTIVE"
export interface addTariffPlans {
    planCode: string;
    name: string;
    monthlyFee?: number;
    yearlyFee?: number;
    foreverFee?: number;
    planPeriod?: number;
    discount?: number;
    qty?: number;
    status?: string | PLAN_STATUS;
    isIp?: number;
    // rating?: number;
    deviceSN?: string;
    bandwidth?: number;
    description?: string;
    deviceQty?: number;
    imageBase64?: string;
}

/** 新增套餐 */
export async function createPlan(data: AdminPlanItem): Promise<void> {
    await http.post('/admin/plans', data)
}

export async function addPlan(data: addTariffPlans): Promise<void> {
    await http.post('/api/tariffplan/add', data)
}

/** 更新套餐（按 planCode） */
export async function updatePlan(data: AdminPlanItem): Promise<void> {
    await http.put(`/admin/plans/${encodeURIComponent(String(data.planCode))}`, data)
}

/** 删除套餐 */
export async function deletePlan(planCode: string): Promise<void> {
    await http.delete(`/admin/plans/${encodeURIComponent(planCode)}`)
}
