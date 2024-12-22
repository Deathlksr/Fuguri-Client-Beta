/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.spartan

import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.SpeedMode
import net.deathlksr.fuguribeta.utils.extensions.tryJump
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.nextDouble

object SpartanYPort : SpeedMode("SpartanYPort") {
    private var airMoves = 0
    override fun onMotion() {
        if (mc.gameSettings.keyBindForward.isKeyDown) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.tryJump()
                airMoves = 0
            } else {
                mc.timer.timerSpeed = 1.08f
                if (airMoves >= 3) mc.thePlayer.jumpMovementFactor = 0.0275f
                if (airMoves >= 4 && airMoves % 2 == 0) {
                    mc.thePlayer.motionY = -0.32 - nextDouble(endInclusive = 0.009)
                    mc.thePlayer.jumpMovementFactor = 0.0238f
                }
                airMoves++
            }
        }
    }

}