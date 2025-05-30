/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render2DEvent
import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.modules.client.AntiBot.isBot
import net.deathlksr.fuguribeta.ui.font.GameFontRenderer.Companion.getColorIndex
import net.deathlksr.fuguribeta.utils.ClientUtils.LOGGER
import net.deathlksr.fuguribeta.utils.EntityUtils.isLookingOnEntities
import net.deathlksr.fuguribeta.utils.EntityUtils.isSelected
import net.deathlksr.fuguribeta.utils.RotationUtils
import net.deathlksr.fuguribeta.utils.extensions.hitBox
import net.deathlksr.fuguribeta.utils.render.ColorUtils
import net.deathlksr.fuguribeta.utils.render.ColorUtils.rainbow
import net.deathlksr.fuguribeta.utils.render.RenderUtils.draw2D
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawEntityBox
import net.deathlksr.fuguribeta.utils.render.WorldToScreen
import net.deathlksr.fuguribeta.utils.render.shader.shaders.GlowShader
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.client.renderer.GlStateManager.enableTexture2D
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import org.lwjgl.util.vector.Vector3f
import java.awt.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ESP : Module("ESP", Category.VISUAL, hideModule = false) {

    val mode by ListValue("Mode",
        arrayOf("Box", "OtherBox", "WireFrame", "2D", "Real2D", "Outline", "Glow"), "Box")

        val outlineWidth by FloatValue("Outline-Width", 3f, 0.5f..5f) { mode == "Outline" }

        val wireframeWidth by FloatValue("WireFrame-Width", 2f, 0.5f..5f) { mode == "WireFrame" }

        private val glowRenderScale by FloatValue("Glow-Renderscale", 1f, 0.5f..2f) { mode == "Glow" }
        private val glowRadius by IntegerValue("Glow-Radius", 4, 1..5) { mode == "Glow" }
        private val glowFade by IntegerValue("Glow-Fade", 10, 0..30) { mode == "Glow" }
        private val glowTargetAlpha by FloatValue("Glow-Target-Alpha", 0f, 0f..1f) { mode == "Glow" }

    private val colorRainbow by BoolValue("Rainbow", false)
        private val colorRed by IntegerValue("R", 255, 0..255) { !colorRainbow }
        private val colorGreen by IntegerValue("G", 255, 0..255) { !colorRainbow }
        private val colorBlue by IntegerValue("B", 255, 0..255) { !colorRainbow }

    private val maxRenderDistance by object : IntegerValue("MaxRenderDistance", 100, 1..200) {
        override fun onUpdate(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
    }

    private val onLook by BoolValue("OnLook", false)
    private val maxAngleDifference by FloatValue("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by BoolValue("ThruBlocks", true)

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    private val colorTeam by BoolValue("Team", false)
    private val bot by BoolValue("Bots", true)

    var renderNameTags = true

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val mvMatrix = WorldToScreen.getMatrix(GL_MODELVIEW_MATRIX)
        val projectionMatrix = WorldToScreen.getMatrix(GL_PROJECTION_MATRIX)
        val real2d = mode == "Real2D"

        if (real2d) {
            glPushAttrib(GL_ENABLE_BIT)
            glEnable(GL_BLEND)
            glDisable(GL_TEXTURE_2D)
            glDisable(GL_DEPTH_TEST)
            glMatrixMode(GL_PROJECTION)
            glPushMatrix()
            glLoadIdentity()
            glOrtho(0.0, mc.displayWidth.toDouble(), mc.displayHeight.toDouble(), 0.0, -1.0, 1.0)
            glMatrixMode(GL_MODELVIEW)
            glPushMatrix()
            glLoadIdentity()
            glDisable(GL_DEPTH_TEST)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            enableTexture2D()
            glDepthMask(true)
            glLineWidth(1f)
        }

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !bot && isBot(entity)) continue
            if (isSelected(entity, false)) {

                val distanceSquared = mc.thePlayer.getDistanceSqToEntity(entity)

                if (onLook && !isLookingOnEntities(entity, maxAngleDifference.toDouble()))
                    continue

                if (!thruBlocks && !RotationUtils.isVisible(Vec3(entity.posX, entity.posY, entity.posZ)))
                    continue

                if (distanceSquared <= maxRenderDistanceSq) {
                    val color = getColor(entity)

                    when (mode) {
                        "Box", "OtherBox" -> drawEntityBox(entity, color, mode != "OtherBox")
                        "2D" -> {
                            val renderManager = mc.renderManager
                            val timer = mc.timer
                            val posX =
                                entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX
                            val posY =
                                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY
                            val posZ =
                                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
                            draw2D(entity, posX, posY, posZ, color.rgb, Color.BLACK.rgb)
                        }

                        "Real2D" -> {
                            val renderManager = mc.renderManager
                            val timer = mc.timer
                            val bb = entity.hitBox
                                .offset(-entity.posX, -entity.posY, -entity.posZ)
                                .offset(
                                    entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks,
                                    entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks,
                                    entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                                )
                                .offset(-renderManager.renderPosX, -renderManager.renderPosY, -renderManager.renderPosZ)
                            val boxVertices = arrayOf(
                                doubleArrayOf(bb.minX, bb.minY, bb.minZ),
                                doubleArrayOf(bb.minX, bb.maxY, bb.minZ),
                                doubleArrayOf(bb.maxX, bb.maxY, bb.minZ),
                                doubleArrayOf(bb.maxX, bb.minY, bb.minZ),
                                doubleArrayOf(bb.minX, bb.minY, bb.maxZ),
                                doubleArrayOf(bb.minX, bb.maxY, bb.maxZ),
                                doubleArrayOf(bb.maxX, bb.maxY, bb.maxZ),
                                doubleArrayOf(bb.maxX, bb.minY, bb.maxZ)
                            )
                            var minX = Float.MAX_VALUE
                            var minY = Float.MAX_VALUE
                            var maxX = -1f
                            var maxY = -1f
                            for (boxVertex in boxVertices) {
                                val screenPos = WorldToScreen.worldToScreen(
                                    Vector3f(
                                        boxVertex[0].toFloat(),
                                        boxVertex[1].toFloat(),
                                        boxVertex[2].toFloat()
                                    ), mvMatrix, projectionMatrix, mc.displayWidth, mc.displayHeight
                                )
                                    ?: continue
                                minX = min(screenPos.x, minX)
                                minY = min(screenPos.y, minY)
                                maxX = max(screenPos.x, maxX)
                                maxY = max(screenPos.y, maxY)
                            }
                            if (minX > 0 || minY > 0 || maxX <= mc.displayWidth || maxY <= mc.displayWidth) {
                                glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, 1f)
                                glBegin(GL_LINE_LOOP)
                                glVertex2f(minX, minY)
                                glVertex2f(minX, maxY)
                                glVertex2f(maxX, maxY)
                                glVertex2f(maxX, minY)
                                glEnd()
                            }
                        }
                    }
                }
            }
        }

        if (real2d) {
            glColor4f(1f, 1f, 1f, 1f)
            glEnable(GL_DEPTH_TEST)
            glMatrixMode(GL_PROJECTION)
            glPopMatrix()
            glMatrixMode(GL_MODELVIEW)
            glPopMatrix()
            glPopAttrib()
        }
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.theWorld == null || mode != "Glow")
            return

        GlowShader.startDraw(event.partialTicks, glowRenderScale)

        renderNameTags = false

        try {
            val entitiesGrouped = getEntitiesByColor(maxRenderDistanceSq)

            entitiesGrouped.forEach { (color, entities) ->
                GlowShader.startDraw(event.partialTicks, glowRenderScale)

                for (entity in entities) {
                    mc.renderManager.renderEntitySimple(entity, event.partialTicks)
                }

                GlowShader.stopDraw(color, glowRadius, glowFade, glowTargetAlpha)
            }
        } catch (ex: Exception) {
            LOGGER.error("An error occurred while rendering all entities for shader esp", ex)
        }

        renderNameTags = true

        GlowShader.stopDraw(getColor(), glowRadius, glowFade, glowTargetAlpha)
    }

    private fun getEntitiesByColor(maxDistanceSquared: Double): Map<Color, List<EntityLivingBase>> {
        return getEntitiesInRange(maxDistanceSquared)
            .groupBy { getColor(it) }
    }

    private fun getEntitiesInRange(maxDistanceSquared: Double): List<EntityLivingBase> {
        val player = mc.thePlayer

        return mc.theWorld.loadedEntityList.asSequence()
            .filterIsInstance<EntityLivingBase>()
            .filterNot { isBot(it) && bot }
            .filter { isSelected(it, false) }
            .filter { player.getDistanceSqToEntity(it) <= maxDistanceSquared }
            .filter { thruBlocks || RotationUtils.isVisible(Vec3(it.posX, it.posY, it.posZ)) }
            .toList()
    }

    fun getColor(entity: Entity? = null): Color {
        run {
            if (entity != null && entity is EntityLivingBase) {
                if (colorTeam) {
                    val chars = (entity.displayName ?: return@run).formattedText.toCharArray()
                    var color = Int.MAX_VALUE

                    for (i in chars.indices) {
                        if (chars[i] != '§' || i + 1 >= chars.size) continue

                        val index = getColorIndex(chars[i + 1])
                        if (index < 0 || index > 15) continue

                        color = ColorUtils.hexColors[index]
                        break
                    }

                    return Color(color)
                }
            }
        }

        return if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)
    }
    fun shouldRender(entity: EntityLivingBase): Boolean {
        return (bot || !isBot(entity))
    }

}