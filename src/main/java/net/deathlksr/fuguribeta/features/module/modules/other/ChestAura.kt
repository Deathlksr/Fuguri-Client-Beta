/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.modules.combat.KillAura
import net.deathlksr.fuguribeta.features.module.modules.player.Blink
import net.deathlksr.fuguribeta.utils.ClientUtils.displayChatMessage
import net.deathlksr.fuguribeta.utils.EntityUtils.isSelected
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.RotationUtils.currentRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.getVectorForRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.performRayTrace
import net.deathlksr.fuguribeta.utils.RotationUtils.performRaytrace
import net.deathlksr.fuguribeta.utils.RotationUtils.setTargetRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.toRotation
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlock
import net.deathlksr.fuguribeta.utils.extensions.*
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils.serverOpenContainer
import net.deathlksr.fuguribeta.utils.misc.StringUtils.contains
import net.deathlksr.fuguribeta.utils.realX
import net.deathlksr.fuguribeta.utils.realY
import net.deathlksr.fuguribeta.utils.realZ
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.block.BlockChest
import net.minecraft.block.BlockEnderChest
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.client.C0APacketAnimation
import net.minecraft.network.play.server.S0EPacketSpawnObject
import net.minecraft.network.play.server.S24PacketBlockAction
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.tileentity.TileEntityEnderChest
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt

object ChestAura : Module("ChestAura", Category.OTHER) {

    private val chest by BoolValue("Chest", true)
    private val enderChest by BoolValue("EnderChest", false)

    private val range: Float by object : FloatValue("Range", 5F, 1F..5F) {
        override fun onUpdate(value: Float) {
            rangeSq = value.pow(2)
            searchRadiusSq = (value + 1).pow(2)
        }
    }
    private val delay by IntegerValue("Delay", 200, 50..500)

    private val throughWalls by BoolValue("ThroughWalls", true)
    private val wallsRange: Float by object : FloatValue("ThroughWallsRange", 3F, 1F..5F) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(this@ChestAura.range)

        override fun onUpdate(value: Float) {
            wallsRangeSq = value.pow(2)
        }

