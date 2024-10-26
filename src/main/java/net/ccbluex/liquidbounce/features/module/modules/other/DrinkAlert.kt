package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.minecraft.item.ItemPotion
import net.minecraftforge.event.entity.player.PlayerUseItemEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object DrinkAlert : Module("DrinkAlert", Category.OTHER, hideModule = false) {

    @SubscribeEvent
    fun onPlayerUseItem(event: PlayerUseItemEvent.Start) {
        val itemStack = event.item
        if (itemStack.item is ItemPotion) {
            val potionItem = itemStack.item as ItemPotion
            val effects = potionItem.getEffects(itemStack)
            if (effects != null && effects.isNotEmpty()) {
                val player = event.entityPlayer
                val playerName = player.name

                for (effect in effects) {
                    displayChatMessage("§e$playerName§r is drinking: ${effect.effectName}")
                }
            }
        }
    }
}
