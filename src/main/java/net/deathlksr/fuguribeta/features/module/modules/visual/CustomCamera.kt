/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue

object CustomCamera : Module("CustomCamera", Category.VISUAL, hideModule = false) {

    val fovValue by FloatValue("FOV", 1f, 0f.. 30f)
    val clipValue by BoolValue("CameraClip", false)
}