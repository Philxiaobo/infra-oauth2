# service-parent-migration — 迁移到 infra-service-parent

> **状态**: Implemented
> **负责人**: Phil
> **创建日期**: 2026-06-06
> **最后更新**: 2026-06-06
> **依赖 ADR**: —
> **依赖 spec**: —

## 1. 背景与目标(Why)

`infra-oauth2` 作为认证鉴权服务，当前 POM 直接继承 `spring-boot-starter-parent`，并声明 `infra.version` 给内部依赖使用。这样会导致后续 Spring Boot / infra starter 版本升级时需要逐服务维护，违背 `infra-genesis` 作为所有微服务底层依赖基线的目标。

本轮目标是将 `infra-oauth2` 迁移为继承 `infra-service-parent`，仅保留 OAuth2 职责需要的独有依赖和按需 starter。

## 2. 需求范围(What)

### 2.1 功能需求
- [x] ✅ POM parent 切换为 `com.phil.infra:infra-service-parent:1.0-SNAPSHOT`(`pom.xml:7`)
- [x] ✅ 删除本服务散落的 `infra.version` 属性(`pom.xml:18`)
- [x] ✅ 删除内部依赖 `infra-web` / `infra-tracing-starter` 上的显式版本，版本交给底层父 POM / BOM 管理(`pom.xml:47`, `pom.xml:51`)
- [x] ✅ 保留 OAuth2 职责独有依赖：Spring Security Authorization Server、Servlet MVC、Undertow、Validation、Actuator(`pom.xml:19`)
- [x] ✅ 同步 spec 索引、AGENTS 实现状态、README 和 WORKLOG(`specs/README.md:19`, `AGENTS.md:39`, `README.md:8`, `WORKLOG.md:6`)

### 2.2 非目标(Non-goals)
- 本轮不实现登录、token 签发、客户端管理等业务能力。
- 本轮不引入数据库或 Redis starter，后续按 OAuth2 数据模型 spec 再决定。
- 本轮不改 `infra-genesis` 版本，只消费已经发布到本地仓库的 `infra-service-parent`。

## 3. 设计方案(How)

### 3.1 POM 关系

```text
infra-oauth2
  ↓ parent
infra-service-parent
  ↓ dependencyManagement import
infra-genesis
```

### 3.2 依赖边界

- 公共构建和依赖版本由 `infra-service-parent` / `infra-genesis` 管理。
- OAuth2 职责型依赖继续留在本服务 POM：`spring-boot-starter-oauth2-authorization-server`。
- Servlet MVC 基础能力使用 `infra-web`，链路追踪使用 `infra-tracing-starter`，二者都不声明版本。

## 4. 验收标准(Verify)

### 4.1 功能用例
- [x] ✅ POM 不再直接继承 `spring-boot-starter-parent`。
- [x] ✅ POM 中 `com.phil.infra` 依赖不再声明版本。
- [x] ✅ OAuth2 职责依赖仍保留在本服务 POM。

### 4.2 测试清单
- [x] ✅ RED: `ruby -rrexml/document -e "doc=REXML::Document.new(File.read('pom.xml')); parent=REXML::XPath.first(doc,'/project/parent/artifactId')&.text; abort('expected infra-service-parent, got ' + parent.to_s) unless parent == 'infra-service-parent'; forbidden=REXML::XPath.match(doc, '//dependency/artifactId[text()=\\\"spring-cloud-dependencies\\\"]'); abort('spring-cloud-dependencies still imported') unless forbidden.empty?; infra_versions=REXML::XPath.match(doc, '//dependency[groupId/text()=\\\"com.phil.infra\\\"]/version'); abort('internal dependency versions still present') unless infra_versions.empty?"`
- [x] ✅ GREEN: `ruby -rrexml/document -e "doc=REXML::Document.new(File.read('pom.xml')); parent=REXML::XPath.first(doc,'/project/parent/artifactId')&.text; abort('expected infra-service-parent, got ' + parent.to_s) unless parent == 'infra-service-parent'; forbidden=REXML::XPath.match(doc, '//dependency/artifactId[text()=\\\"spring-cloud-dependencies\\\"]'); abort('spring-cloud-dependencies still imported') unless forbidden.empty?; infra_versions=REXML::XPath.match(doc, '//dependency[groupId/text()=\\\"com.phil.infra\\\"]/version'); abort('internal dependency versions still present') unless infra_versions.empty?"`
- [x] ✅ MAVEN: `JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test`
- [x] ✅ DOCS: `git diff --check`

## 5. 任务拆分(Tasks)

> 状态:✅ 已完成 / 🚧 进行中 / ⬜ 未开始

- [x] ✅ T1: 切换 POM parent 到 `infra-service-parent`(`pom.xml:7`)
- [x] ✅ T2: 删除 `infra.version` 和内部依赖显式版本(`pom.xml:18`, `pom.xml:47`, `pom.xml:51`)
- [x] ✅ T3: 保留 OAuth2 职责独有依赖(`pom.xml:35`)
- [x] ✅ T4: 同步 spec 索引、AGENTS、README、WORKLOG(`specs/README.md:19`, `AGENTS.md:39`, `README.md:8`, `WORKLOG.md:6`)
- [x] ✅ T5: 完成 GREEN / Maven / diff 验证(`pom.xml:1`)

## 6. 风险与待决问题

- ❓ 需要确认本地 Maven 仓库已安装 `infra-genesis` 与 `infra-service-parent` 的 `1.0-SNAPSHOT`。

## 7. 变更日志

| 日期 | 变更 | 作者 |
|------|------|------|
| 2026-06-06 | POM 迁移到 `infra-service-parent` | Phil |
