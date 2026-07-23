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

---

## 5. ensureFamily() 返回了错误的家庭 ID

`SettingsViewModel.ensureFamily()` 原实现：

```kotlin
val families = familyService.loadMyFamilies()
families.firstOrNull()?.id  // ❌ 取了列表第一个，不是"当前家庭"
```

`loadMyFamilies()` 内部会根据 SharedPreferences 的 savedId 设置 `_currentFamily.value`，但 `ensureFamily()` 却无视它，返回了 `families.firstOrNull()`。如果用户有多个家庭，实际使用非第一个家庭时，同步会错误地连接到第一个家庭的数据。

**修复：** 改用 `familyService.currentFamily.value?.id` 代替 `families.firstOrNull()?.id`：

```kotlin
familyService.loadMyFamilies()
familyService.currentFamily.value?.id  // ✅ loadMyFamilies 已设好 currentFamily
```

**排查方法：** 在日志中对比 `familyChanged id=...` 和 `tryAutoSync: fid=...` 的值，不一致则触发此 bug。

---

## 6. JsonNull.toString() 返回字符串"null"而非 Kotlin null

`JsonNull.toString()` 返回字符串 `"null"`，而不是 Kotlin 的 `null`。所有 parse 函数中 `json["field"]?.toString()?.removeSurrounding("\"")` 模式在 JSON null 时会把字段值设成 `"null"` 字符串。

**影响范围：** 所有 String? 字段（brand、note、foodName、doctorName 等）。用户没填的字段，其他账户拉取后显示为字符串 `"null"` 而非空白。

**修复：**
```kotlin
// ❌ 有 bug
brand = json["brand"]?.toString()?.removeSurrounding("\"")

// ✅ 正确
private fun jsonStr(json: JsonObject, key: String): String? =
    (json[key] as? JsonPrimitive)?.content
brand = jsonStr(json, "brand")
```

**规则：** 所有从 JSON 解析字段的地方**禁止**使用 `json["x"]?.toString()?.removeSurrounding("\"")`，必须用 `(json[x] as? JsonPrimitive)?.content` 或封装辅助函数。`JsonNull` 不是 `JsonPrimitive` 的子类，`as?` 会自动返回 null。

---

## 7. Android 15+ 的 RSA-OAEP 必须单独授权 MGF1 摘要

**现象：** AI 配置获取成功，但解密供应商密钥时抛出：

```text
InvalidKeyException: Keystore operation failed
INCOMPATIBLE_MGF_DIGEST
```

**原因：** `setDigests(SHA-256)` 只授权 RSA-OAEP 的主摘要。Android 15（API 35）起，MGF1 摘要是独立的密钥授权项；未指定时默认使用 SHA-1，而服务端 WebCrypto 的 RSA-OAEP 使用 SHA-256，导致硬件 Keystore 拒绝操作。

**修复：**

```kotlin
val builder = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_DECRYPT)
    .setDigests(KeyProperties.DIGEST_SHA256)
    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)

if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
    builder.setMgf1Digests(KeyProperties.DIGEST_SHA256)
}
```

**规则：**

- 客户端的 OAEP 主摘要、MGF1 摘要必须与服务端完全一致。
- 修正密钥生成参数后必须更换密钥别名或删除旧密钥；已有 Keystore 密钥的授权参数无法原地修改。
- 密钥轮换会使本地缓存的旧密文失效，调用链必须捕获解密异常，重新获取用新公钥加密的配置，并且最多自动重试一次。
- `InvalidKeyException` 发生在模型请求之前，不要误判为供应商余额、模型名或 API Key 问题。
