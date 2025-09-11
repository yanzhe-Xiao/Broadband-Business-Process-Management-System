// api/device.ts

import { http } from "./http"

export type DeviceInfo = {
    sn: string
    model?: string
    qty?: string

}

export async function getDeviceList(params?: { keyword?: string; status?: string }) {
    const res = await http.get<DeviceInfo[]>('/api/device/all', { params })
    return res.data || []
}
