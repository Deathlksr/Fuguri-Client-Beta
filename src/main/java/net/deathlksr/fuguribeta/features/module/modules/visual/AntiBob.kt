package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.MotionEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category

object AntiBob : Module("AntiBob", Category.VISUAL, gameDetecting = false, hideModule = false) {

    @EventTarget
    fun onMotion(event: MotionEvent) {
        mc.thePlayer?.distanceWalkedModified = -1f
    }
}
