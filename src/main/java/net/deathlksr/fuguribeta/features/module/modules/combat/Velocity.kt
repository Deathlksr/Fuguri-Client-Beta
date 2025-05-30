package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.extensions.toDegrees
import net.deathlksr.fuguribeta.utils.extensions.tryJump
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.nextInt
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import kotlin.math.abs
import kotlin.math.atan2

object Velocity : Module("Velocity", Category.COMBAT, hideModule = false) {

    private val mode by ListValue(
        "Mode", arrayOf(
            "Intave", "IntaveReduce"
        ), "Intave"
    )

    private val hurtTime by IntegerValue("HurtTime", 6, 0..10) { mode in arrayOf("IntaveReduce") }
    private val Motionxz by FloatValue("XZ-SprintHit", 0.6F, 0F..1F, true) { mode in arrayOf("Intave") }
    private val MotionnotSprintxz by FloatValue("XZ-Hit", 1.0F, 0F..1F) { mode in arrayOf("Intave", "IntaveReduce") }
    private val falsesprint by BoolValue("FalseClientSprint", true) { mode in arrayOf("Intave") }
    private val intavejump by BoolValue("Jump", false) { mode in arrayOf("Intave", "IntaveReduce") }
    private val chance by IntegerValue("Chance", 100, 0..100) { intavejump }
    private val jumpCooldownMode by ListValue("JumpCooldownMode", arrayOf("Ticks", "ReceivedHits"), "Ticks") { intavejump }
    private val ticksUntilJump by IntegerValue("TicksUntilJump", 4, 0..20) { jumpCooldownMode == "Ticks" && intavejump }
    private val hitsUntilJump by IntegerValue("ReceivedHitsUntilJump", 2, 0..5) { jumpCooldownMode == "ReceivedHits" && intavejump }

    // Intave Jump
    private var limitUntilJump = 0
    private var hasReceivedVelocity = false

    @EventTarget
    fun onAttack(event: AttackEvent) {
        when (mode.lowercase()) {
            "intave" -> {
                if (mc.thePlayer.hurtTime > 0 && mc.thePlayer.isSprinting) {
                    mc.thePlayer.motionX *= Motionxz
                    mc.thePlayer.motionZ *= Motionxz
                    if (falsesprint) mc.thePlayer.isSprinting = false
                } else if (mc.thePlayer.hurtTime > 0) {
                    mc.thePlayer.motionX *= MotionnotSprintxz
                    mc.thePlayer.motionZ *= MotionnotSprintxz
                    if (falsesprint) mc.thePlayer.isSprinting = false
                }
            }
            "intavereduce" -> {
                if (mc.thePlayer.hurtTime == hurtTime) {
                    mc.thePlayer.motionX *= MotionnotSprintxz
                    mc.thePlayer.motionZ *= MotionnotSprintxz
                }
            }
        }
    }

    // TODO: Recode
    private fun getDirection(): Double {
        var moveYaw = mc.thePlayer.rotationYaw
        if (mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing == 0f) {
            moveYaw += if (mc.thePlayer.moveForward > 0) 0 else 180
        } else if (mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing != 0f) {
            if (mc.thePlayer.moveForward > 0) moveYaw += if (mc.thePlayer.moveStrafing > 0) -45 else 45 else moveYaw -= if (mc.thePlayer.moveStrafing > 0) -45 else 45
            moveYaw += if (mc.thePlayer.moveForward > 0) 0 else 180
        } else if (mc.thePlayer.moveStrafing != 0f && mc.thePlayer.moveForward == 0f) {
            moveYaw += if (mc.thePlayer.moveStrafing > 0) -90 else 90
        }
        return Math.floorMod(moveYaw.toInt(), 360).toDouble()
    }

    @EventTarget(priority = 1)
    fun onPacket(event: PacketEvent) {
        val thePlayer = mc.thePlayer ?: return

        val packet = event.packet

        when (mode.lowercase()) {
            "intave" -> {
                if (intavejump) {
                    // TODO: Recode and make all velocity modes support velocity direction checks
                    var packetDirection = 0.0
                    when (packet) {
                        is S12PacketEntityVelocity -> {
                            val motionX = packet.motionX.toDouble()
                            val motionZ = packet.motionZ.toDouble()

                            packetDirection = atan2(motionX, motionZ)
                        }

                        is S27PacketExplosion -> {
                            val motionX = thePlayer.motionX + packet.field_149152_f
                            val motionZ = thePlayer.motionZ + packet.field_149159_h

                            packetDirection = atan2(motionX, motionZ)
                        }
                    }
                    val degreePlayer = getDirection()
                    val degreePacket = Math.floorMod(packetDirection.toDegrees().toInt(), 360).toDouble()
                    var angle = abs(degreePacket + degreePlayer)
                    val threshold = 120.0
                    angle = Math.floorMod(angle.toInt(), 360).toDouble()
                    val inRange = angle in 180 - threshold / 2..180 + threshold / 2
                    if (inRange)
                        hasReceivedVelocity = true
                }
            }

            "intavereduce" -> {
                if (intavejump) {
                    // TODO: Recode and make all velocity modes support velocity direction checks
                    var packetDirection = 0.0
                    when (packet) {
                        is S12PacketEntityVelocity -> {
                            val motionX = packet.motionX.toDouble()
                            val motionZ = packet.motionZ.toDouble()

                            packetDirection = atan2(motionX, motionZ)
                        }

                        is S27PacketExplosion -> {
                            val motionX = thePlayer.motionX + packet.field_149152_f
                            val motionZ = thePlayer.motionZ + packet.field_149159_h

                            packetDirection = atan2(motionX, motionZ)
                        }
                    }
                    val degreePlayer = getDirection()
                    val degreePacket = Math.floorMod(packetDirection.toDegrees().toInt(), 360).toDouble()
                    var angle = abs(degreePacket + degreePlayer)
                    val threshold = 120.0
                    angle = Math.floorMod(angle.toInt(), 360).toDouble()
                    val inRange = angle in 180 - threshold / 2..180 + threshold / 2
                    if (inRange)
                        hasReceivedVelocity = true
                }
            }
        }
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        val player = mc.thePlayer ?: return

        if (mode == "Intave" && hasReceivedVelocity && intavejump || mode == "IntaveReduce" && hasReceivedVelocity && intavejump) {
            if (!player.isJumping && nextInt(endExclusive = 100) < chance && shouldJump() && player.isSprinting && player.onGround && player.hurtTime == 9) {
                player.tryJump()
                limitUntilJump = 0
            }
            hasReceivedVelocity = false
            return
        }

        when (jumpCooldownMode.lowercase()) {
            "ticks" -> limitUntilJump++
            "receivedhits" -> if (player.hurtTime == 9) limitUntilJump++
        }
    }

    private fun shouldJump() = when (jumpCooldownMode.lowercase()) {
        "ticks" -> limitUntilJump >= ticksUntilJump
        "receivedhits" -> limitUntilJump >= hitsUntilJump
        else -> false
    }

    override val tag
        get() = mode
}