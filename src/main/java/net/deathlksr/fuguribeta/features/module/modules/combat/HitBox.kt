/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.modules.client.AntiBot.isBot
import net.deathlksr.fuguribeta.features.module.modules.client.Teams
import net.deathlksr.fuguribeta.utils.extensions.isAnimal
import net.deathlksr.fuguribeta.utils.extensions.isClientFriend
import net.deathlksr.fuguribeta.utils.extensions.isMob
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer

object HitBox : Module("HitBox", Category.COMBAT, hideModule = false) {

    private val targetPlayers by BoolValue("TargetPlayers", true)
        private val playerSize by FloatValue("PlayerSize", 0.4F, 0F..1F) { targetPlayers }
        private val friendSize by FloatValue("FriendSize", 0.4F, 0F..1F) { targetPlayers }
        private val teamMateSize by FloatValue("TeamMateSize", 0.4F, 0F..1F) { targetPlayers }
        private val botSize by FloatValue("BotSize", 0.4F, 0F..1F) { targetPlayers }

    private val targetMobs by BoolValue("TargetMobs", false)
        private val mobSize by FloatValue("MobSize", 0.4F, 0F..1F) { targetMobs }

    private val targetAnimals by BoolValue("TargetAnimals", false)
        private val animalSize by FloatValue("AnimalSize", 0.4F, 0F..1F) { targetAnimals }

    fun determineSize(entity: Entity): Float {
        return when (entity) {
            is EntityPlayer -> {
                if (entity.isSpectator || !targetPlayers) {
                    return 0F
                }

                if (isBot(entity)) {
                    return botSize
                } else if (entity.isClientFriend()) {
                    return friendSize
                } else if (Teams.handleEvents() && Teams.isInYourTeam(entity)) {
                    return teamMateSize
                }

                playerSize
            }

            else -> {
                if (entity.isMob() && targetMobs) {
                    return mobSize
                } else if (entity.isAnimal() && targetAnimals) {
                    return animalSize
                }

                0F
            }
        }
    }
}