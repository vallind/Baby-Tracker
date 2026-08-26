# 超级参照组件 · 能力轴审计与升级计划（已完成 ✅）

> 目标：8 个参照组件覆盖设计系统的全部难点能力，后续所有组件以它们为模板。
> 结果：8/8 完成升级或标注，每件独立提交，testDebugUnitTest + themeTokenAudit 全绿。
> 关联：《component-taxonomy.md》（收敛蓝图）·《component-gap-analysis.md》（现状盘点）

## 一、能力矩阵与现状评级

| # | 组件 | 验证能力 | 评级 | 结论 |
|---|------|---------|:---:|------|
| 1 | AppSurface | Theme / Material / Shape / Elevation | C | 薄包装，Defaults 绕过令牌 |
| 2 | AppButton | State / Interaction / Motion | B+ | 三轴完整，缺 Motion 与两处尺寸不一致 |
| 3 | AppCard | Composition / Surface | **A** | 已达金标准，仅标注为参照 |
| 4 | AppListItem | Slot / Density / Composition | B- | 密度单档、几何硬编码、语义缺失 |
| 5 | AppInput | Focus / Validation / Complex State | B+ | 缺辅助文案/计数器/IME 动作 |
| 6 | AppDialog(+ActionSheet) | Overlay / Layer / Focus | C+ | ⚠️ 硬编码中文文案，违反 i18n 规范 |
| 7 | AppBottomSheet | Gesture / Physics / Motion | C | 薄包装，手势/物理入口未暴露 |
| 8 | AppNavigationBar | Selection / Navigation | B | 几何硬编码、"99+" 硬编码、语义不全 |

## 二、逐件缺口清单（升级动作）

### 1. AppSurface —— Theme/Material/Shape/Elevation
- ❌ `SurfaceDefaults.tonalElevation()` 硬编码 `0.dp`，绕过 `SurfaceTokens`（其余字段走令牌）
- ❌ 无 `border` / `shadowElevation` 能力，无法作为 Material 四要素的完整参照
- 动作：Defaults 全令牌化；组件补 `border: BorderStroke?`、`shadowElevation`（默认取 `SurfaceTokens.elevation` 派生），保持既有调用兼容

### 2. AppButton —— State/Interaction/Motion
- ❌ **尺寸轴不一致**：Ghost 变体未应用 `height(size)`，五变体高度不齐
- ❌ Motion 轴空白：无按压反馈动画（应演示 `interactionSource` + `LocalAppMotion` 曲线的标准写法）
- ❌ 硬编码：loading 圈 `strokeWidth = 2.dp`、Outline 边框 `1.dp`
- 动作：统一五变体 size 应用；按压 scale 动画走 `LocalAppMotion`；两个常量进 `ButtonTokens` 新字段（组已注册，扩字段不触发 MissingRegistration）

### 3. AppCard —— Composition/Surface ✅ 达标
- header/content/footer 槽位、交互轴（onClick/onLongClick/enabled/selected/loading）、骨架加载、暖阴影、选中语义齐备
- 动作：不改代码，在 KDoc 标注"三轴样板参照"，供 generate-component.sh 流程引用

### 4. AppListItem —— Slot/Density/Composition
- ❌ Density 单档：vertical padding `12.dp`、leading 间距 `12.dp`、分隔线 `0.5.dp` 全部硬编码
- ❌ Composition 语义：无 `Role`、选中态无 `stateDescription`
- 动作：`ListItemTokens` 扩 `verticalPadding/itemGap/dividerThickness` 字段；新增 `density` 参数（Compact/Regular 两档，映射 AppControlTokens）；补语义

### 5. AppInput —— Focus/Validation/Complex State
- ✅ 焦点/错误边框、错误 supportingText、密码开关、suffix、多行已有
- ❌ Complex State 缺口：无非错误辅助文案（helperText）、无字数计数器、无 IME 动作/键盘动作入参
- 动作：补 `helperText`、`counterMaxLength`、`imeAction`、`keyboardActions` 四个参数（纯增量，默认行为不变）

