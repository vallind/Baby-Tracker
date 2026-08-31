#!/data/data/com.termux/files/usr/bin/bash
# ═══════════════════════════════════════════════════════════════
#  四层架构（taxonomy §〇.4）：本脚手架只生成 Core 组件（designsystem/components/）。
#  业务形态组件（设置行/记录卡/仪表卡/聊天 …）一律手动放入 app/ui/patterns/<域>/，
#  禁止回流 designsystem（组件准入三问，见 AGENTS.md §4）。
# ═══════════════════════════════════════════════════════════════
# ═══════════════════════════════════════════════════════════════
#  组件脚手架生成器（TT-040）
#  用法: ./scripts/generate-component.sh Button
#  生成:
#    designsystem/components/{button}/{Button}.kt
#    designsystem/components/{button}/{Button}Defaults.kt
#    designsystem/components/{button}/{Button}Logic.kt
#    designsystem/theme/AppComponentTokens.kt 追加入口（手动追加）
# ═══════════════════════════════════════════════════════════════

set -euo pipefail

NAME="$1"
if [ -z "$NAME" ]; then
  echo "❌ 用法: $0 <ComponentName>"
  echo "示例: $0 Switch"
  exit 1
fi

LOWER="$(echo "$NAME" | tr '[:upper:]' '[:lower:]')"
DIR="app/src/main/java/com/babytracker/designsystem/components/${LOWER}"
PKG="com.babytracker.designsystem.components.${LOWER}"
TOKEN_PROP="$(echo "${LOWER}" | sed 's/_//g')"

mkdir -p "$DIR"

# ── Xxx.kt ──
cat > "${DIR}/${NAME}.kt" <<EOF
package ${PKG}

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ${NAME}(
    modifier: Modifier = Modifier,
) {
    // TODO: implement
}
EOF

# ── XxxDefaults.kt ──
cat > "${DIR}/${NAME}Defaults.kt" <<EOF
package ${PKG}

import androidx.compose.runtime.Composable
import com.babytracker.designsystem.theme.LocalAppComponentTokens

object ${NAME}Defaults {
    // 示例：从 AppComponentTokens 读取默认值
    // @Composable fun height(): Dp = LocalAppComponentTokens.current.${TOKEN_PROP}.height
}
EOF

# ── XxxLogic.kt ──
cat > "${DIR}/${NAME}Logic.kt" <<EOF
package ${PKG}

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ${NAME}Logic(
    private val scope: CoroutineScope,
) {
    private val _state = MutableStateFlow(false)
    val state: StateFlow<Boolean> = _state.asStateFlow()
}
EOF

# ── XxxLogicTest.kt ──
TEST_DIR="app/src/test/java/com/babytracker/designsystem/components/${LOWER}"
mkdir -p "$TEST_DIR"
cat > "${TEST_DIR}/${NAME}LogicTest.kt" << EOF
package ${PKG}

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ${NAME}LogicTest {

    private val scope = TestScope()

    @Test
    fun \`initial state is false\`() = runTest {
        val logic = ${NAME}Logic(scope)
        assert(!logic.state.value)
    }
}
EOF

echo "✅ 生成 ${NAME} 组件脚手架:"
echo "   ${DIR}/${NAME}.kt"
echo "   ${DIR}/${NAME}Defaults.kt"
echo "   ${DIR}/${NAME}Logic.kt"
echo "   ${TEST_DIR}/${NAME}LogicTest.kt"
echo ""
echo "📋 下一步:"
echo "   1. 在 AppComponentTokens 中添加 ${NAME}Tokens"
echo "   2. 在 AppComponentTokens data class 中添加 val ${TOKEN_PROP}"
echo "   3. 打开 ${DIR}/${NAME}.kt 实现 UI"
echo "   4. 运行 ./gradlew :app:compileDebugKotlin 验证"
