/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes

import net.deathlksr.fuguribeta.event.JumpEvent
import net.deathlksr.fuguribeta.event.MoveEvent
import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.utils.MinecraftInstance

open class SpeedMode(val modeName: String) : MinecraftInstance() {
    open fun onMotion() {}
    open fun onUpdate() {}
    open fun onMove(event: MoveEvent) {}
    open fun onTick() {}
    open fun onStrafe() {}
    open fun onJump(event: JumpEvent) {}
    open fun onPacket(event: PacketEvent) {}
    open fun onEnable() {}
    open fun onDisable() {}

}