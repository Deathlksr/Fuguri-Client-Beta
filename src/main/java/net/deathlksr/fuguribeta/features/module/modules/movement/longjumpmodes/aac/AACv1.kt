/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.aac

import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.LongJumpMode
import net.deathlksr.fuguribeta.utils.MovementUtils

object AACv1 : LongJumpMode("AACv1") {
    override fun onUpdate() {
        mc.thePlayer.motionY += 0.05999
        MovementUtils.speed *= 1.08f
    }
}