# infra-oauth2

统一认证鉴权服务，负责登录、token 签发/刷新/撤销、客户端管理、用户身份解析和 Gateway 内部鉴权接口。

## 本项目硬规则(违反直接指正)

1. 写代码前:先查 `specs/<feature>.spec.md`,没有先按 `.codex/prompts/new-spec.md` 建
2. 完成一个 Task:立即勾 ✅ 并在任务行末尾标注代码位置(文件:行号)
3. 选型讨论超过 1 小时 → 必须写 ADR 再继续(`docs/decisions/NNNN-<topic>.md`)
4. session 结束前:在 `WORKLOG.md` 顶部追加一段
5. 改动某功能代码:顺手同步对应 spec 的 Tasks 状态
6. 相对时间转绝对日期:"昨天" → "YYYY-MM-DD"
7. P0/P1 改动必须按 `docs/conventions/risk-levels.md` 扩大验证并执行 session-close
8. 完成功能前必须记录验证命令;无法验证要写明原因和替代检查

## 技术栈

Java 21 + Spring Boot 3.5.14 + Spring Security Authorization Server + Spring MVC + Maven

## 核心架构

详见 `docs/architecture/overview.md`。

## 项目结构

本服务按 `docs/conventions/directory-structure.md` 维护目录结构，标准来源为 `infra-genesis/docs/conventions/microservice-directory-structure.md`。

## 📊 实现状态总览(2026-06-03)

### ✅ 已实现
| 模块 | 关键位置 | 说明 |
|------|----------|------|
| 项目骨架 | `pom.xml`, `src/main/java` | 已创建 OAuth2 认证鉴权服务 基础 Maven / Spring Boot 骨架 |
| 协作文档 | `specs/`, `docs/`, `WORKLOG.md` | 已接入 vibe coding 文档体系 |

### 🚧 进行中
| 项 | 原因 / 阻塞 |
|----|-------------|
| 业务能力实现 | 当前仅完成项目骨架，具体能力需按 spec 继续 |

### ⬜ 计划中
| 项 | spec 文件 / 预期 |
|----|-------------------|
| 具体业务能力 | 后续按 `specs/<feature>.spec.md` 逐项实现 |

### ❌ 已归档 / 放弃
| 项 | 去向 |
|----|------|
| _(暂无)_ |  |

## 📚 文档入口

| 位置 | 作用 | 何时查 |
|------|------|--------|
| `specs/README.md` | spec 索引表 | 做某个功能前 |
| `docs/decisions/README.md` | ADR 索引 | 遇到"为什么这么设计" |
| `docs/architecture/overview.md` | 架构事实表 | 判断模块边界和核心链路 |
| `docs/conventions/directory-structure.md` | 本服务目录结构 | 初始化或调整包结构前 |
| `docs/conventions/verification.md` | 验证入口 | 改完代码准备测试时 |
| `docs/conventions/testing.md` | 测试策略 | 判断该补什么测试时 |
| `docs/conventions/risk-levels.md` | 变更风险等级 | 判断是否要扩大检索和验证时 |
| `docs/alignments/` | 跨团队对齐 | 接外部契约前 |
| `WORKLOG.md` | 最近 session 进度 | 每次开场扫顶部 3 段 |

## Definition of Done

- 代码实现已完成,对应 spec Task 已勾选并标注关键代码位置
- 验证命令已运行并记录;无法运行时已写明原因和替代检查
- `specs/README.md`、`AGENTS.md` 实现状态、`WORKLOG.md` 已同步
- 需要 ADR 的决策已写入 `docs/decisions/`

## 开发约定

- **命名**: Java 标准命名;接口字段统一 camelCase
- **配置**: 统一 `@ConfigurationProperties`，禁止 `@Value`
- **测试**: 单测和源文件对称目录;集成测试不 mock 关键依赖(DB/MQ/缓存)
- **提交**: `<type>(<scope>): <subject>` 格式(feat/fix/refactor/docs/test/chore);高频小 commit
- **Secrets**: `.env` 和 `application-local.yml` 进 `.gitignore`;`.env.example` 提供模板

## AI 协作上下文目录

新 session 先扫这几个位置:

1. 本文件(每次必读)
2. `WORKLOG.md` 顶部 3 段(最近在折腾什么)
3. `specs/README.md`(所有功能的状态一览)
4. 做具体功能时才读对应 `specs/<feature>.spec.md`
