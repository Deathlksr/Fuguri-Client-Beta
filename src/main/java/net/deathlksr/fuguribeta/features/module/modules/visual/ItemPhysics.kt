/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue

object ItemPhysics: Module("ItemPhysics", Category.VISUAL, hideModule = false) {

    val realistic by BoolValue("Realistic", false)
    val weight by FloatValue("Weight", 0.5F, 0.1F..3F)
    val rotationSpeed by FloatValue("RotationSpeed", 1.0F, 0.01F..3F)

}