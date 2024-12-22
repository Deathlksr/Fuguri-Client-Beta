/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.EntityUtils.isLookingOnEntities
import net.deathlksr.fuguribeta.utils.render.RenderUtils.disableGlCap
import net.deathlksr.fuguribeta.utils.render.RenderUtils.enableGlCap
import net.deathlksr.fuguribeta.utils.render.RenderUtils.resetCaps
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.FontValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.entity.item.EntityTNTPrimed
import org.lwjgl.opengl.GL11.*
import kotlin.math.pow

object TNTTimer : Module("TNTTimer", Category.VISUAL, spacedName = "TNT Timer", hideModule = false) {

    private val scale by FloatValue("Scale", 3F, 1F..4F)
    private val font by FontValue("Font", Fonts.font40)
    private val fontShadow by BoolValue("Shadow", true)

    private val colorRed by IntegerValue("R", 255, 0..255)
    private val colorGreen by IntegerValue("G", 255, 0..255)
    private val colorBlue by IntegerValue("B", 255, 0..255)

    private val maxRenderDistance by object : IntegerValue("MaxRenderDistance", 100, 1..200) {
        override fun onUpdate(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
    }

    private val onLook by BoolValue("OnLook", false)
    private val maxAngleDifference by FloatValue("MaxAngleDifference", 5.0f, 5.0f..90f) { onLook }

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        for (entity in world.loadedEntityList.filterNotNull()) {
            if (entity is EntityTNTPrimed && player.getDistanceSqToEntity(entity) <= maxRenderDistanceSq) {
                val explosionTime = entity.fuse / 5

                if (explosionTime > 0 && (isLookingOnEntities(entity, maxAngleDifference.toDouble()) || !onLook)) {
                    renderTNTTimer(entity, explosionTime)
                }
            }
        }
    }

    private fun renderTNTTimer(tnt: EntityTNTPrimed, timeRemaining: Int) {
        val thePlayer = mc.thePlayer ?: return
        val renderManager = mc.renderManager

        glPushAttrib(GL_ENABLE_BIT)
        glPushMatrix()

        // Translate to TNT position
        glTranslated(
            tnt.lastTickPosX + (tnt.posX - tnt.lastTickPosX) - renderManager.renderPosX,
            tnt.lastTickPosY + (tnt.posY - tnt.lastTickPosY) - renderManager.renderPosY + 1.5,
            tnt.lastTickPosZ + (tnt.posZ - tnt.lastTickPosZ) - renderManager.renderPosZ
        )

        glRotatef(-renderManager.playerViewY, 0F, 1F, 0F)
        glRotatef(renderManager.playerViewX, 1F, 0F, 0F)

        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)

        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        val color = ((colorRed and 0xFF) shl 16) or ((colorGreen and 0xFF) shl 8) or (colorBlue and 0xFF)

        val text = "TNT Explodes in: $timeRemaining"

        val fontRenderer = font

        // Scale
        val scale = (thePlayer.getDistanceToEntity(tnt) / 4F).coerceAtLeast(1F) / 150F * scale
        glScalef(-scale, -scale, scale)

        // Draw text
        val width = fontRenderer.getStringWidth(text) * 0.5f
        fontRenderer.drawString(
            text, 1F + -width, if (fontRenderer == Fonts.minecraftFont) 1F else 1.5F, color, fontShadow
        )

        resetCaps()
        glPopMatrix()
        glPopAttrib()
    }

}