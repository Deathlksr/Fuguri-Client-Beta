package net.deathlksr.fuguribeta.features.module.modules.client

import net.deathlksr.fuguribeta.FuguriBeta.discordRPC
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module

object DiscordRPCModule : Module("DiscordRPC", Category.CLIENT, hideModule = false) {

    override fun onEnable() {
        discordRPC.ClientRPCStarted = true
        discordRPC.running = true
    }

    override fun onDisable() {
        discordRPC.ClientRPCStarted = false
        discordRPC.running = false
    }
}
