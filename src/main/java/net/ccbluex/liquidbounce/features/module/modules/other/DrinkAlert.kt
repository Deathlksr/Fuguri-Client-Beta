package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.minecraft.item.ItemPotion
import net.minecraft.potion.PotionEffect
import net.minecraftforge.event.entity.player.PlayerUseItemEvent

object DrinkAlert : Module("DrinkAlert", Category.OTHER, hideModule = false) {

    fun onPlayerUseItem(event: PlayerUseItemEvent) {
        val itemStack = event.item
        if (itemStack.item is ItemPotion) {
            val potionItem = itemStack.item as ItemPotion
            val effects: List<PotionEffect> = potionItem.getEffects(itemStack)
            if (effects.isNotEmpty()) {
                for (player in mc.theWorld.playerEntities) {
                    for (effect in effects) {
                        displayChatMessage("§e" + player.name + "§r is drinking: ${effect.effectName}")
                    }
                }
            }
        }
    }
}
