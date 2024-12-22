/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.JumpEvent
import net.deathlksr.fuguribeta.event.MoveEvent
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.MovementUtils.strafe
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlock
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.block.BlockPane
import net.minecraft.util.BlockPos

object HighJump : Module("HighJump", Category.MOVEMENT) {
    private val mode by ListValue("Mode", arrayOf("Vanilla", "Damage", "AACv3", "DAC", "Mineplex"), "Vanilla")
        private val height by FloatValue("Height", 2f, 1.1f..5f) { mode in arrayOf("Vanilla", "Damage") }

    private val glass by BoolValue("OnlyGlassPane", false)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer

        if (glass && getBlock(BlockPos(thePlayer)) !is BlockPane)
            return

        when (mode.lowercase()) {
            "damage" -> if (thePlayer.hurtTime > 0 && thePlayer.onGround) thePlayer.motionY += 0.42f * height
            "aacv3" -> if (!thePlayer.onGround) thePlayer.motionY += 0.059
            "dac" -> if (!thePlayer.onGround) thePlayer.motionY += 0.049999
            "mineplex" -> if (!thePlayer.onGround) strafe(0.35f)
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (glass && getBlock(BlockPos(thePlayer)) !is BlockPane)
            return
        if (!thePlayer.onGround) {
            if ("mineplex" == mode.lowercase()) {
                thePlayer.motionY += if (thePlayer.fallDistance == 0f) 0.0499 else 0.05
            }
        }
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (glass && getBlock(BlockPos(thePlayer)) !is BlockPane)
            return
        when (mode.lowercase()) {
            "vanilla" -> event.motion *= height
            "mineplex" -> event.motion = 0.47f
        }
    }

    override val tag
        get() = mode
}