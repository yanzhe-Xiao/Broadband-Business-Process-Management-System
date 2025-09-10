// src/pages/profile/ProfilePage.tsx
import React, { useEffect, useState } from 'react'
import {
    Card, Avatar, Typography, Tag, Space, Skeleton, message,
    Button, Drawer, Form, Input, Upload, Divider, Descriptions, Tooltip, Modal
} from 'antd'
import {
    UserOutlined, MailOutlined, PhoneOutlined, SafetyCertificateOutlined,
    EditOutlined, LockOutlined, CameraOutlined
} from '@ant-design/icons'
import './home.css'
import { getProfile, changePassword, type Profile, updateAllProfile, calcStrength } from '../../../api/user'

const { Title, Text } = Typography

const statusColor: Record<string, string> = {
    active: 'green',
    disabled: 'red',
    pending: 'orange',
}

const ProfilePage: React.FC = () => {
    const [loading, setLoading] = useState(true)
    const [saving, setSaving] = useState(false)
    const [pwdOpen, setPwdOpen] = useState(false)
    const [editOpen, setEditOpen] = useState(false)
    const [data, setData] = useState<Profile | null>(null)
    const [form] = Form.useForm()
    const [pwdForm] = Form.useForm()

    // 拉取资料
    useEffect(() => {
        ; (async () => {
            try {
                const res = await getProfile()
                setData(res)
            } catch (e: any) {
                message.error(e?.message || '获取用户资料失败')
            } finally {
                setLoading(false)
            }
        })()
    }, [])

    // 打开编辑
    const onEdit = () => {
        form.setFieldsValue({
            fullName: data?.fullName,
            phone: data?.phone,
            email: data?.email,
            avatar: data?.avatar,
        })
        setEditOpen(true)
    }

    // 上传头像（前端转base64，直接随资料提交）
    const beforeUpload = async (file: File) => {
        const base64 = await fileToDataUrl(file)
        form.setFieldValue('avatar', base64)
        message.success('头像已更新（未保存）')
        return false // 阻止 antd 自动上传
    }

    const onSave = async () => {
        try {
            const values = await form.validateFields()
            setSaving(true)
            const updated = await updateAllProfile({
                ...values,
                username: data?.username ?? "",
                password: data?.password ?? "",
                roleName: data?.roleName ?? "客户",
                status: data?.status ?? "无"
            })
            setData(updated)
            message.success('资料已更新')
            setEditOpen(false)
        } catch (e: any) {
            if (!e?.errorFields) message.error(e?.message || '更新失败')
        } finally {
            setSaving(false)
        }
    }

    // 修改密码
    const onSavePwd = async () => {
        try {
            const { newPassword } = await pwdForm.validateFields()
            await changePassword(data?.username ?? "temp", newPassword)
            message.success('密码已修改')
            setPwdOpen(false)
            pwdForm.resetFields()
        } catch (e: any) {
            if (!e?.errorFields) message.error(e?.message || '修改失败')
        }
    }

    return (
        <div className="pro-root">
            {/* 顶部动效横幅 */}
            <div className="pro-hero">
                <div className="pro-hero-wave" />
                <div className="pro-hero-text">
                    <div className="chip">PROFILE</div>
                    <h1>个人中心</h1>
                    <p>管理你的账号信息、安全设置与个性化资料。</p>
                </div>
            </div>

            {/* 主卡片 */}
            <Card className="pro-card" bodyStyle={{ padding: 24 }}>
                {loading ? (
                    <Skeleton active avatar paragraph={{ rows: 8 }} />
                ) : data ? (
                    <div className="pro-grid">
                        {/* 左栏：头像与概览 */}
                        <div className="pro-left">
                            <div className="pro-avatar-wrap">
                                <Avatar
                                    size={120}
                                    src={data.avatar}
                                    icon={<UserOutlined />}
                                    className="pro-avatar"
                                />
                                <Tooltip title="更换头像（选择后记得保存）">
                                    <Upload
                                        accept="image/*"
                                        showUploadList={false}
                                        beforeUpload={beforeUpload}
                                    >
                                        <Button icon={<CameraOutlined />} className="pro-avatar-btn">更换头像</Button>
                                    </Upload>
                                </Tooltip>
                            </div>

                            <Title level={3} className="pro-name">{data.fullName}</Title>
                            <Tag color={statusColor[data.status] || 'blue'} className="pro-status">
                                {String(data.status).toUpperCase()}
                            </Tag>

                            <Space direction="vertical" size={6} style={{ marginTop: 16 }}>
                                <div className="pro-chip-row">
                                    <SafetyCertificateOutlined />
                                    <span>安全认证通过</span>
                                </div>
                                <div className="pro-chip-row">
                                    <UserOutlined />
                                    <span>角色：{data.roleName}</span>
                                </div>
                            </Space>

                            <div className="pro-actions">
                                <Button icon={<EditOutlined />} onClick={onEdit}>编辑资料</Button>
                                <Button icon={<LockOutlined />} onClick={() => setPwdOpen(true)}>修改密码</Button>
                            </div>
                        </div>

                        {/* 右栏：详细字段 */}
                        <div className="pro-right">
                            <Descriptions
                                title="基本信息"
                                bordered
                                column={1}
                                labelStyle={{ width: 120 }}
                                className="pro-desc"
                            >
                                <Descriptions.Item label="用户名">{data.username}</Descriptions.Item>
                                <Descriptions.Item label="姓名">{data.fullName}</Descriptions.Item>
                                <Descriptions.Item label="邮箱">
                                    {data.email ? (
                                        <Space><MailOutlined />{data.email}</Space>
                                    ) : <Text type="secondary">未填写</Text>}
                                </Descriptions.Item>
                                <Descriptions.Item label="电话">
                                    {data.phone ? (
                                        <Space><PhoneOutlined />{data.phone}</Space>
                                    ) : <Text type="secondary">未填写</Text>}
                                </Descriptions.Item>
                                <Descriptions.Item label="角色">{data.roleName}</Descriptions.Item>
                                <Descriptions.Item label="状态">
                                    <Tag color={statusColor[data.status] || 'blue'}>
                                        {String(data.status).toUpperCase()}
                                    </Tag>
                                </Descriptions.Item>
                            </Descriptions>

                            <Divider />

                            <Card className="pro-subcard" title="账号与安全" bordered={false}>
                                <div className="pro-safe">
                                    <div className="pro-safe-item">
                                        <div className="pro-safe-title">登录邮箱</div>
                                        <div className="pro-safe-value">{data.email || '未设置'}</div>
                                    </div>
                                    <div className="pro-safe-item">
                                        <div className="pro-safe-title">绑定手机</div>
                                        <div className="pro-safe-value">{data.phone || '未设置'}</div>
                                    </div>
                                    <div className="pro-safe-item">
                                        <div className="pro-safe-title">密码强度</div>
                                        <div className="pro-safe-value">
                                            {Array.from({ length: 3 }).map((_, i) => (
                                                <span
                                                    key={i}
                                                    className={`pro-strength ${calcStrength(data?.password ?? "123456") > i ? 'pro-strength-ok' : ''}`}
                                                />
                                            ))}
                                        </div>
                                    </div>
                                </div>
                            </Card>
                        </div>
                    </div>
                ) : (
                    <Text type="danger">暂无数据</Text>
                )}
            </Card>

            {/* 编辑抽屉 */}
            <Drawer
                title="编辑资料"
                open={editOpen}
                onClose={() => setEditOpen(false)}
                width={440}
                destroyOnClose
            >
                <Form form={form} layout="vertical" requiredMark={false}>
                    <Form.Item label="姓名" name="fullName" rules={[{ required: true, message: '请输入姓名' }]}>
                        <Input placeholder="请输入姓名" />
                    </Form.Item>
                    <Form.Item
                        label="邮箱"
                        name="email"
                        rules={[{ type: 'email', message: '邮箱格式不正确' }]}
                    >
                        <Input placeholder="name@example.com" prefix={<MailOutlined />} />
                    </Form.Item>
                    <Form.Item
                        label="电话"
                        name="phone"
                        rules={[
                            { pattern: /^1\d{10}$/, message: '手机号格式不正确（示例为大陆11位）' }
                        ]}
                    >
                        <Input placeholder="请输入手机号" prefix={<PhoneOutlined />} />
                    </Form.Item>
                    {/* 隐藏域：头像 base64 */}
                    <Form.Item name="avatar" hidden>
                        <Input />
                    </Form.Item>

                    <Space style={{ marginTop: 8 }}>
                        <Button onClick={() => setEditOpen(false)}>取消</Button>
                        <Button type="primary" loading={saving} onClick={onSave}>保存</Button>
                    </Space>
                </Form>
            </Drawer>

            {/* 修改密码弹窗（可选） */}
            <Modal
                title="修改密码"
                open={pwdOpen}
                onCancel={() => setPwdOpen(false)}
                onOk={onSavePwd}
                okText="保存"
                cancelText="取消"
                confirmLoading={saving}
            >
                <Form form={pwdForm} layout="vertical" requiredMark={false}>
                    <Form.Item
                        label="新密码"
                        name="newPassword"
                        rules={[
                            { required: true, message: '请输入新密码' },
                            { min: 6, message: '至少 6 位' }
                        ]}
                    >
                        <Input.Password />
                    </Form.Item>
                    <Form.Item
                        label="确认新密码"
                        name="confirmPassword"
                        dependencies={['newPassword']}
                        rules={[
                            { required: true, message: '请再次输入新密码' },
                            ({ getFieldValue }) => ({
                                validator(_, v) {
                                    if (!v || v === getFieldValue('newPassword')) return Promise.resolve()
                                    return Promise.reject(new Error('两次输入不一致'))
                                }
                            })
                        ]}
                    >
                        <Input.Password />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    )
}

export default ProfilePage

// 小工具：File -> base64
async function fileToDataUrl(file: File): Promise<string> {
    const reader = new FileReader()
    return await new Promise<string>((resolve, reject) => {
        reader.onload = () => resolve(reader.result as string)
        reader.onerror = reject
        reader.readAsDataURL(file)
    })
}
