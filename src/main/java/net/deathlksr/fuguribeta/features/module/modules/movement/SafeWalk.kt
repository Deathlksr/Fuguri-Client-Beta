/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.MoveEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlock
import net.deathlksr.fuguribeta.utils.misc.FallingPlayer
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.block.BlockAir
import net.minecraft.util.BlockPos

object SafeWalk : Module("SafeWalk", Category.MOVEMENT, hideModule = false) {

    private val airSafe by BoolValue("AirSafe", false)
    private val maxFallDistanceValue = IntegerValue("MaxFallDistance", 5, 0..100)

    private var lastGroundY: Double? = null
    private var lastCollisionY: Int? = null

    @EventTarget
    fun onMove(event: MoveEvent) {
        val player = mc.thePlayer ?: return
        if (player.capabilities.allowFlying || player.capabilities.isFlying
            || !mc.playerController.gameIsSurvivalOrAdventure()) return

        if (!maxFallDistanceValue.isMinimal() && player.onGround && getBlock(BlockPos(player).down()) !is BlockAir) {
            lastGroundY = player.posY
            lastCollisionY = FallingPlayer(player, true).findCollision(60)?.pos?.y
        }

        if (airSafe || player.onGround) {
            event.isSafeWalk = maxFallDistanceValue.isMinimal()
                    || (lastGroundY != null && lastCollisionY != null
                    && lastGroundY!! - lastCollisionY!! > maxFallDistanceValue.get() + 1)
        }
    }
}
