package com.babytracker.designsystem.i18n

/**
 * 国际化文案 — 对标 Palette PaiStrings
 *
 * 当前仅支持 zh-CN，后续可扩展 en-US 等语言。
 * 所有 UI 字符串集中管理，避免散落硬编码。
 */
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
    const val noData = "暂无数据"

    // —— 账户 ——
    const val nickname = "昵称"
    const val editNickname = "修改昵称"
    const val nicknameHint = "请输入昵称"
    const val nicknameSaved = "昵称已保存"

    // —— 表单 ——
    const val pleaseSelect = "请选择"
    const val pleaseInput = "请输入"
    const val required = "必填"

    // —— 确认/提示 ——
    const val confirmDelete = "确认删除"
    const val confirmDeleteMessage = "删除后无法恢复，确定要删除吗？"
    const val undo = "撤销"
    const val deleted = "已删除"

    // —— 宝宝 ——
    const val addBaby = "添加宝宝"
    const val noBabyTitle = "还没有添加宝宝"
    const val noBabySubtitle = "点击下方按钮，记录宝宝成长的每一个瞬间"
    const val babyProfile = "宝宝资料"

    // —— 喂养 ——
    const val feeding = "喂养"
    const val feedingRecords = "喂养记录"
    const val breastFeeding = "母乳"
    const val formulaFeeding = "配方奶"
    const val solidFood = "辅食"
    const val feedingCount = "喂养次数"
    const val feedingDurationMinutes = "分钟"
    const val feedingAmountMl = "ml"

    // —— 睡眠 ——
    const val sleep = "睡眠"
    const val sleepRecords = "睡眠记录"
    const val nightSleep = "夜间睡眠"
    const val nap = "小睡"
    const val sleepHours = "睡眠时长"

    // —— 尿布 ——
    const val diaper = "尿布"
    const val diaperRecords = "尿布记录"
    const val diaperChange = "换尿布"
    const val diaperCount = "换尿布"

    // —— 生长 ——
    const val growth = "生长"
    const val growthRecords = "生长记录"

    // —— 疫苗 ——
    const val vaccination = "疫苗"
    const val vaccinationRecords = "疫苗接种"
    const val generateVaccinePlan = "生成接种计划"
    const val vaccineDone = "已完成"
    const val vaccineUpcoming = "待接种"

    // —— 健康 ——
    const val health = "健康"
    const val healthRecords = "健康档案"

    // —— 设置 ——
    const val settings = "设置"
    const val backup = "备份"
    const val backupManage = "备份管理"
    const val theme = "主题"
    const val about = "关于"

    // —— 时间 ——
    const val today = "今天"
    const val yesterday = "昨天"
    const val recentRecords = "最近记录"
    const val expired = "已过期"
    const val days = "天"

    // —— 消息 ——
    const val markAllRead = "全部已读"

    // —— 发育评估 ——
    const val developmentAssessment = "发育评估"

    // —— 提醒 ——
    const val reminder = "提醒"

    // —— AI 育儿助手 ——
    const val aiAssistant = "AI 育儿助手"
    const val aiAssistantSubtitle = "结合宝宝信息，解答日常育儿问题"
    const val aiWelcome = "你好，我会结合当前宝宝的信息提供育儿建议。"
    const val aiAnswering = "AI 正在回答…"
    const val aiDataNotice = "根据问题仅使用必要的宝宝近期记录"
    const val aiDataNoticeDisabled = "当前仅使用宝宝档案，不读取近期记录"
    const val aiInputLabel = "输入问题"
    const val aiInputPlaceholder = "例如：这个月龄需要注意什么？"
    const val aiSend = "发送"
    const val aiStop = "停止"
    const val aiRetry = "重试"
    const val aiCopy = "复制回答"
    const val aiCopied = "回答已复制"
    const val aiConfigLoading = "正在准备 AI 模型配置…"
    const val aiConfigUnavailable = "AI 模型配置暂不可用，请稍后重试"
    const val aiNoBaby = "请先添加并选择宝宝"
    const val aiNotLoggedIn = "请先登录，再使用家庭专属的 AI 配置"
    const val aiNoFamily = "请先选择云端家庭；本地模式不能获取家庭 AI 配置"
    const val aiFamilyVerifying = "正在验证当前家庭…"
    const val aiFamilyUnverified = "当前家庭尚未验证，请联网后重试"
    const val aiDisabled = "AI 育儿助手已在本机关闭，可前往助手设置重新开启"
    const val aiMonthAgeUnknown = "月龄未知"
    const val aiInputTooLong = "问题不能超过 2000 字"
    const val aiAuthError = "模型认证失败，请检查服务端配置"
    const val aiBalanceError = "模型账户余额不足，请充值后重试"
    const val aiRequestError = "模型或请求参数不受支持，请检查服务端配置"
    const val aiRateLimitError = "请求较多，请稍后再试"
    const val aiServiceError = "AI 服务暂时不可用，请稍后重试"
    const val aiNetworkError = "网络连接失败，请检查网络后重试"
    const val aiUnknownError = "回答生成失败，请重试"
    const val aiReferencePrefix = "本次参考："
    const val aiNoRecentRecordReference = "本次未使用宝宝近期记录"
    const val aiDisclaimer = "AI 回答仅供育儿参考，不能替代医生诊断。"
    const val aiRiskEmergencyTitle = "紧急情况"
    const val aiRiskEmergencyMessage = "请立即拨打 120 或前往最近的急诊，不要等待 AI 回答。"
    const val aiRiskHighTitle = "建议尽快就医"
    const val aiRiskHighMessage = "宝宝情况可能属于高风险，请尽快联系儿科医生或前往医院。"
    const val aiRiskAttentionTitle = "需要密切关注"
    const val aiRiskAttentionMessage = "请持续观察宝宝状态；若症状加重、精神变差或进食尿量继续减少，请及时就医。"
    const val aiQuestionAge = "宝宝这个月龄需要注意什么？"
    const val aiQuestionSleep = "如何建立规律的睡前流程？"
    const val aiQuestionFeeding = "这个月龄喂养需要注意什么？"
    const val aiSettings = "助手设置"
    const val aiSettingsGeneral = "通用"
    const val aiSettingsEnabled = "启用 AI 育儿助手"
    const val aiSettingsEnabledSubtitle = "仅控制本机，不影响家庭中的其他设备"
    const val aiSettingsModelAndAnswer = "模型与回答"
    const val aiSettingsDefaultModel = "默认模型"
    const val aiSettingsDetail = "回答详细程度"
    const val aiSettingsTone = "回答语气"
    const val aiSettingsChecklist = "优先生成行动清单"
    const val aiSettingsBabyData = "宝宝数据"
    const val aiSettingsUseRecords = "使用近期记录"
    const val aiSettingsUseRecordsSubtitle = "仅按问题读取必要类别，不会发送全部记录"
    const val aiSettingsFeeding = "喂养记录"
    const val aiSettingsSleep = "睡眠记录"
    const val aiSettingsDiaper = "尿布记录"
    const val aiSettingsGrowth = "生长记录"
    const val aiSettingsHealth = "健康记录"
    const val aiSettingsExperience = "对话体验"
    const val aiSettingsRecommended = "显示推荐问题"
    const val aiSettingsAutoScroll = "回答时自动滚动"
    const val aiSettingsMarkdown = "渲染 Markdown 格式"
    const val aiSettingsCopyFeedback = "复制后显示提示"
    const val aiSettingsPrivacyAndStatus = "隐私与状态"
    const val aiSettingsSafety = "育儿安全规则"
    const val aiSettingsSafetySubtitle = "风险识别、紧急提示和免责声明不可关闭"
    const val aiSettingsAlwaysOn = "始终开启"
    const val aiSettingsDataNotice = "数据使用范围"
    const val aiSettingsDataNoticeSubtitle = "宝宝档案与已启用的近期记录；不会发送内部 ID"
    const val aiSettingsConfigStatus = "模型配置状态"
    const val aiSettingsExpiresAt = "有效至 "
    const val aiSettingsClearChat = "清除当前会话"
    const val aiSettingsClearChatSubtitle = "仅清除内存中的问答，不删除宝宝记录"
    const val aiSettingsClearConfirm = "确定清除当前 AI 问答吗？此操作不会删除宝宝记录。"
    const val aiDetailConcise = "简洁"
    const val aiDetailBalanced = "适中"
    const val aiDetailDetailed = "详细"
    const val aiTonePractical = "务实"
    const val aiToneGentle = "温和"
    const val aiToneProfessional = "专业"
    const val clear = "清除"
    const val unknown = "未知"
}
