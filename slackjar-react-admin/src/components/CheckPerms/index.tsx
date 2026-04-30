import React from "react";
import {useLocation} from "react-router";
import {useHasPermission} from "../../hooks/permissions";
import Error403 from "../../pages/Error403";


const CheckPerms: React.FC<{ children: React.ReactNode }> = (props) => {
    const location = useLocation();
    
    // 直接使用 Hook 检查权限
    const hasPermission = useHasPermission(location.pathname);

    return (
        <>
            {hasPermission ? props.children : <Error403/>}
        </>
    )
}

export default CheckPerms