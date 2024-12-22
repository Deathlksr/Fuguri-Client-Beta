/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.client

import net.deathlksr.fuguribeta.FuguriBeta.clickGui
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.ui.client.clickgui.ClickGui
import net.deathlksr.fuguribeta.ui.client.clickgui.style.styles.*
import net.deathlksr.fuguribeta.utils.render.ColorUtils
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.network.play.server.S2EPacketCloseWindow
import org.lwjgl.input.Keyboard
import java.awt.Color

object ClickGUIModule : Module("ClickGUI", Category.CLIENT, Keyboard.KEY_RSHIFT, canBeEnabled = false) {
    private val style by
        object : ListValue("Style", arrayOf("Black", "Null", "Slowly", "LiquidBounce"), "Black") {
            override fun onChanged(oldValue: String, newValue: String) = updateStyle()
        }

    var scale by FloatValue("Scale", 0.8f, 0.5f..1.5f)
    val maxElements by IntegerValue("MaxElements", 15, 1..30)
    val fadeSpeed by FloatValue("FadeSpeed", 1f, 0.5f..4f)
    val scrolls by BoolValue("Scrolls", true)
    val spacedModules by BoolValue("SpacedModules", false)
    val panelsForcedInBoundaries by BoolValue("PanelsForcedInBoundaries", false)
    val clickSound by ListValue("ClickSound", arrayOf("Augustus", "Rise"), "Augustus")

    val volume by FloatValue("Volume", 1F, 0F..1F)

    private val colorRainbowValue = BoolValue("Rainbow", false)
    private val colorRed by IntegerValue("R", 0, 0..255)
    private val colorGreen by IntegerValue("G", 160, 0..255)
    private val colorBlue by IntegerValue("B", 255, 0..255)

    val guiColor
        get() = if (colorRainbowValue.get()) ColorUtils.rainbow().rgb
        else Color(colorRed, colorGreen, colorBlue).rgb

    override fun onEnable() {
        updateStyle()
        mc.displayGuiScreen(clickGui)
    }

    private fun updateStyle() {
        clickGui.style = when (style) {
            "Black" -> BlackStyle
            "Null" -> NullStyle
            "Slowly" -> SlowlyStyle
            "LiquidBounce" -> LiquidBounceStyle
            else -> return
        }
    }

    @EventTarget(ignoreCondition = true)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if (packet is S2EPacketCloseWindow && mc.currentScreen is ClickGui)
            event.cancelEvent()
    }
}