### 6. AppDialog / AppActionSheet —— Overlay/Layer/Focus ⚠️ 合规违规
- ❌ `confirmText = "确认"`、`cancelText = "取消"`（×2 处）硬编码中文，违反 AGENTS.md §4 i18n 规范
- ❌ dismissButton 固定渲染，无法隐藏（单按钮确认场景）
- 动作：全部改走 `AppStrings`；`cancelText: String? = null` 可空控制取消按钮显隐

### 7. AppBottomSheet —— Gesture/Physics/Motion
- ❌ 手势/物理入口未暴露：`sheetState` 不可传入（高级调用方无法控制 partial expand/confirmValueChange）
- ❌ `dragHandle` 无法定制/隐藏；`SheetTokens.elevation` 未接入
- 动作：可选 `state: SheetState?` 入参（缺省内部 remember）、`dragHandle` 槽位、tonalElevation 走令牌；KDoc 说明手势物理语义

### 8. AppNavigationBar —— Selection/Navigation
- ❌ 几何硬编码：胶囊圆角 28dp、外边距 14/8dp、阴影 16dp
- ❌ `"99+"` 溢出徽章硬编码格式串
- ❌ Selection 语义：选中项无 `stateDescription` 补充朗读
- 动作：`BottomBarTokens` 扩几何字段（组已注册）；溢出格式进 `AppStrings`；补选中语义

## 三、执行结果记录

| 提交 | 内容 | 备注 |
|------|------|------|
| 参照组件① | AppSurface 材质四要素令牌化 | SurfaceTokens 扩 contentColor/border/shadowElevation/tonalElevation；暖阴影与卡片同语义 |
| 参照组件② | AppDialog/AppActionSheet i18n 修复 | 硬编码"确认/取消"入 AppStrings；取消按钮可空 |
| 参照组件③ | AppButton Motion 轴 | 按压 0.97 缩放走 LocalAppMotion；修复 Ghost 未应用尺寸高度；描边宽入令牌 |
| 参照组件④ | AppListItem Density 轴 | Compact/Regular 两档；几何硬编码入 ListItemTokens；Button 角色+选中朗读 |
| 参照组件⑤ | AppInput Complex State 轴 | helperText、字数计数器、imeAction、keyboardActions |
| 参照组件⑥ | AppBottomSheet Gesture/Physics/Motion | scrim/拖拽把手/tonal 全令牌化。**决策**：不把实验型 SheetState 暴露进公开签名（会向全部调用方传染 @OptIn），高级手势策略待真实需求以独立重载提供 |
| 参照组件⑦ | AppNavigationBar Selection/Navigation | 胶囊几何入 BottomBarTokens；99+ 溢出格式入 AppStrings |
| 参照组件⑧ | AppCard 三轴样板标注 | 代码零改动，KDoc 标注为 Composition/Surface 参照 |

遗留观察（存量，非本次新增）：AppComponentTokens 中 SkeletonTokens 的 shimmer 双色仍为 `Color(0xFF…)` 字面量（detekt `DefaultsHardcodedColor` 类规则命中），属历史决策的深浅色适配值，建议后续波次迁入核心色令牌后由 default() 引用。

feature 层另发现多处 confirmText/cancelText 硬编码中文（FamilyScreen/VaccinationListScreen/BackupScreen 等 9 处），属 Screen 层清理项，不在参照组件范围，记入待办。

## 四、原执行顺序与验收闸门（存档）

顺序（低风险 → 高风险）：Surface → Dialog → Button → ListItem → Input → BottomSheet → NavigationBar → Card 标注收尾。

每件验收闸门：
1. `gradlew :app:compileDebugKotlin` 快速编译通过；
2. `gradlew testDebugUnitTest` 全绿（含 DesignSystemBoundaryAudit / ScreenBoundaryAudit / TokenAudit 等静态守门测试）；
3. `gradlew themeTokenAudit` 通过；
4. detekt 报告无新增违规（report-only，人工核查）;
5. 中文规范提交：`设计系统：参照组件之 <Name> <摘要>`。
