/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.flymodes

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.utils.MinecraftInstance

open class FlyMode(val modeName: String): MinecraftInstance() {
	open fun onMove(event: MoveEvent) {}
	open fun onPacket(event: PacketEvent) {}
	open fun onRender3D(event: Render3DEvent) {}
	open fun onBB(event: BlockBBEvent) {}
	open fun onJump(event: JumpEvent) {}
	open fun onStep(event: StepEvent) {}
	open fun onMotion(event: MotionEvent) {}
	open fun onUpdate() {}

	open fun onEnable() {}
	open fun onDisable() {}
	open fun onTick () {}
}