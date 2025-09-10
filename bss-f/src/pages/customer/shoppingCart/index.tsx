import React, { useEffect, useMemo, useState } from 'react'
import { Button, Card, Image, Space, Table, Tag, message, Popconfirm, Tooltip, InputNumber } from 'antd'
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
                    {r.period}
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
                                            await updateCartQty({ id: r.id, qty: newQty })
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
                            <Button size="small" type="primary" danger style={{ height: '30px' }} shape="round" icon={<DeleteOutlined />}>
                                删除
                            </Button>
                        </Popconfirm>
                    </Space>
                )
            },
        }


    ]

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
                                        await removeCartBatch(selectedRowKeys as string[])
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
                            <Button
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
                            </Button>
                        </div>
                    </div>
                )}
            />
        </Card>
    )
}

export default ShoppingCart
