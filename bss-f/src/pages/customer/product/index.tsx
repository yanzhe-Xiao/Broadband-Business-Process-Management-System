import React, { useEffect, useMemo, useState } from 'react'
import { Button, message, Modal, Descriptions, Tag } from 'antd'
import './Product.css'
import { getProducts, type ProductItem, type SortType } from '../../../api/customer'
import { useAuthStore } from '../../../store/auth'

export type ShowItem = {
    id: string
    title: string
    price: number
    rating: number
    stock: number | string
    monthlyFree?: number | null | string,
    yearlyFree?: number | null | string;
    foreverFree?: number | null | string;
    bandWidth?: number | null,
    cover: string
    tags?: string[]
}

export type ProductProps = {
    onAddToCart?: (item: ShowItem) => void
}

const Product: React.FC<ProductProps> = ({ onAddToCart }) => {
    // ======= 状态 =======
    const { roleName, username } = useAuthStore()

    // 查询条件
    const [query, setQuery] = useState('')
    const [sortKey, setSortKey] = useState<SortType>('pop')
    const [minPrice, setMinPrice] = useState<number | ''>('')
    const [maxPrice, setMaxPrice] = useState<number | ''>('')
    const [onlyInStock, setOnlyInStock] = useState(false)

    // 分页（服务端）
    const [current, setCurrent] = useState(1)
    const [size] = useState(8)
    const [total, setTotal] = useState(0)
    const [loading, setLoading] = useState(false)
    const [records, setRecords] = useState<ProductItem[]>([])

    // 详情弹窗
    const [detailOpen, setDetailOpen] = useState(false)
    const [detail, setDetail] = useState<ProductItem | null>(null)

    // 拉取数据
    const fetchData = async (page = current, pageSize = size) => {
        setLoading(true)
        try {
            const res = await getProducts({
                roleName,
                username,
                current: page,
                size: pageSize,
                keyword: query || undefined,
                sort: sortKey,
                onlyInStock,
                minPrice: typeof minPrice === 'number' ? minPrice : undefined,
                maxPrice: typeof maxPrice === 'number' ? maxPrice : undefined,
            })
            setRecords(res.records ?? [])
            setTotal(Number(res.total ?? 0))
            setCurrent(Number(res.current ?? page))
        } catch (e: any) {
            message.error(e?.response?.data?.message || e?.message || '获取商品列表失败')
        } finally {
            setLoading(false)
        }
    }

    useEffect(() => {
        fetchData(1, size)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [])

    useEffect(() => {
        fetchData(1, size)
        // eslint-disable-next-line react-hooks/exhaustive-deps
    }, [query, sortKey, onlyInStock, minPrice, maxPrice])

    // 方便通过 id(planCode) 找到原始记录
    const recordMap = useMemo(
        () => new Map(records.map(r => [r.planCode, r] as const)),
        [records]
    )

    // UI 映射
    const pageData: ShowItem[] = useMemo(() => {
        return records.map((r, i) => {
            const rating =
                typeof r.discount === 'number'
                    ? Math.max(4, Math.min(4.9, 5 - Number(r.discount) * 0.01))
                    : 4.4
            const stock =
                typeof r.qty === 'number' && r.qty > 0
                    ? Math.ceil(r.qty)
                    : 0
            return {
                id: r.planCode || `${r.name}-${i}`,
                title: r.name,
                price: Number(r.price ?? 0),
                monthlyFree: r.monthlyFree ?? null,
                yearlyFree: r.yearlyFree ?? null,
                foreverFree: r.foreverFree ?? null,
                bandWidth: r.bandWidth ?? null,
                rating,
                stock,
                cover: r.picture || 'https://picsum.photos/seed/fallback/600/400',
                tags: [
                    r.status === 'onSale' ? '热销' : undefined,
                    stock === 0 ? '断货' : undefined,
                ].filter(Boolean) as string[],
            }
        })
    }, [records])

    const totalPages = Math.max(1, Math.ceil(total / size))

    const handleAdd = (item: ShowItem, e?: React.MouseEvent) => {
        e?.stopPropagation?.()
        console.log(item);

        if (item.stock === 0) return message.info('到货后将第一时间通知你')

        const opts = getBillingOptions(item)
        if (opts.length === 0) {
            return message.warning('当前商品未设置计费价格，无法加入购物车')
        }
        // if (opts.length === 1) {
        //     // 只有一种计费方式，直接加入
        //     onAddToCart?.({ ...item, price: opts[0].price })
        //     return message.success(`已加入购物车（${opts[0].label}）`)
        // }
        // 多种计费方式 -> 弹窗选择
        setChooseItem(item)
        setChooseSelected(opts[0].type) // 默认选第一项
        setChooseOpen(true)
    }


    const openDetail = (item: ShowItem) => {
        const raw = recordMap.get(item.id)
        if (!raw) return
        setDetail(raw)
        setDetailOpen(true)
    }

    const closeDetail = () => {
        setDetailOpen(false)
        setDetail(null)
    }


    //添加选择
    type BillingType = 'monthly' | 'yearly' | 'lifetime'
    const [chooseOpen, setChooseOpen] = useState(false)
    const [chooseItem, setChooseItem] = useState<ShowItem | null>(null)
    const [chooseSelected, setChooseSelected] = useState<BillingType | null>(null)
    const getBillingOptions = (item: ShowItem) => {
        const opts: { type: BillingType; label: string; price: number }[] = []
        const m = Number(item.monthlyFree)
        const y = Number(item.yearlyFree)
        const f = Number(item.foreverFree)
        if (!Number.isNaN(m) && m > 0) opts.push({ type: 'monthly', label: `月费 ¥${m.toFixed(2)}`, price: m })
        if (!Number.isNaN(y) && y > 0) opts.push({ type: 'yearly', label: `年费 ¥${y.toFixed(2)}`, price: y })
        if (!Number.isNaN(f) && f > 0) opts.push({ type: 'lifetime', label: `永久 ¥${f.toFixed(2)}`, price: f })
        return opts
    }


    return (
        <div className="products-page">
            {/* 顶部展示区 */}
            <div className="products-hero">
                <div className="hero-badge">SHOP</div>
                <div style={{ height: '30px', width: '100%', display: 'flex', flexDirection: 'column', marginLeft: '20px', marginTop: '5px' }}>
                    <h2 style={{ fontSize: '24px', fontWeight: 600 }}>精选网络设备</h2>
                    <p style={{ height: '30px' }}>为高带宽与稳定连接而生，挑一款最适合你的设备。</p>
                </div>

            </div>

            {/* 工具条 */}
            <div className="products-toolbar">
                <input
                    className="search"
                    placeholder="搜索商品（如：路由器 / Mesh / 网线）"
                    value={query}
                    onChange={e => { setCurrent(1); setQuery(e.target.value) }}
                    disabled={loading}
                />

                <div className="filters">
                    <div className="price">
                        <span>价格：</span>
                        <input
                            type="number"
                            placeholder="最低"
                            value={minPrice}
                            onChange={e => { const v = e.target.value; setCurrent(1); setMinPrice(v === '' ? '' : +v) }}
                            disabled={loading}
                        />
                        <span className="tilde">~</span>
                        <input
                            type="number"
                            placeholder="最高"
                            value={maxPrice}
                            onChange={e => { const v = e.target.value; setCurrent(1); setMaxPrice(v === '' ? '' : +v) }}
                            disabled={loading}
                        />
                    </div>

                    <label className="checkbox">
                        <input
                            type="checkbox"
                            checked={onlyInStock}
                            onChange={e => { setCurrent(1); setOnlyInStock(e.target.checked) }}
                            disabled={loading}
                        />
                        仅看有货
                    </label>

                    <select
                        className="sort"
                        value={sortKey}
                        onChange={e => setSortKey(e.target.value as SortType)}
                        disabled={loading}
                    >
                        <option value="pop">综合</option>
                        <option value="priceUp">价格 ↑</option>
                        <option value="priceDown">价格 ↓</option>
                        <option value="rating">评分</option>
                    </select>
                </div>
            </div>

            {/* 商品网格 */}
            <div className="grid">
                {pageData.map(p => (
                    <div
                        key={p.id}
                        className="product-card product-card-clickable"
                        onClick={() => openDetail(p)}
                    >
                        <div className="product-cover">
                            {p.tags?.includes('断货') && <span className="ribbon soldout">缺货</span>}
                            {p.tags?.includes('新品') && <span className="ribbon new">新品</span>}
                            {p.tags?.includes('热销') && <span className="ribbon hot">热销</span>}
                            <img src={p.cover} alt={p.title} />
                        </div>
                        <div className="product-body">
                            <div className="product-title" title={p.title}>{p.title}</div>
                            <div className="product-meta">
                                <span className="price">
                                    {(() => {
                                        const m = Number(p.monthlyFree)
                                        const y = Number(p.yearlyFree)
                                        const f = Number(p.foreverFree)
                                        if (!Number.isNaN(m) && m > 0) return `¥ ${m.toFixed(2)}/月`
                                        if (!Number.isNaN(y) && y > 0) return `¥ ${y.toFixed(2)}/年`
                                        if (!Number.isNaN(f) && f > 0) return `¥ ${f.toFixed(2)}`
                                        return '价格待定'
                                    })()}
                                </span>

                                <span className="rating">★ {p.rating.toFixed(1)}</span>
                            </div>
                            <div className="product-actions" onClick={(e) => e.stopPropagation()}>
                                <Button
                                    type="primary"
                                    className="gradient-btn"
                                    disabled={p.stock === 0 || loading}
                                    onClick={(e) => handleAdd(p, e)}
                                >
                                    {p.stock === 0 ? '到货通知' : '加入购物车'}
                                </Button>
                                <span className={`stock ${p.stock === 0 ? 'zero' : ''}`}>
                                    {p.stock === 0 ? '无库存' : `库存 ${p.stock}`}
                                </span>
                            </div>
                        </div>
                    </div>
                ))}
                {pageData.length === 0 && <div className="empty">{loading ? '加载中...' : '没有匹配的商品'}</div>}
            </div>

            {/* 分页 */}
            <div className="pager">
                <Button disabled={current <= 1 || loading} onClick={() => fetchData(current - 1, size)}>上一页</Button>
                <span className="page-no">第 {current} / {totalPages} 页</span>
                <Button disabled={current >= totalPages || loading} onClick={() => fetchData(current + 1, size)}>下一页</Button>
            </div>
            {/* 详情弹窗 */}
            <Modal
                open={detailOpen}
                onCancel={closeDetail}
                footer={null}
                width={860}
                destroyOnClose
                className="product-detail-modal"
            >
                {detail && (
                    <div className="detail-container">
                        {/* 左侧封面图 */}
                        <div className="detail-cover">
                            <img
                                src={detail.picture || 'https://picsum.photos/seed/fallback/600/400'}
                                alt={detail.name}
                            />
                            <div className="cover-overlay">
                                {detail.status === 'onSale' && <Tag color="green" className='tag-style'>在售</Tag>}
                                {detail.status === 'offSale' && <Tag className='tag-style'>下架</Tag>}
                                {typeof detail.discount !== 'undefined' && (
                                    <Tag color="purple" className='tag-style'>折扣：{String(detail.discount)}</Tag>
                                )}
                                {detail.planPeriod && <Tag color="geekblue" className='tag-style'>周期：{String(detail.planPeriod)}</Tag>}
                            </div>
                        </div>

                        {/* 右侧信息 */}
                        <div className="detail-info">
                            <h2 className="detail-title">{detail.name}</h2>
                            <p className="detail-price">¥ {detail.price}</p>

                            <Descriptions column={1} bordered size="small" className="detail-desc">
                                <Descriptions.Item label="套餐编码">{detail.planCode}</Descriptions.Item>
                                <Descriptions.Item label="月费">
                                    {String(typeof (detail as any).monthlyFree === 'undefined' || detail.monthlyFree === 0 ? '-' : detail.monthlyFree + "¥/月")}
                                </Descriptions.Item>
                                <Descriptions.Item label="年费">
                                    {String(typeof (detail as any).yearlyFree === 'undefined' || detail.yearlyFree === 0 ? '-' : detail.yearlyFree + "¥/年")}
                                </Descriptions.Item>
                                <Descriptions.Item label="永久费">
                                    {String(typeof (detail as any).foreverFree === 'undefined' || detail.foreverFree === 0 ? '-' : detail.foreverFree + "¥")}
                                </Descriptions.Item>
                                {detail.bandWidth ?? -1 === -1 ? <></> :
                                    <Descriptions.Item label="宽带">
                                        {typeof detail.maxBandwidth === 'number' ? `${detail.maxBandwidth} Mbps` : '-'}
                                    </Descriptions.Item>
                                }
                                <Descriptions.Item label="是否需要IP">{detail.isIp || '-'}</Descriptions.Item>
                                <Descriptions.Item label="设备名称">{detail.deviceName || '-'}</Descriptions.Item>
                                <Descriptions.Item label="设备型号">{detail.requiredDeviceModel || '-'}</Descriptions.Item>
                            </Descriptions>


                            <div className="detail-actions">
                                <Button
                                    type="primary"
                                    className="gradient-btn"
                                    disabled={detail.qty === 0}
                                    onClick={() =>
                                        handleAdd({
                                            id: detail.planCode,
                                            title: detail.name,
                                            price: Number(detail.price),
                                            rating: 4.5,
                                            stock: detail.availableBandwidth ?? 0,
                                            cover: detail.picture || 'https://picsum.photos/seed/fallback/600/400',
                                            monthlyFree: (detail as any).monthlyFree,
                                            yearlyFree: (detail as any).yearlyFree,
                                            foreverFree: (detail as any).foreverFree,
                                        })
                                    }
                                >
                                    {detail.qty === 0 ? '到货通知' : '加入购物车'}
                                </Button>

                            </div>
                        </div>
                    </div>
                )
                }
            </Modal >
            <Modal
                open={chooseOpen}
                title="选择计费方式"
                onCancel={() => { setChooseOpen(false); setChooseItem(null) }}
                onOk={() => {
                    if (!chooseItem || !chooseSelected) return
                    const opts = getBillingOptions(chooseItem)
                    const picked = opts.find(o => o.type === chooseSelected)
                    if (!picked) return message.warning('请选择计费方式')

                    onAddToCart?.({ ...chooseItem, price: picked.price })
                    message.success(`已加入购物车（${picked.label}）`)
                    setChooseOpen(false)
                    setChooseItem(null)
                }}
                okText="确定"
                cancelText="取消"
                destroyOnClose
            >
                {chooseItem ? (
                    <div style={{ display: 'grid', gap: 12 }}>
                        <div style={{ fontWeight: 600 }}>{chooseItem.title}</div>
                        <div>
                            {/* 单选列表 */}
                            <div className="billing-radio-group">
                                {getBillingOptions(chooseItem).map(opt => (
                                    <label key={opt.type} className={`billing-radio ${chooseSelected === opt.type ? 'active' : ''}`}>
                                        <input
                                            type="radio"
                                            name="billing"
                                            value={opt.type}
                                            checked={chooseSelected === opt.type}
                                            onChange={() => setChooseSelected(opt.type)}
                                        />
                                        <span>{opt.label}</span>
                                    </label>
                                ))}
                            </div>
                        </div>
                    </div>
                ) : null}
            </Modal>

        </div >
    )
}

export default Product
