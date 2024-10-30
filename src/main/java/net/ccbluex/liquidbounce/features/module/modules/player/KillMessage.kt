package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.TextValue
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.event.entity.living.LivingDeathEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

object KillMessage : Module("KillMessage", Category.PLAYER, gameDetecting = false, hideModule = false) {

    private val textString by TextValue(
        "Message",
        "You were a good opponent but you were still killed by the Fuguri Client!"
    )

    init {
        // Регистрация обработчика события
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this)
    }

    @SubscribeEvent
    fun onEntityDeath(event: LivingDeathEvent) {
        val entity = event.entity
        val source = event.source.entity

        // Проверка, что жертва и атакующий - игроки
        if (entity is EntityPlayer && source is EntityPlayer && source == mc.thePlayer) {
            // Отправка сообщения только один раз (исключаем отправку всем игрокам)
            mc.thePlayer.sendChatMessage("${entity.displayNameString} $textString")
        }
    }
}
