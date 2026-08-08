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

---

## 8. 流式回答不能只按消息数量触发滚动

**现象：** AI 回答正文持续增长，但聊天列表停留在回答开头；页面还会同时显示部分正文、“AI 正在回答”、参考信息和复制按钮，看起来像回答既完成又未完成。

**原因：**

- `LaunchedEffect` 只监听 `messages.size` 和 `isSending`，同一条流式消息的正文变化不会触发。
- `scrollToItem(lastIndex)` 对长消息只会对齐消息顶部，不是整个列表底部。
- 流式消息复用了完成态气泡，没有区分正文生成中与回答已完成。

**规则：**

- 流式聊天自动滚动必须监听最后一条消息的内容变化，并滚动到独立的底部锚点。
- 收到首段正文后不再额外显示等待提示。
- 参考范围、免责声明、复制等完成态操作只在回答完成或用户停止后展示。

---

## 9. 开启思考模式不等于客户端会收到正文

**现象：** 请求已经开启思考模式，模型也执行了推理，但应用只显示最终回答。

**原因：**

- DeepSeek 等 OpenAI 兼容接口把思考内容放在 `reasoning_content`，与最终回答的 `content` 平级。
- 流式接口同样分别通过 `delta.reasoning_content` 和 `delta.content` 返回两类内容。
- 只解析 `content` 会在网络层永久丢弃思考过程，后续 ViewModel 和 UI 无法补救。

**规则：**

- 供应商适配层必须将思考内容与最终回答作为两个独立字段贯通，禁止拼接成同一段正文。
- 流式状态更新必须同时携带当前累计思考内容和最终回答。
- 思考内容只用于当前回答展示，不加入下一轮对话上下文。
- OpenAI 兼容协议的 `reasoning_effort` 位于请求顶层，不能嵌套在 `thinking` 对象中。

---

## 10. 回答后校验不能被流式展示绕过

**现象：** 最终回答虽然会经过安全规则检查，但健康问题的部分正文已经在流式阶段显示，校验完成后再替换也无法撤回用户已经看到的危险建议。

**规则：**

- 健康、用药和已命中本地风险的问题必须先缓冲完整回答，通过校验后再展示。
- 普通问题可以继续流式展示，但完成态仍必须统一经过本地回答校验。
- 具体儿童用药剂量、擅自停换药、确定性诊断和绝对安全保证必须阻止展示。
- 紧急和高风险回答缺少就医指引时，必须由应用补充固定文案，不能依赖模型自行修正。
- 被阻止的回答同时清除思考内容，避免从思考区域绕过安全限制。

---

## 11. 快捷分析可用性必须由主数据决定

**问题：** 睡眠分析会附带喂养记录，喂养分析会附带生长记录。如果用“上下文里任意类别有记录”判断可用性，可能出现没有睡眠记录却显示“睡眠可分析”的假状态。

**规则：**

- 专项快捷分析必须以主类别记录判断可用性：睡眠看睡眠、喂养看喂养、健康看健康。
- 辅助类别只能丰富已经可用的分析，不能单独激活专项入口。
- 综合概览可以由任意已启用类别的有效记录激活。
- 快捷入口只保存分析类型；发送前必须按当前家庭和当前宝宝重新读取记录，禁止缓存业务摘要。

---

## 12. M3 Button 的 content 参数要求 RowScope 接收者

**现象：** 按计划文档逐字转写 `AppButton` 时，`val content: @Composable () -> Unit` 传给 `Button(content = content)` 编译失败：`Argument type mismatch: actual type is 'ComposableFunction0<Unit>', but 'ComposableFunction1<RowScope, Unit>' was expected.`

**原因：** M3 `Button`/`OutlinedButton`/`TextButton` 的 content 参数签名是 `@Composable RowScope.() -> Unit`（拖尾 lambda 写法因 Kotlin 的隐式接收者推断不会报错，显式传参时类型不匹配才暴露）。计划/设计文档里的代码块可能省略或写错接收者类型，逐字转写前先检查。

**规则：** 显式构造组件 content 变量时，必须用 `@Composable RowScope.() -> Unit` 并 import `androidx.compose.foundation.layout.RowScope`。

---

## 13. material3 1.4.0 移除 Snackbar 的 tonalElevation 参数

**现象：** 按计划文档给 `Snackbar(snackbarData = ...)` 传 `tonalElevation = ...` 编译失败：参数不存在。

**原因：** material3 1.4.0（BOM 2026.05.01）重构了 Snackbar，公开 API 只剩 `(snackbarData, modifier, actionOnNewLine, shape, containerColor, contentColor, actionColor, actionContentColor, actionIconColor)`，阴影高度改为内部固定读 `SnackbarTokens.ContainerElevation`，既没有 `tonalElevation` 也没有改名的 `elevation` 参数。

**规则：** 升级/换用新版本 M3 组件时，先 `javap` 或查文档核对组件公开参数再写代码；对已移除的参数，令牌照常定义（设计契约），组件内不注入并留中文注释说明原因。

---

## 14. JVM 单测工作目录是 app 模块根，不是仓库根

**现象：** 静态审计测试用 `File("app/src/main/...")` 相对路径读源码，测试一直"全绿"；改用正确路径后立即暴露两个真实违规（RecordCardDefaults 未走 LocalAppComponentTokens）。

**原因：** AGP 的 unitTest 任务工作目录是 `$moduleDir`（`app/`），不是仓库根。`File("app/src/main/...")` 指向不存在的目录时，`walkTopDown()` 静默返回空流、`readText()` 抛 FileNotFoundException——审计测试对空清单/异常无感知，等于空断言假绿。

**规则：** 单测里引用源码路径一律用模块相对路径 `src/main/...`（不要带 `app/` 前缀）；静态审计测试必须自带"扫描到文件数 > 0"的断言，防止路径漂移后静默空转。

