import React from "react";
import {useAuthStore} from "../../store/authStore";
import {Navigate} from "react-router";


const CheckLogin: React.FC<{ children: React.ReactNode }> = (props) => {
    // 精确订阅 isLogin 状态，只有 isLogin 变化时才重渲染
    const isLogin = useAuthStore((state) => state.isLogin);

    return (
        <>
            {isLogin ? props.children : <Navigate to={"/login"} replace={true}/>}
        </>
    )
}

export default CheckLogin