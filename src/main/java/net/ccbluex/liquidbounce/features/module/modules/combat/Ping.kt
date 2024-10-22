package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
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

object Ping : Module("Ping", Category.COMBAT, gameDetecting = false, hideModule = false) {

    private val delay by IntegerValue("Delay", 500, 0..1000)
    private val recoildelay by IntegerValue("Recoil-Delay", 0, 0..250)
    private val ticksexxisted by BoolValue("Ticks-Existed", true)
    private val minticksalive by IntegerValue("Max-Ticks-Existed", 15, 0..60) { ticksexxisted }
    private val flushflag by BoolValue("Flush-Flag", true)
    private val tickflag by IntegerValue("Ticks-Flag", 3, 0..60) { flushflag }
    private val flushoninv by BoolValue("Flush-Inventory", true)
    private val flushoncontainer by BoolValue("Flush-Container", false)
    private val flushonattack by BoolValue("Flush-Attack", true)
    private val flushblock by BoolValue("Flush-Blocking", true)
    private val flushdamage by BoolValue("Flush Got-Damaged", false)
    private val flushvelocity by BoolValue("Flush-Velocity", false)
    private val flushuseitem by BoolValue("Flush-UsingItem", false)
    private val flushback by BoolValue("Flush-Back", false)
    private val flushclickgui by BoolValue("Flush-Click-Gui", false)

    private val packetQueue = LinkedHashMap<Packet<*>, Long>()
    private val positions = LinkedHashMap<Vec3, Long>()
    private val resetTimer = MSTimer()
    private var ignoreWholeTick = false
    private val line by BoolValue("Render", true, subjective = true)
    private val thirdperson by BoolValue("Only-Third-Person", true) { line }
    private val red by FloatValue("red", 1.0F, 0.0F..1.0F) { line }
    private val green by FloatValue("green", 1.0F, 0.0F..1.0F) { line }
    private val blue by FloatValue("blue", 1.0F, 0.0F..1.0F) { line }

    private var ticksFlag = 0

    override fun onDisable() {
        if (mc.thePlayer == null) {
            return
        }
        blink()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (!handleEvents())
            return

        if (mc.thePlayer == null || mc.thePlayer.isDead)
            return

        if (ticksexxisted) {
            if (mc.thePlayer.ticksExisted < minticksalive) {
                blink()
                return
            }
        }

        if (ticksFlag > 0) {
            blink()
            return
        }

        if (event.isCancelled)
            return

        if (ignoreWholeTick)
            return

        // Flush
        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> {
                return
            }

            is C02PacketUseEntity -> {
                if (flushonattack) {
                    blink()
                    return
                }
            }

            is C0EPacketClickWindow, is C0DPacketCloseWindow -> {
                if (flushoninv) {
                    blink()
                    return
                }
            }

            is S08PacketPlayerPosLook -> {
                if (flushflag) {
                    ticksFlag = tickflag
                    return
                }
            }

            is C07PacketPlayerDigging, is C08PacketPlayerBlockPlacement -> {
                if (flushuseitem) {
                    blink()
                    return
                }
            }

            is S12PacketEntityVelocity -> {
                if (mc.thePlayer.entityId == packet.entityID) {
                    if (flushvelocity) {
                        blink()
                        return
                    }
                }
            }
        }

        val screen = mc.currentScreen

        if (flushoninv) {
            if (screen is GuiInventory) {
                blink()
                return
            }
        }

        if (flushoncontainer) {
            if (screen is GuiContainer) {
                blink()
                return
            }
        }

        if (flushclickgui) {
            if (screen is ClickGui) {
                blink()
                return
            }
        }

        if (flushblock) {
            if (mc.thePlayer.isUsingItem) {
                blink()
                return
            }
        }

        if (mc.gameSettings.keyBindBack.pressed) {
            if (flushback) {
                blink()
                return
            }
        }

        if (mc.thePlayer.health < mc.thePlayer.maxHealth) {
            if (mc.thePlayer.hurtTime != 0) {
                if (flushdamage) {
                    blink()
                    return
                }
            }
        }

        // Passed time
        if (!resetTimer.hasTimePassed(recoildelay))
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
    fun onUpdate(event: UpdateEvent) {
        if (ticksFlag > 0) {
            ticksFlag--
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        // Clear packets on disconnect only
        if (event.worldClient == null)
            blink(false)
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        mc.thePlayer ?: return

        if (!resetTimer.hasTimePassed(0))
            return

        handlePackets()
        ignoreWholeTick = false
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