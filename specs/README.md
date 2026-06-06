# Specs Index

所有功能 spec 的状态一览。新建 spec 时让 Codex 按 `.codex/prompts/new-spec.md` 流程创建 `<name>`,并更新此表。

## 状态说明

| 状态 | 含义 |
|------|------|
| ⬜ Draft | 需求刚建,未对齐 |
| 🚧 In Progress | 实现中 |
| ✅ Implemented | 已完成并验收通过 |
| 🟡 Partial | 部分实现(说明在 spec 顶部) |
| ❌ Archived | 已放弃/下线 |

## 索引表

| spec | 状态 | 负责人 | 最后更新 | 关联 ADR |
|------|------|--------|----------|----------|
| [cloud-native-deploy-config.spec.md](cloud-native-deploy-config.spec.md) | ✅ Implemented | Phil | 2026-06-06 | infra-genesis ADR-0003 |
| [rbac-schema.spec.md](rbac-schema.spec.md) | ✅ Implemented | Phil | 2026-06-06 | - |
| [oauth2-authorization-server-mvp.spec.md](oauth2-authorization-server-mvp.spec.md) | ✅ Implemented | Phil | 2026-06-06 | - |
| [web-dependency-cleanup.spec.md](web-dependency-cleanup.spec.md) | ✅ Implemented | Phil | 2026-06-06 | - |
| [service-parent-migration.spec.md](service-parent-migration.spec.md) | ✅ Implemented | Phil | 2026-06-06 | - |
| [project-bootstrap.spec.md](project-bootstrap.spec.md) | ✅ Implemented | Phil | 2026-06-03 | - |

## 约定

- 一个功能一份 spec,不要一份塞多个
- spec 状态必须和代码实际状态一致(每次改动顺手同步)
- 放弃的 spec → 状态改 Archived,顶部加说明,不删文件
