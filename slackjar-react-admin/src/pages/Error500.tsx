import React from 'react';
import error500 from "../assets/images/error_500.svg";


const Error500: React.FC = () => {
    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '60vh',
            padding: '40px 20px'
        }}>
            <img src={error500} style={{maxWidth: "40%", marginBottom: '24px'}} alt="404" />
            <h2 style={{
                fontSize: '24px',
                color: '#333',
                margin: '0 0 12px 0',
                fontWeight: 600
            }}>
                500 - 服务器错误
            </h2>
            <p style={{
                fontSize: '16px',
                color: '#666',
                margin: 0,
                textAlign: 'center'
            }}>
                抱歉，服务端出现异常
            </p>
        </div>
    );
};

export default Error500;
