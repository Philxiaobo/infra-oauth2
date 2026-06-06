# infra-oauth2 Kubernetes 配置

本目录按 `infra-genesis/deploy/k8s` 和 `infra-genesis/docs/conventions/microservice-deployment-standard.md` 落地 `infra-oauth2` 的云原生部署配置。

默认基线：

- 服务发现：Kubernetes Service + DNS
- 配置交付：ConfigMap / Secret + Spring Boot `configtree`
- 发布方式：GitOps / Deployment rollout
- 不默认引入 Nacos Discovery / Nacos Config

## 文件说明

| 文件 | 用途 |
|------|------|
| `configmap.yaml` | 非敏感环境差异配置 |
| `secret.example.yaml` | Secret 占位模板，不提交真实密钥 |
| `deployment.yaml` | Pod、镜像、探针、资源、Downward API、ConfigMap / Secret 挂载 |
| `service.yaml` | ClusterIP Service 和 Kubernetes DNS 入口 |
| `kustomization.yaml` | 最小 kustomize 聚合入口 |

## 服务替换项

| 项 | 当前值 | 上线前要求 |
|----|--------|------------|
| Service name | `infra-oauth2` | 已替换 |
| Namespace | `infra` | 按目标环境确认 |
| Part of | `infra-platform` | 按平台归属确认 |
| Image | `com.phil/infra-oauth2:1.0.0` | 替换为真实 registry/tag |
| Container port | `9000` | 与 `server.port` 保持一致 |
| Config version | `2026-06-06-001` | 配置变更 rollout 时递增 |

## ConfigMap key

| Key | 说明 |
|-----|------|
| `server.port` | 应用 HTTP 端口 |
| `infra.oauth2.issuer` | OAuth2 issuer，生产应使用稳定外部域名 |
| `infra.oauth2.token.audiences` | JWT audience 列表 |
| `management.tracing.sampling.probability` | tracing 采样率 |
| `management.otlp.tracing.endpoint` | OTLP tracing endpoint |
| `infra.tracing.enabled` | tracing 开关 |
| `infra.log.enabled` | 访问日志开关 |
| `infra.log.slow-request-ms` | 慢请求阈值 |
| `infra.cors.enabled` | CORS 开关 |
| `infra.xss.enabled` | XSS 防护开关 |

## Secret key

`secret.example.yaml` 只声明占位 key，不提交真实值。真实 Secret 由 External Secrets、Sealed Secrets、Vault、云厂商 Secret Manager 或部署平台生成。

| Key | 说明 |
|-----|------|
| `spring.datasource.username` | 后续 JDBC 持久化数据库用户名 |
| `spring.datasource.password` | 后续 JDBC 持久化数据库密码 |
| `spring.data.redis.password` | 后续 Redis 密码 |
| `infra.oauth2.clients.1.client-secret` | 当前 seed service client secret 占位 |
| `infra.oauth2.users.0.password` | 当前 seed admin user password 占位 |
| `infra.oauth2.jwt.private-key` | 后续生产 JWT 私钥托管占位 |

## 本地验证

只检查 YAML 与 kustomize 聚合：

```bash
ruby -e "require 'yaml'; Dir['deploy/k8s/*.yaml'].each { |path| YAML.safe_load(File.read(path), aliases: false) }"
ruby -e "require 'yaml'; require 'pathname'; k = YAML.safe_load(File.read('deploy/k8s/kustomization.yaml')); missing = k.fetch('resources').reject { |r| Pathname('deploy/k8s').join(r).file? }; abort('missing resources: ' + missing.join(', ')) unless missing.empty?"
```

如本机安装 `kubectl`，可额外执行：

```bash
kubectl kustomize deploy/k8s
```

应用到本地 Kubernetes 前先确认镜像可拉取：

```bash
kubectl create namespace infra
kubectl apply -k deploy/k8s
kubectl -n infra rollout status deployment/infra-oauth2
kubectl -n infra get service infra-oauth2
```
