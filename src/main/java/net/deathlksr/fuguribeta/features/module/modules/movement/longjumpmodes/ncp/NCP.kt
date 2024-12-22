/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.ncp

import net.deathlksr.fuguribeta.event.MoveEvent
import net.deathlksr.fuguribeta.features.module.modules.movement.LongJump.canBoost
import net.deathlksr.fuguribeta.features.module.modules.movement.LongJump.jumped
import net.deathlksr.fuguribeta.features.module.modules.movement.LongJump.ncpBoost
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.MovementUtils.speed

object NCP : LongJumpMode("NCP") {
    override fun onUpdate() {
        speed *= if (canBoost) ncpBoost else 1f
        canBoost = false
    }

    override fun onMove(event: MoveEvent) {
        if (!isMoving && jumped) {
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            event.zeroXZ()
        }
    }
}