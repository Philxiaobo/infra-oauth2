# 本地 ConfigMap / Secret 模拟目录

本目录用于本地开发时模拟 SRE / 平台在 Kubernetes 中挂载出来的 ConfigMap 和 Secret。

Spring Boot `configtree` 读取的是目录中的文件，不是 `deploy/k8s/configmap.yaml` 或 `deploy/k8s/secret.example.yaml` 本身。

## 目录结构

```text
deploy/local/
├── config/    # 模拟 ConfigMap，放非敏感配置
└── secrets/   # 模拟 Secret，放本地敏感配置；真实值不提交
```

## application-local.yml 示例

```yaml
spring:
  config:
    import:
      - optional:configtree:./deploy/local/config/
      - optional:configtree:./deploy/local/secrets/
```

## 示例

```bash
printf '19000' > deploy/local/config/server.port
printf 'http://localhost:9000' > deploy/local/config/infra.oauth2.issuer
printf '{bcrypt}<replace-with-bcrypt-hash>' > deploy/local/secrets/infra.oauth2.users.0.password
```

这些文件会被读取为：

```properties
server.port=19000
infra.oauth2.issuer=http://localhost:9000
infra.oauth2.users.0.password={bcrypt}<replace-with-bcrypt-hash>
```

`deploy/local/secrets/` 已通过 `.gitignore` 忽略，禁止提交真实密钥。
