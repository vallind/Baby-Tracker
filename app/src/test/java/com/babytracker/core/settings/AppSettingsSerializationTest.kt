package com.babytracker.core.settings

import com.babytracker.core.ai.settings.AiAnswerTone
import com.babytracker.core.sync.SyncDelay
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppSettingsSerializationTest {
    @Test
    fun `设置模型 JSON 往返后保持完整`() = runBlocking {
        val original = AppSettings(
            appearance = AppearanceSettings(themeName = "night"),
            sync = AppSettings().sync.copy(syncDelay = SyncDelay.SECONDS_10),
            ai = AiSettings(
                preferences = AppSettings().ai.preferences.copy(
                    answerTone = AiAnswerTone.GENTLE,
                ),
                defaultModels = mapOf("family-1" to "model-1"),
            ),
            diagnostics = DiagnosticsSettings(logCaptureEnabled = true),
        )
        val output = ByteArrayOutputStream()

        AppSettingsSerializer.writeTo(original, output)
        val restored = AppSettingsSerializer.readFrom(
            ByteArrayInputStream(output.toByteArray()),
        )

        assertEquals(original, restored)
    }

    @Test
    fun `未知字段不阻断后续版本读取`() = runBlocking {
        val json = """
            {
              "schemaVersion": 99,
              "appearance": {"themeName": "warm", "futureField": true},
              "futureSection": {"enabled": true}
            }
        """.trimIndent()

        val restored = AppSettingsSerializer.readFrom(
            ByteArrayInputStream(json.encodeToByteArray()),
        )

        assertEquals("warm", restored.appearance.themeName)
        assertTrue(restored.sync.autoSync)
    }
}
