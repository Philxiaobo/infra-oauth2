# infra-oauth2 项目初始化 — 基础骨架

> **状态**: Implemented
> **负责人**: Phil
> **创建日期**: 2026-06-03
> **最后更新**: 2026-06-03
> **依赖 ADR**: —
> **依赖 spec**: —

## 1. 背景与目标(Why)

`infra-oauth2` 需要作为独立认证鉴权微服务建设，先创建最小 Maven / Spring Boot / Servlet MVC 骨架，并接入统一文档体系。

## 2. 需求范围(What)

### 2.1 功能需求
- [x] ✅ 创建 Maven POM，使用 Spring MVC、Undertow、Spring Security Authorization Server starter(`pom.xml:1`)
- [x] ✅ 创建 Spring Boot 启动类和上下文测试(`src/main/java/com/phil/infra/oauth2/InfraOauth2Application.java:1`, `src/test/java/com/phil/infra/oauth2/InfraOauth2ApplicationTest.java:1`)
- [x] ✅ 创建 `application.yml`，记录应用名、端口、actuator、infra-web 和 tracing 默认配置(`src/main/resources/application.yml:1`)
- [x] ✅ 按 OAuth2 特化结构创建包目录和文档(`docs/conventions/directory-structure.md:1`)
- [x] ✅ 创建 AGENTS / README / WORKLOG / specs / ADR 文档骨架(`AGENTS.md:1`, `README.md:1`, `WORKLOG.md:1`, `specs/README.md:1`)

### 2.2 非目标(Non-goals)

- 本次不实现登录、token 签发、刷新、撤销或客户端管理。
- 本次不设计数据库表结构。
- 本次不实现 Gateway 内部鉴权接口。

## 3. 设计方案(How)

详见 `docs/architecture/overview.md` 和 `docs/conventions/directory-structure.md`。

## 4. 验收标准(Verify)

### 4.1 功能用例
- [x] ✅ 目录结构符合 `infra-genesis` 微服务目录标准。
- [x] ✅ 项目文档记录当前初始化进度。

### 4.2 非功能

- 后续具体功能实现前必须先新建对应 spec。

### 4.3 测试清单
- [x] ✅ `JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test`

## 5. 任务拆分(Tasks)

- [x] ✅ T1: 创建 Maven 和 Spring Boot 基础骨架(`pom.xml:1`, `src/main/java/com/phil/infra/oauth2/InfraOauth2Application.java:1`)
- [x] ✅ T2: 创建 OAuth2 包目录和配置文件(`src/main/resources/application.yml:1`, `docs/conventions/directory-structure.md:1`)
- [x] ✅ T3: 创建协作文档并记录进度(`AGENTS.md:1`, `README.md:1`, `WORKLOG.md:1`, `specs/README.md:1`)

## 6. 风险与待决问题

- ❓ 认证协议细节、token 存储策略、客户端表结构需要后续单独 spec。

## 7. 变更日志

| 日期 | 变更 | 作者 |
|------|------|------|
| 2026-06-03 | 初始化基础骨架 | Phil |
