---
description: 创建新的 ADR(架构决策记录),自动算编号并更新索引
---

从 `docs/decisions/_template.md` 建新 ADR。

**参数**: $ARGUMENTS(决策主题,kebab-case,如 use-spring-boot / switch-to-sse)

## 执行步骤

1. **前置校验**
   - `$ARGUMENTS` 为空 → 问用户决策主题,不要凭空编
   - 转 kebab-case(空格/下划线→短横线,全小写,去非字母数字)
   - `docs/decisions/` 不存在 → 告知"先用 vibe-init 铺骨架",停止
   - `docs/decisions/_template.md` 不存在 → 告知模板缺失,提示重跑 vibe-init,停止

2. **算下一个编号**
   - 用 `find docs/decisions -maxdepth 1 -name '*.md'` 或 `rg --files docs/decisions`,排除 `README.md` 和 `_template.md`
   - 提取文件名前 4 位数字前缀(格式 `NNNN-xxx.md`),找最大值
   - 没有已存在 ADR → 从 `0001` 开始
   - 下一个编号 = max + 1,格式化为 4 位补零(如 `0003`)
   - 忽略不符合 `NNNN-*.md` 命名的文件(不纳入计数)

3. **读模板并替换占位符**
   - 读 `docs/decisions/_template.md`
   - 替换:
     - 第一行 `ADR-NNNN: <决策标题>` → `ADR-<编号>: $ARGUMENTS`
     - `{{DATE}}` → `date +%Y-%m-%d`
     - `{{OWNER}}` → `git config user.name`,取不到留 `—`
   - 写入 `docs/decisions/<编号>-$ARGUMENTS.md`

4. **更新索引表**
   - 读 `docs/decisions/README.md`
   - 定位索引表(表头含 `| 编号 | 标题 |`)
   - 如果只有 `_(暂无)_` 占位 → 替换为新行
   - 否则在表末追加:
     ```
     | [ADR-<编号>](./<编号>-$ARGUMENTS.md) | $ARGUMENTS | Proposed | {{DATE}} |
     ```

5. **引导用户**
   - 告知文件路径和编号
   - 直接问用户:"现在就填 Context / Decision 两段核心内容吗?"
   - 提醒:初始状态是 Proposed,决策通过后改 Accepted;有关联 spec 的话更新 spec 顶部的"依赖 ADR"字段

## 注意

- **不猜备选方案**:Alternatives 留空,避免编造没讨论过的选项
- **编号不可复用**:废弃的 ADR 也保留编号,状态改 Deprecated
- **一个 ADR 一个决策**:不要一个文件塞多个
