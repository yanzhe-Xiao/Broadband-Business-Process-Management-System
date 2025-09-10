import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Drawer, Empty, Form, Input, Select, Space, Steps, Tag, Upload, message } from 'antd'
import { ReloadOutlined, SearchOutlined } from '@ant-design/icons'
import dayjs from 'dayjs'
import './workorder.css'
import { useAuthStore } from '../../../store/auth'
import {
    fileToDataUrl,
    listEngineerOrders,
    submitStep,
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
    const [files, setFiles] = useState<File[]>([])
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
                                                继续施工
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
                                                        <div className="flowx-field">
                                                            <div className="flowx-label">施工备注</div>
                                                            <Input.TextArea
                                                                rows={3}
                                                                value={remark}
                                                                onChange={e => setRemark(e.target.value)}
                                                                placeholder={s.remark ? `上次保存：${s.remark}` : '记录关键点、材料/故障位置/处理方式等…'}
                                                                className="flowx-input"
                                                            />
                                                        </div>

                                                        <div className="flowx-field">
                                                            <div className="flowx-label">上传凭证</div>
                                                            {/* 更美观的图片上传：使用 antd Upload（本地存前端，不直传后端） */}
                                                            <Upload
                                                                listType="picture-card"
                                                                fileList={[
                                                                    // 历史图片（只读展示）
                                                                    ...(s.proofs || []).map((url, i) => ({
                                                                        uid: `old_${i}`,
                                                                        name: `proof_${i}.jpg`,
                                                                        status: 'done' as const,
                                                                        url
                                                                    })),
                                                                    // 新选择的本地文件（预览）
                                                                    ...files.map((f, i) => ({
                                                                        uid: `new_${i}`,
                                                                        name: f.name || `local_${i}.jpg`,
                                                                        status: 'done' as const,
                                                                        url: URL.createObjectURL(f),
                                                                    })),
                                                                ]}
                                                                showUploadList={{ showRemoveIcon: true }}
                                                                beforeUpload={() => false} // 阻止自动上传
                                                                onRemove={(file) => {
                                                                    // 仅移除“新文件”；旧的 proofs 由后端字段维护
                                                                    if (file.uid.startsWith('new_')) {
                                                                        setFiles(prev => prev.filter((_, i) => `new_${i}` !== file.uid))
                                                                        URL.revokeObjectURL(file.url!)
                                                                    }
                                                                    return true
                                                                }}
                                                                onChange={(info) => {
                                                                    // 仅处理新添加的本地文件
                                                                    const added = info.fileList.filter(f => f.uid.startsWith('rc-upload')) // antd 临时 uid
                                                                    if (added.length) {
                                                                        const raw = added[added.length - 1].originFileObj as File
                                                                        if (raw) setFiles(prev => [...prev, raw])
                                                                    }
                                                                }}
                                                            >
                                                                <div className="flowx-upload-btn">+ 添加</div>
                                                            </Upload>
                                                        </div>

                                                        <div className="flowx-actions">
                                                            {/* <Button onClick={onBack} disabled={currentStepIndex === 0} loading={flowLoading}>
                                                                上一步
                                                            </Button> */}
                                                            <div style={{ width: '100%' }}>
                                                                <Button
                                                                    type="primary"
                                                                    className="gradient-btn"
                                                                    style={{ marginLeft: "45%" }}
                                                                    onClick={async () => {
                                                                        try {
                                                                            setFlowLoading(true)
                                                                            // ① File[] → Base64 Data URLs（与后端约定好的格式）
                                                                            const base64List = await Promise.all(files.map(fileToDataUrl))
                                                                            // 调用接口
                                                                            // const newProofUrls = files.map(f => URL.createObjectURL(f));
                                                                            await submitStep({
                                                                                orderId: flowOrder.id,
                                                                                stepKey: s.key,
                                                                                remark,
                                                                                files: base64List,
                                                                            })

                                                                            message.success('步骤已提交')


                                                                            // 本地更新 UI：把该步骤标记为 finish，并推进到下一个
                                                                            setFlowOrder(prev => {
                                                                                if (!prev) return prev;

                                                                                // 把新上传文件转成可预览 url（若你已拿到后端 url，就用后端返回的）
                                                                                const newProofUrls = files.map(f => URL.createObjectURL(f));

                                                                                const updatedSteps: FlowStep[] = prev.steps.map((step) => {
                                                                                    if (step.key !== s.key) return step;
                                                                                    return {
                                                                                        ...step,
                                                                                        // 关键：把 status 收窄为字面量
                                                                                        status: 'finish' as const,
                                                                                        // 若 FlowStep 没有 remark/proofs 字段，把这两行去掉或改到你真实字段上
                                                                                        remark,
                                                                                        proofs: [...(step.proofs ?? []), ...newProofUrls],
                                                                                    };
                                                                                });

                                                                                const nextIndex = Math.min(
                                                                                    (updatedSteps.findIndex(st => st.key === s.key) + 1),
                                                                                    updatedSteps.length - 1
                                                                                );
                                                                                const nextKey = updatedSteps[nextIndex].key; // 类型为 FlowStepKey

                                                                                // 返回值完整保持 EngineerOrder 结构
                                                                                return {
                                                                                    ...prev,
                                                                                    steps: updatedSteps,
                                                                                    currentKey: nextKey,
                                                                                };
                                                                            });


                                                                            // 清空输入框和上传缓存
                                                                            setRemark('')
                                                                            setFiles([])
                                                                        } catch (e: any) {
                                                                            message.error(e?.response?.data?.message || e?.message || '提交失败')
                                                                        } finally {
                                                                            setFlowLoading(false)
                                                                        }
                                                                    }}
                                                                    loading={flowLoading}
                                                                >
                                                                    {s.status !== 'finish' ? '提交本步骤' : '完成'}
                                                                </Button>

                                                            </div>
                                                        </div>
                                                    </>
                                                )}

                                                {/* 已完成：只读 */}
                                                {/* 已完成：可收缩 */}
                                                {/* !isCurrent && */}
                                                {isFinished && (
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
                                                )}


                                                {/* 待开始：提示 */}
                                                {!isCurrent && !isFinished && isFuture && (
                                                    <div className="flowx-todo">
                                                        <span className="flowx-tag">待开始</span>
                                                        <span className="flowx-hint">完成当前步骤后可继续</span>
                                                    </div>
                                                )}
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
        </Card>
    )
}

export default EngineerDashboard
