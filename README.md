## 项目介绍

SlackJar 是一个基于 Spring Boot 3 + React 19 的前后端分离管理系统，提供完善的权限认证、动态菜单、国际化、参数字典等企业级功能。

### 核心特性

- **权限管理**：基于 Sa-Token 实现登录认证、权限控制、动态菜单
- **实时通信**：集成 WebSocket（netty-socketio）实现实时消息推送
- **多数据库支持**：MySQL 主数据库 + Redis 缓存
- **安全防护**：RSA 加密登录、Sentinel 限流熔断、统一异常处理
- **文件存储**：支持阿里云 OSS，具备动态对象存储扩展能力
- **AI 集成**：支持火山引擎 AI 大模型，具备灵活的 AI 对接扩展能力
- **国际化**：基于 react-i18next 实现中英文切换
- **现代化技术栈**：React 19、TypeScript 5.9、Ant Design 6、Spring Boot 3.5


## 后端技术

| <div style="width: 150px;">技术/工具</div> | <div style="width: 100px;">版本</div> |  <div style="width: 200px;">功能介绍</div> |
| --- | --- | --- |
| JDK          | 17         | Java开发工具包，提供运行Java程序的基础环境        |
| SpringBoot   | 3.5.4      | 快速开发Spring应用的框架，简化配置与部署          |
| SaToken      | 1.44.0     | 轻量级Java权限认证框架，处理登录、权限控制        |
| MySQL        | 9.4.0      | 关系型数据库，存储结构化业务数据                  |
| Redis        | latest     | 内存数据库，用于缓存、分布式锁、会话存储          |
| WebSocket    | 2.0.3      | 基于netty-socketio实现实时双向通信                |
| mybatis-plus | 3.5.7      | MyBatis增强工具，简化数据库CRUD操作              |
| Hutool       | 5.8.24     | Java工具类库，提供字符串、日期等常用功能封装      |
| Lombok       | 1.18.30    | 简化Java代码，通过注解自动生成getter/setter等     |
| Sentinel     | 1.8.8      | 流量控制、熔断降级、系统负载保护                  |
| 阿里云OSS    | 3.17.4     | 对象存储服务，用于文件上传与管理                  |
| 火山引擎AI   | 0.2.29     | AI大模型集成，提供智能对话能力                    |
| fastjson2    | 2.0.54     | 高性能JSON解析库                                  |
| OkHttp       | 4.12.0     | HTTP客户端，用于外部API调用                       |

## 前端技术

| <div style="width: 150px;">技术/工具</div> | <div style="width: 100px;">版本</div> |  <div style="width: 200px;">功能介绍</div> |
| --- | --- | --- |
| Node                    | 22     | 提供JavaScript运行环境                  |
| React                   | 19.2.5 | 核心UI库，构建组件化交互式界面                  |
| TypeScript              | 5.9.3  | 强类型JS超集，提升代码可维护性和类型安全        |
| Vite                    | 7.1.0  | 前端构建工具，极速开发热更新                    |
| Zustand                 | 5.0.8  | 轻量级状态管理，简洁高效的React状态管理方案        |
| React Router            | 7.14.1 | 路由管理，实现单页应用页面跳转和守卫            |
| Axios                   | 1.15.0 | HTTP客户端，处理前后端数据交互                  |
| Ant Design              | 6.3.6  | 企业级UI设计语言和React组件库          |
| Sass                    | 1.90.0 | CSS预处理器，增强样式编写效率和可维护性          |
| react-i18next           | 16.3.0 | React国际化解决方案，支持多语言切换            |
| react-hook-form         | 7.62.0 | 高性能表单管理库，简化表单验证和状态管理          |
| @ant-design/icons       | 6.0.0  | Ant Design官方图标库            |
| JSEncrypt               | 3.5.4  | RSA加密库，用于登录密码加密传输            |
| @vitejs/plugin-react    | 5.1.0  | Vite的React插件，提供React快速刷新支持            |
| vite-plugin-mock        | 1.8.0  | Mock开发服务器，提供本地模拟数据            |
