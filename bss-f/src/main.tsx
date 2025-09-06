import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import 'antd/dist/reset.css'
import { AppRouter } from './router'
// const router = createBrowserRouter([
//   {
//     path: '/',
//     element: <Navigate to={'/login'} replace />
//   },
//   {
//     path: '/login',
//     element: <div>login</div>
//   },
//   {
//     path: '/register',
//     element: <div>register</div>
//   },
//   {
//     path: '/app',
//     element: <App />
//   },
//   {
//     path: '/admin',
//     element: <div>admin</div>
//   }
// ])
// 创建一个全局 QueryClient 实例。
// retry: 2 → 请求失败时最多重试 2 次。
// refetchOnWindowFocus: false → 窗口重新获得焦点时不自动刷新数据。
const qc = new QueryClient({
  defaultOptions: { queries: { retry: 2, refetchOnWindowFocus: false } }
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={qc}>
      <AppRouter />
    </QueryClientProvider>
  </StrictMode>,
)
