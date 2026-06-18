# 宝宝记录 (Android) — 升级路线图

> 当前版本 v1.0.0，基础功能已完整实现。以下为后续可做方向（按优先级排序）。

## 工程化

- Release 构建开启 `isMinifyEnabled = true` + `isShrinkResources = true`
- 补全 ProGuard 规则（Room/Koin/Retrofit/Compose keep 规则）
- 单元测试（JUnit + MockK 测试 ViewModel）
- UI 测试（Compose UI Test）
- CI/CD（GitHub Actions 自动构建）

## 体验优化

- 国际化：UI 字符串外提到 `strings.xml`
- 无障碍：补全 `contentDescription` + 语义化
- 健康页与疫苗页列表支持日期筛选
- 生长页 Canvas 曲线图触摸取值
- 骨架屏加载（shimmer 效果）

## 功能补全

- 深色模式跟随系统（为每个 AppTheme 定义独立暗色 ThemeColors）
- 本地通知（WorkManager + NotificationManager 疫苗提醒）
- 数据导出 CSV
- 分页加载（Room + Paging 3）

## 安全与健壮性

- WebDAV 密码使用 EncryptedSharedPreferences 或 Jetpack Security
- Room TypeConverter 统一处理日期解析，消除各处散落的 try-catch
- 表单输入校验（null/负数/越界）

## 潜在新模块

- 日记本（图文日记，时间线展示）
- 成长里程碑（预设里程碑 + 达成标记）
- 用药记录
