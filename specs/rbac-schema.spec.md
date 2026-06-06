# IAM/RBAC Schema — 用户权限体系表结构

> **状态**: Implemented
> **负责人**: Phil
> **创建日期**: 2026-06-06
> **最后更新**: 2026-06-06
> **依赖 ADR**: —
> **依赖 spec**: `oauth2-authorization-server-mvp`

## 1. 背景与目标(Why)

`infra-oauth2` 当前已完成 Spring Authorization Server MVP 基线，但用户、角色、权限仍是启动期 seed 数据。下一步需要先沉淀 IAM 域下的基础 RBAC 数据模型，为后续真实用户体系、登录认证、JWT claim 中 `roles` / `scope` 解析和管理端权限配置提供稳定表结构。

本轮目标是按 SQL Convention 落地基础 IAM/RBAC 建表脚本，覆盖用户、角色、权限、用户角色绑定和角色权限绑定。

## 2. 需求范围(What)

### 2.1 功能需求
- [x] ✅ 固化 SQL 建表规范到 `docs/conventions/sql.md`
- [x] ✅ 新增 `docs/sql/2026-06-06-init-rbac-schema.sql`
- [x] ✅ 表名前缀采用 `t_iam_`，表达身份与访问管理域；RBAC 作为权限模型保留在 spec 和业务语义中
- [x] ✅ 设计用户表，承载登录名、密码摘要、账号状态和租户边界
- [x] ✅ 设计角色表，承载租户内角色编码、名称和启停状态
- [x] ✅ 设计权限表，承载 API / MENU / ACTION 粗粒度资源权限
- [x] ✅ 设计用户角色和角色权限关系表，跨表引用均使用业务 id
- [x] ✅ 所有表包含统一基础字段、中文注释、业务唯一主键和软删除唯一性策略
- [x] ✅ 明确后续权限校验最佳实践：默认拒绝、allow-only、scope 与 permission 分层、Gateway 粗粒度与服务侧细粒度校验

### 2.2 非目标(Non-goals)

- 本轮不实现 Java entity / mapper / repository / service
- 本轮不实现权限缓存、权限版本失效、Spring Security permission evaluator 或 Gateway Filter
- 本轮不实现 OAuth2 Authorization Server 官方表结构
- 本轮不做组织架构、部门、岗位、数据权限、ABAC / policy engine
- 本轮不建租户主数据表；`tenant_id` 仅作为外部租户业务 id 引用

## 3. 设计方案(How)

### 3.1 整体流程

```text
t_iam_user
    |
    | user_id
    v
t_iam_user_role
    |
    | role_id
    v
t_iam_role
    |
    | role_id
    v
t_iam_role_permission
    |
    | permission_id
    v
t_iam_permission
```

### 3.2 关键表结构

- `t_iam_user`: 用户身份、登录名、密码摘要、账号启停/锁定/MFA 状态
- `t_iam_role`: 租户内角色，使用 `role_code` 做业务编码
- `t_iam_permission`: 权限资源，支持 `API`、`MENU`、`ACTION`
- `t_iam_user_role`: 用户和角色多对多关系
- `t_iam_role_permission`: 角色和权限多对多关系

### 3.3 数据模型

- 所有表保留自增 `id` 作为数据库主键
- 表名前缀使用 `t_iam_` 表达 IAM 业务域，避免把 RBAC 模型名称固化为数据域边界
- 所有业务引用使用 `user_id`、`role_id`、`permission_id`、`user_role_id`、`role_permission_id`
- `tenant_id` 使用 `varchar(64) NOT NULL DEFAULT ''`，空值表示平台级或全局权限
- 启停/锁定/MFA 等二值字段使用 `char(1)` 的 `Y/N`
- 不建立数据库物理外键，依赖业务校验和索引保障一致性

### 3.4 接口契约

本轮只交付 SQL 脚本，不暴露 HTTP / RPC 接口。后续 Java 实现时默认查询必须过滤 `del_flag = 'N'`。

### 3.5 权限校验约定

