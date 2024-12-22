package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.render.ColorUtils.rainbow
import net.deathlksr.fuguribeta.utils.render.RenderUtils.glColor
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*

object Breadcrumbs : Module("Breadcrumbs", Category.VISUAL, hideModule = false) {
    val colorRainbow by BoolValue("Rainbow", false)
    val colorRed by IntegerValue("R", 255, 0..255) { !colorRainbow }
    val colorGreen by IntegerValue("G", 179, 0..255) { !colorRainbow }
    val colorBlue by IntegerValue("B", 72, 0..255) { !colorRainbow }

    private val positions = LinkedList<DoubleArray>()

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val color = if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)

        synchronized(positions) {
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

            for (pos in positions)
                glVertex3d(pos[0] - renderPosX, pos[1] - renderPosY, pos[2] - renderPosZ)

            glColor4d(1.0, 1.0, 1.0, 1.0)
            glEnd()
            glEnable(GL_DEPTH_TEST)
            glDisable(GL_LINE_SMOOTH)
            glDisable(GL_BLEND)
            glEnable(GL_TEXTURE_2D)
            glPopMatrix()
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        synchronized(positions) {
            positions += doubleArrayOf(mc.thePlayer.posX, mc.thePlayer.entityBoundingBox.minY, mc.thePlayer.posZ)
        }
    }

    override fun onEnable() {
        val thePlayer = mc.thePlayer ?: return

        synchronized(positions) {
            positions += doubleArrayOf(thePlayer.posX, thePlayer.posY + thePlayer.eyeHeight * 0.5f, thePlayer.posZ)

            positions += doubleArrayOf(thePlayer.posX, thePlayer.posY, thePlayer.posZ)
        }
    }

    override fun onDisable() {
        synchronized(positions) { positions.clear() }
    }
}