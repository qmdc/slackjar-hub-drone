package com.slack.slackjarservice.common.constant;

/**
 * 系统配置项常量类
 * 用于统一管理所有配置项的 key 值
 * @author zhn
 */
public interface ConfigKeys {

    /**
     * 主服务器参数配置
     */
    interface ServerParams {
        // 连接服务器的 IP 地址
        String SERVER_IP = "server_ip";
        // SSH 连接端口号（默认为 22）
        String SERVER_PORT = "server_port";
        // 登录服务器的用户名
        String SERVER_USERNAME = "server_username";
        // 登录服务器的密码
        String SERVER_PASSWORD = "server_password";
        // SSL 证书存放目录
        String SSL_CERT_PATH = "ssl_cert_path";
        // 解析到当前服务器的域名，多个域名用英文逗号分隔
        String DOMAIN_NAMES = "domain_names";
    }

    /**
     * 系统参数配置
     */
    interface SystemParams {
        // 选择当前使用的文件存储服务提供商，默认选中阿里 OSS
        String ACTIVE_FILE_STORAGE = "active_file_storage";
    }

    /**
     * 阿里云 OSS 存储配置
     */
    interface AliOssStorage {
        // OSS 存储服务的访问地址
        String ALI_OSS_ENDPOINT = "ali_oss_endpoint";
        // OSS 访问密钥 ID
        String ALI_OSS_ACCESS_KEY = "ali_oss_access_key";
        // OSS 访问密钥
        String ALI_OSS_SECRET_KEY = "ali_oss_secret_key";
        // OSS 存储空间名称
        String ALI_OSS_BUCKET = "ali_oss_bucket";
        // OSS 存储地域（如 oss-cn-beijing）
        String ALI_OSS_REGION = "ali_oss_region";
        // 自定义域名，用于文件访问 URL 自定义
        String ALI_OSS_DOMAIN = "ali_oss_domain";
        // 上传回调 URL，用于处理上传完成后的回调通知
        String ALI_OSS_CALLBACK_URL = "ali_oss_callback_url";
        // 文件上传根目录，用于自定义文件上传路径
        String ALI_OSS_FILE_PATH = "ali_oss_file_path";
        // 文件名规则：UUID（唯一标识符）或 DATE（日期时间格式）
        String ALI_OSS_FILE_NAME_RULE = "ali_oss_file_name_rule";
    }

    /**
     * 豆包 AI 配置
     */
    interface AiDoubao {
        // 豆包 API 访问密钥
        String AI_DOUBAO_API_KEY = "ai_doubao_api_key";
        // 豆包模型路径
        String AI_DOUBAO_MODEL_URL = "ai_doubao_model_url";
        // 豆包模型 ID
        String AI_DOUBAO_MODEL_ID = "ai_doubao_model_id";
        // 温度参数，控制生成文本的随机性
        String AI_DOUBAO_TEMPERATURE = "ai_doubao_temperature";
        // 最大令牌数，控制生成文本的长度
        String AI_DOUBAO_MAX_TOKENS = "ai_doubao_max_tokens";
        // 上下文窗口长度
        String AI_DOUBAO_WINDOW_SIZE = "ai_doubao_window_size";
        // 地址标识，补全大模型调用路径
        String AI_DOUBAO_COMPLETIONS_PATH = "ai_doubao_completions_path";
        // 深度思考模式：自动、开启、关闭
        String AI_DOUBAO_DEEP_THINKING = "ai_doubao_deep_thinking";
    }
}
