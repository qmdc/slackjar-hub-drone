import React, {useCallback, useEffect, useState} from 'react';
import {Button, Form, Input, InputNumber, message} from 'antd';
import {SaveOutlined} from '@ant-design/icons';
import {getConfigByCategory, saveConfig} from '../../../../apis';
import type {ConfigItem, SysConfigRequest, SysConfigResponse} from '../../../../apis';
import styles from '../index.module.scss';
import globalStyles from '../../../global.module.scss';

const CATEGORY = 'server_params';

interface ConfigDisplayItem {
    configKey: string;
    configValue: string;
    description: string;
}

const configLabelMap: Record<string, string> = {
    'server_ip': '服务器IP',
    'server_port': 'SSH端口',
    'server_username': '用户名',
    'server_password': '密码',
    'ssl_cert_path': 'SSL证书存放目录',
    'domain_names': '域名配置',
};

const ServerParams: React.FC = () => {
    const [form] = Form.useForm();
    const [loading, setLoading] = useState(false);
    const [saveLoading, setSaveLoading] = useState(false);
    const [configItems, setConfigItems] = useState<ConfigDisplayItem[]>([]);

    const initConfigItems = useCallback(() => {
        const defaultItems: ConfigDisplayItem[] = [
            {configKey: 'server_ip', configValue: '', description: '连接服务器的IP地址'},
            {configKey: 'server_port', configValue: '', description: 'SSH连接端口号（默认为22）'},
            {configKey: 'server_username', configValue: '', description: '登录服务器的用户名'},
            {configKey: 'server_password', configValue: '', description: '登录服务器的密码'},
            {configKey: 'ssl_cert_path', configValue: '', description: 'SSL证书存放目录，如：/etc/ssl/certs/'},
            {
                configKey: 'domain_names',
                configValue: '',
                description: '解析到当前服务器的域名，多个域名换行输入，如：slackjar.online'
            },
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
                                let configValue = String(foundItem.configValue || '');
                                if (item.configKey === 'domain_names') {
                                    configValue = configValue.replace(/,/g, '\n');
                                }
                                return {
                                    ...item,
                                    configValue,
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
                        configValue: item.configKey === 'domain_names'
                            ? String(item.configValue).replace(/\n/g, ',').replace(/,+/g, ',').replace(/^,|,$/g, '')
                            : String(item.configValue),
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
                    return {...item, configValue: value};
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
        if (item.configKey === 'server_port') {
            return (
                <InputNumber
                    value={item.configValue ? Number(item.configValue) : undefined}
                    onChange={(value) => handleConfigChange(item.configKey, value)}
                    placeholder={`请输入${configLabelMap[item.configKey] || item.configKey}`}
                    min={1}
                    max={65535}
                    style={{width: '100%'}}
                />
            );
        }
        if (item.configKey === 'domain_names') {
            return (
                <Input.TextArea
                    value={item.configValue}
                    onChange={(e) => handleConfigChange(item.configKey, e.target.value)}
                    placeholder={`请输入${configLabelMap[item.configKey] || item.configKey}`}
                    rows={3}
                    autoSize={{minRows: 2, maxRows: 5}}
                />
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

export default ServerParams;
