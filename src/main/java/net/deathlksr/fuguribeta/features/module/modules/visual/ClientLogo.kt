package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render2DEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.render.RenderUtils
import net.deathlksr.fuguribeta.value.*
import net.minecraft.util.ResourceLocation
import java.awt.Color

object ClientLogo : Module("ClientLogo", Category.VISUAL, hideModule = false) {

    private val mode by ListValue("Mode", arrayOf("Text", "Logo"), "Logo")
    private val posx by IntegerValue("PosX", 0, 5..1000)
    private val posy by IntegerValue("PosY", 0, 0..1000)
    private val scale by IntegerValue("Scale", 0, 50..400)
    private val font by FontValue("Font", Fonts.font40) { mode == "Text" }
    private val shadow by BoolValue("ShadowText", true) { mode == "Text" }
    private val red by IntegerValue("TextRed", 255, 0..255) { mode == "Text" }
    private val green by IntegerValue("TextGreen", 255, 0..255) { mode == "Text" }
    private val blue by IntegerValue("TextBlue", 255, 0..255) { mode == "Text" }
    private val alpha by IntegerValue("TextAlpha", 255, 0..255) { mode == "Text" }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        when (mode.lowercase()) {
            "logo" -> {
                val rl = ResourceLocation("fuguribeta/textures/fuguri.png")
                RenderUtils.drawImage(
                    rl,
                    posx,
                    posy,
                    scale,
                    scale
                )
            }
            "text" -> {
                val color = Color(red, green, blue, alpha).rgb
                font.drawString(
                    "FuguriBeta",
                    posx.toFloat(),
                    posy.toFloat(),
                    color,
                    shadow,
                )
            }
        }
    }
}