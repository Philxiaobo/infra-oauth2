# OAuth2 Authorization Server MVP — 首批认证鉴权能力初始化

> **状态**: Implemented
> **负责人**: Phil
> **创建日期**: 2026-06-06
> **最后更新**: 2026-06-06
> **依赖 ADR**: —
> **依赖 spec**: —

## 1. 背景与目标(Why)

参考 `/Users/photonpay/Desktop/infra-gateway-oauth2-push-implementation.md` 中 `infra-oauth2` 建设目标，本服务需要从当前基础骨架推进到可启动的 Spring Authorization Server 基线，为后续 infra-gateway JWT 校验、infra-push 握手鉴权和业务服务 Resource Server 接入提供标准 OAuth2/OIDC 能力。

本轮目标是初始化首批可验证能力：标准 Authorization Server 端点、OIDC metadata、JWK Set、JWT access token、Authorization Code + PKCE、Client Credentials、Refresh Token 基线、seed client/user 和基础安全审计事件。

## 2. 需求范围(What)

### 2.1 功能需求
- [x] ✅ 启用 Spring Authorization Server 标准端点，暴露 OAuth2/OIDC metadata 与 `/oauth2/jwks`
- [x] ✅ 初始化 browser public client，支持 Authorization Code + PKCE 和 Refresh Token
- [x] ✅ 初始化 service confidential client，支持 Client Credentials
- [x] ✅ 禁用 Password Grant 和 Implicit Grant
- [x] ✅ 签发 JWT access token，包含 `iss`、`sub`、`aud`、`exp`、`iat`、`jti`、`scope`
- [x] ✅ 初始化本地 seed user，后续可替换为企业 IdP/SSO
- [x] ✅ 发布认证成功、认证失败、退出登录等基础审计事件

### 2.2 非目标(Non-goals)

- 本轮不接真实 MySQL/PostgreSQL 持久化；client、authorization、consent 的 JDBC 持久化在后续 DB 连接规范明确后落地
- 本轮不做自定义登录页、MFA、企业 SSO、KMS/Vault 私钥托管和 JWK 三阶段轮换
- 本轮不实现管理端 client CRUD、scope 审批、token 查询运营台
- 本轮不实现 infra-gateway、infra-push 或业务服务 Resource Server 接入代码

## 3. 设计方案(How)

### 3.1 整体流程

```text
Browser/App
  -> /oauth2/authorize + PKCE
  -> Spring Authorization Server
  -> JWT access_token + refresh_token

Service Client
  -> /oauth2/token client_credentials
  -> Spring Authorization Server
  -> JWT access_token

infra-gateway / infra-push
  -> /.well-known/openid-configuration
  -> /oauth2/jwks
  -> 校验 JWT 签名和 issuer
```

### 3.2 关键类 / 包结构

- `src/main/java/com/phil/infra/oauth2/config/InfraOAuth2Properties.java`: `@ConfigurationProperties` 承载 issuer、token TTL、seed clients/users
- `src/main/java/com/phil/infra/oauth2/config/AuthorizationServerSecurityConfiguration.java`: Spring Security / Authorization Server / JWK / JWT 配置
- `src/main/java/com/phil/infra/oauth2/config/OAuth2BootstrapConfiguration.java`: seed `RegisteredClientRepository`、`UserDetailsService`、token settings
- `src/main/java/com/phil/infra/oauth2/audit/OAuth2AuditEvent.java`: 统一审计事件对象
- `src/main/java/com/phil/infra/oauth2/audit/OAuth2AuditEventPublisher.java`: 审计事件发布入口
- `src/main/java/com/phil/infra/oauth2/audit/OAuth2SecurityAuditListener.java`: Spring Security 认证事件监听并转发审计

### 3.3 数据模型

本轮为 in-memory MVP，不新增 DB 表。后续 JDBC 持久化将直接使用 Spring Authorization Server 官方 schema：

- `oauth2_registered_client`
- `oauth2_authorization`
- `oauth2_authorization_consent`

### 3.4 接口契约

使用 Spring Authorization Server 标准端点：

- `GET /.well-known/openid-configuration`
- `GET /.well-known/oauth-authorization-server`
- `GET /oauth2/jwks`
- `GET /oauth2/authorize`
- `POST /oauth2/token`
- `POST /oauth2/revoke`
- `POST /oauth2/introspect`

### 3.5 错误 & 降级

