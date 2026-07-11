# 开发教训

> 每次踩坑后追加到此文件。AI 每次任务前必须全文读取。

---

## 1. messages 表不走同步

`messages`（站内信）是用户私有本地数据，**不加入同步流程**。它的 Supabase RLS 策略是 `user_id = auth.uid()`，不是 family 级别的，推送到云端必定触发 `42501` 错误。

**规则：** `SyncEngine` 中以下三处列表**禁止出现** `"messages"`：

- `getEntityDao()` 的 `when` 分支
- `pull()` 的 tables 列表
- `markExistingPending()` 的 tables 列表

**修复教训：** 新增表到同步前，先确认该表在 Supabase 的 RLS 策略与同步机制一致（必须有 `family_id` 且策略为 `is_family_member(family_id)`）。

---

## 2. Supabase RLS 42501 排查链

`PostgrestRestException: Code: 42501` 表示 RLS 阻止了操作。排查顺序：

1. **请求 payload 是否包含 `family_id`？** — 查 `injectFamilyId()` 逻辑
2. **用户是否在 `family_members` 表中？** — 在 Supabase Dashboard 直接查
3. **该表的 RLS 策略是否正确？** — 确认策略引用了 `is_family_member()` 函数
4. **策略是 FOR ALL 还是 FOR INSERT？** — 不同操作需要不同策略

---

## 3. markExistingPending 的 conflict 循环陷阱

`markExistingPending()` 中有以下逻辑：

```kotlin
UPDATE sync_metadata SET syncStatus='pending' WHERE tableName='$table' AND syncStatus='conflict'
```

这会把之前标记为 `conflict` 的记录**重置为 pending**，导致 push 失败 → conflict → markExistingPending 重置 → push 再失败 → 死循环。

**规则：** 如果某条记录反复失败，不要只修业务逻辑。先检查是否被这个 conflict→pending 循环困住。从 tables 列表中摘除某个表时，必须在 `markExistingPending()` 开头加上 `DELETE FROM sync_metadata WHERE tableName='<表名>'` 清理存量。

---

## 4. 日志系统优先于 adb logcat

**问题：** adb logcat 在真机上使用不便（需要 USB 连接、权限受限）。

**方案：** 使用 Timber + 自定义 Tree，同时写入：
- 内存环形缓冲区（上限 1000 条，`LogBuffer`）
- 日志文件（`$filesDir/logs/app.log`，512KB 自动轮转）

在 App 设置页内置日志查看器（`LogViewerScreen`），支持过滤、级别着色、复制到剪贴板。

**新增埋点时：** 用 `Timber.tag("模块名").d/e(...)`，Tag 命名规范：`Sync` / `Family` / `SyncVM` / `Auth` / `Backup`。
