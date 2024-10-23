package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.MovementUtils.isMoving
import net.ccbluex.liquidbounce.utils.RotationUtils.getAngleDifference
import net.ccbluex.liquidbounce.utils.RotationUtils.toRotation
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.timing.TimeUtils
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.ccbluex.liquidbounce.value.*
import net.minecraft.entity.EntityLivingBase
import kotlin.math.abs

object MoreKB : Module("MoreKB", Category.COMBAT, hideModule = false) {

    // Modes of operation
    private val mode by ListValue(
        "Mode",
        arrayOf("WTap", "LegitFast", "WTapNew"),
        "LegitFast"
    )

    // Timing and tick controls
    private val delay by IntegerValue("Delay", 0, 0..250) { mode in arrayOf("WTap") }
    private val hurtTime by IntegerValue("Tick", 0, 0..10) { mode in arrayOf("WTap") }
    private val mindelay by IntegerValue("MinDelayTicks", 4, 0..10) { mode in arrayOf("LegitFast") }
    private val maxdelay by IntegerValue("MaxDelayTicks", 8, 0..10) { mode in arrayOf("LegitFast") }
    private val minlfticks by IntegerValue("MinTicks", 1, 1..10) { mode in arrayOf("LegitFast", "WTapNew") }
    private val maxlfticks by IntegerValue("MaxTicks", 3, 1..10) { mode in arrayOf("LegitFast", "WTapNew") }

    // KillAura and misc settings
    private val onlyKillaura by BoolValue("OnlyKillAura", false) { mode in arrayOf("LegitFast") }
    private val falseclientsprint by BoolValue("FalseClientSprint", true) { mode in arrayOf("LegitFast") }
    private val presssprint by BoolValue("PressSprint", true) { mode in arrayOf("LegitFast") }
    private val pressforward by BoolValue("PressForward", true) { mode in arrayOf("LegitFast") }
    private val debuglf by BoolValue("Debug", false) { mode in arrayOf("LegitFast") }

    // WTap specific settings
    private val maxTicksUntilBlock: IntegerValue = object : IntegerValue("MaxTicksUntilBlock", 2, 0..5) {
        override fun isSupported() = mode == "WTap"

        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minTicksUntilBlock.get())
    }

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
    private val minEnemyRotDiffToIgnore by FloatValue("MinRotationDiffFromEnemyToIgnore", 180f, 0f..180f)

    // Movement-related options
    val onlyMove by BoolValue("OnlyMove", true)
    val onlyMoveForward by BoolValue("OnlyMoveForward", false) { onlyMove }

    // Internal timing variables for WTap and LegitFast modes
    private var blockInputTicks = randomDelay(minTicksUntilBlock.get(), maxTicksUntilBlock.get())
    private var blockTicksElapsed = 0
    private var startWaiting = false
    private var blockInput = false
    private var allowInputTicks = randomDelay(reSprintMinTicks.get(), reSprintMaxTicks.get())
    private var ticksElapsed = 0
    private var legitfastTicks = 0
    private var wtaptick = 0

    // Resets on module toggle
    override fun onToggle(state: Boolean) {
        blockInput = false
        startWaiting = false
        blockTicksElapsed = 0
        ticksElapsed = 0
        legitfastTicks = 0
        wtaptick = 0
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val player = mc.thePlayer ?: return
        val target = event.targetEntity as? EntityLivingBase ?: return
        val distance = player.getDistanceToEntityBox(target)

        val rotationToPlayer = toRotation(player.hitBox.center, false, target).fixedSensitivity().yaw
        val angleDifferenceToPlayer = getAngleDifference(rotationToPlayer, target.rotationYaw)

        if (onlyMove && (!isMoving || onlyMoveForward && player.movementInput.moveStrafe != 0f)) return

        if (angleDifferenceToPlayer > minEnemyRotDiffToIgnore && !target.hitBox.isVecInside(player.eyes)) return

        when (mode) {
            "WTap" -> {
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
        }
    }

    @EventTarget
    fun onSprintUpdate(event: SprintUpdateEvent) {
        when (mode) {
            "LegitFast" -> {
                if (legitfastTicks > 0 && mc.thePlayer.isSprinting) {
                    if (falseclientsprint) mc.thePlayer.isSprinting = false
                    mc.thePlayer.serverSprintState = false
                    if (presssprint) mc.gameSettings.keyBindSprint.pressed = false
                    if (pressforward) mc.gameSettings.keyBindForward.pressed = false
                    if (debuglf) displayChatMessage("Falsely-Sprint")
                    legitfastTicks--
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (mode) {
            "WTap" -> handleWTap()
            "LegitFast" -> handleLegitFast()
            "WTapNew" -> handleWTapNew()
        }
    }

    private fun handleWTap() {
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

    private fun handleWTapNew() {
        if (KillAura.target?.hurtTime == 10) {
            wtaptick = nextInt(minlfticks, maxlfticks)
        }
        if (wtaptick > 0 && mc.thePlayer.isSprinting) {
            mc.thePlayer.isSprinting = false
            mc.gameSettings.keyBindForward.pressed = false
            wtaptick--
        }
    }

    private fun handleLegitFast() {
        if (onlyKillaura && KillAura.target?.hurtTime == 10) {
            TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                if (debuglf) displayChatMessage("Start-False-Sprint")
                legitfastTicks = nextInt(minlfticks, maxlfticks)
            }
        } else if (CombatManager.target?.hurtTime == 10) {
            TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                if (debuglf) displayChatMessage("Start-False-Sprint")
                legitfastTicks = nextInt(minlfticks, maxlfticks)
            }
        }
    }

    fun shouldBlockInput() = handleEvents() && mode == "WTap" && blockInput

    override val tag
        get() = mode
}