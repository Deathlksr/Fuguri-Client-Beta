/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.utils.APIConnecter
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard

class GuiInfo(private val prevGui: GuiScreen) : GuiScreen() {

    override fun initGui() {
        val yOffset = height / 4 + 20
        val buttonWidth = 200
        val buttonHeight = 20

        val buttons = listOf(
            GuiButton(1, width / 2 - buttonWidth / 2, yOffset + buttonHeight * 1 + 10, "Join Discord Server"),
            GuiButton(2, width / 2 - buttonWidth / 2, yOffset + buttonHeight * 2 + 20, "Server Status"),
            GuiButton(3, width / 2 - buttonWidth / 2, yOffset + buttonHeight * 3 + 30, "Scripts"),
            GuiButton(4, width / 2 - buttonWidth / 2, yOffset + buttonHeight * 4 + 40, "Client Configuration"),
            GuiButton(5, width / 2 - buttonWidth / 2, yOffset + buttonHeight * 5 + 50, "Done")
        )

        buttonList.addAll(buttons)

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(prevGui)
        }
        super.keyTyped(typedChar, keyCode)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> MiscUtils.showURL(APIConnecter.discord)
            2 -> mc.displayGuiScreen(GuiServerStatus(this))
            3 -> mc.displayGuiScreen(GuiScripts(this))
            4 -> mc.displayGuiScreen(GuiClientConfiguration(this))
            5 -> mc.displayGuiScreen(prevGui)
        }
    }
}
