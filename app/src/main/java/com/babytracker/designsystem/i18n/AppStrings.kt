package com.babytracker.designsystem.i18n

// ═══════════════════════════════════════════════════════════
//  AppStrings — 设计系统通用文案（四层架构 Phase 5 分区）
//
//  仅保留通用 UI 语义文案（designsystem 组件可引用集合 + 全库通用词）。
//  产品域文案（AI/记录/消息/提醒/账户/家庭…）已移至
//  app/ui/i18n/AppStringsProduct（AppStringsProduct）。
//  禁止新增产品文案进本文件。
// ═══════════════════════════════════════════════════════════

object AppStrings {
    // —— 导航 ——
    const val home = "首页"
    const val records = "记录"
    const val stats = "统计"
    const val messages = "消息"
    const val profile = "我的"

    // —— 通用操作 ——
    const val save = "保存"
    const val cancel = "取消"
    const val delete = "删除"
    const val edit = "编辑"
    const val confirm = "确认"
    const val back = "返回"
    const val add = "添加"
    const val search = "搜索"
    const val viewAll = "查看全部"
    const val loading = "加载中..."
    const val selected = "已选中"
    const val noData = "暂无数据"

    /** 输入框字数计数器（Complex State 参照），如 12/140 */
    fun charCounter(count: Int, max: Int): String = "$count/$max"

    /** 导航徽章溢出计数，超过上限时显示 */
    const val badgeOverflowMax = "99+"

    // —— 步骤条三态（AppStepper 语义）——
    const val stepCompleted = "已完成"
    const val stepCurrent = "进行中"
    const val stepUpcoming = "未开始"

    // —— 表格排序（AppDataTable 表头箭头语义）——
    const val sortAscending = "升序"
    const val sortDescending = "降序"

    // —— 验证码输入（AppPinInput 无障碍）——
    const val pinCodeField = "验证码输入框，请输入数字"

    // —— 错误状态（E 批：AppErrorState 默认文案）——
    const val retry = "重试"
    const val reload = "重新加载"
    const val errorGenericTitle = "出错了"
    const val errorGenericHint = "加载失败，请稍后重试"
    const val errorNetworkTitle = "网络不可用"
    const val errorNetworkHint = "请检查网络连接后重试"
    const val errorNotFoundTitle = "没有找到"
    const val errorNotFoundHint = "内容不存在或已被删除"

    // —— 表单 ——
    const val pleaseSelect = "请选择"
    const val pleaseInput = "请输入"
    const val required = "必填"
    const val showPassword = "显示密码"
    const val hidePassword = "隐藏密码"

    // —— 确认/提示 ——
    const val confirmDelete = "确认删除"
    const val confirmDeleteMessage = "删除后无法恢复，确定要删除吗？"
    const val undo = "撤销"
    const val deleted = "已删除"

    // —— 评分 ——
    const val rateDescription = "评分 %1\$d，共 %2\$d 星"

    // —— 时间 ——
    const val today = "今天"
    const val yesterday = "昨天"
    const val tomorrow = "明天"
    const val days = "天"
    const val prevDay = "前一天"
    const val nextDay = "后一天"
    const val selectDate = "选择日期"

    // —— 时间 ——
    const val timeJustNow = "刚刚"
    const val timeMinutesAgo = "%1\$d分钟前"
    const val clear = "清除"
    const val unknown = "未知"
}
