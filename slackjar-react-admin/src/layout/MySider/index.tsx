import React from 'react';
import {Layout, Menu, theme} from 'antd';
import {MenuItem, useChildMenuItems} from "../../routers/router";
import {useLocation, useNavigate} from "react-router";


/**
 * 侧边栏菜单组件
 * 根据当前路由路径动态渲染子菜单，并处理菜单选中状态
 * @constructor
 */
const MySider: React.FC = () => {
    let {pathname} = useLocation();
    const {token: {colorBgContainer}} = theme.useToken();
    const navigate = useNavigate();

    // 根据当前路径获取子菜单项
    const childMenuItems: MenuItem[] | null = useChildMenuItems(pathname);

    // 计算当前应该选中的菜单项（支持多级菜单）
    let selectMenuKeys: string[] = [];
    if (childMenuItems) {
        let menuItems = childMenuItems;
        // 逐级遍历菜单树，找到所有匹配的父级菜单
        while (menuItems) {
            let needMatchChildren = false;
            for (let menuItem of menuItems) {
                // 判断当前路径是否匹配该菜单项
                if (pathname.startsWith(menuItem!.key + "/") || pathname === menuItem!.key) {
                    selectMenuKeys.push(menuItem!.key as string);
                    // 如果该菜单项有子菜单，继续向下匹配
                    if (menuItem.children) {
                        needMatchChildren = true;
                        menuItems = menuItem.children;
                        break;
                    }
                }
            }
            // 如果没有子菜单需要匹配，退出循环
            if (!needMatchChildren) {
                break;
            }
        }
    }

    /**
     * 处理菜单项点击事件
     * @param item - 被点击的菜单项，包含 key 属性
     */
    function handlerItemClick(item: { key: string }) {
        navigate(item.key);
    }

    return (
        <>
            {/* 仅当存在子菜单时才渲染侧边栏 */}
            {
                childMenuItems && childMenuItems.length > 0 ?
                    <Layout.Sider width={200} style={{background: colorBgContainer}}>
                        <Menu
                            mode="inline"
                            defaultSelectedKeys={selectMenuKeys}  // 默认选中的菜单项
                            defaultOpenKeys={selectMenuKeys}       // 默认展开的菜单项
                            style={{height: '100%', color: "#777"}}
                            items={childMenuItems}
                            onClick={handlerItemClick}
                        />
                    </Layout.Sider> : <div/>
            }
        </>

    );
};

export default MySider;
