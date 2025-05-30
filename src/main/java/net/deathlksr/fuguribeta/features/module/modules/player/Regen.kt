/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.MovementUtils.serverOnGround
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.potion.Potion

object Regen : Module("Regen", Category.PLAYER) {

    private val mode by ListValue("Mode", arrayOf("Vanilla", "Spartan"), "Vanilla")
        private val speed by IntegerValue("Speed", 100, 1..100) { mode == "Vanilla" }

    private val delay by IntegerValue("Delay", 0, 0..10000)
    private val health by IntegerValue("Health", 18, 0..20)
    private val food by IntegerValue("Food", 18, 0..20)

    private val noAir by BoolValue("NoAir", false)
    private val potionEffect by BoolValue("PotionEffect", false)

    private val timer = MSTimer()

    private var resetTimer = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (resetTimer) {
            mc.timer.timerSpeed = 1F
        } else {
            resetTimer = false
        }

        val thePlayer = mc.thePlayer ?: return

        if (
            !mc.playerController.gameIsSurvivalOrAdventure()
            || noAir && !serverOnGround
            || thePlayer.foodStats.foodLevel <= food
            || !thePlayer.isEntityAlive
            || thePlayer.health >= health
            || (potionEffect && !thePlayer.isPotionActive(Potion.regeneration))
            || !timer.hasTimePassed(delay)
        ) return

        when (mode.lowercase()) {
            "vanilla" -> {
                repeat(speed) {
                    sendPacket(C03PacketPlayer(serverOnGround))
                }
            }

            "spartan" -> {
                if (!isMoving && serverOnGround) {
                    repeat(9) {
                        sendPacket(C03PacketPlayer(serverOnGround))
                    }

                    mc.timer.timerSpeed = 0.45F
                    resetTimer = true
                }
            }
        }

        timer.reset()
    }
}
