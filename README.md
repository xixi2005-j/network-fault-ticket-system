# 网络故障工单系统

> 《互联网应用开发实践》课程设计项目

## 项目简介

面向网络工程领域的故障工单管理系统，实现故障报修、工单流转、指派处理、评论回复、数据统计等核心功能。

## 技术栈

| 层 | 技术 |
|---|------|
| 前端 | Vue 3 + Vite + Element Plus + TypeScript + Pinia + ECharts |
| 后端 | Spring Boot 3.2 + MyBatis-Plus + JWT |
| 数据库 | MySQL 8.x |

## 项目结构

```
network-fault-ticket-system/
├── frontend/    ← 前端项目 (Vue 3)
├── backend/     ← 后端项目 (Spring Boot)
├── docs/        ← 设计文档
└── README.md
```

## 快速启动

### 后端
```bash
cd backend
mvn spring-boot:run
```

### 前端
```bash
cd frontend
npm install
npm run dev
```

## 角色说明

| 角色 | 权限 |
|------|------|
| 管理员 | 全部功能，包括指派工单、用户管理、统计面板 |
| 运维人员 | 接单处理、修改状态、评论 |
| 普通用户 | 创建工单、查看自己工单、评论 |
