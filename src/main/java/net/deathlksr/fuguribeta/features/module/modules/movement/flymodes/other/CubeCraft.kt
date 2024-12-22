/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.other

import net.deathlksr.fuguribeta.event.MoveEvent
import net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.FlyMode
import net.deathlksr.fuguribeta.utils.extensions.toRadiansD
import net.deathlksr.fuguribeta.utils.timing.TickTimer
import kotlin.math.cos
import kotlin.math.sin

object CubeCraft : FlyMode("CubeCraft") {
	private val tickTimer = TickTimer()

	override fun onUpdate() {
		mc.timer.timerSpeed = 0.6f
		tickTimer.update()
	}

	override fun onMove(event: MoveEvent) {
		val yaw = mc.thePlayer.rotationYaw.toRadiansD()

		if (tickTimer.hasTimePassed(2)) {
			event.x = -sin(yaw) * 2.4
			event.z = cos(yaw) * 2.4
			tickTimer.reset()
		} else {
			event.x = -sin(yaw) * 0.2
			event.z = cos(yaw) * 0.2
		}
	}
}
