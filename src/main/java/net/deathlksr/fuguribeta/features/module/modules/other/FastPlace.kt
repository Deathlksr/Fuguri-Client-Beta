/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue

object FastPlace : Module("FastPlace", Category.OTHER, hideModule = false) {
    val speed by IntegerValue("Speed", 0, 0..4)
    val onlyBlocks by BoolValue("OnlyBlocks", true)
    val facingBlocks by BoolValue("OnlyWhenFacingBlocks", true)
}
