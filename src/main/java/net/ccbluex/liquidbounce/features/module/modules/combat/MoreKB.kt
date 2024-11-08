package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import net.ccbluex.liquidbounce.utils.timing.TimeUtils
import net.ccbluex.liquidbounce.value.*

object MoreKB : Module("MoreKB", Category.COMBAT, hideModule = false) {

    // Modes of operation
    private val mode by ListValue(
        "Mode",
        arrayOf("LegitFast"),
        "LegitFast"
    )

    // Timing and tick controls
    private val mindelay by IntegerValue("MinDelayTicks", 4, 0..10) { mode in arrayOf("LegitFast") }
    private val maxdelay by IntegerValue("MaxDelayTicks", 8, 0..10) { mode in arrayOf("LegitFast") }
    private val minlfticks by IntegerValue("MinTicks", 1, 1..10) { mode in arrayOf("LegitFast") }
    private val maxlfticks by IntegerValue("MaxTicks", 3, 1..10) { mode in arrayOf("LegitFast") }

    // KillAura and misc settings
    private val onlyKillaura by BoolValue("OnlyKillAura", false) { mode in arrayOf("LegitFast") }
    private val falseclientsprint by BoolValue("FalseClientSprint", true) { mode in arrayOf("LegitFast") }
    private val presssprint by BoolValue("PressSprint", true) { mode in arrayOf("LegitFast") }
    private val pressforward by BoolValue("PressForward", true) { mode in arrayOf("LegitFast") }
    private val debuglf by BoolValue("Debug", false) { mode in arrayOf("LegitFast") }

    // Internal timing variables for WTap and LegitFast modes
    private var legitfastTicks = 0

    // Init KillAura and CombatManager
    private var ka = KillAura
    private var cm = CombatManager

    // Resets on module toggle
    override fun onToggle(state: Boolean) {
        legitfastTicks = 0
    }

    @EventTarget
    fun onSprintUpdate(e: SprintUpdateEvent) {
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
        if (!MovementUtils.isMoving) return
        when (mode) {
            "LegitFast" -> handleLegitFast()
        }
    }

    private fun handleLegitFast() {
        if (onlyKillaura) {
            if (ka.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    if (debuglf) displayChatMessage("Start-False-Sprint")
                    legitfastTicks = nextInt(minlfticks, maxlfticks)
                }
            }
        } else {
            if (cm.target?.hurtTime == 10) {
                TimeUtils.delay(nextInt(mindelay, maxdelay)) {
                    if (debuglf) displayChatMessage("Start-False-Sprint")
                    legitfastTicks = nextInt(minlfticks, maxlfticks)
                }
            }
        }
    }

    override val tag
        get() = mode
}