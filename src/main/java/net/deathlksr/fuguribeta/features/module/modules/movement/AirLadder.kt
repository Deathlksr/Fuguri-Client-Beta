/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlock
import net.minecraft.block.BlockLadder
import net.minecraft.block.BlockVine
import net.minecraft.util.BlockPos

object AirLadder : Module("AirLadder", Category.MOVEMENT, hideModule = false) {
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        val currBlock = getBlock(BlockPos(thePlayer))
        val block = getBlock(BlockPos(thePlayer).up())
        if ((block is BlockLadder && thePlayer.isCollidedHorizontally) || (block is BlockVine || currBlock is BlockVine)) {
            thePlayer.motionY = 0.15
            thePlayer.motionX = 0.0
            thePlayer.motionZ = 0.0
        }
    }
}