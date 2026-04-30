import React, {useState} from 'react';
import {
    Avatar,
    Badge,
    Button,
    Card,
    Form,
    Input,
    message,
    Modal,
    Space,
    Table,
    Tag,
    Tooltip,
    Upload
} from 'antd';
import type {UploadProps} from 'antd';
import {
    UserOutlined,
    MailOutlined,
    PhoneOutlined,
    EnvironmentOutlined,
    ClockCircleOutlined,
    EditOutlined,
    SafetyOutlined,
    KeyOutlined,
    MenuOutlined,
    DesktopOutlined,
    ApiOutlined,
    PictureOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import {useAuthStore} from "../../../store/authStore";
import {getUserInfo, updateUserInfo, uploadFile} from "../../../apis";
import styles from './index.module.scss';
import globalStyles from '../../global.module.scss';

type UploadFileType = Parameters<NonNullable<UploadProps['beforeUpload']>>[0];

/**
 * 编辑表单字段类型
 */
interface EditFormValues {
    nickname?: string
}

/**
 * 个人中心账号信息页组件
 * 展示用户基本信息、角色列表和权限详情
 *
 * @author zhn
 */
const AccountInfo: React.FC = () => {
    const userInfo = useAuthStore((state) => state.userInfo);
    const roles = useAuthStore((state) => state.roles);
    const permissions = useAuthStore((state) => state.permissions);
    const setUserInfo = useAuthStore((state) => state.setUserInfo);

    /**
     * 用户状态映射
     */
    const statusMap: Record<number, { text: string; color: string }> = {
        0: {text: '正常', color: 'green'},
        1: {text: '禁用', color: 'red'},
    };

    /**
     * 角色类型映射
     */
    const roleTypeMap: Record<number, string> = {
        1: '系统角色',
        2: '自定义角色',
    };

    /**
     * 角色状态映射
     */
    const roleStatusMap: Record<number, { text: string; color: string }> = {
        0: {text: '启用', color: 'green'},
        1: {text: '禁用', color: 'red'},
    };

    /**
     * 权限类型映射
     */
    const permissionTypeMap: Record<number, { text: string; color: string; icon: React.ReactNode }> = {
        1: {text: '菜单', color: 'blue', icon: <MenuOutlined/>},
        2: {text: '按钮', color: 'green', icon: <DesktopOutlined/>},
        3: {text: '接口', color: 'orange', icon: <ApiOutlined/>},
    };

    // 权限弹窗状态
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [modalTitle, setModalTitle] = useState('');
    const [modalPermissions, setModalPermissions] = useState<any[]>([]);

    // 编辑资料弹窗状态
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [avatarFile, setAvatarFile] = useState<File | null>(null);
    const [backgroundFile, setBackgroundFile] = useState<File | null>(null);
    const [avatarPreviewUrl, setAvatarPreviewUrl] = useState<string>('');
    const [backgroundPreviewUrl, setBackgroundPreviewUrl] = useState<string>('');
    const [form] = Form.useForm<EditFormValues>();

    if (!userInfo) {
        return <div className={styles.accountInfoPage}>暂无用户信息</div>;
    }

    /**
     * 根据角色ID和角色编码获取权限列表
     * @param roleId 角色ID
     * @param roleCode 角色编码
     * @returns 权限列表
     */
    const getPermissionsByRole = (roleId: number, roleCode: string) => {
        let perms = permissions.filter(p => p.roleId === roleId.toString());
        if (perms.length === 0) {
            perms = permissions.filter(p => p.roleCode === roleCode);
        }
        if (perms.length === 0) {
            perms = permissions.filter(p => p.roleCode && p.roleCode.includes(roleCode));
        }
        return perms;
    };

    /**
     * 按权限类型过滤并排序
     * @param perms 权限列表
     * @param type 权限类型
     * @returns 过滤后的权限列表
     */
    const filterPermissionsByType = (perms: any[], type: number) => {
        return perms.filter(p => p.permissionType === type)
            .sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0));
    };

    /**
     * 打开权限详情弹窗
     * @param role 角色信息
     * @param type 权限类型
     */
    const handleShowPermissions = (role: any, type: number) => {
        const rolePerms = getPermissionsByRole(role.id, role.roleCode);
        const filteredPerms = filterPermissionsByType(rolePerms, type);
        setModalTitle(`${role.roleName} - ${permissionTypeMap[type].text}权限`);
        setModalPermissions(filteredPerms);
        setIsModalOpen(true);
    };

    /**
     * 打开编辑资料弹窗并初始化表单数据
     */
    const handleOpenEditModal = () => {
        form.setFieldsValue({nickname: userInfo.nickname});
        setAvatarFile(null);
        setBackgroundFile(null);
        setAvatarPreviewUrl(userInfo.avatarUrl || '');
        setBackgroundPreviewUrl(userInfo.backgroundUrl || '');
        setIsEditModalOpen(true);
    };

    /**
     * 关闭编辑资料弹窗并重置临时状态
     */
    const handleCancelEditModal = () => {
        setIsEditModalOpen(false);
        setAvatarFile(null);
        setBackgroundFile(null);
        setAvatarPreviewUrl('');
        setBackgroundPreviewUrl('');
        form.resetFields();
    };

    /**
     * 处理图片选择并生成本地预览
     * @param file 选择的文件
     * @param field 文件字段类型
     * @returns 阻止 antd 自动上传
     */
    const handleSelectImage = (file: UploadFileType, field: 'avatar' | 'background') => {
        const previewUrl = URL.createObjectURL(file);
        if (field === 'avatar') {
            setAvatarFile(file);
            setAvatarPreviewUrl(previewUrl);
        } else {
            setBackgroundFile(file);
            setBackgroundPreviewUrl(previewUrl);
        }
        return false;
    };

    /**
     * 上传用户图片并返回文件ID
     * @param file 待上传文件
     * @param bizType 业务类型
     * @returns 文件ID
     */
    const uploadUserImage = async (file: File, bizType: 'avatar' | 'background') => {
        const result = await uploadFile(file, bizType);
        if (result.code !== 200 || !result.data?.fileId) {
            throw new Error(result.message || '文件上传失败');
        }
        return result.data.fileId;
    };

    /**
     * 提交编辑资料表单
     */
    const handleEditSubmit = async () => {
        try {
            const values = await form.validateFields();
            setIsSubmitting(true);

            const payload = {
                nickname: values.nickname?.trim(),
                avatarId: userInfo.avatarId,
                backgroundId: userInfo.backgroundId,
            };

            if (avatarFile) {
                payload.avatarId = await uploadUserImage(avatarFile, 'avatar');
            }
            if (backgroundFile) {
                payload.backgroundId = await uploadUserImage(backgroundFile, 'background');
            }

            const result = await updateUserInfo(payload);
            if (result.code !== 200) {
                message.error(result.message || '修改失败');
                return;
            }

            const updatedUserInfo = await getUserInfo(userInfo.id);
            if (updatedUserInfo.code !== 200) {
                message.error(updatedUserInfo.message || '获取用户信息失败');
                return;
            }
            const data = updatedUserInfo.data || userInfo;
            setUserInfo({...data, city: data.city ?? userInfo.city});
            message.success('修改成功');
            handleCancelEditModal();
        } catch (error) {
            const validationError = error as { errorFields?: unknown[] };
            if (!validationError.errorFields) {
                const errorMessage = error instanceof Error ? error.message : '修改失败，请稍后重试';
                message.error(errorMessage);
            }
        } finally {
            setIsSubmitting(false);
        }
    };

    /**
     * 权限弹窗表格列定义
     */
    const permissionColumns = [
        {
            title: '权限名称',
            dataIndex: 'permissionName',
            key: 'permissionName',
            width: 150,
        },
        {
            title: '权限编码',
            dataIndex: 'permissionCode',
            key: 'permissionCode',
            width: 280,
            render: (text: string) => <Tag color="blue">{text}</Tag>,
        },
        {
            title: '描述',
            dataIndex: 'description',
            key: 'description',
            width: 200,
            ellipsis: true,
            render: (text: string) => (
                <Tooltip title={text || '暂无描述'} autoAdjustOverflow placement="top">
                    <span>{text || '暂无描述'}</span>
                </Tooltip>
            ),
        },
        {
            title: '排序',
            dataIndex: 'sortOrder',
            key: 'sortOrder',
            width: 80,
            align: 'center' as const,
        },
    ];

    /**
     * 角色表格列定义
     */
    const roleColumns = [
        {
            title: '角色编码',
            dataIndex: 'roleCode',
            key: 'roleCode',
            width: 200,
            render: (text: string) => <Tag color="blue">{text}</Tag>,
        },
        {
            title: '角色名称',
            dataIndex: 'roleName',
            key: 'roleName',
            width: 150,
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 80,
            align: 'center' as const,
            render: (status: number) => (
                <Tag color={roleStatusMap[status]?.color || 'default'}>
                    {roleStatusMap[status]?.text || '未知'}
                </Tag>
            ),
        },
        {
            title: '类型',
            dataIndex: 'roleType',
            key: 'roleType',
            width: 120,
            align: 'center' as const,
            render: (type: number) => roleTypeMap[type] || '未知',
        },
        {
            title: '描述',
            dataIndex: 'description',
            key: 'description',
            width: 200,
            ellipsis: true,
            render: (text: string) => (
                <Tooltip title={text || '暂无描述'} autoAdjustOverflow placement="top">
                    <span>{text || '暂无描述'}</span>
                </Tooltip>
            ),
        },
        {
            title: '权限操作',
            key: 'actions',
            width: 330,
            align: 'center' as const,
            render: (_: any, record: any) => {
                const rolePerms = getPermissionsByRole(record.id, record.roleCode);
                const menuCount = filterPermissionsByType(rolePerms, 1).length;
                const buttonCount = filterPermissionsByType(rolePerms, 2).length;
                const apiCount = filterPermissionsByType(rolePerms, 3).length;

                return (
                    <Space size="small">
                        <Button
                            size="small"
                            icon={<MenuOutlined/>}
                            onClick={() => handleShowPermissions(record, 1)}
                        >
                            菜单 ({menuCount})
                        </Button>
                        <Button
                            size="small"
                            icon={<DesktopOutlined/>}
                            onClick={() => handleShowPermissions(record, 2)}
                        >
                            按钮 ({buttonCount})
                        </Button>
                        <Button
                            size="small"
                            icon={<ApiOutlined/>}
                            onClick={() => handleShowPermissions(record, 3)}
                        >
                            接口 ({apiCount})
                        </Button>
                    </Space>
                );
            },
        },
    ];

    return (
        <div className={`${globalStyles.scrollbar}`} style={{height: '100%', overflowY: 'auto', paddingBottom: 24}}>
            <div className={styles.accountInfoPage}>
            {/* 个人信息卡片 */}
            <Card className={styles.infoCard}>
                <div className={styles.cardBody}>
                    <div className={styles.infoHeader}>
                        {/* 头像区域 */}
                        <div className={styles.avatarSection}>
                            <div className={styles.avatarWrapper}>
                                <Avatar
                                    className={styles.avatar}
                                    src={userInfo.avatarUrl}
                                    icon={!userInfo.avatarUrl ? <UserOutlined/> : undefined}
                                />
                                {/* {userInfo.status === 0 && <span className={styles.statusDot}/>} */}
                            </div>
                        </div>

                        {/* 用户信息区域 */}
                        <div className={styles.userInfoSection}>
                            <div className={styles.userName}>
                                {userInfo.nickname || userInfo.username}
                            </div>
                            <div className={styles.userTag}>@{userInfo.username}</div>

                            <div className={styles.userTags}>
                                <Tag icon={<SafetyOutlined/>} color="blue">
                                    ID: {userInfo.id}
                                </Tag>
                                <Tag color={statusMap[userInfo.status]?.color || 'default'}>
                                    {statusMap[userInfo.status]?.text || '未知'}
                                </Tag>
                            </div>

                            <div className={styles.infoActions}>
                                <Button
                                    type="primary"
                                    icon={<EditOutlined/>}
                                    onClick={handleOpenEditModal}
                                >
                                    编辑资料
                                </Button>
                            </div>
                        </div>
                    </div>

                    {/* 详细信息网格 */}
                    <div className={styles.detailGrid}>
                        <div className={styles.detailItem}>
                            <MailOutlined className={styles.detailIcon}/>
                            <span className={styles.detailLabel}>邮箱</span>
                            <span className={styles.detailValue}>{userInfo.email || '未设置'}</span>
                        </div>
                        <div className={styles.detailItem}>
                            <PhoneOutlined className={styles.detailIcon}/>
                            <span className={styles.detailLabel}>手机号</span>
                            <span className={styles.detailValue}>{userInfo.phone || '未设置'}</span>
                        </div>
                        <div className={styles.detailItem}>
                            <ClockCircleOutlined className={styles.detailIcon}/>
                            <span className={styles.detailLabel}>注册日期</span>
                            <span className={styles.detailValue}>
                                {dayjs(userInfo.createTime).format('YYYY-MM-DD HH:mm:ss')}
                            </span>
                        </div>
                        <div className={styles.detailItem}>
                            <EnvironmentOutlined className={styles.detailIcon}/>
                            <span className={styles.detailLabel}>IP属地</span>
                            <span className={styles.detailValue}>{userInfo.city || '-'}</span>
                        </div>
                        <div className={styles.detailItem}>
                            <EnvironmentOutlined className={styles.detailIcon}/>
                            <span className={styles.detailLabel}>最后登录IP</span>
                            <span className={styles.detailValue}>{userInfo.ip || '-'}</span>
                        </div>
                        <div className={styles.detailItem}>
                            <ClockCircleOutlined className={styles.detailIcon}/>
                            <span className={styles.detailLabel}>最后登录时间</span>
                            <span className={styles.detailValue}>
                                {userInfo.lastLoginTime
                                    ? dayjs(userInfo.lastLoginTime).format('YYYY-MM-DD HH:mm:ss')
                                    : '-'}
                            </span>
                        </div>
                    </div>
                </div>
            </Card>

            {/* 角色信息卡片 */}
            <Card className={styles.roleCard}>
                <div className={styles.cardHeader}>
                    <SafetyOutlined className={styles.cardTitleIcon}/>
                    <span>角色信息</span>
                </div>

                {roles.length > 0 ? (
                    <Table
                        className={styles.roleTable}
                        dataSource={roles}
                        columns={roleColumns}
                        rowKey="id"
                        pagination={false}
                        size="middle"
                        bordered={false}
                    />
                ) : (
                    <div style={{padding: '24px', textAlign: 'center', color: 'rgba(0, 0, 0, 0.45)'}}>
                        暂无角色信息
                    </div>
                )}
            </Card>

            {/* 权限详情弹窗 */}
            <Modal
                title={
                    <Space>
                        <KeyOutlined/>
                        <span>{modalTitle}</span>
                    </Space>
                }
                open={isModalOpen}
                onCancel={() => setIsModalOpen(false)}
                footer={null}
                width={800}
            >
                <div className={styles.permissionModal}>
                    <Table
                        dataSource={modalPermissions}
                        columns={permissionColumns}
                        rowKey="id"
                        pagination={{pageSize: 10}}
                        size="small"
                        locale={{emptyText: '暂无权限数据'}}
                        bordered={false}
                    />
                </div>
            </Modal>

            <Modal
                title="编辑资料"
                open={isEditModalOpen}
                onCancel={handleCancelEditModal}
                onOk={handleEditSubmit}
                confirmLoading={isSubmitting}
                width={520}
            >
                <Form form={form} layout="vertical">
                    <Form.Item label="昵称" name="nickname" rules={[{max: 50, message: '昵称长度不能超过50个字符'}]}>
                        <Input placeholder="请输入昵称"/>
                    </Form.Item>

                    <Form.Item label="头像与背景">
                        <div style={{display: 'flex', gap: 32}}>
                            <Upload
                                listType="picture-card"
                                showUploadList={false}
                                accept=".jpg,.jpeg,.png"
                                maxCount={1}
                                beforeUpload={(file) => handleSelectImage(file, 'avatar')}
                            >
                                {avatarPreviewUrl ? (
                                    <img src={avatarPreviewUrl} alt="avatar" style={{width: 100, height: 100, objectFit: 'cover'}}/>
                                ) : (
                                    <div style={{width: 100, height: 100}}>
                                        <UserOutlined style={{fontSize: 28}}/>
                                        <div style={{marginTop: 4}}>上传头像</div>
                                    </div>
                                )}
                            </Upload>

                            <Upload
                                listType="picture-card"
                                showUploadList={false}
                                accept=".jpg,.jpeg,.png,.webp"
                                maxCount={1}
                                beforeUpload={(file) => handleSelectImage(file, 'background')}
                            >
                                {backgroundPreviewUrl ? (
                                    <img src={backgroundPreviewUrl} alt="background" style={{width: 100, height: 100, objectFit: 'cover'}}/>
                                ) : (
                                    <div style={{width: 100, height: 100}}>
                                        <PictureOutlined style={{fontSize: 28}}/>
                                        <div style={{marginTop: 4}}>上传背景</div>
                                    </div>
                                )}
                            </Upload>
                        </div>
                    </Form.Item>
                </Form>
            </Modal>
            </div>
        </div>
    );
};

export default AccountInfo;
