/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.other

import net.deathlksr.fuguribeta.features.module.modules.movement.Speed
import net.deathlksr.fuguribeta.features.module.modules.movement.Speed.customAirStrafe
import net.deathlksr.fuguribeta.features.module.modules.movement.Speed.customAirTimer
import net.deathlksr.fuguribeta.features.module.modules.movement.Speed.customAirTimerTick
import net.deathlksr.fuguribeta.features.module.modules.movement.Speed.customGroundStrafe
import net.deathlksr.fuguribeta.features.module.modules.movement.Speed.customGroundTimer
import net.deathlksr.fuguribeta.features.module.modules.movement.Speed.customY
import net.deathlksr.fuguribeta.features.module.modules.movement.Speed.notOnConsuming
import net.deathlksr.fuguribeta.features.module.modules.movement.Speed.notOnFalling
import net.deathlksr.fuguribeta.features.module.modules.movement.Speed.notOnVoid
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.SpeedMode
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.MovementUtils.strafe
import net.deathlksr.fuguribeta.utils.extensions.stopXZ
import net.deathlksr.fuguribeta.utils.extensions.stopY
import net.deathlksr.fuguribeta.utils.extensions.tryJump
import net.deathlksr.fuguribeta.utils.misc.FallingPlayer
import net.minecraft.item.ItemBucketMilk
import net.minecraft.item.ItemFood
import net.minecraft.item.ItemPotion

object CustomSpeed : SpeedMode("Custom") {

    override fun onMotion() {
        val player = mc.thePlayer ?: return
        val heldItem = player.heldItem

        val fallingPlayer = FallingPlayer()
        if (notOnVoid && fallingPlayer.findCollision(500) == null
            || notOnFalling && player.fallDistance > 2.5f
            || notOnConsuming && player.isUsingItem
            && (heldItem.item is ItemFood
                    || heldItem.item is ItemPotion
                    || heldItem.item is ItemBucketMilk)
        ) {

            if (player.onGround) player.tryJump()
            mc.timer.timerSpeed = 1f
            return
        }

        if (isMoving) {
            if (player.onGround) {
                if (customGroundStrafe > 0) {
                    strafe(customGroundStrafe)
                }

                mc.timer.timerSpeed = customGroundTimer
                player.motionY = customY.toDouble()
            } else {
                if (customAirStrafe > 0) {
                    strafe(customAirStrafe)
                }

                if (player.ticksExisted % customAirTimerTick == 0) {
                    mc.timer.timerSpeed = customAirTimer
                } else {
                    mc.timer.timerSpeed = 1f
                }
            }
        }
    }

    override fun onEnable() {
        val player = mc.thePlayer ?: return

        if (Speed.resetXZ) player.stopXZ()
        if (Speed.resetY) player.stopY()

        super.onEnable()
    }

}