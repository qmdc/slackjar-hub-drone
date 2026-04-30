import React, {useState} from 'react';
import {Button, Card, Empty, Popconfirm, Space, Table, Tag, Tooltip} from 'antd';
import {DeleteOutlined, EditOutlined, PlusOutlined} from '@ant-design/icons';
import type {DictItemResponse} from '../../../apis';
import globalStyles from '../../global.module.scss';
import styles from './index.module.scss';

/**
 * 字典项表格组件属性
 */
interface DictItemTableProps {
    // 字典项列表数据
    dataSource: DictItemResponse[];
    // 加载状态
    loading: boolean;
    // 编辑字典项回调
    onEdit: (record: DictItemResponse, index: number) => void;
    // 删除字典项回调
    onDelete: (index: number) => void;
    // 批量删除字典项回调
    onBatchDelete: (keys: React.Key[]) => void;
    // 新增字典项回调
    onAdd: () => void;
}

/**
 * 字典项表格组件
 * 展示字典项列表，支持分页、选中、编辑、删除、新增操作
 *
 * @param dataSource 字典项列表数据
 * @param loading 加载状态
 * @param onEdit 编辑字典项回调
 * @param onDelete 删除字典项回调
 * @param onAdd 新增字典项回调
 * @returns 字典项表格组件
 * @author zhn
 */
const DictItemTable: React.FC<DictItemTableProps> = ({
                                                         dataSource,
                                                         loading,
                                                         onEdit,
                                                         onDelete,
                                                         onBatchDelete,
                                                         onAdd
                                                     }) => {
    const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([]);

    const handleDeleteSelected = () => {
        onBatchDelete(selectedRowKeys);
        setSelectedRowKeys([]);
    };

    const columns = [
        {
            title: '值',
            dataIndex: 'itemValue',
            key: 'itemValue',
            width: 80
        },
        {
            title: '含义',
            dataIndex: 'itemLabel',
            key: 'itemLabel',
            width: 100
        },
        {
            title: '描述',
            dataIndex: 'description',
            key: 'description',
            width: 100,
            ellipsis: true,
            render: (value: string) => <Tooltip placement="left" autoAdjustOverflow title={value}>{value}</Tooltip>
        },
        {
            title: '排序',
            dataIndex: 'sortOrder',
            key: 'sortOrder',
            width: 60,
            align: 'center' as const
        },
        {
            title: '状态',
            dataIndex: 'status',
            key: 'status',
            width: 60,
            align: 'center' as const,
            render: (status: number) => (
                <Tag color={status === 0 ? 'success' : 'default'}>
                    {status === 0 ? '启用' : '禁用'}
                </Tag>
            )
        },
        {
            title: '操作',
            key: 'action',
            width: 120,
            align: 'center' as const,
            render: (_: any, record: DictItemResponse, index: number) => {
                return (
                    <Space>
                        <Button
                            type="link"
                            size="small"
                            icon={<EditOutlined/>}
                            onClick={() => onEdit(record, index)}
                        >
                            编辑
                        </Button>
                        <Popconfirm
                            title="确认删除"
                            description="确定删除该字典项吗？"
                            placement="left"
                            onConfirm={() => onDelete(index)}
                        >
                            <Button type="link" danger size="small" icon={<DeleteOutlined/>}>
                                删除
                            </Button>
                        </Popconfirm>
                    </Space>
                );
            }
        }
    ];

    const rowSelection = {
        selectedRowKeys,
        onChange: (keys: React.Key[]) => setSelectedRowKeys(keys)
    };

    return (
        <Card
            className={globalStyles.tableCard}
            styles={{
                body: {
                    padding: '2px'
                }
            }}
        >
            <div className={styles.itemTableWrap}>
                <div className={styles.itemTableHeader}>
                <span className={styles.itemTableTitle}>
                    独立值集
                </span>
                    <Space>
                        <Button
                            danger
                            size="small"
                            icon={<DeleteOutlined/>}
                            disabled={selectedRowKeys.length === 0}
                            onClick={handleDeleteSelected}
                        >
                            删除值
                        </Button>
                        <Button type="primary" size="small" icon={<PlusOutlined/>} onClick={onAdd}>
                            新增值
                        </Button>
                    </Space>
                </div>
                <Table
                    className={globalStyles.table}
                    rowKey="id"
                    rowSelection={rowSelection}
                    columns={columns}
                    dataSource={dataSource}
                    loading={loading}
                    pagination={false}
                    size="small"
                    locale={{emptyText: <Empty description="暂无数据"/>}}
                    scroll={{y: 'calc(100vh - 372px)'}}
                />
            </div>
        </Card>
    );
};

export default DictItemTable;
