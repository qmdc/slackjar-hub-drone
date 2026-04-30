# SlackJar Service 🔥

## 特点

- 1、基于 Java 17 + Spring Boot 3.5.4 🔥🔥
- 2、基于 Sa-Token 实现完善的权限认证体系 🔥🔥
- 3、基于 MyBatis-Plus 简化数据库操作 🔥🔥
- 4、支持 MySQL + Redis 多数据源 🔥🔥
- 5、基于 WebSocket（netty-socketio）实现实时通信 🔥🔥
- 6、集成 Sentinel 限流熔断保护 👍
- 7、支持动态对象存储扩展（阿里云 OSS） 👍
- 8、支持动态AI 模型集成（火山引擎 AI） 👍
- 9、完善的异常处理和统一响应封装 👍
- 10、基于 Hutool、FastJSON2 等工具库提升开发效率
- 11、支持多环境配置（local、lan、frp）
- 12、友好的代码风格和注释...
- 13、更多小惊喜...

## 当前规划和进度

- ✅ 登录认证于退出（Sa-Token + Redis）
- ✅ 权限管理，当前用户的权限来源于服务器的配置
- ✅ 权限注解支持（@SaCheckPermission）
- ✅ 统一响应封装（ResponseData）
- ✅ 全局异常处理
- ✅ WebSocket 实时通信
- ✅ Sentinel 限流熔断
- ✅ 文件存储（阿里云 OSS + 动态扩展）
- ✅ AI 集成（火山引擎大模型 + 灵活扩展）
- ✅ 参数字典管理
- ✅ 多环境配置支持
- ⏳ 接口文档自动生成（SpringDoc/Swagger）

## 开始使用

**环境要求**

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

**Clone**

```shell
git clone https://gitee.com/notre/slack-hub.git
```

**配置数据库**

1. 创建数据库 `slackjar`
2. 执行 `sql/foundation.sql` 创建数据库结构
3. 执行 `sql/init-data.sql` 初始化数据
4. 修改 `src/main/resources/application-xxx.yml` 中的数据库连接信息

**默认路径**
http://127.0.0.1:8024/slack

## 服务管理

使用项目提供的服务启停脚本：

```shell
# 赋予执行权限
chmod +x backend.sh

# 启动服务
./backend.sh start

# 停止服务
./backend.sh stop

# 重启服务
./backend.sh restart

# 查看状态
./backend.sh status

# 清理日志
./backend.sh clean-log
```

## 目录结构

```
slackjar-service/
├── sql/                          # 数据库初始化脚本
│    ├── foundation.sql           # 基础表结构
│    └── init-data.sql            # 初始化数据
├── src/
│    ├── main/
│    │    ├── java/com/slack/slackjarservice/
│    │    │    ├── SlackjarServiceApplication.java  # 启动类
│    │    │    ├── common/         # 通用模块
│    │    │    │    ├── base/          # 基础类（BaseController、BaseEntity等）
│    │    │    │    ├── config/        # 配置类（Sa-Token、Redis、MyBatis等）
│    │    │    │    ├── constant/      # 常量定义
│    │    │    │    ├── enumtype/      # 枚举类型
│    │    │    │    ├── exception/     # 异常处理
│    │    │    │    ├── process/       # 处理流程
│    │    │    │    ├── response/      # 统一响应封装
│    │    │    │    └── util/          # 工具类
│    │    │    └── foundation/     # 基础业务模块
│    │    │         ├── aipolicy/        # AI 策略（火山引擎等）
│    │    │         ├── controller/      # 控制器层
│    │    │         ├── dao/             # 数据访问层
│    │    │         ├── entity/          # 实体类
│    │    │         ├── event/           # 事件定义
│    │    │         ├── filepolicy/      # 文件存储策略（OSS等）
│    │    │         ├── listener/        # 事件监听器
│    │    │         ├── model/           # 模型类（DTO、VO等）
│    │    │         ├── service/         # 服务层
│    │    │         ├── socketio/        # WebSocket 配置
│    │    │         └── util/            # 业务工具类
│    │    └── resources/
│    │         ├── keys/                 # 密钥文件（RSA等）
│    │         ├── mapper/foundation/    # MyBatis XML 映射文件
│    │         ├── application.yml       # 主配置文件
│    │         ├── application-local.yml # 本地环境配置
│    │         ├── application-lan.yml   # 局域网环境配置
│    │         ├── application-frp.yml   # FRP 内网穿透配置
│    │         └── logback-spring.xml    # 日志配置
│    └── test/                           # 测试代码
├── logs/                          # 日志目录
├── target/                        # 编译输出目录
├── pom.xml                        # Maven 配置
├── mvnw                           # Maven Wrapper
├── mvnw.cmd                       # Maven Wrapper (Windows)
└── README.md                      # 项目说明
```

