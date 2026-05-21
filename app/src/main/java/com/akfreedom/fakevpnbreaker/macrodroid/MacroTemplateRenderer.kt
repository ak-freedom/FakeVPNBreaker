package com.akfreedom.fakevpnbreaker.macrodroid

object MacroTemplateRenderer {
    const val TOKEN_PLACEHOLDER = "{{TRIGGER_TOKEN}}"

    fun render(template: String, triggerToken: String): MacroTemplateRenderResult =
        when {
            template.isBlank() -> MacroTemplateRenderResult.Failure("Macro template is blank")
            triggerToken.isBlank() -> MacroTemplateRenderResult.Failure("Trigger token is blank")
            !template.contains(TOKEN_PLACEHOLDER) ->
                MacroTemplateRenderResult.Failure("Macro template token placeholder is missing")
            else -> MacroTemplateRenderResult.Success(template.replace(TOKEN_PLACEHOLDER, triggerToken))
        }
}

sealed class MacroTemplateRenderResult {
    data class Success(val content: String) : MacroTemplateRenderResult()
    data class Failure(val message: String) : MacroTemplateRenderResult()
}
