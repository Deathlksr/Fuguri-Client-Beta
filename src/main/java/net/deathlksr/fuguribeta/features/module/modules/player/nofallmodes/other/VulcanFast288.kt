package net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.other

import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.NoFallMode
import net.deathlksr.fuguribeta.utils.extensions.stopXZ
import net.deathlksr.fuguribeta.utils.misc.FallingPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

/*
* Working on Vulcan: 2.8.8
* Tested on: eu.loysia.cn, anticheat-test.com
* Credit: @Razzy52 / VulcanTP
*/

object VulcanFast288 : NoFallMode("VulcanFast288") {
    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C04PacketPlayerPosition) {
            val fallingPlayer = FallingPlayer()
            if (player.fallDistance > 2.5 && player.fallDistance < 50) {
                // Checks to prevent fast falling to void.
                if (fallingPlayer.findCollision(500) != null) {
                    packet.onGround = true

                    player.stopXZ()
                    player.motionY = -99.887575
                    player.isSneaking = true
                }
            }
        }
    }
}