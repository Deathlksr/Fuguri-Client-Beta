/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.script.api

import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.ScriptUtils
import net.deathlksr.fuguribeta.utils.inventory.ItemUtils
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack

class ScriptTab(private val tabObject: JSObject) : CreativeTabs(tabObject.getMember("name") as String) {
    val items = ScriptUtils.convert(tabObject.getMember("items"), Array<ItemStack>::class.java) as Array<ItemStack>

    override fun getTabIconItem() = ItemUtils.createItem(tabObject.getMember("icon") as String)?.item

    override fun getTranslatedTabLabel() = tabObject.getMember("name") as String

    override fun displayAllReleventItems(items: MutableList<ItemStack>) = items.forEach { items += it }
}