package net.deathlksr.fuguribeta.utils.extensions

import net.minecraft.util.AxisAlignedBB

fun AxisAlignedBB.expands(v: Double, modifyYDown: Boolean=true, modifyYUp: Boolean=true): AxisAlignedBB {
    return AxisAlignedBB(this.minX - v, this.minY - (if (modifyYDown) v else 0.0), this.minZ - v, this.maxX + v, this.maxY + (if (modifyYUp) v else 0.0), this.maxZ + v)
}