/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes

import net.deathlksr.fuguribeta.event.JumpEvent
import net.deathlksr.fuguribeta.event.MoveEvent
import net.deathlksr.fuguribeta.utils.MinecraftInstance

open class LongJumpMode(val modeName: String) : MinecraftInstance() {
    open fun onUpdate() {}
    open fun onMove(event: MoveEvent) {}
    open fun onJump(event: JumpEvent) {}

    open fun onEnable() {}
    open fun onDisable() {}
}