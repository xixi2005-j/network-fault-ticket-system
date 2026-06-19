# 网络故障工单系统

> 《互联网应用开发实践》课程设计项目

面向网络工程领域的故障工单管理系统，实现故障报修、工单流转、指派处理、完成报告、评论回复、数据统计等核心功能。

## ✨ 功能特性

- **用户系统**：登录注册、JWT认证、三种角色（管理员/运维人员/普通用户）
- **工单管理**：创建、编辑、删除、搜索筛选、分页
- **工单流转**：待处理 → 处理中 → 审核中 → 已完成 → 已结束（5状态）
- **工单指派**：管理员指派工单给运维人员
- **完成报告**：运维人员提交完成报告，管理员审核/驳回
- **评论系统**：工单评论、回复
- **统计面板**：ECharts图表（趋势折线图、分类饼图）
- **用户管理**：管理员管理用户角色和状态
- **跨标签页同步**：BroadcastChannel实现多标签页数据实时同步

## 🛠️ 技术栈

| 层 | 技术 | 版本 |
|---|------|------|
| 前端框架 | Vue 3 + Vite | 3.4+ / 5.x |
| UI组件库 | Element Plus | 2.14+ |
| 状态管理 | Pinia | 2.1+ |
| 路由 | Vue Router | 4.3+ |
| HTTP请求 | Axios | 1.16+ |
| 图表 | ECharts | 5.x |
| 动画 | GSAP | 3.12+ |
| 类型系统 | TypeScript | 5.4+ |
| 后端框架 | Spring Boot | 3.2+ |
| Java版本 | JDK | 17 |
| ORM | MyBatis-Plus | 3.5.9 |
| 认证 | JWT | - |
| 数据库 | MySQL | 8.x |

## 📁 项目结构

```
network-fault-ticket-system/
├── app/                          ← 前端项目 (Vue 3)
│   ├── src/
│   │   ├── api/                  ← API请求封装
│   │   ├── assets/               ← 静态资源
│   │   ├── router/               ← 路由配置
│   │   ├── stores/               ← Pinia状态管理
│   │   ├── types/                ← TypeScript类型定义
│   │   ├── utils/                ← 工具函数
│   │   ├── views/                ← 页面视图
│   │   ├── App.vue
│   │   └── main.ts
│   ├── package.json
│   └── vite.config.ts
├── server/                       ← 后端项目 (Spring Boot)
│   ├── src/main/java/com/faultticket/
│   │   ├── config/               ← 配置类
│   │   ├── controller/           ← 控制器层
│   │   ├── service/              ← 业务逻辑层
│   │   ├── mapper/               ← 数据访问层
│   │   ├── domain/               ← 实体类（pojo/dto/vo/query）
│   │   ├── common/               ← 公共类
│   │   ├── interceptor/          ← 拦截器
│   │   └── utils/                ← 工具类
│   ├── src/main/resources/
│   │   └── application.yml
│   └── pom.xml
├── docs/                         ← 设计文档
│   ├── 系统架构与数据库设计.md
│   ├── init.sql                  ← 数据库初始化脚本
│   └── init-test-data.sql        ← 测试数据
└── README.md
```

## 🚀 快速启动

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.x
- Node.js 18+

### 数据库初始化

```bash
# 登录MySQL并执行初始化脚本
mysql -u root -p < docs/init.sql

# 可选：插入测试数据
mysql -u root -p fault_ticket < docs/init-test-data.sql
```

### 后端启动

```bash
cd server
mvn spring-boot:run
```

后端运行在 http://localhost:8080

### 前端启动

```bash
cd app
npm install
npm run dev
```

前端运行在 http://localhost:5173

## 👥 角色说明

| 角色 | 权限说明 |
|------|---------|
| 管理员 | 全部功能：指派工单、审核报告、用户管理、统计面板 |
| 运维人员 | 接单处理、提交完成报告、评论 |
| 普通用户 | 创建工单、查看自己工单、验收评价、评论 |

### 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 运维人员 | ops01 | 123456 |
| 普通用户 | user01 | 123456 |

## 📊 工单状态流转

```
创建工单 → 待处理 → 处理中 → 审核中 → 已完成 → 已结束
                ↓                  ↓
             已结束              处理中（驳回返工）
```

| 当前状态 | 可转换为 | 操作角色 |
|---------|---------|---------|
| 待处理 | 处理中 | 管理员（指派）、运维人员（接单） |
| 处理中 | 审核中 | 运维人员（提交报告） |
| 审核中 | 已完成 | 管理员（审核通过） |
| 审核中 | 处理中 | 管理员（驳回） |
| 已完成 | 已结束 | 普通用户（验收评价） |

## 📡 API接口

### 认证模块
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `GET /api/auth/info` - 获取用户信息

### 工单模块
- `POST /api/tickets` - 创建工单
- `GET /api/tickets` - 工单列表（分页、筛选）
- `GET /api/tickets/{id}` - 工单详情
- `PUT /api/tickets/{id}` - 编辑工单
- `DELETE /api/tickets/{id}` - 删除工单
- `PUT /api/tickets/{id}/status` - 修改状态
- `PUT /api/tickets/{id}/assign` - 指派工单

### 评论模块
- `POST /api/tickets/{id}/comments` - 添加评论
- `GET /api/tickets/{id}/comments` - 获取评论

### 报告模块
- `POST /api/tickets/{id}/report` - 提交完成报告
- `PUT /api/reports/{id}/approve` - 审核通过
- `PUT /api/reports/{id}/reject` - 驳回报告

### 统计模块
- `GET /api/stats/overview` - 概览数据
- `GET /api/stats/trend` - 趋势数据
- `GET /api/stats/by-category` - 分类统计

### 用户管理（管理员）
- `GET /api/users` - 用户列表
- `PUT /api/users/{id}/role` - 修改角色
- `PUT /api/users/{id}/status` - 启用/禁用

## 🎨 页面展示

- 登录页：玻璃拟态风格，GSAP动画
- 注册页：与登录页同款风格
- 统计面板：ECharts图表可视化
- 工单列表：筛选搜索、分页
- 工单详情：信息展示、评论、状态操作
- 用户管理：角色和状态管理

## 📝 设计文档

详细设计文档请查看 [docs/系统架构与数据库设计.md](docs/系统架构与数据库设计.md)

## 📄 许可证

MIT License
