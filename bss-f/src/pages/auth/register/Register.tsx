import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Button, Card, Form, Input, Select, Typography, message, Divider } from 'antd'
import { UserOutlined, LockOutlined, MailOutlined, PhoneOutlined, IdcardOutlined, ArrowRightOutlined, TeamOutlined } from '@ant-design/icons'
import { registerApi } from '../../../api/auth'
import './RegisterPage.css'

const phoneRule = /^1\d{10}$/  // 中国大陆手机号示例：以1开头共11位

export default function RegisterPage() {
    const nav = useNavigate()
    const [form] = Form.useForm()
    const [loading, setLoading] = useState(false)
    const [messageApi, contextHolder] = message.useMessage()

    const onFinish = async (values: any) => {
        const { confirmPassword, ...payload } = values
        setLoading(true)
        try {
            console.log(1111);
            await registerApi(payload)
            messageApi.open({
                type: 'success',
                content: '注册成功，请登录',
                duration: 1,             // 展示 1s
                onClose: () => nav('/login', { replace: true }), // 关闭后再跳转
            })

        } catch (e: any) {
            messageApi.open({ type: 'error', content: e?.response?.data?.message ?? '注册失败' })
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="register-page">
            {contextHolder}

            <Card className="register-card" bodyStyle={{ padding: 34 }}>
                <div className="register-header">
                    <div className="register-badge">
                        <span>REG</span>
                    </div>
                    <Typography.Title level={3} style={{ marginBottom: 4 }}>
                        注册
                    </Typography.Title>
                    <Typography.Text type="secondary">
                        创建你的账户，开始使用吧
                    </Typography.Text>
                </div>

                <Divider style={{ margin: '14px 0 18px' }} />

                <Form
                    form={form}
                    layout="vertical"
                    onFinish={onFinish}
                    autoComplete="off"
                    validateTrigger={["onBlur", "onSubmit"]}
                    requiredMark={false}
                    initialValues={{ roleName: '客户' }}
                    className="register-form"
                >
                    <Form.Item
                        label="用户名："
                        name="username"
                        className="register-form-item"
                        rules={[
                            { required: true, message: '请输入用户名' },
                            { min: 4, message: '用户名至少 4 个字符' },
                            { max: 32, message: '用户名最多 32 个字符' },
                        ]}
                    >
                        <Input placeholder="请输入用户名" prefix={<UserOutlined />} allowClear size="large" />
                    </Form.Item>

                    <Form.Item
                        label="密码："
                        name="password"
                        className="register-form-item"
                        rules={[{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少 6 位' }]}
                        hasFeedback
                    >
                        <Input.Password placeholder="请输入密码" prefix={<LockOutlined />} size="large" />
                    </Form.Item>

                    <Form.Item
                        label="确认密码："
                        name="confirmPassword"
                        className="register-form-item"
                        dependencies={["password"]}
                        hasFeedback
                        rules={[
                            { required: true, message: '请再次输入密码' },
                            ({ getFieldValue }) => ({
                                validator(_, value) {
                                    if (!value || getFieldValue('password') === value) return Promise.resolve()
                                    return Promise.reject(new Error('两次输入的密码不一致'))
                                },
                            }),
                        ]}
                    >
                        <Input.Password placeholder="请再次输入密码" prefix={<LockOutlined />} size="large" />
                    </Form.Item>

                    <Form.Item label="姓名：" name="fullName" className="register-form-item" rules={[{ required: true, message: '请输入姓名' }]}>
                        <Input placeholder="请输入姓名" prefix={<IdcardOutlined />} allowClear size="large" />
                    </Form.Item>

                    <Form.Item
                        label="电话："
                        name="phone"
                        className="register-form-item"
                        rules={[
                            { required: true, message: '请输入电话' },
                            { pattern: phoneRule, message: '电话号码格式不正确' },
                        ]}
                    >
                        <Input placeholder="请输入电话" prefix={<PhoneOutlined />} allowClear size="large" />
                    </Form.Item>

                    <Form.Item
                        label="邮箱："
                        name="email"
                        className="register-form-item"
                        rules={[{ required: true, message: '请输入邮箱' }, { type: 'email', message: '邮箱格式不正确' }]}
                    >
                        <Input placeholder="请输入邮箱" prefix={<MailOutlined />} allowClear size="large" />
                    </Form.Item>

                    <Form.Item label="角色：" name="roleName" className="register-form-item" rules={[{ required: true, message: '请选择角色' }]}>
                        <Select
                            size="large"
                            options={[
                                { value: '客户', label: '客户' },
                                { value: '平台管理员', label: '平台管理员' },
                                { value: '客服坐席', label: '客服坐席' },
                                { value: '装维工程师', label: '装维工程师' }
                            ]}
                            suffixIcon={<TeamOutlined />}
                        />
                    </Form.Item>

                    <Button
                        type="primary"
                        htmlType="submit"
                        block
                        size="large"
                        icon={<ArrowRightOutlined />}
                        loading={loading}
                        className="register-button"
                    >
                        注册
                    </Button>

                    <div className="register-footer-links">
                        <Typography.Text type="secondary">
                            已有账号？ <Link to="/login">去登录</Link>
                        </Typography.Text>
                    </div>
                </Form>
            </Card>

            <div className="register-copyright">
                <Typography.Text type="secondary" style={{ fontSize: 12 }}>
                    © {new Date().getFullYear()} Your Company · All rights reserved
                </Typography.Text>
            </div>
        </div>
    )
}
