import React, { useEffect, useState } from 'react'
import {
    Avatar,
    Button,
    Card,
    Drawer,
    Form,
    Image,
    Input,
    Modal,
    Popconfirm,
    Select,
    Space,
    Table,
    Tag,
    Upload,
    message,
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import {
    ReloadOutlined,
    EditOutlined,
    DeleteOutlined,
    EyeOutlined,
    UploadOutlined,
    KeyOutlined,
    UserAddOutlined,
    SearchOutlined,
} from '@ant-design/icons'
import './user.css'
import {
    listUsers,
    createUser,
    updateUser,
    deleteUser,
    setUserStatus,
    adminResetPassword,
    uploadMyAvatar, // 可选给“编辑本人头像”，此处也用于管理员新增/编辑通用上传
    type PageReq,
    type UserRow,
    type UserStatus,
} from '../../../api/user'

/** 可选：你系统支持的角色列表（也可从后端获取） */
const roleOptions = [
    { label: '平台管理员', value: '平台管理员' },
    { label: '客服坐席', value: '客服坐席' },
    { label: '装维工程师', value: '装维工程师' },
    { label: '客户', value: '客户' },
    { label: 'admin', value: 'admin' },
    { label: 'user', value: 'user' },
]

const statusText: Record<UserStatus | string, string> = {
    active: '已启用',
    disabled: '已禁用',
    pending: '待审核',
}

const statusColor: Record<UserStatus | string, string> = {
    active: 'success',
    disabled: 'default',
    pending: 'warning',
}

/** 创建/编辑表单类型 */
type UpsertForm = {
    id?: string
    username: string
    password?: string
    fullName: string
    roleName: string
    status?: UserStatus
    email?: string
    phone?: string
    avatar?: string
}

const AdminUsers: React.FC = () => {
    // ========= 查询区 =========
    const [form] = Form.useForm()
    const [loading, setLoading] = useState(false)
    const [data, setData] = useState<UserRow[]>([])
    const [current, setCurrent] = useState(1)
    const [size, setSize] = useState(10)
    const [total, setTotal] = useState(0)
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])

    // ========= 抽屉查看 =========
    const [viewOpen, setViewOpen] = useState(false)
    const [viewRow, setViewRow] = useState<UserRow | null>(null)

    // ========= 新增/编辑 =========
    const [upOpen, setUpOpen] = useState(false)
    const [upForm] = Form.useForm<UpsertForm>()
    const [editing, setEditing] = useState<UserRow | null>(null)
    const [submitLoading, setSubmitLoading] = useState(false)

    // ========= 重置密码 =========
    const [resetOpen, setResetOpen] = useState(false)
    const [resetForm] = Form.useForm<{ username: string; newPassword: string }>()
    const [resetLoading, setResetLoading] = useState(false)

    const fetchList = async (page = current, pageSize = size) => {
        setLoading(true)
        try {
            const params: PageReq = {
                current: page,
                size: pageSize,
                keyword: form.getFieldValue('keyword') || '',
                status: form.getFieldValue('status') || 'all',
                roleName: form.getFieldValue('roleName'),
            }
            const resp = await listUsers(params)
            setData(resp.records)
            setTotal(resp.total)
            setCurrent(resp.current)
            setSize(resp.size)
            setSelectedRowKeys(prev => prev.filter(k => resp.records.some(i => i.id === k)))
        } catch (e: any) {
            message.error(e?.message || '获取用户列表失败')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { fetchList(1, size) }, []) // eslint-disable-line

    const columns: ColumnsType<UserRow> = [
        {
            title: '用户',
            dataIndex: 'username',
            key: 'username',
            width: 260,
            render: (_: any, r) => (
                <div className="user-cell">
                    <Avatar src={r.avatar} size={40}>
                        {(!r.avatar && r.fullName) ? r.fullName[0] : ''}
                    </Avatar>
                    <div className="user-meta">
                        <div className="user-name" title={r.fullName || r.username}>
                            {r.fullName || '-'}
                            <span className="uname">@{r.username}</span>
                        </div>
                        <div className="user-sub">
                            <Tag color="geekblue">{r.roleName}</Tag>
                            <Tag color={statusColor[r.status] || 'default'}>{statusText[r.status] || r.status}</Tag>
                        </div>
                    </div>
                </div>
            ),
        },
        {
            title: '联系方式',
            key: 'contact',
            width: 260,
            render: (_, r) => (
                <div className="contact">
                    <div className="line"><span className="k">Email</span><span className="v">{r.email || '-'}</span></div>
                    <div className="line"><span className="k">Phone</span><span className="v">{r.phone || '-'}</span></div>
                </div>
            ),
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 140,
            render: (s: string, r) => (
                <Select
                    size="small"
                    value={s}
                    options={[
                        { label: '已启用', value: 'active' },
                        { label: '已禁用', value: 'disabled' },
                        { label: '待审核', value: 'pending' },
                    ]}
                    onChange={async (val) => {
                        const hide = message.loading('更新状态...')
                        try {
                            await setUserStatus(r.id, val as UserStatus)
                            message.success('状态已更新')
                            setData(prev => prev.map(it => it.id === r.id ? { ...it, status: val } : it))
                        } catch (e: any) {
                            message.error(e?.message || '更新状态失败')
                        } finally {
                            hide()
                        }
                    }}
                    style={{ minWidth: 110 }}
                />
            ),
        },
        {
            title: '操作',
            key: 'action',
            fixed: 'right',
            width: 260,
            render: (_, r) => (
                <Space wrap>
                    <Button size="small" icon={<EyeOutlined />} onClick={() => { setViewRow(r); setViewOpen(true) }}>
                        查看
                    </Button>
                    <Button
                        size="small"
                        icon={<EditOutlined />}
                        onClick={() => {
                            setEditing(r)
                            setUpOpen(true)
                            upForm.setFieldsValue({
                                id: r.id,
                                username: r.username,
                                fullName: r.fullName,
                                roleName: r.roleName,
                                status: (['active', 'disabled', 'pending'] as string[]).includes(r.status) ? (r.status as UserStatus) : 'active',
                                email: r.email,
                                phone: r.phone,
                                avatar: r.avatar,
                            })
                        }}
                    >
                        编辑
                    </Button>
                    <Button
                        size="small"
                        icon={<KeyOutlined />}
                        onClick={() => {
                            setResetOpen(true)
                            resetForm.setFieldsValue({ username: r.username, newPassword: '' })
                        }}
                    >
                        重置密码
                    </Button>
                    <Popconfirm
                        title="确认删除该用户？"
                        okText="删除"
                        okButtonProps={{ danger: true }}
                        onConfirm={async () => {
                            try {
                                await deleteUser(r.id)
                                message.success('已删除')
                                fetchList()
                            } catch (e: any) {
                                message.error(e?.message || '删除失败')
                            }
                        }}
                    >
                        <Button size="small" icon={<DeleteOutlined />} danger>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ]

    // ========= 顶部合计/筛选栏 =========
    const toolbar = (
        <div className="users-toolbar">
            <div className="left">
                <div className="badge">USR</div>
                <div className="title-wrap">
                    <h3>用户管理</h3>
                    <p>共 <b>{total}</b> 位用户</p>
                </div>
            </div>
            <Space wrap>
                <Button icon={<ReloadOutlined />} onClick={() => fetchList()} />
                <Popconfirm
                    title="确认删除选中用户？"
                    okText="删除"
                    okButtonProps={{ danger: true }}
                    disabled={selectedRowKeys.length === 0}
                    onConfirm={async () => {
                        try {
                            // 逐个删除（也可后端提供批量删除接口）
                            for (const id of selectedRowKeys) {
                                await deleteUser(String(id))
                            }
                            message.success('已删除所选')
                            setSelectedRowKeys([])
                            fetchList()
                        } catch (e: any) {
                            message.error(e?.message || '批量删除失败')
                        }
                    }}
                >
                    <Button danger disabled={selectedRowKeys.length === 0} icon={<DeleteOutlined />}>
                        删除所选
                    </Button>
                </Popconfirm>
                <Button
                    type="primary"
                    icon={<UserAddOutlined />}
                    className="gradient-btn"
                    onClick={() => {
                        setEditing(null)
                        setUpOpen(true)
                        upForm.resetFields()
                        upForm.setFieldsValue({ roleName: 'user', status: 'active' })
                    }}
                >
                    新增用户
                </Button>
            </Space>
        </div>
    )

    return (
        <Card className="users-card" bodyStyle={{ padding: 16 }}>
            {toolbar}

            {/* 筛选 */}
            <Form
                form={form}
                layout="inline"
                className="users-filters"
                initialValues={{ status: 'all' }}
                onFinish={() => fetchList(1, size)}
            >
                <Form.Item name="keyword">
                    <Input
                        allowClear
                        prefix={<SearchOutlined />}
                        placeholder="用户名/姓名/邮箱/手机号"
                        style={{ width: 280 }}
                    />
                </Form.Item>
                <Form.Item name="roleName">
                    <Select
                        allowClear
                        placeholder="角色"
                        options={roleOptions}
                        style={{ width: 160 }}
                    />
                </Form.Item>
                <Form.Item name="status">
                    <Select
                        style={{ width: 140 }}
                        options={[
                            { label: '全部', value: 'all' },
                            { label: '已启用', value: 'active' },
                            { label: '已禁用', value: 'disabled' },
                            { label: '待审核', value: 'pending' },
                        ]}
                    />
                </Form.Item>
                <Form.Item>
                    <Button type="primary" htmlType="submit" className="gradient-btn">查询</Button>
                </Form.Item>
                <Form.Item>
                    <Button onClick={() => { form.resetFields(); fetchList(1, size) }}>重置</Button>
                </Form.Item>
            </Form>

            {/* 表格 */}
            <Table<UserRow>
                rowKey="id"
                loading={loading}
                columns={columns}
                dataSource={data}
                className="users-table"
                tableLayout="fixed"
                scroll={{ x: 'max-content' }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: setSelectedRowKeys,
                    selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT, Table.SELECTION_NONE],
                }}
                pagination={{
                    current,
                    pageSize: size,
                    total,
                    showSizeChanger: true,
                    showTotal: t => `共 ${t} 条`,
                    onChange: (p, ps) => { setCurrent(p); setSize(ps); fetchList(p, ps) },
                }}
            />

            {/* 查看侧滑 */}
            <Drawer
                title={null}
                width={520}
                open={viewOpen}
                onClose={() => { setViewOpen(false); setViewRow(null) }}
                destroyOnClose
                styles={{ body: { padding: 0 } }}
            >
                {viewRow && (
                    <div className="user-view">
                        <div className="uv-head">
                            <div className="uv-badge">VIEW</div>
                            <div className="uv-title">{viewRow.fullName || viewRow.username}</div>
                            <Tag color={statusColor[viewRow.status] || 'default'}>{statusText[viewRow.status] || viewRow.status}</Tag>
                        </div>
                        <div className="uv-body">
                            <div className="uv-avatar">
                                <Image
                                    src={viewRow.avatar || 'https://picsum.photos/seed/user/200/200'}
                                    width={120}
                                    height={120}
                                    style={{ objectFit: 'cover', borderRadius: 16 }}
                                    preview={true}
                                />
                            </div>

                            <div className="uv-grid">
                                <div className="uv-item"><span className="k">用户名</span><span className="v">{viewRow.username}</span></div>
                                <div className="uv-item"><span className="k">姓名</span><span className="v">{viewRow.fullName || '-'}</span></div>
                                <div className="uv-item"><span className="k">角色</span><span className="v">{viewRow.roleName}</span></div>
                                <div className="uv-item"><span className="k">邮箱</span><span className="v">{viewRow.email || '-'}</span></div>
                                <div className="uv-item"><span className="k">电话</span><span className="v">{viewRow.phone || '-'}</span></div>
                                <div className="uv-item"><span className="k">状态</span>
                                    <span className="v">
                                        <Tag color={statusColor[viewRow.status] || 'default'}>{statusText[viewRow.status] || viewRow.status}</Tag>
                                    </span>
                                </div>
                            </div>
                        </div>
                        <div className="uv-actions">
                            <Space>
                                <Button icon={<EditOutlined />} onClick={() => {
                                    setViewOpen(false)
                                    setEditing(viewRow)
                                    setUpOpen(true)
                                    upForm.setFieldsValue({
                                        id: viewRow.id,
                                        username: viewRow.username,
                                        fullName: viewRow.fullName,
                                        roleName: viewRow.roleName,
                                        status: (['active', 'disabled', 'pending'] as string[]).includes(viewRow.status) ? (viewRow.status as UserStatus) : 'active',
                                        email: viewRow.email,
                                        phone: viewRow.phone,
                                        avatar: viewRow.avatar,
                                    })
                                }}>编辑</Button>

                                <Popconfirm
                                    title="确认删除该用户？"
                                    okText="删除"
                                    okButtonProps={{ danger: true }}
                                    onConfirm={async () => {
                                        try {
                                            await deleteUser(viewRow.id)
                                            message.success('已删除')
                                            setViewOpen(false)
                                            setViewRow(null)
                                            fetchList()
                                        } catch (e: any) {
                                            message.error(e?.message || '删除失败')
                                        }
                                    }}
                                >
                                    <Button icon={<DeleteOutlined />} danger>删除</Button>
                                </Popconfirm>
                            </Space>
                        </div>
                    </div>
                )}
            </Drawer>

            {/* 新增/编辑弹窗 */}
            <Modal
                title={editing ? '编辑用户' : '新增用户'}
                open={upOpen}
                onCancel={() => { setUpOpen(false); setEditing(null); upForm.resetFields() }}
                onOk={async () => {
                    try {
                        const values = await upForm.validateFields()
                        setSubmitLoading(true)
                        if (editing) {
                            // 编辑
                            const { id, username, ...rest } = values
                            await updateUser(editing.id, rest)
                            message.success('保存成功')
                        } else {
                            // 新增
                            if (!values.password) {
                                return message.warning('请设置初始密码')
                            }
                            await createUser({
                                username: values.username,
                                password: values.password,
                                fullName: values.fullName,
                                roleName: values.roleName,
                                email: values.email,
                                phone: values.phone,
                                status: values.status || 'active',
                                avatar: values.avatar,
                            })
                            message.success('创建成功')
                        }
                        setUpOpen(false)
                        setEditing(null)
                        upForm.resetFields()
                        fetchList()
                    } catch (e: any) {
                        if (e?.errorFields) return // 表单校验已提示
                        message.error(e?.message || '保存失败')
                    } finally {
                        setSubmitLoading(false)
                    }
                }}
                okText="保存"
                cancelText="取消"
                okButtonProps={{ className: 'gradient-btn' as any, loading: submitLoading }}
                destroyOnClose
            >
                <Form
                    form={upForm}
                    layout="vertical"
                    initialValues={{ status: 'active', roleName: 'user' }}
                >
                    <Form.Item label="头像" name="avatar" valuePropName="fileUrl">
                        <Upload
                            listType="picture-card"
                            maxCount={1}
                            showUploadList={false}
                            beforeUpload={async (file) => {
                                try {
                                    // 这里复用上传接口；也可以改为你的通用上传接口
                                    const { avatar } = await uploadMyAvatar(file)
                                    upForm.setFieldValue('avatar', avatar)
                                } catch (e: any) {
                                    message.error(e?.message || '上传失败')
                                }
                                return Upload.LIST_IGNORE
                            }}
                        >
                            {upForm.getFieldValue('avatar') ? (
                                <img src={upForm.getFieldValue('avatar')} alt="avatar" style={{ width: '100%' }} />
                            ) : (
                                <div>
                                    <UploadOutlined />
                                    <div style={{ marginTop: 8 }}>上传</div>
                                </div>
                            )}
                        </Upload>
                    </Form.Item>

                    <Form.Item
                        label="用户名"
                        name="username"
                        rules={[
                            { required: true, message: '请输入用户名' },
                            { min: 3, message: '至少 3 个字符' },
                        ]}
                    >
                        <Input placeholder="唯一账号" disabled={!!editing} />
                    </Form.Item>

                    {!editing && (
                        <Form.Item
                            label="初始密码"
                            name="password"
                            rules={[{ required: true, message: '请输入初始密码' }, { min: 6, message: '至少 6 位' }]}
                        >
                            <Input.Password placeholder="用于首次登录" />
                        </Form.Item>
                    )}

                    <Form.Item
                        label="姓名"
                        name="fullName"
                        rules={[{ required: true, message: '请输入姓名' }]}
                    >
                        <Input placeholder="真实姓名" />
                    </Form.Item>

                    <Form.Item label="角色" name="roleName" rules={[{ required: true, message: '请选择角色' }]}>
                        <Select options={roleOptions} placeholder="选择角色" />
                    </Form.Item>

                    <Form.Item label="状态" name="status">
                        <Select
                            options={[
                                { label: '已启用', value: 'active' },
                                { label: '已禁用', value: 'disabled' },
                                { label: '待审核', value: 'pending' },
                            ]}
                        />
                    </Form.Item>

                    <Form.Item label="邮箱" name="email" rules={[{ type: 'email', message: '邮箱格式不正确' }]}>
                        <Input placeholder="name@example.com" />
                    </Form.Item>

                    <Form.Item label="手机" name="phone" rules={[{ pattern: /^1\d{10}$/, message: '手机号格式不正确' }]}>
                        <Input placeholder="11位手机号" />
                    </Form.Item>
                </Form>
            </Modal>

            {/* 重置密码 */}
            <Modal
                title="重置密码"
                open={resetOpen}
                onCancel={() => { setResetOpen(false); resetForm.resetFields() }}
                onOk={async () => {
                    try {
                        const v = await resetForm.validateFields()
                        setResetLoading(true)
                        await adminResetPassword(v.username, v.newPassword)
                        message.success('已重置密码')
                        setResetOpen(false)
                        resetForm.resetFields()
                    } catch (e: any) {
                        if (e?.errorFields) return
                        message.error(e?.message || '重置失败')
                    } finally {
                        setResetLoading(false)
                    }
                }}
                okText="确定"
                cancelText="取消"
                okButtonProps={{ className: 'gradient-btn' as any, loading: resetLoading }}
                destroyOnClose
            >
                <Form form={resetForm} layout="vertical">
                    <Form.Item label="用户名" name="username">
                        <Input disabled />
                    </Form.Item>
                    <Form.Item
                        label="新密码"
                        name="newPassword"
                        rules={[{ required: true, message: '请输入新密码' }, { min: 6, message: '至少 6 位' }]}
                    >
                        <Input.Password placeholder="新密码" />
                    </Form.Item>
                </Form>
            </Modal>
        </Card>
    )
}

export default AdminUsers
