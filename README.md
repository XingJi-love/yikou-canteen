# 🍜 一口食堂 (Yikou Canteen)

基于微信小程序的食堂/餐饮在线点餐系统，支持菜品浏览、购物车下单、订单管理等完整业务流程。

## 🏗 系统架构

```mermaid
flowchart LR
    subgraph Client["📱 客户端"]
        MP[微信小程序<br/>WXML / WXSS / JS]
        Admin[管理后台<br/>Vite + Vue 3]
    end

    subgraph Backend["🖥 后端服务"]
        API[Controller<br/>RESTful API]
        SVC[Service<br/>业务逻辑层]
        MAP[MyBatis Mapper<br/>数据持久化]
    end

    subgraph Data["💾 数据存储"]
        MySQL[(MySQL 8.0<br/>wxshop)]
        FS[(文件存储<br/>uploads/)]
    end

    MP -->|HTTP + Cookie| API
    Admin -->|HTTP + Token| API
    API -->|调用| SVC
    SVC -->|ORM| MAP
    MAP -->|JDBC| MySQL
    MAP -->|读写| FS

    style MP fill:#07C160,color:#fff
    style Admin fill:#42B883,color:#fff
    style Backend fill:#6DB33F,color:#fff
    style MySQL fill:#4479A1,color:#fff
```

## 🔄 业务流程

```mermaid
flowchart TD
    Start([启动小程序]) --> Login{微信授权}
    Login -->|成功| Home[首页]
    Login -->|失败| ReLogin[重新登录]
    ReLogin --> Login

    Home --> Browse[菜品列表]
    Home --> Banner[轮播推荐]

    Browse --> Detail[菜品详情]
    Detail --> Cart[加入购物车]

    Checkout[确认订单] --> Note[填写备注]
    Note --> Pay[提交订单]

    Pay --> Success{支付结果}
    Success -->|成功| OrderDetail[订单详情页]
    Success -->|失败| Retry[重试支付]
    Retry --> Pay

    OrderDetail --> OrderList[订单列表]
    OrderList --> Profile[个人中心]

    Profile --> Record[我的记录]
    Profile --> Logout[退出登录] --> Start

    Cart --> Checkout
    Browse --> Cart

    style Start fill:#07C160,color:#fff
    style Pay fill:#FF9C35,color:#fff
    style Success fill:#4CAF50,color:#fff
    style Logout fill:#f44336,color:#fff
```

## 🛠 技术栈

```mermaid
graph TB
    subgraph Frontend["📱 前端"]
        WXMP[微信小程序原生开发]
        WXML[WXML 模板]
        WXSS[WXSS 样式]
        JS[JavaScript 逻辑]
        WXMP --> WXML & WXSS & JS
    end

    subgraph Backend["⚙️ 后端"]
        SB[Spring Boot 3.3.5]
        JAVA[Java 17]
        MB[MyBatis ORM]
        PH[PageHelper 分页]
        JACK[Jackson JSON]
        SB --> JAVA & MB & PH & JACK
    end

    subgraph Admin["🎨 管理后台"]
        Vite[Vite 构建工具]
        Vue3[Vue 3 响应式]
        Pinia[Pinia 状态管理]
        Router[Vue Router 路由]
        Vite --> Vue3 & Pinia & Router
    end

    subgraph Database["🗄 数据层"]
        DB[(MySQL 8.0)]
        FileStore[(本地文件存储)]
    end

    Frontend -->|HTTP /api| Backend
    Admin -->|HTTP /admin| Backend
    Backend --> DB & FileStore

    style Frontend fill:#07C160,color:#fff
    style Backend fill:#6DB33F,color:#fff
    style Admin fill:#42B883,color:#fff
    style Database fill:#4479A1,color:#fff
```

## 📡 API 时序图

