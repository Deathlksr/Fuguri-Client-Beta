/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.other

import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.neruxVaceTicks
import net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.FlyMode

object NeruxVace : FlyMode("NeruxVace") {
	private var tick = 0
	override fun onUpdate() {
		if (!mc.thePlayer.onGround)
			tick++

		if (tick >= neruxVaceTicks && !mc.thePlayer.onGround) {
			tick = 0
			mc.thePlayer.motionY = .015
		}
	}
}
