# infra-oauth2 目录结构

> 最后更新: 2026-06-03

标准来源：`infra-genesis/docs/conventions/microservice-directory-structure.md`。

```text
com.phil.infra.oauth2
├── InfraOauth2Application.java
├── config/
├── properties/
├── support/
├── auth/
├── token/
├── client/
├── user/
└── permission/
```

每个业务模块内部默认按以下结构组织：

```text
<module>/
├── interfaces/
├── application/
├── domain/
└── infrastructure/
```

## 约定

- `auth` 负责登录、登出、会话、授权码等认证用例。
- `token` 负责 token 签发、刷新、撤销、解析。
- `client` 负责 OAuth2 client / 应用接入方管理。
- `user` 负责用户身份、账号状态、凭证绑定。
- `permission` 负责角色、权限、资源粗粒度模型，按需实现。
