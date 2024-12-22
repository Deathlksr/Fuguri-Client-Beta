/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.JumpEvent
import net.deathlksr.fuguribeta.event.StrafeEvent
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.MovementUtils.direction
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.MovementUtils.speed
import net.deathlksr.fuguribeta.utils.extensions.toDegreesF
import net.deathlksr.fuguribeta.utils.extensions.tryJump
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import kotlin.math.cos
import kotlin.math.sin

object Strafe : Module("Strafe", Category.MOVEMENT, gameDetecting = false, hideModule = false) {

    private val strength by FloatValue("Strength", 0.5F, 0F..1F)
    private val noMoveStop by BoolValue("NoMoveStop", false)
    private val onGroundStrafe by BoolValue("OnGroundStrafe", false)
    private val allDirectionsJump by BoolValue("AllDirectionsJump", false)

    private var wasDown = false
    private var jump = false

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (jump) {
            event.cancelEvent()
        }
    }

    override fun onEnable() {
        wasDown = false
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.onGround && mc.gameSettings.keyBindJump.isKeyDown && allDirectionsJump && isMoving && !(mc.thePlayer.isInWater || mc.thePlayer.isInLava || mc.thePlayer.isOnLadder || mc.thePlayer.isInWeb)) {
            if (mc.gameSettings.keyBindJump.isKeyDown) {
                mc.gameSettings.keyBindJump.pressed = false
                wasDown = true
            }
            val yaw = mc.thePlayer.rotationYaw
            mc.thePlayer.rotationYaw = direction.toDegreesF()
            mc.thePlayer.tryJump()
            mc.thePlayer.rotationYaw = yaw
            jump = true
            if (wasDown) {
                mc.gameSettings.keyBindJump.pressed = true
                wasDown = false
            }
        } else {
            jump = false
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (!isMoving) {
            if (noMoveStop) {
                mc.thePlayer.motionX = .0
                mc.thePlayer.motionZ = .0
            }
            return
        }

        val shotSpeed = speed
        val speed = shotSpeed * strength
        val motionX = mc.thePlayer.motionX * (1 - strength)
        val motionZ = mc.thePlayer.motionZ * (1 - strength)

        if (!mc.thePlayer.onGround || onGroundStrafe) {
            val yaw = direction
            mc.thePlayer.motionX = -sin(yaw) * speed + motionX
            mc.thePlayer.motionZ = cos(yaw) * speed + motionZ
        }
    }
}
