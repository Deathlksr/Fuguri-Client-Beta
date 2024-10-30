package net.ccbluex.liquidbounce.features.module.modules.player

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.file.FileManager.friendsConfig
import net.ccbluex.liquidbounce.utils.ClientUtils.displayChatMessage
import net.ccbluex.liquidbounce.utils.RaycastUtils
import net.ccbluex.liquidbounce.utils.render.ColorUtils.stripColor
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.input.Mouse

object MidClick : Module("MidClick", Category.PLAYER, subjective = true, gameDetecting = false, hideModule = false) {
    private var wasDown = false
    private val distanceraytrace by FloatValue("Distance", 100F, 3F..1000F)
    val reverse by BoolValue("Reverse Friends", false)
    private val debug by BoolValue("Debug", false)

    @EventTarget
    fun onRender(event: Render2DEvent) {
        if (mc.currentScreen != null)
            return

        if (!wasDown && Mouse.isButtonDown(2)) {
            val entity = RaycastUtils.raycastEntity(distanceraytrace.toDouble(), mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)

            if (entity is EntityPlayer) {
                val playerName = stripColor(entity.name)

                if (reverse) {
                    if (!friendsConfig.isFriend(playerName)) {
                        friendsConfig.addFriend(playerName)
                        if (debug) {
                            displayChatMessage("§a§l[$playerName]§c Friend deleted.")
                        }
                    } else {
                        friendsConfig.removeFriend(playerName)
                        if (debug) {
                            displayChatMessage("§a§l[$playerName]§c Friend added.")
                        }
                    }
                }

                if (!reverse) {
                    if (!friendsConfig.isFriend(playerName)) {
                        friendsConfig.addFriend(playerName)
                        if (debug) {
                            displayChatMessage("§a§l[$playerName]§c Friend added.")
                        }
                    } else {
                        friendsConfig.removeFriend(playerName)
                            if (debug) {
                                displayChatMessage("§a§l[$playerName]§c Friend deleted.")
                            }
                    }
                }
            } else if (debug) {
                displayChatMessage("§c§lError: §a You didn't target the player.")
            }
        }
        wasDown = Mouse.isButtonDown(2)
    }
}