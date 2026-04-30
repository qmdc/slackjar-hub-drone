import React from 'react';
import error403 from "../assets/images/error_403.svg";



const Error403: React.FC = () => {
    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '60vh',
            padding: '40px 20px'
        }}>
            <img src={error403} style={{maxWidth: "40%", marginBottom: '24px'}} alt="403" />
            <h2 style={{ 
                fontSize: '24px', 
                color: '#333', 
                margin: '0 0 12px 0',
                fontWeight: 600
            }}>
                403 - 禁止访问
            </h2>
            <p style={{ 
                fontSize: '16px', 
                color: '#666',
                margin: 0,
                textAlign: 'center'
            }}>
                抱歉，您没有权限查看当前页面
            </p>
        </div>
    );
};

export default Error403;
