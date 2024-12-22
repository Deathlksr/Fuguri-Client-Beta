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
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.client.gui.Gui
import net.minecraft.client.settings.GameSettings
import net.minecraft.init.Blocks.air
import net.minecraft.util.BlockPos

object Eagle : Module("Eagle", Category.PLAYER, hideModule = false) {

    private val sneakDelay by IntegerValue("SneakDelay", 0, 0..100)
    private val onlyWhenLookingDown by BoolValue("OnlyWhenLookingDown", false)
    private val lookDownThreshold by FloatValue("LookDownThreshold", 45f, 0f..90f) { onlyWhenLookingDown }

    private val sneakTimer = MSTimer()
    private var sneakOn = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.onGround && getBlock(BlockPos(thePlayer).down()) == air) {
            val shouldSneak = !onlyWhenLookingDown || thePlayer.rotationPitch >= lookDownThreshold

            if (shouldSneak && !GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
                if (sneakTimer.hasTimePassed(sneakDelay)) {
                    mc.gameSettings.keyBindSneak.pressed = true
                    sneakTimer.reset()
                    sneakOn = false
                }
            } else {
                mc.gameSettings.keyBindSneak.pressed = false
            }

            sneakOn = true
        } else {
            if (sneakOn) {
                mc.gameSettings.keyBindSneak.pressed = false
                sneakOn = false
            }
        }

        if (!sneakOn && mc.currentScreen !is Gui) mc.gameSettings.keyBindSneak.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)
    }

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        sneakOn = false

        if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak))
            mc.gameSettings.keyBindSneak.pressed = false
    }
}