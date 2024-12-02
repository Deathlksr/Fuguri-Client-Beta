package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura

object MoreSwing : Module("MoreSwing", Category.VISUAL, hideModule = false) {

    @EventTarget
    fun onTick(event: GameTickEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return

        if (KillAura.target != null) {
            if (mc.thePlayer.swingProgressInt >= 3 || mc.thePlayer.swingProgressInt < 0) {
                mc.thePlayer.swingProgressInt = -1
                mc.thePlayer.isSwingInProgress = true
            }
        }
    }
}