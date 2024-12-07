package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FuguriBeta.moduleManager
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.ccbluex.liquidbounce.value.FontValue
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.render.animation.Animation2DUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.TextValue
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11
import java.awt.Color

object KeyBinds : Module("KeyBinds", Category.VISUAL, hideModule = false) {

    private val penis = arrayListOf<String>()

    private val font by FontValue("Font", Fonts.font40)
    private val textkey by TextValue("Text", "KeyBinds")
    private val radiuspenis by FloatValue("BorderRadius", 0f, 0f..15f)

    private val posx by IntegerValue("PosX", 0, -2000..2000)
    private val posy by IntegerValue("PosY", 0, -2000..2000)

    private val animation = Animation2DUtils(0F, 0F, 0F, 0F)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        penis.clear()
        for (module in moduleManager.modules) {
            if (module.state && module.keyBind != 0) {
                penis.add(module.getName(true) + " [${Keyboard.getKeyName(module.keyBind)}]")
            }
        }
        var peniswight = 50
        val penisotstup = 2
        val sc = ScaledResolution(mc)
        var penisheight = 20
        for (st in penis) {
            penisheight += font.FONT_HEIGHT + 2
            if (font.getStringWidth(st) > peniswight) {
                peniswight = font.getStringWidth(st)
            }
        }

        animation.endX = peniswight.toFloat()
        animation.endY = penisheight.toFloat()
        animation.update(0.02f)

        GL11.glTranslated(posx.toDouble(), posy.toDouble(), 0.0)

        RenderUtils.drawRoundedRect(
            sc.scaledWidth / 2F + 50 - penisotstup,
            sc.scaledHeight / 2F - penisotstup,
            sc.scaledWidth / 2F + 50 + penisotstup + animation.x,
            sc.scaledHeight / 2F + penisotstup + animation.y,
            radiuspenis,
            Color(15, 15, 15, 100).rgb,
            false
        )
        var peniseblan = 0
        font.drawString(
            textkey,
            sc.scaledWidth / 2 + 50,
            sc.scaledHeight / 2,
            -1
        )
        for (st in penis) {
            font.drawString(
            st,
            sc.scaledWidth / 2 + 50,
            sc.scaledHeight / 2 + 15 + peniseblan * 12,
            -1
            )
            peniseblan++
        }
        GL11.glTranslated(-posx.toDouble(), -posy.toDouble(), 0.0)
    }
}