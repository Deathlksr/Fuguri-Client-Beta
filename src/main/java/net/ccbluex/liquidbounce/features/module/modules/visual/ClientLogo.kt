package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.IntegerValue
import net.minecraft.util.ResourceLocation

object ClientLogo : Module("ClientLogo", Category.VISUAL, hideModule = false) {

    private val posx by IntegerValue("PosX", 0, -200..1000)
    private val posy by IntegerValue("PosY", 0, -200..1000)
    private val scale by IntegerValue("Scale", 0, 50..400)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        val rl = ResourceLocation("fuguribeta/textures/fuguri.png")
        RenderUtils.drawImage(
            rl,
            posx,
            posy,
            scale,
            scale
        )
    }
}