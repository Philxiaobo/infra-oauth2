---
description: 老项目或已有功能的标准迭代流程:恢复上下文、定位 spec、实施改动、同步状态
---

用于每次功能迭代开始前。目标是让本轮改动能落到 spec / AGENTS.md / WORKLOG.md,下一次 session 可以继续。

**参数**: $ARGUMENTS(功能名或问题描述,如 payment-retry / 修复登录超时)

## 执行步骤

1. **恢复上下文**
   - 读 `AGENTS.md`
   - 读 `WORKLOG.md` 顶部最近 3 段
   - 读 `specs/README.md`
   - 如果涉及架构/选型,读 `docs/decisions/README.md`

2. **定位本轮对应 spec**
   - 根据 `$ARGUMENTS` 在 `specs/README.md` 和 `specs/*.spec.md` 中找已有 spec
   - 找到唯一匹配:读取该 spec,确认状态和未完成 Tasks
   - 找到多个匹配:列出候选并询问用户选哪个
   - 未找到:询问用户是否按 `.codex/prompts/new-spec.md` 创建新 spec

3. **确认本轮范围**
   - 用 3-6 条 bullet 总结本轮要做和不做的事
   - 如果需求不清,先问清楚,不要直接猜实现
   - 如果改动涉及重大选型、外部契约或超过 1 小时讨论,先按 `.codex/prompts/new-adr.md` 建 ADR

4. **执行改动**
   - 按 spec 的 Tasks 实施
   - 每完成一个 Task,立即在 spec 中打勾并标注关键代码位置
   - 新增任务或发现风险时,追加到 spec 的 Tasks / 风险与待决问题

5. **验证**
   - 运行与本轮改动直接相关的测试、lint 或手工验证
   - 无法运行时,在最终说明和 WORKLOG 中写清原因

6. **同步状态**
   - 更新 `specs/README.md` 中该 spec 的状态和更新时间
   - 如功能状态变化,更新 `AGENTS.md` 的实现状态总览
   - 按 `.codex/prompts/session-close.md` 收尾

## 输出要求

- 说明本轮对应的 spec 文件
- 列出完成的 Tasks 和未完成事项
- 列出验证命令及结果
- 提醒是否已完成 session-close

## 注意

- 不要为已经稳定且本轮不改的老功能补完整 spec
- 不要把实现细节塞进 AGENTS.md,实现细节进 spec
- 需求、状态、验证结果必须写绝对日期,不写"今天/昨天"
