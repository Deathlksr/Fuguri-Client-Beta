/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.utils.extensions

import net.deathlksr.fuguribeta.utils.block.BlockUtils
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3

/**
 * Get block by position
 */
fun BlockPos.getBlock() = BlockUtils.getBlock(this)

/**
 * Get vector of block position
 */
fun BlockPos.getVec() = Vec3(x + 0.5, y + 0.5, z + 0.5)

fun BlockPos.toVec() = Vec3(this)

fun BlockPos.isReplaceable() = BlockUtils.isReplaceable(this)

fun BlockPos.canBeClicked() = BlockUtils.canBeClicked(this)