import { lazy, Suspense } from 'react'
import { createBrowserRouter, Navigate, RouterProvider } from 'react-router-dom'
import { RequireAuth, RequireRole } from './router/guard'
import { LazyLoading } from '../components'
import Admin from './pages/admin'
import Engineer from './pages/engineer'

const Login = lazy(() => import('./pages/auth/login/Login'))
const CustomerHome = lazy(() => import('./pages/customer/index'))
const RegisterPage = lazy(() => import('./pages/auth/register/Register'))

// const LoginSuccess = (el: ReactElement) => (
//     <Suspense fallback={<div style={{ padding: 24, fontSize: 30 }}>登录成功</div>}>{el}</Suspense>
// )
// const AfterLogin = () => <div style={{ padding: 24 }}>已登录（占位页）。后续再接入真实页面。</div>
const router = createBrowserRouter([
    // 启动默认进登录页
    {
        path: '/',
        element: <Navigate to="/login" replace />
    },
    {
        path: '/login',
        element:
            <Suspense fallback={<LazyLoading />}>
                <Login />
            </Suspense>
    },
    {
        path: '/register',
        element:
            <Suspense fallback={<LazyLoading />}>
                <RegisterPage />
            </Suspense>
    },

    // 登录后落地的临时占位页[用户]（为了能验证登录跳转逻辑）
    {
        path: '/app',
        element:
            <Suspense fallback={<LazyLoading />}>
                <RequireAuth>
                    <RequireRole roleName='客户'>
                        <CustomerHome />
                    </RequireRole>
                </RequireAuth>
            </Suspense>
    },

    // 管理员占位（同样仅验证守卫）
    {
        path: '/admin',
        element: (
            <Suspense fallback={<LazyLoading />}>
                <RequireAuth>
                    <RequireRole roleName='平台管理员'>
                        <Admin />
                    </RequireRole>
                </RequireAuth>
            </Suspense>
        ),
    },
    // 管理员占位（同样仅验证守卫）
    {
        path: '/engineer',
        element: (
            <Suspense fallback={<LazyLoading />}>
                <RequireAuth>
                    <RequireRole roleName='装维工程师'>
                        <Engineer />
                    </RequireRole>
                </RequireAuth>
            </Suspense>
        ),
    },

    { path: '*', element: <Navigate to="/login" replace /> },
])

export const AppRouter = () => {
    return <RouterProvider router={router} />
}