package net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.blocksmc

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.boostSpeed
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.debugFly
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.extraBoost
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.stable
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.stopOnLanding
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.stopOnNoMove
import net.deathlksr.fuguribeta.features.module.modules.movement.Flight.timerSlowed
import net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.FlyMode
import net.deathlksr.fuguribeta.script.api.global.Chat
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.MovementUtils.strafe
import net.deathlksr.fuguribeta.utils.PacketUtils
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPackets
import net.deathlksr.fuguribeta.utils.extensions.tryJump
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.Packet
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.world.World

/**
 * Implements a flying method similar to BlocksMC Fly mode, but instead of clipping,
 * it lags (blinks) the player instead.
 *
 * Note:
 * Clipping is likely patched, as players may receive false bans if phased through a block after reaching
 * certain (VL). Prolonged flight over long distances is not recommended.
 *
 * @author EclipsesDev
 */
object BlocksMC2 : FlyMode("BlocksMC2") {

    private var isFlying = false
    private var isNotUnder = false
    private var isBlinked = false
    private var airborneTicks = 0
    private var jumped = false

    private val packets = mutableListOf<Packet<*>>()
    private val packetsReceived = mutableListOf<Packet<*>>()

    override fun onUpdate() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        if (isFlying) {
            if (player.onGround && stopOnLanding) {
                if (debugFly)
                    Chat.print("Ground Detected.. Stopping Fly")
                Flight.state = false
            }

            if (!isMoving && stopOnNoMove) {
                if (debugFly)
                    Chat.print("No Movement Detected.. Stopping Fly. (Could be flagged)")
                Flight.state = false
            }
        }

        updateOffGroundTicks(player)

        if (shouldFly(player, world)) {
            if (isBlinked) {

                if (stable)
                    player.motionY = 0.0

                handleTimerSlow(player)
                handlePlayerFlying(player)
            } else {
                if (player.onGround)
                    strafe()
            }
        } else {
            if (debugFly)
                Chat.print("Pls stand under a block")
        }
    }

    override fun onDisable() {
        isNotUnder = false
        isFlying = false
        jumped = false
        isBlinked = false

        if (mc.thePlayer == null)
            return

        blink()
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        Flight.state = false

        // Clear packets on disconnect
        if (event.worldClient == null) {
            packets.clear()
            packetsReceived.clear()
        }
    }

    private fun updateOffGroundTicks(player: EntityPlayerSP) {
        airborneTicks = if (player.onGround) 0 else airborneTicks++
    }

    private fun handleTimerSlow(player: EntityPlayerSP) {
        if (!player.onGround && timerSlowed) {
            if (player.ticksExisted % 4 == 0) {
                mc.timer.timerSpeed = 0.45f
            } else {
                mc.timer.timerSpeed = 0.4f
            }
        } else {
            mc.timer.timerSpeed = 1.0f
        }
    }

    private fun shouldFly(player: EntityPlayerSP, world: World): Boolean {
        return world.getCollidingBoundingBoxes(player, player.entityBoundingBox.offset(0.0, 0.5, 0.0)).isEmpty() || isFlying
    }

    private fun handlePlayerFlying(player: EntityPlayerSP) {
        when (airborneTicks) {
            0 -> {
                if (isNotUnder) {
                    strafe(boostSpeed + extraBoost)
                    player.tryJump()
                    isFlying = true
                    isNotUnder = false
                }
            }
            1 -> {
                if (isFlying) {
                    strafe(boostSpeed)
                }
            }
        }
    }

    @EventTarget
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (mc.thePlayer == null || mc.theWorld == null || mc.thePlayer.isDead)
            return

        if (event.isCancelled)
            return

        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is S02PacketChat, is S40PacketDisconnect -> {
                return
            }
        }

        if (!isBlinked) {

            isNotUnder = true
            isBlinked = true

            if (debugFly)
                Chat.print("blinked.. fly now!")

            if (event.eventType == EventState.RECEIVE && mc.thePlayer.ticksExisted > 10) {
                event.cancelEvent()
                synchronized(packetsReceived) {
                    packetsReceived += packet
                }
            }
            if (event.eventType == EventState.SEND) {
                synchronized(packets) {
                    sendPackets(*packets.toTypedArray(), triggerEvents = false)
                }
                packets.clear()
            }
        }
    }

    @EventTarget
    override fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isDead || mc.thePlayer.ticksExisted <= 10) {
            blink()
        }

        synchronized(packets) {
            sendPackets(*packets.toTypedArray(), triggerEvents = false)
        }
        packets.clear()
    }

    private fun blink() {
        synchronized(packetsReceived) {
            PacketUtils.queuedPackets.addAll(packetsReceived)
        }
        synchronized(packets) {
            sendPackets(*packets.toTypedArray(), triggerEvents = false)
        }

        packets.clear()
        packetsReceived.clear()
    }
}