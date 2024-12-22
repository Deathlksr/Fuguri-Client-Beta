/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player

import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.value.FloatValue
import kotlin.math.max

object Reach : Module("Reach", Category.PLAYER, hideModule = false) {

    val combatReach by FloatValue("CombatReach", 3.5f, 3f..7f)
    val buildReach by FloatValue("BuildReach", 5f, 4.5f..7f)

    val maxRange
        get() = max(combatReach, buildReach)
}
