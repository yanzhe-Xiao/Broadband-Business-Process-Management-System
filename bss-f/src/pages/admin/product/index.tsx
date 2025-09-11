import React, { useEffect, useState } from 'react'
import { Upload } from 'antd'
import type { UploadFile } from 'antd/es/upload/interface'
// import { getDeviceList, type DeviceInfo } from '../../../api/device'

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
import { getDeviceList, type DeviceInfo } from '../../../api/device'


const statusOptions = [
    { label: 'ACTIVE', value: 'ACTIVE' },
    { label: 'INACTIVE', value: 'INACTIVE' },
]

const isIpOptions = [
    { label: '是', value: 1 },
    { label: '否', value: 2 },
]
const fileToBase64 = (file: File) =>
    new Promise<string>((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => resolve(String(reader.result))
        reader.onerror = reject
        reader.readAsDataURL(file)
    })


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
    // **新增：设备下拉相关状态**
    const [deviceOpts, setDeviceOpts] = useState<DeviceInfo[]>([])
    const [deviceLoading, setDeviceLoading] = useState(false)
    const [deviceSearchTimer, setDeviceSearchTimer] = useState<any>(null)

    // 修复点：确保 deviceOpts 始终为数组
const loadDevices = async (keyword?: string) => {
    setDeviceLoading(true)
    try {
        const list = await getDeviceList(keyword ? { keyword } : undefined)
        // 确保 list 是数组类型，否则设为空数组
        setDeviceOpts(Array.isArray(list) ? list : [])
    } catch (e: any) {
        message.error(e?.message || '获取设备列表失败')
        // 出错时也设为空数组，避免后续 .map 报错
        setDeviceOpts([])
    } finally {
        setDeviceLoading(false)
    }
}


    // **弹窗打开时加载设备数据**
    useEffect(() => {
        if (open) {
            loadDevices()  // 弹窗打开时获取所有设备
        }
    }, [open])

    // **搜索防抖函数（可选）**
    const handleDeviceSearch = (val: string) => {
        if (deviceSearchTimer) {
            clearTimeout(deviceSearchTimer)
        }
        setDeviceSearchTimer(setTimeout(() => {
            // 输入为空时加载全部设备
            loadDevices(val || undefined)
        }, 300))
    }
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
    // Upload 受控
    const [fileList, setFileList] = useState<UploadFile[]>([])
    // 打开弹窗时，给 Upload 做回显（编辑用）
    useEffect(() => {
        if (!open) {
            setFileList([])
            return
        }
        if (editing) {
            // 优先用已有 base64，否则用 picture(URL)
            const url = editing.imageBase64 || editing.picture
            if (url) {
                setFileList([{
                    uid: '-1',
                    name: 'cover.png',
                    status: 'done',
                    url, // antd 直接预览
                }])
                // 同步回表单字段，保证不改图也能提交
                upForm.setFieldsValue({ imageBase64: url })
            }
        } else {
            setFileList([])
            upForm.setFieldsValue({ imageBase64: undefined })
        }
    }, [open, editing, upForm])


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

    const columns: ColumnsType<AdminPlanItem> = [
        {
            title: '展示', dataIndex: 'imageBase64', key: 'imageBase64', width: 110,
            render: (_, r) => {
                return <Image src={r.picture} width={88} height={56} style={{ objectFit: 'cover', borderRadius: 8 }} />
            }
        },
        { title: '套餐编码', dataIndex: 'planCode', key: 'planCode', width: 150, sorter: true, ellipsis: true },
        { title: '套餐名称', dataIndex: 'name', key: 'name', width: 220, ellipsis: true },
        // {
        //     title: '售卖价(¥)', dataIndex: 'price', key: 'price', width: 120, sorter: true,
        //     render: (v) => (typeof v === 'number' ? `¥ ${v.toFixed(2)}` : '-')
        // },
        {
            title: '计费', key: 'billing', width: 240, ellipsis: true,
            render: (_, r) => (
                <span className="billing-tags">
                    {r.monthlyFee != null && <Tag color="processing">月 {`¥ ${Number(r.monthlyFee).toFixed(2)}`}</Tag>}
                    {r.yearlyFee != null && <Tag color="success">年 {`¥ ${Number(r.yearlyFee).toFixed(2)}`}</Tag>}
                    {r.foreverFee != null && <Tag color="purple">永久 {`¥ ${Number(r.foreverFee).toFixed(2)}`}</Tag>}
                    {!r.monthlyFee && !r.yearlyFee && !r.foreverFee && <Tag>未设置</Tag>}
                </span>
            )
        },
        // { title: '有效期(月)', dataIndex: 'planPeriod', key: 'planPeriod', width: 110, sorter: true },
        {
            title: '折扣(%)', dataIndex: 'discount', key: 'discount', width: 100, sorter: true,
            render: (v) => v == null ? '-' : `${v}%`
        },
        { title: '库存', dataIndex: 'qty', key: 'qty', width: 100 },
        {
            title: "设备", dataIndex: 'requireDeviceSn', key: 'requireDeviceSn', width: 100
            , render: (v) => v == "" ? '-' : v
        },
        {
            title: "设备模型", dataIndex: 'requiredDeviceModel', key: "requiredDeviceModel", width: 100
            , render: (v) => v == "" ? '-' : v
        },
        {
            title: '设备数', dataIndex: 'requiredDeviceQty', key: 'requiredDeviceQty', width: 110,
        },
        { title: '带宽(MB)', dataIndex: 'bandwidth', key: 'bandwidth', width: 120, sorter: true },
        {
            title: '评分', dataIndex: 'rating', key: 'rating', width: 90, sorter: true,
            render: (v) => v != null ? Number(v).toFixed(1) : '-'
        },
        {
            title: '状态', dataIndex: 'status', key: 'status', width: 100,
            render: (s) => s === 'ACTIVE' ? <Tag color="green">ACTIVE</Tag> : <Tag>下架</Tag>
        },
        {
            title: '是否配置IP', dataIndex: 'isIp', key: "isIp", width: 100,
            render: (s) => s === 1 ? '是' : '否'
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
            delete values.billingTypes

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


    // const [deviceOpts, setDeviceOpts] = useState<DeviceInfo[]>([])
    // const [deviceLoading, setDeviceLoading] = useState(false)
    // const [deviceSearchTimer, setDeviceSearchTimer] = useState<any>(null)

    // const loadDevices = async (kw?: string) => {
    //     setDeviceLoading(true)
    //     try {
    //         const list = await getDeviceList(kw ? { keyword: kw } : undefined)
    //         setDeviceOpts(list || [])
    //     } finally {
    //         setDeviceLoading(false)
    //     }
    // }
    // useEffect(() => {
    //     if (open) {
    //         loadDevices()
    //     }
    // }, [open])
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
                    initialValues={{ status: 'ACTIVE', billingTypes: [] }}   // ← 默认不选
                >
                    <Form.Item label="套餐编码" name="planCode" rules={[{ required: true, message: '请输入编码' }]}>
                        <Input placeholder="例如：PLAN-1001" disabled={!!editing} />
                    </Form.Item>

                    <Form.Item label="套餐名称" name="name" rules={[{ required: true, message: '请输入名称' }]}>
                        <Input placeholder="例如：千兆宽带 1000M" />
                    </Form.Item>

                    {/* <Form.Item label="套餐有效期（月）" name="planPeriod">
                        <InputNumber min={1} precision={0} style={{ width: '100%' }} placeholder="月" />
                    </Form.Item> */}
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
                    {/* <Form.Item label="评分" name="rating" >
                        <InputNumber min={0} precision={1} max={5} style={{ width: '100%' }} placeholder="请输入评分" />
                    </Form.Item> */}
                    <Form.Item label="设备资源" name="deviceSN" >
                        {/* 将数字输入框改为下拉选择框 */}
                    <Select
                        style={{ width: '100%' }}
                        placeholder="请选择设备"
                        showSearch
                        onSearch={handleDeviceSearch}       // 输入搜索时调用
                        filterOption={false}                // 关闭本地筛选，使用后端搜索
                        loading={deviceLoading}
                        options={deviceOpts.map(device => ({
                            value: device.sn,
                            label: device.sn
                        }))}
                    />
                    </Form.Item>
                    <Form.Item label="所需设备数量" name="deviceQty" >
                        <InputNumber min={0} precision={0} style={{ width: '100%' }} placeholder="10" />
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
                        label="展示图片"
                        required
                        // 自定义校验：必须存在 imageBase64
                        rules={[
                            {
                                validator: async () => {
                                    const val = upForm.getFieldValue('imageBase64')
                                    if (!val) return Promise.reject(new Error('请上传展示图片'))
                                },
                            },
                        ]}
                    >
                        <Upload
                            listType="picture-card"
                            fileList={fileList}
                            accept="image/*"
                            maxCount={1}
                            // 阻止自动上传（改为本地处理）
                            beforeUpload={() => false}
                            onChange={async (info) => {
                                const fl = info.fileList.slice(-1)  // 只保留最后一张
                                setFileList(fl)
                                const f = fl[0]
                                // 新选的本地文件（originFileObj 存在），转成 Base64 存到表单
                                if (f?.originFileObj) {
                                    const base64 = await fileToBase64(f.originFileObj as File)
                                    upForm.setFieldsValue({ imageBase64: base64 })
                                }
                                // 若只是回显/保持原图（无 originFileObj），保持当前 form 值不变
                            }}
                            onRemove={() => {
                                setFileList([])
                                upForm.setFieldsValue({ imageBase64: undefined })
                            }}
                        >
                            {fileList.length >= 1 ? null : (
                                <div style={{ width: 100 }}>
                                    <div style={{ fontSize: 28, lineHeight: 1 }}>＋</div>
                                    <div>上传</div>
                                </div>
                            )}
                        </Upload>
                        {/* 隐藏域：真正提交给后端的 base64 值 */}
                        <Form.Item name="imageBase64" noStyle>
                            <Input type="hidden" />
                        </Form.Item>
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
                            name="monthlyFee"
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
                            name="yearlyFee"
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
                            name="foreverFee"
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




