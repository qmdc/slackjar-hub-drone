import React, {useCallback, useEffect, useState} from 'react';
import {Button, Form, Input, Radio, message} from 'antd';
import {SaveOutlined} from '@ant-design/icons';
import {getConfigByCategory, saveConfig} from '../../../../apis';
import type {ConfigItem, SysConfigRequest, SysConfigResponse} from '../../../../apis';
import styles from '../index.module.scss';
import globalStyles from '../../../global.module.scss';

const CATEGORY = 'system_params';

interface FormFields {
    active_file_storage: string;
}

interface ConfigDisplayItem {
    configKey: string;
    configValue: string;
    description: string;
}

const configLabelMap: Record<string, string> = {
    'active_file_storage': '当前生效OSS存储',
};

const SystemParams: React.FC = () => {
    const [form] = Form.useForm<FormFields>();
    const [loading, setLoading] = useState(false);
    const [saveLoading, setSaveLoading] = useState(false);
    const [configItems, setConfigItems] = useState<ConfigDisplayItem[]>([]);

    const initConfigItems = useCallback(() => {
        const defaultItems: ConfigDisplayItem[] = [
            { configKey: 'active_file_storage', configValue: 'aliyun', description: '选择当前使用的文件存储服务提供商' },
        ];
        setConfigItems(defaultItems);
    }, []);

    const loadConfig = useCallback(() => {
        setLoading(true);
        getConfigByCategory(CATEGORY)
            .then((res) => {
                if (res.code === 200 && res.data) {
                    const data: SysConfigResponse = res.data;
                    const configItemsFromServer = data.configItems || [];
                    setConfigItems(prevItems => {
                        return prevItems.map(item => {
                            const foundItem = configItemsFromServer.find((i: ConfigItem) => i.configKey === item.configKey);
                            if (foundItem) {
                                return {
                                    ...item,
                                    configValue: foundItem.configValue || '',
                                };
                            }
                            return item;
                        });
                    });
                }
            })
            .finally(() => setLoading(false));
    }, []);

    useEffect(() => {
        initConfigItems();
        loadConfig();
    }, [initConfigItems, loadConfig]);

    const handleSave = () => {
        form.validateFields()
            .then((values) => {
                setSaveLoading(true);
                const itemsToSave: ConfigItem[] = configItems
                    .filter(item => String(item.configValue).trim() !== '')
                    .map(item => ({
                        configKey: item.configKey,
                        configValue: String(item.configValue),
                        description: item.description,
                    }));
                const request: SysConfigRequest = {
                    category: CATEGORY,
                    configItems: itemsToSave,
                };
                saveConfig(request)
                    .then((res) => {
                        if (res.code === 200) {
                            message.success('配置保存成功');
                            loadConfig();
                        } else {
                            message.error(res.message);
                        }
                    })
                    .finally(() => setSaveLoading(false));
            });
    };

    const handleConfigChange = (configKey: string, value: any) => {
        setConfigItems(prevItems => {
            return prevItems.map(item => {
                if (item.configKey === configKey) {
                    return { ...item, configValue: value };
                }
                return item;
            });
        });
    };

    return (
        <div className={styles.appConfigContainer}>
            <div className={`${styles.configContent} ${globalStyles.scrollbar}`}>
                <Form
                    form={form}
                    layout="vertical"
                >
                    {configItems.map((item, index) => (
                        <div key={index} className={styles.configItem}>
                            <div className={styles.configName}>
                                <span>{configLabelMap[item.configKey] || item.configKey}</span>
                            </div>
                            <div className={styles.configValue}>
                                {item.configKey === 'active_file_storage' ? (
                                    <Radio.Group
                                        value={item.configValue}
                                        onChange={(e) => handleConfigChange(item.configKey, e.target.value)}
                                    >
                                        <Radio.Button value="aliyun">阿里OSS</Radio.Button>
                                        <Radio.Button value="tencent" disabled>腾讯云COS</Radio.Button>
                                        <Radio.Button value="minio" disabled>MinIO</Radio.Button>
                                        <Radio.Button value="local">本地上传</Radio.Button>
                                    </Radio.Group>
                                ) : (
                                    <Input
                                        value={item.configValue}
                                        onChange={(e) => handleConfigChange(item.configKey, e.target.value)}
                                        placeholder={`请输入${configLabelMap[item.configKey] || item.configKey}`}
                                    />
                                )}
                            </div>
                            <div className={styles.configDescription}>
                                <div className={styles.descriptionText}>{item.description}</div>
                            </div>
                        </div>
                    ))}
                </Form>
            </div>
            <div className={styles.configFooter}>
                <Button
                    type="primary"
                    icon={<SaveOutlined/>}
                    loading={saveLoading}
                    onClick={handleSave}
                >
                    保存
                </Button>
            </div>
        </div>
    );
};

export default SystemParams;
