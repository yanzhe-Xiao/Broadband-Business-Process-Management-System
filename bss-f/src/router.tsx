import { lazy, Suspense, type ReactElement } from 'react'
import { createBrowserRouter, Navigate } from 'react-router-dom'
import { RequireAuth, RequireRole } from './router/guard'

const Login = lazy(() => import('./pages/auth/login/Login'))
const CustomerHome = lazy(() => import('./pages/customer/home/home'))
const RegisterPage = lazy(() => import('./pages/auth/register/Register'))
const withSuspense = (el: ReactElement) => (
    <Suspense fallback={<div style={{ padding: 24 }}>Loading…</div>}>{el}</Suspense>
)

const LoginSuccess = (el: ReactElement) => (
    <Suspense fallback={<div style={{ padding: 24, fontSize: 30 }}>登录成功</div>}>{el}</Suspense>
)
const AfterLogin = () => <div style={{ padding: 24 }}>已登录（占位页）。后续再接入真实页面。</div>
export const router = createBrowserRouter([
    // 启动默认进登录页
    { path: '/', element: <Navigate to="/login" replace /> },
    { path: '/login', element: withSuspense(<Login />) },
    {
        path: '/register',
        element: withSuspense(<RegisterPage />)
    },

    // 登录后落地的临时占位页[用户]（为了能验证登录跳转逻辑）
    {
        path: '/app',
        element: LoginSuccess(
            <RequireAuth>
                <RequireRole role='user'>
                    <CustomerHome />
                </RequireRole>
            </RequireAuth>
        ),
    },

    // 管理员占位（同样仅验证守卫）
    {
        path: '/admin',
        element: (
            <RequireAuth>
                <RequireRole role='admin'>
                    <AfterLogin />
                </RequireRole>
            </RequireAuth>
        ),
    },


    { path: '*', element: <Navigate to="/login" replace /> },
])
