---
description: 把已有项目迁移到 Codex 版 vibe coding 规范,保留现状并补齐可持续迭代骨架
---

用于老项目第一次接入 Codex 版 vibe coding。目标是低风险迁移,不强行重写已有文档和历史功能。

## 执行步骤

1. **扫描现状**
   - 检查 `AGENTS.md` / `CLAUDE.md` / `WORKLOG.md`
   - 检查 `.codex/` / `.claude/`
   - 检查 `specs/` / `docs/decisions/` / `doc/` / `docs/`
   - 检查 git 状态,确认是否有未提交改动

2. **制定迁移策略**
   - 如果已有 `AGENTS.md`:默认保留,只补缺失章节
   - 如果只有 `CLAUDE.md`:备份为 `CLAUDE.md.bak`,提炼迁移到 `AGENTS.md`
   - 如果 `.claude/` 存在:不删除,只把仍有价值的命令/说明迁到 `.codex/prompts/`
   - 如果 `doc/` 和 `docs/` 并存:建议统一到 `docs/`,先询问用户再移动

3. **补齐骨架**
   - 确保存在 `AGENTS.md`
   - 确保存在 `WORKLOG.md`
   - 确保存在 `specs/README.md` 和 `specs/_template.spec.md`
   - 确保存在 `docs/decisions/README.md` 和 `docs/decisions/_template.md`
   - 确保存在 `.codex/hooks.json`
   - 确保存在 `.codex/prompts/new-spec.md`
   - 确保存在 `.codex/prompts/new-adr.md`
   - 确保存在 `.codex/prompts/iterate-feature.md`
   - 确保存在 `.codex/prompts/session-close.md`
   - 确保存在 `.codex/prompts/migrate-existing-project.md`
   - 确保存在 `.codex/prompts/status-sync.md`
   - 确保存在 `.codex/prompts/code-review.md`
   - 确保存在 `docs/architecture/overview.md`
   - 确保存在 `docs/conventions/verification.md`
   - 确保存在 `docs/conventions/testing.md`
   - 确保存在 `docs/conventions/risk-levels.md`

4. **处理历史功能**
   - 不给所有历史功能补完整 spec
   - 只给仍在迭代、将要修改、风险较高的功能补 spec
   - 已稳定功能登记到 `AGENTS.md` 的实现状态总览即可
   - 迁移过程中发现的待确认问题写入 `WORKLOG.md`

5. **处理旧 Claude 规范**
   - `CLAUDE.md` 中稳定规则迁到 `AGENTS.md`
   - Claude 专用工具名改成 Codex 表述
   - `.claude/settings.json` 的提醒逻辑迁到 `.codex/hooks.json`
   - Claude slash command 文档迁到 `.codex/prompts/*.md`

6. **收尾**
   - 在 `WORKLOG.md` 顶部追加迁移记录
   - 输出已迁移、已跳过、需用户确认的列表
   - 建议用户随后跑一次 `vibe-check`

## 迁移原则

- 默认不覆盖已有内容
- 默认不删除 `.claude/` 和 `CLAUDE.md`,除非用户明确要求
- 优先提炼长期有效规则,不要搬运历史聊天记录
- 老项目迁移不是补历史账,重点是让下一次迭代可持续
