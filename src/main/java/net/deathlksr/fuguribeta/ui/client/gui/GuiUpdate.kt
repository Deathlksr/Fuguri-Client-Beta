/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.ui.client.gui

import net.deathlksr.fuguribeta.FuguriBeta.IN_DEV
import net.deathlksr.fuguribeta.handler.api.ClientUpdate
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.misc.MiscUtils
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import org.lwjgl.opengl.GL11.glScalef
import java.awt.Color

class GuiUpdate : GuiScreen() {

    override fun initGui() {
        val j = height / 4 + 48

        buttonList.run {
            add(GuiButton(1, width / 2 + 2, j + 24 * 2, 98, 20, "Ignore"))
            add(GuiButton(2, width / 2 - 100, j + 24 * 2, 98, 20, "Go to download page"))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        if (!IN_DEV) {
            Fonts.font35.drawCenteredStringWithShadow("${ClientUpdate.newestVersion?.lbVersion} got released!", width / 2f, height / 8f + 80, 0xffffff)
        } else {
            Fonts.font35.drawCenteredStringWithShadow("New build available!", width / 2f, height / 8f + 80, 0xffffff)
        }

        Fonts.font35.drawCenteredStringWithShadow("Press \"Download\" to visit our website or dismiss this message by pressing \"OK\".", width / 2f, height / 8f + 80 + Fonts.font35.fontHeight, 0xffffff)

        super.drawScreen(mouseX, mouseY, partialTicks)

        // Title
        glScalef(2F, 2F, 2F)
        Fonts.font35.drawCenteredStringWithShadow("New update available!", width / 4f, height / 16f + 20, Color(255, 0, 0).rgb)
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> mc.displayGuiScreen(GuiMainMenu())
            2 -> MiscUtils.showURL("https://github.com/VerLouF/Fuguri-Client-Beta")
        }
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode)
            return

        super.keyTyped(typedChar, keyCode)
    }
}