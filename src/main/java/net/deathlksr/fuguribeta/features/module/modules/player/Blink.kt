package net.deathlksr.fuguribeta.features.module.modules.player

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.modules.visual.Breadcrumbs
import net.deathlksr.fuguribeta.utils.BlinkUtils
import net.deathlksr.fuguribeta.utils.render.ColorUtils.rainbow
import net.deathlksr.fuguribeta.utils.render.RenderUtils.glColor
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object Blink : Module("Blink", Category.PLAYER, gameDetecting = false, hideModule = false) {

	private val mode by ListValue("Mode", arrayOf("Sent", "Received", "Both"), "Sent")

    private val pulse by BoolValue("Pulse", false)
		private val pulseDelay by IntegerValue("PulseDelay", 1000, 500..5000) { pulse }

    private val fakePlayerMenu by BoolValue("FakePlayer", true)

    val pulseTimer = MSTimer()

    override fun onEnable() {
        pulseTimer.reset()

        if (fakePlayerMenu)
            BlinkUtils.addFakePlayer()
    }

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        BlinkUtils.unblink()
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (mc.thePlayer == null || mc.thePlayer.isDead)
            return

        when (mode.lowercase()) {
            "sent" -> {
                BlinkUtils.blink(packet, event, sent = true, receive = false)
            }
            "received" -> {
                BlinkUtils.blink(packet, event, sent = false, receive = true)
            }
            "both" -> {
                BlinkUtils.blink(packet, event)
            }
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState == EventState.POST) {
            val thePlayer = mc.thePlayer ?: return

            if (thePlayer.isDead || mc.thePlayer.ticksExisted <= 10) {
                BlinkUtils.unblink()
            }

            when (mode.lowercase()) {
                "sent" -> {
                    BlinkUtils.syncSent()
                }

                "received" -> {
                    BlinkUtils.syncReceived()
                }
            }

            if (pulse && pulseTimer.hasTimePassed(pulseDelay)) {
                BlinkUtils.unblink()
                if (fakePlayerMenu) {
                    BlinkUtils.addFakePlayer()
                }
                pulseTimer.reset()
            }
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val color =
            if (Breadcrumbs.colorRainbow) rainbow()
            else Color(Breadcrumbs.colorRed, Breadcrumbs.colorGreen, Breadcrumbs.colorBlue)

        synchronized(BlinkUtils.positions) {
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

            for (pos in BlinkUtils.positions)
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

    override val tag
        get() = (BlinkUtils.packets.size + BlinkUtils.packetsReceived.size).toString()

    fun blinkingSend() = handleEvents() && (mode == "Sent" || mode == "Both")
    fun blinkingReceive() = handleEvents() && (mode == "Received" || mode == "Both")
}
