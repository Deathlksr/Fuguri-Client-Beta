/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.command.commands

import net.deathlksr.fuguribeta.features.command.Command
import net.deathlksr.fuguribeta.features.module.modules.exploit.Plugins

object PluginsCommand : Command("plugins") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        Plugins.toggle()
    }
}