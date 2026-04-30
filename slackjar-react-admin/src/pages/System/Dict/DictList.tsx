import React, {useEffect, useRef} from 'react';
import {Card, Empty, Spin, Tag} from 'antd';
import type {DictItem} from '../../../apis';
import styles from './index.module.scss';
import globalStyles from '../../global.module.scss';

/**
 * 字典列表组件属性
 */
interface DictListProps {
    // 字典列表数据
    dataSource: DictItem[];
    // 加载状态
    loading: boolean;
    // 当前选中字典ID
    selectedId: number | null;
    // 是否有更多数据
    hasMore: boolean;
    // 选中字典回调
    onSelect: (id: number) => void;
    // 滚动加载更多回调
    onLoadMore: () => void;
    // 删除字典回调
    onDelete: (id: number) => void;
}

/**
 * 左侧字典列表组件
 * 以卡片形式展示字典列表，支持滚动加载、选中、删除
 *
 * @param dataSource 字典列表数据
 * @param loading 加载状态
 * @param selectedId 当前选中字典ID
 * @param hasMore 是否有更多数据
 * @param onSelect 选中字典回调
 * @param onLoadMore 滚动加载更多回调
 * @param onDelete 删除字典回调
 * @returns 字典列表组件
 * @author zhn
 */
const DictList: React.FC<DictListProps> = ({
                                               dataSource,
                                               loading,
                                               selectedId,
                                               hasMore,
                                               onSelect,
                                               onLoadMore,
                                               onDelete
                                           }) => {
    const listRef = useRef<HTMLDivElement>(null);

    /**
     * 监听滚动事件，滚动到底部时加载更多
     */
    useEffect(() => {
        const element = listRef.current;
        if (!element) return;

        const handleScroll = () => {
            const {scrollTop, scrollHeight, clientHeight} = element;
            // 距离底部 50px 时触发加载
            if (scrollHeight - scrollTop - clientHeight < 50 && hasMore && !loading) {
                onLoadMore();
            }
        };

        element.addEventListener('scroll', handleScroll);
        return () => element.removeEventListener('scroll', handleScroll);
    }, [hasMore, loading, onLoadMore]);
    return (
        <Card
            className={styles.dictListWrap}
            styles={{
                body: {
                    padding: '2px'
                }
            }}
        >
            <div className={styles.dictListHeader}>字典配置列表</div>
            <div ref={listRef} className={`${globalStyles.scrollbar} ${styles.dictListBody}`}>
                {dataSource.length === 0 && !loading ? (
                    <Empty description="暂无数据" image={Empty.PRESENTED_IMAGE_SIMPLE}/>
                ) : (
                    <>
                        {dataSource.map((item) => (
                            <div
                                key={item.id}
                                className={`${styles.dictCard} ${selectedId === item.id ? styles.dictCardActive : ''}`}
                                onClick={() => onSelect(item.id)}
                            >
                                <div className={styles.dictCardHeader}>
                                    <span className={styles.dictCardTitle}>{item.dictName}</span>
                                    <Tag
                                        color={item.status === 0 ? '#52c41a' : '#bfbfbf'}
                                        className={styles.dictCardStatus}
                                    >
                                        {item.status === 0 ? '启用' : '禁用'}
                                    </Tag>
                                </div>
                                <div className={styles.dictCardCode}>{item.dictCode}</div>
                            </div>
                        ))}
                        {loading && (
                            <div style={{textAlign: 'center', padding: '16px'}}>
                                <Spin size="small"/>
                            </div>
                        )}
                        {!hasMore && dataSource.length > 0 && (
                            <div style={{
                                textAlign: 'center',
                                padding: '12px',
                                color: '#bfbfbf',
                                fontSize: '12px'
                            }}>
                                已加载全部数据
                            </div>
                        )}
                    </>
                )}
            </div>
        </Card>
    );
};

export default DictList;
