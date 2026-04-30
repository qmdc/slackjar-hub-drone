import {
    BookOutlined,
    CheckCircleOutlined,
    CloseCircleOutlined,
    ControlOutlined,
    ExperimentOutlined,
    FolderOpenOutlined,
    QuestionCircleOutlined,
    SafetyCertificateOutlined,
    SettingOutlined,
    SolutionOutlined,
    StopOutlined,
    TeamOutlined,
    UserOutlined,
} from "@ant-design/icons";
import lazyLoad from "../lazyLoad";
import React, {lazy} from "react";
import {MenuRouteObject} from "../router";
import Error403 from "../../pages/Error403";
import Error404 from "../../pages/Error404";
import ResultSuccess from "../../pages/ResultSuccess";
import ResultFail from "../../pages/ResultFail";

const systemAndAccount: MenuRouteObject = {
    path: "system",
    label: "menu.system management",
    icon: <SettingOutlined/>,
    children: [
        {
            path: "account",
            label: "menu.account management",
            icon: <TeamOutlined/>,
            children: [
                {
                    path: "user/manage",
                    label: "menu.user management",
                    icon: <UserOutlined/>,
                    element: lazyLoad(lazy(() => import("../../pages/Account/User")))
                },
                {
                    path: "role/manage",
                    label: "menu.role management",
                    icon: <SolutionOutlined/>,
                    element: lazyLoad(lazy(() => import("../../pages/Account/Role")))
                },
                {
                    path: "permission/manage",
                    label: "menu.permission management",
                    icon: <SafetyCertificateOutlined/>,
                    element: lazyLoad(lazy(() => import("../../pages/Account/Permission")))
                }
            ] as MenuRouteObject[]
        },
        {
            path: "config",
            label: "menu.config management",
            icon: <ControlOutlined/>,
            children: [
                {
                    path: "params",
                    label: "menu.system params",
                    icon: <ControlOutlined/>,
                    element: lazyLoad(lazy(() => import("../../pages/System/Param")))
                },
                {
                    path: "dict",
                    label: "menu.system dict",
                    icon: <BookOutlined/>,
                    element: lazyLoad(lazy(() => import("../../pages/System/Dict")))
                }
            ] as MenuRouteObject[]
        },
        {
            path: "file",
            label: "menu.system file",
            icon: <FolderOpenOutlined/>,
            hidden: true,
            element: lazyLoad(lazy(() => import("../../pages/System/File")))
        },
        {
            label: "menu.dev examples",
            path: "dev",
            icon: <ExperimentOutlined/>,
            hidden: true,
            children: [
                {
                    path: "success",
                    label: "menu.action success",
                    icon: <CheckCircleOutlined/>,
                    element: <ResultSuccess/>
                },
                {
                    path: "fail",
                    label: "menu.action fail",
                    icon: <CloseCircleOutlined/>,
                    element: <ResultFail/>
                },
                {
                    path: "403",
                    label: "menu.page 403",
                    icon: <StopOutlined/>,
                    element: <Error403/>
                },
                {
                    path: "404",
                    label: "menu.page 404",
                    icon: <QuestionCircleOutlined/>,
                    element: <Error404/>
                },

            ] as MenuRouteObject[]
        }
    ] as MenuRouteObject[]
}

export default systemAndAccount
