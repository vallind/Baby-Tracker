# 暂存：Batch 2 遗留 Route 半成品

来源：上一个 AI 会话在 Batch 2 起步阶段（2026-08-24 10:27~10:30）生成的 11 个 `XxxRoute.kt`，
因 `AppBottomBar` 与 Screen 新签名尚未落地而无法编译，随工作区快照遗留。

**处置**：Batch 2 执行时**按 v4 方案约定重写，不直接复用**。原因：

1. v4 要求「Route 是组合根」——currentBabyId 解析、数据加载应进 ViewModel，而这些文件把
   `koinInject BabyRepository/BabyController` + `LaunchedEffect(loadData)` 放进了 Route；
2. v4 要求 Screen 只收 `state + 回调`，这些文件却向 Screen 传 `viewModel` 对象；
3. 徽章/选中判定等细节已由 Batch 1 的 `AppBottomBar` 承担，Routes 中的 `bottomBar` 槽位约定可沿用。

参考价值：各屏的导航回调清单（哪些动作需要跳转、跳去哪）已在此整理，重写时对照使用。
Batch 2 完成后删除本目录。