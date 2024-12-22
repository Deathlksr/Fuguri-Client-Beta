/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.intave

import net.deathlksr.fuguribeta.features.module.modules.movement.Speed
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.SpeedMode
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.MovementUtils.strafe
import net.deathlksr.fuguribeta.utils.extensions.tryJump

object IntaveHop14 : SpeedMode("IntaveHop14") {

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        if (!isMoving || player.isInWater || player.isInLava || player.isInWeb || player.isOnLadder) return

        if (player.onGround) {
            player.tryJump()

            if (player.isSprinting) strafe(strength = Speed.strafeStrength.toDouble())

            mc.timer.timerSpeed = Speed.groundTimer
        } else {
            mc.timer.timerSpeed = Speed.airTimer
        }

        if (Speed.boost && player.motionY > 0.003 && player.isSprinting) {
            player.motionX *= 1.0015
            player.motionZ *= 1.0015
        }
    }
}
