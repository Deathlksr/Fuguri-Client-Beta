/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement

import net.deathlksr.fuguribeta.event.EventState
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.MotionEvent
import net.deathlksr.fuguribeta.event.WorldEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPackets
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.play.client.C0BPacketEntityAction
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.START_SNEAKING
import net.minecraft.network.play.client.C0BPacketEntityAction.Action.STOP_SNEAKING

object Sneak : Module("Sneak", Category.MOVEMENT, hideModule = false) {

    val mode by ListValue("Mode", arrayOf("Legit", "Vanilla", "Switch", "MineSecure"), "MineSecure")
    val stopMove by BoolValue("StopMove", false)

    private var sneaking = false

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (stopMove && isMoving) {
            if (sneaking)
                onDisable()
            return
        }

        when (mode.lowercase()) {
            "legit" -> mc.gameSettings.keyBindSneak.pressed = true
            "vanilla" -> {
                if (sneaking)
                    return

                sendPacket(C0BPacketEntityAction(mc.thePlayer, START_SNEAKING))
            }

            "switch" -> {
                when (event.eventState) {
                    EventState.PRE -> {
                        sendPackets(
                            C0BPacketEntityAction(mc.thePlayer, START_SNEAKING),
                            C0BPacketEntityAction(mc.thePlayer, STOP_SNEAKING)
                        )
                    }
                    EventState.POST -> {
                        sendPackets(
                            C0BPacketEntityAction(mc.thePlayer, STOP_SNEAKING),
                            C0BPacketEntityAction(mc.thePlayer, START_SNEAKING)
                        )
                    }

                    else -> {}
                }
            }

            "minesecure" -> {
                if (event.eventState == EventState.PRE)
                    return

                sendPacket(C0BPacketEntityAction(mc.thePlayer, START_SNEAKING))
            }
        }
    }

    @EventTarget
    fun onWorld(worldEvent: WorldEvent) {
        sneaking = false
    }

    override fun onDisable() {
        val player = mc.thePlayer ?: return

        when (mode.lowercase()) {
            "legit" -> {
                if (!GameSettings.isKeyDown(mc.gameSettings.keyBindSneak)) {
                    mc.gameSettings.keyBindSneak.pressed = false
                }
            }
            "vanilla", "switch", "minesecure" -> sendPacket(C0BPacketEntityAction(player, STOP_SNEAKING))
        }
        sneaking = false
    }
}