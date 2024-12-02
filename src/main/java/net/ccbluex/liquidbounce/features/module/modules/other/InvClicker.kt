/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.gui.inventory.GuiInventory
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.lang.reflect.InvocationTargetException

object InvClicker : Module("GuiClicker", Category.OTHER, hideModule = false, canBeEnabled = false) {

    private val delayValue by IntegerValue("Delay", 5, 0..10)
    private var mouseDown = 0

    @EventTarget
    fun onRender2D(event: Render2DEvent?) {
        if (!Mouse.isButtonDown(0) || !Keyboard.isKeyDown(54) && !Keyboard.isKeyDown(42)) {
            mouseDown = 0
            return
        }
        mouseDown++
        inInvClick(mc.currentScreen)
    }

    private fun inInvClick(guiScreen: GuiScreen) {

        try {
            if (mouseDown >= delayValue) {
                if (guiScreen is GuiInventory) {

                }
                mouseDown = 0
            }
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: InvocationTargetException) {
        }
    }
}