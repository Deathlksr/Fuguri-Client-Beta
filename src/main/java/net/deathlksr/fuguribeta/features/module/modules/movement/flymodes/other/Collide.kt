/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.other

import net.deathlksr.fuguribeta.event.BlockBBEvent
import net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.FlyMode
import net.minecraft.block.BlockLadder
import net.minecraft.block.material.Material
import net.minecraft.util.AxisAlignedBB


object Collide : FlyMode("Collide") {
    override fun onBB(event: BlockBBEvent) {
        if (!mc.gameSettings.keyBindJump.isKeyDown && mc.gameSettings.keyBindSneak.isKeyDown) return
        if (!event.block.material.blocksMovement() && event.block.material != Material.carpet && event.block.material != Material.vine && event.block.material != Material.snow && event.block !is BlockLadder) {
            event.boundingBox = AxisAlignedBB(
                -2.0,
                -1.0,
                -2.0,
                2.0,
                1.0,
                2.0
            ).offset(
                event.x.toDouble(),
                event.y.toDouble(),
                event.z.toDouble()
            )
        }
    }
}
