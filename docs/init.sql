-- 网络故障工单系统 - 数据库初始化脚本

-- 创建数据库
CREATE DATABASE IF NOT EXISTS fault_ticket DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fault_ticket;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码',
    `real_name` VARCHAR(50) DEFAULT NULL COMMENT '真实姓名',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    `role` TINYINT NOT NULL DEFAULT 3 COMMENT '角色：1-管理员 2-运维人员 3-普通用户',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 工单表
CREATE TABLE IF NOT EXISTS `ticket` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '工单ID',
    `title` VARCHAR(200) NOT NULL COMMENT '工单标题',
    `description` TEXT COMMENT '问题描述',
    `category` TINYINT NOT NULL COMMENT '分类：1-网络故障 2-设备故障 3-服务异常 4-其他',
    `priority` TINYINT NOT NULL DEFAULT 3 COMMENT '优先级：1-紧急 2-高 3-中 4-低',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-待处理 2-处理中 3-已完成 4-已关闭',
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `assignee_id` BIGINT DEFAULT NULL COMMENT '处理人ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `resolve_time` DATETIME DEFAULT NULL COMMENT '解决时间',
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_assignee_id` (`assignee_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工单表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
    `user_id` BIGINT NOT NULL COMMENT '评论人ID',
    `content` TEXT NOT NULL COMMENT '评论内容',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评论时间',
    PRIMARY KEY (`id`),
    KEY `idx_ticket_id` (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 操作日志表（毕业设计预留）
CREATE TABLE IF NOT EXISTS `operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `ticket_id` BIGINT NOT NULL COMMENT '工单ID',
    `user_id` BIGINT NOT NULL COMMENT '操作人ID',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型',
    `detail` TEXT COMMENT '操作详情',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_ticket_id` (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 插入默认管理员账号（密码：admin123）
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`)
VALUES ('admin', 'admin123', '系统管理员', 1, 1);

-- 插入测试运维人员
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`)
VALUES ('ops01', '123456', '张运维', 2, 1);

-- 插入测试普通用户
INSERT INTO `user` (`username`, `password`, `real_name`, `role`, `status`)
VALUES ('user01', '123456', '李用户', 3, 1);
