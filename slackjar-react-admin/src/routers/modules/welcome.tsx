import {DashboardOutlined} from "@ant-design/icons";
import {MenuRouteObject} from "../router";
import Welcome from "../../pages/Welcome";

const welcome: MenuRouteObject = {
    path: "index",
    label: "menu.home",
    icon: <DashboardOutlined/>,
    element: <Welcome/>
}

export default welcome;
