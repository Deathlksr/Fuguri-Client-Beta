/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.other.FlagDetect
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.injection.implementations.IMixinEntity
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.*
import net.ccbluex.liquidbounce.utils.render.ColorUtils.rainbow
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.Vec3
import okhttp3.internal.notify
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object DelayPackets : Module("DelayPackets", Category.COMBAT, gameDetecting = false, hideModule = false) {

    private val delay by IntegerValue("Delay", 610, 0..1000)
    private val distanceToPlayers by FloatValue("Allowed-Distance-To-Players", 3.0f, 0.0f..6.0f)
    private val minticksalive by IntegerValue("Min-Ticks-Alive-To-Work", 20, 0..60)
    private val flushoninv by BoolValue("Flush-Inventory", false)
    private val flushoncontainer by BoolValue("Flush-Container", false)
    private val flushonattack by BoolValue("Flush-Attack", false)
    private val gotdamage by BoolValue("Flush Got-Damaged", false)
    private val gotvelocity by BoolValue("Flush-Velocity", false)
    private val scaffoldcheck by BoolValue("Flush-BlockPlacement", false)
    private val useitemcheck by BoolValue("Flush-UsingItem", false)

    private val packetQueue = LinkedHashMap<Packet<*>, Long>()
    private val positions = LinkedHashMap<Vec3, Long>()
    private val resetTimer = MSTimer()
    private var wasNearPlayer = false
    private var ignoreWholeTick = false

    private val line by BoolValue("Line", true, subjective = true)
    private val rainbow by BoolValue("Rainbow", false, subjective = true) { line }
    private val red by IntegerValue("R", 0, 0..255, subjective = true) { !rainbow && line }
    private val green by IntegerValue("G", 255, 0..255, subjective = true) { !rainbow && line }
    private val blue by IntegerValue("B", 0, 0..255, subjective = true) { !rainbow && line }

    override fun onDisable() {
        if (mc.thePlayer == null) {
            return
        }
        blink()
    }

    @EventTarget
    fun onPacket(event : PacketEvent) {
        val packet = event.packet

        if (!handleEvents())
            return

        if (mc.thePlayer == null || mc.thePlayer.isDead)
            return

        if (mc.thePlayer.ticksExisted < minticksalive) {
            blink()
            return
        }

        if (event.isCancelled)
            return

        if (distanceToPlayers > 0.0 && wasNearPlayer)
            return

        if (ignoreWholeTick)
            return

        if (mc.thePlayer.health < mc.thePlayer.maxHealth) {
            if (mc.thePlayer.hurtTime != 0) {
                if (gotdamage.takeIf{isActive} == true) {
                    blink()
                    return
                }
            }
        }

        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> {
                return
            }

            is C0DPacketCloseWindow, is C0EPacketClickWindow -> {
                if (flushoninv.takeIf { isActive } == true) {
                    blink()
                    return
                }
            }

            is C12PacketUpdateSign, is C02PacketUseEntity -> {
                if (flushonattack.takeIf {isActive} == true) {
                    blink()
                    return
                }
            }

            is C07PacketPlayerDigging, is C08PacketPlayerBlockPlacement -> {
                if (useitemcheck.takeIf {isActive} == true) {
                    blink()
                    return
                }
            }

            is C08PacketPlayerBlockPlacement -> {
                if (scaffoldcheck.takeIf { isActive } == true) {
                    blink()
                    return
                }
            }

            is S12PacketEntityVelocity -> {
                if (mc.thePlayer.entityId == packet.entityID) {
                    if (gotvelocity.takeIf{isActive} == true) {
                        blink()
                        return
                    }
                }
            }
        }

        val screen = mc.currentScreen

        if (flushoninv.takeIf { isActive } == true) {
            if (screen is GuiInventory) {
                blink()
                return
            }
        }

        if (flushoncontainer.takeIf { isActive } == true) {
            if (screen is GuiContainer) {
                blink()
                return
            }
        }

        if (!resetTimer.hasTimePassed(0))
            return

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
    fun onWorld(event: WorldEvent) {
        // Clear packets on disconnect only
        if (event.worldClient == null)
            blink(false)
    }

    private fun getTruePositionEyes(player: EntityPlayer): Vec3 {
        val mixinPlayer = player as? IMixinEntity
        return Vec3(mixinPlayer!!.trueX, mixinPlayer.trueY + player.getEyeHeight().toDouble(), mixinPlayer.trueZ)
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (distanceToPlayers > 0) {
            val playerPos = thePlayer.positionVector
            val serverPos = positions.keys.firstOrNull() ?: playerPos

            val otherPlayers = mc.theWorld.playerEntities.filter { it != thePlayer }

            val (dx, dy, dz) = serverPos - playerPos
            val playerBox = thePlayer.hitBox.offset(dx, dy, dz)

            wasNearPlayer = false

            for (otherPlayer in otherPlayers) {
                val entityMixin = otherPlayer as? IMixinEntity
                if (entityMixin != null) {
                    val eyes = getTruePositionEyes(otherPlayer)
                    if (eyes.distanceTo(getNearestPointBB(eyes, playerBox)) <= distanceToPlayers.toDouble()) {
                        blink()
                        wasNearPlayer = true
                        return
                    }
                }
            }
        }

        if (Blink.blinkingSend() || mc.thePlayer.isDead || thePlayer.isUsingItem) {
            blink()
            return
        }

        if (!resetTimer.hasTimePassed(0))
            return

        handlePackets()
        ignoreWholeTick = false
    }




    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!line) return

        val color = if (rainbow) rainbow() else Color(red, green, blue)

        if (Blink.blinkingSend())
            return

        synchronized(positions.keys) {
            glPushMatrix()
            glDisable(GL_TEXTURE_2D)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            glEnable(GL_LINE_SMOOTH)
            glEnable(GL_BLEND)
            glDisable(GL_DEPTH_TEST)
            mc.entityRenderer.disableLightmap()
            glBegin(GL_LINE_STRIP)
            glColor(color)

            val renderPosX = mc.renderManager.viewerPosX
            val renderPosY = mc.renderManager.viewerPosY
            val renderPosZ = mc.renderManager.viewerPosZ

            for (pos in positions.keys)
                glVertex3d(pos.xCoord - renderPosX, pos.yCoord - renderPosY, pos.zCoord - renderPosZ)

            glColor4d(1.0, 1.0, 1.0, 1.0)
            glEnd()
            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    private fun blink(handlePackets: Boolean = true) {
        synchronized(packetQueue) {
            if (handlePackets) {
                resetTimer.reset()

                packetQueue.forEach { (packet) -> sendPacket(packet, false) }
            }
        }

        packetQueue.clear()
        positions.clear()
        ignoreWholeTick = true
    }

    private fun handlePackets() {
        synchronized(packetQueue) {
            packetQueue.entries.removeAll { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - delay) {
                    sendPacket(packet, false)
                    true
                } else false
            }
        }

        synchronized(positions) {
            positions.entries.removeAll { (_, timestamp) -> timestamp <= System.currentTimeMillis() - delay }
        }
    }

}