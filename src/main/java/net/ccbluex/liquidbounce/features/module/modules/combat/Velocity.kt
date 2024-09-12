/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.visual.ESP
import net.ccbluex.liquidbounce.utils.extensions.tryJump
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue

object Velocity : Module("Velocity", Category.COMBAT, hideModule = false) {

    private val mode by ListValue(
        "Mode", arrayOf(
            "Intave"
        ), "Intave"
    )



    // Intave MotionXZ
    private val Motionx by FloatValue("VelocityX-SprintHit", 0.6F, 0.6F..1F) { mode in arrayOf("Intave") }
    private val Motionz by FloatValue("VelocityZ-SprintHit", 0.6F, 0.6F..1F) { mode in arrayOf("Intave") }


    // Intave Jump
    private val intavejump by BoolValue("Intave-Jump", false) { mode in arrayOf("Intave") }
    private val intavejumpticks by IntegerValue("Jump-Ticks", 2, 2..7) { mode in arrayOf("Intave") }

    // Values
    private var intaveTick = 0

    @EventTarget
    fun AttackEvent(event: AttackEvent) {
        when (mode.lowercase()) {
            "intave" -> {

                val motionx = Motionx
                val motionz = Motionz

                if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.isSprinting) {
                    mc.thePlayer.motionX *= motionx
                    mc.thePlayer.motionZ *= motionz
                    mc.thePlayer.isSprinting = false
                }
                if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.isSprinting) {
                    mc.thePlayer.motionX *= motionx
                    mc.thePlayer.motionZ *= motionz
                    mc.thePlayer.isSprinting = false
                }
                if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.isSprinting) {
                    mc.thePlayer.motionX *= motionx
                    mc.thePlayer.motionZ *= motionz
                    mc.thePlayer.isSprinting = false
                }
                if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.isSprinting) {
                    mc.thePlayer.motionX *= motionx
                    mc.thePlayer.motionZ *= motionz
                    mc.thePlayer.isSprinting = false
                }
            }
        }
    }

    @Override
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isInWater || thePlayer.isInLava || thePlayer.isInWeb || thePlayer.isDead)
            return

        when (mode.lowercase()) {
            "intave" -> {
                if (intavejump.takeIf {isActive} == true) {
                    intaveTick++
                    if (mc.thePlayer.hurtTime == intavejumpticks) {
                        if (thePlayer.onGround && intaveTick % 2 == 0) {
                            thePlayer.tryJump()
                            intaveTick = 0
                        }
                    }
                }
            }
        }
    }

    override val tag
        get() = mode

}