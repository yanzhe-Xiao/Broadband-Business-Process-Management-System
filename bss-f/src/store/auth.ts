import { create } from 'zustand'
import { persist } from 'zustand/middleware'

type Role = 'user' | 'admin' | null

interface AuthState {
    token: string | null
    role: Role
    username: string | null
    login: (p: { token: string; role: Role; username: string }) => void
    logout: () => void
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set) => ({
            token: null,
            role: null,
            username: null,
            login: ({ token, role, username }) => set({ token, role, username }),
            logout: () => set({ token: null, role: null, username: null }),
        }),
        { name: 'bss-auth-store' }
    )
)
