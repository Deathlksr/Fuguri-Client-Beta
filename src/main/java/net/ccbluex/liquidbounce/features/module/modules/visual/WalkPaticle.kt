package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.Particle3D
import net.ccbluex.liquidbounce.value.IntegerValue
import java.awt.Color
import javax.vecmath.Vector3d

object WalkPaticle : Module("WalkParticle", Category.VISUAL, hideModule = false) {

    private val lifetime = IntegerValue("LifeTime", 2000, 1000..5000)
    private val particleAmount = IntegerValue("Amount", 2, 1..10)

    private val particles = mutableListOf<Particle3D>()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val playerPos = Vector3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ)
        repeat(particleAmount.get()) {
            particles.add(Particle3D(playerPos))
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for (particle in particles) {
            val ratio = (System.currentTimeMillis() - particle.getSpawnTime()) / lifetime.get().toFloat()
            val inverseRatio = 1.0 - ratio

            if (inverseRatio < 0) continue

            val color = Color(255, 255, 255, (inverseRatio * 100).toInt())
            particle.render(color)
        }

        particles.removeIf { (System.currentTimeMillis() - it.getSpawnTime()) / lifetime.get() >= 1.0 }
    }
}