        override fun isSupported() = throughWalls
    }

    private val minDistanceFromOpponent: Float by object : FloatValue("MinDistanceFromOpponent", 10F, 0F..30F) {
        override fun onUpdate(value: Float) {
            minDistanceFromOpponentSq = value.pow(2)
        }
    }

    private val visualSwing by BoolValue("VisualSwing", true, subjective = true)

    private val ignoreLooted by BoolValue("IgnoreLootedChests", true)
    private val detectRefill by BoolValue("DetectChestRefill", true)

    private val rotations by BoolValue("RotationHandler", true)
    private val silentRotation by BoolValue("SilentRotation", true) { rotations }

    // Turn Speed
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
    private val strafe by ListValue("Strafe", arrayOf("Off", "Strict", "Silent"), "Off") { silentRotation && rotations }
    private val smootherMode by ListValue("SmootherMode", arrayOf("Linear", "Relative"), "Relative") { rotations }

    private val keepRotation by IntegerValue("KeepRotationTicks", 5, 1..20) { silentRotation && rotations }

    private val angleThresholdUntilReset by FloatValue("AngleThresholdUntilReset",
        5f,
        0.1f..180f
    ) { silentRotation && rotations }

    private val minRotationDifference by FloatValue("MinRotationDifference", 0f, 0f..1f) { rotations }

    private val openInfo by ListValue("OpenInfo", arrayOf("Off", "Self", "Other", "Everyone"), "Off")

    var tileTarget: Triple<Vec3, TileEntity, Double>? = null
    private val timer = MSTimer()

    // Squared distances, they get updated when values initiate or get changed
    private var searchRadiusSq = 0f // (range + 1) ^ 2
    private var rangeSq = 0f
    private var wallsRangeSq = 0f
    private var minDistanceFromOpponentSq = 0f

    val clickedTileEntities = mutableSetOf<TileEntity>()
    private val chestOpenMap = mutableMapOf<BlockPos, Pair<Int, Long>>()

    // Substrings that indicate that chests have been refilled, broadcasted via title packet
    private val refillSubstrings = arrayOf("refill", "reabastecidos")
    private val decimalFormat = DecimalFormat("##0.00", DecimalFormatSymbols(Locale.ENGLISH))

    @EventTarget
    fun onRotationUpdate(event: RotationUpdateEvent) {
        if (Blink.handleEvents() || KillAura.isBlockingChestAura || !timer.hasTimePassed(delay))
            return

        val thePlayer = mc.thePlayer ?: return

        // Check if there is an opponent in range
        if (mc.theWorld.loadedEntityList.any {
                isSelected(it, true) && thePlayer.getDistanceSqToEntity(it) < minDistanceFromOpponentSq
            }) return

        if (serverOpenContainer && tileTarget != null) {
            timer.reset()

            return
        }

        val eyes = thePlayer.eyes

        val pointsInRange = mc.theWorld.tickableTileEntities
            // Check if tile entity is correct type, not already clicked, not blocked by a block and in range
            .filter {
                shouldClickTileEntity(it) && it.getDistanceSq(thePlayer.posX,
                    thePlayer.posY,
                    thePlayer.posZ
                ) <= searchRadiusSq
            }.flatMap { entity ->
                val box = entity.blockType.getSelectedBoundingBox(mc.theWorld, entity.pos)

                val points = mutableListOf(getNearestPointBB(eyes, box))

                for (x in 0.0..1.0) {
                    for (y in 0.0..1.0) {
                        for (z in 0.0..1.0) {
                            points += Vec3(
                                box.minX + (box.maxX - box.minX) * x,
                                box.minY + (box.maxY - box.minY) * y,
                                box.minZ + (box.maxZ - box.minZ) * z
                            )
                        }
                    }
                }

                points
                    .map { Triple(it, entity, it.squareDistanceTo(eyes)) }
                    .filter { it.third <= rangeSq }

            }.sortedBy { it.third }

        // Vecs are already sorted by distance
        val closestClickable = pointsInRange
            .firstOrNull { (vec, entity) ->
                // If through walls is enabled and its range is same as normal, just return the first one
                if (throughWalls && wallsRange >= range)
                    return@firstOrNull true

                val result = mc.theWorld.rayTraceBlocks(eyes, vec) ?: return@firstOrNull false
                val distanceSq = result.hitVec.squareDistanceTo(eyes)

                // If chest is behind a wall, check if through walls is enabled and its range
                if (result.blockPos != entity.pos) throughWalls && distanceSq <= wallsRangeSq
                else distanceSq <= rangeSq
            } ?: return

        tileTarget = closestClickable

        if (rotations) {
            setTargetRotation(
                toRotation(closestClickable.first),
                keepRotation,
                silentRotation && strafe != "Off",
                silentRotation && strafe == "Strict",
                !silentRotation,
                minHorizontalSpeed..maxHorizontalSpeed to minVerticalSpeed..maxVerticalSpeed,
                angleThresholdUntilReset,
                smootherMode,
                simulateShortStop,
                startRotatingSlow,
                slowDownOnDirChange = slowDownOnDirectionChange,
                useStraightLinePath = useStraightLinePath,
                minRotationDifference = minRotationDifference
            )
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) = onDisable()

    override fun onDisable() {
        clickedTileEntities.clear()
        chestOpenMap.clear()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        when (val packet = event.packet) {
            // Detect chest opening from sound effect
            is S29PacketSoundEffect -> {
                if (packet.soundName != "random.chestopen")
                    return

                val entity = mc.theWorld.getTileEntity(BlockPos(packet.x, packet.y, packet.z)) ?: return

                clickedTileEntities += entity
            }

            // Detect already looted chests by having their lid open or closed
            is S24PacketBlockAction -> {
                if (!ignoreLooted || (packet.blockType !is BlockChest && packet.blockType !is BlockEnderChest))
                    return

                clickedTileEntities += mc.theWorld.getTileEntity(packet.blockPosition)

                if (openInfo != "Off") {
                    val (prevState, prevTime) = chestOpenMap[packet.blockPosition] ?: (null to null)

                    // Prevent repetitive packet spamming
                    if (prevState == packet.data2)
                        return

                    // If there is no info about the chest ever being opened, don't print anything
                    if (packet.data2 == 0 && prevState != 1)
                        return

                    val player: EntityPlayer
                    val distance: String

                    // If chest is not last clicked chest, find a player that might have opened it
                    if (packet.blockPosition != tileTarget?.second?.pos) {
                        val nearPlayers = mc.theWorld.playerEntities
                            .mapNotNull {
                                val distanceSq = it.getDistanceSqToCenter(packet.blockPosition)

                                if (distanceSq <= 36) it to distanceSq
                                else null
                            }.sortedBy { it.second }

                        if (nearPlayers.isEmpty())
                            return

                        // Find the closest player that is looking at the chest or else just the closest
                        player = (nearPlayers.firstOrNull { (player) ->
                            player.rayTrace(5.0, 1f)?.blockPos == packet.blockPosition
                        } ?: nearPlayers.first()).first

                        val entity = mc.theWorld.getTileEntity(packet.blockPosition)
                        val box = entity.blockType.getSelectedBoundingBox(mc.theWorld, packet.blockPosition)
                        distance = decimalFormat.format(player.getDistanceToBox(box))
                    } else {
                        player = mc.thePlayer
                        distance = decimalFormat.format(sqrt(tileTarget!!.third))
                    }

                    when (player) {
                        mc.thePlayer -> if (openInfo == "Other") return
                        else -> if (openInfo == "Self") return
                    }

                    val actionMsg = if (packet.data2 == 1) "§a§lOpened§3" else "§c§lClosed§3"
                    val timeTakenMsg = if (packet.data2 == 0 && prevTime != null)
                        ", took §b${decimalFormat.format((System.currentTimeMillis() - prevTime) / 1000.0)} s§3"
                    else ""
                    val playerMsg = if (player == mc.thePlayer) actionMsg else "§b${player.name} §3${actionMsg.lowercase()}"

                    displayChatMessage("§8[§9§lChestAura§8] $playerMsg chest from §b$distance m§3$timeTakenMsg.")

                    chestOpenMap[packet.blockPosition] = packet.data2 to System.currentTimeMillis()
                }
            }

            // Detect chests getting refilled
            is S45PacketTitle -> {
                if (!detectRefill)
                    return

                if (refillSubstrings in packet.message?.unformattedText)
                    clickedTileEntities.clear()
            }

            // Armor stands might be showing time until opened chests get refilled
            // Whenever an armor stand spawns, blacklist chest that it might be inside
            is S0EPacketSpawnObject -> {
                if (ignoreLooted && packet.type == 78) {
                    val entity = mc.theWorld.getTileEntity(
                        BlockPos(packet.realX, packet.realY + 2.0, packet.realZ)
                    )

                    if (entity !is TileEntityChest && entity !is TileEntityEnderChest)
                        return

                    clickedTileEntities += entity
                }
            }
        }
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        val player = mc.thePlayer ?: return
        val target = tileTarget ?: return

        val rotationToUse = if (rotations) {
            currentRotation ?: return
        } else toRotation(target.first)

        val distance = sqrt(target.third)

        if (distance <= range) {
            val pos = target.second.pos

            val rotationVec = getVectorForRotation(rotationToUse) * mc.playerController.blockReachDistance.toDouble()

            val visibleResult = performRayTrace(pos, rotationVec)
            val invisibleResult = performRaytrace(pos, rotationToUse)

            val resultToUse = if (visibleResult?.blockPos == pos) {
                visibleResult
            } else {
                if (invisibleResult?.blockPos == pos) {
                    invisibleResult
                } else null
            }

            resultToUse?.run {
                if (player.onPlayerRightClick(blockPos, sideHit, hitVec)) {
                    if (visualSwing) player.swingItem()
                    else sendPacket(C0APacketAnimation())

                    timer.reset()
                }
            }
        }
    }

    private fun shouldClickTileEntity(entity: TileEntity): Boolean {
        // Check if entity hasn't been clicked already
        if (entity in clickedTileEntities) return false

        // Check if entity is of correct type
        return when (entity) {
            is TileEntityChest -> {
                if (!chest) return false

                val block = getBlock(entity.pos)

                if (block !is BlockChest) return false

                // Check if there isn't a block above the chest (works even for double chests)
                block.getLockableContainer(mc.theWorld, entity.pos) != null
            }

            is TileEntityEnderChest ->
                enderChest && getBlock(entity.pos.up())?.isNormalCube != true

            else -> return false
        }
    }
}