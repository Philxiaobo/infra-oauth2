# infra-oauth2 架构概览

> 最后更新: 2026-06-03

## 定位

`infra-oauth2` 是统一认证鉴权服务，负责登录、token 签发、刷新、撤销、客户端管理、用户身份解析，以及给 Gateway 提供内部鉴权接口。

## 核心链路

```text
Client / Gateway
    ↓
infra-oauth2
    ├─ auth: 登录、登出、认证流程
    ├─ token: token 签发、刷新、撤销、解析
    ├─ client: OAuth2 client / 应用接入方管理
    ├─ user: 用户身份、账号状态、凭证绑定
    └─ permission: 角色、权限、资源粗粒度模型
```

## 边界

- 可以依赖 Servlet MVC、`infra-web`、MyBatis、Redis 和 tracing starter。
- 不负责入口路由、CORS、WAF、黑白名单、限流和熔断。
- 不把 Gateway 路由规则写入认证服务。
