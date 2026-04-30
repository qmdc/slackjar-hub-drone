import React, {useEffect, useState} from "react";
import {Button, Checkbox, Form, Input, message, Modal} from "antd";
import {LockOutlined, UserOutlined} from "@ant-design/icons";
import styles from "./login.module.scss";
import {useAuthStore} from "../../store/authStore";
import {useNavigate} from "react-router";
import {getUserInfo, getUserRoles, login, pushIpCityInfo} from "../../apis";
import {encryptPassword} from "../../utils/rsaEncrypt";
import {getRolePermissionsBatch} from "../../apis/modules/role";

const Login: React.FC = () => {

    const [isLoading, setIsLoading] = useState(false);
    const loginAction = useAuthStore((state) => state.login);
    const isLogin = useAuthStore((state) => state.isLogin);

    const [form] = Form.useForm();
    const navigate = useNavigate();
    const [messageApi, contextHolder] = message.useMessage();

    useEffect(() => {
        if (isLogin) {
            navigate("/index");
        }
    }, [isLogin]);

    const handlerSubmit = async (values: any) => {
        setIsLoading(true);
        try {
            // 使用 RSA 加密密码
            const encryptedPassword = encryptPassword(values.password);
            // 使用账号密码登录
            const loginResult = await login({username: values.username, encryptedPassword});
            if (loginResult.code === 200 && loginResult.data) {
                // 显示成功提示
                messageApi.success('登录成功');
                // 获取用户信息
                const userInfoResult = await getUserInfo(loginResult.data.loginId);
                if (userInfoResult.code === 200 && userInfoResult.data) {
                    // 获取用户角色
                    const rolesResult = await getUserRoles(userInfoResult.data.id);
                    if (rolesResult.code === 200 && rolesResult.data) {
                        // 获取角色权限
                        const permissionsResult = await getRolePermissionsBatch(rolesResult.data.map(item => item.id));
                        if (permissionsResult.code === 200 && permissionsResult.data) {
                            // 存储登录信息
                            loginAction(loginResult.data.tokenValue, userInfoResult.data, rolesResult.data, permissionsResult.data);
                            navigate("/index");
                            // 获取地区信息
                            pushIpCityInfo().then();
                        }
                    }
                }
            } else {
                messageApi.error(loginResult.message);
                form.resetFields(['password']);
            }
        } catch (error: any) {
            messageApi.error(error.response?.data?.message || "登录失败，请检查网络连接");
        } finally {
            setIsLoading(false);
        }
    };

    const showModal = () => {
        Modal.success({
            title: '提示',
            content: '待实现...',
        })
    }

    return (
        <div className={styles.container}>
            {contextHolder}

            <Form
                form={form}
                name="normal_login"
                className={styles.loginForm}
                initialValues={{
                    remember: true,
                    username: 'slackjar',
                    password: '1234Abc666'
                }}
                onFinish={handlerSubmit}
            >
                <h1 className={styles.title}>{import.meta.env.VITE_APP_TITLE}</h1>

                <Form.Item
                    name="username"
                    rules={[{required: true, message: '账号不能为空'}]}
                >
                    <Input prefix={<UserOutlined className="site-form-item-icon"/>}
                           placeholder="请输入账号"
                    />
                </Form.Item>

                <Form.Item
                    name="password"
                    rules={[{required: true, message: '密码不能为空'}]}
                >
                    <Input.Password
                        prefix={<LockOutlined className="site-form-item-icon"/>}
                        placeholder="请输入密码"
                    />
                </Form.Item>

                <Form.Item>
                    <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}>
                        <Form.Item name="remember" valuePropName="checked" noStyle style={{margin: 0}}>
                            <Checkbox disabled>记住我</Checkbox>
                        </Form.Item>

                        <div style={{display: 'flex', alignItems: 'center', gap: '1px'}}>
                            <Button type={"link"} onClick={showModal}>注册</Button>
                            <span style={{color: '#d9d9d9'}}>|</span>
                            <Button type={"link"} onClick={showModal}>忘记密码</Button>
                        </div>
                    </div>
                </Form.Item>

                <Form.Item>
                    <Button type="primary" htmlType="submit" className="login-form-button" block loading={isLoading}>
                        登 录
                    </Button>
                </Form.Item>
            </Form>
        </div>
    )
}

export default Login;
