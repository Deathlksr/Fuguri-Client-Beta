/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.modules.movement.nowebmodes.aac.*
import net.deathlksr.fuguribeta.features.module.modules.movement.nowebmodes.intave.*
import net.deathlksr.fuguribeta.features.module.modules.movement.nowebmodes.other.*
import net.deathlksr.fuguribeta.value.ListValue

object NoWeb : Module("NoWeb", Category.MOVEMENT, hideModule = false) {

    private val noWebModes = arrayOf(
        // Vanilla
        None,

        // AAC
        AAC, LAAC,

        // Intave
        OldMineBlaze,
        NewMineBlaze,
        
        // Other
        Rewi
    )

    private val modes = noWebModes.map { it.modeName }.toTypedArray()

    val mode by ListValue(
        "Mode", modes, "None"
    )

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        modeModule.onUpdate()
    }

    override val tag
        get() = mode

    private val modeModule
        get() = noWebModes.find { it.modeName == mode }!!
}
