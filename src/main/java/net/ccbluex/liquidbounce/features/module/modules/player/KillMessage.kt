package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingDeathEvent

object KillMessage : Module("KillMessage", Category.PLAYER, gameDetecting = false, hideModule = false) {

    @SubscribeEvent
    fun onPlayerDeath(event: LivingDeathEvent) {
        val deadEntity = event.entityLiving
        if (deadEntity is EntityPlayer) {
            val source = event.source.entity
            if (source is EntityPlayer && source.uniqueID == mc.thePlayer.uniqueID) {
                val globalKillMessage = "You were a good opponent ${deadEntity.displayNameString}, but you were still killed by the Fuguri Client!"
                deadEntity.entityWorld.playerEntities.forEach { _ ->
                    mc.thePlayer.sendChatMessage(globalKillMessage)
                }
            }
        }
    }
}