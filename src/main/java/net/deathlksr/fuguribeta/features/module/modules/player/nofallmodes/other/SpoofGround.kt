package net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.other

import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.NoFallMode
import net.minecraft.network.play.client.C03PacketPlayer

object SpoofGround : NoFallMode("SpoofGround") {
    override fun onPacket(event: PacketEvent) {
        if (event.packet is C03PacketPlayer)
            event.packet.onGround = true
    }
}