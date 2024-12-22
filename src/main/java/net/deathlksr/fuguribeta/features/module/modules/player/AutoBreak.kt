/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlock
import net.minecraft.init.Blocks.air

object AutoBreak : Module("AutoBreak", Category.PLAYER, subjective = true, gameDetecting = false) {

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.objectMouseOver == null || mc.objectMouseOver.blockPos == null || mc.theWorld == null)
            return

        mc.gameSettings.keyBindAttack.pressed = getBlock(mc.objectMouseOver.blockPos) != air
    }

    override fun onDisable() {
        if (!mc.gameSettings.keyBindAttack.pressed)
            mc.gameSettings.keyBindAttack.pressed = false
    }
}
