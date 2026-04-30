import {
    IeOutlined,
    VideoCameraOutlined,
    MonitorOutlined,
    HistoryOutlined,
    DatabaseOutlined,
    BarChartOutlined,
} from "@ant-design/icons";
import lazyLoad from "../lazyLoad";
import React, {lazy} from "react";
import {MenuRouteObject} from "../router";

const detection: MenuRouteObject = {
    path: "detection",
    label: "menu.detection",
    icon: <MonitorOutlined/>,
    children: [
        {
            path: "image",
            label: "menu.image detection",
            icon: <IeOutlined/>,
            element: lazyLoad(lazy(() => import("../../pages/Detection/ImageDetection")))
        },
        {
            path: "video",
            label: "menu.video detection",
            icon: <VideoCameraOutlined/>,
            element: lazyLoad(lazy(() => import("../../pages/Detection/VideoDetection")))
        },
        {
            path: "multi-video",
            label: "menu.multi video detection",
            icon: <MonitorOutlined/>,
            element: lazyLoad(lazy(() => import("../../pages/Detection/MultiVideoDetection")))
        },
        {
            path: "history",
            label: "menu.detection history",
            icon: <HistoryOutlined/>,
            element: lazyLoad(lazy(() => import("../../pages/Detection/DetectionHistory")))
        },
        {
            path: "models",
            label: "menu.model management",
            icon: <DatabaseOutlined/>,
            element: lazyLoad(lazy(() => import("../../pages/Detection/ModelManagement")))
        },
        {
            path: "metrics",
            label: "menu.metrics",
            icon: <BarChartOutlined/>,
            element: lazyLoad(lazy(() => import("../../pages/Detection/Metrics")))
        },
    ] as MenuRouteObject[]
};

export default detection;