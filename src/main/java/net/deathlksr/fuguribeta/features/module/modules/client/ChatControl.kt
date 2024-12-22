/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.client

import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.value.BoolValue

object ChatControl : Module("ChatControl", Category.CLIENT, gameDetecting = false, hideModule = false, subjective = true) {

    init {
        state = true
    }

    val chatLimitValue by BoolValue("NoChatLimit", true)
    val chatClearValue by BoolValue("NoChatClear", true)
    val fontChatValue by BoolValue("FontChat", false)
}