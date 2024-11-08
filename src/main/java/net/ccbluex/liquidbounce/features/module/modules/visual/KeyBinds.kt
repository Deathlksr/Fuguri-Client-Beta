package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object KeyBinds : Module("KeyBinds", Category.VISUAL, hideModule = false) {

//    private val penis = arrayListOf<String>()
//
//    private val font by FontValue("Font", Fonts.minecraftFont)
//
//    fun onRender2D(e: Render2DEvent) {
//        penis.clear()
//        for (module in moduleManager.modules) {
//            if (module.isActive && module.keyBind != 0) {
//                penis.add(module.getName(true) + " [${Keyboard.getKeyName(module.keyBind)}]")
//            }
//        }
//        var peniswight = 50
//        val penisotstup = 2
//        val sc = ScaledResolution(mc)
//        var penisheight = 20
//        for (st in penis) {
//            penisheight += font.FONT_HEIGHT + 2
//            if (font.getStringWidth(st) > peniswight) {
//                peniswight = font.getStringWidth(st)
//            }
//        }
//        RenderUtils.drawRoundedRect(
//            sc.scaledWidth / 2F + 50 - penisotstup,
//            sc.scaledHeight / 2F - penisotstup,
//            sc.scaledWidth / 2F + 50 + penisotstup + peniswight,
//            sc.scaledHeight / 2F + penisotstup + penisheight,
//            4F,
//            Color(15, 15, 15, 100).rgb,
//            false
//        )
//        var peniseblan = 0
//        font.drawString(
//            "KeyBinds",
//            sc.scaledWidth / 2 + 50,
//            sc.scaledHeight / 2,
//            -1
//        )
//        for (st in penis) {
//            font.drawString(
//            st,
//            sc.scaledWidth / 2 + 50,
//            sc.scaledHeight / 2 + 15,
//            -1
//            )
//        }
//    }
}