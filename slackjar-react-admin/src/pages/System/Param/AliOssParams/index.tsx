import React, {useCallback, useEffect, useState} from 'react';
import {Button, Form, Input, Radio, message} from 'antd';
import {SaveOutlined} from '@ant-design/icons';
import {getConfigByCategory, saveConfig} from '../../../../apis';
import type {ConfigItem, SysConfigRequest, SysConfigResponse} from '../../../../apis';
import styles from '../index.module.scss';
import globalStyles from '../../../global.module.scss';

const CATEGORY = 'ali_oss_storage';

interface ConfigDisplayItem {
    configKey: string;
    configValue: string;
    description: string;
}

const configLabelMap: Record<string, string> = {
    'ali_oss_endpoint': '外网 Endpoint',
    'ali_oss_region': 'OSS 专用地域 ID',
    'ali_oss_access_key': 'Access Key',
    'ali_oss_secret_key': 'Secret Key',
    'ali_oss_bucket': '存储桶名称',
    'ali_oss_domain': 'Bucket 域名',
    'ali_oss_callback_url': '回调 URL',
    'ali_oss_file_path': '文件上传根目录',
    'ali_oss_file_name_rule': '文件命名规则',
};

const AliOssParams: React.FC = () => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [saveLoading, setSaveLoading] = useState(false);
    const [configItems, setConfigItems] = useState<ConfigDisplayItem[]>([]);

    const initConfigItems = useCallback(() => {
        const defaultItems: ConfigDisplayItem[] = [
            { configKey: 'ali_oss_endpoint', configValue: '', description: '访问OSS的入口，允许通过互联网从任何地点访问OSS，如：oss-cn-hangzhou.aliyuncs.com' },
            { configKey: 'ali_oss_region', configValue: '', description: 'OSS API的入参、返回信息以及OSS Endpoint中使用的是OSS专用地域ID，如：oss-cn-hangzhou' },
            { configKey: 'ali_oss_domain', configValue: '', description: '文件访问URL自定义，如：https://slackjar.oss-cn-hangzhou.aliyuncs.com' },
            { configKey: 'ali_oss_file_name_rule', configValue: 'UUID', description: '文件名规则：UUID（32位无分隔符的UUID字符串）或DATE（格式:yyyyMMddHHmmssSSS）' },
            { configKey: 'ali_oss_file_path', configValue: '', description: '自定义文件上传根路径，如：service/image/' },
            { configKey: 'ali_oss_bucket', configValue: '', description: 'OSS存储桶名称' },
            { configKey: 'ali_oss_access_key', configValue: '', description: 'OSS访问密钥ID' },
            { configKey: 'ali_oss_secret_key', configValue: '', description: 'OSS访问密钥' },
            { configKey: 'ali_oss_callback_url', configValue: '', description: '上传回调URL，用于处理上传完成后的回调通知' },
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

    const renderConfigInput = (item: ConfigDisplayItem) => {
        if (item.configKey.includes('password') || item.configKey.includes('secret') || item.configKey.includes('api_key')) {
            return (
                <Input.Password
                    value={item.configValue}
                    onChange={(e) => handleConfigChange(item.configKey, e.target.value)}
                    placeholder={`请输入${configLabelMap[item.configKey] || item.configKey}`}
                />
            );
        }
        if (item.configKey === 'ali_oss_file_name_rule') {
            return (
                <Radio.Group
                    value={item.configValue}
                    onChange={(e) => handleConfigChange(item.configKey, e.target.value)}
                >
                    <Radio.Button value="UUID">UUID</Radio.Button>
                    <Radio.Button value="DATE">DATE</Radio.Button>
                </Radio.Group>
            );
        }
        return (
            <Input
                value={item.configValue}
                onChange={(e) => handleConfigChange(item.configKey, e.target.value)}
                placeholder={`请输入${configLabelMap[item.configKey] || item.configKey}`}
            />
        );
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
                                {renderConfigInput(item)}
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

export default AliOssParams;
