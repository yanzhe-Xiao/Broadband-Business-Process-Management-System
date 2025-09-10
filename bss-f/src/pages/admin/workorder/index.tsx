import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Drawer, Empty, Form, Input, Select, Space, Steps, Tag, message } from 'antd'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import './workorder.css'
import { useAuthStore } from '../../../store/auth'
import {
    listEngineerOrders,
    type EngineerOrder, type FlowStep, type WorkStatus
} from '../../../api/flowPath'

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

// ===== 可切换 mock（没有后端时用）=====
const USE_MOCK = false
const mockData: EngineerOrder[] = [
    {
        id: 'wo_1001',
        orderNo: 'WO-20250909-001',
        customerName: '李雷',
        customerPhone: '13800000001',
        address: '上海市浦东新区 XX 路 100 号',
        planName: '千兆宽带',
        createdAt: new Date().toISOString(),
        priority: 'high',
        status: 'working',
        currentKey: 'install',
        steps: [
            { key: 'survey', title: '现场勘察', status: 'finish' },
            { key: 'cabling', title: '布线/熔纤', status: 'finish' },
            { key: 'install', title: '设备安装与上电', status: 'process' },
            { key: 'opticalTest', title: '光功率测试', status: 'wait' },
            { key: 'speedTest', title: '上网测速', status: 'wait' },
            { key: 'signoff', title: '用户签字确认', status: 'wait' },
        ]
    },
    {
        id: 'wo_1002',
        orderNo: 'WO-20250909-002',
        customerName: '韩梅梅',
        customerPhone: '13800000002',
        address: '北京市海淀区 XX 大街 88 号',
        planName: '500M 宽带',
        createdAt: new Date().toISOString(),
        priority: 'medium',
        status: 'accepted',
        currentKey: 'survey',
        steps: [
            { key: 'survey', title: '现场勘察', status: 'process' },
            { key: 'cabling', title: '布线/熔纤', status: 'wait' },
            { key: 'install', title: '设备安装与上电', status: 'wait' },
            { key: 'opticalTest', title: '光功率测试', status: 'wait' },
            { key: 'speedTest', title: '上网测速', status: 'wait' },
            { key: 'signoff', title: '用户签字确认', status: 'wait' },
        ]
    },
]

