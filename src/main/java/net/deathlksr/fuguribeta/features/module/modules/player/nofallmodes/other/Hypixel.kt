/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.other

import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

object Hypixel : NoFallMode("Hypixel") {

    private var jump = false

    override fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer.C04PacketPlayerPosition) {
            if (player.fallDistance >= 3.3) {
                jump = true
            }

            if (jump && player.onGround) {
                packet.onGround = false

                if (!mc.gameSettings.keyBindJump.isKeyDown) {
                    player.setPosition(packet.positionX, packet.positionY + 0.09, packet.positionZ)
                }

                jump = false
            }
        }
    }
}