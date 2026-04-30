import React, {useCallback, useState} from 'react';
import {Button, Form, Input, message, Typography} from 'antd';
import {LockOutlined, MailOutlined, MobileOutlined} from '@ant-design/icons';
import {changeEmail, changePassword, changePhone} from '../../../apis';
import {useAuthStore} from '../../../store/authStore';
import styles from './index.module.scss';
import globalStyles from '../../global.module.scss';

const {Text} = Typography;

const AccountManagement: React.FC = () => {
    const [passwordForm] = Form.useForm();
    const [phoneForm] = Form.useForm();
    const [emailForm] = Form.useForm();

    const [passwordLoading, setPasswordLoading] = useState(false);
    const [phoneLoading, setPhoneLoading] = useState(false);
    const [emailLoading, setEmailLoading] = useState(false);

    const userInfo = useAuthStore((state) => state.userInfo);
    const setUserInfo = useAuthStore((state) => state.setUserInfo);

    const handlePasswordChange = useCallback(() => {
        passwordForm.validateFields()
            .then((values) => {
                setPasswordLoading(true);
                changePassword(values)
                    .then((res) => {
                        if (res.code === 200) {
                            message.success('密码修改成功');
                            passwordForm.resetFields();
                        } else {
                            message.error(res.message);
                        }
                    })
                    .finally(() => setPasswordLoading(false));
            });
    }, [passwordForm]);

    const handlePhoneChange = useCallback(() => {
        phoneForm.validateFields()
            .then((values) => {
                setPhoneLoading(true);
                changePhone(values.phone)
                    .then((res) => {
                        if (res.code === 200) {
                            message.success('手机号修改成功');
                            if (userInfo) {
                                setUserInfo({...userInfo, phone: values.phone});
                            }
                            // phoneForm.resetFields();
                        } else {
                            message.error(res.message);
                        }
                    })
                    .finally(() => setPhoneLoading(false));
            });
    }, [phoneForm, userInfo, setUserInfo]);

    const handleEmailChange = useCallback(() => {
        emailForm.validateFields()
            .then((values) => {
                setEmailLoading(true);
                changeEmail(values.email)
                    .then((res) => {
                        if (res.code === 200) {
                            message.success('邮箱修改成功');
                            if (userInfo) {
                                setUserInfo({...userInfo, email: values.email});
                            }
                            // emailForm.resetFields();
                        } else {
                            message.error(res.message);
                        }
                    })
                    .finally(() => setEmailLoading(false));
            });
    }, [emailForm, userInfo, setUserInfo]);

    return (
        <div className={`${globalStyles.scrollbar}`} style={{height: '100%', overflowY: 'auto', paddingBottom: 24}}>
            <div className={styles.accountContainer}>
                <div className={styles.section}>
                    <div className={styles.sectionHeader}>
                        <Text strong className={styles.sectionTitle}>修改密码</Text>
                    </div>
                    <Form
                        form={passwordForm}
                        layout="vertical"
                        className={styles.form}
                    >
                        <Form.Item
                            name="oldPassword"
                            label="原密码"
                            rules={[{required: true, message: '请输入原密码'}]}
                        >
                            <Input.Password prefix={<LockOutlined/>} placeholder="请输入原密码"/>
                        </Form.Item>
                        <Form.Item
                            name="newPassword"
                            label="新密码"
                            rules={[
                                {required: true, message: '请输入新密码'},
                                {min: 6, max: 32, message: '密码长度必须在6-32个字符之间'}
                            ]}
                        >
                            <Input.Password prefix={<LockOutlined/>} placeholder="请输入新密码"/>
                        </Form.Item>
                        <Form.Item
                            name="confirmPassword"
                            label="确认密码"
                            rules={[
                                {required: true, message: '请确认新密码'},
                                ({getFieldValue}) => ({
                                    validator(_, value) {
                                        if (!value || getFieldValue('newPassword') === value) {
                                            return Promise.resolve();
                                        }
                                        return Promise.reject(new Error('两次输入的密码不一致'));
                                    }
                                })
                            ]}
                        >
                            <Input.Password prefix={<LockOutlined/>} placeholder="请再次输入新密码"/>
                        </Form.Item>
                        <Form.Item>
                            <Button type="primary" loading={passwordLoading} onClick={handlePasswordChange}>
                                修改密码
                            </Button>
                        </Form.Item>
                    </Form>
                </div>

                <div className={styles.section}>
                    <div className={styles.sectionHeader}>
                        <Text strong className={styles.sectionTitle}>修改手机号</Text>
                    </div>
                    <Form
                        form={phoneForm}
                        layout="vertical"
                        className={styles.form}
                        initialValues={{phone: userInfo?.phone || ''}}
                    >
                        <Form.Item
                            name="phone"
                            label="手机号"
                            rules={[
                                {required: true, message: '请输入手机号'},
                                {pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号'}
                            ]}
                        >
                            <Input prefix={<MobileOutlined/>} placeholder="请输入新手机号"/>
                        </Form.Item>
                        <Form.Item>
                            <Button type="primary" loading={phoneLoading} onClick={handlePhoneChange}>
                                确认修改
                            </Button>
                        </Form.Item>
                    </Form>
                </div>

                <div className={styles.section}>
                    <div className={styles.sectionHeader}>
                        <Text strong className={styles.sectionTitle}>修改邮箱</Text>
                    </div>
                    <Form
                        form={emailForm}
                        layout="vertical"
                        className={styles.form}
                        initialValues={{email: userInfo?.email || ''}}
                    >
                        <Form.Item
                            name="email"
                            label="邮箱"
                            rules={[
                                {required: true, message: '请输入邮箱'},
                                {type: 'email', message: '请输入正确的邮箱地址'}
                            ]}
                        >
                            <Input prefix={<MailOutlined/>} placeholder="请输入新邮箱"/>
                        </Form.Item>
                        <Form.Item>
                            <Button type="primary" loading={emailLoading} onClick={handleEmailChange}>
                                确认修改
                            </Button>
                        </Form.Item>
                    </Form>
                </div>
            </div>
        </div>
    );
};

export default AccountManagement;
