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
    if (childMenuItems && childMenuItems.length > 0) {
        let menuItems = childMenuItems;
        let currentPath = pathname;

        // 逐级遍历菜单树，找到所有匹配的父级菜单
        while (menuItems && menuItems.length > 0) {
            let matched = false;
            for (let menuItem of menuItems) {
                const itemKey = menuItem!.key as string;
                // 判断当前路径是否匹配该菜单项或其子路径
                if (currentPath === itemKey || currentPath.startsWith(itemKey + "/")) {
                    selectMenuKeys.push(itemKey);
                    // 如果该菜单项有子菜单，继续向下匹配
                    if (menuItem.children && menuItem.children.length > 0) {
                        matched = true;
                        menuItems = menuItem.children;
                        break;
                    }
                }
            }
            // 如果没有匹配到或者没有子菜单，退出循环
            if (!matched) {
                break;
            }
        }

        // 如果 selectMenuKeys 为空，但存在子菜单，说明当前路径可能是父级目录
        // 需要找到第一个有 element 的叶子节点并展开其父级
        if (selectMenuKeys.length === 0) {
            // 尝试找到与当前路径最匹配的菜单项
            const findMatchingKeys = (items: MenuItem[], path: string, keys: string[]): string[] => {
                for (let item of items) {
                    const itemKey = item!.key as string;
                    if (path === itemKey || path.startsWith(itemKey + "/")) {
                        keys.push(itemKey);
                        if (item.children && item.children.length > 0) {
                            return findMatchingKeys(item.children, path, keys);
                        }
                        return keys;
                    }
                }
                return keys;
            };
            selectMenuKeys = findMatchingKeys(childMenuItems, currentPath, []);
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
                            selectedKeys={selectMenuKeys}  // 选中的菜单项（使用受控模式）
                            openKeys={selectMenuKeys}       // 展开的菜单项（使用受控模式）
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
