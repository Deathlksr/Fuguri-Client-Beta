package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object TestPing : Module("TestPing", Category.COMBAT) {

    private val randomdelay by BoolValue("RandomDelay", false)
    private val delay by IntegerValue("Delay", 500, 0..1000) { !randomdelay }
    private val mindelay = IntegerValue("MinDelay", 500, 0..1000) { randomdelay }
    private val maxdelay = IntegerValue("MaxDelay", 520, 0..1000) { randomdelay }
    private val attackrecoil by IntegerValue("AttackFlushConditionTime", 100, 0..500)
    private val flagrecoil by IntegerValue("FlagFlushConditionTime", 100, 0..500)
    private val velocityrecoil by IntegerValue("VelocityFlushConditionTime", 0, 0..500)
    private val line by BoolValue("Line", true, subjective = true)
    private val thirdperson by BoolValue("OnlyThirdPerson", true) { line }
    private val red by FloatValue("Red", 1.0F, 0.0F..1.0F) { line }
    private val green by FloatValue("Green", 1.0F, 0.0F..1.0F) { line }
    private val blue by FloatValue("Blue", 1.0F, 0.0F..1.0F) { line }

    private val scale by FloatValue("Scale", 5.0F, 5.0F..500.0F) { line }

    private var lastMS = 0L
    private val packetQueue = LinkedHashMap<Packet<*>, Long>()
    private val positions = LinkedHashMap<Vec3, Long>()
    private var randomdelays = 0
    private var recoiltime = 0

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val serverData = mc.currentServerData

        if (mc.isIntegratedServerRunning || serverData == null) {
            return
        }

        if (!handleEvents())
            return

        if (event.isCancelled)
            return

        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> {
                return
            }

            is C02PacketUseEntity -> {
                recoiltime = attackrecoil
            }

            is S08PacketPlayerPosLook -> {
                recoiltime = flagrecoil
            }

            is S12PacketEntityVelocity -> {
                if (mc.thePlayer.entityId == packet.entityID) {
                    recoiltime = velocityrecoil
                }
            }
        }

        if (recoiltime > 0) {
            resetPackets()
            return
        }
        if (event.eventType == EventState.SEND) {
            event.cancelEvent()
            if (packet is C03PacketPlayer && packet.isMoving) {
                val packetPos = Vec3(packet.x, packet.y, packet.z)
                synchronized(positions) {
                    positions[packetPos] = System.currentTimeMillis()
                }
                if (packet.rotating) {
                    RotationUtils.serverRotation = Rotation(packet.yaw, packet.pitch)
                }
            }
            synchronized(packetQueue) {
                packetQueue[packet] = System.currentTimeMillis()
            }
        }
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        mc.thePlayer ?: return

        if (lastMS == 0L) {
            lastMS = System.currentTimeMillis()
        }

        if (recoiltime > 0) {
            recoiltime -= (System.currentTimeMillis() - lastMS).toInt()
            lastMS = System.currentTimeMillis()
        }

        handlePackets()

        if (!randomdelay) randomdelays = delay
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (event.worldClient == null)
            resetPackets(false)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!line) return

        if (mc.gameSettings.thirdPersonView == 0 && thirdperson)
            return

        val color = Color(red, green, blue)

        synchronized(positions.keys) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_BLEND)
            glDisable(GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            glBegin(GL_POINTS)
            glColor(color)
            glPointSize(scale)

            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            for (pos in positions.keys)
                glVertex3d(pos.xCoord - renderPosX, pos.yCoord - renderPosY, pos.zCoord - renderPosZ)

            glColor4d(1.0, 1.0, 1.0, 1.0)
            glPointSize(1F)
            glEnd()
            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    private fun resetPackets(handlePackets: Boolean = true) {
        synchronized(packetQueue) {
            if (handlePackets) {
                packetQueue.forEach { (packet) -> sendPacket(packet, false) }
            }
        }

        packetQueue.clear()
        positions.clear()
        if (randomdelay) randomdelays = RandomUtils.nextInt(mindelay.get(), maxdelay.get())
    }

    private fun handlePackets() {
        synchronized(packetQueue) {
            packetQueue.entries.removeAll { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - randomdelays) {
                    sendPacket(packet, false)
                    true
                } else false
            }
        }

        synchronized(positions) {
            positions.entries.removeAll { (_, timestamp) -> timestamp <= System.currentTimeMillis() - randomdelays }
        }
    }
}