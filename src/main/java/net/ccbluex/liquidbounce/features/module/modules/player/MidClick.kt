/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.file.FileManager.saveConfig
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.input.Mouse

object MidClick : Module("MidClick", Category.PLAYER, subjective = true, gameDetecting = false, hideModule = false) {
    private var wasDown = false
    private val distanceraytrace by FloatValue("Distance", 3F, 3F..100F)
    val reverse by BoolValue("Reverse Friends", false)

    @EventTarget
    fun onRender(event: Render2DEvent) {
        if (mc.currentScreen != null)
            return

        if (!wasDown && Mouse.isButtonDown(2)) {
            val entity = RaycastUtils.raycastEntity(distanceraytrace.toDouble(), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

            if (entity is EntityPlayer) {
                val playerName = stripColor(entity.name)

                if (reverse.takeIf {isActive} == true) {
                    if (!friendsConfig.isFriend(playerName)) {
                        friendsConfig.addFriend(playerName)
                        displayChatMessage("§a§l[$playerName]§c Friend deleted.")
                    } else {
                        friendsConfig.removeFriend(playerName)
                        displayChatMessage("§a§l[$playerName]§c Friend added.")
                    }
                }

                if (reverse.takeIf {isActive} == false) {
                    if (!friendsConfig.isFriend(playerName)) {
                        friendsConfig.addFriend(playerName)
                        displayChatMessage("§a§l[$playerName]§c Friend added.")
                    } else {
                        friendsConfig.removeFriend(playerName)
                        displayChatMessage("§a§l[$playerName]§c Friend deleted.")
                    }
                }

            } else displayChatMessage("§c§lError: §a You didn't target the player.")
        }
        wasDown = Mouse.isButtonDown(2)
    }
}