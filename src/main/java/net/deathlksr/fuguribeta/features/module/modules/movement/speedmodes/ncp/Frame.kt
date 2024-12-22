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
import net.deathlksr.fuguribeta.utils.timing.TickTimer

object Frame : SpeedMode("Frame") {
    private var motionTicks = 0
    private var move = false
    private val tickTimer = TickTimer()
    override fun onMotion() {
        if (isMoving) {
            val speed = 4.25
            if (mc.thePlayer.onGround) {
                mc.thePlayer.tryJump()
                if (motionTicks == 1) {
                    tickTimer.reset()
                    if (move) {
                        mc.thePlayer.motionX = 0.0
                        mc.thePlayer.motionZ = 0.0
                        move = false
                    }
                    motionTicks = 0
                } else motionTicks = 1
            } else if (!move && motionTicks == 1 && tickTimer.hasTimePassed(5)) {
                mc.thePlayer.motionX *= speed
                mc.thePlayer.motionZ *= speed
                move = true
            }
            if (!mc.thePlayer.onGround) strafe()
            tickTimer.update()
        }
    }

}