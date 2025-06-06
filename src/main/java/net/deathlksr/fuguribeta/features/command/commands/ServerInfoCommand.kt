/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.command.commands

import net.deathlksr.fuguribeta.event.EventManager.registerListener
import net.deathlksr.fuguribeta.event.Listenable
import net.deathlksr.fuguribeta.features.command.Command
import net.deathlksr.fuguribeta.utils.ServerUtils.serverData
import net.minecraft.client.multiplayer.ServerAddress

object ServerInfoCommand : Command("serverinfo"), Listenable {
    init {
        registerListener(this)
    }

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (mc.currentServerData == null || mc.isSingleplayer) {
            chat("This command does not work in single player.")
            return
        }

        val serverAddress = ServerAddress.fromString(serverData?.serverIP) ?: return
        val data = mc.currentServerData ?: return

        chat("Server info:")
        chat("§7Name: §8${data.serverName}")
        chat("§7IP: §8${serverAddress.ip}:${serverAddress.port}")
        chat("§7Players: §8${data.populationInfo}")
        chat("§7MOTD: §8${data.serverMOTD}")
        chat("§7ServerVersion: §8${data.gameVersion}")
        chat("§7ProtocolVersion: §8${data.version}")
        chat("§7Ping: §8${data.pingToServer}")
    }

    override fun handleEvents() = true
}