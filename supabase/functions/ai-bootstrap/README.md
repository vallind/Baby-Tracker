# AI Bootstrap 配置

`ai-bootstrap` 只负责验证 Supabase 用户与家庭成员关系，并把供应商凭据加密给当前 Android 设备。正常 AI 问答不会经过 Supabase。

部署前需要把 `runtime-config.example.json` 中的供应商地址、模型 ID 和凭据环境变量名替换为真实值，然后设置以下 Secrets：

```powershell
supabase secrets set AI_RUNTIME_CONFIG='<压缩后的配置 JSON>'
supabase secrets set AI_PROVIDER_A_KEY='<供应商 A 的 API Key>'
supabase secrets set AI_PROVIDER_B_KEY='<供应商 B 的 API Key>'
```

规则：

- `credentialEnv` 只能使用 `AI_PROVIDER_*_KEY` 格式。
- 供应商地址必须是 HTTPS。
- 轮换某个供应商密钥时，同时增加它的 `credentialVersion` 和总 `configVersion`。
- 不要把真实配置、API Key、Supabase secret/service_role 写入仓库。
- 当前凭据封装使用 RSA-2048 OAEP-SHA256，单个供应商凭据不能超过 190 字节。

部署函数时必须开启 JWT 校验：

```powershell
supabase functions deploy ai-bootstrap --verify-jwt
```
