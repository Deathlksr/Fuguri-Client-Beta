/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.ui.client.altmanager.menus

import me.liuli.elixir.account.MinecraftAccount
import net.deathlksr.fuguribeta.ui.client.altmanager.GuiAltManager.Companion.login
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawLoadingCircle
import net.minecraft.client.gui.GuiScreen

class GuiLoginProgress(minecraftAccount: MinecraftAccount, success: () -> Unit, error: (Exception) -> Unit, done: () -> Unit) : GuiScreen() {

    init {
        login(minecraftAccount, success, error, done)
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawLoadingCircle(width / 2f, height / 4f + 70)
        drawCenteredString(fontRendererObj, "Logging into account...", width / 2, height / 2 - 60, 16777215)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

}