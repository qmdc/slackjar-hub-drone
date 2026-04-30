import React, {useCallback, useEffect, useState} from 'react';
import {Button, Form, Input, InputNumber, Radio, message} from 'antd';
import {SaveOutlined} from '@ant-design/icons';
import {getConfigByCategory, saveConfig} from '../../../../apis';
import type {ConfigItem, SysConfigRequest, SysConfigResponse} from '../../../../apis';
import styles from '../index.module.scss';
import globalStyles from '../../../global.module.scss';

const CATEGORY = 'ai_doubao_1_6_key';

interface ConfigDisplayItem {
    configKey: string;
    configValue: string;
    description: string;
}

const configLabelMap: Record<string, string> = {
    'ai_doubao_api_key': 'API Key',
    'ai_doubao_model_url': '模型 URL',
    'ai_doubao_completions_path': '地址标识',
    'ai_doubao_model_id': '模型 ID',
    'ai_doubao_temperature': '模型温度',
    'ai_doubao_max_tokens': '最大令牌数',
    'ai_doubao_window_size': '上下文窗口大小',
    'ai_doubao_deep_thinking': '深度思考',
};

const AiDoubao1_6Params: React.FC = () => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [saveLoading, setSaveLoading] = useState(false);
    const [configItems, setConfigItems] = useState<ConfigDisplayItem[]>([]);

    const initConfigItems = useCallback(() => {
        const defaultItems: ConfigDisplayItem[] = [
            { configKey: 'ai_doubao_model_id', configValue: '', description: '豆包模型ID' },
            { configKey: 'ai_doubao_model_url', configValue: '', description: '豆包模型路径' },
            { configKey: 'ai_doubao_completions_path', configValue: '', description: '补全大模型调用路径，如：/v1/chat/completions' },
            { configKey: 'ai_doubao_api_key', configValue: '', description: '豆包API访问密钥' },
            { configKey: 'ai_doubao_temperature', configValue: '', description: '控制生成文本的随机性，默认为0.7，范围为[0, 2]（确定<->随机）' },
            { configKey: 'ai_doubao_max_tokens', configValue: '', description: '控制生成文本的长度，默认为1024，范围为[1, 4096]' },
            { configKey: 'ai_doubao_window_size', configValue: '', description: '上下文窗口大小，默认为5，范围为[1, 20]' },
            { configKey: 'ai_doubao_deep_thinking', configValue: 'auto', description: '深度思考模式：自动、开启、关闭' },
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
        if (item.configKey === 'ai_doubao_temperature') {
            return (
                <InputNumber
                    value={item.configValue ? Number(item.configValue) : undefined}
                    onChange={(value) => handleConfigChange(item.configKey, value)}
                    placeholder={`请输入${configLabelMap[item.configKey] || item.configKey}`}
                    min={0}
                    max={2}
                    precision={2}
                    style={{ width: '100%' }}
                />
            );
        }
        if (item.configKey === 'ai_doubao_max_tokens') {
            return (
                <InputNumber
                    value={item.configValue ? Number(item.configValue) : undefined}
                    onChange={(value) => handleConfigChange(item.configKey, value)}
                    placeholder={`请输入${configLabelMap[item.configKey] || item.configKey}`}
                    min={1}
                    max={4096}
                    style={{ width: '100%' }}
                />
            );
        }
        if (item.configKey === 'ai_doubao_window_size') {
            return (
                <InputNumber
                    value={item.configValue ? Number(item.configValue) : undefined}
                    onChange={(value) => handleConfigChange(item.configKey, value)}
                    placeholder={`请输入${configLabelMap[item.configKey] || item.configKey}`}
                    min={1}
                    max={20}
                    style={{ width: '100%' }}
                />
            );
        }
        if (item.configKey === 'ai_doubao_deep_thinking') {
            return (
                <Radio.Group
                    value={item.configValue}
                    onChange={(e) => handleConfigChange(item.configKey, e.target.value)}
                >
                    <Radio.Button value="auto">自动</Radio.Button>
                    <Radio.Button value="enabled">开启</Radio.Button>
                    <Radio.Button value="disabled">关闭</Radio.Button>
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

export default AiDoubao1_6Params;
