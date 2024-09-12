/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.RotationUtils.getAngleDifference
import net.ccbluex.liquidbounce.utils.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.*
import kotlin.math.abs

object MoreKB : Module("MoreKB", Category.COMBAT, hideModule = false) {

    private val delay by IntegerValue("Delay", 0, -50..50) { mode in arrayOf("WTap", "PacketFast", "LegitFastNew") }
    private val hurtTime by IntegerValue("Tick", 0, 0..10) { mode in arrayOf("WTap", "PacketFast") }

    private val mode by ListValue(
        "Mode",
        arrayOf("WTap", "PacketFast", "LegitFast", "LegitFastNew"),
        "LegitFastNew"
    )

    private val maxTicksUntilBlock: IntegerValue = object : IntegerValue("MaxTicksUntilBlock", 2, 0..5) {
        override fun isSupported() = mode == "WTap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minTicksUntilBlock.get())
    }

    private val legitfastnewticks by IntegerValue("LegitFastTicks", 1, 1..5) { mode in arrayOf("LegitFastNew") }

    private val minTicksUntilBlock: IntegerValue = object : IntegerValue("MinTicksUntilBlock", 0, 0..5) {
        override fun isSupported() = mode == "WTap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxTicksUntilBlock.get())
    }

    private val reSprintMaxTicks: IntegerValue = object : IntegerValue("ReSprintMaxTicks", 2, 1..5) {
        override fun isSupported() = mode == "WTap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(reSprintMinTicks.get())
    }

    private val reSprintMinTicks: IntegerValue = object : IntegerValue("ReSprintMinTicks", 1, 1..5) {
        override fun isSupported() = mode == "WTap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(reSprintMaxTicks.get())
    }

    private val targetDistance by IntegerValue("TargetDistance", 3, 1..5) { mode == "WTap" }


    private val legitfaststopTicks: IntegerValue = object : IntegerValue("SprintTicks", 1, 1..5) {
        override fun isSupported() = mode == "LegitFast"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(legitfastSprintTicks.get())
    }

    private val legitfastSprintTicks: IntegerValue = object : IntegerValue("ReleaseSprintTicks", 2, 1..5) {
        override fun isSupported() = mode == "LegitFast"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(legitfaststopTicks.get())
    }

    private val minEnemyRotDiffToIgnore by FloatValue("MinRotationDiffFromEnemyToIgnore", 180f, 0f..180f)

    val onlyMove by BoolValue("OnlyMove", true)
    val onlyMoveForward by BoolValue("OnlyMoveForward", false) { onlyMove }

    private var ticks = 0

    // WTap
    private var blockInputTicks = randomDelay(minTicksUntilBlock.get(), maxTicksUntilBlock.get())
    private var blockTicksElapsed = 0
    private var startWaiting = false
    private var blockInput = false
    private var allowInputTicks = randomDelay(reSprintMinTicks.get(), reSprintMaxTicks.get())
    private var ticksElapsed = 0

    // LegitFast
    private var legitfastTicks = 0
    private var legitfastTicksNew = 0

    override fun onToggle(state: Boolean) {
        // Make sure the user won't have their input forever blocked
        blockInput = false
        startWaiting = false
        blockTicksElapsed = 0
        ticksElapsed = 0
        legitfastTicks = 0
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val player = mc.thePlayer ?: return
        val target = event.targetEntity as? EntityLivingBase ?: return
        val distance = player.getDistanceToEntityBox(target)

        val rotationToPlayer = toRotation(player.hitBox.center, false, target).fixedSensitivity().yaw
        val angleDifferenceToPlayer = getAngleDifference(rotationToPlayer, target.rotationYaw)


        if (onlyMove && (!isMoving || onlyMoveForward && player.movementInput.moveStrafe != 0f)) return

        // Is the enemy facing his back on us?
        if (angleDifferenceToPlayer > minEnemyRotDiffToIgnore && !target.hitBox.isVecInside(player.eyes)) return

        when (mode) {
            "PacketFast" -> if (player.isSprinting && player.serverSprintState) ticks = 1

            "WTap" -> {
                // We want the player to be sprinting before we block inputs
                if (player.isSprinting && player.serverSprintState && !blockInput && !startWaiting) {
                    val delayMultiplier = 1.0 / (abs(targetDistance - distance) + 1)

                    blockInputTicks = (randomDelay(
                        minTicksUntilBlock.get(),
                        maxTicksUntilBlock.get()
                    ) * delayMultiplier).toInt()

                    blockInput = blockInputTicks == 0

                    if (!blockInput) {
                        startWaiting = true
                    }

                    allowInputTicks = (randomDelay(
                        reSprintMinTicks.get(),
                        reSprintMaxTicks.get()
                    ) * delayMultiplier).toInt()
                }
            }
            "LegitFastNew" -> {
                legitfastTicksNew = legitfastnewticks
            }
            "LegitFast" -> {
                if (legitfastTicks == legitfaststopTicks.get()) {
                    if (player.isSprinting) {
                        player.isSprinting = false
                        player.serverSprintState = false
                    } else {
                        if (player.moveForward > 0.8) {
                            player.isSprinting = true
                            player.serverSprintState = true
                        }
                    }
                } else if (legitfastTicks >= legitfastSprintTicks.get()) {
                    player.isSprinting = false
                    player.serverSprintState = false
                }
            }
        }
    }

    @EventTarget
    fun onSprint(event: PostSprintUpdateEvent) {
        if (mode == "LegitFastNew") {
            if (legitfastTicksNew == legitfastnewticks) {
                mc.thePlayer.isSprinting = false
                mc.thePlayer.serverSprintState = false
                legitfastTicksNew--
            }
        }
    }

    @EventTarget
    fun onUpdate(event : UpdateEvent) {
        if (mode == "WTap") {
            if (blockInput) {
                if (ticksElapsed++ >= allowInputTicks) {
                    blockInput = false
                    ticksElapsed = 0
                }
            } else {
                if (startWaiting) {
                    blockInput = blockTicksElapsed++ >= blockInputTicks

                    if (blockInput) {
                        startWaiting = false
                        blockTicksElapsed = 0

                    }
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event : PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (packet is C03PacketPlayer && mode == "PacketFast") {
            if (ticks == 1 && player.isSprinting) {
                if (player.moveForward > 0.7999) {
                    sendPacket(C0BPacketEntityAction(player, START_SPRINTING))
                    ticks--
                }
                if (ticks == 2) {
                    sendPacket(C0BPacketEntityAction(player, STOP_SPRINTING))
                    ticks--
                }
            }
        }
    }

    fun shouldBlockInput() = handleEvents() && mode == "WTap" && blockInput

    override val tag
        get() = mode
}