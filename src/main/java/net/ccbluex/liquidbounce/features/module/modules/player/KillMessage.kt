package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.FuguriBeta
import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.entity.EntityLivingBase

object KillMessage : Module("KillMessage", Category.PLAYER, gameDetecting = false, hideModule = false) {

    private val textString by TextValue("Message", "You were a good opponent but you were still killed by the Fuguri Client!")

    var target: EntityLivingBase? = null
    private val attackedEntityList = mutableListOf<EntityLivingBase>()

    @EventTarget
    fun onUpdate(e: UpdateEvent) {
        if (mc.thePlayer == null) return
        MovementUtils.updateBlocksPerSecond()

        attackedEntityList.map { it }.forEach {
            if (it.isDead) {
                FuguriBeta.eventManager.callEvent(EntityKilledEvent(it))
                mc.thePlayer.sendChatMessage(it.name + " " + textString)
                attackedEntityList.remove(it)
            }
        }
    }
}


