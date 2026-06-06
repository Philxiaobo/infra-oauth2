# infra-oauth2

统一认证鉴权服务，负责登录、token 签发/刷新/撤销、客户端管理、用户身份解析和 Gateway 内部鉴权接口。

## 技术栈

Java 21 + Spring Boot 3.5.14 + Spring Security Authorization Server + Spring MVC + Maven

## 依赖基线

本服务继承 `com.phil.infra:infra-service-parent:1.0-SNAPSHOT`。Spring Boot parent、公共插件和 `infra-*` 版本由 `infra-genesis` 统一维护；本服务只保留 OAuth2 / Authorization Server 等职责型独有依赖。

## 快速开始

```bash
# 如本地尚未安装 infra-genesis / infra-service-parent 依赖，先在相邻仓库执行:
cd ../infra-genesis
JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q -DskipTests install

# 回到本服务验证:
cd ../infra-oauth2
JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test
```

## 文档

- [AGENTS.md](./AGENTS.md) — AI 协作主文档
- [specs/](./specs/) — 功能 spec
- [docs/architecture/overview.md](./docs/architecture/overview.md) — 架构事实表
- [docs/conventions/directory-structure.md](./docs/conventions/directory-structure.md) — 本服务目录结构
- [WORKLOG.md](./WORKLOG.md) — session 倒序日志
