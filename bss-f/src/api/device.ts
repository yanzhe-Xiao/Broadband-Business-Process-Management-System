// api/device.ts

import { http } from "./http"

export type DeviceInfo = {
    sn: string
    model?: string
    qty?: string

}
export interface code<T> {
    code: string
    message: string
    data: T
}

export async function getDeviceList(params?: { keyword?: string; status?: string }) {
    const res = await http.get<code<DeviceInfo[]>>('/api/device/all', { params })
    // console.log(res)
    return res.data.data || []
}
