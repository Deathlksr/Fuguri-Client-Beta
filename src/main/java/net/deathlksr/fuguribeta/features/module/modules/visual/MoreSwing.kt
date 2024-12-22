package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.GameTickEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.modules.combat.KillAura

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