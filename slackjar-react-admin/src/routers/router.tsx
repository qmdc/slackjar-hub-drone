import {createBrowserRouter, RouteObject} from "react-router";
import CheckLogin from "../components/CheckLogin";
import Error404 from "../pages/Error404";
import React from "react";
import {PieChartOutlined} from "@ant-design/icons";
import {MenuProps} from "antd";
import {useTranslation} from "react-i18next";
import Portal from "../layout/Portal";
import welcome from "./modules/welcome";
import system from "./modules/system";
import front from "./front/front";
import other from "./modules/other";
import detection from "./modules/detection";


/**
 * 扩展 RouteObject，增加菜单和图标属性
 * icon: 菜单图标
 * label: 菜单显示文本（用于国际化）
 * hidden: 是否在菜单中隐藏（隐藏后仍可通过路由访问）
 * children: 子路由/子菜单
 */
export declare type MenuRouteObject = {
    icon?: React.ReactNode;
    label?: string;
    hidden?: boolean;
    children?: MenuRouteObject[] | null;
} & RouteObject;

/**
 * 需要登录后才能访问的路由（后台管理路由）
 * 这些路由会被包裹在 CheckLogin 组件中，未登录会跳转到登录页
 */
const portalRouters: MenuRouteObject[] = [
    {...welcome},       // 首页/欢迎页
    {...detection},     // 目标检测模块
    {...system},        // 系统设置模块
    {...other},         // 其他模块
]

/**
 * 全部路由配置
 * routers[0]: 需要登录的主布局路由，包含侧边栏和头部
 * front: 不需要登录的路由（如登录页、注册页等）
 */
const routers: MenuRouteObject[] = [
    {
        path: "/",
        // CheckLogin 是登录守卫，未登录会重定向到 /login
        // Portal 是主布局组件（包含头部、侧边栏、内容区）
        element: <CheckLogin><Portal/></CheckLogin>,
        // 404 错误页面
        errorElement: <Error404/>,
        icon: <PieChartOutlined/>,
        // 子路由，对应 portalRouters 中定义的具体页面
        children: portalRouters,
    },
    // 展开不需要登录的路由（登录页等）
    ...front
];

/**
 * 菜单项类型，用于 antd Menu 组件
 */
export type MenuItem = Required<MenuProps>['items'][number] & { children?: MenuItem[] | null };

/**
 * 将路由配置转换为 antd Menu 组件所需的菜单数据结构
 * @param routers - 路由配置数组
 * @param key - 父级路径前缀
 * @param t - 国际化翻译函数
 * @param topMenuOnly - 是否只生成顶级菜单（用于顶部导航）
 */
const createMenuItems = (routers: MenuRouteObject[] | undefined, key: string, t: Function, topMenuOnly?: boolean): MenuItem[] => {
    return routers ? routers
        // 过滤掉隐藏的路由（hidden === true 的不生成菜单项）
        .filter(item => !item.hidden)
        .map(item => {
            return {
                // 菜单唯一标识 = 父路径 + 当前路径
                key: key + processPath(item.path),
                // 菜单图标
                icon: item.icon,
                // 递归生成子菜单（如果 topMenuOnly 为 true 则不生成子菜单）
                children: !topMenuOnly && item.children ? createMenuItems(item.children, key + processPath(item.path), t, false) : null,
                // 菜单显示文本，通过国际化翻译
                label: t(item.label || item.path),
            } as MenuItem;

        }) : [];
};

/**
 * 处理路由路径，确保格式正确
 * - 去掉末尾的斜杠
 * - 确保开头有斜杠
 */
const processPath = (path: string | undefined): string => {
    if (path) {
        if (path.lastIndexOf("/") == path.length - 1) path = path.substring(0, path.length - 1);
        if (path.indexOf("/") != 0) path = "/" + path;
    }
    return path ? path : "";
}

/**
 * 获取所有菜单项（用于侧边栏或顶部导航）
 * @param topMenuOnly - 是否只返回顶级菜单
 * @returns antd Menu 所需的菜单数据
 */
