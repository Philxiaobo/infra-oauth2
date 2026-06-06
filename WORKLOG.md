# Worklog

每次 session 的进度。跨 session 保留上下文,**每次结束前在顶部追加一段**。

---

## 2026-06-06 (云原生部署配置)

### 🎯 目标
- 参考 `/Users/photonpay/work/github/infra-genesis/deploy` 和微服务部署规范，为 `infra-oauth2` 建立 Kubernetes ConfigMap / Secret / Deployment / Service 配置文件

### ✅ 完成
- spec: 新增并完成 `cloud-native-deploy-config`，明确 P1 风险、非目标、回滚思路和验证范围 (`specs/cloud-native-deploy-config.spec.md:1`, `specs/README.md:19`)
- deploy: 新增 `deploy/k8s`，包含 ConfigMap、Secret example、Deployment、Service、Kustomization 和说明文档 (`deploy/k8s/deployment.yaml:1`, `deploy/k8s/configmap.yaml:1`, `deploy/k8s/README.md:1`)
- local: 新增 `deploy/local` 本地 configtree 模拟目录，Secret 本地值通过 `.gitignore` 忽略 (`deploy/local/README.md:1`, `deploy/local/.gitignore:1`)
- config: `application.yml` 增加 Kubernetes configtree import，默认读取 `/etc/infra/config/` 和 `/etc/infra/secrets/` (`src/main/resources/application.yml:4`)
- docs: 同步 specs 索引、AGENTS 状态、README 部署入口和验证命令 (`AGENTS.md:38`, `README.md:37`, `docs/conventions/verification.md:10`)

### 🚧 进行中 / 未完成
- `kubectl kustomize deploy/k8s` 未运行：本机未安装 `kubectl`
- 镜像地址 `com.phil/infra-oauth2:1.0.0` 仍是环境替换项，需接入 CI/CD registry 后确认
- 生产 `infra.oauth2.issuer` 域名需由 Gateway / Ingress / SRE 平台最终确认

### 📌 下次继续
- 接入真实镜像仓库和 GitOps 环境 overlay
- 结合 JDBC / Redis / JWT 私钥托管 spec，收敛 Secret key
- 如需要公网访问，补 Ingress / Gateway 路由 spec

### 💡 记录
- 验证命令：`git diff --check` 通过
- 验证命令：`ruby -e "require 'yaml'; Dir['deploy/k8s/*.yaml'].each { |path| YAML.safe_load(File.read(path), aliases: false) }"` 通过
- 验证命令：`ruby -e "require 'yaml'; require 'pathname'; k = YAML.safe_load(File.read('deploy/k8s/kustomization.yaml')); missing = k.fetch('resources').reject { |r| Pathname('deploy/k8s').join(r).file? }; abort('missing resources: ' + missing.join(', ')) unless missing.empty?"` 通过
- 验证命令：`rg -n "infra-service" deploy/k8s deploy/local` 无匹配，确认模板占位值已替换
- 验证命令：`JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test` 通过；仅出现既有 Mockito / ByteBuddy 动态 agent 警告

---

## 2026-06-06 (IAM/RBAC 表结构设计)

### 🎯 目标
- 按用户提供的 SQL Convention 设计基础 IAM/RBAC 用户权限体系表结构，并将 SQL 规范和脚本留存在项目文档目录

### ✅ 完成
- spec: 新增并完成 `rbac-schema`，明确 P0 风险、非目标、软删除唯一性策略、权限校验约定和回滚思路 (`specs/rbac-schema.spec.md:1`, `specs/README.md:19`)
- convention: 固化 SQL 建表、字段、注释、基础字段、软删除和脚本留存规范 (`docs/conventions/sql.md:1`)
- sql: 新增 IAM/RBAC 初始化 SQL，包含 `t_iam_user`、`t_iam_role`、`t_iam_permission`、`t_iam_user_role`、`t_iam_role_permission` 五张表 (`docs/sql/2026-06-06-init-rbac-schema.sql:13`)
- design: 所有跨表引用均使用业务 id，所有二值状态字段使用 `char(1)` 的 `Y/N`，软删除唯一性使用 `active_*_key` 生成列，表名前缀统一使用 `t_iam_` (`docs/sql/2026-06-06-init-rbac-schema.sql:5`)
- authz: 后续权限校验按默认拒绝、allow-only、scope / permission 分层、Gateway 粗粒度与服务侧细粒度边界设计 (`specs/rbac-schema.spec.md:82`)
- docs: AGENTS 增加 IAM/RBAC SQL 表结构状态和 SQL convention 文档入口 (`AGENTS.md:35`, `AGENTS.md:70`)

### 🚧 进行中 / 未完成
- Java 运行时代码: entity、mapper、repository、service 和权限查询链路尚未实现
- DB 实机验证: 本地未发现 `mysql` 客户端，本轮仅执行静态 SQL 检查
- 扩展权限模型: 组织架构、岗位、数据权限、ABAC / policy engine 暂未纳入基础 IAM/RBAC

