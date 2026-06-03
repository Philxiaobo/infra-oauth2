# Worklog

每次 session 的进度。跨 session 保留上下文,**每次结束前在顶部追加一段**。

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
