package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.ui.client.clickgui.ClickGui
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.Rotation
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.glColor
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.GuiChat
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

    private val randomdelay by BoolValue("RandomDelay", false)
    private val delay by IntegerValue("Delay", 500, 0..1000) { !randomdelay }
    private val mindelay = IntegerValue("MinDelay", 500, 0..1000) { randomdelay }
    private val maxdelay = IntegerValue("MaxDelay", 520, 0..1000) { randomdelay }
    private val minrecoildelay by IntegerValue("MinRecoilDelay", 0, 0..1000)
    private val maxrecoildelay by IntegerValue("ManRecoilDelay", 0, 0..1000)
    private val ticksexxisted by BoolValue("TicksExisted", true)
    private val minticksalive by IntegerValue("MaxTicksExisted", 15, 0..60) { ticksexxisted }
    private val flushflag by BoolValue("FlushFlag", true)
    private val tickflag by IntegerValue("TicksFlag", 5, 0..60) { flushflag }
    private val flushoninv by BoolValue("FlushInventory", true)
    private val flushchat by BoolValue("FlushChat", true)
    private val flushoncontainer by BoolValue("FlushContainer", false)
    private val flushonattack by BoolValue("FlushAttack", true)
    private val flushusingitem by BoolValue("FlushUsingItem", true)
    private val flushdamage by BoolValue("FlushGotDamaged", false)
    private val flushvelocity by BoolValue("FlushVelocity", false)
    private val flushblockplacement by BoolValue("FlushBlockPlacement", false)
    private val flushback by BoolValue("FlushBack", false)
    private val flushclickgui by BoolValue("FlushClickGui", false)
    private val flushsprintreset by BoolValue("FlushSprintReset", false)
    private val flushstand by BoolValue("FlushStandingStill", false)
    private val packetQueue = LinkedHashMap<Packet<*>, Long>()
    private val positions = LinkedHashMap<Vec3, Long>()
    private val resetTimer = MSTimer()
    private var ignoreWholeTick = false
    private val line by BoolValue("Line", true, subjective = true)
    private val thirdperson by BoolValue("OnlyThirdPerson", true) { line }
    private val red by FloatValue("Red", 1.0F, 0.0F..1.0F) { line }
    private val green by FloatValue("Green", 1.0F, 0.0F..1.0F) { line }
    private val blue by FloatValue("Blue", 1.0F, 0.0F..1.0F) { line }

    private var ticksFlag = 0
    private var randomdelays = 0
    private var recoil = 0

    override fun onDisable() {
        if (mc.thePlayer == null) {
            return
        }
        blink()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val serverData = mc.currentServerData

        if (!handleEvents())
            return

        if (mc.thePlayer == null || mc.thePlayer.isDead)
            return

        if (MovementUtils.isMoving && !mc.thePlayer.isSprinting && flushsprintreset) {
            blink()
            return
        }

        if (mc.isIntegratedServerRunning || serverData == null) {
            return
        }

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

            is C08PacketPlayerBlockPlacement -> {
                if (flushblockplacement) {
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

        if (flushchat) {
            if (screen is GuiChat) {
                blink()
                return
            }
        }

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

        if (flushusingitem) {
            if (mc.thePlayer.isUsingItem) {
                blink()
                return
            }
        }

        if (flushback) {
            if (mc.gameSettings.keyBindBack.pressed) {
                blink()
                return
            }
        }
        if (flushdamage) {
            if (mc.thePlayer.health < mc.thePlayer.maxHealth) {
                if (mc.thePlayer.hurtTime != 0) {
                    blink()
                    return
                }
            }
        }
        if (flushstand) {
            if (!MovementUtils.isMoving) {
                blink()
                return
            }
        }

        // Passed time
        if (!resetTimer.hasTimePassed(recoil))
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
        if (event.worldClient == null)
            blink(false)
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        mc.thePlayer ?: return

        if (!randomdelay)
            randomdelays = delay

        if (!resetTimer.hasTimePassed(recoil))
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
        if (randomdelay) randomdelays = RandomUtils.nextInt(mindelay.get(), maxdelay.get())
        recoil = RandomUtils.nextInt(minrecoildelay, maxrecoildelay)
    }

    private fun handlePackets() {
        synchronized(packetQueue) {
            packetQueue.entries.removeAll { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - randomdelays ) {
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