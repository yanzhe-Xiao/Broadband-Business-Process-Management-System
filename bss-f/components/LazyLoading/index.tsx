import { Spin } from 'antd';
import React from 'react'
import { LazyLoadingContainer } from './style';
export const LazyLoading: React.FC = () => (
    <LazyLoadingContainer>
        <Spin size='large' />
    </LazyLoadingContainer>
)