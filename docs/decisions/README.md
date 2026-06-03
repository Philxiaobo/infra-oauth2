# Architecture Decision Records (ADR)

重大决策的档案库。目的:**未来任何人(或 AI)问"为什么这么选"时,这里有答案**。

## 什么时候写 ADR

- 选型决策(用 X 不用 Y)
- 架构模式(SSE vs WebSocket、同步 vs 异步)
- 核心约定(字段命名全 camelCase、错误码分段)
- 放弃某功能
- **任何讨论超过 1 小时的选择**

## 什么时候不用

- 普通 bug 修复
- 小重构
- 实现细节

## 索引表

| 编号 | 标题 | 状态 | 日期 |
|------|------|------|------|
| _(暂无)_ |  |  |  |

## 约定

- 编号递增,`NNNN-<topic>.md`(如 `0001-use-spring-boot.md`)
- 废弃的 ADR 状态改 Deprecated,指向新 ADR 编号,**不删文件**
- 新 ADR 要配合更新受影响的 spec
