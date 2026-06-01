# 设计系统

## 1. 色彩

### 主色调

| Token | 色值 | 用途 |
|-------|------|------|
| Primary | #6C8DFF | 主色、按钮、强调 |
| Background | #F8F9FD | 页面背景 |
| Surface / Card | #FFFFFF | 卡片背景 |

### 语义色

| Token | 色值 | 用途 |
|-------|------|------|
| Success | #4CAF50 | 已完成、成功状态 |
| Warning | #FF9800 | 待处理、警告 |
| Error | #F44336 | 错误、已过期 |

## 2. 字体

使用 Material 3 默认字体 (Roboto)：

| 样式 | 用途 |
|------|------|
| headlineLarge | 数字统计 (36sp) |
| headlineSmall | 睡眠时长等 (24sp) |
| titleLarge | 宝宝姓名 (22sp) |
| titleMedium | 卡片标题 (16sp) |
| bodyLarge | 列表内容 (16sp) |
| bodyMedium | 描述文字 (14sp) |
| bodySmall | 时间戳、辅助 (12sp) |

## 3. 布局

| Token | 值 |
|-------|-----|
| 页面边距 | 16dp |
| 卡片间距 | 12dp |
| 圆角 | 24dp |
| 按钮高度 | 52dp |
| 图标尺寸 | 24dp |
| 间距 | 8dp（小）/ 16dp（中）/ 24dp（大） |

## 4. 组件库

### ParentingCard

统一卡片容器：

```kotlin
ParentingCard(modifier, content)
```

- 圆角 24dp
- 白色背景
- 16dp 内边距
- fillMaxWidth

### ParentingButton

统一按钮：

```kotlin
ParentingButton(text, onClick, modifier, enabled)
```

- 高度 52dp
- 圆角 24dp
- fillMaxWidth

### EmptyState

空列表占位：

```kotlin
EmptyState(message, modifier)
```

- 居中显示
- Inbox 图标 (64dp)
- 灰色提示文字

### LoadingView

加载指示器：

```kotlin
LoadingView(modifier)
```

- 居中显示
- CircularProgressIndicator

### ErrorView

错误提示：

```kotlin
ErrorView(message, modifier, onRetry)
```

- 错误文字（红色）
- 可选重试按钮

### SectionTitle

区块标题：

```kotlin
SectionTitle(title, modifier)
```

- titleMedium 样式
- 8dp 垂直间距