### 📌 下次继续
- 新建 IAM/RBAC Java 持久化 spec，基于本 SQL 落 entity / mapper / repository
- 补充 MySQL 实例语法验证和 migration 执行验证
- 结合 Authorization Server token 生成，设计 `roles` / `scope` claim 从 RBAC 表解析的查询链路

### 💡 记录
- 验证命令：`git diff --check` 通过
- 验证命令：`rg -n "CREATE TABLE|COMMENT =|COMMENT '" docs/sql/2026-06-06-init-rbac-schema.sql` 通过，确认 5 张表和字段/表注释
- 验证命令：`rg -n "(tenant|user|role|permission|user_role|role_permission)_id.*bigint" docs/sql/2026-06-06-init-rbac-schema.sql` 无匹配，确认跨表业务 id 未使用 bigint 自增主键类型
- 验证命令：`rg -n "tinyint|FOREIGN KEY|REFERENCES" docs/sql/2026-06-06-init-rbac-schema.sql` 无匹配，确认未使用 tinyint 状态字段且未建物理外键
- 验证命令：`rg -n "t_rbac_" docs/sql/2026-06-06-init-rbac-schema.sql` 无匹配，确认 SQL 表名前缀已切换为 `t_iam_`

---

## 2026-06-06 (OAuth2 MVP 基线初始化)

### 🎯 目标
- 参考 `/Users/photonpay/Desktop/infra-gateway-oauth2-push-implementation.md`，为 infra-oauth2 初始化首批 Spring Authorization Server MVP 能力

### ✅ 完成
- spec: 新增并完成 `oauth2-authorization-server-mvp`，明确 P0/P1 范围、非目标、回滚思路和后续 JDBC/KMS 待决项 (`specs/oauth2-authorization-server-mvp.spec.md:1`, `specs/README.md:19`)
- oauth2: 启用 Authorization Server 安全链、OIDC metadata、JWK Set、JWT access token claim 定制、issuer 配置和默认 form login (`src/main/java/com/phil/infra/oauth2/config/AuthorizationServerSecurityConfiguration.java:43`, `src/main/resources/application.yml:30`)
- bootstrap: 初始化 browser public client、service confidential client、本地 seed user、in-memory authorization/consent 服务，并禁用 password / implicit grant (`src/main/java/com/phil/infra/oauth2/config/InfraOAuth2Properties.java:57`, `src/main/java/com/phil/infra/oauth2/config/OAuth2BootstrapConfiguration.java:37`)
- audit: 新增 OAuth2 安全审计事件、发布器和 Spring Security 认证事件监听 (`src/main/java/com/phil/infra/oauth2/audit/OAuth2AuditEvent.java:15`, `src/main/java/com/phil/infra/oauth2/audit/OAuth2SecurityAuditListener.java:22`)
- test: 新增 seed client/issuer/audit 测试和 OIDC/JWK 发现端点测试 (`src/test/java/com/phil/infra/oauth2/config/OAuth2BootstrapConfigurationTest.java:40`, `src/test/java/com/phil/infra/oauth2/config/OAuth2DiscoveryEndpointTest.java:23`)
- docs: README / AGENTS / verification / `.env.example` 同步 OAuth2 MVP 基线和本地覆盖入口 (`README.md:27`, `AGENTS.md:35`, `docs/conventions/verification.md:8`, `.env.example:1`)

### 🚧 进行中 / 未完成
- JDBC 持久化: registered client、authorization、consent 仍为 in-memory MVP，需确认 MySQL/PostgreSQL 与数据访问 starter 后单独实现
- 生产安全增强: JWK 轮换、KMS/Vault/Secret Manager 私钥托管、refresh token reuse 检测、client secret 轮换仍未实现
- 端到端授权码流程: 本轮验证 metadata/JWK 和配置约束，未做真实浏览器授权码回调联调

### 📌 下次继续
- 新建 JDBC 持久化 spec，落地 Spring Authorization Server 官方 schema 和 client/authorization/consent 存储
- 增加授权码 + PKCE、client_credentials、refresh token 的端到端接口测试
- 明确生产私钥托管与 JWK 轮换方案，必要时写 ADR

### 💡 记录
- RED：`OAuth2BootstrapConfigurationTest` 首次失败，缺少 `com.phil.infra.oauth2.audit` 与 OAuth2 配置实现
- GREEN：`JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q -Dtest=OAuth2BootstrapConfigurationTest test` 通过
- 验证命令：`JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q -Dtest=OAuth2BootstrapConfigurationTest,OAuth2DiscoveryEndpointTest test` 通过
- 验证命令：`JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test` 通过；当前 JDK 仍输出 Mockito / ByteBuddy 动态 agent 未来兼容警告
- 验证命令：`git diff --check` 通过

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
