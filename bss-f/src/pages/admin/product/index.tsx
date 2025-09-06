import React, { useEffect, useState } from 'react'
import {
    PlusOutlined,
    ReloadOutlined,
    SearchOutlined,
} from '@ant-design/icons'
import {
    Button,
    Card,
    Form,
    Input,
    InputNumber,
    Select,
    Space,
    Table,
    Tag,
    message,
    Modal,
    Image,
    Popconfirm,
    Checkbox
} from 'antd'
import type { ColumnsType, TableProps } from 'antd/es/table'
import './product.css'
import {
    getPlanPage,
    updatePlan,
    deletePlan,
    type AdminPlanItem,
    type AdminPlanQuery,
    addPlan,
    type addTariffPlans,
} from '../../../api/admin'

const statusOptions = [
    { label: '在售', value: 'onSale' },
    { label: '下架', value: 'offSale' },
]

const isIpOptions = [
    { label: '是', value: 1 },
    { label: '否', value: 2 },
]

const Product: React.FC = () => {
    // 查询表单
    const [form] = Form.useForm<AdminPlanQuery>()

    // 列表状态
    const [loading, setLoading] = useState(false)
    const [data, setData] = useState<AdminPlanItem[]>([])
    const [total, setTotal] = useState(0)
    const [pagination, setPagination] = useState({ current: 1, size: 10 })
    const [sorter, setSorter] = useState<{ field?: string; order?: 'ascend' | 'descend' }>({})

    // 弹窗（新增/编辑）
    const [open, setOpen] = useState(false)
    const [editing, setEditing] = useState<AdminPlanItem | null>(null)
    const [upForm] = Form.useForm<AdminPlanItem>()

    const fetchList = async (resetPage?: boolean) => {
        const values = form.getFieldsValue()
        const current = resetPage ? 1 : pagination.current
        setLoading(true)
        try {
            const resp = await getPlanPage({
                ...values,
                current,
                size: pagination.size,
                sortField: sorter.field,
                sortOrder: sorter.order === 'ascend' ? 'asc' : sorter.order === 'descend' ? 'desc' : undefined,
            })
            setData(resp.records)
            setTotal(resp.total)
            setPagination(p => ({ ...p, current: resp.current, size: resp.size }))
        } catch (e: any) {
            message.error(e?.message || '获取套餐失败')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchList(true)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    const onTableChange: TableProps<AdminPlanItem>['onChange'] = (pg, _filters, sort) => {
        const s = Array.isArray(sort) ? sort[0] : sort
        setSorter({ field: (s?.field as string) || undefined, order: s?.order as any })
        setPagination({ current: pg.current || 1, size: pg.pageSize || 10 })
        setTimeout(() => fetchList(), 0)
    }
    // 小工具
    // const fmtCNY = (v?: number) => (typeof v === 'number' ? `¥ ${v.toFixed(2)}` : '-')

    // 统一状态/需求 IP 渲染
    // const StatusTag: React.FC<{ s?: string }> = ({ s }) =>
    //     s === 'onSale' ? <Tag color="green">在售</Tag> : <Tag>下架</Tag>

    // const IpTag: React.FC<{ v?: string | number | boolean }> = ({ v }) =>
    //     String(v) === '1' || v === true ? <Tag color="geekblue">需IP</Tag> : <Tag>无需IP</Tag>
    const columns: ColumnsType<AdminPlanItem> = [
        {
            title: '展示', dataIndex: 'imageBase64', key: 'imageBase64', width: 110,
            render: (b64: string, r) => {
                const src = b64?.startsWith('http') ? b64 : b64 ? `data:image/*;base64,${b64}` : r.picture
                return <Image src={src} width={88} height={56} style={{ objectFit: 'cover', borderRadius: 8 }} />
            }
        },
        { title: '套餐编码', dataIndex: 'planCode', key: 'planCode', width: 150, sorter: true, ellipsis: true },
        { title: '套餐名称', dataIndex: 'name', key: 'name', width: 220, ellipsis: true },
        {
            title: '售卖价(¥)', dataIndex: 'price', key: 'price', width: 120, sorter: true,
            render: (v) => (typeof v === 'number' ? `¥ ${v.toFixed(2)}` : '-')
        },
        {
            title: '计费', key: 'billing', width: 240, ellipsis: true,
            render: (_, r) => (
                <span className="billing-tags">
                    {r.monthlyPrice != null && <Tag color="processing">月 {`¥ ${Number(r.monthlyPrice).toFixed(2)}`}</Tag>}
                    {r.yearlyPrice != null && <Tag color="success">年 {`¥ ${Number(r.yearlyPrice).toFixed(2)}`}</Tag>}
                    {r.lifetimePrice != null && <Tag color="purple">永久 {`¥ ${Number(r.lifetimePrice).toFixed(2)}`}</Tag>}
                    {!r.monthlyPrice && !r.yearlyPrice && !r.lifetimePrice && <Tag>未设置</Tag>}
                </span>
            )
        },
        { title: '有效期(月)', dataIndex: 'planPeriod', key: 'planPeriod', width: 110, sorter: true },
        {
            title: '折扣(%)', dataIndex: 'discount', key: 'discount', width: 100, sorter: true,
            render: (v) => v == null ? '-' : `${v}%`
        },
        { title: '库存', dataIndex: 'qty', key: 'qty', width: 100 },
        { title: '设备数', dataIndex: 'deviceQty', key: 'deviceQty', width: 110 },
        { title: '带宽(Mbps)', dataIndex: 'bandwidth', key: 'bandwidth', width: 120, sorter: true },
        {
            title: '评分', dataIndex: 'rating', key: 'rating', width: 90, sorter: true,
            render: (v) => v != null ? Number(v).toFixed(1) : '-'
        },
        {
            title: '状态', dataIndex: 'status', key: 'status', width: 100,
            render: (s) => s === 'onSale' ? <Tag color="green">在售</Tag> : <Tag>下架</Tag>
        },

        // ⭐ 固定在右侧的操作列
        {
            title: '操作',
            key: 'action',
            width: 200,                 // ⚠️ 给足宽度，避免拥挤
            fixed: 'right',
            className: 'action-col',    // 用于加背景与层级修复
            render: (_, record) => (
                <Space>
                    <Button size="small" onClick={() => { setEditing(record); setOpen(true); upForm.setFieldsValue(record) }}>编辑</Button>
                    <Popconfirm title="确认删除该套餐？" okText="删除" okButtonProps={{ danger: true }} onConfirm={() => handleDelete(record.planCode)}>
                        <Button size="small" danger>删除</Button>
                    </Popconfirm>
                </Space>
            ),
        },
    ]


    const handleDelete = async (planCode: string) => {
        try {
            await deletePlan(planCode)
            message.success('已删除')
            fetchList()
        } catch (e: any) {
            message.error(e?.message || '删除失败')
        }
    }
    const billingTypes = Form.useWatch('billingTypes', upForm) || []

    const onSubmitUpsert = async () => {
        try {
            const values = await upForm.validateFields()
            console.log(values);

            if (editing) {
                await updatePlan(values as AdminPlanItem)
                message.success('已保存修改')
            } else {
                // await createPlan(values as AdminPlanItem)
                await addPlan(values as addTariffPlans)
                message.success('新增成功')
            }
            setOpen(false)
            setEditing(null)
            upForm.resetFields()
            fetchList()
        } catch {
            /* 校验/请求错误已提示 */
        }
    }

    return (
        <div className="admin-plans-page">
            <Card className="admin-plans-card" bodyStyle={{ padding: 16 }}>
                {/* 顶部标题 + 快捷操作 */}
                <div className="admin-plans-header">
                    <div className="badge">ADM</div>
                    <div className="title-wrap">
                        <h3>套餐管理</h3>
                        <p>支持搜索、筛选、排序、分页与新增/编辑/删除</p>
                    </div>
                    <Space>
                        <Button icon={<ReloadOutlined />} onClick={() => fetchList()} />
                        <Button type="primary" className="gradient-btn" icon={<PlusOutlined />} onClick={() => setOpen(true)}>
                            新增套餐
                        </Button>
                    </Space>
                </div>

                {/* 工具条：搜索 / 筛选 / 价格区间 */}
                <Form
                    form={form}
                    layout="inline"
                    className="admin-plans-toolbar"
                    onFinish={() => fetchList(true)}
                    initialValues={{ keyword: '', status: undefined, minPrice: undefined, maxPrice: undefined }}
                >
                    <Form.Item name="keyword">
                        <Input
                            allowClear
                            prefix={<SearchOutlined />}
                            placeholder="名称 / 编码 关键词"
                            style={{ width: 260 }}
                        />
                    </Form.Item>

                    <Form.Item name="status" style={{ minWidth: 160 }}>
                        <Select allowClear options={statusOptions} placeholder="状态" />
                    </Form.Item>

                    <Form.Item name="minPrice">
                        <InputNumber style={{ width: 140 }} placeholder="最低价" min={0} />
                    </Form.Item>
                    <span className="tilde">~</span>
                    <Form.Item name="maxPrice">
                        <InputNumber style={{ width: 140 }} placeholder="最高价" min={0} />
                    </Form.Item>

                    <Space style={{ marginLeft: 'auto' }}>
                        <Button onClick={() => { form.resetFields(); fetchList(true) }}>重置</Button>
                        <Button type="primary" htmlType="submit" className="gradient-btn">查询</Button>
                    </Space>
                </Form>

                {/* 表格 */}
                <Table<AdminPlanItem>
                    rowKey="planCode"
                    loading={loading}
                    columns={columns}
                    dataSource={data}
                    className="admin-plans-table"
                    tableLayout="fixed"                 // ✅ 固定布局，配合列宽更稳
                    scroll={{ x: 'max-content' }}       // ✅ 允许横向滚动
                    sticky                               // 可选：粘性头和滚动条体验更好
                    pagination={{
                        current: pagination.current,
                        pageSize: pagination.size,
                        total,
                        showSizeChanger: true,
                        showTotal: t => `共 ${t} 条`,
                        onChange: (page, pageSize) => {
                            setPagination({ current: page, size: pageSize })
                            fetchList()
                        },
                    }}
                    onChange={onTableChange}
                />
            </Card>

            {/* 新增/编辑弹窗 */}
            <Modal
                title={editing ? '编辑套餐' : '新增套餐'}
                open={open}
                onCancel={() => { setOpen(false); setEditing(null); upForm.resetFields() }}
                onOk={onSubmitUpsert}
                okText="保存"
                cancelText="取消"
                okButtonProps={{ className: 'gradient-btn' as any }}
                destroyOnClose
            >
                <Form
                    form={upForm}
                    layout="vertical"
                    initialValues={{ status: 'onSale', billingTypes: [] }}   // ← 默认不选
                >
                    <Form.Item label="套餐编码" name="planCode" rules={[{ required: true, message: '请输入编码' }]}>
                        <Input placeholder="例如：PLAN-1001" disabled={!!editing} />
                    </Form.Item>

                    <Form.Item label="套餐名称" name="name" rules={[{ required: true, message: '请输入名称' }]}>
                        <Input placeholder="例如：千兆宽带 1000M" />
                    </Form.Item>

                    <Form.Item label="套餐有效期（月）" name="planPeriod">
                        <InputNumber min={1} precision={0} style={{ width: '100%' }} placeholder="月" />
                    </Form.Item>
                    <Form.Item label="折扣率" name="discount" tooltip="100表示无折扣">
                        <InputNumber min={0} max={100} precision={0} style={{ width: '100%' }} placeholder="请输入折扣" />
                    </Form.Item>
                    <Form.Item label="套餐数量" name="qty" >
                        <InputNumber min={0} precision={0} style={{ width: '100%' }} placeholder="请输入数量" />
                    </Form.Item>
                    <Form.Item label="状态" name="status">
                        <Select options={statusOptions} />
                    </Form.Item>
                    <Form.Item label="是否需要IP" name="isIp">
                        <Select options={isIpOptions} />
                    </Form.Item>
                    <Form.Item label="评分" name="rating" >
                        <InputNumber min={0} precision={1} max={5} style={{ width: '100%' }} placeholder="请输入评分" />
                    </Form.Item>
                    <Form.Item label="设备资源" name="deviceSN" >
                        <InputNumber style={{ width: '100%' }} placeholder="请输入设备" />
                    </Form.Item>
                    <Form.Item label="所需设备数量" name="deviceQty" >
                        <InputNumber min={0} precision={0} style={{ width: '100%' }} placeholder="请输入评分" />
                    </Form.Item>
                    <Form.Item label="带宽(Mbps)" name="bandwidth">
                        <InputNumber min={0} precision={0} style={{ width: '100%' }} />
                    </Form.Item>

                    <Form.Item
                        name="description"
                        label="套餐描述"
                        rules={[{ required: true, message: '请输入描述' }]}
                    >
                        <Input.TextArea showCount maxLength={100} />
                    </Form.Item>
                    <Form.Item
                        label="展示图片 URL"
                        name="imageBase64"
                        rules={[{ required: true, message: '请填写图片 URL' }]}
                    >
                        <Input placeholder="https://..." />
                    </Form.Item>
                    <Form.Item>
                        <Image src={upForm.getFieldValue('picture')} width={160} style={{ borderRadius: 8 }} />
                    </Form.Item>

                    {/* === 新增：计费方式选择 === */}
                    <Form.Item label="计费方式" name="billingTypes" tooltip="可多选">
                        <Checkbox.Group
                            options={[
                                { label: '月费', value: 'monthly' },
                                { label: '年费', value: 'yearly' },
                                { label: '永久', value: 'lifetime' },
                            ]}
                        />
                    </Form.Item>

                    {/* 月费金额（仅在选择时显示 & 必填） */}
                    {billingTypes.includes('monthly') && (
                        <Form.Item
                            label="月费金额（¥/月）"
                            name="monthlyPrice"
                            rules={[
                                ({ getFieldValue }) => ({
                                    validator(_, value) {
                                        const types = getFieldValue('billingTypes') || []
                                        if (!types.includes('monthly')) return Promise.resolve()
                                        if (typeof value === 'number') return Promise.resolve()
                                        return Promise.reject(new Error('请输入月费金额'))
                                    },
                                }),
                            ]}
                        >
                            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="例如：99.00" />
                        </Form.Item>
                    )}

                    {/* 年费金额（仅在选择时显示 & 必填） */}
                    {billingTypes.includes('yearly') && (
                        <Form.Item
                            label="年费金额（¥/年）"
                            name="yearlyPrice"
                            rules={[
                                ({ getFieldValue }) => ({
                                    validator(_, value) {
                                        const types = getFieldValue('billingTypes') || []
                                        if (!types.includes('yearly')) return Promise.resolve()
                                        if (typeof value === 'number') return Promise.resolve()
                                        return Promise.reject(new Error('请输入年费金额'))
                                    },
                                }),
                            ]}
                        >
                            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="例如：999.00" />
                        </Form.Item>
                    )}

                    {/* 永久买断金额（仅在选择时显示 & 必填） */}
                    {billingTypes.includes('lifetime') && (
                        <Form.Item
                            label="永久买断金额（¥）"
                            name="lifetimePrice"
                            rules={[
                                ({ getFieldValue }) => ({
                                    validator(_, value) {
                                        const types = getFieldValue('billingTypes') || []
                                        if (!types.includes('lifetime')) return Promise.resolve()
                                        if (typeof value === 'number') return Promise.resolve()
                                        return Promise.reject(new Error('请输入永久买断金额'))
                                    },
                                }),
                            ]}
                        >
                            <InputNumber min={0} precision={2} style={{ width: '100%' }} placeholder="例如：1999.00" />
                        </Form.Item>
                    )}
                </Form>
            </Modal>

        </div>
    )
}

export default Product




