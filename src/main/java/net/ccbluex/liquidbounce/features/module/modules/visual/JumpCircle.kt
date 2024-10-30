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
import net.ccbluex.liquidbounce.utils.render.ColorUtils.fade
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.renderer.entity.Render
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

object JumpCircle: Module("JumpCircle", Category.VISUAL, gameDetecting = false, hideModule = false) {

    private val typeValue = ListValue("Mode", arrayOf("NewCircle"), "NewCircle")
    val disappearTime = IntegerValue("Time", 1000, 1000..6000)
    val radius = FloatValue("Radius", 3f, 1f..6f)
    private val colorModeValue = ListValue("Color", arrayOf("Custom", "Fade", "Rainbow"), "Custom")
    private val colorRedValue: IntegerValue = IntegerValue("Red", 255, 0..255)
    private val colorGreenValue = IntegerValue("Green", 255, 0..255)
    private val colorBlueValue = IntegerValue("Blue", 255, 0..255)
    private val colorstep = IntegerValue("ColorStep", 10, 0..10)

    private var jump=false
    private val points = mutableMapOf<Int, MutableList<Render<*>>>()
    private val circles = mutableListOf<Circle>()
    var red = colorRedValue.get()
    var green = colorGreenValue.get()
    var blue = colorBlueValue.get()

    @EventTarget
    fun onRender3D(e: Render3DEvent?) {
        when (typeValue.get().lowercase(Locale.getDefault())) {
            "newcircle" -> {
                circles.removeIf { System.currentTimeMillis() > it.time + disappearTime.get() }

                GL11.glPushMatrix()

                GL11.glEnable(GL11.GL_BLEND)
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
                GL11.glDisable(GL11.GL_CULL_FACE)
                GL11.glDisable(GL11.GL_TEXTURE_2D)
                GL11.glDisable(GL11.GL_DEPTH_TEST)
                GL11.glDepthMask(false)
                GL11.glDisable(GL11.GL_ALPHA_TEST)
                GL11.glShadeModel(GL11.GL_SMOOTH)

                circles.forEach { it.draw() }

                GL11.glDisable(GL11.GL_BLEND)
                GL11.glEnable(GL11.GL_CULL_FACE)
                GL11.glEnable(GL11.GL_TEXTURE_2D)
                GL11.glEnable(GL11.GL_DEPTH_TEST)
                GL11.glDepthMask(true)
                GL11.glEnable(GL11.GL_ALPHA_TEST)
                GL11.glShadeModel(GL11.GL_FLAT)

                GL11.glPopMatrix()
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
        private val jumpModule = FuguriBeta.moduleManager.getModule(JumpCircle::class.java) as JumpCircle
        private var colorModeValue = jumpModule.colorModeValue.get()
        private var colorRedValue = jumpModule.colorRedValue.get()
        private var colorGreenValue = jumpModule.colorGreenValue.get()
        private var colorBlueValue = jumpModule.colorBlueValue.get()

        fun draw() {
            if(jumpModule == null) {
                return
            }

            val dif = (System.currentTimeMillis() - time)
            val c = 255 - (dif / jumpModule.disappearTime.get().toFloat()) * 255

            GL11.glPushMatrix()

            GL11.glTranslated(
                x - mc.renderManager.viewerPosX,
                y - mc.renderManager.viewerPosY,
                z - mc.renderManager.viewerPosZ
            )

            GL11.glBegin(GL11.GL_TRIANGLE_STRIP)
            for (i in 0..360) {
                val color = getColor(entity, 0)

                val x = (dif * jumpModule.radius.get() * 0.001 * sin(i.toDouble().toRadians()))
                val z = (dif * jumpModule.radius.get() * 0.001 * cos(i.toDouble().toRadians()))

                RenderUtils.glColor(color.red, color.green, color.blue, 0)
                GL11.glVertex3d(x / 2, 0.0, z / 2)

                RenderUtils.glColor(color.red, color.green, color.blue, c.toInt())
                GL11.glVertex3d(x, 0.0, z)
            }
            GL11.glEnd()

            GL11.glPopMatrix()
        }

        private val penis2 = ColorUtils.rainbow(40000L * 10000000 * colorstep.get(), 1.0F)

        fun getColor(ent: Entity?, index: Int): Color {
            return when (colorModeValue) {
                "Custom" -> Color(colorRedValue, colorGreenValue, colorBlueValue)
                "Fade" -> fade(Color(colorRedValue, colorGreenValue, colorBlueValue), index, 100)
                "Rainbow" -> Color(penis2.red, penis2.green, penis2.blue)
                else -> fade(Color(colorRedValue, colorGreenValue, colorBlueValue), index, 100)
            }
        }
    }
}
