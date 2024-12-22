/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.hypixel

import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.SpeedMode
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.MovementUtils.strafe
import net.deathlksr.fuguribeta.utils.extensions.tryJump

object HypixelHop : SpeedMode("HypixelHop") {
    override fun onStrafe() {
        val player = mc.thePlayer ?: return
        if (player.isInWater || player.isInLava)
            return

        if (player.onGround && isMoving) {
            if (player.isUsingItem) {
                player.tryJump()
            } else {
                player.tryJump()
                strafe(0.48f)
            }
        }

    }
}
