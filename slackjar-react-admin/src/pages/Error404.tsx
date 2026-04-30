import React from 'react';
import error404 from "../assets/images/error_404.svg";


const Error404: React.FC = () => {
    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            minHeight: '60vh',
            padding: '40px 20px'
        }}>
            <img src={error404} style={{maxWidth: "40%", marginBottom: '24px'}} alt="404" />
            <h2 style={{ 
                fontSize: '24px', 
                color: '#333', 
                margin: '0 0 12px 0',
                fontWeight: 600
            }}>
                404 - 页面不存在
            </h2>
            <p style={{ 
                fontSize: '16px', 
                color: '#666',
                margin: 0,
                textAlign: 'center'
            }}>
                抱歉，您查看的页面不存在
            </p>
        </div>
    );
};

export default Error404;
