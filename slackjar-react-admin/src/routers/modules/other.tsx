import {AppstoreOutlined, IdcardOutlined} from "@ant-design/icons";
import lazyLoad from "../lazyLoad";
import React, {lazy} from "react";
import {MenuRouteObject} from "../router";

const other: MenuRouteObject = {
    path: "other",
    label: "menu.other",
    hidden: true,
    icon: <AppstoreOutlined/>,
    children: [
        {
            path: "personal-center",
            label: "menu.personal center",
            icon: <IdcardOutlined/>,
            element: lazyLoad(lazy(() => import("../../pages/PersonalCenter")))
        }
    ] as MenuRouteObject[]
}

export default other;
