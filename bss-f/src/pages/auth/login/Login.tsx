import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Form, Input, Typography, Card, message, Divider } from 'antd'
import { UserOutlined, LockOutlined, ArrowRightOutlined } from '@ant-design/icons'
import { loginApi } from '../../../api/auth'
import { useAuthStore } from '../../../store/auth'
import './LoginPage.css'

export default function LoginPage() {
    const [loading, setLoading] = useState(false)
    const nav = useNavigate()
    // const loc = useLocation() as any
    const login = useAuthStore((s) => s.login)
    const [messageApi, contextHolder] = message.useMessage()

    const onFinish = async (values: { username: string; password: string }) => {
        setLoading(true)
        try {
            const data1 = await loginApi(values)
            let data = data1.data;
            login({ token: data.token, roleName: data.roleName, username: data.username })
            // const from = loc.state?.from?.pathname as string | undefined
            let address = '/login';
            if (data.roleName === '客户') {
                address = '/app'
            } else if (data.roleName === '平台管理员') {
                address = '/admin'
            } else if (data.roleName === '客服坐席') {
                address = '/servicer'
            } else if (data.roleName === '装维工程师') {
                address = '/engineer'
            }
            console.log(data.roleName, data.token, data.username);

            messageApi.open({
                type: 'success',
                content: '登录成功',
                duration: 1,
                // onClose: () => nav(from ?? address, { replace: true })
                onClose: () => nav(address, { replace: true })
            })


        } catch (e: any) {
            const msg = e.message
            messageApi.open({ type: 'error', content: msg })
            console.log('fail:' + msg)
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="login-page">
            {contextHolder}

            <Card className="login-card" bodyStyle={{ padding: 24 }}>
                <div className="login-header">
                    <div className="login-badge">
                        <span>BSS</span>
                    </div>
                    <Typography.Title level={3} style={{ marginBottom: 4 }}>
                        登录
                    </Typography.Title>
                    <Typography.Text type="secondary">
                        欢迎回来，请输入账号信息
                    </Typography.Text>
                </div>

                <Divider style={{ margin: '14px 0 18px' }} />

                <Form
                    layout="vertical"
                    onFinish={onFinish}
                    autoComplete="off"
                    requiredMark={false}
                    validateTrigger={["onBlur", "onSubmit"]}
                >
                    <Form.Item
                        label="用户名"
                        name="username"
                        rules={[
                            { required: true, message: '请输入用户名' },
                            { min: 3, message: '至少 3 个字符' },
                        ]}
                    >
                        <Input
                            size="large"
                            allowClear
                            prefix={<UserOutlined />}
                            placeholder="请输入用户名"
                            autoFocus
                            disabled={loading}
                        />
                    </Form.Item>

                    <Form.Item
                        label="密码"
                        name="password"
                        rules={[{ required: true, message: '请输入密码' }]}
                    >
                        <Input.Password
                            size="large"
                            prefix={<LockOutlined />}
                            placeholder="请输入密码"
                            disabled={loading}
                        />
                    </Form.Item>

                    <Button
                        type="primary"
                        htmlType="submit"
                        block
                        size="large"
                        icon={<ArrowRightOutlined />}
                        loading={loading}
                        className="login-button"
                    >
                        登录
                    </Button>

                    <div className="login-footer-links">
                        {/* <Typography.Text type="secondary">
                            忘记密码？
                            <Typography.Link href="#" style={{ marginLeft: 6 }}>
                                点此找回
                            </Typography.Link>
                        </Typography.Text> */}
                        <Typography.Text type="secondary">
                            还没有账号？
                            <Typography.Link href="/register" style={{ marginLeft: 6 }}>
                                注册
                            </Typography.Link>
                        </Typography.Text>
                    </div>
                </Form>
            </Card>

            <div className="login-copyright">
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    © {new Date().getFullYear()} BBS · All rights reserved
                </Typography.Text>
            </div>
        </div>
    )
}
