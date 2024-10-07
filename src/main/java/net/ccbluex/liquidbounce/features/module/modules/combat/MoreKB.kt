package net.ccbluex.liquidbounce.features.module.modules.combat

import kotlinx.coroutines.delay
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
import net.ccbluex.liquidbounce.utils.timing.TimeUtils.randomDelay
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import kotlin.math.abs

object MoreKB : Module("MoreKB", Category.COMBAT, hideModule = false) {

    private val mode by ListValue(
        "Mode",
        arrayOf("WTap", "PacketFast", "LegitFast", "Legit"),
        "LegitFast"
    )

    private val delay by IntegerValue("Delay", 0, -50..50) { mode in arrayOf("WTap") }
    private val hurtTime by IntegerValue("Tick", 0, 0..10) { mode in arrayOf("WTap") }
    private val mindelay by IntegerValue("MinTiming", 2, 0..10) { mode in arrayOf("LegitFast", "Legit") }
    private val maxdelay by IntegerValue("MaxTiming", 2, 0..10) { mode in arrayOf("LegitFast", "Legit") }
    private val minlfticks by IntegerValue("MinTicks", 1, 1..10) { mode in arrayOf("LegitFast", "Legit") }
    private val maxlfticks by IntegerValue("MaxTicks", 2, 1..10) { mode in arrayOf("LegitFast", "Legit") }
    private val onlyKillaura by BoolValue("OnlyKillAura", false) { mode in arrayOf("LegitFast", "Legit") }
    private val fixlf by BoolValue("Fix1", true) { mode in arrayOf("LegitFast", "Legit") }
    private val fix2lf by BoolValue("Fix2", true) { mode in arrayOf("LegitFast", "Legit") }
    private val debuglf by BoolValue("Debug", false) { mode in arrayOf("LegitFast", "Legit") }

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

    val onlyMove by BoolValue("OnlyMove", true)
    val onlyMoveForward by BoolValue("OnlyMoveForward", false) { onlyMove }

    // WTap
    private var blockInputTicks = randomDelay(minTicksUntilBlock.get(), maxTicksUntilBlock.get())
    private var blockTicksElapsed = 0
    private var startWaiting = false
    private var blockInput = false
    private var allowInputTicks = randomDelay(reSprintMinTicks.get(), reSprintMaxTicks.get())
    private var ticksElapsed = 0

    // LegitFast
    private var legitfastTicks = 0

    // Legit
    private var legitticks = 0

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
        }
    }

        @EventTarget
        fun onSprint(event: PostSprintUpdateEvent) {
            if (mode == "LegitFast") {
                if (legitfastTicks > 0 && mc.thePlayer.isSprinting) {
                    mc.thePlayer.isSprinting = false
                    mc.thePlayer.serverSprintState = false
                    if (fixlf.takeIf { isActive } == true) {
                        mc.gameSettings.keyBindSprint.pressed = false
                    }
                    if (fix2lf.takeIf { isActive } == true) {
                        mc.gameSettings.keyBindForward.pressed = false
                    }
                    if (debuglf.takeIf { isActive } == true) {
                        displayChatMessage("Sprinting")
                    }
                    legitfastTicks--
                }
            }

            if (mode == "Legit") {
                if (legitticks > 0 && mc.thePlayer.isSprinting) {
                    mc.thePlayer.serverSprintState = false
                    if (fixlf.takeIf { isActive } == true) {
                        mc.gameSettings.keyBindSprint.pressed = false
                    }
                    if (fix2lf.takeIf { isActive } == true) {
                        mc.gameSettings.keyBindForward.pressed = false
                    }
                    if (debuglf.takeIf { isActive } == true) {
                        displayChatMessage("Tapped")
                    }
                    legitticks--
                }
            }
        }

        @EventTarget
        fun onUpdate(event: UpdateEvent) {
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

            if (mode == "LegitFast") {
                if (onlyKillaura.takeIf { isActive } == true) {
                    if (KillAura.target?.hurtTime == nextInt(mindelay, maxdelay)) {
                        if (debuglf.takeIf { isActive } == true) {
                            displayChatMessage("Start Sprinting")
                        }
                        legitfastTicks = nextInt(minlfticks, maxlfticks)
                    }
                } else {
                    if (CombatManager.target?.hurtTime == nextInt(mindelay, maxdelay)) {
                        if (debuglf.takeIf { isActive } == true) {
                            displayChatMessage("Start Sprinting")
                        }
                        legitfastTicks = nextInt(minlfticks, maxlfticks)
                    }
                }
            }

            if (mode == "Legit") {
                if (onlyKillaura.takeIf { isActive } == true) {
                    if (KillAura.target?.hurtTime == nextInt(mindelay, maxdelay)) {
                        if (debuglf.takeIf { isActive } == true) {
                            displayChatMessage("Start-Tap")
                        }
                        legitticks = nextInt(minlfticks, maxlfticks)
                    }
                } else {
                    if (CombatManager.target?.hurtTime == nextInt(mindelay, maxdelay)) {
                        if (debuglf.takeIf { isActive } == true) {
                            displayChatMessage("Start-Tap")
                        }
                        legitticks = nextInt(minlfticks, maxlfticks)
                    }
                }
            }
        }

        fun shouldBlockInput() = handleEvents() && mode == "WTap" && blockInput

        override val tag
        get() = mode
}