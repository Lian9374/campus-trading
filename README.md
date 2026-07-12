# 校园二手交易平台

校园二手商品买卖平台，基于 Vue 3 + Spring Boot 构建。

## 技术栈

| 层 | 技术 |
|---|---|
| 前端 | Vue 3 (Composition API) + Vite + Element Plus + Pinia |
| 后端 | Spring Boot 3 + Spring Security + JPA + JWT |
| 数据库 | MySQL 8 |

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Node.js 18+
- MySQL 8+

### 1. 创建数据库

```sql
CREATE DATABASE IF NOT EXISTS campus_trading DEFAULT CHARACTER SET utf8mb4;
```

修改 `backend/src/main/resources/application.yml` 中的数据库用户名和密码。

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`。启动后会自动创建表结构，并插入初始分类数据。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端运行在 `http://localhost:3000`，API 请求自动代理到后端。

## 项目结构

```
campus-trading/
├── backend/                         # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/campustrading/
│       │   ├── config/              # SecurityConfig, JWT, WebConfig
│       │   ├── common/              # Result, BusinessException, GlobalExceptionHandler
│       │   ├── entity/              # JPA 实体
│       │   ├── dto/                 # 请求/响应 DTO
│       │   ├── repository/          # JPA Repository
│       │   ├── service/             # 业务逻辑层
│       │   ├── controller/          # REST Controller
│       │   └── util/                # JwtUtil
│       └── resources/
│           ├── application.yml
│           └── data.sql             # 分类种子数据
├── frontend/                        # Vue 3 前端
│   └── src/
│       ├── api/                     # API 调用模块
│       ├── stores/                  # Pinia 状态管理
│       ├── router/                  # 路由 + 导航守卫
│       ├── components/              # 公共组件
│       └── views/                   # 页面组件
└── README.md
```

## MVP 功能

- 用户注册 / 登录（JWT 认证）
- 商品发布（图片上传 + 多图支持）
- 商品浏览（分页 + 关键词搜索 + 分类筛选 + 价格区间）
- 商品详情（浏览量统计）
- 下单购买
- 订单状态流转：待确认 → 已确认 → 已完成 / 已取消
- 商品收藏 / 取消收藏
- "我买到的" / "我卖出的"订单管理

## API 概览

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | /api/auth/register | 注册 |
| POST | /api/auth/login | 登录 |
| GET | /api/user/me | 获取当前用户信息 |
| PUT | /api/user/me | 修改个人信息 |
| GET | /api/products | 商品列表（分页+搜索+筛选） |
| GET | /api/products/:id | 商品详情 |
| POST | /api/products | 发布商品 |
| PUT | /api/products/:id | 编辑商品 |
| GET | /api/categories | 分类列表 |
| POST | /api/orders | 下单 |
| GET | /api/orders | 我的订单 |
| PUT | /api/orders/:id/confirm | 卖家确认 |
| PUT | /api/orders/:id/complete | 买家确认收货 |
| PUT | /api/orders/:id/cancel | 取消订单 |
| POST | /api/favorites | 添加收藏 |
| DELETE | /api/favorites/:productId | 取消收藏 |
| GET | /api/favorites | 我的收藏 |
| POST | /api/upload/image | 上传图片 |
