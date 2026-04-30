import React, {useCallback, useEffect, useState} from 'react';
import {message} from 'antd';
import type {DictItem, DictItemRequest, DictItemResponse, SysDictPageQuery, SysDictRequest} from '../../../apis';
import {deleteDict, getDictByCode, getDictById, pageQueryDicts, saveDict} from '../../../apis';
import DictSearchBar from './DictSearchBar';
import DictList from './DictList';
import DictDetailForm from './DictDetailForm';
import DictItemTable from './DictItemTable';
import DictItemModal from './DictItemModal';
import styles from './index.module.scss';

/**
 * 字典管理页面
 * 提供数据字典的增删改查功能，包含字典基本信息管理和字典项管理
 *
 * @author zhn
 */
const Dict: React.FC = () => {
    // 顶部检索参数
    const [query, setQuery] = useState<SysDictPageQuery>({pageNo: 1, pageSize: 20});

    // 左侧字典列表数据
    const [dictList, setDictList] = useState<DictItem[]>([]);
    // 是否有更多数据
    const [hasMore, setHasMore] = useState(true);
    // 字典列表加载状态
    const [dictLoading, setDictLoading] = useState(false);
    // 当前选中的字典ID
    const [selectedDictId, setSelectedDictId] = useState<number | null>(null);

    // 右侧字典详情表单数据
    const [dictForm, setDictForm] = useState<SysDictRequest>({
        dictCode: '',
        dictName: '',
        description: '',
        status: 0,
        sortOrder: 0,
        dictItems: []
    });
    // 保存按钮加载状态
    const [saveLoading, setSaveLoading] = useState(false);
    // 字典项列表数据
    const [dictItemList, setDictItemList] = useState<DictItemResponse[]>([]);
    // 字典项表格加载状态
    const [itemTableLoading, setItemTableLoading] = useState(false);

    // 弹窗相关状态
    const [modalOpen, setModalOpen] = useState(false);
    // 弹窗标题
    const [modalTitle, setModalTitle] = useState('');
    // 当前编辑的字典项数据
    const [editingItem, setEditingItem] = useState<DictItemRequest | undefined>();
    // 当前编辑的字典项索引（-1表示新增）
    const [editingIndex, setEditingIndex] = useState<number>(-1);

    /**
     * 加载字典列表
     * 根据查询条件分页获取字典数据，滚动加载时追加数据
     * @param isLoadMore 是否为滚动加载更多
     */
    const loadDictList = useCallback((isLoadMore = false, overrideQuery?: SysDictPageQuery, shouldAutoSelect = true) => {
        const currentQuery = overrideQuery || query;
        setDictLoading(true);
        pageQueryDicts(currentQuery)
            .then((res) => {
                if (res.code === 200 && res.data) {
                    const records = (res.data.list || []) as DictItem[];
                    if (isLoadMore) {
                        // 滚动加载：追加数据
                        setDictList(prev => [...prev, ...records]);
                    } else {
                        // 首次加载或搜索：替换数据
                        setDictList(records);
                        // 自动选中第一个
                        if (shouldAutoSelect && records.length > 0) {
                            handleSelectDict(records[0].id);
                        }
                    }
                    // 判断是否还有更多数据
                    setHasMore(records.length >= (currentQuery.pageSize || 20));
                }
            })
            .finally(() => setDictLoading(false));
    }, [query]);

    // 钩子函数，进入页面首次加载
    useEffect(() => {
        loadDictList(false);
    }, []);

    /**
     * 滚动加载更多字典
     */
    const handleLoadMore = () => {
        if (!hasMore || dictLoading) return;
        // 页码加1，加载下一页
        setQuery(prev => ({...prev, pageNo: (prev.pageNo || 1) + 1}));
    };

    // 监听 query 变化，页码增加时加载下一页
    useEffect(() => {
        if ((query.pageNo || 1) > 1) {
            loadDictList(true);
        }
    }, [query]);

    /**
     * 选中字典
     * 加载字典详情和字典项列表
     * @param id 字典ID
     */
    const handleSelectDict = (id: number) => {
        setSelectedDictId(id);
        setDictItemList([]);
        setItemTableLoading(true);
        getDictById(id)
            .then((res) => {
                if (res.code === 200 && res.data) {
                    const data = res.data;
                    setDictForm({
                        id: data.id,
                        dictCode: data.dictCode,
                        dictName: data.dictName,
                        description: data.description,
                        status: data.status,
                        sortOrder: data.sortOrder,
                        dictItems: []
                    });
                    setDictItemList(data.dictItems || []);
                }
            })
            .finally(() => setItemTableLoading(false));
    };

    /**
     * 新增字典
     * 清空当前选中和表单，进入新增模式
     */
    const handleAddDict = () => {
        setSelectedDictId(null);
        setDictForm({
            dictCode: '',
            dictName: '',
            description: '',
            status: 0,
            sortOrder: 0,
            dictItems: []
        });
        setDictItemList([]);
    };

    /**
     * 删除字典
     * @param id 字典ID
     */
    const handleDeleteDict = (id: number) => {
        deleteDict(id).then((res) => {
            if (res.code === 200) {
                message.success('删除成功');
                // 如果删除的是当前选中的字典，清空右侧表单
                if (selectedDictId === id) {
                    handleAddDict();
                }
                // 重置页码并重新加载
                setQuery({pageNo: 1, pageSize: 20});
                setDictList([]);
                loadDictList(false);
            }
        });
    };

    /**
     * 保存字典
     * 将字典基本信息和字典项列表一并提交
     */
    const handleSaveDict = () => {
        if (!dictForm.dictCode || !dictForm.dictName) {
            message.warning('请填写字典编码和名称').then();
            return;
        }
        setSaveLoading(true);
        const request: SysDictRequest = {
            ...dictForm,
            dictItems: dictItemList.map((item) => ({
                id: item.id,
                itemValue: item.itemValue,
                itemLabel: item.itemLabel,
                sortOrder: item.sortOrder,
                status: item.status,
                description: item.description
            }))
        };
        saveDict(request)
            .then((res) => {
                if (res.code === 200) {
                    message.success('保存成功').then();
                    const isNewDict = !selectedDictId;
                    const savedDictCode = dictForm.dictCode;
                    setQuery({pageNo: 1, pageSize: 20});
                    setDictList([]);
                    loadDictList(false, undefined, false);
                    if (isNewDict && savedDictCode) {
                        getDictByCode(savedDictCode).then((findRes) => {
                            if (findRes.code === 200 && findRes.data) {
                                const savedDict = findRes.data;
                                setSelectedDictId(savedDict.id);
                                setDictForm((prev) => ({...prev, id: savedDict.id}));
                            }
                        });
                    }
                } else {
                    message.error(res.message).then();
                }
            })
            .finally(() => setSaveLoading(false));
    };

    /**
     * 删除字典项
     * @param index 字典项索引
     */
    const handleDeleteItem = (index: number) => {
        const newList = [...dictItemList];
        newList.splice(index, 1);
        setDictItemList(newList);
    };

    /**
     * 批量删除字典项
     * @param keys 选中的行key集合
     */
    const handleBatchDeleteItems = (keys: React.Key[]) => {
        const newList = dictItemList.filter((item) => !keys.includes(item.id));
        setDictItemList(newList);
    };

    /**
     * 打开新增字典项弹窗
     */
    const handleAddItem = () => {
        setEditingItem(undefined);
        setEditingIndex(-1);
        setModalTitle('新增值');
        setModalOpen(true);
    };

    /**
     * 打开编辑字典项弹窗
     * @param record 字典项数据
     * @param index 字典项索引
     */
    const handleEditItem = (record: DictItemResponse, index: number) => {
        setEditingItem({
            id: record.id,
            itemValue: record.itemValue,
            itemLabel: record.itemLabel,
            sortOrder: record.sortOrder,
            status: record.status,
            description: record.description
        });
        setEditingIndex(index);
        setModalTitle('编辑值');
        setModalOpen(true);
    };

    /**
     * 弹窗确认回调
     * 新增或编辑字典项
     * @param values 表单数据
     */
    const handleModalOk = (values: DictItemRequest) => {
        const newList = [...dictItemList];
        if (editingIndex >= 0) {
            // 编辑模式：更新指定索引的数据
            newList[editingIndex] = {...newList[editingIndex], ...values};
        } else {
            // 新增模式：添加到列表末尾
            newList.push({
                id: 0,
                dictId: dictForm.id || 0,
                itemValue: values.itemValue,
                itemLabel: values.itemLabel,
                sortOrder: values.sortOrder ?? 0,
                status: values.status ?? 0,
                description: values.description || ''
            } as unknown as DictItemResponse);
        }
        setDictItemList(newList);
        setModalOpen(false);
    };

    return (
        <div className={styles.dictPage}>
            <DictSearchBar
                query={query}
                onQueryChange={setQuery}
                onSearch={(overrideQuery) => {
                    const searchQuery = overrideQuery || {...query, pageNo: 1};
                    setQuery(searchQuery);
                    setDictList([]);
                    loadDictList(false, searchQuery);
                }}
                onAddDict={handleAddDict}
            />
            <div className={styles.dictMain}>
                <div className={styles.dictLeftPanel}>
                    <DictList
                        dataSource={dictList}
                        loading={dictLoading}
                        selectedId={selectedDictId}
                        hasMore={hasMore}
                        onSelect={handleSelectDict}
                        onLoadMore={handleLoadMore}
                        onDelete={handleDeleteDict}
                    />
                </div>
                <div className={styles.dictRightPanel}>
                    <DictDetailForm
                        form={dictForm}
                        isEdit={!!selectedDictId}
                        onChange={setDictForm}
                        onSave={handleSaveDict}
                        saveLoading={saveLoading}
                    />
                    <DictItemTable
                        dataSource={dictItemList}
                        loading={itemTableLoading}
                        onEdit={handleEditItem}
                        onDelete={handleDeleteItem}
                        onBatchDelete={handleBatchDeleteItems}
                        onAdd={handleAddItem}
                    />
                </div>
            </div>
            <DictItemModal
                open={modalOpen}
                title={modalTitle}
                initialValues={editingItem}
                onOk={handleModalOk}
                onCancel={() => setModalOpen(false)}
            />
        </div>
    );
};

export default Dict;
