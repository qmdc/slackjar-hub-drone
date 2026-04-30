-- ============================================
-- SlackJar 基础数据初始化脚本
-- 说明：创建默认管理员账号、角色和权限
-- ============================================

-- 1. 系统用户
insert into slackjar_drone.sys_user (id, username, password, nickname, email, phone, status, avatar_id, background_id, last_login_time, ip, create_time, update_time, deleted, version)
 values (1, 'slackjar', '$2a$10$Y4OGk9zgwowrdmYTrKvbvuoR/6l4bmL.krmdgw81lTVw6twnCUGai', '超级管理员', 'notre1024@163.com', '18726740474', 0, 1, 2, 1777429528622, '127.0.0.1', 1775136107000, 1777444915590, 0, 1);

-- 2. 角色管理
insert into slackjar_drone.role (id, role_name, role_code, description, role_type, status, sort_order, create_time, update_time, deleted, version) values (1, '超级管理员', 'ROLE_SUPER_ADMIN', '超级管理员拥有所有权限', 1, 0, 1, 1775136107000, 1777443502888, 0, 1);
insert into slackjar_drone.role (id, role_name, role_code, description, role_type, status, sort_order, create_time, update_time, deleted, version) values (2, '普通用户', 'ROLE_ORDINARY_USER', '用户注册后系统默认角色', 1, 0, 2, 1775136107000, 1777443446175, 0, 1);

-- 3. 用户角色关联
insert into slackjar_drone.user_role (id, user_id, role_id, create_time, update_time, deleted, version) values (1, 1, 1, 1777370607515, 1777370607515, 0, 1);

