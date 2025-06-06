/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPackets
import net.deathlksr.fuguribeta.utils.RotationUtils.faceBlock
import net.deathlksr.fuguribeta.utils.RotationUtils.setTargetRotation
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlock
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getCenterDistance
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawBlockBox
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.init.Blocks.air
import net.minecraft.init.Blocks.bedrock
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.START_DESTROY_BLOCK
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.awt.Color

object CivBreak : Module("CivBreak", Category.OTHER) {

    private val range by FloatValue("Range", 5F, 1F..6F)
    private val visualSwing by BoolValue("VisualSwing", true, subjective = false)

    private val rotations by BoolValue("RotationHandler", true)
    private val strafe by ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Off") { rotations }
    private val smootherMode by ListValue("SmootherMode", arrayOf("Linear", "Relative"), "Relative") { rotations }

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

    private var blockPos: BlockPos? = null
    private var enumFacing: EnumFacing? = null

    @EventTarget
    fun onBlockClick(event: ClickBlockEvent) {
        if (event.clickedBlock?.let { getBlock(it) } == bedrock) {
            return
        }

        blockPos = event.clickedBlock ?: return
        enumFacing = event.enumFacing ?: return

        // Break
        sendPackets(
            C07PacketPlayerDigging(START_DESTROY_BLOCK, blockPos, enumFacing),
            C07PacketPlayerDigging(STOP_DESTROY_BLOCK, blockPos, enumFacing)
        )
    }

    @EventTarget
    fun onRotationUpdate(event: RotationUpdateEvent) {

        val pos = blockPos ?: return
        val isAirBlock = getBlock(pos) == air

        if (isAirBlock || getCenterDistance(pos) > range) {
            blockPos = null
            return
        }

        if (rotations) {
            val spot = faceBlock(pos) ?: return

            setTargetRotation(
                spot.rotation,
                strafe = strafe != "Off",
                strict = strafe == "Strict",
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
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        blockPos ?: return
        enumFacing ?: return

        if (visualSwing) {
            mc.thePlayer.swingItem()
        } else {
            sendPacket(C0APacketAnimation())
        }

        // Break
        sendPackets(
            C07PacketPlayerDigging(START_DESTROY_BLOCK, blockPos, enumFacing),
            C07PacketPlayerDigging(STOP_DESTROY_BLOCK, blockPos, enumFacing)
        )

        mc.playerController.clickBlock(blockPos, enumFacing)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        drawBlockBox(blockPos ?: return, Color.RED, true)
    }
}