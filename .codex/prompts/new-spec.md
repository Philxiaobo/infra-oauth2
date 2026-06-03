---
description: 从模板创建新功能 spec 并更新 specs/README.md 索引表
---

基于 `specs/_template.spec.md` 创建新功能 spec。

**参数**: $ARGUMENTS(功能名,kebab-case,如 user-login)

## 执行步骤

1. **前置校验**
   - `$ARGUMENTS` 为空 → 问用户功能名,不要凭空编
   - 转 kebab-case(空格/下划线→短横线,全小写,去非字母数字)
   - `specs/` 不存在 → 告知"先用 vibe-init 铺骨架",停止
   - `specs/_template.spec.md` 不存在 → 告知模板缺失,提示重跑 vibe-init,停止
   - `specs/$ARGUMENTS.spec.md` 已存在 → 停止,提示用户直接编辑现有文件

2. **读模板并替换占位符**
   - 读 `specs/_template.spec.md`
   - 替换:
     - 第一行的 `<功能名>` → `$ARGUMENTS`
     - `{{DATE}}` → `date +%Y-%m-%d`
     - `{{OWNER}}` → `git config user.name`,取不到留 `—`
   - 写入 `specs/$ARGUMENTS.spec.md`

3. **更新索引表**
   - 读 `specs/README.md`
   - 定位索引表(表头含 `| spec | 状态 |`)
   - 如果只有 `_(暂无)_` 占位行 → 替换为新行
   - 否则在表末追加:
     ```
     | [$ARGUMENTS](./$ARGUMENTS.spec.md) | ⬜ Draft | {{OWNER}} | {{DATE}} | — |
     ```

4. **引导用户**
   - 告知文件路径
   - 直接问用户:"现在就填'1. 背景与目标'吗?"
     - 是 → 追问简要背景,写入 spec 第 1 节
     - 否 → 提示手动填,对齐后把状态改 In Progress

## 注意

- **不猜需求**:空模板留给用户填,不要 AI 凭空编造功能描述
- **不自动改状态**:新建一律 Draft,流转靠实际进度
- **不建代码文件**:只建文档,实现走后续讨论
