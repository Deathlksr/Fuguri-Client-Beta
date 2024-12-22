package net.deathlksr.fuguribeta.features.module.modules.other

import net.deathlksr.fuguribeta.event.EventState
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.MotionEvent
import net.deathlksr.fuguribeta.event.WorldEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.utils.ClientUtils.displayChatMessage
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemPotion

object Alert : Module("Alert", Category.OTHER, hideModule = false) {
    private val alertTimer = MSTimer()
    private val drinkers = arrayListOf<EntityLivingBase>()
    private val enderperl = arrayListOf<EntityLivingBase>()

    override fun onDisable() {
        clearDrag()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        clearDrag()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.PRE) {
            for (player in mc.theWorld.playerEntities) {
                if (player !in drinkers && player != mc.thePlayer && player.isUsingItem && player.heldItem != null && player.heldItem.item is ItemPotion) {
                    val pation = player.heldItem.item as ItemPotion
                    displayChatMessage("§e" + player.name + "§r is drinking " + pation.getEffects(player.heldItem)[0].effectName + " potion!")
                    drinkers.add(player)
                    alertTimer.reset()
                }
            }
            if (alertTimer.hasTimePassed(3000L) && drinkers.isNotEmpty()) {
                clearDrag()
            }
        }
    }

    private fun clearDrag() {
        drinkers.clear()
        alertTimer.reset()
    }
}