package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.timing.TimeUtils
import net.ccbluex.liquidbounce.value.*

object MoreKB : Module("MoreKB", Category.COMBAT, hideModule = false, forcedDescription = "Deal more knock back") {

    // Modes of operation
    private val mode by ListValue(
        "Mode",
        arrayOf("LegitFast", "Legit", "LegitSneak"),
        "LegitFast"
    )

    // Timing and tick controls
    private val mindelay by IntegerValue("MinDelayTicks", 4, 0..10) { mode in arrayOf("LegitFast", "Legit", "LegitSneak") }
    private val maxdelay by IntegerValue("MaxDelayTicks", 8, 0..10) { mode in arrayOf("LegitFast", "Legit", "LegitSneak") }
    private val minlfticks by IntegerValue("MinTicks", 1, 1..10) { mode in arrayOf("LegitFast", "Legit", "LegitSneak") }
    private val maxlfticks by IntegerValue("MaxTicks", 3, 1..10) { mode in arrayOf("LegitFast", "Legit", "LegitSneak") }

    // KillAura and misc settings
    private val onlyKillaura by BoolValue("OnlyKillAura", false) { mode in arrayOf("LegitFast", "Legit", "LegitSneak") }

    // Internal timing variables for WTap and LegitFast modes
    private var legitfastTicks = 0
    private var ticks = 0

    // Init KillAura and CombatManager
    private var ka = KillAura
    private var cm = CombatManager

    // Fix Bug SaveMoveKeys
    var stopPenis = false

    // Resets on module toggle
    override fun onToggle(state: Boolean) {
        legitfastTicks = 0
        ticks = 0
        stopPenis = false
    }

    @EventTarget
    fun onSprint(event: SprintUpdateEvent) {
        if (!MovementUtils.isMoving) return
        when (mode) {
            "LegitFast" -> {
                if (legitfastTicks > 0 && mc.thePlayer.isSprinting) {
                    mc.thePlayer.isSprinting = false
                    mc.thePlayer.serverSprintState = false
                    legitfastTicks--
                }
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        when (mode) {
            "LegitFast" -> handleLegitFast()
            "Legit" -> {
                handleOther()
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
            "LegitSneak" -> {
                handleOther()
                if (ticks > 1) {
                    stopPenis = true
                    mc.gameSettings.keyBindSneak.pressed = true
                    ticks--
                } else if (ticks == 1) {
                    stopPenis = false
                    mc.gameSettings.keyBindSneak.pressed = false
                    ticks--
                }
            }
        }
    }

    private fun handleLegitFast() {
        if (onlyKillaura) {
            if (ka.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    legitfastTicks = nextInt(minlfticks, maxlfticks)
                }
            }
        } else {
            if (cm.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    legitfastTicks = nextInt(minlfticks, maxlfticks)
                }
            }
        }
    }

    private fun handleOther() {
        if (onlyKillaura) {
            if (ka.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    ticks = nextInt(minlfticks, maxlfticks) + 1
                }
            }
        } else {
            if (cm.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    ticks = nextInt(minlfticks, maxlfticks) + 1
                }
            }
        }
    }

    override val tag
        get() = mode
}