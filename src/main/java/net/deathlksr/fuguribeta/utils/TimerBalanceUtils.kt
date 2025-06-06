/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.utils

import net.deathlksr.fuguribeta.event.*
import net.minecraft.network.play.client.C03PacketPlayer

object TimerBalanceUtils : MinecraftInstance(), Listenable {

    private var balance = 0L
    private var frametime = -1L
    private var prevframetime = -1L
    private var currframetime = -1L

    private val inGame: Boolean
        get() = mc.thePlayer != null && mc.theWorld != null && mc.netHandler != null && mc.playerController != null

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        if (frametime == -1L) {
            frametime = 0L
            currframetime = System.currentTimeMillis()
            prevframetime = currframetime
        }

        prevframetime = currframetime
        currframetime = System.currentTimeMillis()
        frametime = currframetime - prevframetime

        if (inGame) {
            balance -= frametime
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (inGame) {
            if (packet is C03PacketPlayer) {
                balance += 50
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        balance = 0
    }

    fun getBalance(): Long {
        return balance
    }

    override fun handleEvents() = true
}