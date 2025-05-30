/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.minecraft.init.Blocks

object XRay : Module("XRay", Category.VISUAL, gameDetecting = false) {

    val xrayBlocks = mutableListOf(
        Blocks.coal_ore,
        Blocks.iron_ore,
        Blocks.gold_ore,
        Blocks.redstone_ore,
        Blocks.lapis_ore,
        Blocks.diamond_ore,
        Blocks.emerald_ore,
        Blocks.quartz_ore,
        Blocks.clay,
        Blocks.glowstone,
        Blocks.crafting_table,
        Blocks.torch,
        Blocks.ladder,
        Blocks.tnt,
        Blocks.coal_block,
        Blocks.iron_block,
        Blocks.gold_block,
        Blocks.diamond_block,
        Blocks.emerald_block,
        Blocks.lapis_block,
        Blocks.fire,
        Blocks.mossy_cobblestone,
        Blocks.mob_spawner,
        Blocks.end_portal_frame,
        Blocks.enchanting_table,
        Blocks.bookshelf,
        Blocks.command_block,
        Blocks.lava,
        Blocks.flowing_lava,
        Blocks.water,
        Blocks.flowing_water,
        Blocks.furnace,
        Blocks.lit_furnace
    )

    private var prevGammaLevel = 0f

    override fun onEnable() {
        prevGammaLevel = mc.gameSettings.gammaSetting
    }

    override fun onToggle(state: Boolean) {
        mc.renderGlobal.loadRenderers()
    }

    override fun onDisable() {
        mc.gameSettings.gammaSetting = prevGammaLevel
    }
}
