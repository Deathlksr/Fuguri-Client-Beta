/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.RotationUtils.currentRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.isRotationFaced
import net.deathlksr.fuguribeta.utils.RotationUtils.setTargetRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.toRotation
import net.deathlksr.fuguribeta.utils.extensions.*
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.projectile.EntityFireball
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.world.WorldSettings

object AntiFireball : Module("AntiFireball", Category.PLAYER, hideModule = false) {
    private val range by FloatValue("Range", 4.5f, 3f..8f)
    private val swing by ListValue("Swing", arrayOf("Normal", "Packet", "None"), "Normal")

    private val rotations by BoolValue("RotationHandler", true)
    private val smootherMode by ListValue("SmootherMode", arrayOf("Linear", "Relative"), "Relative") { rotations }
    private val strafe by BoolValue("Strafe", false) { rotations }

    private val simulateShortStop by BoolValue("SimulateShortStop", false) { rotations }
    private val startRotatingSlow by BoolValue("StartRotatingSlow", false) { rotations }
    private val slowDownOnDirectionChange by BoolValue("SlowDownOnDirectionChange", false) { rotations }
    private val useStraightLinePath by BoolValue("UseStraightLinePath", true) { rotations }
    private val maxHorizontalSpeedValue = object : FloatValue("MaxHorizontalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minHorizontalSpeed)
        override fun isSupported() = rotations

    }
    private val maxHorizontalSpeed by maxHorizontalSpeedValue

    private val minHorizontalSpeed: Float by object : FloatValue("MinHorizontalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxHorizontalSpeed)
        override fun isSupported() = !maxHorizontalSpeedValue.isMinimal() && rotations
    }

    private val maxVerticalSpeedValue = object : FloatValue("MaxVerticalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minVerticalSpeed)
    }
    private val maxVerticalSpeed by maxVerticalSpeedValue

    private val minVerticalSpeed: Float by object : FloatValue("MinVerticalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxVerticalSpeed)
        override fun isSupported() = !maxVerticalSpeedValue.isMinimal() && rotations
    }

    private val angleThresholdUntilReset by FloatValue("AngleThresholdUntilReset", 5f, 0.1f..180f) { rotations }

    private val minRotationDifference by FloatValue("MinRotationDifference", 0f, 0f..1f) { rotations }

    private val fireballTickCheck by BoolValue("FireballTickCheck", true)
    private val minFireballTick by IntegerValue("MinFireballTick", 10, 1..20) { fireballTickCheck }

    private var target: Entity? = null

    @EventTarget
    fun onRotationUpdate(event: RotationUpdateEvent) {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        target = null

        for (entity in world.loadedEntityList.filterIsInstance<EntityFireball>()
            .sortedBy { player.getDistanceToBox(it.hitBox) }) {
            val nearestPoint = getNearestPointBB(player.eyes, entity.hitBox)

            val entityPrediction = entity.currPos - entity.prevPos

            val normalDistance = player.getDistanceToBox(entity.hitBox)

            val predictedDistance = player.getDistanceToBox(
                entity.hitBox.offset(
                    entityPrediction.xCoord,
                    entityPrediction.yCoord,
                    entityPrediction.zCoord
                )
            )

            // Skip if the predicted distance is (further than/same as) the normal distance or the predicted distance is out of reach
            if (predictedDistance >= normalDistance || predictedDistance > range) {
                continue
            }

            // Skip if the fireball entity tick exist is lower than minFireballTick
            if (fireballTickCheck && entity.ticksExisted <= minFireballTick) {
                continue
            }

            if (rotations) {
                setTargetRotation(
                    toRotation(nearestPoint, true),
                    strafe = this.strafe,
                    turnSpeed = minHorizontalSpeed..maxHorizontalSpeed to minVerticalSpeed..maxVerticalSpeed,
                    angleThresholdForReset = angleThresholdUntilReset,
                    smootherMode = smootherMode,
                    simulateShortStop = simulateShortStop,
                    startOffSlow = startRotatingSlow,
                    slowDownOnDirChange = slowDownOnDirectionChange,
                    useStraightLinePath = useStraightLinePath,
                    minRotationDifference = minRotationDifference
                )
            }

            target = entity
            break
        }
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        val player = mc.thePlayer ?: return
        val entity = target ?: return

        val rotation = currentRotation ?: player.rotation

        if (!rotations && player.getDistanceToBox(entity.hitBox) <= range
            || isRotationFaced(entity, range.toDouble(), rotation)
        ) {
            when (swing) {
                "Normal" -> mc.thePlayer.swingItem()
                "Packet" -> sendPacket(C0APacketAnimation())
            }

            sendPacket(C02PacketUseEntity(entity, C02PacketUseEntity.Action.ATTACK))

            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR) {
                player.attackTargetEntityWithCurrentItem(entity)
            }

            target = null
        }
    }
}