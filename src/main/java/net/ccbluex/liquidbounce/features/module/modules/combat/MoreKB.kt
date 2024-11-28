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
    private val falseclientsprint by BoolValue("FalseClientSprint", true) { mode in arrayOf("LegitFast") }
    private val presssprint by BoolValue("PressSprint", true) { mode in arrayOf("LegitFast") }
    private val pressforward by BoolValue("PressForward", true) { mode in arrayOf("LegitFast") }

    // Internal timing variables for WTap and LegitFast modes
    private var legitfastTicks = 0
    private var wtapTicks = 0
    private var sneakTicks = 0

    // Init KillAura and CombatManager
    private var ka = KillAura
    private var cm = CombatManager

    // Fix Bug SaveMoveKeys
    var stopPenis = false

    // Resets on module toggle
    override fun onToggle(state: Boolean) {
        legitfastTicks = 0
        wtapTicks = 0
        sneakTicks = 0
        stopPenis = false
    }

    @EventTarget
    fun onSprint(event: SprintUpdateEvent) {
        if (!MovementUtils.isMoving) return
        when (mode) {
            "LegitFast" -> {
                if (legitfastTicks > 0 && mc.thePlayer.isSprinting) {
                    if (falseclientsprint) mc.thePlayer.isSprinting = false
                    mc.thePlayer.serverSprintState = false
                    if (pressforward) mc.gameSettings.keyBindForward.pressed = false
                    if (presssprint) mc.gameSettings.keyBindSprint.pressed = false
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
                handleWTap()
                if (wtapTicks > 1) {
                    stopPenis = true
                    mc.gameSettings.keyBindForward.pressed = false
                    wtapTicks--
                } else if (wtapTicks == 1) {
                    mc.gameSettings.keyBindForward.pressed = true
                    stopPenis = false
                    wtapTicks--
                }
            }
            "LegitSneak" -> {
                handleLegitSneak()
                if (sneakTicks > 1) {
                    stopPenis = true
                    mc.gameSettings.keyBindSneak.pressed = true
                    sneakTicks--
                } else if (sneakTicks == 1) {
                    stopPenis = false
                    mc.gameSettings.keyBindSneak.pressed = false
                    sneakTicks--
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

    private fun handleWTap() {
        if (onlyKillaura) {
            if (ka.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    wtapTicks = nextInt(minlfticks, maxlfticks) + 1
                }
            }
        } else {
            if (cm.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    wtapTicks = nextInt(minlfticks, maxlfticks) + 1
                }
            }
        }
    }

    private fun handleLegitSneak() {
        if (onlyKillaura) {
            if (ka.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    sneakTicks = nextInt(minlfticks, maxlfticks) + 1
                }
            }
        } else {
            if (cm.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    sneakTicks = nextInt(minlfticks, maxlfticks) + 1
                }
            }
        }
    }

    override val tag
        get() = mode
}