- 未认证访问授权端点跳转 `/login`
- client 配置包含 `password` 或 `implicit` grant 时启动失败
- JWK 本轮使用启动期内存 RSA key；生产私钥托管与轮换在后续安全增强 spec 中实现
- 审计事件本轮发布到 Spring ApplicationEvent 并写日志；审计落库后续单独实现

## 4. 验收标准(Verify)

### 4.1 功能用例
- [x] browser client 只允许 Authorization Code + Refresh Token，且强制 PKCE
- [x] service client 只允许 Client Credentials，且使用 confidential client secret
- [x] Password Grant 和 Implicit Grant 不出现在 seed client 配置中
- [x] `AuthorizationServerSettings` issuer 为 `http://localhost:9000`
- [x] OIDC/JWK 基础 bean 可启动
- [x] 审计发布器能发布结构化安全事件

### 4.2 性能 / 非功能
- Access Token TTL 默认 10 分钟，满足 5-15 分钟建议区间
- Refresh Token 默认 30 天，默认不复用 refresh token
- 默认 seed secret 仅用于本地开发，生产必须通过环境配置覆盖

### 4.3 测试清单
- [x] 单测/上下文测试覆盖 `OAuth2BootstrapConfigurationTest`
- [x] 端点测试覆盖 `OAuth2DiscoveryEndpointTest`
- [x] 应用上下文启动测试 `InfraOauth2ApplicationTest`
- [x] Maven 最小验证 `JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test`

## 5. 任务拆分(Tasks)

> 状态:✅ 已完成 / 🚧 进行中 / ⬜ 未开始

- [x] ✅ T1: 新增 OAuth2 MVP spec 和索引状态(`specs/oauth2-authorization-server-mvp.spec.md:1`, `specs/README.md:19`)
- [x] ✅ T2: 写入 RED 测试覆盖 seed client、issuer、审计发布器和发现端点(`src/test/java/com/phil/infra/oauth2/config/OAuth2BootstrapConfigurationTest.java:40`, `src/test/java/com/phil/infra/oauth2/config/OAuth2DiscoveryEndpointTest.java:23`)
- [x] ✅ T3: 实现 `InfraOAuth2Properties` 与 seed client/user 配置(`src/main/java/com/phil/infra/oauth2/config/InfraOAuth2Properties.java:14`, `src/main/resources/application.yml:30`)
- [x] ✅ T4: 实现 Authorization Server 安全链、JWK、JWT customizer 与 token settings(`src/main/java/com/phil/infra/oauth2/config/AuthorizationServerSecurityConfiguration.java:43`, `src/main/java/com/phil/infra/oauth2/config/OAuth2BootstrapConfiguration.java:37`)
- [x] ✅ T5: 实现基础审计事件发布与监听(`src/main/java/com/phil/infra/oauth2/audit/OAuth2AuditEvent.java:15`, `src/main/java/com/phil/infra/oauth2/audit/OAuth2AuditEventPublisher.java:44`, `src/main/java/com/phil/infra/oauth2/audit/OAuth2SecurityAuditListener.java:22`)
- [x] ✅ T6: 运行 Maven 验证并同步 README / AGENTS / WORKLOG(`README.md:27`, `AGENTS.md:35`, `WORKLOG.md:7`)

## 6. 风险与待决问题

- ⚠️ 风险等级: P0/P1。认证、token、client、JWK 属于权限和核心业务链路，必须保留 spec、验证命令和回滚思路。
- ❓ JDBC 持久化选型待决：需要确认本服务使用 MySQL 还是 PostgreSQL，以及是否通过 `infra-mybatis-plus-starter` 统一数据访问。
- ❓ 生产私钥托管待决：需要确认 KMS、Vault 或 Kubernetes Secret Manager 的目标方案。
- ↩️ 回滚思路：移除本 spec 对应新增的 `config/`、`audit/`、OAuth2 测试文件和 `infra.oauth2.issuer` 配置，恢复到仅保留 Authorization Server 依赖的服务骨架。

## 7. 变更日志

| 日期 | 变更 | 作者 |
|------|------|------|
| 2026-06-06 | 初稿，按参考文档拆出 infra-oauth2 首批 MVP 初始化范围 | Phil |
| 2026-06-06 | 完成 Spring Authorization Server MVP 基线、seed clients/users、JWK/OIDC metadata、审计事件和测试验证 | Phil |
