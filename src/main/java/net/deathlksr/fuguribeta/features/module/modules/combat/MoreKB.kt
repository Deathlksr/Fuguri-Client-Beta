package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.handler.combat.CombatManager
import net.deathlksr.fuguribeta.utils.MovementUtils
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.nextInt
import net.deathlksr.fuguribeta.utils.timing.TimeUtils
import net.deathlksr.fuguribeta.value.*

object MoreKB : Module("MoreKB", Category.COMBAT, hideModule = false, forcedDescription = "Deal more knock back") {

    // Modes of operation
    private val mode by ListValue(
        "Mode",
        arrayOf("LegitFast", "Legit"),
        "LegitFast"
    )

    // Timing and tick controls
    private val minDelay by IntegerValue("MinDelayTicks", 3, 0..10) { mode in arrayOf("LegitFast", "Legit") }
    private val maxDelay by IntegerValue("MaxDelayTicks", 3, 0..10) { mode in arrayOf("LegitFast", "Legit") }
    private val minTicks by IntegerValue("MinTicks", 1, 1..10) { mode in arrayOf("LegitFast", "Legit") }
    private val maxTicks by IntegerValue("MaxTicks", 1, 1..10) { mode in arrayOf("LegitFast", "Legit") }

    // KillAura and misc settings
    private val onlyKillAura by BoolValue("OnlyKillAura", false) { mode in arrayOf("LegitFast", "Legit") }

    // Internal timing variables for WTap and LegitFast modes
    private var ticks = 0

    // Init KillAura and CombatManager
    private var ka = KillAura
    private var cm = CombatManager

    // Fix Bug SaveMoveKeys
    var stopPenis = false

    // Resets on module toggle
    override fun onToggle(state: Boolean) {
        ticks = 0
        stopPenis = false
    }

    @EventTarget
    fun onSprint(event: SprintUpdateEvent) {
        if (!MovementUtils.isMoving) return
        when (mode) {
            "LegitFast" -> {
                if (ticks > 0 && mc.thePlayer.isSprinting && mc.thePlayer.serverSprintState) {
                    mc.thePlayer.isSprinting = false
                    mc.thePlayer.serverSprintState = false
                    ticks--
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        handleOther()
        when (mode) {
            "Legit" -> {
                if (ticks > 1) {
                    stopPenis = true
                    mc.gameSettings.keyBindForward.pressed = false
                    ticks--
                } else if (ticks == 1) {
                    stopPenis = false
                    mc.gameSettings.keyBindForward.pressed = true
                    ticks--
                }
            }
        }
    }

    private fun handleOther() {
        if (onlyKillAura) {
            if (ka.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(minDelay, maxDelay)) {
                    ticks = nextInt(minTicks, maxTicks) + if (mode == "LegitFast") 0 else 1
                }
            }
        } else {
            if (cm.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(minDelay, maxDelay)) {
                    ticks = nextInt(minTicks, maxTicks) + if (mode == "LegitFast") 0 else 1
                }
            }
        }
    }

    override val tag
        get() = mode
}