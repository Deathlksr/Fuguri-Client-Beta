package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.client.gui.GuiMainMenu
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation

object MotionBlur : Module("MotionBlur", Category.VISUAL, hideModule = false) {

    private val blurAmount = IntegerValue("BlurAmount", 2, 1..10)

    override fun onEnable() {
        applyShader()
    }

    override fun onDisable() {
        mc.entityRenderer.stopUseShader()
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        try {
            if (mc.entityRenderer.shaderGroup == null) applyShader()

            val uniform = 1f - (blurAmount.get() / 10f).coerceAtMost(0.9f)

            mc.entityRenderer.shaderGroup?.listShaders?.get(0)?.shaderManager?.getShaderUniform("Phosphor")?.set(uniform, 0f, 0f)
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }

    private fun applyShader() {
        try {
            val currentScreen: GuiScreen? = mc.currentScreen
            if (currentScreen == null || currentScreen is GuiMainMenu || mc.theWorld != null) {
                mc.entityRenderer.loadShader(
                    ResourceLocation(
                        "minecraft",
                        "shaders/post/motion_blur.json"
                    )
                )
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
    }
}
