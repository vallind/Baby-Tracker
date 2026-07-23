# AI Bootstrap 部署手册

本文说明如何把 `ai-bootstrap` 部署到 Supabase，并为 Android 应用配置 DeepSeek。

## 1. 这个函数负责什么

`ai-bootstrap` 不是 AI 请求代理。它只执行以下操作：

1. 校验 Android 端传入的 Supabase 登录令牌。
2. 校验用户是否属于请求中的家庭。
3. 读取服务端模型配置和供应商 API Key。
4. 使用设备提供的 RSA 公钥加密 API Key，再返回给该设备。

正常问答仍由 Android 应用直接请求 DeepSeek。Supabase 不转发对话内容，也不承担模型流量。

## 2. 当前线上配置

| 项目 | 值 |
|---|---|
| Supabase Project Ref | `kzwmcbdgmngyqmjgmjne` |
| Edge Function | `ai-bootstrap` |
| 函数地址 | `https://kzwmcbdgmngyqmjgmjne.supabase.co/functions/v1/ai-bootstrap` |
| JWT 校验 | 开启 |
| AI 供应商 | DeepSeek |
| 默认选项 | `balanced` / `deepseek-v4-flash` |
| 深度选项 | `deep` / `deepseek-v4-pro` |

DeepSeek 已宣布 `deepseek-chat` 和 `deepseek-reasoner` 将于 2026-07-24 23:59（北京时间）停用，因此新配置不得继续使用这两个旧模型名。

参考：

