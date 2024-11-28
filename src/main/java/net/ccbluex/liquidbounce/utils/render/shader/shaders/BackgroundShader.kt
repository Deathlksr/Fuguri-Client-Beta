package net.ccbluex.liquidbounce.utils.render.shader.shaders

import net.ccbluex.liquidbounce.utils.render.RenderUtils.deltaTime
import net.ccbluex.liquidbounce.utils.render.shader.Shader
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL20.*
import java.io.File
import java.io.IOException

class BackgroundShader : Shader {
    constructor() : super("fuguri.frag")

    @Throws(IOException::class)
    constructor(fragmentShader: File) : super(fragmentShader)

    companion object {
        val BACKGROUND_SHADER = BackgroundShader()
    }

    private var time = 0f

    override fun setupUniforms() {
        setupUniform("resolution")
        setupUniform("time")
    }

    override fun updateUniforms() {
        val resolutionID = getUniform("resolution")
        if (resolutionID > -1)
            glUniform2f(resolutionID, Display.getWidth().toFloat(), Display.getHeight().toFloat())

        val timeID = getUniform("time")
        if (timeID > -1) glUniform1f(timeID, time)

        time += 0.003f * deltaTime
    }
}
