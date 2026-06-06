# web-dependency-cleanup — 清理 OAuth2 公共 Web 依赖声明

> **状态**: Implemented
> **负责人**: Phil
> **创建日期**: 2026-06-06
> **最后更新**: 2026-06-06
> **依赖 ADR**: —
> **依赖 spec**: [service-parent-migration.spec.md](service-parent-migration.spec.md)

## 1. 背景与目标(Why)

`infra-oauth2` 已经继承 `infra-service-parent` 并依赖 `infra-web`。`infra-web` 作为 Servlet MVC 基础设施已经包含 Web MVC、排除 Tomcat 后的 Undertow、Validation、AOP、Actuator 和通用 Web 自动配置。

当前 OAuth2 POM 仍直接声明 `spring-boot-starter-web`、`spring-boot-starter-undertow`、`spring-boot-starter-validation`、`spring-boot-starter-actuator`，这会让公共 Web 基线散落在服务 POM 中，不符合“公共依赖由底层统一维护”的目标。

本轮目标是让 OAuth2 POM 只保留 OAuth2 职责依赖和底层 starter：公共 Servlet MVC 能力统一经由 `infra-web` 引入，trace 能力经由 `infra-tracing-starter` 引入。

## 2. 需求范围(What)

### 2.1 功能需求

- [x] ✅ 删除 OAuth2 POM 中直接声明的 Web MVC、Undertow、Validation、Actuator 公共依赖(`pom.xml:19`)
- [x] ✅ 保留 OAuth2 职责依赖 `spring-boot-starter-oauth2-authorization-server`(`pom.xml:20`)
- [x] ✅ 保留底层依赖 `infra-web` 和 `infra-tracing-starter`，且不声明版本(`pom.xml:24`, `pom.xml:28`)
- [x] ✅ 同步 README、AGENTS、service-parent-migration spec、spec 索引和 WORKLOG(`README.md:13`, `AGENTS.md:39`, `specs/service-parent-migration.spec.md:22`, `specs/README.md:19`, `WORKLOG.md:6`)

### 2.2 非目标(Non-goals)

- 本轮不改 OAuth2 业务代码。
- 本轮不新增数据库、Redis 或客户端管理依赖。
- 本轮不调整 `infra-web` 的底层依赖结构。

## 3. 设计方案(How)

### 3.1 依赖边界

```text
infra-oauth2
  ├─ spring-boot-starter-oauth2-authorization-server  # OAuth2 职责依赖
  ├─ infra-web                                        # Servlet MVC + Undertow + Validation + Actuator 等公共 Web 基线
  └─ infra-tracing-starter                            # Micrometer Tracing / OTel / OTLP
```

### 3.2 判断规则

- 如果是普通 Servlet MVC 服务也需要的 Web 基线，放到 `infra-web`。
- 如果是 OAuth2 签发、认证、授权职责独有能力，保留在 `infra-oauth2`。
- 如果是版本管理，交给 `infra-service-parent` / `infra-genesis`。

## 4. 验收标准(Verify)

### 4.1 功能用例

- [x] ✅ POM 不再直接声明 `spring-boot-starter-web`、`spring-boot-starter-undertow`、`spring-boot-starter-validation`、`spring-boot-starter-actuator`。
- [x] ✅ POM 仍声明 `spring-boot-starter-oauth2-authorization-server`。
- [x] ✅ POM 仍声明 `infra-web` 和 `infra-tracing-starter`，并继续由底层管理版本。

### 4.2 测试清单

- [x] ✅ RED: POM 结构检查失败，确认 OAuth2 仍直接声明公共 Web 依赖
- [x] ✅ GREEN: POM 结构检查通过
- [x] ✅ MAVEN: `JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test`
- [x] ✅ DOCS: `git diff --check`

## 5. 任务拆分(Tasks)

> 状态:✅ 已完成 / 🚧 进行中 / ⬜ 未开始

- [x] ✅ T1: 新增 OAuth2 Web 依赖清理 spec(`specs/web-dependency-cleanup.spec.md:1`)
- [x] ✅ T2: 删除 POM 中重复公共 Web 依赖(`pom.xml:19`)
- [x] ✅ T3: 保留 OAuth2 职责依赖和底层 starter(`pom.xml:20`, `pom.xml:24`, `pom.xml:28`)
- [x] ✅ T4: 同步 README、AGENTS、service-parent-migration spec、spec 索引和 WORKLOG(`README.md:13`, `AGENTS.md:39`, `specs/service-parent-migration.spec.md:22`, `specs/README.md:19`, `WORKLOG.md:6`)
- [x] ✅ T5: 完成 POM 结构检查、Maven test 和 diff 检查(`pom.xml:1`)

## 6. 风险与待决问题

- ❓ 如果 `infra-web` 后续拆分 Actuator 或 Validation，需要再回看 OAuth2 是否仍需显式声明。

## 7. 变更日志

| 日期 | 变更 | 作者 |
|------|------|------|
| 2026-06-06 | 新增 OAuth2 公共 Web 依赖清理 spec | Phil |
