/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.client

import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.value.BoolValue
import net.minecraft.entity.EntityLivingBase

object Teams : Module("Teams", Category.CLIENT, gameDetecting = false, hideModule = false) {

    private val scoreboard by BoolValue("ScoreboardTeam", true)
    private val color by BoolValue("Color", true)
    private val gommeSW by BoolValue("GommeSW", false)

    /**
     * Check if [entity] is in your own team using scoreboard, name color or team prefix
     */
    fun isInYourTeam(entity: EntityLivingBase): Boolean {
        val thePlayer = mc.thePlayer ?: return false

        if (scoreboard && thePlayer.team != null && entity.team != null &&
                thePlayer.team.isSameTeam(entity.team))
            return true

        val displayName = thePlayer.displayName

        if (gommeSW && displayName != null && entity.displayName != null) {
            val targetName = entity.displayName.formattedText.replace("§r", "")
            val clientName = displayName.formattedText.replace("§r", "")
            if (targetName.startsWith("T") && clientName.startsWith("T"))
                if (targetName[1].isDigit() && clientName[1].isDigit())
                    return targetName[1] == clientName[1]
        }

        if (color && displayName != null && entity.displayName != null) {
            val targetName = entity.displayName.formattedText.replace("§r", "")
            val clientName = displayName.formattedText.replace("§r", "")
            return targetName.startsWith("§${clientName[1]}")
        }

        return false
    }

}
