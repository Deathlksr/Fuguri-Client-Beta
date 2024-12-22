/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.ncp

import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.SpeedMode
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.MovementUtils.strafe
import net.deathlksr.fuguribeta.utils.extensions.tryJump

object NCPHop : SpeedMode("NCPHop") {
    override fun onEnable() {
        mc.timer.timerSpeed = 1.0865f
        super.onEnable()
    }

    override fun onDisable() {
        mc.thePlayer.speedInAir = 0.02f
        mc.timer.timerSpeed = 1f
        super.onDisable()
    }

    override fun onUpdate() {
        if (isMoving) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.tryJump()
                mc.thePlayer.speedInAir = 0.0223f
            }
            strafe()
        } else {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
    }

}