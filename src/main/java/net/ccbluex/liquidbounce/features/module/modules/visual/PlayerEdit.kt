package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue

object PlayerEdit : Module("PlayerEdit", Category.VISUAL, subjective = true, gameDetecting = false, hideModule = false) {
        val editPlayerSizeValue = BoolValue("EditPlayerSize", false)
        val playerSizeValue = FloatValue("PlayerSize", 0.5f, 0.01f..5f) { editPlayerSizeValue.get() }
        val mode by ListValue("Mode", arrayOf("Imposter", "Rabbit", "Freddy", "None"), "Imposter")
        val rotatePlayer by BoolValue("RotatePlayer", false)
}

