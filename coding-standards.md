# SlackJar 项目编码规范
> 你无需编译启动代码！！！改代码即可，我自己使用其他IDE来启动代码！！！

## SlackJar 后端编码规范

### 技术栈

Spring Boot 3.5.4 + MyBatis-Plus + Sa-Token + Sentinel + Redis

### 目录结构

```
common/
├── config/          ## 配置类（XxxConfig.java）
├── constant/        ## 常量接口（XxxConstants.java）
├── enumtype/        ## 枚举类（XxxEnum.java）
├── exception/       ## 异常类
├── util/            ## 工具类（XxxUtil.java）
├── response/        ## 统一响应 ApiResponse
└── base/            ## 基类（BaseController、BaseModel）

foundation/
├── controller/      ## 控制器
├── service/         ## 服务接口
├── service/impl/    ## 服务实现
├── dao/             ## Mapper接口
├── entity/          ## 实体类（继承BaseModel）
└── model/
    ├── dto/         ## DTO对象
    ├── request/     ## 请求参数
    └── response/    ## 响应对象
```

### 编码规范

> 编程要符合阿里巴巴代码编程规范，以下是特殊要求

#### 注释

- 注释必须独立成行或成块，禁止行尾注释

#### 常量与枚举

- 常量放 `common/constant/`，使用接口定义
- HTTP状态码、业务错误码统一放 `ResponseEnum`
- 策略模式常量放对应 `XxxConstants`
- 分页查询DTO都需要继承：`BasePagination` 内置分页参数
- 分页查询返回使用：`PageResult`，最终包装在ApiResponse中

#### 实体类

- 继承 `BaseModel`（含createTime、updateTime、deleted、version）
- 使用 `@TableName` 指定表名
- 主键使用 `@TableId(type = IdType.AUTO)`
- 无需手动设置 createTime/updateTime，由 MyBatisMetaObjectHandler 自动填充

#### Controller

- 继承 `BaseController`
- 返回 `ApiResponse<T>` 包装
- 使用 `@Valid` 校验参数

#### Service

- 接口继承 `IService<Entity>`
- 实现类继承 `ServiceImpl<Dao, Entity>`
- 业务逻辑必须写在 Service 层，Controller 只做参数校验和调用

#### 异常处理

- 业务异常抛 `BusinessException(ResponseEnum.XXX)`
- 禁止直接抛 Exception

#### 命名规范

- 类名：大驼峰（XxxService、XxxController）
- 方法名：小驼峰（getUserById）
- 常量：全大写下划线（MAX\_SIZE）
- 包名：全小写

#### 工具方法

- 随机生成类方法放 `RandomUtil`
- 断言类方法放 `AssertUtil` 一些简单逻辑尽量少使用if判断，尽量使用断言，复杂表达式仍使用if
- Redis操作用 `RedisUtil`
- Stream API使用toList()替代collect(Collectors.toList())
- 对象判空使用Objects.isNull()/Objects.nonNull()方法
- 集合判空使用CollectionUtils.isEmpty()/CollectionUtils.isNotEmpty()方法

## SlackJar 前端编码规范

### 技术栈

React 19.2.5 + TypeScript 5.9.3 + Ant Design 6.3.6 + Vite 7.1.0 + Zustand 5.0.8 + React Router 7.14.1

### 目录结构

```
src/
├── apis/              ## API 接口层
│   ├── modules/       ## 按业务模块划分（auth.ts、dict.ts等）
│   ├── request.ts     ## axios 实例配置
│   └── index.ts       ## 统一导出
├── assets/            ## 静态资源（图片、图标等）
├── components/        ## 公共组件（首字母大写）
├── hooks/             ## 自定义 Hooks（useXxx.ts）
├── layout/            ## 布局组件（MyHeader、MySider等）
├── locales/           ## 多语言配置
│   ├── translations/  ## 语言包（zh.json、en.json）
│   └── i18n.ts        ## i18n 配置
├── pages/             ## 页面组件（按业务模块划分）
│   ├── Login/         ## 登录页
│   ├── Welcome/       ## 欢迎页
│   └── PersonalCenter/ ## 个人中心
│       └── AccountInfo/ ## 账号信息
├── routers/           ## 路由配置
│   ├── modules/       ## 按业务模块划分路由
│   ├── front/         ## 前端路由（登录等）
│   └── router.tsx     ## 路由总配置
├── store/             ## Zustand 状态管理
│   ├── authStore.ts   ## 认证状态
│   └── appGlobalStore.ts ## 全局状态
├── utils/             ## 工具函数
├── App.tsx            ## 根组件
└── main.tsx           ## 入口文件
```