## 核心模块说明

### common（通用模块）

| 包名 | 说明 |
|------|------|
| base | 基础类，包括 BaseController、BaseEntity、BaseService |
| config | 各类配置类：Sa-Token、Redis、MyBatis-Plus、WebSocket、Sentinel 等 |
| constant | 系统常量定义 |
| enumtype | 枚举类型定义 |
| exception | 自定义异常和全局异常处理器 |
| response | 统一响应结果封装（ResponseData） |
| util | 通用工具类 |

### foundation（基础业务模块）

| 包名 | 说明 |
|------|------|
| aipolicy | AI 策略实现，支持火山引擎等多种 AI 服务扩展 |
| controller | RESTful 接口控制器 |
| dao | MyBatis-Plus 数据访问接口 |
| entity | 数据库实体类 |
| event | 业务事件定义 |
| filepolicy | 文件存储策略，支持阿里云 OSS 等多种存储服务扩展 |
| listener | 事件监听器 |
| model | 数据传输对象（DTO）、视图对象（VO） |
| service | 业务逻辑服务层 |
| socketio | WebSocket 配置和消息处理 |

## 核心配置

### 服务配置

```yaml
server:
  port: 8024                # 服务端口
  servlet:
    context-path: /slack    # 上下文路径
```

### Sa-Token 配置

```yaml
sa-token:
  token-name: token         # token 名称
  timeout: 2592000          # token 有效期（30天）
  is-concurrent: true       # 允许多地同时登录
  is-share: false           # 不共享 token
  token-style: random-128   # token 风格
```

### 多环境配置

| 环境 | 配置文件 | 说明 |
|------|----------|------|
| local | application-local.yml | 本地开发环境 |
| lan | application-lan.yml | 局域网环境 |
| frp | application-frp.yml | FRP 内网穿透环境 |

切换环境：修改 `application.yml` 中的 `spring.profiles.active`

## 技术栈

| 技术/工具 | 版本 | 功能介绍 |
| --- | --- | --- |
| JDK | 17 | Java开发工具包 |
| Spring Boot | 3.5.4 | 快速开发框架 |
| Sa-Token | 1.44.0 | 权限认证框架 |
| MyBatis-Plus | 3.5.7 | ORM 增强工具 |
| MySQL Connector | 9.4.0 | MySQL 驱动 |
| Redis | latest | 缓存和会话存储 |
| WebSocket | 2.0.3 | 实时通信（netty-socketio） |
| Hutool | 5.8.24 | Java 工具类库 |
| Lombok | 1.18.30 | 代码简化工具 |
| Sentinel | 1.8.8 | 限流熔断 |
| 阿里云 OSS | 3.17.4 | 对象存储 |
| 火山引擎 AI | 0.2.29 | AI 大模型 |
| FastJSON2 | 2.0.54 | JSON 解析 |
| OkHttp | 4.12.0 | HTTP 客户端 |

## 开发规范

1. **包命名**：com.slack.slackjarservice.{模块}.{分层}
2. **类命名**：使用 PascalCase，如 `UserController`、`UserServiceImpl`
3. **方法命名**：使用 camelCase，如 `getUserById`、`saveUser`
4. **统一响应**：所有接口返回 `ResponseData<T>` 格式
5. **异常处理**：使用全局异常处理器，不直接在 Controller 中捕获异常
6. **日志规范**：使用 Slf4j，记录关键业务操作和异常信息