- [DeepSeek API 快速开始](https://api-docs.deepseek.com/)
- [DeepSeek 更新日志](https://api-docs.deepseek.com/updates/)
- [Supabase Edge Function Secrets](https://supabase.com/docs/guides/functions/secrets)

## 3. 首次部署：推荐使用 Supabase Dashboard

不熟悉命令行时，使用控制台最简单。

### 3.1 创建 DeepSeek API Key

1. 登录 [DeepSeek 开放平台](https://platform.deepseek.com/api_keys)。
2. 创建 API Key，并确认账户有可用余额。
3. 只在 DeepSeek 和 Supabase Secrets 页面粘贴该 Key。

不要把 API Key 发到聊天、提交到 Git、写入 Android 资源文件或数据库。

### 3.2 添加 Edge Function Secrets

打开本项目的 [Edge Function Secrets](https://supabase.com/dashboard/project/kzwmcbdgmngyqmjgmjne/functions/secrets)，添加以下两项。

第一项：

```text
Name:  AI_PROVIDER_DEEPSEEK_KEY
Value: DeepSeek 控制台生成的 API Key
```

第二项：

```text
Name:  AI_RUNTIME_CONFIG
Value: 下方压缩后的单行 JSON
```

```json
{"configVersion":2,"defaultOption":"balanced","ttlSeconds":604800,"providers":[{"id":"deepseek","protocol":"openai_compatible_chat","baseUrl":"https://api.deepseek.com","credentialVersion":1,"credentialEnv":"AI_PROVIDER_DEEPSEEK_KEY"}],"options":[{"id":"balanced","name":"均衡","maxOutputTokens":2400,"capabilities":{"streaming":true,"thinking":true,"reasoningEfforts":["high","max"],"temperature":true},"targets":[{"providerId":"deepseek","model":"deepseek-v4-flash","priority":0}]},{"id":"deep","name":"深度","maxOutputTokens":4000,"capabilities":{"streaming":true,"thinking":true,"reasoningEfforts":["high","max"],"temperature":true},"targets":[{"providerId":"deepseek","model":"deepseek-v4-pro","priority":0}]}]}
```

保存 Secret 后立即生效，不需要重新部署函数。

### 3.3 部署函数代码

函数源文件位于：

```text
supabase/functions/ai-bootstrap/index.ts
supabase/functions/ai-bootstrap/deno.json
```

通过 Supabase 集成或 Dashboard 部署时必须确认：

- Function Name 为 `ai-bootstrap`。
- Entrypoint 为 `index.ts`。
- 同时上传 `deno.json`。
- `verify_jwt` 必须为 `true`。

部署后打开 [Edge Functions](https://supabase.com/dashboard/project/kzwmcbdgmngyqmjgmjne/functions)，确认 `ai-bootstrap` 状态为 `ACTIVE`。

## 4. 使用 Supabase CLI 部署

已经安装 CLI 时，可以在仓库根目录执行。先通过帮助确认本机 CLI 的当前参数，不要直接照搬旧版本命令：

```powershell
supabase --version
supabase functions deploy --help
supabase secrets set --help
```

首次连接项目：

```powershell
supabase login
supabase link --project-ref kzwmcbdgmngyqmjgmjne
```

部署函数并保持 JWT 校验：

```powershell
supabase functions deploy ai-bootstrap --project-ref kzwmcbdgmngyqmjgmjne
```

不要添加 `--no-verify-jwt`。如果项目的 `supabase/config.toml` 单独配置了该函数，也必须保持：

```toml
[functions.ai-bootstrap]
verify_jwt = true
```

设置 Secrets 的推荐方式是创建一个不会提交的临时环境文件：

```dotenv
AI_PROVIDER_DEEPSEEK_KEY=替换为真实Key
AI_RUNTIME_CONFIG={"configVersion":2,"defaultOption":"balanced","ttlSeconds":604800,"providers":[{"id":"deepseek","protocol":"openai_compatible_chat","baseUrl":"https://api.deepseek.com","credentialVersion":1,"credentialEnv":"AI_PROVIDER_DEEPSEEK_KEY"}],"options":[{"id":"balanced","name":"均衡","maxOutputTokens":2400,"capabilities":{"streaming":true,"thinking":true,"reasoningEfforts":["high","max"],"temperature":true},"targets":[{"providerId":"deepseek","model":"deepseek-v4-flash","priority":0}]},{"id":"deep","name":"深度","maxOutputTokens":4000,"capabilities":{"streaming":true,"thinking":true,"reasoningEfforts":["high","max"],"temperature":true},"targets":[{"providerId":"deepseek","model":"deepseek-v4-pro","priority":0}]}]}
```

然后执行：

```powershell
supabase secrets set --env-file .env.ai-bootstrap --project-ref kzwmcbdgmngyqmjgmjne
supabase secrets list --project-ref kzwmcbdgmngyqmjgmjne
```

确认远端存在两个 Secret 后，立即删除本地临时文件。不得提交 `.env.ai-bootstrap`。

## 5. 部署后验证

### 5.1 控制台检查

1. Edge Functions 页面中 `ai-bootstrap` 状态应为 `ACTIVE`。
2. Secrets 页面应能看到 `AI_RUNTIME_CONFIG` 和 `AI_PROVIDER_DEEPSEEK_KEY` 的名称。
3. 页面不会显示已经保存的 Secret 明文，这是正常现象。

### 5.2 安全检查

未携带登录令牌调用函数时，应返回 `401`，不能返回模型配置或加密凭据：

```powershell
Invoke-WebRequest `
  -Method Post `
  -Uri 'https://kzwmcbdgmngyqmjgmjne.supabase.co/functions/v1/ai-bootstrap' `
  -ContentType 'application/json' `
  -Body '{"familyId":"test","devicePublicKey":"test"}'
```

PowerShell 可能把非 2xx 响应显示为异常；重点是 HTTP 状态码必须为 `401`。

### 5.3 应用内验证

完整成功调用必须同时满足：

- 用户已登录 Supabase。
- 应用已选中当前家庭。
- 用户在 `family_members` 中属于该家庭。
- 设备 RSA 公钥格式有效。
- 两个 Secret 均存在且配置 JSON 合法。

应用启动后检查应用内日志。成功时应得到配置版本 `1`，并缓存 `balanced`、`deep` 两个选项；失败时根据下一节排查。

## 6. 常见错误

| 状态/错误 | 常见原因 | 处理方式 |
|---|---|---|
| `401 unauthorized` | 未携带用户 JWT，或登录会话过期 | 重新登录并刷新会话 |
| `403 family_forbidden` | 当前用户不属于请求的家庭 | 检查 `family_members` 与当前家庭 ID |
| `400 invalid_request:familyId` | 家庭 ID 为空或过长 | 检查应用当前家庭初始化 |
| `400 invalid_request:devicePublicKey` | 设备公钥不是有效的 RSA SPKI Base64 | 清理 AI 设备密钥缓存后重新生成 |
| `503 bootstrap_unavailable` | Secret 缺失、JSON 错误或凭据过长 | 查看 Edge Function 日志中的错误名称 |
| `missing_env:AI_RUNTIME_CONFIG` | 未保存运行配置 | 添加 `AI_RUNTIME_CONFIG` |
| `missing_env:AI_PROVIDER_DEEPSEEK_KEY` | 未保存 DeepSeek Key | 添加对应 Secret |
| `invalid_runtime_config_json` | JSON 复制不完整或包含额外字符 | 使用本文单行 JSON 覆盖 |
| DeepSeek 返回 `401` | DeepSeek Key 无效或已被撤销 | 创建新 Key 并执行密钥轮换 |
| DeepSeek 返回余额相关错误 | 账户余额不足 | 在 DeepSeek 平台充值并确认账户状态 |

函数日志不要输出 API Key、完整供应商响应或用户对话内容。

## 7. 修改模型配置

模型展示选项由 `AI_RUNTIME_CONFIG.options` 控制，Android 端只缓存服务端下发的配置。

字段说明：

| 字段 | 作用 |
|---|---|
| `configVersion` | 整套配置版本；每次修改配置都递增 |
| `defaultOption` | 应用未选择模型时使用的选项 ID |
| `ttlSeconds` | Android 缓存配置的有效期 |
| `providers[].protocol` | 供应商接口协议；DeepSeek 使用 `openai_compatible_chat` |
| `providers[].baseUrl` | 供应商 API 根地址，必须是 HTTPS |
| `credentialVersion` | 单个供应商凭据版本；换 Key 时递增 |
| `credentialEnv` | 存放真实 Key 的 Secret 名称 |
| `options[].targets` | 选项对应的供应商、模型和回退优先级 |
| `maxOutputTokens` | App 选择“自动”时使用的建议输出 Token，不是上限 |
| `capabilities.streaming` | 当前模型选项是否保证支持流式输出 |
| `capabilities.thinking` | 当前模型选项是否保证支持思考开关 |
| `capabilities.reasoningEfforts` | 可选择的推理强度，例如 `high`、`max` |
| `capabilities.temperature` | 是否允许 App 自定义温度 |

`capabilities` 只描述模型能力，不限制用户的上下文轮数或最大输出 Token。一个选项配置多个回退目标时，应声明所有目标共同支持的能力，避免切换供应商后参数失效。

仅修改模型映射时：

1. 修改 `AI_RUNTIME_CONFIG`。
2. 增加 `configVersion`。
3. 保存 Secret；不需要重新部署函数。

## 8. 轮换 DeepSeek API Key

建议在 Key 泄露、撤销或计划性轮换时执行：

1. 在 DeepSeek 创建新 Key，暂时保留旧 Key。
2. 用新值覆盖 `AI_PROVIDER_DEEPSEEK_KEY`。
3. 把 `credentialVersion` 从 `1` 增加到 `2`。
4. 同时把 `configVersion` 从 `1` 增加到 `2`。
5. 保存更新后的 `AI_RUNTIME_CONFIG`。
6. 等待应用成功取得新版配置后，再撤销 DeepSeek 旧 Key。

版本号必须递增，否则仍在有效期内的 Android 缓存可能继续使用旧凭据。

## 9. 添加第二个供应商

添加供应商时需要同时完成：

1. 在 `providers` 中添加供应商协议、HTTPS 地址和独立 `credentialEnv`。
2. 在 Supabase Secrets 添加对应 API Key。
3. 在需要回退的 `targets` 中添加该供应商，设置更大的 `priority`。
4. 增加 `configVersion`。
5. Android 工程必须已有对应协议 Adapter；当前支持 `openai_responses` 和 `openai_compatible_chat`。

供应商之间不要共用 Secret 名称。`credentialEnv` 必须匹配 `AI_PROVIDER_*_KEY` 格式。

## 10. 安全约束与后续迁移

- 不得把 DeepSeek API Key、Supabase secret key 或 `service_role` 写进 Android 安装包。
- `ai-bootstrap` 必须校验用户会话和家庭成员关系。
- 当前 RSA-2048 OAEP-SHA256 封装最多加密约 190 字节的供应商凭据。
- 当前实现读取 Supabase 默认环境变量 `SUPABASE_ANON_KEY`，并依赖平台 `verify_jwt` 校验用户 JWT。
- Supabase 正在从旧 `anon` / `service_role` Key 迁移到 publishable / secret keys。旧 Key 预计支持到 2026 年底；迁移时必须先升级本函数鉴权实现，不能直接停用旧 Key。

本函数向设备下发可解密的供应商凭据，适用于个人/家庭自用场景。若未来公开发行、用户规模扩大或需要严格控制成本，应把实际 AI 请求迁移到可信后端代理，避免客户端凭据被提取。
