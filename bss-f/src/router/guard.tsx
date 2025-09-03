import type { ReactNode } from 'react'
import { Navigate, useLocation } from 'react-router-dom'
import { useAuthStore } from '../store/auth'

export function RequireAuth({ children }: { children: ReactNode }) {
    const token = useAuthStore((s) => s.token)
    const loc = useLocation()
    if (!token) return <Navigate to="/login" replace state={{ from: loc }} />
    return <>{children}</>
}

export function RequireRole({ role, children }: { role: 'user' | 'admin'; children: ReactNode }) {
    const current = useAuthStore((s) => s.role)
    if (current !== role) return <Navigate to="/login" replace />
    return <>{children}</>
}
