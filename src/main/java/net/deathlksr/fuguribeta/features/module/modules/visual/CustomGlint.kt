/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.utils.render.ColorUtils
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import java.awt.Color

object CustomGlint: Module("CustomGlint", Category.VISUAL, hideModule = false) {

    private val modeValue by ListValue("Mode", arrayOf("Rainbow", "Custom"), "Custom")
    private val redValue by IntegerValue("Red", 255, 0.. 255) { modeValue == "Custom" }
    private val greenValue by IntegerValue("Green", 0, 0.. 255) { modeValue == "Custom" }
    private val blueValue by IntegerValue("Blue", 0, 0.. 255) { modeValue == "Custom" }
    private val alphaValue by IntegerValue("Alpha", 255, 0..255) { modeValue == "Custom" }

    fun getColor(): Color {
        return when (modeValue.lowercase()) {
            "rainbow" -> ColorUtils.rainbow(10, 0.9F)
            else -> Color(redValue, greenValue, blueValue, alphaValue)
        }
    }
}