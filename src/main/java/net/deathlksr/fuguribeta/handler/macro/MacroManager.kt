/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.handler.macro

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.KeyEvent
import net.deathlksr.fuguribeta.event.Listenable
import net.deathlksr.fuguribeta.utils.MinecraftInstance

object MacroManager : MinecraftInstance(), Listenable {
    val macros = ArrayList<Macro>()

    @EventTarget
    fun onKey(event: KeyEvent) {
        macros.filter { it.key == event.key }.forEach { it.exec() }
    }

    override fun handleEvents() = true
}