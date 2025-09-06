import { create } from 'zustand'
import { persist } from 'zustand/middleware'

type Role = 'user' | 'admin' | '客户' | '平台管理员' | '客服坐席' | '装维工程师' | null

interface AuthState {
    token: string | null
    roleName: Role
    username: string | null
    login: (p: { token: string; roleName: Role; username: string }) => void
    logout: () => void
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            token: null,
            roleName: null,
            username: null,
            login: ({ token, roleName, username }) => set({ token, roleName, username }),
            logout: () => set({ token: null, roleName: null, username: null }),
        }),
        { name: 'bss-auth-store' }
    )
)
