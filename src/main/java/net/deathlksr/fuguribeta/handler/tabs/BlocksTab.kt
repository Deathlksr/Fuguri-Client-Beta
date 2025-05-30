/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.handler.tabs

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack


class BlocksTab : CreativeTabs("Special blocks") {

    /**
     * Initialize of special blocks tab
     */
    init {
        backgroundImageName = "item_search.png"
    }

    /**
     * Add all items to tab
     *
     * @param itemList list of tab items
     */
    override fun displayAllReleventItems(itemList: MutableList<ItemStack>) {
        itemList += ItemStack(Blocks.command_block)
        itemList += ItemStack(Items.command_block_minecart)
        itemList += ItemStack(Blocks.barrier)
        itemList += ItemStack(Blocks.dragon_egg)
        itemList += ItemStack(Blocks.brown_mushroom_block)
        itemList += ItemStack(Blocks.red_mushroom_block)
        itemList += ItemStack(Blocks.farmland)
        itemList += ItemStack(Blocks.mob_spawner)
        itemList += ItemStack(Blocks.lit_furnace)
    }

    /**
     * Return icon item of tab
     *
     * @return icon item
     */
    override fun getTabIconItem(): Item = ItemStack(Blocks.command_block).item

    /**
     * Return name of tab
     *
     * @return tab name
     */
    override fun getTranslatedTabLabel() = "Special blocks"

    /**
     * @return searchbar status
     */
    override fun hasSearchBar() = true
}