### 编码规范

> 编程要符合 React 和 TypeScript 最佳实践，以下是特殊要求

#### 注释

- 组件必须添加 JSDoc 注释，说明组件用途
- 函数必须添加注释，说明参数和返回值
- 复杂逻辑必须添加注释说明
- 变量、组件内部的状态、属性等使用行注释，方法、组件使用块注释

#### 组件命名

- 组件文件名：大驼峰（AccountInfo、UserList）
- 组件目录名：大驼峰（AccountInfo、UserList）
- 组件导出：使用 `export default`
- 每个组件一个文件，文件名为组件名

#### 样式规范

- 使用 SCSS Modules（xxx.module.scss）
- 样式类名使用 kebab-case（header-logo、user-info）
- 避免使用全局样式，尽量使用 CSS Modules
- 覆盖第三方库样式时使用 `:global()` 选择器
- 强制覆盖使用 `!important`（谨慎使用）
- 全局复用的样式放在page目录下的最外层index.module.scss并写好模块注释，每个模块复用的放在该目录下，以此类推

#### API 接口规范

- API 函数和类型定义分离：函数在上，类型在下
- 使用 `// =====` 分隔不同类型分组
- 每个接口函数必须添加 JSDoc 注释
- 请求参数和响应数据必须定义 TypeScript 类型
- 使用 `request.ts` 统一的 axios 实例
- 错误处理在 axios 拦截器中统一处理

#### 路由配置规范

- 路由定义放在 `routers/modules/` 目录
- 使用 `MenuRouteObject` 类型定义路由
- `hidden: true` 表示隐藏在导航菜单中
- 路由路径使用 kebab-case（user-info、account-settings）
- 路由组件使用 `lazyLoad()` 懒加载
- 路由配置使用展开运算符 `{...welcome}` 避免循环引用

#### 状态管理规范

- 使用 Zustand 进行状态管理
- Store 文件命名：`xxxStore.ts`（authStore、appGlobalStore）
- 使用 `persist` 中间件持久化需要保存的状态
- 状态更新使用 `set()` 方法
- 使用 `useAuthStore((state) => state.xxx)` 选择器模式获取状态
- 避免在组件中直接修改 store，通过 action 函数修改

#### 权限控制规范

- 使用 `CheckLogin` 组件保护需要登录的路由
- 使用 `CheckPerms` 组件保护需要权限的路由
- 权限码使用常量定义（`PERMISSION_USER_VIEW`）
- 按钮级权限使用 `CheckPerms` 组件包裹
- 路由级权限在路由配置中定义 `permission` 字段

#### 多语言规范

- 翻译文件放在 `locales/translations/` 目录
- 使用 `useTranslation()` Hook 获取翻译函数
- 翻译键使用点号分隔（`menu.user.info`）
- 目前来说先给菜单维护多语言即可，使用`t()` 函数，并维护多语言文件

#### 命名规范

- 变量/函数：小驼峰（userInfo、getUserById）
- 常量：全大写下划线（MAX\_LOGIN\_COUNT）
- 组件：大驼峰（AccountInfo、UserList）
- 类型/接口：大驼峰（UserInfo、LoginParams）
- 文件名：大驼峰（AccountInfo.tsx）
- 样式类名：kebab-case（header-logo、user-info）

#### 最佳实践

- 使用函数组件 + Hooks，不使用类组件
- 优先使用 TypeScript 类型，避免 `any`
- 使用可选链 `?.` 和空值合并 `??` 替代复杂判断
- 列表渲染使用唯一 key，避免使用 index
- 使用 `React.FC` 定义组件类型
- 使用 `useCallback` 和 `useMemo` 优化性能
- 副作用使用 `useEffect`，并清理副作用
- 表单使用 Ant Design Form 组件，不要手动管理状态
- 表格使用 Ant Design Table 组件，配置 columns 数组

#### 工具函数

- 日期格式化使用 `dayjs`
- 密码加密使用 `jsencrypt`（RSA）
- 唯一 ID 生成使用 `nanoid`
- 请求封装统一在 `apis/request.ts`
- 公共工具函数放在 `utils/` 目录
- 封装的高阶组件放在 `components/` 目录，尤其是DictSelect字典组件
- 所有使用的Tooltip都应该加上autoAdjustOverflow placement="topLeft"属性

#### &#x20;特殊定制

- api/modules目录下的文件上方集中放请求，下方集中放ts定义的interface，如auth.ts文件的结构

