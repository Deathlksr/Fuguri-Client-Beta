/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.file.FileManager.friendsConfig
import net.deathlksr.fuguribeta.script.api.global.Chat
import net.deathlksr.fuguribeta.utils.render.ColorUtils.stripColor
import net.deathlksr.fuguribeta.value.BoolValue

object AutoRole : Module("AutoRole", Category.OTHER, gameDetecting = false, hideModule = false) {
    private val formattingValue = BoolValue("Formatting", true)

        private val STAFF_PREFIXES = arrayOf(
            "[Moderador] ",
            "[MODERADOR] ",
            "[Mod] ",
            "[MOD] ",
            "[Administrador] ",
            "[ADMINISTRADOR] ",
            "[Admin] ",
            "[ADMIN] ",
            "[Coordenador] ",
            "[COORDENADOR] ",
            "[Coord] ",
            "[COORD] ",
            "[Gerente] ",
            "[GERENTE] ",
            "[CEO] ",
            "[Dono] ",
            "[DONO] ",
            "[DIRETOR] ",
            "[DEV] ",
            "[Dev] ",
            "Diretor ",
            "DIRETOR ",
            "Dev ",
            "DEV ",
            "Mod ",
            "MOD ",
            "[Master] ",
            "[MASTER] ",
            "[CEO] ",
            "[TRIAL] "
        )

    private fun isStaff(prefix: String): Boolean {
        return STAFF_PREFIXES.toList().contains(stripColor(prefix))
    }

    @EventTarget
    fun handle(event: UpdateEvent?) {
        val friendManager = friendsConfig

        val formatCodes = arrayOf("§k", "§l", "§m", "§n", "§o")
        var currentFormatIndex = 0

        for (team in mc.theWorld.scoreboard.teams) {
            if (this.isStaff(team.colorPrefix)) {
                for (member in team.membershipCollection) {
                    if (!friendManager.isFriend(member)) {
                        friendManager.addFriend(member)

                        var colorPrefix = team.colorPrefix
                        if (formattingValue.get() && currentFormatIndex < formatCodes.size) {
                            colorPrefix = formatCodes[currentFormatIndex] + colorPrefix
                            currentFormatIndex++
                            if (currentFormatIndex >= formatCodes.size) {
                                currentFormatIndex = 0
                            }
                        }

                        Chat.print("§7[§d!§7]§7 ADDED: $colorPrefix$member")
                    }
                }
            }
        }
    }
}