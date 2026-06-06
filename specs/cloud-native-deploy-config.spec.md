# Cloud Native Deploy Config — Kubernetes 配置交付

> **状态**: Implemented
> **负责人**: Phil
> **创建日期**: 2026-06-06
> **最后更新**: 2026-06-06
> **依赖 ADR**: `infra-genesis/docs/decisions/0003-use-kubernetes-native-discovery-config.md`
> **依赖 spec**: `oauth2-authorization-server-mvp`

## 1. 背景与目标(Why)

`infra-oauth2` 是可运行的统一认证鉴权服务，后续需要按整套微服务云原生基线部署到 Kubernetes。`infra-genesis` 已定义默认注册配置方案：Kubernetes Service/DNS + ConfigMap / Secret + Spring Boot `configtree` + GitOps，不默认引入 Nacos Discovery / Nacos Config。

本轮目标是参考 `/Users/photonpay/work/github/infra-genesis/deploy` 和 `microservice-deployment-standard.md`，在当前服务仓库落地 `deploy/k8s` 与 `deploy/local` 配置文件，并补齐 Spring Boot configtree 配置入口。

## 2. 需求范围(What)

### 2.1 功能需求
- [x] ✅ 新增 `deploy/k8s`，包含 README、ConfigMap、Secret example、Deployment、Service、Kustomization
- [x] ✅ 新增 `deploy/local`，用于本地模拟 ConfigMap / Secret 挂载目录
- [x] ✅ 将模板中的 `infra-service` 替换为 `infra-oauth2`
- [x] ✅ Deployment 使用端口 `9000`，健康检查指向 actuator readiness / liveness
- [x] ✅ ConfigMap 只放非敏感配置，包括 issuer、tracing、OTLP、CORS、XSS 和 token audience
- [x] ✅ Secret example 只声明占位 key，不提交真实密钥
- [x] ✅ `application.yml` 增加 Kubernetes configtree import

### 2.2 非目标(Non-goals)

- 本轮不实现 Helm Chart、Argo CD Application、Ingress、HPA、PDB 或 NetworkPolicy
- 本轮不提交真实 Secret，不创建生产 namespace，不执行 `kubectl apply`
- 本轮不把 seed client/user 持久化到数据库
- 本轮不引入 Nacos Discovery / Nacos Config

## 3. 设计方案(How)

### 3.1 整体流程

```text
application.yml
  + optional:configtree:/etc/infra/config/
  + optional:configtree:/etc/infra/secrets/

deploy/k8s/configmap.yaml
deploy/k8s/secret.example.yaml
        |
        v
Kubernetes volume mount
        |
        v
/etc/infra/config/ + /etc/infra/secrets/
        |
        v
Spring Boot Environment
```

### 3.2 关键文件 / 目录结构

- `deploy/k8s/configmap.yaml`: 非敏感环境差异配置
- `deploy/k8s/secret.example.yaml`: Secret key 占位模板
- `deploy/k8s/deployment.yaml`: Pod、镜像、探针、资源、Downward API、configtree 挂载
- `deploy/k8s/service.yaml`: ClusterIP Service 和 Kubernetes DNS 入口
- `deploy/k8s/kustomization.yaml`: 最小 kustomize 聚合入口
- `deploy/local/`: 本地模拟 ConfigMap / Secret 的挂载目录
- `src/main/resources/application.yml`: 默认公开配置和 configtree import

### 3.3 配置模型

- `application.yml` 保留应用名、默认端口、actuator、tracing 默认值和 configtree import
- `ConfigMap` 放非敏感环境差异配置，例如 `infra.oauth2.issuer`、OTLP endpoint、采样率、开关项
- `Secret example` 放敏感配置占位，例如 datasource、Redis、seed client/user、JWT 私钥
- `Deployment env` 只放 `POD_NAMESPACE`、`POD_IP`、`SPRING_CONFIG_IMPORT` 等平台运行时信息

### 3.4 接口契约