```mermaid
sequenceDiagram
    actor User as 👤 用户
    participant MP as 📱 小程序前端
    participant API as ⚙️ Spring Boot
    participant DB as 💾 MySQL

    Note over User,DB: === 登录鉴权 ===
    User->>MP: 打开小程序
    MP->>MP: wx.login() 获取 code
    MP->>API: POST /api/user/login {code}
    API->>API: 微信 code2Session → openid
    API->>DB: 查询/创建用户记录
    DB-->>API: 用户信息
    API-->>MP: 返回 Set-Cookie JSESSIONID
    MP->>MP: 存储 Cookie，完成登录

    Note over User,DB: === 浏览菜品 ===
    User->>MP: 点击分类
    MP->>API: GET /api/food/list?categoryId=xxx
    API->>DB: SELECT * FROM wxshop_food WHERE category_id = ?
    DB-->>API: 菜品数据列表
    API-->>MP: 返回菜品JSON
    MP->>User: 渲染菜品卡片

    Note over User,DB: === 下单流程 ===
    User->>MP: 确认购物车并提交
    MP->>API: POST /api/order/create {items[], remark}
    API->>DB: 开启事务
    API->>DB: INSERT INTO wxshop_order ...
    API->>DB: INSERT INTO wxshop_order_item ...
    API->>DB: 提交事务 COMMIT
    DB-->>API: 订单创建成功
    API-->>MP: 返回订单ID
    MP->>User: 跳转订单详情页

    Note over User,DB: === 查看记录 ===
    User->>MP: 进入「我的」页面
    MP->>API: GET /api/order/myOrders
    API->>DB: SELECT * FROM wxshop_order WHERE user_id = ?
    DB-->>API: 订单历史
    API-->>MP: 订单列表JSON
    MP->>User: 展示消费记录
```

## 📂 目录结构

```
mp-weixin/
├── README.md
├── frontend/                        # 微信小程序前端
│   ├── app.js / app.json / app.wxss  # 小程序入口与全局配置
│   ├── utils/
│   │   ├── config.js                # API 请求基础地址
│   │   ├── fetch.js                 # 网络请求封装 (Cookie 会话)
│   │   └── decodeCookie.js          # Cookie 解析工具
│   ├── pages/
│   │   ├── login/                   # 登录页
│   │   ├── index/                   # 首页
│   │   ├── list/                    # 菜品列表页
│   │   ├── order/
│   │   │   ├── checkout/            # 确认下单页
│   │   │   ├── detail/              # 订单详情页
│   │   │   └── list/                # 订单列表页
│   │   └── record/                  # 我的记录页
│   └── images/                      # 图标与静态资源
└── backend/
    └── shop-springboot/             # Spring Boot 后端
        ├── pom.xml                  # Maven 项目配置
        ├── src/main/java/fun/xingji/wxshop/
        │   ├── controller/          # 控制器层
        │   ├── service/             # 服务层
        │   ├── mapper/              # MyBatis Mapper 接口
        │   ├── entity/              # 实体类
        │   ├── config/              # 配置类
        │   └── util/                # 工具类
        └── admin-ui/                # Vite+Vue 管理后台
```

## 🚀 快速开始

### 1. 前端（微信小程序）

1. 下载并安装 [微信开发者工具](https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html)
2. 将 `frontend/` 目录导入开发者工具
3. 修改 `frontend/utils/config.js` 中的 `baseUrl` 为你的后端地址
4. 在开发者工具中填写你的小程序 AppID，即可预览运行

### 2. 后端（Spring Boot）

**环境要求：**
- JDK 17+
- Maven 3.6+
- MySQL 8.0+

**启动步骤：**

```bash
# 1. 进入后端目录
cd backend/shop-springboot

# 2. 复制并修改配置文件
cp application-example.yml application.yml
# 编辑 application.yml，填入你的 MySQL 密码和微信小程序 AppID/AppSecret

# 3. 创建数据库（MySQL 中执行）
# CREATE DATABASE wxshop DEFAULT CHARACTER SET utf8mb4;

# 4. 编译运行
mvn clean package -DskipTests
java -jar target/wxshop-1.0.0.jar
```

服务默认运行在 `http://localhost:8080`，API 路径前缀为 `/api`。

### 3. 管理后台（可选）

```bash
cd backend/shop-springboot/admin-ui
npm install
npm run dev
```

## ⚠️ 注意事项

1. **安全提醒**：`application.yml` 已加入 `.gitignore`，首次使用需将 `application-example.yml` 重命名为 `application.yml` 并填入真实的数据库密码、微信 AppID 和 AppSecret。
2. **小程序配置**：需在微信公众平台注册小程序，获取 AppID 和 AppSecret。
3. **上传目录**：`uploads/` 目录用于存放菜品图片，请确保该目录有写入权限。

## 📄 License

MIT License
