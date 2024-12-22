/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.command.commands

import net.deathlksr.fuguribeta.features.command.Command
import net.deathlksr.fuguribeta.features.module.modules.client.IRCModule
import net.deathlksr.fuguribeta.utils.misc.StringUtils

object IRCChatCommand : Command("chat", "lc", "irc") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            if (!IRCModule.state) {
                chat("§cError: §7IRC is disabled!")
                return
            }

            if (!IRCModule.client.isConnected()) {
                chat("§cError: §bIRC is currently not connected to the server!")
                return
            }

            val message = StringUtils.toCompleteString(args, 1)

            IRCModule.client.sendMessage(message)
        } else
            chatSyntax("chat <message>")
    }
}