-- 4. 权限管理
-- 4.1 一级菜单（顶级权限）
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (1, '首页', '/index', '系统首页', 1, null, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (2, '系统管理', '/system', '系统管理', 1, null, 2, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (12, '其他', '/other', '其他', 1, null, 3, 1775136107000, 1775136107000, 0, 1);
-- 4.2 系统管理 - 二级菜单
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (3, '账户管理', '/system/account', '系统管理 - 账户管理', 1, 2, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (4, '配置管理', '/system/config', '系统管理 - 配置管理', 1, 2, 2, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (11, '系统文件', '/system/file', '系统管理 - 系统文件', 1, 2, 3, 1775136107000, 1775136107000, 0, 1);
-- 4.3 账户管理 - 三级菜单
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (5, '用户管理', '/system/account/user/manage', '系统管理 - 账户管理 - 用户管理', 1, 3, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (6, '角色管理', '/system/account/role/manage', '系统管理 - 账户管理 - 角色管理', 1, 3, 2, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (7, '权限管理', '/system/account/permission/manage', '系统管理 - 账户管理 - 权限管理', 1, 3, 3, 1775136107000, 1775136107000, 0, 1);
-- 4.4 配置管理 - 三级菜单
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (8, '系统参数', '/system/config/params', '系统管理 - 配置管理 - 系统参数', 1, 4, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (9, '数据字典', '/system/config/dict', '系统管理 - 配置管理 - 数据字典', 1, 4, 2, 1775136107000, 1775136107000, 0, 1);
-- 4.5 其他 - 三级菜单
insert into slackjar_drone.permission (id, permission_name, permission_code, description, permission_type, parent_id, sort_order, create_time, update_time, deleted, version) values (10, '个人中心', '/other/personal-center', '其他 - 个人中心', 1, 12, 1, 1775136107000, 1775136107000, 0, 1);

-- 5. 角色权限关联
-- 5.1 超级管理员角色 - 所有权限
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (1, 1, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (2, 1, 2, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (3, 1, 3, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (4, 1, 4, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (5, 1, 5, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (6, 1, 6, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (7, 1, 7, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (8, 1, 8, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (9, 1, 9, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (10, 1, 10, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (11, 1, 11, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (12, 1, 12, 1775136107000, 1775136107000, 0, 1);
-- 5.2 普通用户角色 - 部分权限
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (13, 2, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (14, 2, 10, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.role_permission (id, role_id, permission_id, create_time, update_time, deleted, version) values (15, 2, 12, 1775136107000, 1775136107000, 0, 1);

-- 6. 系统文件
insert into slackjar_drone.sys_file (id, file_name, user_id, file_path, thumbnail_path, file_size, file_type, file_md5, storage_type, biz_type, access_level, audit_status, download_count, upload_status, expired, create_time, update_time, deleted, version) values (1, '20260424031922468.png', 1, 'https://slackjar_drone.oss-cn-hangzhou.aliyuncs.com/service/avatar/20260424031922468.png', null, 1016113, 'avatar', 'c0910bb00c5deceff5c5f263eb8d86e8', 'aliyun', 'avatar', 1, 1, 0, 1, -1, 1776971962774, 1776971962774, 0, 1);
insert into slackjar_drone.sys_file (id, file_name, user_id, file_path, thumbnail_path, file_size, file_type, file_md5, storage_type, biz_type, access_level, audit_status, download_count, upload_status, expired, create_time, update_time, deleted, version) values (2, '20260429144152534.jpg', 1, 'https://slackjar_drone.oss-cn-hangzhou.aliyuncs.com/service/background/20260429144152534.jpg', null, 596085, 'background', '719427efa068efe77976e1d0c971ff5c', 'aliyun', 'background', 1, 1, 0, 1, -1, 1777444914075, 1777444914075, 0, 1);

-- 7. 系统配置
-- 7.1 服务器参数
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (1, 'server_ip', 'xxx', 'server_params', '连接服务器的 IP 地址', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (2, 'server_port', '22', 'server_params', 'SSH 连接端口号（默认为 22）', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (3, 'server_username', 'root', 'server_params', '登录服务器的用户名', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (4, 'server_password', 'xxx', 'server_params', '登录服务器的密码', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (5, 'ssl_cert_path', '/usr/local/nginx/ssl/slackjar', 'server_params', 'SSL 证书存放目录，如：/etc/ssl/certs/', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (6, 'domain_names', 'slackjar.online,blog.slackjar.online,core.slackjar.online,hub.slackjar.online', 'server_params', '解析到当前服务器的域名，多个域名换行输入，如：slackjar.online', 0, 1775136107000, 1775136107000, 0, 1);
-- 7.2 豆包 AI 配置
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (7, 'ai_doubao_model_id', 'doubao-seed-1-6-251015', 'ai_doubao_1_6_key', '豆包模型 ID', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (8, 'ai_doubao_model_url', 'https://ark.cn-beijing.volces.com/api/v3/responses', 'ai_doubao_1_6_key', '豆包模型路径', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (9, 'ai_doubao_completions_path', '/chat/completions', 'ai_doubao_1_6_key', '补全大模型调用路径，如：/v1/chat/completions', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (10, 'ai_doubao_api_key', 'xxx', 'ai_doubao_1_6_key', '豆包 API 访问密钥', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (11, 'ai_doubao_temperature', '0.7', 'ai_doubao_1_6_key', '控制生成文本的随机性，默认为 0.7，范围为 [0, 2]（确定 <-> 随机）', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (12, 'ai_doubao_max_tokens', '1024', 'ai_doubao_1_6_key', '控制生成文本的长度，默认为 1024，范围为 [1, 4096]', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (13, 'ai_doubao_window_size', '5', 'ai_doubao_1_6_key', '上下文窗口大小，默认为 5，范围为 [1, 20]', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (14, 'ai_doubao_deep_thinking', 'disabled', 'ai_doubao_1_6_key', '深度思考模式：自动、开启、关闭', 0, 1775136107000, 1775136107000, 0, 1);
-- 7.3 阿里云 OSS 配置
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (15, 'ali_oss_endpoint', 'oss-cn-hangzhou.aliyuncs.com', 'ali_oss_storage', '访问 OSS 的入口，允许通过互联网从任何地点访问 OSS，如：oss-cn-hangzhou.aliyuncs.com', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (16, 'ali_oss_region', 'cn-hangzhou', 'ali_oss_storage', 'OSS API 的入参、返回信息以及 OSS Endpoint 中使用的是 OSS 专用地域 ID，如：oss-cn-hangzhou', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (17, 'ali_oss_domain', 'https://slackjar_drone.oss-cn-hangzhou.aliyuncs.com', 'ali_oss_storage', '文件访问 URL 自定义，如：https://slackjar_drone.oss-cn-hangzhou.aliyuncs.com', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (18, 'ali_oss_file_name_rule', 'DATE', 'ali_oss_storage', '文件名规则：UUID（32 位无分隔符的 UUID 字符串）或 DATE（格式:yyyyMMddHHmmssSSS）', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (19, 'ali_oss_file_path', 'service', 'ali_oss_storage', '自定义文件上传根路径，如：service/image/', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (20, 'ali_oss_bucket', 'slackjar', 'ali_oss_storage', 'OSS 存储桶名称', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (21, 'ali_oss_access_key','xxx', 'ali_oss_storage', 'OSS 访问密钥 ID', 0, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (22, 'ali_oss_secret_key','xxx', 'ali_oss_storage', 'OSS 访问密钥', 0, 1775136107000, 1775136107000, 0, 1);
-- 7.4 系统参数
insert into slackjar_drone.sys_config (id, config_key, config_value, category, description, status, create_time, update_time, deleted, version) values (23, 'active_file_storage', 'aliyun', 'system_params', '选择当前使用的文件存储服务提供商', 0, 1775136107000, 1775136107000, 0, 1);

-- 8. 数据字典
-- 8.1 字典定义
insert into slackjar_drone.sys_dict (id, dict_name, dict_code, description, status, sort_order, create_time, update_time, deleted, version) values (1, '性别', 'GENDER', '', 0, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict (id, dict_name, dict_code, description, status, sort_order, create_time, update_time, deleted, version) values (2, '是否', 'YES_NO', '', 0, 2, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict (id, dict_name, dict_code, description, status, sort_order, create_time, update_time, deleted, version) values (3, '启用状态', 'ENABLE_STATUS', '', 0, 3, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict (id, dict_name, dict_code, description, status, sort_order, create_time, update_time, deleted, version) values (4, '删除状态', 'DELETED', '', 0, 4, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict (id, dict_name, dict_code, description, status, sort_order, create_time, update_time, deleted, version) values (5, '权限类型', 'PERM_TYPE', '', 0, 5, 1775136107000, 1775136107000, 0, 1);

-- 8.2 字典项
-- 权限类型字典项
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (1, 5, '菜单权限', '1', '', 0, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (2, 5, '按钮权限', '2', '', 0, 2, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (3, 5, '接口权限', '3', '', 0, 3, 1775136107000, 1775136107000, 0, 1);
-- 删除状态字典项
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (4, 4, '未删除', '0', '', 0, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (5, 4, '已删除', '1', '', 0, 2, 1775136107000, 1775136107000, 0, 1);
-- 启用状态字典项
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (6, 3, '启用', '0', '', 0, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (7, 3, '禁用', '1', '', 0, 2, 1775136107000, 1775136107000, 0, 1);
-- 是否字典项
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (8, 2, '是', '1', '', 0, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (9, 2, '否', '0', '', 0, 2, 1775136107000, 1775136107000, 0, 1);
-- 性别字典项
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (10, 1, '男性', '1', '', 0, 1, 1775136107000, 1775136107000, 0, 1);
insert into slackjar_drone.sys_dict_item (id, dict_id, item_label, item_value, description, status, sort_order, create_time, update_time, deleted, version) values (11, 1, '女性', '2', '', 0, 2, 1775136107000, 1775136107000, 0, 1);

-- ============================================
-- 初始化完成
-- 默认账号：slackjar
-- 默认密码：1234Abc666
-- ============================================
