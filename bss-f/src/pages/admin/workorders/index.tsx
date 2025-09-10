import React, { useEffect, useMemo, useState } from 'react'
import {
    Badge, Button, Card, DatePicker, Drawer, Form, Image, Input, message,
    Popconfirm, Select, Space, Table, Tag, Timeline, Upload
} from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { InboxOutlined, ReloadOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import './workorders.css'
import {
    listWorkOrders, updateWorkStatus, addWorkRemark, uploadProof,
    type WorkOrderItem, type WorkStatus
} from '../../../api/workOrder'

const { RangePicker } = DatePicker

const statusText: Record<WorkStatus, string> = {
    pending: '待接单',
    accepted: '已接单',
    onroute: '出发中',
    arrived: '已到场',
    working: '施工中',
    done: '已完工',
    canceled: '已取消'
}
const statusColor: Record<WorkStatus, string> = {
    pending: 'default',
    accepted: 'processing',
    onroute: 'gold',
    arrived: 'blue',
    working: 'purple',
    done: 'success',
    canceled: 'error'
}
const priorityColor = { low: 'default', medium: 'warning', high: 'error' } as const

const WorkOrders: React.FC = () => {
    // ======= 查询 & 分页 =======
    const [form] = Form.useForm()
    const [loading, setLoading] = useState(false)
    const [data, setData] = useState<WorkOrderItem[]>([])
    const [current, setCurrent] = useState(1)
    const [size, setSize] = useState(10)
    const [total, setTotal] = useState(0)

    const fetchList = async (page = current, pageSize = size) => {
        const { keyword, status, dateRange } = form.getFieldsValue()
        const [dateFrom, dateTo] =
            (dateRange && dateRange.length === 2)
                ? [dayjs(dateRange[0]).startOf('day').toISOString(), dayjs(dateRange[1]).endOf('day').toISOString()]
                : [undefined, undefined]
        setLoading(true)
        try {
            const resp = await listWorkOrders({
                current: page, size: pageSize, keyword,
                status: status ?? 'all',
                dateFrom, dateTo
            })
            setData(resp.records)
            setTotal(resp.total)
            setCurrent(resp.current)
            setSize(resp.size)
        } catch (e: any) {
            message.error(e?.response?.data?.message || e?.message || '获取工单失败')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { fetchList(1, size) }, []) // eslint-disable-line

    // ======= 详情抽屉 =======
    const [open, setOpen] = useState(false)
    const [row, setRow] = useState<WorkOrderItem | null>(null)

    const openDetail = (r: WorkOrderItem) => { setRow(r); setOpen(true) }
    const closeDetail = () => { setOpen(false); setRow(null) }

    // ======= 完工上传 =======
    const [proofs, setProofs] = useState<string[]>([])
    const [finishing, setFinishing] = useState(false)
    const doFinish = async (id: string) => {
        if (proofs.length === 0) {
            return message.warning('请先上传完工凭证')
        }
        try {
            setFinishing(true)
            await updateWorkStatus({ id, status: 'done', remark: `完工凭证: ${proofs.join(',')}` })
            message.success('已完工')
            setProofs([])
            fetchList()
            closeDetail()
        } catch (e: any) {
            message.error(e?.response?.data?.message || e?.message || '完工失败')
        } finally {
            setFinishing(false)
        }
    }

    // ======= 列定义 =======
    const columns: ColumnsType<WorkOrderItem> = [
        {
            title: '工单号 / 客户',
            dataIndex: 'orderNo',
            width: 260,
            render: (_: any, r) => (
                <div className="wo-id">
                    <div className="no">{r.orderNo}</div>
                    <div className="cus">
                        <span>{r.customerName}</span>
                        <span className="light">（{r.customerPhone}）</span>
                    </div>
                </div>
            )
        },
        { title: '地址', dataIndex: 'address', width: 260, ellipsis: true },
        {
            title: '套餐/优先级',
            width: 160,
            render: (_: any, r) => (
                <Space direction="vertical" size={2}>
                    <span>{r.planName || '-'}</span>
                    <Tag color={priorityColor[r.priority]}>{r.priority.toUpperCase()}</Tag>
                </Space>
            )
        },
        {
            title: '创建时间 / 截止',
            width: 200,
            render: (_: any, r) => (
                <Space direction="vertical" size={2}>
                    <span>{dayjs(r.createdAt).format('YYYY-MM-DD HH:mm')}</span>
                    <span className="sla">SLA：{r.deadline ? dayjs(r.deadline).format('MM-DD HH:mm') : '-'}</span>
                </Space>
            )
        },
        {
            title: '状态',
            dataIndex: 'status',
            width: 120,
            render: (s: WorkStatus) => <Tag color={statusColor[s]}>{statusText[s]}</Tag>,
            filters: Object.entries(statusText).map(([value, text]) => ({ text, value })),
            onFilter: (v, r) => r.status === v
        },
        {
            title: '操作',
            key: 'action',
            fixed: 'right',
            width: 320,
            render: (_: any, r) => {
                const s = r.status
                return (
                    <Space wrap>
                        <Button onClick={() => openDetail(r)}>查看</Button>

                        {s === 'pending' && (
                            <Button type="primary" ghost onClick={async () => {
                                await updateWorkStatus({ id: r.id, status: 'accepted' })
                                message.success('已接单')
                                fetchList()
                            }}>接单</Button>
                        )}

                        {s === 'accepted' && (
                            <Button type="primary" ghost onClick={async () => {
                                await updateWorkStatus({ id: r.id, status: 'onroute' })
                                message.success('已出发')
                                fetchList()
                            }}>出发</Button>
                        )}

                        {s === 'onroute' && (
                            <Button type="primary" ghost onClick={async () => {
                                await updateWorkStatus({ id: r.id, status: 'arrived' })
                                message.success('已到场')
                                fetchList()
                            }}>到场</Button>
                        )}

                        {s === 'arrived' && (
                            <Button type="primary" ghost onClick={async () => {
                                await updateWorkStatus({ id: r.id, status: 'working' })
                                message.success('已开始施工')
                                fetchList()
                            }}>开始施工</Button>
                        )}

                        {s === 'working' && (
                            <Button className="gradient-btn" onClick={() => openDetail(r)}>提交完工</Button>
                        )}

                        {s !== 'done' && s !== 'canceled' && (
                            <Popconfirm title="确认取消该工单？" onConfirm={async () => {
                                await updateWorkStatus({ id: r.id, status: 'canceled' })
                                message.success('已取消')
                                fetchList()
                            }}>
                                <Button danger>取消</Button>
                            </Popconfirm>
                        )}
                    </Space>
                )
            }
        },
    ]

    // 顶部统计（待接单数）
    const pendingCount = useMemo(() => data.filter(i => i.status === 'pending').length, [data])

    return (
        <Card className="wo-card" bodyStyle={{ padding: 16 }}>
            <div className="wo-header">
                <div className="left">
                    <div className="badge"><Badge count={pendingCount} size="small" /> </div>
                    <div className="title-wrap">
                        <h3>工单管理</h3>
                        <p>当前待接单 <b>{pendingCount}</b> 单</p>
                    </div>
                </div>
                <Space>
                    <Button icon={<ReloadOutlined />} onClick={() => fetchList()} />
                </Space>
            </div>

            {/* 筛选器 */}
            <Form
                form={form}
                layout="inline"
                className="wo-filters"
                initialValues={{ status: 'all' }}
                onFinish={() => fetchList(1, size)}
            >
                <Form.Item name="keyword">
                    <Input allowClear placeholder="工单号/客户/地址 关键词" style={{ width: 260 }} />
                </Form.Item>
                <Form.Item name="status">
                    <Select style={{ width: 160 }}
                        options={[
                            { label: '全部状态', value: 'all' },
                            ...Object.entries(statusText).map(([value, label]) => ({ label, value }))
                        ]}
                    />
                </Form.Item>
                <Form.Item name="dateRange">
                    <RangePicker />
                </Form.Item>
                <Form.Item>
                    <Button type="primary" htmlType="submit" className="gradient-btn">查询</Button>
                </Form.Item>
                <Form.Item>
                    <Button onClick={() => { form.resetFields(); fetchList(1, size) }}>重置</Button>
                </Form.Item>
            </Form>

            {/* 表格 */}
            <Table<WorkOrderItem>
                rowKey="id"
                loading={loading}
                columns={columns}
                dataSource={data}
                tableLayout="fixed"
                scroll={{ x: 1100 }}
                pagination={{
                    current, pageSize: size, total,
                    showSizeChanger: true,
                    showTotal: t => `共 ${t} 单`,
                    onChange: (p, ps) => { setCurrent(p); setSize(ps); fetchList(p, ps) }
                }}
            />

            {/* 详情抽屉 */}
            <Drawer
                width={720}
                title={row ? `工单详情：${row.orderNo}` : '工单详情'}
                open={open}
                onClose={closeDetail}
                destroyOnClose
            >
                {row && (
                    <div className="wo-detail">
                        <div className="kv">
                            <div><b>客户：</b>{row.customerName}（{row.customerPhone}）</div>
                            <div><b>地址：</b>{row.address}</div>
                            <div><b>套餐：</b>{row.planName || '-'}</div>
                            <div><b>创建：</b>{dayjs(row.createdAt).format('YYYY-MM-DD HH:mm')}</div>
                            <div><b>SLA：</b>{row.deadline ? dayjs(row.deadline).format('YYYY-MM-DD HH:mm') : '-'}</div>
                            <div><b>状态：</b><Tag color={statusColor[row.status]}>{statusText[row.status]}</Tag></div>
                            <div><b>优先级：</b><Tag color={priorityColor[row.priority]}>{row.priority.toUpperCase()}</Tag></div>
                        </div>

                        <div className="block">
                            <h4>施工备注</h4>
                            <RemarkBox id={row.id} onAdded={() => fetchList()} />
                        </div>

                        {row.status === 'working' && (
                            <div className="block">
                                <h4>完工凭证</h4>
                                <Upload.Dragger
                                    multiple
                                    maxCount={6}
                                    accept="image/*"
                                    itemRender={() => null}
                                    customRequest={async ({ file, onSuccess, onError }) => {
                                        try {
                                            const fd = new FormData()
                                            fd.append('file', file as File)
                                            const { url } = await uploadProof(fd)
                                            setProofs(prev => [...prev, url])
                                            onSuccess && onSuccess({}, new XMLHttpRequest())
                                        } catch (e) {
                                            onError && onError(e as any)
                                        }
                                    }}
                                >
                                    <p className="ant-upload-drag-icon"><InboxOutlined /></p>
                                    <p className="ant-upload-text">点击或拖拽图片到此上传</p>
                                </Upload.Dragger>
                                <div className="proofs">
                                    {proofs.map((u, i) => <Image key={i} src={u} width={120} style={{ borderRadius: 8 }} />)}
                                </div>
                                <Button
                                    type="primary"
                                    className="gradient-btn"
                                    loading={finishing}
                                    onClick={() => doFinish(row.id)}
                                    style={{ marginTop: 12 }}
                                >
                                    提交完工
                                </Button>
                            </div>
                        )}

                        <div className="block">
                            <h4>进度流</h4>
                            <Timeline
                                items={[
                                    { color: 'gray', children: `创建：${dayjs(row.createdAt).format('YYYY-MM-DD HH:mm')}` },
                                    ...(row.status !== 'pending' ? [{ color: 'blue', children: '工程师已接单/出发/到场/施工中…' }] : []),
                                    ...(row.status === 'done' ? [{ color: 'green', children: '已完工' }] : []),
                                    ...(row.status === 'canceled' ? [{ color: 'red', children: '已取消' }] : []),
                                ]}
                            />
                        </div>
                    </div>
                )}
            </Drawer>
        </Card>
    )
}

export default WorkOrders

// 备注输入组件（内联）
const RemarkBox: React.FC<{ id: string; onAdded: () => void }> = ({ id, onAdded }) => {
    const [val, setVal] = useState('')
    const [saving, setSaving] = useState(false)
    return (
        <Space.Compact style={{ width: '100%' }}>
            <Input.TextArea
                value={val}
                onChange={e => setVal(e.target.value)}
                placeholder="记录现场情况、故障点、材料用量等"
                autoSize={{ minRows: 2, maxRows: 4 }}
            />
            <Button
                type="primary"
                className="gradient-btn"
                loading={saving}
                onClick={async () => {
                    if (!val.trim()) return message.info('请填写备注')
                    try {
                        setSaving(true)
                        await addWorkRemark({ id, remark: val.trim() })
                        setVal('')
                        onAdded()
                        message.success('已添加备注')
                    } catch (e: any) {
                        message.error(e?.response?.data?.message || e?.message || '添加备注失败')
                    } finally {
                        setSaving(false)
                    }
                }}
            >
                保存
            </Button>
        </Space.Compact>
    )
}
