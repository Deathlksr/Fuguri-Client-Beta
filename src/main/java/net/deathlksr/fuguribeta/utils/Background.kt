/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.utils

import kotlinx.coroutines.*
import net.deathlksr.fuguribeta.FuguriBeta.CLIENT_NAME
import net.deathlksr.fuguribeta.utils.ClientUtils.LOGGER
import net.deathlksr.fuguribeta.utils.MinecraftInstance.Companion.mc
import net.deathlksr.fuguribeta.utils.render.shader.Shader
import net.deathlksr.fuguribeta.utils.render.shader.shaders.BackgroundShader
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager.color
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.ResourceLocation
import java.io.File
import java.util.concurrent.CountDownLatch
import javax.imageio.ImageIO

abstract class Background(val backgroundFile: File) {

    companion object {

        fun createBackground(backgroundFile: File): Background = runBlocking {
            CoroutineScope(Dispatchers.Default).async {
                val background = when (backgroundFile.extension) {
                    "png" -> ImageBackground(backgroundFile)
                    "frag", "glsl", "shader" -> ShaderBackground(backgroundFile)
                    else -> throw IllegalArgumentException("Invalid background file extension")
                }

                background.initBackground()
                background
            }.await()
        }

    }

    protected abstract fun initBackground()

    abstract fun drawBackground(width: Int, height: Int)

}

class ImageBackground(backgroundFile: File) : Background(backgroundFile) {

    private val resourceLocation = ResourceLocation("${CLIENT_NAME.lowercase()}/background/background.png")

    override fun initBackground() {
        val image = ImageIO.read(backgroundFile.inputStream())
        mc.textureManager.loadTexture(resourceLocation, DynamicTexture(image))
    }

    override fun drawBackground(width: Int, height: Int) {
        mc.textureManager.bindTexture(resourceLocation)
        color(1f, 1f, 1f, 1f)
        Gui.drawScaledCustomSizeModalRect(0, 0, 0f, 0f, width, height, width, height, width.toFloat(), height.toFloat())
    }


}

class ShaderBackground(backgroundFile: File) : Background(backgroundFile) {

    private var shaderInitialized = false
    private lateinit var shader: Shader
    private val initializationLatch = CountDownLatch(1)

    override fun initBackground() {
        GlobalScope.launch {
            runCatching {
                shader = BackgroundShader(backgroundFile)
            }.onFailure {
                LOGGER.error("Failed to load background.", it)
            }.onSuccess {
                initializationLatch.countDown()
                shaderInitialized = true
                LOGGER.info("Successfully loaded background.")
            }
        }
    }

    override fun drawBackground(width: Int, height: Int) {
        if (!shaderInitialized) {
            runCatching {
                initializationLatch.await()
            }.onFailure {
                LOGGER.error(it.message)
                return
            }
        }

        if (shaderInitialized) {
            shader.startShader()

            val instance = Tessellator.getInstance()
            val worldRenderer = instance.worldRenderer
            worldRenderer.begin(7, DefaultVertexFormats.POSITION)
            worldRenderer.pos(0.0, height.toDouble(), 0.0).endVertex()
            worldRenderer.pos(width.toDouble(), height.toDouble(), 0.0).endVertex()
            worldRenderer.pos(width.toDouble(), 0.0, 0.0).endVertex()
            worldRenderer.pos(0.0, 0.0, 0.0).endVertex()
            instance.draw()

            shader.stopShader()
        }
    }
}
