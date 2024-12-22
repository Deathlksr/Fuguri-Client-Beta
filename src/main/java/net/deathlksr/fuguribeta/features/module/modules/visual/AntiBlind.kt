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

object AntiBlind : Module("AntiBlind", Category.VISUAL, gameDetecting = false, hideModule = false) {
    val confusionEffect by BoolValue("Confusion", true)
    val pumpkinEffect by BoolValue("Pumpkin", true)
    val fireEffect by FloatValue("FireAlpha", 0.3f, 0f..1f)
    val bossHealth by BoolValue("BossHealth", true)
}