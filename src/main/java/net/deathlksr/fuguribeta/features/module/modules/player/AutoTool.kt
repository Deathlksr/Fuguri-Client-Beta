/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player

import net.deathlksr.fuguribeta.event.ClickBlockEvent
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.render.FakeItemRender
import net.deathlksr.fuguribeta.value.BoolValue
import net.minecraft.util.BlockPos

object AutoTool :
    Module("AutoTool", Category.PLAYER, subjective = true, gameDetecting = false, hideModule = false) {

    private val fakeItem by BoolValue("FakeItem", false)
    private val switchBack by BoolValue("SwitchBack", false)
    private val onlySneaking by BoolValue("OnlySneaking", false)

    @EventTarget
    fun onClick(event: ClickBlockEvent) {
        switchSlot(event.clickedBlock ?: return)
    }

    var formerSlot = -1;

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        // set fakeItem to null if mouse is not pressed
        if (!mc.gameSettings.keyBindAttack.isKeyDown) {
            if (switchBack && formerSlot != -1) {
                mc.thePlayer.inventory.currentItem = formerSlot
                formerSlot = -1
            }
            FakeItemRender.fakeItem = -1
        }
    }

    fun switchSlot(blockPos: BlockPos) {
        var bestSpeed = 1F
        var bestSlot = -1

        val blockState = mc.theWorld.getBlockState(blockPos)

        if (onlySneaking && !mc.thePlayer.isSneaking) return

        for (i in 0..8) {
            val item = mc.thePlayer.inventory.getStackInSlot(i) ?: continue
            val speed = item.getStrVsBlock(blockState.block)

            if (speed > bestSpeed) {
                bestSpeed = speed
                bestSlot = i
            }
        }

        if (bestSlot != -1 && mc.thePlayer.inventory.currentItem != bestSlot) {
            if (fakeItem && FakeItemRender.fakeItem == -1) {
                FakeItemRender.fakeItem = mc.thePlayer.inventory.currentItem
            }
            if (formerSlot == -1) {
                formerSlot = mc.thePlayer.inventory.currentItem
            }
            mc.thePlayer.inventory.currentItem = bestSlot
        }

    }

}