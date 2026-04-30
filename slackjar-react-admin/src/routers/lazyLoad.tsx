import React, {Suspense} from "react";
import PageLoading from "../components/PageLoading";

/**
 * 路由懒加载封装函数
 * 结合 React.lazy 和 Suspense 实现组件按需加载，并提供 Loading 状态
 *
 * @param LazyComponent - React.lazy() 包装的懒加载组件
 * @returns 包含 Suspense 边界的 React 节点
 */
const lazyLoad = (LazyComponent: React.LazyExoticComponent<any>): React.ReactNode => {
    return (
        // Suspense 用于包裹懒加载组件，在组件加载完成前显示 fallback 内容
        <Suspense fallback={<PageLoading/>}>
            {/* 懒加载组件，首次访问时会触发代码下载 */}
            <><LazyComponent/></>
        </Suspense>
    );
};

export default lazyLoad;