const EngineerDashboard: React.FC = () => {
    const username = useAuthStore(s => s.username)
    const [form] = Form.useForm()

    // 分页/数据
    const [, setLoading] = useState(false)
    const [records, setRecords] = useState<EngineerOrder[]>([])
    const [current, setCurrent] = useState(1)
    const [size, setSize] = useState(8)
    const [total, setTotal] = useState(0)

    const fetchList = async (page = current, pageSize = size) => {
        if (!username) return
        setLoading(true)
        try {
            if (USE_MOCK) {
                // 模拟分页
                const all = mockData.filter(i => {
                    const kw = (form.getFieldValue('keyword') || '').trim()
                    const st = form.getFieldValue('status') || 'all'
                    const okKw = !kw || (i.orderNo.includes(kw) || i.customerName.includes(kw) || i.address.includes(kw))
                    const okSt = st === 'all' || i.status === st
                    return okKw && okSt
                })
                const start = (page - 1) * pageSize
                const slice = all.slice(start, start + pageSize)
                setRecords(slice)
                setTotal(all.length)
                setCurrent(page)
                setSize(pageSize)
            } else {
                const st = form.getFieldValue('status') || 'all'
                const kw = form.getFieldValue('keyword') || ''
                const resp = await listEngineerOrders({ username, current: page, size: pageSize, keyword: kw, status: st })
                setRecords(resp.records)
                setTotal(resp.total)
                setCurrent(resp.current)
                setSize(resp.size)
            }
        } catch (e: any) {
            message.error(e?.response?.data?.message || e?.message || '获取工单失败')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { fetchList(1, size) }, []) // eslint-disable-line

    const pendingCount = useMemo(() => records.filter(i => i.status === 'pending').length, [records])

    // ==== Drawer 相关状态 ====
    const [flowOpen, setFlowOpen] = useState(false)
    const [flowOrder, setFlowOrder] = useState<EngineerOrder | null>(null)
    const [flowLoading, setFlowLoading] = useState(false)
    const [, setFiles] = useState<File[]>([])
    const [remark, setRemark] = useState('')

    // 当前步骤索引 & 当前步骤
    const currentStepIndex = useMemo(() => {
        if (!flowOrder) return 0
        const idx = flowOrder.steps.findIndex(s => s.key === flowOrder.currentKey)
        return idx >= 0 ? idx : 0
    }, [flowOrder])

    const step = useMemo(() => flowOrder?.steps[currentStepIndex] ?? null, [flowOrder, currentStepIndex])

    // 打开 Drawer
    const openFlow = (ord: EngineerOrder) => {
        setFlowOrder(ord)
        setRemark('')
        setFiles([])
        setFlowOpen(true)
    }

    // 本地更新工具：只在前端改状态（接后端时把这些挪到请求成功回调里）
    const setStepPatch = (idx: number, patch: Partial<FlowStep>) => {
        setFlowOrder(prev => {
            if (!prev) return prev
            const steps = prev.steps.slice()
            steps[idx] = { ...steps[idx], ...patch }
            return { ...prev, steps }
        })
    }


    // ====== 操作按钮回调（可在此接入后端）======

    // 保存（只保存备注/图片）
    const onSave = async () => {
        if (!flowOrder || !step) return
        try {
            setFlowLoading(true)
            // TODO: 接口示例
            // await api.saveStep({ orderId: flowOrder.id, stepKey: step.key, remark, files })
            setStepPatch(currentStepIndex, { remark })
            message.success('已保存（本地）')
            setFiles([])
        } catch (e: any) {
            message.error(e?.message || '保存失败')
        } finally {
            setFlowLoading(false)
        }
    }



    //折叠
    // key → 是否折叠；默认折叠 true
    const [doneCollapsed, setDoneCollapsed] = useState<Record<string, boolean>>({})
    const isDoneCollapsed = (key: string) => (doneCollapsed[key] ?? true)
    const toggleDone = (key: string) =>
        setDoneCollapsed(prev => ({ ...prev, [key]: !isDoneCollapsed(key) }))

    return (
        <Card className="eng-card" bodyStyle={{ padding: 16 }}>
            <div className="eng-head">
                <div className="left">
                    <div className="badge">WO</div>
                    <div className="title-wrap">
                        <h3>我的工单</h3>
                        <p>当前待接单 <b>{pendingCount}</b> 单</p>
                    </div>
                </div>
                <Space>
                    <Button icon={<ReloadOutlined />} onClick={() => fetchList()} />
                </Space>
            </div>

            {/* 筛选 */}
            <Form
                form={form}
                layout="inline"
                className="eng-filters"
                initialValues={{ status: 'all' }}
                onFinish={() => fetchList(1, size)}
            >
                <Form.Item name="keyword">
                    <Input allowClear prefix={<SearchOutlined />} placeholder="工单号/客户/地址" style={{ width: 260 }} />
                </Form.Item>
                <Form.Item name="status">
                    <Select style={{ width: 160 }}
                        options={[
                            { label: '全部状态', value: 'all' },
                            { label: '待接单', value: 'pending' },
                            { label: '已接单', value: 'accepted' },
                            { label: '出发中', value: 'onroute' },
                            { label: '已到场', value: 'arrived' },
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
            </Form>

            {/* 工单卡片 + 流程预览 */}
            {records.length === 0 ? (
                <Empty style={{ padding: '40px 0' }} />
            ) : (
                <div className="eng-grid">
                    {records.map(item => {
                        const currentIndex = Math.max(0, item.steps.findIndex(s => s.key === item.currentKey))
                        return (
                            <Card key={item.id} className="eng-card-item" bodyStyle={{ padding: 16 }}>
                                <div className="row1">
                                    <div className="left">
                                        <div className="no">{item.orderNo}</div>
                                        <div className="meta">
                                            <span>{item.customerName}（{item.customerPhone}）</span>
                                            <span className="light"> · {item.address}</span>
                                        </div>
                                    </div>
                                    <div className="right">
                                        <Tag color={priorityColor[item.priority]}>{item.priority.toUpperCase()}</Tag>
                                        <Tag color={statusColor[item.status]}>{statusText[item.status]}</Tag>
                                        <span className="ts">{dayjs(item.createdAt).format('MM-DD HH:mm')}</span>
                                    </div>
                                </div>

                                <Steps
                                    className="eng-steps"
                                    size="small"
                                    current={currentIndex < 0 ? 0 : currentIndex}
                                    items={item.steps.map((s: FlowStep) => ({
                                        title: s.title,
                                        status: s.status === 'error' ? 'error' : (s.status === 'finish' ? 'finish' : (s.status === 'process' ? 'process' : 'wait')),
                                    }))}
                                />

                                <div className="row2">
                                    <div className="plan">
                                        套餐：<b>{item.planName || '-'}</b>
                                    </div>
                                    <Space>
                                        {item.status !== 'done' && item.status !== 'canceled' && (
                                            <Button
                                                size="small"
                                                onClick={() => {
                                                    // 如果需要，先把工单状态推进到 working
                                                    // await ensureWorking(item.id)
                                                    setFlowOrder(item)
                                                    setFlowOpen(true)
                                                    openFlow(item)
                                                    console.log(item);

                                                }}
                                            >
                                                查看详情
                                            </Button>
                                        )}
                                        {/* <Button
                                            size="small"
                                            onClick={() => {
                                                setFlowOrder(item)   // 当前工单
                                                setFlowOpen(true)    // 打开抽屉
                                                openFlow(item)
                                            }}
                                        >
                                            查看详情
                                        </Button> */}
                                    </Space>
                                </div>
                            </Card>
                        )
                    })}
                </div>
            )}

            <Drawer
                title={null}
                placement="right"
                width={880}
                onClose={() => { setFlowOpen(false); setFlowOrder(null) }}
                open={flowOpen}
                destroyOnClose
                styles={{ body: { padding: 0, background: 'var(--canvas)' } }}
            >
                {flowOrder ? (
                    <div className="flowx-root">
                        {/* 头部 */}
                        <div className="flowx-head">
                            <div className="flowx-head-left">
                                <div className="flowx-chip">FLOW</div>
                                <div className="flowx-titles">
                                    <div className="flowx-title">施工流程 · 工单 {flowOrder.orderNo}</div>
                                    <div className="flowx-sub">在步骤卡片内填写备注、上传凭证并推进流程</div>
                                </div>
                            </div>
                            <Button ghost className="flowx-ghost" onClick={onSave} loading={flowLoading}>
                                保存全部
                            </Button>
                        </div>

                        {/* 竖直流程条 */}
                        <div className="flowx-body">
                            <Steps
                                direction="vertical"
                                current={currentStepIndex}
                                className="flowx-steps"
                                items={flowOrder.steps.map((s, idx) => {
                                    const isCurrent = idx === currentStepIndex
                                    const isFinished = s.status === 'finish'
                                    const isFuture = idx > currentStepIndex
                                    const antStatus =
                                        s.status === 'error' ? 'error' :
                                            isFinished ? 'finish' :
                                                isCurrent ? 'process' : 'wait'

                                    return {
                                        title: <span className="flowx-step-title">{s.title}</span>,
                                        status: antStatus as 'wait' | 'process' | 'finish' | 'error',
                                        description: (
                                            // <div className={`flowx-card ${isCurrent ? 'is-current' : isFinished ? 'is-done' : 'is-todo'}`}>
                                            <div className={`flowx-card ${isFinished ? 'is-done' : isCurrent ? 'is-current' : 'is-todo'}`}>
                                                {/* 当前步骤：可编辑 */}
                                                {isCurrent && !isFinished && (
                                                    <>
                                                        <div className="flowx-field" style={{
                                                            borderRadius: '12px',
                                                            border: '1px solid #e47076ff',
                                                            padding: '10px 12px'
                                                        }}>
                                                            <div className="flowx-todo">
                                                                <span className="flowx-tag" style={{ color: '#c80a0aff', border: '1px solid #f19999ff' }}>进行中</span>
                                                                <span className="flowx-hint" style={{ color: '#c80a0aff', fontWeight: '600' }}>正在进行当前步骤</span>
                                                            </div>
                                                        </div>
                                                    </>
                                                )
                                                }

                                                {/* 已完成：只读 */}
                                                {/* 已完成：可收缩 */}
                                                {/* !isCurrent && */}
                                                {
                                                    isFinished && (
                                                        <div className="flowx-done">
                                                            <div className="flowx-done-bar" onClick={() => toggleDone(s.key)}>
                                                                <div className="flowx-badge-done">已完成</div>
                                                                <div className="flowx-done-title">
                                                                    {s.title}{s.remark ? ' · 有备注' : ' · 无备注'}{(s.proofs?.length || 0) > 0 ? ' · 有凭证' : ' · 无凭证'}
                                                                </div>
                                                                <button
                                                                    type="button"
                                                                    className={`flowx-toggle ${isDoneCollapsed(s.key) ? '' : 'open'}`}
                                                                    aria-label="切换展开"
                                                                />
                                                            </div>

                                                            {!isDoneCollapsed(s.key) && (
                                                                <div className="flowx-done-content">
                                                                    {s.remark && (
                                                                        <div className="flowx-read">
                                                                            <div className="flowx-read-label">备注:</div>
                                                                            <div className="flowx-read-text">{s.remark}</div>
                                                                        </div>
                                                                    )}
                                                                    {(s.proofs?.length || 0) > 0 && (
                                                                        <div className="flowx-read">
                                                                            <div className="flowx-read-label">凭证:</div>
                                                                            <div className="flowx-proof-grid">
                                                                                {s.proofs!.map((url, i) => (
                                                                                    <img key={i} src={url} alt="" className="flowx-proof" />
                                                                                ))}
                                                                            </div>
                                                                        </div>
                                                                    )}
                                                                </div>
                                                            )}
                                                        </div>
                                                    )
                                                }


                                                {/* 待开始：提示 */}
                                                {
                                                    !isCurrent && !isFinished && isFuture && (
                                                        <div className="flowx-todo">
                                                            <span className="flowx-tag">待开始</span>
                                                            <span className="flowx-hint">完成当前步骤后可继续</span>
                                                        </div>
                                                    )
                                                }
                                            </div>
                                        ),
                                    }
                                })}
                            />
                        </div>

                    </div>
                ) : null}
            </Drawer>





            {/* 分页（简单版，列表多时可以换成 Pagination 组件） */}
            <div className="eng-pager">
                <Space>
                    <Button disabled={current <= 1} onClick={() => fetchList(current - 1, size)}>上一页</Button>
                    <span>第 {current} 页</span>
                    <Button disabled={(current * size) >= total} onClick={() => fetchList(current + 1, size)}>下一页</Button>
                    <span>共 {total} 单</span>
                </Space>
            </div>
        </Card >
    )
}

export default EngineerDashboard
