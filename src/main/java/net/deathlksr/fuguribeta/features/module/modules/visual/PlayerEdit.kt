package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue

object PlayerEdit : Module("PlayerEdit", Category.VISUAL, subjective = true, gameDetecting = false, hideModule = false) {
        val editPlayerSizeValue = BoolValue("EditPlayerSize", false)
        val playerSizeValue = FloatValue("PlayerSize", 0.5f, 0.01f..5f) { editPlayerSizeValue.get() }
        val editPlayerModel = BoolValue("EditModel", false)
        val mode by ListValue("Mode", arrayOf("Imposter", "Rabbit", "Freddy", "None"), "Imposter") { editPlayerModel.get() }
        val rotatePlayer by BoolValue("RotatePlayer", false) { editPlayerModel.get() }
        val rotateValue by IntegerValue("RotateValue", 180, 0..360) { rotatePlayer }
}