import React from "react";
import {MenuRouteObject} from "../router";
import Login from "../../pages/Login";
import Error403 from "../../pages/Error403";
import Error500 from "../../pages/Error500";

const front: MenuRouteObject[] = [
    {
        path: "/login",
        element: <Login/>,
    },
    {
        path: "/error/403",
        element: <Error403/>,
    },
    {
        path: "/error/500",
        element: <Error500/>,
    }
]

export default front
