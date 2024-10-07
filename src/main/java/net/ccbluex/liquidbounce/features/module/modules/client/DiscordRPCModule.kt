package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.FuguriBeta.discordRPC
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object DiscordRPCModule : Module("DiscordRPC", Category.CLIENT, hideModule = false) {

    override fun onEnable() {
        discordRPC.ClientRPCStarted = true
    }

    override fun onDisable() {
        discordRPC.ClientRPCStarted = false
    }
}
