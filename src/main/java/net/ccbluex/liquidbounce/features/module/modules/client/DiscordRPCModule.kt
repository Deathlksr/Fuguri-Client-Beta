/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FuguriBeta.discordRPC
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object DiscordRPCModule : Module("DiscordRPC", Category.CLIENT, hideModule = false) {

    override fun onEnable() {
        discordRPC.clientrpcstarted = true
    }

    override fun onDisable() {
        discordRPC.clientrpcstarted = false
    }
}