const useMenuItems = (topMenuOnly?: boolean): MenuItem[] => {
    const {t} = useTranslation();
    // routers[0].children 就是 portalRouters（需要登录的路由）
    return createMenuItems(routers[0].children, "", t, topMenuOnly);
}

/**
 * 根据当前路径获取子菜单项
 * 用于侧边栏显示当前顶级菜单下的子菜单
 * 如果当前路径匹配的是隐藏路由（hidden: true），则返回 null，隐藏侧边栏
 * @param pathname - 当前浏览器路径
 */
const useChildMenuItems = (pathname: string): MenuItem[] | null => {
    const {t} = useTranslation();

    pathname = processPath(pathname);

    // 找到当前路径匹配的顶级路由
    let matchedRouter;
    for (let router of routers[0].children!) {
        const searchString = processPath(router.path);
        if (searchString === pathname || pathname.startsWith(searchString + "/")) {
            matchedRouter = router;
            break
        }
    }

    // 如果匹配到的路由是隐藏的，则返回 null，隐藏侧边栏
    // if (matchedRouter && matchedRouter.hidden) {
    //     return null;
    // }

    // 返回匹配路由的子菜单
    return matchedRouter ? createMenuItems(matchedRouter.children, processPath(matchedRouter.path), t, false) : null;
}

/**
 * 递归查找第一个有 element 的叶子路由路径
 * @param router - 路由对象
 * @param parentPath - 父级路径前缀
 */
const findFirstLeafRoute = (router: MenuRouteObject, parentPath: string): string | null => {
    const currentPath = parentPath + processPath(router.path);
    // 如果当前路由有 element，说明是叶子路由，直接返回
    if (router.element) {
        return currentPath;
    }
    // 否则递归查找子路由
    if (router.children && router.children.length > 0) {
        for (const child of router.children) {
            const result = findFirstLeafRoute(child, currentPath);
            if (result) return result;
        }
    }
    return null;
}

/**
 * 获取父级路由下的第一个子路由路径（递归查找第一个有 element 的叶子路由）
 * 用于点击顶部菜单时自动跳转到第一个子页面
 * @param pathname - 父级路径
 */
const getFirstChildPathByParent = (pathname: string): string | null => {
    for (let router of routers[0].children!) {
        const searchString = processPath(router.path);
        if (searchString === pathname || pathname.startsWith(searchString + "/")) {
            if (router.children && router.children.length > 0) {
                // 递归查找第一个有 element 的叶子路由
                return findFirstLeafRoute(router.children[0], searchString);
            }
        }
    }
    return null;
}

/**
 * 生成面包屑导航数据
 * @param t - 国际化翻译函数
 * @param pathname - 当前浏览器路径
 * @returns 面包屑数组，如 ["首页", "用户管理", "用户列表"]
 */
const getBreadcrumbs = (t: Function, pathname: string): string[] => {
    let breadcrumbs: string[] = [];
    let tempRouters: MenuRouteObject[] = routers[0].children!;
    let matchedPath = "";
    // 逐级查找匹配的路由
    while (tempRouters) {
        let isMatchedRouters = false;
        for (let router of tempRouters) {
            const routerPath = matchedPath + processPath(router.path);
            if (routerPath === pathname || pathname.startsWith(routerPath + "/")) {
                breadcrumbs.push(t(router.label || router.path!))
                matchedPath = routerPath;
                isMatchedRouters = true;
                tempRouters = router.children as MenuRouteObject[]
                break
            }
        }
        if (!isMatchedRouters) {
            break;
        }
    }
    return breadcrumbs;
}

/**
 * 创建浏览器路由实例
 */
const router = createBrowserRouter(routers);

/**
 * 导出供其他组件使用的函数和路由实例
 * useMenuItems: 获取菜单数据（顶部导航用）
 * useChildMenuItems: 获取子菜单数据（侧边栏用）
 * getBreadcrumbs: 获取面包屑数据
 * getFirstChildPathByParent: 获取父级路由的第一个子路径
 * router: 路由实例，用于 RouterProvider
 */
export {useMenuItems, useChildMenuItems, getBreadcrumbs, getFirstChildPathByParent, router}