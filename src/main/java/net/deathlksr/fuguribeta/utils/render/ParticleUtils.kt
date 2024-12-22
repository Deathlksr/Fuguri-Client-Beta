/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.utils.render

import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import net.vitox.ParticleGenerator

@SideOnly(Side.CLIENT)
object ParticleUtils {
    private val particleGenerator = ParticleGenerator(150)

    fun drawParticles(mouseX: Int, mouseY: Int) = particleGenerator.draw(mouseX, mouseY)
}