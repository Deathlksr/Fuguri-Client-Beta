/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.command.commands

import net.deathlksr.fuguribeta.features.command.Command
import net.deathlksr.fuguribeta.features.module.modules.client.IRCModule
import net.deathlksr.fuguribeta.utils.misc.StringUtils

object PrivateChatCommand : Command("pchat", "privatechat", "lcpm") {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 2) {
            if (!IRCModule.state) {
                chat("§cError: §7IRC is disabled!")
                return
            }

            if (!IRCModule.client.isConnected()) {
                chat("§cError: §7IRC is currently not connected to the server!")
                return
            }

            val target = args[1]
            val message = StringUtils.toCompleteString(args, 2)

            IRCModule.client.sendPrivateMessage(target, message)
            chat("Message was successfully sent.")
        } else
            chatSyntax("pchat <username> <message>")
    }
}