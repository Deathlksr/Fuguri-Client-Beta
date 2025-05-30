/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.modules.combat.KillAura
import net.deathlksr.fuguribeta.features.module.modules.exploit.Disabler
import net.deathlksr.fuguribeta.features.module.modules.exploit.disablermodes.verus.VerusFly.dontPlaceOnAttack
import net.deathlksr.fuguribeta.script.api.global.Chat
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.client.gui.GuiGameOver
import net.minecraft.init.Blocks
import net.minecraft.network.login.server.S00PacketDisconnect
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.server.S01PacketJoinGame
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.util.BlockPos
import kotlin.math.abs
import kotlin.math.sqrt

object FlagDetector : Module("FlagDetector", Category.OTHER, gameDetecting = true, hideModule = false) {

    private val resetFlagCounterTicks by IntegerValue("ResetCounterTicks", 600, 100..1000)
    private val rubberbandCheck by BoolValue("RubberbandCheck", false)
    private val rubberbandThreshold by FloatValue("RubberBandThreshold", 5.0f, 0.05f..10.0f) { rubberbandCheck }
    private val clearflagcheck by BoolValue("ClearFlags", false)

    var flagCount = 0
    private var lastYaw = 0F
    private var lastPitch = 0F

    private var blockPlacementAttempts = mutableMapOf<BlockPos, Long>()
    private var successfulPlacements = mutableSetOf<BlockPos>()

    private fun clearFlags() {
        flagCount = 0
        blockPlacementAttempts.clear()
        successfulPlacements.clear()
    }

    private var lagbackDetected = false
    private var forceRotateDetected = false

    private var lastMotionX = 0.0
    private var lastMotionY = 0.0
    private var lastMotionZ = 0.0

    private var lastPosX = 0.0
    private var lastPosY = 0.0
    private var lastPosZ = 0.0

    override fun onDisable() {
        clearFlags()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (player.ticksExisted <= 100)
            return

        if (player.isDead || (player.capabilities.isFlying && player.capabilities.disableDamage && !player.onGround))
            return

        if (packet is S08PacketPlayerPosLook) {
            val deltaYaw = calculateAngleDelta(packet.yaw, lastYaw)
            val deltaPitch = calculateAngleDelta(packet.pitch, lastPitch)

            if (deltaYaw > 90 || deltaPitch > 90) {
                forceRotateDetected = true
                flagCount++
                Chat.print("§cFlag Detected: §7${flagCount}")
            } else {
                forceRotateDetected = false
            }

            if (!forceRotateDetected) {
                lagbackDetected = true
                flagCount++
                Chat.print("§cFlag Detected: §7${flagCount}")
            }

            if (mc.thePlayer.ticksExisted % 3 == 0) {
                lagbackDetected = false
            }

            lastYaw = mc.thePlayer.rotationYawHead
            lastPitch = mc.thePlayer.rotationPitch
        }

        if (packet is C08PacketPlayerBlockPlacement) {
            val blockPos = packet.position
            blockPlacementAttempts[blockPos] = System.currentTimeMillis()
            successfulPlacements.add(blockPos)
        }

        when (packet) {
            is S01PacketJoinGame, is S00PacketDisconnect -> {
                if (clearflagcheck.takeIf { isActive } == true) {
                    clearFlags()
                }
            }
        }
    }

    private fun calculateAngleDelta(newAngle: Float, oldAngle: Float): Float {
        var delta = newAngle - oldAngle
        if (delta > 180) delta -= 360
        if (delta < -180) delta += 360
        return abs(delta)
    }

    /**
     * Rubberband, Invalid Health/Hunger & GhostBlock Checks
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        if (player.isDead || mc.currentScreen is GuiGameOver || player.ticksExisted <= 100) {
            return
        }

        val currentTime = System.currentTimeMillis()

        // GhostBlock Checks | Checks is disabled when using VerusFly Disabler, to prevent false flag.
        if (!Disabler.handleEvents() || (Disabler.handleEvents() && Disabler.mode.contains("VerusFly") && !dontPlaceOnAttack)) {
            blockPlacementAttempts.filter { (_, timestamp) ->
                currentTime - timestamp > 500
            }.forEach { (blockPos, _) ->
                val block = world.getBlockState(blockPos).block
                val isNotUsing =
                    !player.isUsingItem && !player.isBlocking && (!KillAura.renderBlocking || !KillAura.blockStatus)

                if (block == Blocks.air && player.swingProgressInt > 2 && successfulPlacements != blockPos && isNotUsing) {
                    successfulPlacements.remove(blockPos)
                    flagCount++
                    Chat.print("§cFlag Detected: §7${flagCount}")
                }

                blockPlacementAttempts.remove(blockPos)
            }
        }

        // Invalid Health/Hunger bar Checks (This is a known lagback by Intave AC)
        val invalidReason = mutableListOf<String>()
        if (player.health <= 0.0f) invalidReason.add("Health")
        if (player.foodStats.foodLevel <= 0) invalidReason.add("Hunger")

        if (invalidReason.isNotEmpty()) {
            flagCount++
            val reasonString = invalidReason.joinToString(" §8|§e ")
            Chat.print("§cFlag Detected: §7${flagCount}")
            invalidReason.clear()
        }

        // Rubberband Checks
        if (!rubberbandCheck || (player.capabilities.isFlying && player.capabilities.disableDamage && !player.onGround))
            return

        val motionX = player.motionX
        val motionY = player.motionY
        val motionZ = player.motionZ

        val deltaX = player.posX - lastPosX
        val deltaY = player.posY - lastPosY
        val deltaZ = player.posZ - lastPosZ

        val distanceTraveled = sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ)

        val rubberbandReason = mutableListOf<String>()

        if (distanceTraveled > rubberbandThreshold) {
            rubberbandReason.add("Invalid Position")
        }

        if (abs(motionX) > rubberbandThreshold || abs(motionY) > rubberbandThreshold || abs(motionZ) > rubberbandThreshold) {
            if (!player.isCollided && !player.onGround) {
                rubberbandReason.add("Invalid Motion")
            }
        }

        if (rubberbandReason.isNotEmpty()) {
            flagCount++
            val reasonString = rubberbandReason.joinToString(" §8|§e ")
            Chat.print("§cFlag Detected: §7${flagCount}")
            rubberbandReason.clear()
        }

        // Update last position and motion
        lastPosX = player.prevPosX
        lastPosY = player.prevPosY
        lastPosZ = player.prevPosZ

        lastMotionX = motionX
        lastMotionY = motionY
        lastMotionZ = motionZ

        // Automatically clear flags (Default: 10 minutes)
        if (player.ticksExisted % (resetFlagCounterTicks * 20) == 0) {
            if (clearflagcheck.takeIf { isActive } == true) {
                clearFlags()
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (clearflagcheck.takeIf { isActive } == true) {
            clearFlags()
        }
    }
}