---
description: 扫描代码、spec、AGENTS.md、WORKLOG 的状态一致性,输出差异和修复建议
---

用于每周、重要功能完成后、或怀疑规范漂移时。默认只读,除非用户明确要求修复。

## 执行步骤

1. **读取项目索引**
   - 读 `AGENTS.md`
   - 读 `WORKLOG.md` 顶部 3 段
   - 读 `specs/README.md`
   - 读 `docs/decisions/README.md`

2. **扫描 spec 状态**
   - 列 `specs/*.spec.md`,排除 `_template.spec.md`
   - 提取每个 spec 的状态、Tasks 勾选情况、最近变更记录
   - 检查 `specs/README.md` 是否索引完整、状态是否一致

3. **扫描代码实际状态**
   - 根据 spec 中标注的关键路径检查文件是否存在
   - 对已标记 Implemented 的 spec,抽查对应入口、测试或配置是否存在
   - 对 In Progress 的 spec,确认 WORKLOG 是否有未完成事项

4. **扫描 AGENTS.md**
   - 检查实现状态总览是否包含已实现 / 进行中 / 计划中 / 归档
   - 检查 AGENTS.md 是否超过 200 行
   - 检查是否把详细实现塞进 AGENTS.md

5. **输出报告**
   - 列出 OK、警告、严重不一致
   - 每项给出建议修复位置
   - 不自动修改文件,除非用户明确要求

## 不一致示例

- spec 标记 Implemented,但 Tasks 未全部完成
- spec 已完成,但 `specs/README.md` 仍是 Draft
- AGENTS.md 写某模块已实现,但 spec 仍 In Progress
- WORKLOG 提到下次继续,但没有对应 spec 任务
- spec 中标注的代码路径不存在

## 输出格式

```markdown
# status-sync 报告 — {{DATE}}

## 严重
- ...

## 警告
- ...

## OK
- ...

## 建议修复顺序
1. ...
```
