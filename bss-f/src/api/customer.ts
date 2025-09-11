import { http } from "./http";

export type SortType = 'pop' | 'priceUp' | 'priceDown' | 'rating'

export interface CustomerReq {
    // username: string | null;
    // roleName: string | null;
    /** 分页：当前页，从 1 开始 */
    current: number;
    /** 分页：每页条数 */
    size: number;
    keyword?: string;
    minPrice?: string | number;
    maxPrice?: string | number;
    onlyInStock?: string | boolean;
    priceSort?: string | 'month'

    sort?: SortType;

}

export interface ProductItem {
    picture: string;            // 图片
    planCode: string;           // 套餐代码
    name: string;               // 名称
    price: number;              // 价格（如为字符串也可改为 string）
    monthlyFee?: number | string | null;       // (可用)每月赠送（例如月费减免/流量等)
    planPeriod?: string | number; // 套餐周期（如 12/24/月度等）
    discount?: number | string;   // 折扣
    status?: string;              // 状态（如 onSale/下架/缺货 等）

    maxBandwidth?: string | number;        // 最大带宽（Mbps/Gbps，按后端单位）
    availableBandwidth?: string | number;  // 当前可用带宽
    deviceName?: string;          // 设备名字
    model?: string;               // 设备型号
    score?: string | number;      //评分
    number?: number;              //库存


    //新增
    yearlyFee?: number | string | null;
    foreverFee?: number | string | null;
    installationFree?: number;      //安装费
    contractPeriod?: number;
    isIp?: number;
    bandWidth?: number | null;
    qty?: number;
    requireDeviceSn: string;
    requiredDeviceModel?: string;
    requiredDeviceQty?: string;
    deviceProce?: string;
    description?: string;
    rating?:string;
}

export interface PageResp<T> {
    records: T[];
    total: number;
    size: number;
    current: number;
    pages: number;
}
export interface code<T> {
    code: string
    message: string
    data: T
}
export async function getProducts(data: CustomerReq): Promise<PageResp<ProductItem>> {
    try {
        const res = await http.post<code<PageResp<ProductItem>>>('/api/customer/menu', {
            ...data,   // GET 请求参数放到 params
        })
        const payload = res.data.data
        return {
            records: payload.records ?? [],
            total: Number(payload.total ?? 0),
            size: Number(payload.size ?? data.size),
            current: Number(payload.current ?? data.current),
            pages: Number(payload.pages ?? 0),
        }
    } catch (e: any) {
        const msg =
            e?.response?.data?.message ||
            e?.message ||
            '获取商品列表失败，请稍后重试'
        throw new Error(msg)
    }
}
