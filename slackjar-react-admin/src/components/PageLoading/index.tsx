import React from 'react';
import {Spin} from 'antd';

interface PageLoadingProps {
    size?: 'small' | 'default' | 'large';
    minHeight?: string | number;
}

/**
 * 页面加载组件
 * 统一处理页面加载时的 Spin 显示效果
 *
 * @param size - Spin 尺寸，默认 large
 * @param minHeight - 最小高度，默认 100%
 */
const PageLoading: React.FC<PageLoadingProps> = ({size = 'large', minHeight = '100%'}) => {
    return (
        <div style={{
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            minHeight
        }}>
            <Spin size={size}/>
        </div>
    );
};

export default PageLoading;
