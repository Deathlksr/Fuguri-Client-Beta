/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.client

import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.ui.client.hud.designer.GuiHudDesigner

object HUDDesigner : Module("HUDDesigner", Category.CLIENT, canBeEnabled = false) {
    override fun onEnable() {
        mc.displayGuiScreen(GuiHudDesigner())
    }
}