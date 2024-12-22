package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.utils.render.Particle3D
import net.deathlksr.fuguribeta.value.IntegerValue
import java.awt.Color
import javax.vecmath.Vector3f

object WalkPaticle : Module("WalkParticle", Category.VISUAL, hideModule = false, canBeEnabled = false) {

    private val lifetime = IntegerValue("LifeTime", 2000, 1000..5000)
    private val particleAmount = IntegerValue("Amount", 2, 1..10)

    private val particles = mutableListOf<Particle3D>()

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val playerPos = Vector3f(mc.thePlayer.posX.toFloat(), mc.thePlayer.posY.toFloat(), mc.thePlayer.posZ.toFloat())
        repeat(particleAmount.get()) {
            //particles.add(Particle3D(playerPos))
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for (particle in particles) {
            val color = Color(1f, 1f, 1f, 1f)
                //particle.render(color)
        }
        //particles.removeIf { (System.currentTimeMillis() - it.spawnTime) / lifetime.get() >= 1.0 }
    }
}