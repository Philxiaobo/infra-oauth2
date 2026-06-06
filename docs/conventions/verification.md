# 验证入口

本文件记录项目可执行的验证命令。每次功能改动后,优先运行与改动直接相关的最小验证集。

## 快速验证

| 场景 | 命令 | 说明 |
|------|------|------|
| 代码格式 / lint | `git diff --check` | 检查补丁空白问题 |
| Kubernetes YAML 语法 | `ruby -e "require 'yaml'; Dir['deploy/k8s/*.yaml'].each { |path| YAML.safe_load(File.read(path), aliases: false) }"` | 检查部署清单 YAML 可解析 |
| Kustomize 资源引用 | `ruby -e "require 'yaml'; require 'pathname'; k = YAML.safe_load(File.read('deploy/k8s/kustomization.yaml')); missing = k.fetch('resources').reject { |r| Pathname('deploy/k8s').join(r).file? }; abort('missing resources: ' + missing.join(', ')) unless missing.empty?"` | 检查 kustomization resources 文件存在 |
| 单元测试 | `JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q -Dtest=<ClassName> test` | 支持指定测试类 |
| 集成测试 | `<待补充>` | 需要说明依赖的 DB/MQ/缓存 |
| 构建 | `JAVA_HOME=/Users/photonpay/software/jdk/jdk-21.0.10.jdk/Contents/Home mvn -q test` | CI 前置检查 |
| 本地启动 | `<待补充>` | 启动后访问路径 / 健康检查 |

## 精准测试

| 技术栈 | 指定模块 | 指定用例 | 备注 |
|--------|----------|----------|------|
| Maven | `mvn test` | `mvn -Dtest=<ClassName>#<method> test` | 单模块服务 |
| npm | `npm test -- <pattern>` | `<待补充>` | 按测试框架调整 |

## 慢验证

| 命令 | 何时运行 | 平均耗时 | 依赖 |
|------|----------|----------|------|
| `<待补充>` | 发布前 / 大改动后 | `<待补充>` | `<待补充>` |

## 无法验证时的记录格式

```markdown
未运行验证: <命令>
原因: <缺依赖 / 环境不可用 / 耗时过长 / 与本改动无关>
替代检查: <已做的静态检查、代码审阅或手工验证>
```