- Kubernetes Service 名称为 `infra-oauth2`
- 默认 namespace 为 `infra`
- 集群内 DNS 可使用 `http://infra-oauth2` 或 `http://infra-oauth2.infra.svc.cluster.local`
- HTTP Service 端口为 `80`，转发到容器 `http:9000`

### 3.5 错误 & 降级

- ConfigMap / Secret 挂载路径使用 `optional:configtree:`，本地缺失目录不影响启动
- Secret 资源在 Deployment 中配置 `optional: true`，便于开发环境先启动基础能力
- 配置变更默认通过 GitOps / Deployment rollout 生效，不启用 Spring Cloud Kubernetes Config reload
- 镜像 tag 和 issuer 域名是环境替换项，上线前必须由 SRE / 平台确认

## 4. 验收标准(Verify)

### 4.1 功能用例
- [x] `deploy/k8s` 必备文件齐全
- [x] 所有 `infra-service` 占位值替换为 `infra-oauth2`
- [x] Deployment 注入 `POD_NAMESPACE` 和 `POD_IP`
- [x] Deployment 挂载 ConfigMap 到 `/etc/infra/config/`
- [x] Deployment 挂载 Secret 到 `/etc/infra/secrets/`
- [x] readiness/liveness probe 指向 actuator 端点
- [x] Secret example 不包含真实密钥
- [x] `application.yml` 包含 configtree import

### 4.2 性能 / 非功能
- 默认副本数为 2
- RollingUpdate `maxUnavailable: 0`
- 资源 requests / limits 按基础模板设置，可由 SRE 后续容量评估调整

### 4.3 测试清单
- [x] `git diff --check`
- [x] Ruby YAML parse 检查 `deploy/k8s/*.yaml`
- [x] Ruby kustomization resources 存在性检查
- [x] 静态检查 `deploy/k8s` 不含 `infra-service`
- [x] `JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test`

未运行验证: `kubectl kustomize deploy/k8s`
原因: 本机未安装 `kubectl`
替代检查: 已执行 Ruby YAML 解析和 kustomization resources 存在性检查

## 5. 任务拆分(Tasks)

> 状态:✅ 已完成 / 🚧 进行中 / ⬜ 未开始

- [x] ✅ T1: 新增云原生部署 spec 并同步索引(`specs/cloud-native-deploy-config.spec.md:1`, `specs/README.md:19`)
- [x] ✅ T2: 新增 `deploy/k8s` Kubernetes 清单(`deploy/k8s/deployment.yaml:1`, `deploy/k8s/configmap.yaml:1`)
- [x] ✅ T3: 新增 `deploy/local` 本地 configtree 模拟目录(`deploy/local/README.md:1`, `deploy/local/.gitignore:1`)
- [x] ✅ T4: `application.yml` 增加 configtree import(`src/main/resources/application.yml:4`)
- [x] ✅ T5: 执行 YAML、占位符、补丁和 Maven 验证(`docs/conventions/verification.md:10`)
- [x] ✅ T6: session-close 同步 AGENTS / WORKLOG(`AGENTS.md:38`, `WORKLOG.md:7`)

## 6. 风险与待决问题

- ⚠️ 风险等级: P1。部署清单影响云原生上线、配置读取和 Secret 边界。
- ❓ 镜像仓库地址 `com.phil/infra-oauth2:1.0.0` 需要按实际 CI/CD registry 替换。
- ❓ `infra.oauth2.issuer` 生产域名需要由网关/入口域名确定。
- ❓ 后续 JDBC、Redis、JWT 私钥托管落地后，需要同步收敛 Secret key。
- ↩️ 回滚思路：删除 `deploy/` 目录并移除 `application.yml` configtree import，不影响当前本地默认启动配置。

## 7. 变更日志

| 日期 | 变更 | 作者 |
|------|------|------|
| 2026-06-06 | 初稿并完成 infra-oauth2 云原生部署配置落地 | Phil |
