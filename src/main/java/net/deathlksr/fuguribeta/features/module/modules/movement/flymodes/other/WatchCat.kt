/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.other

import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.startY
import net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.FlyMode
import net.deathlksr.fuguribeta.utils.MovementUtils.strafe
import net.deathlksr.fuguribeta.utils.extensions.stopXZ
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.nextDouble

object WatchCat : FlyMode("WatchCat") {
	override fun onUpdate() {
		strafe(0.15f)
		mc.thePlayer.isSprinting = true

		if (mc.thePlayer.posY < startY + 2) {
			mc.thePlayer.motionY = nextDouble(endInclusive = 0.5)
			return
		}

		if (startY > mc.thePlayer.posY) mc.thePlayer.stopXZ()
	}
}
