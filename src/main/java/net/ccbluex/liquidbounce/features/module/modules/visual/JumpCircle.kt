package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.FuguriBeta
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.event.WorldEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.extensions.toRadians
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.entity.Render
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

object JumpCircle: Module("JumpCircle", Category.VISUAL, gameDetecting = false, hideModule = false) {

    private val typeValue = ListValue("Mode", arrayOf("NewCircle"), "NewCircle")
    val disappearTime = IntegerValue("Time", 1000, 1000..6000)
    val radius = FloatValue("Radius", 3f, 1f..6f)
    private val colorRedValue = FloatValue("Red", 1f, 0f..1f)
    private val colorGreenValue = FloatValue("Green", 1f, 0f..1f)
    private val colorBlueValue = FloatValue("Blue", 1f, 0f..1f)
    private val colorRedValue2 = FloatValue("Red2", 1f, 0f..1f)
    private val colorGreenValue2 = FloatValue("Green2", 1f, 0f..1f)
    private val colorBlueValue2 = FloatValue("Blue2", 1f, 0f..1f)
    private val stepcolor = IntegerValue("StepColor", 10, 0..10)
    private val stephui = IntegerValue("StepCircle", 10, 10..180)

    private var jump=false
    private val points = mutableMapOf<Int, MutableList<Render<*>>>()
    private val circles = mutableListOf<Circle>()


    @EventTarget
    fun onRender3D(e: Render3DEvent?) {
        when (typeValue.get().lowercase(Locale.getDefault())) {
            "newcircle" -> {
                circles.removeIf { System.currentTimeMillis() > it.time + disappearTime.get() }

                glPushMatrix()

                glEnable(GL_BLEND)
                glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
                glDisable(GL_CULL_FACE)
                glDisable(GL_TEXTURE_2D)
                glDisable(GL_DEPTH_TEST)
                glDepthMask(false)
                glDisable(GL_ALPHA_TEST)
                glShadeModel(GL_SMOOTH)

                circles.forEach { it.draw() }

                glDisable(GL_BLEND)
                glEnable(GL_CULL_FACE)
                glEnable(GL_TEXTURE_2D)
                glEnable(GL_DEPTH_TEST)
                glDepthMask(true)
                glEnable(GL_ALPHA_TEST)
                glShadeModel(GL_FLAT)

                glPopMatrix()
            }
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!mc.thePlayer.onGround && !jump) {
            jump = true
        }
        if (mc.thePlayer.onGround && jump) {
            updatePoints(mc.thePlayer)
            jump = false
        }
    }

    private fun updatePoints(entity: EntityLivingBase) {
        when (typeValue.get().lowercase(Locale.getDefault())) {
            "newcircle" -> {
                circles.add(Circle(System.currentTimeMillis(), mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ))
            }
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        points.clear()
    }

    override fun onDisable() {
        points.clear()
    }

    class Circle(val time: Long, val x: Double, val y: Double, val z: Double){
        var entity: EntityLivingBase = mc.thePlayer

        fun draw() {
            val dif = (System.currentTimeMillis() - time)
            val c = 255 - (dif / disappearTime.get().toFloat()) * 255

            glPushMatrix()

            glTranslated(
                x - mc.renderManager.viewerPosX,
                y - mc.renderManager.viewerPosY,
                z - mc.renderManager.viewerPosZ
            )

            glBegin(GL_QUAD_STRIP)

            for (i in 0..360 step stephui.get()) {

                val red = colorRedValue.get()
                val green = colorGreenValue.get()
                val blue = colorBlueValue.get()
                val red2 = colorRedValue2.get()
                val green2 = colorGreenValue2.get()
                val blue2 = colorBlueValue2.get()

                val color1 = Color(red, green, blue)
                val color2 = Color(red2, green2, blue2)
                val penis25 = (cos(i * Math.PI / 180 * stepcolor.get()) + 1) / 2
                val gradientcolor = ColorUtils.mixColorse(color1, color2, penis25.toFloat())

                val x = (dif * radius.get() * 0.001 * sin(i.toDouble().toRadians()))
                val z = (dif * radius.get() * 0.001 * cos(i.toDouble().toRadians()))

                glColor4f(gradientcolor.red / 255F, gradientcolor.green / 255F, gradientcolor.blue / 255F, 0f)
                glVertex3d(x / 2, 0.0, z / 2)

                glColor4f(gradientcolor.red / 255F, gradientcolor.green / 255F, gradientcolor.blue / 255F, c.toInt() / 255F)
                glVertex3d(x, 0.0, z)
            }
            glEnd()

            glPopMatrix()
        }
    }
}
