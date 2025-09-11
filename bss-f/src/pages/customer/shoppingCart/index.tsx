import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Image, Space, Table, Tag, message, Popconfirm, Tooltip, InputNumber, Modal } from 'antd'
import { Form, Input } from 'antd'
import type { ColumnsType } from 'antd/es/table'
import { DeleteOutlined, ReloadOutlined, ShoppingCartOutlined } from '@ant-design/icons'
import './shoppingCart.css'
import { useAuthStore } from '../../../store/auth'
import {
    getCart,
    removeCartItem,
    removeCartBatch,
    clearCart,
    checkoutApi,
    type CartItem,
    type PlanType,
    updateCartQty,
    sendAddress,
} from '../../../api/cart'

const typeMap: Record<PlanType, { label: string; color: string; unit?: string }> = {
    month: { label: '月费', color: 'processing', unit: '/月' },
    year: { label: '年费', color: 'success', unit: '/年' },
    forever: { label: '永久', color: 'purple', unit: '' },
}

const ShoppingCart: React.FC = () => {
    const username = useAuthStore(s => s.username)
    const [loading, setLoading] = useState(false)
    const [data, setData] = useState<CartItem[]>([])
    const [current, setCurrent] = useState(1)
    const [size, setSize] = useState(10)
    const [total, setTotal] = useState(0)
    const [orderIdStorage, setOrderIdStorage] = useState(-1)
    // 选择状态
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
    const fetchList = async (page = current, pageSize = size) => {
        if (!username) return
        setLoading(true)
        try {
            const resp = await getCart({ username, current: page, size: pageSize })
            setData(resp.records)
            setTotal(resp.total)
            setCurrent(resp.current)
            setSize(resp.size)
            console.log(resp.total);

            // 若之前选择的项被删除，收敛一下
            setSelectedRowKeys(prev => prev.filter(k => resp.records.some(i => i.id === k)))
        } catch (e: any) {
            message.error(e?.response?.data?.message || e?.message || '获取购物车失败')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => { fetchList(1, size) }, []) // eslint-disable-line

    const totalSelectedPrice = useMemo(() => {
        return data
            .filter(i => selectedRowKeys.includes(i.id))
            .reduce((sum, it) => sum + Number(it.price) * Number(it.qty), 0)
    }, [data, selectedRowKeys])

    // 行内编辑数量的映射：{ [id]: number }
    const [editQty, setEditQty] = useState<Record<string, number>>({})
    // data 变化时，给没有值的行初始化一次
    useEffect(() => {
        setEditQty(prev => {
            const next = { ...prev }
            data.forEach(r => {
                if (next[r.id] == null) next[r.id] = Number(r.qty) || 1
            })
            return next
        })
    }, [data])



    const columns: ColumnsType<CartItem> = [
        {
            title: '商品',
            dataIndex: 'name',
            key: 'product',
            width: 370,
            render: (_: any, r) => (
                <div className="cart-product">
                    <Image
                        src={r.imageUrl || 'https://picsum.photos/seed/cart/320/200'}
                        width={96}
                        height={64}
                        style={{ objectFit: 'cover', borderRadius: 8 }}
                        preview={false}
                    />
                    <div className="cart-product-meta">
                        <div className="title" title={r.name}>{r.name}</div>
                        <div className="sub">
                            <Tag color={typeMap[r.planType].color}>
                                {typeMap[r.planType].label}{typeMap[r.planType].unit}
                            </Tag>
                            <span className="plan-code">编码：{r.planCode}</span>
                            {r.deviceName && <span className="sep">·</span>}
                            {r.deviceName && <span>{r.deviceName}</span>}
                            {r.model && <span className="light">（{r.model}）</span>}
                        </div>
                    </div>
                </div>
            ),
        },
        {
            title: '单价',
            dataIndex: 'price',
            key: 'price',
            width: 120,
            render: (v, r) => <>¥ {Number(v).toFixed(2)}<span className="unit">{typeMap[r.planType].unit}</span></>,
            sorter: (a, b) => Number(a.price) - Number(b.price),
        },
        {
            title: '数量',
            dataIndex: 'qty',
            key: 'qty',
            width: 100,
            render: (_, r) => (
                <>
                    {r.qty}
                </>
            ),
        },
        {
            title: '小计',
            key: 'subtotal',
            width: 140,
            render: (_, r) => <>¥ {(Number(r.price) * Number(r.qty)).toFixed(2)}</>,
        },
        {
            title: '可用日期范围',
            key: 'qty',
            width: 140,
            render: (_, r) => (
                <>
                    {r.period} 天
                </>
            )
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 120,
            render: (s: string) => <Tag color="blue">{s || '在购物车中'}</Tag>,
        },
        {
            title: '操作',
            key: 'action',
            fixed: 'right',
            width: 260,
            render: (_, r) => {
                const disabled = r.planType === 'forever'
                const value = editQty[r.id] ?? r.qty

                return (
                    <Space size="middle">
                        {/* 一体化数量控件 */}
                        <Tooltip title={disabled ? '永久买断套餐不可修改数量' : ''}>
                            <Space.Compact size="small" className="qty-compact">
                                <div className="qty-chip" style={{ width: 77 }}>增加至</div>
                                <InputNumber
                                    min={1}
                                    max={99}
                                    value={value}
                                    style={{ width: 40, paddingTop: 3 }}
                                    onChange={(v) => {
                                        const qty = Number(v || 1)
                                        setEditQty(q => ({ ...q, [r.id]: qty }))
                                    }}
                                    disabled={disabled}
                                    className="qty-input"
                                />
                                <Button
                                    type="primary"

                                    onClick={async () => {
                                        if (disabled) return
                                        try {
                                            const newQty = Number(editQty[r.id] ?? r.qty)
                                            await updateCartQty({ id: r.id, qty: newQty, planType: r.planType })
                                            setData(prev => prev.map(it => it.id === r.id ? { ...it, qty: newQty } : it))
                                            message.success(`数量已更新为 ${newQty}`)
                                        } catch (e: any) {
                                            message.error(e?.response?.data?.message || e?.message || '更新数量失败')
                                        }
                                    }}
                                    disabled={disabled}
                                    className="qty-update-btn"
                                >
                                    更新
                                </Button>
                            </Space.Compact>
                        </Tooltip>

                        {/* 删除按钮保持不变 */}
                        <Popconfirm
                            title="确认移除此商品？"
                            okText="删除"
                            okButtonProps={{ danger: true }}
                            placement="topRight"
                            onConfirm={async () => {
                                try {
                                    await removeCartItem(r.id)
                                    message.success('已删除')
                                    fetchList()
                                } catch (e: any) {
                                    message.error(e?.response?.data?.message || e?.message || '删除失败')
                                }
                            }}
                        >
                            {/* <Button size="small" type="primary" danger style={{ height: '30px' }} shape="round" icon={<DeleteOutlined />}>
                                删除
                            </Button> */}
                        </Popconfirm>
                    </Space>
                )
            },
        }


    ]


    // ...

    // 1) 新增：步骤状态
    type PayMethod = 'alipay' | 'wechat' | 'unionpay' | 'applepay'
    const [payOpen, setPayOpen] = useState(false)
    const [payMethod, setPayMethod] = useState<PayMethod>('alipay')
    const [payLoading, setPayLoading] = useState(false)
    const [payForm] = Form.useForm()
    const [payStep, setPayStep] = useState<0 | 1>(0) // 0=地址，1=支付

    return (
        <Card className="cart-card" bodyStyle={{ padding: 16 }}>
            <div className="cart-header">
                <div className="badge"><ShoppingCartOutlined /></div>
                <div className="title-wrap">
                    <h3>我的购物车</h3>
                    <p>已选择 <b>{selectedRowKeys.length}</b> 项，共 <b>{data.length}</b> 件商品</p>
                </div>
                <Space>
                    <Button icon={<ReloadOutlined />} onClick={() => fetchList()} />
                    <Popconfirm
                        title="确认清空购物车？"
                        okText="清空"
                        okButtonProps={{ danger: true }}
                        onConfirm={async () => {
                            try {
                                await clearCart(username)
                                message.success('已清空')
                                fetchList(1, size)
                            } catch (e: any) {
                                message.error(e?.response?.data?.message || e?.message || '清空失败')
                            }
                        }}
                    >
                        <Button danger>清空</Button>
                    </Popconfirm>
                </Space>
            </div>

            <Table<CartItem>
                rowKey="id"
                loading={loading}
                columns={columns}
                dataSource={data}
                className="cart-table"
                tableLayout="fixed"
                scroll={{ x: 'max-content' }}
                rowSelection={{
                    selectedRowKeys,
                    onChange: (keys) => setSelectedRowKeys(keys),
                    selections: [Table.SELECTION_ALL, Table.SELECTION_INVERT, Table.SELECTION_NONE],
                }}
                pagination={{
                    current,
                    pageSize: size,
                    total,
                    showSizeChanger: true,
                    showTotal: t => `共 ${t} 件`,
                    onChange: (p, ps) => { setCurrent(p); setSize(ps); fetchList(p, ps) },
                }}
                footer={() => (
                    <div className="cart-footer">
                        <Space size="large">
                            <Popconfirm
                                title="删除所选商品？"
                                okText="删除"
                                okButtonProps={{ danger: true }}
                                disabled={selectedRowKeys.length === 0}
                                onConfirm={async () => {
                                    try {
                                        await removeCartBatch(selectedRowKeys as number[])
                                        message.success('已删除所选')
                                        setSelectedRowKeys([])
                                        fetchList()
                                    } catch (e: any) {
                                        message.error(e?.response?.data?.message || e?.message || '删除失败')
                                    }
                                }}
                            >
                                <Button danger disabled={selectedRowKeys.length === 0} icon={<DeleteOutlined />}>
                                    删除所选
                                </Button>
                            </Popconfirm>
                        </Space>

                        <div className="sum">
                            <span>合计：</span>
                            <b>¥ {totalSelectedPrice.toFixed(2)}</b>
                            {/* <Button
                                type="primary"
                                className="gradient-btn"
                                style={{ marginLeft: 16 }}
                                disabled={selectedRowKeys.length === 0}
                                onClick={async () => {
                                    try {
                                        await checkoutApi({ username, itemIds: selectedRowKeys as string[] })
                                        message.success('下单成功')
                                        setSelectedRowKeys([])
                                        fetchList()
                                    } catch (e: any) {
                                        message.error(e?.response?.data?.message || e?.message || '下单失败')
                                    }
                                }}
                            >
                                去结算
                            </Button> */}

                            <Button
                                type="primary"
                                className="gradient-btn"
                                style={{ marginLeft: 16 }}
                                disabled={selectedRowKeys.length === 0}
                                onClick={() => {
                                    payForm.resetFields()
                                    setPayMethod('alipay')
                                    setPayStep(0)           // 进入地址步骤
                                    setPayOpen(true)
                                }}
                            >
                                去结算
                            </Button>


                        </div>
                        <Modal
                            title={null}
                            open={payOpen}
                            onCancel={() => setPayOpen(false)}
                            destroyOnClose
                            footer={null}
                            width={680}
                            className="pay-modal"
                        >
                            {payOpen && (
                                <div className="pay-wrap">
                                    {/* 头部 */}
                                    <div className="pay-head">
                                        <div className="pay-badge">PAY</div>
                                        <div>
                                            <div className="pay-title">
                                                {payStep === 0 ? '填写服务地址' : '选择支付方式'}
                                            </div>
                                            <div className="pay-sub">
                                                当前共 {selectedRowKeys.length} 项，应付金额
                                            </div>
                                        </div>
                                        <div className="pay-amount">¥ {totalSelectedPrice.toFixed(2)}</div>
                                    </div>

                                    {/* 步骤指示（可选） */}
                                    <div style={{ padding: '0 8px 16px' }}>
                                        <Space size="small">
                                            <Tag color={payStep === 0 ? 'processing' : 'default'}>1 地址</Tag>
                                            <span>—</span>
                                            <Tag color={payStep === 1 ? 'processing' : 'default'}>2 支付</Tag>
                                        </Space>
                                    </div>

                                    {/* Step 0: 地址 */}
                                    {payStep === 0 && (
                                        <div className="addr-panel">
                                            <div className="addr-head">
                                                <div className="addr-badge">ADR</div>
                                                <div className="addr-title">服务地址信息</div>
                                                <div className="addr-sub">请填写本次安装/施工的准确地址</div>
                                            </div>

                                            <Form
                                                form={payForm}
                                                layout="vertical"
                                                requiredMark={false}
                                                initialValues={{ province: '', city: '', district: '', detailAddress: '' }}
                                            >
                                                <div className="addr-grid">
                                                    <Form.Item
                                                        label="省/自治区/直辖市"
                                                        name="province"
                                                        rules={[{ required: true, message: '请输入省/直辖市' }]}
                                                    >
                                                        <Input placeholder="如：浙江省 / 上海市" />
                                                    </Form.Item>
                                                    <Form.Item
                                                        label="地级市/自治州"
                                                        name="city"
                                                    >
                                                        <Input placeholder="直辖市可留空或与省相同" />
                                                    </Form.Item>
                                                    <Form.Item
                                                        label="区/县"
                                                        name="district"
                                                        rules={[{ required: true, message: '请输入区/县' }]}
                                                    >
                                                        <Input placeholder="如：余杭区 / 海淀区" />
                                                    </Form.Item>
                                                    <Form.Item
                                                        label="详细地址"
                                                        name="detailAddress"
                                                        className="addr-col-span"
                                                        rules={[
                                                            { required: true, message: '请输入详细地址' },
                                                            { min: 5, message: '不少于 5 个字符' },
                                                        ]}
                                                    >
                                                        <Input.TextArea
                                                            placeholder="小区/写字楼/门牌号等，如：良渚文化村××苑 16 幢 302 室"
                                                            rows={2}
                                                            showCount
                                                            maxLength={120}
                                                        />
                                                    </Form.Item>
                                                </div>
                                            </Form>

                                            {/* 底部操作（地址步） */}
                                            <div className="pay-actions">
                                                <Button onClick={() => setPayOpen(false)}>取消</Button>
                                                <Button
                                                    type="primary"
                                                    className="gradient-btn"
                                                    onClick={async () => {
                                                        try {
                                                            await payForm.validateFields()   // 先校验地址
                                                            setPayStep(1)                  // 进入支付步骤
                                                            const addr = await payForm.validateFields()
                                                            let data = {
                                                                orderItemId: selectedRowKeys as number[],
                                                                province: addr.province as string,
                                                                city: addr.city as string,
                                                                district: addr.district as string,
                                                                detailAddress: addr.detailAddress as string
                                                            }
                                                            const res = await sendAddress(data)
                                                            setOrderIdStorage(res as unknown as number)

                                                        } catch { /* 校验不通过会自动提示 */ }
                                                    }}
                                                >
                                                    下一步
                                                </Button>
                                            </div>
                                        </div>
                                    )}

                                    {/* Step 1: 支付 */}
                                    {payStep === 1 && (
                                        <>
                                            {/* 支付方式卡片 */}
                                            <div className="pay-methods">
                                                {[
                                                    { key: 'alipay', label: '支付宝', desc: '推荐支付宝用户', img: '/支付宝支付.png' },
                                                    { key: 'wechat', label: '微信支付', desc: '推荐微信用户', img: '/微信.png' },
                                                    { key: 'unionpay', label: '银联云闪付', desc: '银行卡快捷', img: '/银联.png' },
                                                    { key: 'applepay', label: 'Apple Pay', desc: '推荐苹果用户', img: '/apple-pay.png' },
                                                ].map(m => (
                                                    <button
                                                        key={m.key}
                                                        type="button"
                                                        className={`pay-card ${payMethod === m.key ? 'active' : ''}`}
                                                        onClick={() => setPayMethod(m.key as PayMethod)}
                                                    >
                                                        <img src={m.img} alt={m.label} className="pay-icon" />
                                                        <div className="pay-info">
                                                            <div className="pay-label">{m.label}</div>
                                                            <div className="pay-desc">{m.desc}</div>
                                                        </div>
                                                        <div className="pay-check" />
                                                    </button>
                                                ))}
                                            </div>

                                            {/* 二维码区 */}
                                            <div className="pay-panel">
                                                <div className="pay-hint">
                                                    {payMethod === 'alipay'
                                                        ? '请使用 支付宝 扫码支付'
                                                        : payMethod === 'wechat'
                                                            ? '请使用 微信 扫码支付'
                                                            : payMethod === 'unionpay'
                                                                ? '请打开 云闪付 完成支付'
                                                                : '请使用 Apple Pay 完成支付'}
                                                </div>
                                                <img
                                                    src={
                                                        payMethod === 'alipay'
                                                            ? '/qrcode/alipay.png'
                                                            : payMethod === 'wechat'
                                                                ? '/qrcode/weixin.png'
                                                                : payMethod === 'unionpay'
                                                                    ? '/qrcode/yinlian.png'
                                                                    : '/qrcode/apple.png'
                                                    }
                                                    alt={`${payMethod} 二维码`}
                                                    className="qr-img"
                                                />
                                            </div>

                                            {/* 底部操作（支付步） */}
                                            <div className="pay-actions">
                                                {/* <Button onClick={() => setPayStep(0)}>上一步</Button> */}
                                                <Button
                                                    type="primary"
                                                    className="gradient-btn"
                                                    loading={payLoading}
                                                    onClick={async () => {
                                                        try {
                                                            // const addr = await payForm.validateFields() // 防止回退后被清空的情况
                                                            setPayLoading(true)
                                                            await checkoutApi({
                                                                username,
                                                                itemIds: selectedRowKeys as string[],
                                                                // payMethod,
                                                                // amount: Number(totalSelectedPrice),
                                                                // province: addr.province,
                                                                // city: addr.city,
                                                                // district: addr.district,
                                                                // detailAddress: addr.detailAddress,
                                                                orderId: orderIdStorage ?? "0"
                                                            } as any)
                                                            message.success('下单成功，已发起支付')
                                                            setPayOpen(false)
                                                            setSelectedRowKeys([])
                                                            payForm.resetFields()
                                                            fetchList()
                                                        } catch (e: any) {
                                                            if (!e?.errorFields) {
                                                                message.error(e?.response?.data?.message || e?.message || '下单失败')
                                                            }
                                                        } finally {
                                                            setPayLoading(false)
                                                        }
                                                    }}
                                                >
                                                    我已完成支付
                                                </Button>
                                            </div>
                                        </>
                                    )}
                                </div>
                            )}
                        </Modal >
                    </div>
                )}
            />
        </Card>
    )
}

export default ShoppingCart
