package net.deathlksr.fuguribeta.features.module.modules.movement.flymodes.blocksmc

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.WorldEvent
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
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPackets
import net.deathlksr.fuguribeta.utils.extensions.tryJump
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.world.World

/**
 * Fly concept originally taken from CrossSine:
 * https://github.com/shxp3/CrossSine/blob/main/src/main/java/net/deathlksr/fuguribeta/features/module/modules/movement/flights/ncp/LatestNCP.java
 *
 * Modified by @Eclipses and mixed with code from NextGen:
 * https://github.com/CCBlueX/LiquidBounce/blob/nextgen/src/main/kotlin/net/deathlksr/fuguribeta/features/module/modules/movement/fly/modes/specific/FlyNcpClip.kt
 *
 * NOTE (Before using this fly mode, READ THIS):
 * Caution: Prolonged flying over long distances is not recommended.
 *
 * Additionally, ensure that you avoid flight before you got flagged or
 * (S08 Packet) teleported, as this will flag u more, or you can wait till
 * you get the Fly message Line(153). Also avoid flying too many times (At long distance).
 *
 * @author EclipsesDev
 */
object BlocksMC : FlyMode("BlocksMC") {

    private var isFlying = false
    private var isNotUnder = false
    private var isTeleported = false
    private var airborneTicks = 0
    private var jumped = false

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
            if (isTeleported) {

                if (stable)
                    player.motionY = 0.0

                handleTimerSlow(player)
                handlePlayerFlying(player)
            } else {
                if (debugFly)
                    Chat.print("Waiting to be Teleported.. Please ensure you're below a block.")
            }
        } else {
            handleTeleport(player)
        }

        strafe()
    }

    override fun onDisable() {
        isNotUnder = false
        isFlying = false
        isTeleported = false
        jumped = false
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        Flight.state = false
    }

    private fun updateOffGroundTicks(player: EntityPlayerSP) {
        airborneTicks = if (player.onGround) 0 else airborneTicks + 1
    }

    private fun handleTimerSlow(player: EntityPlayerSP) {
        if (!player.onGround && timerSlowed) {
            if (player.ticksExisted % 7 == 0) {
                mc.timer.timerSpeed = 0.415f
            } else {
                mc.timer.timerSpeed = 0.35f
            }
        } else {
            mc.timer.timerSpeed = 1.0f
        }
    }

    private fun shouldFly(player: EntityPlayerSP, world: World): Boolean {
        return world.getCollidingBoundingBoxes(player, player.entityBoundingBox.offset(0.0, 1.0, 0.0)).isEmpty() || isFlying
    }

    private fun handlePlayerFlying(player: EntityPlayerSP) {
        when (airborneTicks) {
            0 -> {
                if (isNotUnder && isTeleported) {
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

    private fun handleTeleport(player: EntityPlayerSP) {
        isNotUnder = true
        if (!isTeleported) {
            sendPackets(
                C04PacketPlayerPosition(
                    player.posX,
                    // Clipping is now patch in BlocksMC
                    player.posY - 0.05,
                    player.posZ,
                    false
                )
            )
            sendPackets(
                C04PacketPlayerPosition(
                    player.posX,
                    player.posY,
                    player.posZ,
                    false
                )
            )

            isTeleported = true
            if (debugFly)
                Chat.print("Teleported.. Fly Now!")
        }
    }
}