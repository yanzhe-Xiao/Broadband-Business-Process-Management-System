import React, { Suspense, useState } from 'react'
import {
    AppstoreOutlined,
    UserOutlined,
    ShoppingCartOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    LogoutOutlined,
} from '@ant-design/icons'
import { Button, Layout, Menu, theme, Dropdown, Avatar, Space, Badge, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import './Customer.css'
import Product from './product'
import { LazyLoading } from '../../../components'
import Profile from './home'
import { useAuthStore } from '../../store/auth'
const { Header, Sider, Content } = Layout

// 模拟当前用户信息
const currentUser = {
    name: '张三',
    avatar:
        'https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png',
}

// 模拟未读订单数量
const unreadOrders = 3

// 定义菜单项类型
type MenuKey = 'products' | 'profile' | 'orders'

const Customer: React.FC = () => {
    const [collapsed, setCollapsed] = useState(false)
    const [selectedKey, setSelectedKey] = useState<MenuKey>('products')
    const [messageApi, contextHolder] = message.useMessage()
    const name = useAuthStore(s => s.username)
    const nav = useNavigate()
    const {
        token: { colorBgContainer, borderRadiusLG },
    } = theme.useToken()

    const logout = useAuthStore(s => s.logout)
    const menuItems = [
        {
            key: 'products',
            icon: <AppstoreOutlined />,
            label: '商品展示',
        },
        {
            key: 'profile',
            icon: <UserOutlined />,
            label: '用户信息',
        },
        {
            key: 'orders',
            icon: <ShoppingCartOutlined />,
            label: (
                <>
                    我的订单
                    {unreadOrders > 0 && (
                        <Badge count={unreadOrders} size="small" style={{ marginInlineStart: 6 }} />
                    )}
                </>
            ),
        },
    ]

    const userMenu = (
        <Menu
            items={[
                {
                    key: 'profile',
                    icon: <UserOutlined />,
                    label: '个人资料',
                    onClick: () => {
                        setSelectedKey('profile')
                    }
                },
                {
                    type: 'divider',
                },
                {
                    key: 'logout',
                    icon: <LogoutOutlined />,
                    label: '退出登录',
                    onClick: () => {
                        logout()                // ✅ 清理 token
                        messageApi.open({
                            type: 'success',
                            content: '退出成功',
                            duration: 1,
                            onClose: () => nav('/login', { replace: true })
                        })
                    },
                },
            ]}
        />
    )

    const renderContent = () => {
        switch (selectedKey) {
            case 'products':
                return (
                    <Suspense fallback={<LazyLoading />}>
                        <Product />
                    </Suspense>)
            case 'profile':
                return (
                    <Suspense fallback={<LazyLoading />}>
                        <Profile />
                    </Suspense>)
            case 'orders':
                return (
                    <div className="card-like">这里是我的订单页面的内容... 有 {unreadOrders} 个待处理订单。</div>
                )
            default:
                return <div className="card-like">加载中...</div>
        }
    }

    return (
        <Layout className="customer-root">
            {/* 侧边栏 */}
            <Sider trigger={null} collapsible collapsed={collapsed} className="customer-sider">
                {/* Logo */}
                <div className="customer-logo">
                    {collapsed ? 'BBS' : '宽带办理系统'}
                </div>

                {/* 菜单 */}
                <Menu
                    theme="dark"
                    mode="inline"
                    selectedKeys={[selectedKey]}
                    onClick={({ key }) => setSelectedKey(key as MenuKey)}
                    items={menuItems}
                    className="customer-menu"
                />
            </Sider>
            {contextHolder}
            <Layout className="customer-main">
                {/* 顶部条 */}
                <Header
                    className={`customer-header ${collapsed ? 'is-collapsed' : ''}`}
                    style={{ background: colorBgContainer }}
                >
                    <Button
                        type="text"
                        icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                        onClick={() => setCollapsed(!collapsed)}
                        style={{
                            fontSize: '20px',
                            height: '64px',
                            width: '64px'
                        }}
                    />

                    <div className="header-actions">
                        <Badge count={unreadOrders} offset={[10, 0]} size='small' style={{ zIndex: 1000 }}>
                            <ShoppingCartOutlined className="header-icon" />
                        </Badge>

                        <Dropdown overlay={userMenu} placement="bottomRight">
                            <Space className="user-chip">
                                <Avatar src={currentUser.avatar} icon={<UserOutlined />} />
                                <span className="user-name">{name}</span>
                            </Space>
                        </Dropdown>
                    </div>
                </Header>

                <Content className="customer-content" style={{ background: colorBgContainer, borderRadius: borderRadiusLG }}>
                    {renderContent()}
                </Content>
            </Layout>
        </Layout>
    )
}

export default Customer