- 权限校验默认拒绝，只有命中有效授权链路时才放行。
- 基础 RBAC 使用 allow-only 语义，不引入 deny 规则、角色继承或策略表达式，避免权限优先级歧义。
- 用户权限解析链路为 `user_id -> role_id -> permission_id`，查询必须同时过滤 `enabled_flag = 'Y'`、`del_flag = 'N'` 和用户角色授权有效期。
- `scope_code` 用于 OAuth2 client / token 的授权边界，`permission_code` 用于内部用户权限点，二者不互相替代。
- Gateway 可基于 `permission_type = 'API'`、`resource_path`、`http_method` 做粗粒度接口鉴权；服务侧继续基于 `permission_code` 做业务动作级鉴权。
- 后续运行时可缓存 `tenant_id + user_id` 的权限集合，用户角色或角色权限关系变更后必须主动失效缓存。

### 3.6 错误 & 降级

- 软删除唯一性通过生成列 `active_*_key` 处理：有效数据参与业务唯一约束，已删除数据使用自身业务 id 避免冲突
- 重复绑定用户角色或角色权限时，唯一索引直接阻断重复有效关系
- 当前未做物理外键，删除用户/角色/权限前需要应用层检查有效绑定关系

## 4. 验收标准(Verify)

### 4.1 功能用例
- [x] SQL 文件头包含作者、日期、用途
- [x] 所有表有中文表注释
- [x] 所有字段有中文字段注释
- [x] 所有表包含 `id`、业务 id、`trace_id`、`created_by`、`created_at`、`updated_by`、`updated_at`、`del_flag`
- [x] 所有跨表引用使用业务 id 字段，不使用其他表自增 `id`
- [x] 所有二值状态字段使用 `char(1)` 的 `Y/N`
- [x] 软删除唯一性策略在 SQL 和 spec 中明确
- [x] 表名统一使用 `t_iam_` 前缀
- [x] 权限校验约定明确默认拒绝、allow-only、scope / permission 分层和 Gateway / 服务侧边界

### 4.2 性能 / 非功能
- 高频查询字段包含索引：`tenant_id + username`、`user_id`、`role_id`、`permission_id`
- 权限判定链路可按 `tenant_id + user_id -> tenant_id + role_id -> permission_id` 三段索引查询

### 4.3 测试清单
- [x] `git diff --check`
- [x] `rg -n "CREATE TABLE|COMMENT =|COMMENT '" docs/sql/2026-06-06-init-rbac-schema.sql`
- [x] 静态检查 SQL 中不存在跨表引用 `*_id bigint`
- [x] 静态检查 SQL 中不存在 `t_rbac_` 表名

## 5. 任务拆分(Tasks)

> 状态:✅ 已完成 / 🚧 进行中 / ⬜ 未开始

- [x] ✅ T1: 新增 RBAC schema spec 并同步索引(`specs/rbac-schema.spec.md:1`, `specs/README.md:19`)
- [x] ✅ T2: 固化 SQL Convention(`docs/conventions/sql.md:1`)
- [x] ✅ T3: 新增 RBAC 初始化 SQL(`docs/sql/2026-06-06-init-rbac-schema.sql:13`)
- [x] ✅ T4: 执行 SQL 静态验证和补丁检查(`docs/sql/2026-06-06-init-rbac-schema.sql:13`)
- [x] ✅ T5: session-close 同步 AGENTS / WORKLOG(`AGENTS.md:38`, `WORKLOG.md:7`)
- [x] ✅ T6: 将表名前缀收敛为 IAM 域并补充权限校验约定(`docs/sql/2026-06-06-init-rbac-schema.sql:13`, `specs/rbac-schema.spec.md:82`)

## 6. 风险与待决问题

- ⚠️ 风险等级: P0。IAM/RBAC 表结构影响用户权限、安全鉴权和后续 JWT claim 生成。
- ❓ 真实租户主数据来源待确认，本轮只保留 `tenant_id` 字段。
- ❓ 数据权限、组织架构、岗位和继承角色暂不进入基础 RBAC。
- ↩️ 回滚思路：删除本轮新增 SQL 文件和 spec 索引，不影响当前运行时代码。

## 7. 变更日志

| 日期 | 变更 | 作者 |
|------|------|------|
| 2026-06-06 | 初稿，定义基础 RBAC 五表模型和 SQL 落地范围 | Phil |
| 2026-06-06 | 完成 SQL Convention 固化、RBAC 五表初始化 SQL 和静态验证 | Phil |
| 2026-06-06 | 表名前缀调整为 IAM 域，并补充权限校验最佳实践 | Phil |
