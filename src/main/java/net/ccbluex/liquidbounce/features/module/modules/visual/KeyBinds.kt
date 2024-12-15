package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FuguriBeta.moduleManager
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.utils.render.animation.Animations2DUtilsNew
import net.ccbluex.liquidbounce.value.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glTranslated
import java.awt.Color

object KeyBinds : Module("KeyBinds", Category.VISUAL) {

    private val posx by IntegerValue("X", 0, 0..1000)
    private val posy by IntegerValue("Y", 0, 0..1000)
    private val margin by FloatValue("Margin", 2f, 0f..5f)
    private val font by FontValue("Font", Fonts.font40)
    private val text by TextValue("Text", "KeyBinds")
    private val dropShadow by BoolValue("Shadow", true)
    private val borderRadius by FloatValue("BorderRadius", 0f, 0f..15f)
    private val red by FloatValue("Red", 0f, 0f..1f)
    private val green by FloatValue("Green", 0f, 0f..1f)
    private val blue by FloatValue("Blue", 0f, 0f..1f)
    private val alpha by FloatValue("Alpha", 0.5f, 0f..1f)
    private val tred by FloatValue("TextRed", 1f, 0f..1f)
    private val tgreen by FloatValue("TextGreen", 1f, 0f..1f)
    private val tblue by FloatValue("TextBlue", 1f, 0f..1f)
    private val talpha by FloatValue("TextAlpha", 1f, 0f..1f)
    private val smoothSpeed by FloatValue("SmoothSpeed", 1f, 0f..10f)
    private val list = arrayListOf<String>()
    private val animation = Animations2DUtilsNew(0f,0f,0f,0f)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        updateList()
        val textColor = Color(tred, tgreen, tblue, talpha).rgb
        var width = 50
        var height = 30
        var tempHeight = font.FONT_HEIGHT + 3
        for (string in list) {
            tempHeight += font.FONT_HEIGHT + 2
            val stingWidth = font.getStringWidth(string)
            if (stingWidth > width) {
                width = stingWidth
            }
        }
        if (tempHeight > height) height = tempHeight
        animation.endX = width.toFloat()
        animation.endY = height.toFloat()
        animation.update(smoothSpeed * 0.005f)
        glTranslated(posx.toDouble(), posy.toDouble(), 0.0)
        RenderUtils.drawRoundedRect(
            -margin,
            -margin,
            animation.x + margin * 2,
            animation.y + margin * 2,
            borderRadius,
            Color(red, green, blue, alpha).rgb,
            false
        )
        if (dropShadow) {
            RenderUtils.drawShadow(
                -margin,
                -margin,
                animation.x + margin * 2,
                animation.y + margin * 2,
            )
        }
        font.drawString(
            text,
            0f,
            0f,
            textColor,
            false
        )
        var offset: Float = font.FONT_HEIGHT + 3f
        for (string in list) {
            font.drawString(
                string,
                0,
                offset.toInt(),
                textColor
            )
            offset += font.FONT_HEIGHT + 2f
        }
        glTranslated(-posx.toDouble(), -posy.toDouble(), 0.0)
    }

    private fun updateList() {
        list.clear()
        for (module in moduleManager.modules) {
            if (module.state && module.keyBind != 0) {
                list.add(module.getName(true) + " [${Keyboard.getKeyName(module.keyBind)}]")
            }
        }
    }
}