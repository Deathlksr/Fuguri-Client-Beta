/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.utils

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.modules.combat.Ping
import net.deathlksr.fuguribeta.injection.implementations.IMixinEntity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.network.NetworkManager
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayClient
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.*
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import kotlin.math.roundToInt

object PacketUtils : MinecraftInstance(), Listenable {

    val queuedPackets = mutableListOf<Packet<*>>()

    @EventTarget(priority = 2)
    fun onTick(event: GameTickEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (entity is EntityLivingBase) {
                (entity as? IMixinEntity)?.apply {
                    if (!truePos) {
                        trueX = entity.posX
                        trueY = entity.posY
                        trueZ = entity.posZ
                        truePos = true
                    }
                }
            }
        }
    }

    @EventTarget(priority = 2)
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val world = mc.theWorld ?: return

        when (packet) {
            is S0CPacketSpawnPlayer ->
                (world.getEntityByID(packet.entityID) as? IMixinEntity)?.apply {
                    trueX = packet.realX
                    trueY = packet.realY
                    trueZ = packet.realZ
                    truePos = true
                }

            is S0FPacketSpawnMob ->
                (world.getEntityByID(packet.entityID) as? IMixinEntity)?.apply {
                    trueX = packet.realX
                    trueY = packet.realY
                    trueZ = packet.realZ
                    truePos = true
                }

            is S14PacketEntity -> {
                val entity = packet.getEntity(world)
                val mixinEntity = entity as? IMixinEntity

                mixinEntity?.apply {
                    if (!truePos) {
                        trueX = entity.posX
                        trueY = entity.posY
                        trueZ = entity.posZ
                        truePos = true
                    }

                    trueX += packet.realMotionX
                    trueY += packet.realMotionY
                    trueZ += packet.realMotionZ
                }
            }

            is S18PacketEntityTeleport ->
                (world.getEntityByID(packet.entityId) as? IMixinEntity)?.apply {
                    trueX = packet.realX
                    trueY = packet.realY
                    trueZ = packet.realZ
                    truePos = true
                }
        }
    }

    @EventTarget(priority = -5)
    fun onGameLoop(event: GameLoopEvent) {
        synchronized(queuedPackets) {
            queuedPackets.forEach {
                handlePacket(it)
                val packetEvent = PacketEvent(it, EventState.RECEIVE)
                Ping.onPacket(packetEvent)
            }

            queuedPackets.clear()
        }
    }

    @EventTarget(priority = -1)
    fun onDisconnect(event: WorldEvent) {
        if (event.worldClient == null) {
            synchronized(queuedPackets) {
                queuedPackets.clear()
            }
        }
    }

    override fun handleEvents() = true

    @JvmStatic
    fun sendPacket(packet: Packet<*>, triggerEvent: Boolean = true) {
        if (triggerEvent) {
            mc.netHandler?.addToSendQueue(packet)
            return
        }

        val netManager = mc.netHandler?.networkManager ?: return

        PPSCounter.registerType(PPSCounter.PacketType.SEND)
        if (netManager.isChannelOpen) {
            netManager.flushOutboundQueue()
            netManager.dispatchPacket(packet, null)
        } else {
            netManager.readWriteLock.writeLock().lock()
            try {
                netManager.outboundPacketsQueue += NetworkManager.InboundHandlerTuplePacketListener(packet, null)
            } finally {
                netManager.readWriteLock.writeLock().unlock()
            }
        }
    }

    @JvmStatic
    fun sendPackets(vararg packets: Packet<*>, triggerEvents: Boolean = true) =
        packets.forEach { sendPacket(it, triggerEvents) }

    fun handlePackets(vararg packets: Packet<*>) =
        packets.forEach { handlePacket(it) }

    fun handlePacket(packet: Packet<*>?) {
        runCatching { (packet as Packet<INetHandlerPlayClient>).processPacket(mc.netHandler) }.onSuccess {
            PPSCounter.registerType(PPSCounter.PacketType.RECEIVED)
        }
    }

    val Packet<*>.type
        get() = when (this.javaClass.simpleName[0]) {
            'C' -> PacketType.CLIENT
            'S' -> PacketType.SERVER
            else -> PacketType.UNKNOWN
        }

    enum class PacketType { CLIENT, SERVER, UNKNOWN }
}

var S12PacketEntityVelocity.realMotionX
    get() = motionX / 8000.0
    set(value) {
        motionX = (value * 8000.0).roundToInt()
    }
var S12PacketEntityVelocity.realMotionY
    get() = motionY / 8000.0
    set(value) {
        motionX = (value * 8000.0).roundToInt()
    }
var S12PacketEntityVelocity.realMotionZ
    get() = motionZ / 8000.0
    set(value) {
        motionX = (value * 8000.0).roundToInt()
    }

val S14PacketEntity.realMotionX
    get() = func_149062_c() / 32.0
val S14PacketEntity.realMotionY
    get() = func_149061_d() / 32.0
val S14PacketEntity.realMotionZ
    get() = func_149064_e() / 32.0

var S0EPacketSpawnObject.realX
    get() = x / 32.0
    set(value) {
        x = (value * 32.0).roundToInt()
    }
var S0EPacketSpawnObject.realY
    get() = y / 32.0
    set(value) {
        y = (value * 32.0).roundToInt()
    }
var S0EPacketSpawnObject.realZ
    get() = z / 32.0
    set(value) {
        z = (value * 32.0).roundToInt()
    }

val S0CPacketSpawnPlayer.realX
    get() = x / 32.0
val S0CPacketSpawnPlayer.realY
    get() = y / 32.0
val S0CPacketSpawnPlayer.realZ
    get() = z / 32.0
val S0FPacketSpawnMob.realX
    get() = x / 32.0
val S0FPacketSpawnMob.realY
    get() = y / 32.0
val S0FPacketSpawnMob.realZ
    get() = z / 32.0
val BlockBBEvent.pos
    get() = BlockPos(x, y, z)
val S18PacketEntityTeleport.realX
    get() = x / 32.0
val S18PacketEntityTeleport.realY
    get() = y / 32.0
val S18PacketEntityTeleport.realZ
    get() = z / 32.0

var C03PacketPlayer.pos
    get() = Vec3(x, y, z)
    set(value) {
        x = value.xCoord
        y = value.yCoord
        z = value.zCoord
    }