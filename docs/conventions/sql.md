# SQL Convention

本文定义 SQL 建表、字段、注释和脚本留存规范。

## 基础要求

- 所有数据库表必须有中文表注释
- 所有数据库字段必须有中文字段注释
- 所有业务表必须包含统一基础字段
- 所有业务表必须包含业务唯一主键字段,跨表引用必须使用业务主键,不得使用 MySQL 自增主键作为业务外键
- 业务唯一主键由项目内 ID 生成工具生成,格式参考 `IdGenerator` 的业务前缀 + 随机串 + Redis 去重模式
- 状态类二值字段统一使用 `char(1)` 的 `Y/N` 表示,不使用 `tinyint(1/0)` 表示启停状态
- SQL 文件头部必须包含作者、日期、用途
- 新增表结构、字段变更、索引变更和数据修复 SQL 统一留存在 `docs/sql/`

## SQL 文件头

```sql
-- 作者: Phil
-- 日期: 2026-05-15
-- 用途: 初始化后台 RBAC 表结构
```

## 建表模板

所有建表 SQL 必须严格参考以下模板:

```sql
CREATE TABLE `t_demo`
(
    `id`         bigint(20)   NOT NULL AUTO_INCREMENT COMMENT '主键id自增',
    `demo_id`    varchar(64)  NOT NULL COMMENT '示例业务id',
    -- 其他字段信息

    `trace_id`   varchar(64)  NOT NULL DEFAULT '' COMMENT 'traceId',
    `created_by` varchar(64)  NOT NULL DEFAULT '' COMMENT '创建者',
    `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `updated_by` varchar(64)  NOT NULL DEFAULT '' COMMENT '修改者',
    `updated_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    `del_flag`   char(1)      NOT NULL DEFAULT 'N' COMMENT '删除标志位',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_demo_id` (`demo_id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  DEFAULT COLLATE = utf8mb4_general_ci COMMENT = '表备注';
```

## 统一基础字段

| 字段 | 类型 | 默认值 | 注释 | 说明 |
|------|------|--------|------|------|
| `id` | `bigint(20)` | 自增 | 主键id自增 | 所有表统一主键 |
| `<biz>_id` | `varchar(64)` | 业务生成 | 业务id | 业务唯一主键,由业务 ID 工具生成 |
| `trace_id` | `varchar(64)` | `''` | traceId | 记录创建或变更链路 |
| `created_by` | `varchar(64)` | `''` | 创建者 | 记录创建人 |
| `created_at` | `timestamp(6)` | `CURRENT_TIMESTAMP(6)` | 创建时间 | 保留微秒精度 |
| `updated_by` | `varchar(64)` | `''` | 修改者 | 记录最后修改人 |
| `updated_at` | `timestamp(6)` | `CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)` | 更新时间 | 自动更新时间 |
| `del_flag` | `char(1)` | `'N'` | 删除标志位 | `N` 未删除,`Y` 已删除 |

## 命名约定

- 表名使用小写下划线,业务表建议以 `t_` 开头
- 字段名使用小写下划线
- 主键统一命名为 `id`
- 业务唯一主键统一命名为 `<业务名>_id`,如 `user_id`、`role_id`、`menu_id`
- 业务唯一主键值使用业务前缀区分类型,如 `NS`、`USR`、`ROLE`、`MENU`
- 跨表引用统一引用业务唯一主键,不要引用其他表的 `id` 自增主键
- 普通索引命名:`idx_<字段或业务含义>`
- 唯一索引命名:`uk_<字段或业务含义>`
- 外键不强制建立数据库物理约束,由业务和索引保障一致性

## 软删除约定

- `del_flag = 'N'` 表示有效
- `del_flag = 'Y'` 表示已删除
- 查询默认过滤 `del_flag = 'N'`
- 唯一索引如需兼容软删除,必须在 SQL 或设计说明中明确策略
- `del_flag` 新增数据依赖表字段默认值 `'N'`,应用层不在 insert 时重复填充
- MyBatis-Plus 默认查询和删除依赖 `@TableLogic`;手写 XML 查询必须显式包含 `del_flag = 'N'`
