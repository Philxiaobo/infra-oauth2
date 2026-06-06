# infra-oauth2

统一认证鉴权服务，负责登录、token 签发/刷新/撤销、客户端管理、用户身份解析和 Gateway 内部鉴权接口。

## 技术栈

Java 21 + Spring Boot 3.5.14 + Spring Security Authorization Server + Spring MVC + Maven

## 依赖基线

本服务继承 `com.phil.infra:infra-service-parent:1.0-SNAPSHOT`。Spring Boot parent、公共插件和 `infra-*` 版本由 `infra-genesis` 统一维护；本服务只保留 OAuth2 / Authorization Server 等职责型独有依赖。

Servlet MVC、Undertow、Validation、Actuator、服务内异常处理和访问日志等公共 Web 基线统一由 `infra-web` 承接，OAuth2 POM 不重复声明这些公共依赖。

## 快速开始

```bash
# 如本地尚未安装 infra-genesis / infra-service-parent 依赖，先在相邻仓库执行:
cd ../infra-genesis
JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q -DskipTests install

# 回到本服务验证:
cd ../infra-oauth2
JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test
```

## OAuth2 MVP 基线

- Issuer: `http://localhost:9000`，可通过 `INFRA_OAUTH2_ISSUER` 覆盖
- OIDC metadata: `GET /.well-known/openid-configuration`
- OAuth2 metadata: `GET /.well-known/oauth-authorization-server`
- JWK Set: `GET /oauth2/jwks`
- Seed clients: `browser-client` 使用 Authorization Code + PKCE，`service-client` 使用 Client Credentials
- Seed user/client secret 仅用于本地开发，非本地环境必须通过环境配置或 `application-local.yml` 覆盖

## 云原生部署

- 部署目录: `deploy/k8s`
- 本地 ConfigMap / Secret 模拟目录: `deploy/local`
- 配置读取: Spring Boot `configtree`，默认读取 `/etc/infra/config/` 和 `/etc/infra/secrets/`
- 服务发现: Kubernetes Service / DNS，服务名 `infra-oauth2`
- 规范来源: `infra-genesis/docs/conventions/microservice-deployment-standard.md`

## 文档

- [AGENTS.md](./AGENTS.md) — AI 协作主文档
- [specs/](./specs/) — 功能 spec
- [deploy/k8s/README.md](./deploy/k8s/README.md) — Kubernetes 部署配置说明
- [docs/architecture/overview.md](./docs/architecture/overview.md) — 架构事实表
- [docs/conventions/directory-structure.md](./docs/conventions/directory-structure.md) — 本服务目录结构
- [WORKLOG.md](./WORKLOG.md) — session 倒序日志
