package com.akfreedom.fakevpnbreaker.macrodroid

import java.nio.file.Path
import kotlin.io.path.readText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MacroTemplateRendererTest {
    @Test
    fun replacesTriggerTokenPlaceholder() {
        val result = MacroTemplateRenderer.render(
            """{"m_extra1Value":"{{TRIGGER_TOKEN}}"}""",
            "token-123",
        )

        assertTrue(result is MacroTemplateRenderResult.Success)
        assertEquals(
            """{"m_extra1Value":"token-123"}""",
            (result as MacroTemplateRenderResult.Success).content,
        )
    }

    @Test
    fun keepsIntentFieldsUntouched() {
        val template = """
            {
              "m_action":"com.akfreedom.fakevpnbreaker.BREAK_VPN",
              "m_packageName":"com.akfreedom.fakevpnbreaker",
              "m_extra1Name":"com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN",
              "m_target":"Activity",
              "m_extra1Value":"{{TRIGGER_TOKEN}}"
            }
        """.trimIndent()

        val result = MacroTemplateRenderer.render(template, "token-123")

        assertTrue(result is MacroTemplateRenderResult.Success)
        val content = (result as MacroTemplateRenderResult.Success).content
        assertTrue(content.contains("com.akfreedom.fakevpnbreaker.BREAK_VPN"))
        assertTrue(content.contains("com.akfreedom.fakevpnbreaker"))
        assertTrue(content.contains("com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN"))
        assertTrue(content.contains(""""m_target":"Activity""""))
        assertTrue(content.contains("token-123"))
    }

    @Test
    fun bundledTemplateKeepsFallbackActivityIntentFields() {
        val template = Path.of(
            "src",
            "main",
            "assets",
            "macrodroid",
            "VPN_OFF.template.macro",
        ).readText()

        val result = MacroTemplateRenderer.render(template, "token-123")

        assertTrue(result is MacroTemplateRenderResult.Success)
        val content = (result as MacroTemplateRenderResult.Success).content
        assertTrue(content.contains(""""m_action":"com.akfreedom.fakevpnbreaker.BREAK_VPN""""))
        assertTrue(content.contains(""""m_packageName":"com.akfreedom.fakevpnbreaker""""))
        assertTrue(content.contains(""""m_extra1Name":"com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN""""))
        assertTrue(content.contains(""""m_extra1Value":"token-123""""))
        assertTrue(content.contains(""""m_target":"Activity""""))
        assertFalse(content.contains(MacroTemplateRenderer.TOKEN_PLACEHOLDER))
    }

    @Test
    fun rejectsMissingPlaceholder() {
        val result = MacroTemplateRenderer.render("""{"m_extra1Value":"ADD TOKEN"}""", "token-123")

        assertTrue(result is MacroTemplateRenderResult.Failure)
    }

    @Test
    fun rejectsBlankTemplateAndBlankToken() {
        assertTrue(MacroTemplateRenderer.render("", "token-123") is MacroTemplateRenderResult.Failure)
        assertTrue(
            MacroTemplateRenderer.render(
                MacroTemplateRenderer.TOKEN_PLACEHOLDER,
                "",
            ) is MacroTemplateRenderResult.Failure,
        )
    }

    @Test
    fun failureMessagesDoNotIncludeTriggerToken() {
        val result = MacroTemplateRenderer.render("""{"m_extra1Value":"ADD TOKEN"}""", "token-123")

        assertTrue(result is MacroTemplateRenderResult.Failure)
        assertFalse((result as MacroTemplateRenderResult.Failure).message.contains("token-123"))
    }

    @Test
    fun doesNotModifyTokenValue() {
        val token = " token:123/ABC "
        val result = MacroTemplateRenderer.render(
            MacroTemplateRenderer.TOKEN_PLACEHOLDER,
            token,
        )

        assertTrue(result is MacroTemplateRenderResult.Success)
        val content = (result as MacroTemplateRenderResult.Success).content
        assertEquals(token, content)
        assertFalse(content.contains(MacroTemplateRenderer.TOKEN_PLACEHOLDER))
    }
}
