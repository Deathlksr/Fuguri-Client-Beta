package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.FuguriBeta.moduleManager
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render2DEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.render.RenderUtils
import net.deathlksr.fuguribeta.utils.render.animation.Animations2DUtilsNew
import net.deathlksr.fuguribeta.value.*
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glTranslated
import java.awt.Color

object KeyBinds : Module("KeyBinds", Category.VISUAL) {

    private val posx by IntegerValue("X", 0, 0..1000)
    private val posy by IntegerValue("Y", 0, 0..1000)
    private val margin by FloatValue("Margin", 2f, 0f..5f)
    private val width by IntegerValue("Width", 50, 20..80)
    private val height by IntegerValue("Height", 20, 10..40)
    private val font by FontValue("Font", Fonts.font40)
    private val text by TextValue("Text", "KeyBinds")
    private val dropShadow by BoolValue("Shadow", true)
    private val borderRadius by FloatValue("BorderRadius", 0f, 0f..5f)
    private val red by IntegerValue("Red", 0, 0..255)
    private val green by IntegerValue("Green", 0, 0..255)
    private val blue by IntegerValue("Blue", 0, 0..255)
    private val alpha by IntegerValue("Alpha", 100, 0..255)
    private val textRed by IntegerValue("TextRed", 0, 0..255)
    private val textGreen by IntegerValue("TextGreen", 0, 0..255)
    private val textBlue by IntegerValue("TextBlue", 0, 0..255)
    private val textAlpha by IntegerValue("TextAlpha", 255, 0..255)
    private val dropShadowText by BoolValue("TextShadow", false)
    private val smoothSpeed by FloatValue("SmoothSpeed", 1f, 0f..10f)
    private val list = arrayListOf<String>()
    private val animation = Animations2DUtilsNew(0f,0f,0f,0f)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        updateList()
        val textColor = Color(textRed, textGreen, textBlue, textAlpha).rgb
        val color = Color(red, green, blue, alpha).rgb
        var width = width
        var height = height
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
            color,
            false
        )
        if (dropShadow) {
            RenderUtils.drawShadow(
                -margin,
                -margin,
                animation.x + margin * 3,
                animation.y + margin * 3,
            )
        }
        font.drawString(
            text,
            0f,
            0f,
            textColor,
            dropShadowText
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