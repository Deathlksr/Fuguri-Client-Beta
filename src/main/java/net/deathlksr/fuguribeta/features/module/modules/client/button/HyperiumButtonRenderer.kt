/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.client.button

import net.deathlksr.fuguribeta.features.module.modules.client.HUDModule
import net.deathlksr.fuguribeta.features.module.modules.client.HUDModule.buttonShadowValue
import net.deathlksr.fuguribeta.features.module.modules.client.HUDModule.uiEffectValue
import net.deathlksr.fuguribeta.utils.UIEffectRenderer.drawShadowWithCustomAlpha
import net.deathlksr.fuguribeta.utils.render.RenderUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.renderer.GlStateManager
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class HyperiumButtonRenderer(button: GuiButton) : AbstractButtonRenderer(button) {
    override fun render(mouseX: Int, mouseY: Int, mc: Minecraft) {

        val hud = HUDModule

        var prevDeltaTime = 0
        if (prevDeltaTime == 0) prevDeltaTime = System.currentTimeMillis().toInt()
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
        val hovered = button.hovered
        val hoverInc = (System.currentTimeMillis().toInt() - prevDeltaTime) / 2
        var fading = 100
        fading = if (hovered) min(100, fading + hoverInc) else max(0, hoverInc - hoverInc)
        RenderUtils.drawRect(button.xPosition.toFloat(), button.yPosition.toFloat(), button.xPosition.toFloat() + button.width.toFloat(), button.yPosition.toFloat() + button.height.toFloat(), Color(0, 0, 0, (100 - (fading / 2))).rgb)
        if (hud.handleEvents() && uiEffectValue && buttonShadowValue) { drawShadowWithCustomAlpha(button.xPosition.toFloat(), button.yPosition.toFloat(), button.width.toFloat(), button.height.toFloat(), 240f) }
    }
}