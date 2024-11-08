package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.entity.player.EntityPlayer

object KillMessage: Module("KillMessage", Category.PLAYER) {

    private var textString by TextValue("Text", "пиздец")

    @EventTarget
    fun onKillEvent(event: EntityKilledEvent) {
        val target = event.targetEntity

        if (target !is EntityPlayer)
            return

        mc.thePlayer.sendChatMessage(target.name + " " + textString)
    }
}