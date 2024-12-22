/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.other

import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.SpeedMode
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.extensions.tryJump

object Legit : SpeedMode("Legit") {
    override fun onStrafe() {
        val player = mc.thePlayer ?: return

        if (mc.thePlayer.onGround && isMoving) {
            player.tryJump()
        }
    }

    override fun onUpdate() {
        val player = mc.thePlayer ?: return

        player.isSprinting = player.movementInput.moveForward > 0.8
    }
}
