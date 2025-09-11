import React, { useEffect, useMemo, useState } from 'react'
import {
    Badge,
    Button,
    Card,
    Descriptions,
    Drawer,
    Empty,
    Form,
    Input,
    Modal,
    Popover,
    Rate,
    Select,
    Space,
    Table,
    Tag,
    message
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { ReloadOutlined, SearchOutlined, FileTextOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import './order.css'
import { useAuthStore } from '../../../store/auth'
import { getMyOrders, type OrderRecord, type OrderItem, submitOrderReview } from '../../../api/order'


// ====== 状态映射 & 颜色 ======
const statusTextMap: Record<string, string> = {
    pending: '待支付',
    paid: '已支付',
    dispatch: '已派单',
    working: '施工中',
    done: '已完工',
    canceled: '已取消',
}
const statusColorMap: Record<string, string> = {
    pending: 'warning',
    paid: 'processing',
    dispatch: 'geekblue',
    working: 'purple',
    done: 'success',
    canceled: 'error',
}
const planTypeColor: Record<string, string> = {
    month: 'processing',
    year: 'success',
    forever: 'purple',
}

// ====== 组件 ======
const MyOrders: React.FC = () => {
    const username = useAuthStore(s => s.username)
    const [form] = Form.useForm()

    const [loading, setLoading] = useState(false)
    const [data, setData] = useState<OrderRecord[]>([])
    const [current, setCurrent] = useState(1)
    const [size, setSize] = useState(10)
    const [total, setTotal] = useState(0)

    // 详情抽屉
    const [open, setOpen] = useState(false)
    const [detail, setDetail] = useState<OrderRecord | null>(null)

    const fetchList = async (page = current, pageSize = size) => {
        if (!username) return
        setLoading(true)
        try {
            const keyword = form.getFieldValue('keyword') || ''
            const status = form.getFieldValue('status') || 'all'
            const resp = await getMyOrders({
                current: page,
                size: pageSize,
                username,
                keyword,
                status: status === 'all' ? undefined : status,
            })
            setData(resp.records || [])
            setTotal(resp.total || 0)
            setCurrent(resp.current || page)
            setSize(resp.size || pageSize)
        } catch (e: any) {
            message.error(e?.response?.data?.message || e?.message || '获取订单失败')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { fetchList(1, size) }, []) // eslint-disable-line

    const pendingCount = useMemo(() => data.filter(d => d.status === 'pending').length, [data])
    // 评价弹窗
    const [reviewOpen, setReviewOpen] = useState(false)
    const [reviewLoading, setReviewLoading] = useState(false)
    const [reviewTarget, setReviewTarget] = useState<OrderRecord | null>(null)
    const [reviewForm] = Form.useForm<{ rating: number; content?: string }>()

    // 子表（明细）列
    const itemCols: ColumnsType<OrderItem> = [
        { title: '套餐编码', dataIndex: 'planCode', key: 'planCode', width: 140, ellipsis: true },
        { title: '套餐名称', dataIndex: 'planName', key: 'planName', width: 220, ellipsis: true },
        {
            title: '类型',
            dataIndex: 'planType',
            key: 'planType',
            width: 100,
            render: (t: string) => <Tag color={planTypeColor[t] || 'default'}>
                {t === 'month' ? '月费' : t === 'year' ? '年费' : t === 'forever' ? '永久' : t}
            </Tag>
        },
        { title: '数量', dataIndex: 'qty', key: 'qty', width: 80 },
        {
            title: '单价',
            dataIndex: 'unitPrice',
            key: 'unitPrice',
            width: 120,
            render: (v) => <>¥ {Number(v).toFixed(2)}</>
        },
        { title: '折扣', dataIndex: 'discount', key: 'discount', width: 90, render: (v) => `${v ?? 0}%` },
        {
            title: '小计',
            dataIndex: 'itemPrice',
            key: 'itemPrice',
            width: 140,
            render: (v) => <>¥ {Number(v).toFixed(2)}</>
        },
        {
            title: '到期/结束',
            dataIndex: 'endTime',
            key: 'endTime',
            width: 180,
            render: (v) => v ? dayjs(v).format('YYYY-MM-DD HH:mm') : '-'
        },
        {
            title: '说明',
            dataIndex: 'description',
            key: 'description',
            ellipsis: true,
            render: (t) => t || '-'
        },
    ]

    // 主表列
    const columns: ColumnsType<OrderRecord> = [
        {
            title: '订单号',
            dataIndex: 'id',
            key: 'id',
            width: 160,
            render: (v) => <Space>
                <Badge color="#6366f1" />
                <span>{String(v)}</span>
            </Space>
        },
        {
            title: '创建时间',
            dataIndex: 'createdAt',
            key: 'createdAt',
            width: 180,
            render: (v) => dayjs(v).format('YYYY-MM-DD HH:mm')
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 120,
            render: (s: string) => {
                const label = statusTextMap[s] || s || '-'
                const color = statusColorMap[s] || 'default'
                return <Tag color={color}>{label}</Tag>
            }
        },
        {
            title: '安装地址',
            dataIndex: 'installAddress',
            key: 'installAddress',
            ellipsis: true,
            render: (t) => t || '-'
        },
        {
            title: '金额',
            dataIndex: 'price',
            key: 'price',
            width: 140,
            render: (v) => <b style={{ color: '#111827' }}>¥ {Number(v).toFixed(2)}</b>
        },
        {
            title: '工程师',
            key: 'eng',
            width: 200,
            render: (_, r) => {
                const content = (
                    <div className="eng-pop">
                        <div><b>姓名：</b>{r.engineerFullName || '-'}</div>
                        <div><b>电话：</b>{r.engineerPhone || '-'}</div>
                        <div><b>邮箱：</b>{r.engineerEmail || '-'}</div>
                    </div>
                )
                return (
                    <Popover content={content} placement="topLeft">
                        <Space>
                            <FileTextOutlined />
                            <span>{r.engineerFullName || '-'}</span>
                        </Space>
                    </Popover>
                )
            }
        },
        {
            title: '操作',
            key: 'action',
            fixed: 'right',
            width: 120,
            render: (_, r) => (
                <Space>
                    <Button
                        size="small"
                        type="primary"
                        className="gradient-btn"
                        onClick={() => { setDetail(r); setOpen(true) }}
                    >
                        订单详情
                    </Button>

                    {/* 待评价显示“评价” */}
                    {r.status === '待评价' && (
                        <Button
                            size="small"
                            onClick={() => {
                                setReviewTarget(r)
                                reviewForm.setFieldsValue({ rating: 5, content: '' })
                                setReviewOpen(true)
                            }}
                        >
                            评价
                        </Button>
                    )}
                </Space>
            )
        }
    ]

    return (
        <Card className="orders-card" bodyStyle={{ padding: 16 }}>
            {/* 顶部 */}
            <div className="orders-head">
                <div className="left">
                    <div className="badge">ORD</div>
                    <div className="title-wrap">
                        <h3>我的订单</h3>
                        <p>待支付 <b>{pendingCount}</b> 单 · 共 <b>{total}</b> 单</p>
                    </div>
                </div>
                <Space>
                    <Button icon={<ReloadOutlined />} onClick={() => fetchList()} />
                </Space>
            </div>

            {/* 过滤 */}
            {/* <Form
                form={form}
                layout="inline"
                className="orders-filters"
                initialValues={{ status: 'all' }}
                onFinish={() => fetchList(1, size)}
            >
                <Form.Item name="keyword">
                    <Input allowClear prefix={<SearchOutlined />} placeholder="搜索订单号/地址" style={{ width: 260 }} />
                </Form.Item>
                <Form.Item name="status">
                    <Select
                        style={{ width: 180 }}
                        options={[
                            { label: '全部状态', value: 'all' },
                            { label: '待支付', value: 'pending' },
                            { label: '已支付', value: 'paid' },
                            { label: '已派单', value: 'dispatch' },
                            { label: '施工中', value: 'working' },
                            { label: '已完工', value: 'done' },
                            { label: '已取消', value: 'canceled' },
                        ]}
                    />
                </Form.Item>
                <Form.Item>
                    <Button type="primary" htmlType="submit" className="gradient-btn">查询</Button>
                </Form.Item>
                <Form.Item>
                    <Button onClick={() => { form.resetFields(); fetchList(1, size) }}>重置</Button>
                </Form.Item>
            </Form> */}

            {/* 表格 */}
            {data.length === 0 ? (
                <Empty style={{ padding: '40px 0' }} />
            ) : (
                <Table<OrderRecord>
                    rowKey="id"
                    loading={loading}
                    columns={columns}
                    dataSource={data}
                    className="orders-table"
                    tableLayout="fixed"
                    scroll={{ x: 'max-content' }}
                    expandable={{
                        expandedRowRender: (record) => (
                            <Table<OrderItem>
                                rowKey={(r) => `${r.planCode}_${r.planType}`}
                                columns={itemCols}
                                dataSource={record.items}
                                pagination={false}
                                size="small"
                            />
                        ),
                    }}
                    pagination={{
                        current,
                        pageSize: size,
                        total,
                        showSizeChanger: true,
                        showTotal: t => `共 ${t} 单`,
                        onChange: (p, ps) => { setCurrent(p); setSize(ps); fetchList(p, ps) },
                    }}
                />
            )}

            {/* 详情抽屉 */}
            <Drawer
                title={detail ? `订单详情 · #${detail.id}` : '订单详情'}
                open={open}
                width={720}
                onClose={() => { setOpen(false); setDetail(null) }}
                destroyOnClose
            >
                {detail && (
                    <>
                        <Descriptions bordered size="small" column={2} className="orders-desc">
                            <Descriptions.Item label="订单号" span={2}>{detail.id}</Descriptions.Item>
                            <Descriptions.Item label="创建时间">{dayjs(detail.createdAt).format('YYYY-MM-DD HH:mm')}</Descriptions.Item>
                            <Descriptions.Item label="状态">
                                <Tag color={statusColorMap[detail.status] || 'default'}>{statusTextMap[detail.status] || detail.status}</Tag>
                            </Descriptions.Item>
                            <Descriptions.Item label="安装地址" span={2}>{detail.installAddress || '-'}</Descriptions.Item>
                            <Descriptions.Item label="金额" span={2}><b>¥ {Number(detail.price).toFixed(2)}</b></Descriptions.Item>
                        </Descriptions>

                        <div className="orders-subtitle">套餐明细</div>
                        <Table<OrderItem>
                            rowKey={(r) => `${r.planCode}_${r.planType}`}
                            columns={itemCols}
                            dataSource={detail.items}
                            size="small"
                            pagination={false}
                        />

                        <div className="orders-subtitle">工程师信息</div>
                        <Descriptions bordered size="small" column={2}>
                            <Descriptions.Item label="姓名">{detail.engineerFullName || '-'}</Descriptions.Item>
                            <Descriptions.Item label="电话">{detail.engineerPhone || '-'}</Descriptions.Item>
                            <Descriptions.Item label="邮箱" span={2}>{detail.engineerEmail || '-'}</Descriptions.Item>
                        </Descriptions>
                    </>
                )}
            </Drawer>
            <Modal
                title={null}
                open={reviewOpen}
                onCancel={() => { setReviewOpen(false); setReviewTarget(null) }}
                footer={null}
                width={520}
                destroyOnClose
                className="review-modal"
            >
                <div className="review-wrap">
                    <div className="review-head">
                        <div className="review-badge">REV</div>
                        <div className="review-titles">
                            <div className="review-title">订单评价</div>
                            <div className="review-sub">订单号：{reviewTarget?.id} · 总金额 ¥ {Number(reviewTarget?.price || 0).toFixed(2)}</div>
                        </div>
                    </div>

                    <Form
                        layout="vertical"
                        form={reviewForm}
                        requiredMark={false}
                        initialValues={{ rating: 5, content: '' }}
                    >
                        <Form.Item
                            label="评分"
                            name="rating"
                            rules={[{ required: true, message: '请打分' }]}
                        >
                            <Rate allowClear={false} />
                        </Form.Item>
                        <Form.Item
                            label="评价内容"
                            name="content"
                            rules={[{ max: 200, message: '最多 200 字' }]}
                        >
                            <Input.TextArea
                                rows={4}
                                placeholder="写点真实体验，给其他用户参考～（可选）"
                                showCount
                                maxLength={200}
                            />
                        </Form.Item>

                        <div style={{ display: 'flex', justifyContent: 'flex-end', gap: 12 }}>
                            <Button onClick={() => { setReviewOpen(false); setReviewTarget(null) }}>
                                取消
                            </Button>
                            <Button
                                type="primary"
                                className="gradient-btn"
                                loading={reviewLoading}
                                onClick={async () => {
                                    try {
                                        const { rating, content } = await reviewForm.validateFields()
                                        if (!reviewTarget) return
                                        setReviewLoading(true)


                                        await submitOrderReview({
                                            orderId: Number(reviewTarget.id),
                                            score: rating,
                                            comment: (content || '').trim(),
                                        })
                                        message.success('评价已提交，感谢反馈！')
                                        setReviewOpen(false)
                                        setReviewTarget(null)
                                        // 提交成功后刷新列表，让该单从“待评价”变为“已完成/已评价”
                                        fetchList(current, size)
                                    } catch (e: any) {
                                        if (!e?.errorFields) {
                                            message.error(e?.response?.data?.message || e?.message || '提交失败')
                                        }
                                    } finally {
                                        setReviewLoading(false)
                                    }
                                }}
                            >
                                提交评价
                            </Button>
                        </div>
                    </Form>
                </div>
            </Modal>
        </Card>
    )
}

export default MyOrders
