/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.getDistanceToEntityBox
import net.ccbluex.liquidbounce.utils.timing.TimeUtils
import net.ccbluex.liquidbounce.value.FloatValue

object TickBase : Module("TickBase", Category.COMBAT) {
    var killAura: KillAura? = null
    var stop: Boolean = false
    private var balance: Int = 0
    var timer: TimeUtils = TimeUtils
    var x: Double = 0.0
    var y: Double = 0.0
    var z: Double = 0.0
    private var stopping: Boolean = false

    private val startRange by FloatValue("Start Range", 3.0F, 3.0F..6.0F)

    override fun onEnable() {
        this.killAura = KillAura
    }

    fun onRunGameLoop() {
        if (killAura!!.target == null) {
            this.balance = 0
            mc.timer.timerSpeed = 1.0f
        } else {
            if (killAura!!.target?.let { mc.thePlayer.getDistanceToEntityBox(it) }!! < startRange && mc.thePlayer.moveForward as Double > 0.8 && timer.hasReached(
                    400.0
                )
            ) {
                try {
                    var stopCycle = false

                    while (!stopCycle) {
                        if (this.needStop() || this.stop) {
                            stopCycle = true
                        }

                        if (!stopCycle) {
                            mc.runTick()
                            ++this.balance
                            timer.reset()
                        }
                    }
                } catch (var2: Exception) {
                }
            }
        }
    }

    fun onTimeDelay() {
        if (killAura!!.target == null) {
            this.balance = 0
            this.stopping = false
        } else {
            if (this.balance > 0) {
                mc.timer.timerSpeed = 0.0f
                timer.reset()
            }

            if (mc.timer.timerSpeed == 0.0f) {
                --this.balance
            }

            if (this.balance == 0) {
                this.stop = false
                mc.timer.timerSpeed = 1.0f
            }
        }
    }

    fun onKillAuraAttack() {
        this.stop = true
    }

    private fun needStop(): Boolean {
        var stop = false
        if (killAura!!.target == null) {
            stop = true
            return stop
        } else {
            if (killAura!!.target!!.hurtTime > 0 || mc.thePlayer.hurtTime > 0) {
                stop = true
            }

            if (mc.thePlayer.moveForward <= 0.08 || !mc.gameSettings.keyBindForward.pressed) {
                stop = true
            }

            if (mc.thePlayer.isInWater) {
                stop = true
            }

            if (killAura!!.target?.let { mc.thePlayer.getDistanceToEntityBox(it) }!! < 2.5) {
                stop = true
            }

            if (this.balance >= 50) {
                stop = true
            }

            return stop
        }
    }
}
