package net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.other

import net.deathlksr.fuguribeta.features.module.modules.player.nofallmodes.NoFallMode
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.minecraft.network.play.client.C03PacketPlayer

object CubeCraft : NoFallMode("CubeCraft") {
    override fun onUpdate() {
        if (mc.thePlayer.fallDistance > 2f) {
            mc.thePlayer.onGround = false
            sendPacket(C03PacketPlayer(true))
        }
    }
}