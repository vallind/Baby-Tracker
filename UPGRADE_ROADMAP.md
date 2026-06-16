# 宝宝记录 (Android) — 升级路线图

> 当前版本 v1.0.0，基础功能已实现。按难易度排序后续可做方向。

---

## P0 — 快速取胜（1-2小时）

| 功能 | 说明 | 涉及文件 |
|---|---|---|
| 表单数据校验 | 输入框 null/负数/越界校验 | `*FormSheet.kt` |
| 提交防重复 | 按钮加 loading 状态防重复点击 | `*Screen.kt` |
| 空状态引导 | 各模块无数据显示操作指引 | `*Screen.kt` |
| 喂养表单 | 新建喂养记录的 BottomSheet | `FeedingFormSheet.kt`（新建） |
| 睡眠表单 | 新建睡眠记录的 BottomSheet | `SleepFormSheet.kt`（新建） |
| 生长表单 | 新建生长记录的 BottomSheet | `GrowthFormSheet.kt`（新建） |
| 疫苗表单 | 新建疫苗记录的 BottomSheet | `VaccinationFormSheet.kt`（新建） |
| 健康表单 | 新建健康记录的 BottomSheet | `HealthFormSheet.kt`（新建） |

## P1 — 体验优化（3-6小时）

| 功能 | 说明 |
|---|---|
| 真实 Canvas 折线图 | 当前为占位绘制，完善 Y 轴刻度/WHO 参考线/触摸取值 |
| 首页最近记录 | 当前首页只有概览，接入各模块 Flow 展示最近动态 |
| 今日概览真实数据 | 接入 FeedingRepo/SleepRepo 实时数据 |
| 深色模式跟随系统 | 监听 Configuration.uiMode |
| WebDAV 配置持久化 | 存入 backup_config 表，接入 Retrofit 客户端 |
| 骨架屏加载 | shimmer 效果，数据加载时显示占位动画 |
| 主题切换弹窗 | 当前为单行切换，改为带预览的底部弹窗 |

## P2 — 功能补全（6-10小时）

| 功能 | 说明 |
|---|---|
| 宝宝管理完整 CRUD | 新建宝宝表单 + 编辑 + 删除确认 |
| 全局宝宝选择器 | AppBar 下拉切换当前宝宝，所有页面响应 |
| 完整备份系统 | 接入 ZipOutputStream 打包 + WebDAV 上传/下载 |
| 本地通知 | WorkManager + NotificationManager 疫苗提醒 |
| DataStore 保存主题偏好 | 应用重启保持上次主题 |

## P3 — 新增模块（8-12小时）

| 模块 | 说明 |
|---|---|
| **便便记录** | PoopEntity + Dao + Screen，类似喂养 CRUD |
| **用药记录** | MedicineEntity + Dao + Screen |
| **成长里程碑** | 预设里程碑 + 达成标记 |
| **日记本** | 图文日记，时间线展示 |

## P4 — 工程化与生产化（12-24小时）

| 功能 | 说明 |
|---|---|
| 单元测试 | JUnit + MockK 测试 ViewModel |
| UI 测试 | Compose UI Test |
| CI/CD | GitHub Actions 自动构建 |
| 崩溃监控 | Firebase Crashlytics |
| 无障碍 | ContentDescription + 焦点顺序 |
| 分页加载 | Room + Paging 3 集成 |
| 数据导出 CSV | 各表数据导出为 CSV 文件 |

---

## 文件清单（当前缺失但需要的 UI 文件）

当前实现了 8 个页面，但缺少各模块的**新建/编辑表单**（BottomSheet）。需创建：

| 文件 | 说明 |
|---|---|
| `ui/feeding/FeedingFormSheet.kt` | 喂养表单（类型选择+动态字段） |
| `ui/sleep/SleepFormSheet.kt` | 睡眠表单（起止时间选择器） |
| `ui/growth/GrowthFormSheet.kt` | 生长表单（维度+数值+日期） |
| `ui/vaccination/VaccinationFormSheet.kt` | 疫苗表单（名称+剂次+日期+状态） |
| `ui/health/HealthFormSheet.kt` | 健康表单（分类+描述+医生+日期） |
| `ui/settings/BabyFormSheet.kt` | 宝宝编辑表单 |

每个表单均使用 `ModalBottomSheet` 实现。

## 实施建议

1. **先做 P0** — 补全缺失的表单文件，让功能可完整使用
2. **P1 完善数据链路** — 接入真实 Flow 数据替换占位
3. **P2-P4 按需选做** — 评估必要性后启动
