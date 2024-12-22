/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.vanilla

import net.deathlksr.fuguribeta.event.MoveEvent
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.handleVanillaKickBypass
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.vanillaSpeed
import net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.FlyMode
import net.deathlksr.fuguribeta.utils.MovementUtils.strafe

object Vanilla : FlyMode("Vanilla") {
	override fun onMove(event: MoveEvent) {
        val thePlayer = mc.thePlayer ?: return

		strafe(vanillaSpeed, true, event)

        thePlayer.onGround = false
        thePlayer.isInWeb = false

        thePlayer.capabilities.isFlying = false

        var ySpeed = 0.0

        if (mc.gameSettings.keyBindJump.isKeyDown)
            ySpeed += vanillaSpeed

        if (mc.gameSettings.keyBindSneak.isKeyDown)
            ySpeed -= vanillaSpeed

        thePlayer.motionY = ySpeed
        event.y = ySpeed

		handleVanillaKickBypass()
	}
}
