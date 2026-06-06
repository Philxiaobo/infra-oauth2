# Worklog

每次 session 的进度。跨 session 保留上下文,**每次结束前在顶部追加一段**。

---

## 2026-06-06 (清理公共 Web 依赖)

### 🎯 目标
- 清理 OAuth2 POM 中重复声明的公共 Web 基线依赖，让 Servlet MVC、Undertow、Validation、Actuator 等统一由 `infra-web` 承接

### ✅ 完成
- pom: 删除 `spring-boot-starter-web`、`spring-boot-starter-undertow`、`spring-boot-starter-validation`、`spring-boot-starter-actuator` 直接声明，仅保留 Authorization Server、`infra-web`、`infra-tracing-starter` 和 test starter (`pom.xml:19`)
- docs: README / AGENTS 明确公共 Web 基线由 `infra-web` 提供，OAuth2 POM 只保留认证鉴权职责依赖 (`README.md:13`, `AGENTS.md:39`)
- spec: 新增并完成 `web-dependency-cleanup` spec，同时修正 `service-parent-migration` 中关于 OAuth2 依赖边界的表述 (`specs/web-dependency-cleanup.spec.md:1`, `specs/service-parent-migration.spec.md:22`, `specs/README.md:19`)

### 🚧 进行中 / 未完成
- 登录、token 签发、客户端管理等业务能力仍未实现

### 📌 下次继续
- 继续按新业务 spec 实现 OAuth2 第一批认证鉴权能力；如需要 DB/Redis，再按职责引入对应 starter

### 💡 记录
- RED：POM 结构检查失败，确认旧 POM 仍直接声明 Web MVC、Undertow、Validation、Actuator
- GREEN：POM 结构检查通过，公共 Web 基线只通过 `infra-web` 引入
- 验证命令：`JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test` 通过
- 验证命令：`git diff --check` 通过

---

## 2026-06-06 (迁移 infra-service-parent)

### 🎯 目标
- 将 OAuth2 POM 从直接继承 Spring Boot parent 迁移到 `infra-service-parent`，统一公共依赖和插件版本来源

### ✅ 完成
- pom: parent 切换为 `com.phil.infra:infra-service-parent:1.0-SNAPSHOT`，删除 `infra.version` 和内部依赖显式版本 (`pom.xml:7`, `pom.xml:47`, `pom.xml:51`)
- deps: `infra-web`、`infra-tracing-starter` 删除显式版本，Authorization Server 等职责型依赖继续保留在本服务 POM (`pom.xml:35`)
- spec: 新增 POM 迁移 spec 并同步索引 (`specs/service-parent-migration.spec.md:1`, `specs/README.md:19`)
- docs: README、AGENTS 同步服务父 POM 基线；保留已有本地 Maven/JDK 环境补充 (`README.md:8`, `AGENTS.md:39`)
- hygiene: `.gitignore` 增加 `logs/`，避免测试生成的日志目录进入提交范围 (`.gitignore:5`)

### 🚧 进行中 / 未完成
- 登录、token 签发、客户端管理等业务能力仍未实现

### 📌 下次继续
- 继续按新 spec 实现 OAuth2 第一批认证鉴权能力

### 💡 记录
- RED：POM 结构检查失败，当前 parent 为 `spring-boot-starter-parent`
- GREEN：POM 结构检查通过，parent 已切到 `infra-service-parent`，内部依赖未声明版本
- 验证命令：`JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test` 通过
- 验证命令：`git diff --check` 通过

---

## 2026-06-03 (项目初始化)

### 🎯 目标
- 初始化 infra-oauth2 基础骨架，并接入统一微服务目录结构与 vibe coding 文档体系

### ✅ 完成
- skeleton: 创建 Maven POM、Spring Boot 启动类、`application.yml`、测试骨架和标准包目录 (`pom.xml:1`, `src/main/java:1`, `src/main/resources/application.yml:1`)
- docs: 创建 AGENTS、README、架构说明、目录结构说明、spec 索引和项目初始化 spec (`AGENTS.md:1`, `README.md:1`, `docs/architecture/overview.md:1`, `docs/conventions/directory-structure.md:1`, `specs/project-bootstrap.spec.md:1`)

### 🚧 进行中 / 未完成
- 当前仅完成基础骨架；具体业务能力还未实现

### 📌 下次继续
- 按 `specs/project-bootstrap.spec.md` 的后续计划继续补齐第一批业务能力 spec

### 💡 记录
- 目录结构按 `infra-genesis/docs/conventions/microservice-directory-structure.md` 落地
- 微服务保留 `application.yml`，环境差异配置后续交给 Nacos Config / K8s ConfigMap / Secret / 环境变量
- 验证命令：`JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q -N validate` 通过
- 验证命令：`JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test` 通过
- 首次 `mvn test` 因本地未安装 `infra-genesis` SNAPSHOT 依赖失败；执行 `infra-genesis` 本地 `mvn -q -DskipTests install` 后通过
