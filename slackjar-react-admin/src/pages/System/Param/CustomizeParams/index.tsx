import React, {useEffect, useState} from 'react';
import {
    Button,
    Card,
    Form,
    Input,
    message,
    Modal,
    Popconfirm,
    Space,
    Table,
    Tag,
    type TableColumnsType, Tooltip
} from 'antd';
import {
    DeleteOutlined,
    EditOutlined,
    PlusOutlined,
    SearchOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import {
    deleteConfigById,
    pageQueryConfigs,
    saveConfigEntity,
    type PageResult,
    type SysConfigItemResponse,
    type SysConfigSaveRequest
} from "../../../../apis";
import globalStyles from '../../../global.module.scss';
import DictSelect from "../../../../components/DictSelect";

interface QueryFormValues {
    category?: string
    configKey?: string
    description?: string
    status?: number
}

/**
 * 兼容后端分页结果字段，统一转换为页面可消费的结构
 */
const normalizePageResult = (
    pageData: PageResult<SysConfigItemResponse>,
    fallbackPageNum: number,
    fallbackPageSize: number
) => {
    const currentPageNum = typeof pageData.pageNo === 'number'
        ? pageData.pageNo
        : fallbackPageNum;
    return {
        list: pageData.list || [],
        total: pageData.total || 0,
        pageNo: currentPageNum < 1 ? 1 : currentPageNum,
        pageSize: pageData.pageSize || fallbackPageSize
    };
};

/**
 * 系统参数管理页面
 */
const CustomizeParams: React.FC = () => {
    const [queryForm] = Form.useForm<QueryFormValues>();
    const [entityForm] = Form.useForm<SysConfigSaveRequest>();
    const [tableLoading, setTableLoading] = useState(false);
    const [saveEntityLoading, setSaveEntityLoading] = useState(false);
    const [tableData, setTableData] = useState<SysConfigItemResponse[]>([]);
    const [pageNo, setPageNum] = useState(1);
    const [pageSize, setPageSize] = useState(10);
    const [total, setTotal] = useState(0);
    const [isEntityModalOpen, setIsEntityModalOpen] = useState(false);
    const [editingItem, setEditingItem] = useState<SysConfigItemResponse | null>(null);

    /**
     * 获取状态展示信息
     */
    const getStatusMeta = (status?: number) => {
        return status === 1 ? {text: '禁用', color: 'red'} : {text: '启用', color: 'green'};
    };

    /**
     * 获取分页配置列表
     */
    const fetchConfigList = async (
        nextPageNum: number = pageNo,
        nextPageSize: number = pageSize,
        overrideQuery?: QueryFormValues
    ) => {
        try {
            setTableLoading(true);
            const queryValues = overrideQuery || queryForm.getFieldsValue();
            const result = await pageQueryConfigs({
                pageNo: nextPageNum,
                pageSize: nextPageSize,
                category: "customize_key",
                configKey: queryValues.configKey?.trim() || undefined,
                description: queryValues.description?.trim() || undefined,
                status: queryValues.status ?? undefined
            });

            if (result.code !== 200 || !result.data) {
                message.error(result.message || '获取配置列表失败');
                return;
            }

            const normalizedPageResult = normalizePageResult(result.data, nextPageNum, nextPageSize);

            setTableData(normalizedPageResult.list);
            setTotal(normalizedPageResult.total);
            setPageNum(normalizedPageResult.pageNo);
            setPageSize(normalizedPageResult.pageSize);
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : '获取配置列表失败';
            message.error(errorMessage);
        } finally {
            setTableLoading(false);
        }
    };

    /**
     * 处理分页检索
     */
    const handleSearch = async () => {
        await fetchConfigList(1, pageSize);
    };

    /**
     * 重置分页筛选条件
     */
    const handleResetSearch = async () => {
        queryForm.resetFields();
        await fetchConfigList(1, pageSize, {});
    };

    /**
     * 打开新建配置弹窗
     */
    const handleOpenCreateModal = () => {
        setEditingItem(null);
        entityForm.setFieldsValue({
            category: 'customize_key',  // 设置默认分类值
            configKey: undefined,
            configValue: undefined,
            description: undefined,
            status: '0'
        });
        setIsEntityModalOpen(true);
    };

    /**
     * 打开编辑配置弹窗
     */
    const handleOpenEditModal = (record: SysConfigItemResponse) => {
        setEditingItem(record);
        entityForm.setFieldsValue({
            id: record.id,
            category: record.category,
            configKey: record.configKey,
            configValue: record.configValue,
            description: record.description,
            status: String(record.status)
        });
        setIsEntityModalOpen(true);
    };

    /**
     * 关闭单项配置弹窗
     */
    const handleCloseEntityModal = () => {
        setIsEntityModalOpen(false);
        setEditingItem(null);
        entityForm.resetFields();
    };

    /**
     * 保存单项配置
     */
    const handleSaveEntity = async () => {
        try {
            const values = await entityForm.validateFields();
            setSaveEntityLoading(true);

            const payload: SysConfigSaveRequest = {
                id: values.id,
                category: values.category.trim(),
                configKey: values.configKey.trim(),
                configValue: values.configValue?.trim(),
                description: values.description?.trim(),
                status: values.status ?? '0'
            };

            const result = await saveConfigEntity(payload);
            if (result.code !== 200) {
                message.error(result.message || '保存失败');
                return;
            }

            message.success(editingItem ? '修改成功' : '新增成功');
            handleCloseEntityModal();
            await fetchConfigList(pageNo, pageSize);
        } catch (error) {
            const validationError = error as { errorFields?: unknown[] };
            if (validationError.errorFields) {
                return;
            }
            const errorMessage = error instanceof Error ? error.message : '保存失败';
            message.error(errorMessage);
        } finally {
            setSaveEntityLoading(false);
        }
    };

    /**
     * 删除配置
     */
    const handleDeleteEntity = async (record: SysConfigItemResponse) => {
        try {
            const result = await deleteConfigById(record.id);
            if (result.code !== 200) {
                message.error(result.message || '删除失败');
                return;
            }

            message.success('删除成功');
            await fetchConfigList(pageNo, pageSize);
        } catch (error) {
            const errorMessage = error instanceof Error ? error.message : '删除失败';
            message.error(errorMessage);
        }
    };

    /**
     * 初始化列表数据
     */
    useEffect(() => {
        fetchConfigList(1, pageSize).then();
    }, []);

    const tableColumns: TableColumnsType<SysConfigItemResponse> = [
        {
            title: '配置键',
            dataIndex: 'configKey',
            key: 'configKey',
            width: 120
        },
        {
            title: '配置值',
            dataIndex: 'configValue',
            key: 'configValue',
            width: 120,
            ellipsis: true,
            render: (value: string) => <Tooltip placement="topLeft" autoAdjustOverflow title={value}>{value}</Tooltip>
        },
        {
            title: '描述',
            dataIndex: 'description',
            key: 'description',
            width: 120,
            ellipsis: true,
            render: (value: string) => <Tooltip placement="topLeft" autoAdjustOverflow title={value}>{value}</Tooltip>
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 70,
            render: (status: number) => {
                const statusMeta = getStatusMeta(status);
                return <Tag color={statusMeta.color}>{statusMeta.text}</Tag>;
            }
        },
        {
            title: '更新时间',
            dataIndex: 'updateTime',
            key: 'updateTime',
            width: 120,
            render: (value: number) => value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-'
        },
        {
            title: '操作',
            key: 'actions',
            width: 120,
            align: 'center' as const,
            render: (_, record) => (
                <Space size="small">
                    <Button
                        type="link"
                        icon={<EditOutlined/>}
                        onClick={() => handleOpenEditModal(record)}
                    >
                        编辑
                    </Button>
                    <Popconfirm
                        title="确认删除该配置吗？"
                        okText="确认"
                        cancelText="取消"
                        onConfirm={() => handleDeleteEntity(record)}
                    >
                        <Button
                            danger
                            type="link"
                            icon={<DeleteOutlined/>}
                        >
                            删除
                        </Button>
                    </Popconfirm>
                </Space>
            )
        }
    ];

    return (
        <div className={globalStyles.pageContainer}>
            {/* 顶部搜索栏 */}
            <Card
                className={globalStyles.searchBar}
            >
                {/* 搜索栏 Form */}
                <Form form={queryForm} className={globalStyles.searchLeft}>
                    <Form.Item name="category" noStyle hidden>
                        <Input
                            placeholder="分类"
                            onPressEnter={handleSearch}
                            prefix={<SearchOutlined/>}
                            style={{width: 150}}
                            size="small"
                            allowClear
                        />
                    </Form.Item>
                    <Form.Item name="configKey" noStyle>
                        <Input
                            placeholder="配置键"
                            onPressEnter={handleSearch}
                            prefix={<SearchOutlined/>}
                            style={{width: 150}}
                            size="small"
                            allowClear
                        />
                    </Form.Item>
                    <Form.Item name="description" noStyle>
                        <Input
                            placeholder="描述"
                            onPressEnter={handleSearch}
                            prefix={<SearchOutlined/>}
                            style={{width: 150}}
                            size="small"
                            allowClear
                        />
                    </Form.Item>
                    <Form.Item name="status" noStyle>
                        <DictSelect
                            dictCode="ENABLE_STATUS"
                            placeholder="启用状态"
                            style={{width: 120}}
                            size="small"
                            allowClear
                            suffixIcon={<SearchOutlined/>}
                        />
                    </Form.Item>
                </Form>

                {/* 右侧：操作按钮 */}
                <div className={globalStyles.searchRight}>
                    <Button
                        type="link"
                        icon={<SearchOutlined/>}
                        onClick={handleSearch}
                        style={{padding: '4px 8px'}}
                    >
                        查询
                    </Button>
                    <Button
                        type="link"
                        icon={<SearchOutlined/>}
                        onClick={handleResetSearch}
                        style={{padding: '4px 8px'}}
                    >
                        重置
                    </Button>
                    <Button
                        type="link"
                        icon={<PlusOutlined/>}
                        onClick={handleOpenCreateModal}
                        style={{padding: '4px 8px'}}
                    >
                        新增配置
                    </Button>
                </div>
            </Card>

            {/* 底部表格 */}
            <Card
                className={globalStyles.tableCard}
                styles={{
                    body: {
                        padding: '0'
                    }
                }}
            >
                <Table
                    className={globalStyles.table}
                    rowKey="id"
                    columns={tableColumns}
                    dataSource={tableData}
                    loading={tableLoading}
                    scroll={{x: 800, y: 'calc(100vh - 380px)'}}
                    pagination={{
                        current: pageNo,
                        pageSize,
                        total,
                        showSizeChanger: true,
                        showTotal: (count) => `共 ${count} 条`
                    }}
                    onChange={(pagination) => {
                        fetchConfigList(pagination.current || 1, pagination.pageSize || pageSize).then();
                    }}
                />
            </Card>

            <Modal
                title={editingItem ? '编辑配置' : '新增配置'}
                open={isEntityModalOpen}
                onCancel={handleCloseEntityModal}
                onOk={handleSaveEntity}
                confirmLoading={saveEntityLoading}
                destroyOnHidden
            >
                <Form
                    form={entityForm}
                    layout="vertical"
                    initialValues={{status: 0}}
                    style={{marginTop: 16}}
                >
                    <Form.Item name="id" hidden>
                        <Input/>
                    </Form.Item>
                    <Form.Item
                        label="分类"
                        name="category"
                        rules={[{required: true, message: '请输入分类'}]}
                    >
                        <Input placeholder="请输入分类" disabled/>
                    </Form.Item>
                    <Form.Item
                        label="配置键"
                        name="configKey"
                        rules={[{required: true, message: '请输入配置键'}]}
                    >
                        <Input placeholder="请输入配置键"/>
                    </Form.Item>
                    <Form.Item
                        label="配置值"
                        name="configValue"
                    >
                        <Input.TextArea rows={3} placeholder="请输入配置值"/>
                    </Form.Item>
                    <Form.Item
                        label="描述"
                        name="description"
                    >
                        <Input placeholder="请输入描述"/>
                    </Form.Item>
                    <Form.Item
                        label="状态"
                        name="status"
                    >
                        <DictSelect
                            dictCode="ENABLE_STATUS"
                            placeholder="启用状态"
                            style={{width: '100%'}}
                        />
                    </Form.Item>
                </Form>
            </Modal>
        </div>
    );
};

export default CustomizeParams;
