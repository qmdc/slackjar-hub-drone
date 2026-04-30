import React, {useEffect, useRef, useState} from 'react';
import {Layout, Menu, theme} from 'antd';
import {MenuItem, useChildMenuItems} from "../../routers/router";
import {useLocation, useNavigate} from "react-router";

const lastPathMap = new Map<string, string>();

export const getLastPathByTopRoute = (topRouteKey: string): string | null => {
    return lastPathMap.get(topRouteKey) || null;
};

const MySider: React.FC = () => {
    const {pathname} = useLocation();
    const {token: {colorBgContainer}} = theme.useToken();
    const navigate = useNavigate();

    const childMenuItems: MenuItem[] | null = useChildMenuItems(pathname);

    const topRouteKey = '/' + pathname.split('/').filter(Boolean)[0];

    const computeSelectKeys = (): string[] => {
        if (!childMenuItems || childMenuItems.length === 0) return [];
        let selectKeys: string[] = [];
        let menuItems = childMenuItems;
        let currentPath = pathname;

        while (menuItems && menuItems.length > 0) {
            let matched = false;
            for (let menuItem of menuItems) {
                const itemKey = menuItem!.key as string;
                if (currentPath === itemKey || currentPath.startsWith(itemKey + "/")) {
                    selectKeys.push(itemKey);
                    if (menuItem.children && menuItem.children.length > 0) {
                        matched = true;
                        menuItems = menuItem.children;
                        break;
                    }
                }
            }
            if (!matched) break;
        }

        if (selectKeys.length === 0) {
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
            selectKeys = findMatchingKeys(childMenuItems, currentPath, []);
        }
        return selectKeys;
    };

    const computeDefaultOpenKeys = (): string[] => {
        return computeSelectKeys().slice(0, -1);
    };

    const selectKeys = computeSelectKeys();

    const openKeysMap = useRef<Map<string, string[]>>(new Map());
    const prevTopRouteKeyRef = useRef<string>(topRouteKey);
    const [openKeys, setOpenKeys] = useState<string[]>(() => {
        lastPathMap.set(topRouteKey, pathname);
        const defaults = computeDefaultOpenKeys();
        openKeysMap.current.set(topRouteKey, defaults);
        return defaults;
    });

    useEffect(() => {
        lastPathMap.set(topRouteKey, pathname);

        if (prevTopRouteKeyRef.current !== topRouteKey) {
            prevTopRouteKeyRef.current = topRouteKey;
            const cached = openKeysMap.current.get(topRouteKey);
            if (cached) {
                setOpenKeys(cached);
            } else {
                const defaults = computeDefaultOpenKeys();
                setOpenKeys(defaults);
                openKeysMap.current.set(topRouteKey, defaults);
            }
        } else {
            const requiredOpenKeys = computeDefaultOpenKeys();
            setOpenKeys(prev => {
                const merged = new Set([...prev, ...requiredOpenKeys]);
                const result = Array.from(merged);
                openKeysMap.current.set(topRouteKey, result);
                return result;
            });
        }
    }, [pathname]);

    const handleOpenChange = (keys: string[]) => {
        setOpenKeys(keys);
        openKeysMap.current.set(topRouteKey, keys);
    };

    function handlerItemClick(item: { key: string }) {
        navigate(item.key);
    }

    return (
        <>
            {
                childMenuItems && childMenuItems.length > 0 ?
                    <Layout.Sider width={200} style={{background: colorBgContainer}}>
                        <Menu
                            mode="inline"
                            selectedKeys={selectKeys}
                            openKeys={openKeys}
                            onOpenChange={handleOpenChange}
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
