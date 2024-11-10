package net.ccbluex.liquidbounce.utils.render

import net.ccbluex.liquidbounce.utils.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.timing.MSTimer
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import javax.vecmath.Vector3d
import kotlin.random.Random

class Particle3D(private val position: Vector3d, private val motion: Vector3d = randomMotion()) {

    private val spawnTime: Long = System.currentTimeMillis()
    private val timer: MSTimer = MSTimer()
    private val scale: Float = if (motion == randomMotion()) Random.nextInt(5, 7).toFloat() else Random.nextInt(2, 3).toFloat()

    constructor(position: Vector3d) : this(position, randomMotion())

    private fun drawPoint(x: Double, y: Double, z: Double, scale: Float, color: Color) {
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_POINT_SMOOTH)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        ColorUtils.setColor(color.rgb)

        glPointSize(scale)

        glBegin(GL_POINTS)
        glVertex3d(
            x - mc.renderManager.viewerPosX,
            y - mc.renderManager.viewerPosY,
            z - mc.renderManager.viewerPosZ
        )
        glEnd()

        glPointSize(1.0f)

        ColorUtils.clearColor()
        glDisable(GL_POINT_SMOOTH)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
    }

    fun render(color: Color) {
        drawPoint(position.x, position.y, position.z, scale + 5, color)
        drawPoint(position.x, position.y, position.z, scale, color)

        repeat(timer.elapsedTime().toInt()) {
            motion.x *= 0.999f
            motion.y *= 0.999f
            motion.z *= 0.999f
        }

        position.x += motion.x
        position.y += motion.y
        position.z += motion.z

        timer.reset()
    }

    fun getSpawnTime(): Long = spawnTime

    companion object {
        private fun randomMotion(): Vector3d {
            return Vector3d(
                Random.nextDouble() * 0.005 - 0.0025,
                Random.nextDouble() * 0.0025,
                Random.nextDouble() * 0.005 - 0.0025
            )
        }
    }
}
