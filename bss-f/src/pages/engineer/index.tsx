import React, { Suspense, useState } from 'react'
import {
    AppstoreOutlined,
    UserOutlined,
    ShoppingCartOutlined,
    MenuFoldOutlined,
    MenuUnfoldOutlined,
    LogoutOutlined,
} from '@ant-design/icons'
import { Button, Layout, Menu, theme, Dropdown, Avatar, Space, message } from 'antd'
import { useNavigate } from 'react-router-dom'
import './engineer.css'
import { LazyLoading } from '../../../components'
import { useAuthStore } from '../../store/auth'
import EngineerDashboard from './workorder'
import ProfilePage from './profile'
const { Header, Sider, Content } = Layout

// 模拟当前用户信息
const currentUser = {
    name: '张三',
    avatar:
        'https://gw.alipayobjects.com/zos/antfincdn/XAosXuNZyF/BiazfanxmamNRoxxVxka.png',
}


// 定义菜单项类型
type MenuKey = 'workorders' | 'flowstep' | 'profile'

const Engineer: React.FC = () => {
    const [collapsed, setCollapsed] = useState(false)
    const [selectedKey, setSelectedKey] = useState<MenuKey>('profile')
    const [messageApi, contextHolder] = message.useMessage()
    const name = useAuthStore(s => s.username)
    const nav = useNavigate()
    const {
        token: { colorBgContainer, borderRadiusLG },
    } = theme.useToken()

    const logout = useAuthStore(s => s.logout)
    const menuItems = [
        {
            key: 'workorders',
            icon: <AppstoreOutlined />,
            label: '工单管理',
        },
        {
            key: 'profile',
            icon: <UserOutlined />,
            label: '基本信息',
        }
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
                        logout()                // 
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
            case 'profile':
                return (
                    <Suspense fallback={<LazyLoading />}>
                        <ProfilePage />
                    </Suspense>)
            case 'workorders':
                return (
                    <Suspense fallback={<LazyLoading />}>
                        <EngineerDashboard />
                    </Suspense>)
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

export default Engineer
