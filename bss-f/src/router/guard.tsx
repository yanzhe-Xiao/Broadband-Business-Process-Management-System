import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '../store/auth'

export function RequireAuth({ children }: { children: ReactNode }) {
    const token = useAuthStore((s) => s.token)
    const loc = useLocation()
    if (!token) return <Navigate to="/login" replace state={{ from: loc }} />
    return <>{children}</>
}

export function RequireRole({ roleName, children }: { roleName: 'user' | 'admin' | '客户' | '平台管理员' | '客服坐席' | '装维工程师'; children: ReactNode }) {
    const current = useAuthStore((s) => s.roleName)
    if (current !== roleName) return <Navigate to="/login" replace />
    return <>{children}</>
}
