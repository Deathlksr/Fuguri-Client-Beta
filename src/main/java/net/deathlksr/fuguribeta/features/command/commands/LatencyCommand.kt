package net.deathlksr.fuguribeta.features.command.commands

import net.deathlksr.fuguribeta.features.command.Command

object LatencyCommand : Command("latency") {

    override fun execute(args: Array<String>) {
        chat("§3Your latency is §a${mc.netHandler.getPlayerInfo(mc.thePlayer.uniqueID).responseTime}ms§3.")
    }

}