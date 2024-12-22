/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.RotationUtils.currentRotation
import net.deathlksr.fuguribeta.utils.extensions.rotation
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.item.ItemBow
import net.minecraft.network.play.client.C03PacketPlayer.C05PacketPlayerLook
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

object FastBow : Module("FastBow", Category.COMBAT, hideModule = false) {

    private val packets by IntegerValue("Packets", 20, 3..20)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (!thePlayer.isUsingItem)
            return

        val currentItem = thePlayer.inventory.getCurrentItem()

        if (currentItem != null && currentItem.item is ItemBow) {
            sendPacket(
                C08PacketPlayerBlockPlacement(
                    BlockPos.ORIGIN,
                    255,
                    mc.thePlayer.currentEquippedItem,
                    0F,
                    0F,
                    0F
                )
            )

            val (yaw, pitch) = currentRotation ?: thePlayer.rotation

            repeat(packets) {
                sendPacket(C05PacketPlayerLook(yaw, pitch, true))
            }

            sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            thePlayer.itemInUseCount = currentItem.maxItemUseDuration - 1
        }
    }
}