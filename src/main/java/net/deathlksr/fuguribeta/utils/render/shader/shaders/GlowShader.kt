/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.utils.render.shader.shaders

import net.deathlksr.fuguribeta.utils.render.shader.FramebufferShader
import org.lwjgl.opengl.GL20.*

object GlowShader : FramebufferShader("glow.frag") {
    
    override fun setupUniforms() {
        setupUniform("texture")
        setupUniform("texelSize")
        setupUniform("color")
        setupUniform("fade")
        setupUniform("radius")
        setupUniform("targetAlpha")
    }

    override fun updateUniforms() {
        glUniform1i(getUniform("texture"), 0)
        glUniform2f(getUniform("texelSize"),
            1f / mc.displayWidth * renderScale,
            1f / mc.displayHeight * renderScale
        )
        glUniform3f(getUniform("color"), red, green, blue)
        glUniform1f(getUniform("fade"), fade.toFloat())
        glUniform1i(getUniform("radius"), radius)
        glUniform1f(getUniform("targetAlpha"), targetAlpha)
    }


}
