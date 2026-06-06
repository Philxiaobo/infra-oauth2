-- 作者: Phil
-- 日期: 2026-06-06
-- 用途: 初始化 IAM/RBAC 用户权限体系表结构

-- 设计说明:
-- 1. 跨表引用统一使用业务 id,不使用其他表的 MySQL 自增主键 id。
-- 2. 软删除下的业务唯一约束通过 active_*_key 生成列实现:
--    del_flag = 'N' 时使用租户 + 业务编码参与唯一约束;del_flag = 'Y' 时使用本行业务 id,避免历史删除数据阻塞重建。
-- 3. 本 SQL 不建立数据库物理外键,由应用层和索引保障一致性。
-- 4. 表名前缀使用 t_iam_ 表示身份与访问管理域,RBAC 是本域内的权限模型。
-- 5. 权限校验默认 allow-only,按 user_id -> role_id -> permission_id 解析,并过滤启用状态、软删除和授权有效期。

CREATE TABLE `t_iam_user`
(
    `id`                    bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id自增',
    `user_id`               varchar(64)  NOT NULL COMMENT '用户业务id',
    `tenant_id`             varchar(64)  NOT NULL DEFAULT '' COMMENT '租户业务id,空表示平台级',
    `username`              varchar(64)  NOT NULL COMMENT '登录用户名',
    `display_name`          varchar(128) NOT NULL DEFAULT '' COMMENT '用户显示名称',
    `email`                 varchar(128) NOT NULL DEFAULT '' COMMENT '邮箱',
    `mobile_no`             varchar(32)  NOT NULL DEFAULT '' COMMENT '手机号',
    `password_hash`         varchar(255) NOT NULL DEFAULT '' COMMENT '密码哈希',
    `password_algo`         varchar(32)  NOT NULL DEFAULT '' COMMENT '密码算法',
    `enabled_flag`          char(1)      NOT NULL DEFAULT 'Y' COMMENT '启用标志位',
    `locked_flag`           char(1)      NOT NULL DEFAULT 'N' COMMENT '锁定标志位',
    `mfa_enabled_flag`      char(1)      NOT NULL DEFAULT 'N' COMMENT 'MFA启用标志位',
    `password_expired_flag` char(1)      NOT NULL DEFAULT 'N' COMMENT '密码过期标志位',
    `last_login_at`         timestamp(6) NULL DEFAULT NULL COMMENT '最后登录时间',

    `trace_id`              varchar(64)  NOT NULL DEFAULT '' COMMENT 'traceId',
    `created_by`            varchar(64)  NOT NULL DEFAULT '' COMMENT '创建者',
    `created_at`            timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `updated_by`            varchar(64)  NOT NULL DEFAULT '' COMMENT '修改者',
    `updated_at`            timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `del_flag`              char(1)      NOT NULL DEFAULT 'N' COMMENT '删除标志位',
    `active_username_key`   varchar(191) GENERATED ALWAYS AS (
        CASE WHEN `del_flag` = 'N' THEN CONCAT(`tenant_id`, ':', `username`) ELSE `user_id` END
    ) STORED COMMENT '有效用户名唯一键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    UNIQUE KEY `uk_active_username_key` (`active_username_key`),
    KEY `idx_tenant_username` (`tenant_id`, `username`),
    KEY `idx_tenant_mobile` (`tenant_id`, `mobile_no`),
    KEY `idx_tenant_email` (`tenant_id`, `email`),
    KEY `idx_tenant_enabled` (`tenant_id`, `enabled_flag`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  DEFAULT COLLATE = utf8mb4_general_ci COMMENT = 'IAM用户表';

CREATE TABLE `t_iam_role`
(
    `id`                   bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id自增',
    `role_id`              varchar(64)  NOT NULL COMMENT '角色业务id',
    `tenant_id`            varchar(64)  NOT NULL DEFAULT '' COMMENT '租户业务id,空表示平台级',
    `role_code`            varchar(64)  NOT NULL COMMENT '角色编码',
    `role_name`            varchar(128) NOT NULL COMMENT '角色名称',
    `role_type`            varchar(32)  NOT NULL DEFAULT 'TENANT' COMMENT '角色类型',
    `role_level`           int(11)      NOT NULL DEFAULT 0 COMMENT '角色等级',
    `description`          varchar(512) NOT NULL DEFAULT '' COMMENT '角色描述',
    `enabled_flag`         char(1)      NOT NULL DEFAULT 'Y' COMMENT '启用标志位',

    `trace_id`             varchar(64)  NOT NULL DEFAULT '' COMMENT 'traceId',
    `created_by`           varchar(64)  NOT NULL DEFAULT '' COMMENT '创建者',
    `created_at`           timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `updated_by`           varchar(64)  NOT NULL DEFAULT '' COMMENT '修改者',
    `updated_at`           timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `del_flag`             char(1)      NOT NULL DEFAULT 'N' COMMENT '删除标志位',
    `active_role_code_key` varchar(191) GENERATED ALWAYS AS (
        CASE WHEN `del_flag` = 'N' THEN CONCAT(`tenant_id`, ':', `role_code`) ELSE `role_id` END
    ) STORED COMMENT '有效角色编码唯一键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_id` (`role_id`),
    UNIQUE KEY `uk_active_role_code_key` (`active_role_code_key`),
    KEY `idx_tenant_role_code` (`tenant_id`, `role_code`),
    KEY `idx_tenant_enabled` (`tenant_id`, `enabled_flag`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  DEFAULT COLLATE = utf8mb4_general_ci COMMENT = 'IAM角色表';

CREATE TABLE `t_iam_permission`
(
    `id`                         bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id自增',
    `permission_id`              varchar(64)  NOT NULL COMMENT '权限业务id',
    `tenant_id`                  varchar(64)  NOT NULL DEFAULT '' COMMENT '租户业务id,空表示全局权限',
    `permission_code`            varchar(128) NOT NULL COMMENT '权限编码',
    `permission_name`            varchar(128) NOT NULL COMMENT '权限名称',
    `permission_type`            varchar(32)  NOT NULL COMMENT '权限类型',
    `parent_permission_id`       varchar(64)  NOT NULL DEFAULT '' COMMENT '父权限业务id',
    `resource_code`              varchar(128) NOT NULL DEFAULT '' COMMENT '资源编码',
    `resource_path`              varchar(255) NOT NULL DEFAULT '' COMMENT '资源路径',
    `http_method`                varchar(16)  NOT NULL DEFAULT '' COMMENT 'HTTP方法',
    `scope_code`                 varchar(128) NOT NULL DEFAULT '' COMMENT 'OAuth2 scope编码',
    `sort_order`                 int(11)      NOT NULL DEFAULT 0 COMMENT '排序号',
    `description`                varchar(512) NOT NULL DEFAULT '' COMMENT '权限描述',
    `enabled_flag`               char(1)      NOT NULL DEFAULT 'Y' COMMENT '启用标志位',

    `trace_id`                   varchar(64)  NOT NULL DEFAULT '' COMMENT 'traceId',
    `created_by`                 varchar(64)  NOT NULL DEFAULT '' COMMENT '创建者',
    `created_at`                 timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `updated_by`                 varchar(64)  NOT NULL DEFAULT '' COMMENT '修改者',
    `updated_at`                 timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `del_flag`                   char(1)      NOT NULL DEFAULT 'N' COMMENT '删除标志位',
    `active_permission_code_key` varchar(255) GENERATED ALWAYS AS (
        CASE WHEN `del_flag` = 'N' THEN CONCAT(`tenant_id`, ':', `permission_code`) ELSE `permission_id` END
    ) STORED COMMENT '有效权限编码唯一键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_id` (`permission_id`),
    UNIQUE KEY `uk_active_permission_code_key` (`active_permission_code_key`),
    KEY `idx_tenant_permission_code` (`tenant_id`, `permission_code`),
    KEY `idx_parent_permission_id` (`parent_permission_id`, `del_flag`),
    KEY `idx_resource` (`resource_code`, `http_method`, `del_flag`),
    KEY `idx_scope_code` (`scope_code`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  DEFAULT COLLATE = utf8mb4_general_ci COMMENT = 'IAM权限表';

CREATE TABLE `t_iam_user_role`
(
    `id`                   bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id自增',
    `user_role_id`         varchar(64)  NOT NULL COMMENT '用户角色关系业务id',
    `tenant_id`            varchar(64)  NOT NULL DEFAULT '' COMMENT '租户业务id,空表示平台级',
    `user_id`              varchar(64)  NOT NULL COMMENT '用户业务id',
    `role_id`              varchar(64)  NOT NULL COMMENT '角色业务id',
    `grant_source`         varchar(32)  NOT NULL DEFAULT 'MANUAL' COMMENT '授权来源',
    `effective_from`       timestamp(6) NULL DEFAULT NULL COMMENT '生效开始时间',
    `effective_to`         timestamp(6) NULL DEFAULT NULL COMMENT '生效结束时间',
    `enabled_flag`         char(1)      NOT NULL DEFAULT 'Y' COMMENT '启用标志位',

    `trace_id`             varchar(64)  NOT NULL DEFAULT '' COMMENT 'traceId',
    `created_by`           varchar(64)  NOT NULL DEFAULT '' COMMENT '创建者',
    `created_at`           timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `updated_by`           varchar(64)  NOT NULL DEFAULT '' COMMENT '修改者',
    `updated_at`           timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `del_flag`             char(1)      NOT NULL DEFAULT 'N' COMMENT '删除标志位',
    `active_user_role_key` varchar(255) GENERATED ALWAYS AS (
        CASE WHEN `del_flag` = 'N' THEN CONCAT(`tenant_id`, ':', `user_id`, ':', `role_id`) ELSE `user_role_id` END
    ) STORED COMMENT '有效用户角色关系唯一键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role_id` (`user_role_id`),
    UNIQUE KEY `uk_active_user_role_key` (`active_user_role_key`),
    KEY `idx_user_id` (`user_id`, `del_flag`),
    KEY `idx_role_id` (`role_id`, `del_flag`),
    KEY `idx_tenant_user_effective` (`tenant_id`, `user_id`, `enabled_flag`, `del_flag`, `effective_from`, `effective_to`),
    KEY `idx_tenant_role` (`tenant_id`, `role_id`, `enabled_flag`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  DEFAULT COLLATE = utf8mb4_general_ci COMMENT = 'IAM用户角色关系表';

CREATE TABLE `t_iam_role_permission`
(
    `id`                         bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id自增',
    `role_permission_id`         varchar(64)  NOT NULL COMMENT '角色权限关系业务id',
    `tenant_id`                  varchar(64)  NOT NULL DEFAULT '' COMMENT '租户业务id,空表示平台级',
    `role_id`                    varchar(64)  NOT NULL COMMENT '角色业务id',
    `permission_id`              varchar(64)  NOT NULL COMMENT '权限业务id',
    `grant_source`               varchar(32)  NOT NULL DEFAULT 'MANUAL' COMMENT '授权来源',
    `enabled_flag`               char(1)      NOT NULL DEFAULT 'Y' COMMENT '启用标志位',

    `trace_id`                   varchar(64)  NOT NULL DEFAULT '' COMMENT 'traceId',
    `created_by`                 varchar(64)  NOT NULL DEFAULT '' COMMENT '创建者',
    `created_at`                 timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `updated_by`                 varchar(64)  NOT NULL DEFAULT '' COMMENT '修改者',
    `updated_at`                 timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `del_flag`                   char(1)      NOT NULL DEFAULT 'N' COMMENT '删除标志位',
    `active_role_permission_key` varchar(255) GENERATED ALWAYS AS (
        CASE WHEN `del_flag` = 'N' THEN CONCAT(`tenant_id`, ':', `role_id`, ':', `permission_id`) ELSE `role_permission_id` END
    ) STORED COMMENT '有效角色权限关系唯一键',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_permission_id` (`role_permission_id`),
    UNIQUE KEY `uk_active_role_permission_key` (`active_role_permission_key`),
    KEY `idx_role_id` (`role_id`, `del_flag`),
    KEY `idx_permission_id` (`permission_id`, `del_flag`),
    KEY `idx_tenant_role_enabled` (`tenant_id`, `role_id`, `enabled_flag`, `del_flag`),
    KEY `idx_tenant_permission` (`tenant_id`, `permission_id`, `enabled_flag`, `del_flag`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  DEFAULT COLLATE = utf8mb4_general_ci COMMENT = 'IAM角色权限关系表';
