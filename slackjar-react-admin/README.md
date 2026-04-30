# SlackJar React Admin 🔥

## 特点

- 1、基于 TypeScript 🔥🔥
- 2、基于最新的 React 19 🔥🔥🔥
- 3、基于 Ant Design v6.x 设计风格 🔥🔥
- 4、基于 React Router v7.x 做路由管理，支持懒加载 🔥🔥
- 5、基于 Vite 7 做项目编译打包工具 🔥🔥
- 6、基于 Zustand 做状态管理 🔥🔥
- 7、基于 Axios 请求管理 👍
- 8、完善的 **国际化** 配置支持（react-i18next） 👍
- 9、完善的 **登录认证** 配置支持（RSA 加密） 👍
- 10、完善的 **权限管理 + 动态菜单** 配置支持 👍
- 11、完善的 Mock 数据支持（vite-plugin-mock-dev-server）
- 12、统一的页面加载组件（PageLoading）
- 13、友好的代码风格和注释...
- 14、更多小惊喜...


## 当前规划和进度

- ✅ 登录认证于退出（RSA 加密）
- ✅ 权限管理，当前用户的权限来源于服务器的配置
- ✅ 权限管理，自定义权限判断组件
- ✅ 统一的路由和菜单配置
- ✅ 中英文多语言切换（react-i18next）
- ✅ Axios 请求拦截，自动处理 JWT 和响应数据
- ✅ 高阶组件：DictSelect (能够根据服务器数据字典自动设置下拉数据)
- ✅ 页面加载组件：PageLoading（统一加载效果）
- ✅ 路由懒加载：lazyLoad（优化首屏加载）
- ⏳ 高阶组件：CrudPage（通过配置自动生成 增删改查 的组件）

## 开始使用

**Clone**

```shell
git clone https://gitee.com/notre/slack-hub.git
```

**Install**

```shell
cd slackjar-react-admin
npm install
```

**Run**

```shell
npm run dev
```

**Build**
```shell
# 开发环境
npm run build:dev

# 测试环境
npm run build:test

# 生产环境
npm run build:pro
```

## 目录结构

```
.
├── public                # 静态资源文件目录
├── src
│    ├── apis             # API 定义目录
│    │    ├── modules/    # API 模块（按功能划分）
│    │    ├── index.ts    # API 统一导出
│    │    └── request.ts  # Axios 请求配置
│    ├── assets           # 资源文件（图片、样式等）
│    ├── components       # 通用组件定义
│    │    ├── CheckLogin/     # 登录守卫组件
│    │    ├── CheckPerms/     # 权限判断组件
│    │    ├── DictSelect/     # 数据字典下拉组件
│    │    └── PageLoading/    # 统一页面加载组件
│    ├── hooks            # React 自定义 Hook
│    │    └── permissions.tsx  # 权限判断 Hook
│    ├── layout           # 布局文件以及布局涉及的组件
│    │    ├── Breadcrumbs/    # 面包屑导航
│    │    ├── MyHeader/       # 顶部导航栏
│    │    ├── MySider/        # 侧边栏菜单
│    │    └── Portal/         # 主布局入口
│    ├── locales          # 国际化语言定义
│    │    ├── translations/   # 多语言翻译文件
│    │    ├── i18n.ts         # i18n 配置
│    │    └── resources.ts    # 语言资源
│    ├── pages            # 页面文件夹
│    │    ├── Login/          # 登录页面
│    │    ├── Welcome/        # 首页
│    │    └── user/           # 用户管理模块
│    ├── routers          # 路由和菜单的定义
│    │    ├── modules/        # 路由模块配置
│    │    ├── front/          # 公开路由（登录页等）
│    │    ├── lazyLoad.tsx    # 路由懒加载封装
│    │    └── router.tsx      # 路由配置入口
│    ├── store            # Zustand store 定义
│    │    ├── appGlobalStore.ts # 全局状态（语言等）
│    │    ├── authStore.ts      # 认证状态
│    │    └── counterStore.ts   # 计数器示例
│    ├── utils            # 工具函数
│    │    └── rsaEncrypt.ts   # RSA 加密工具
│    ├── App.tsx          # React 运行入口文件
│    ├── main.tsx         # 入口文件
│    └── vite-env.d.ts    # Vite 声明文件
├── index.html            # 应用运行入口文件
├── LICENSE               # 授权文件（MIT）
├── package-lock.json     # 依赖包版本锁定文件
├── package.json          # NPM 管理
├── readme.md
├── tsconfig.json         # TypeScript 配置文件
├── tsconfig.node.json
├── vite.config.ts        # Vite 配置文件
└── .env                  # 环境变量配置
```

## 技术栈版本

- React: 19.2.5
- TypeScript: 5.9.3
- Ant Design: 6.3.6
- React Router: 7.14.1
- Vite: 7.1.0
- Zustand: 5.0.8
- Axios: 1.15.0
- react-i18next: 16.3.0
