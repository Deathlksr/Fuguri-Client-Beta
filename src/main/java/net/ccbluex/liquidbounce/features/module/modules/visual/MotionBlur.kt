package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.util.ResourceLocation

object MotionBlur: Module("MotionBlur", Category.VISUAL, hideModule = false) {

    private var motionBlurs = false
    private val blurAmount = IntegerValue("BlurAmount", 2, 1..10)

    override fun onEnable() {
        motionBlurs = true
    }

    override fun onDisable() {
        motionBlurs = false
        mc.entityRenderer.stopUseShader()
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        try {

            if (mc.thePlayer != null) {
                if (motionBlurs) {
                    if (mc.entityRenderer.shaderGroup == null) mc.entityRenderer.loadShader(
                        ResourceLocation(
                            "minecraft",
                            "shaders/post/motion_blur.json"
                        )
                    )
                    val uniform = 1f - (blurAmount.get() / 10f).coerceAtMost(0.9f)
                    if (mc.entityRenderer.shaderGroup != null) {
                        mc.entityRenderer.shaderGroup.listShaders[0].shaderManager.getShaderUniform("Phosphor")
                            .set(uniform, 0f, 0f)
                    }
                } else {
                    if (mc.entityRenderer.isShaderActive) mc.entityRenderer.stopUseShader()
                }
            }

        } catch (a: Exception) {
            a.printStackTrace()
        }
    }
}