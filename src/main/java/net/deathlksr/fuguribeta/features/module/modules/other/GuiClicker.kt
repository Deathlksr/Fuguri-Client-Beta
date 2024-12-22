/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render2DEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.client.gui.GuiScreen
import net.minecraftforge.fml.relauncher.ReflectionHelper
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import java.lang.reflect.InvocationTargetException

object GuiClicker : Module("GuiClicker", Category.OTHER, hideModule = false) {

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
        val mouseInGUIPosX = Mouse.getX() * guiScreen.width / mc.displayWidth
        val mouseInGUIPosY = guiScreen.height - Mouse.getY() * guiScreen.height / mc.displayHeight - 1

        try {
            if (mouseDown >= delayValue) {
                ReflectionHelper.findMethod<GuiScreen?>(
                    GuiScreen::class.java,
                    null,
                    arrayOf(
                        "func_73864_a",
                        "mouseClicked"
                    ),
                    Integer.TYPE,
                    Integer.TYPE,
                    Integer.TYPE
                ).invoke(guiScreen, mouseInGUIPosX, mouseInGUIPosY, 0)
                mouseDown = 0
            }
        } catch (ignored: IllegalAccessException) {
        } catch (ignored: InvocationTargetException) {
        }
    }
}