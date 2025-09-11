import React, { Suspense, useState } from 'react'
import {
    AppstoreOutlined,
    UserOutlined,
    ShoppingCartOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    LogoutOutlined,
    ProfileOutlined,
} from '@ant-design/icons'
import { Button, Layout, Menu, theme, Dropdown, Avatar, Space, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import './customer.css'
import Product from './product'
import { LazyLoading } from '../../../components'
import { useAuthStore } from '../../store/auth'
import ShoppingCart from './shoppingCart'
import ProfilePage from './home'

import MyOrders from './order'
const { Header, Sider, Content } = Layout

// 模拟当前用户信息
const currentUser = {
    name: '张三',
    avatar:
        'https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png',
}

// 模拟未读订单数量

// 定义菜单项类型
type MenuKey = 'products' | 'profile' | 'orders' | 'shoppingCart'

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
            key: 'shoppingCart',
            icon: <ShoppingCartOutlined />,
            label: '购物车'
        },
        {
            key: 'orders',
            icon: <ProfileOutlined />,
            label: '我的订单'
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
                        <ProfilePage />
                    </Suspense>)
            case 'orders':
                return (
                    <MyOrders />
                )
            case 'shoppingCart':
                return (
                    <Suspense fallback={<LazyLoading />}>
                        <ShoppingCart />
                    </Suspense>
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
                        <ShoppingCartOutlined className="header-icon" />
                        <Dropdown overlay={userMenu} placement="bottomRight">
                            <Space className="user-chip">
                                <Avatar src={currentUser.avatar} icon={<UserOutlined />} />
                                <span className="user-name" style={{ textAlign: 'center', marginTop: '26px' }}>{name}</span>
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
