/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.vanilla

import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.flyspeed
import net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.FlyMode

object SmoothVanilla : FlyMode("SmoothVanilla") {

	override fun onUpdate() {
		mc.thePlayer.capabilities.isFlying = true
		mc.thePlayer.capabilities.flySpeed = flyspeed
	}
}
