/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.other

import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.NoFallMode
import net.deathlksr.fuguribeta.utils.misc.FallingPlayer
import net.deathlksr.fuguribeta.utils.timing.WaitTickUtils
import net.minecraft.network.play.client.C03PacketPlayer

/*
* Working on Watchdog
* Tested on: mc.hypixel.net
* Credit: @localpthebest / HypixelPacket
*/
object HypixelTimer : NoFallMode("HypixelTimer") {

    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        val fallingPlayer = FallingPlayer()

        if (packet is C03PacketPlayer) {
            if (fallingPlayer.findCollision(500) != null && player.fallDistance - player.motionY >= 3.3) {
                mc.timer.timerSpeed = 0.5f

                packet.onGround = true
                player.fallDistance = 0f

                WaitTickUtils.scheduleTicks(1) {
                    mc.timer.timerSpeed = 1f
                }
            }
        }
    }
}