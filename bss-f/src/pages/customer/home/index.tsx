import React, { useState } from 'react'
import {
    Card,
    Form,
    Input,
    Button,
    Typography,
    Divider,
    Avatar,
    Upload,
    Space,
    Tag,
    Descriptions,
    message,
} from 'antd'
import {
    UserOutlined,
    MailOutlined,
    PhoneOutlined,
    IdcardOutlined,
    CameraOutlined,
    LockOutlined,
} from '@ant-design/icons'
import { useAuthStore } from '../../../store/auth'
import './home.css'

const { Title, Text } = Typography

const Profile: React.FC = () => {
    // 你的全局用户信息（按你的 store 字段调整）
    const { username, roleName } = useAuthStore()
    const [avatarUrl, setAvatarUrl] = useState<string | undefined>(undefined)
    const [savingBase, setSavingBase] = useState(false)
    const [savingPwd, setSavingPwd] = useState(false)

    const [formBase] = Form.useForm()
    const [formPwd] = Form.useForm()

    const handleUpload = (file: File) => {
        // 仅预览，不实际上传；接入后端时改为请求上传接口
        const reader = new FileReader()
        reader.onload = e => setAvatarUrl(String(e.target?.result || ''))
        reader.readAsDataURL(file)
        return false // 阻止 antd 自动上传
    }

    const onSaveBase = async (values: any) => {
        setSavingBase(true)
        try {
            // TODO: 调用 updateProfileApi(values)
            message.success('已保存基本信息')
        } catch (e: any) {
            message.error(e?.message || '保存失败')
        } finally {
            setSavingBase(false)
        }
    }

    const onSavePwd = async (values: any) => {
        setSavingPwd(true)
        try {
            if (values.newPassword !== values.confirmPassword) {
                message.warning('两次新密码不一致')
                return
            }
            // TODO: 调用 changePasswordApi(values)
            message.success('密码修改成功')
            formPwd.resetFields()
        } catch (e: any) {
            message.error(e?.message || '修改失败')
        } finally {
            setSavingPwd(false)
        }
    }

    return (
        <div className="profile-page">
            <Card className="profile-card" bodyStyle={{ padding: 20 }}>
                {/* 头部 */}
                <div className="profile-header">
                    <div className="profile-badge">PRO</div>
                    <Title level={3} style={{ marginBottom: 4 }}>个人资料</Title>
                    <Text type="secondary">完善你的账户信息，享受更流畅的办理体验</Text>
                </div>

                <Divider style={{ margin: '14px 0 18px' }} />

                {/* 主体两栏 */}
                <div className="profile-grid">
                    {/* 左侧：头像与概览 */}
                    <Card className="profile-side" bordered={false}>
                        <div className="avatar-wrap">
                            <Avatar
                                size={108}
                                src={avatarUrl}
                                icon={<UserOutlined />}
                                className="avatar-large"
                            />
                            <Upload
                                accept="image/*"
                                showUploadList={false}
                                beforeUpload={handleUpload}
                            >
                                <Button icon={<CameraOutlined />} className="upload-btn">更换头像</Button>
                            </Upload>
                        </div>

                        <div className="user-brief">
                            <Title level={4} style={{ marginBottom: 2 }}>{username || '未登录'}</Title>
                            <Space size={[8, 8]} wrap>
                                <Tag color="geekblue">{roleName || '访客'}</Tag>
                            </Space>
                        </div>

                        <Descriptions
                            column={1}
                            size="small"
                            className="brief-desc"
                            items={[
                                { key: 'u', label: '用户名', children: username || '-' },
                                { key: 'r', label: '角色', children: roleName || '-' },
                            ]}
                        />
                    </Card>

                    {/* 右侧：表单区块 */}
                    <div className="profile-main">
                        {/* 基本信息 */}
                        <Card className="block-card" bordered={false}>
                            <Title level={5} style={{ marginBottom: 12 }}>基本信息</Title>
                            <Form
                                form={formBase}
                                layout="vertical"
                                onFinish={onSaveBase}
                                requiredMark={false}
                                initialValues={{
                                    fullName: '',
                                    email: '',
                                    phone: '',
                                    idNo: '',
                                }}
                            >
                                <div className="form-grid">
                                    <Form.Item
                                        label="姓名"
                                        name="fullName"
                                        rules={[{ required: true, message: '请输入姓名' }]}
                                    >
                                        <Input prefix={<IdcardOutlined />} placeholder="请输入姓名" />
                                    </Form.Item>

                                    <Form.Item
                                        label="邮箱"
                                        name="email"
                                        rules={[{ type: 'email', message: '邮箱格式不正确' }]}
                                    >
                                        <Input prefix={<MailOutlined />} placeholder="用于接收通知与找回密码" />
                                    </Form.Item>

                                    <Form.Item
                                        label="电话"
                                        name="phone"
                                        rules={[{ pattern: /^1\d{10}$/, message: '手机号格式不正确' }]}
                                    >
                                        <Input prefix={<PhoneOutlined />} placeholder="用于验证与联系" />
                                    </Form.Item>

                                    <Form.Item
                                        label="证件号"
                                        name="idNo"
                                    >
                                        <Input prefix={<IdcardOutlined />} placeholder="选填" />
                                    </Form.Item>
                                </div>

                                <div className="form-actions">
                                    <Button
                                        type="primary"
                                        htmlType="submit"
                                        loading={savingBase}
                                        className="profile-primary-btn"
                                    >
                                        保存信息
                                    </Button>
                                </div>
                            </Form>
                        </Card>

                        {/* 账号安全 */}
                        <Card className="block-card" bordered={false}>
                            <Title level={5} style={{ marginBottom: 12 }}>账号安全</Title>
                            <Form
                                form={formPwd}
                                layout="vertical"
                                onFinish={onSavePwd}
                                requiredMark={false}
                            >
                                <div className="form-grid">
                                    <Form.Item
                                        label="当前密码"
                                        name="oldPassword"
                                        rules={[{ required: true, message: '请输入当前密码' }]}
                                    >
                                        <Input.Password prefix={<LockOutlined />} placeholder="当前密码" />
                                    </Form.Item>

                                    <Form.Item
                                        label="新密码"
                                        name="newPassword"
                                        rules={[{ required: true, message: '请输入新密码' }, { min: 6, message: '至少 6 位' }]}
                                    >
                                        <Input.Password prefix={<LockOutlined />} placeholder="新密码（至少 6 位）" />
                                    </Form.Item>

                                    <Form.Item
                                        label="确认新密码"
                                        name="confirmPassword"
                                        dependencies={['newPassword']}
                                        rules={[{ required: true, message: '请再次输入新密码' }]}
                                    >
                                        <Input.Password prefix={<LockOutlined />} placeholder="再次输入新密码" />
                                    </Form.Item>
                                </div>

                                <div className="form-actions">
                                    <Button
                                        type="primary"
                                        htmlType="submit"
                                        loading={savingPwd}
                                        className="profile-primary-btn"
                                    >
                                        修改密码
                                    </Button>
                                </div>
                            </Form>
                        </Card>
                    </div>
                </div>
            </Card>
        </div>
    )
}

export default Profile
