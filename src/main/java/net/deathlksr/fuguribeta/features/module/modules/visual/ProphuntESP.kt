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
import net.deathlksr.fuguribeta.utils.ClientUtils.LOGGER
import net.deathlksr.fuguribeta.utils.EntityUtils.isLookingOnEntities
import net.deathlksr.fuguribeta.utils.RotationUtils
import net.deathlksr.fuguribeta.utils.render.ColorUtils.rainbow
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawBlockBox
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawEntityBox
import net.deathlksr.fuguribeta.utils.render.shader.shaders.GlowShader
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.entity.item.EntityFallingBlock
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.math.pow

object ProphuntESP : Module("ProphuntESP", Category.VISUAL, gameDetecting = false) {
    val blocks = mutableMapOf<BlockPos, Long>()

    private val mode by ListValue("Mode", arrayOf("Box", "OtherBox", "Glow"), "OtherBox")
        private val glowRenderScale by FloatValue("Glow-Renderscale", 1f, 0.5f..2f) { mode == "Glow" }
        private val glowRadius by IntegerValue("Glow-Radius", 4, 1..5) { mode == "Glow" }
        private val glowFade by IntegerValue("Glow-Fade", 10, 0..30) { mode == "Glow" }
        private val glowTargetAlpha by FloatValue("Glow-Target-Alpha", 0f, 0f..1f) { mode == "Glow" }

    private val colorRainbow by BoolValue("Rainbow", false)
        private val colorRed by IntegerValue("R", 0, 0..255) { !colorRainbow }
        private val colorGreen by IntegerValue("G", 90, 0..255) { !colorRainbow }
        private val colorBlue by IntegerValue("B", 255, 0..255) { !colorRainbow }

    private val maxRenderDistance by object : IntegerValue("MaxRenderDistance", 50, 1..200) {
        override fun onInit(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
        override fun onUpdate(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
    }

    private var maxRenderDistanceSq = 0.0

    private val onLook by BoolValue("OnLook", false)
    private val maxAngleDifference by FloatValue("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by BoolValue("ThruBlocks", true)

    private val color
        get() = if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)

    override fun onDisable() {
        synchronized(blocks) { blocks.clear() }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        for (entity in mc.theWorld.loadedEntityList) {
            if (mode != "Box" && mode != "OtherBox") break
            if (entity !is EntityFallingBlock) continue
            if (onLook && !isLookingOnEntities(entity, maxAngleDifference.toDouble())) continue
            if (!thruBlocks && !RotationUtils.isVisible(Vec3(entity.posX, entity.posY, entity.posZ))) continue

            val distanceSquared = mc.thePlayer.getDistanceSqToEntity(entity)

            if (distanceSquared <= maxRenderDistanceSq) {
                drawEntityBox(entity, color, mode == "Box")
            }
        }

        synchronized(blocks) {
            val iterator = blocks.entries.iterator()

            while (iterator.hasNext()) {
                val (pos, time) = iterator.next()

                if (System.currentTimeMillis() - time > 2000L) {
                    iterator.remove()
                    continue
                }

                drawBlockBox(pos, color, mode == "Box")
            }
        }
    }
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (mc.theWorld == null || mode != "Glow")
            return


        GlowShader.startDraw(event.partialTicks, glowRenderScale)

        for (entities in mc.theWorld.loadedEntityList) {
            val distanceSquared = mc.thePlayer.getDistanceSqToEntity(entities)

            if (distanceSquared <= maxRenderDistanceSq) {
                if (entities !is EntityFallingBlock) continue
                if (onLook && !isLookingOnEntities(entities, maxAngleDifference.toDouble())) continue
                if (!thruBlocks && !RotationUtils.isVisible(Vec3(entities.posX, entities.posY, entities.posZ))) continue

                try {
                    mc.theWorld.loadedEntityList.forEach { entity ->
                        if (entity is EntityFallingBlock) {
                            mc.renderManager.renderEntityStatic(entity, mc.timer.renderPartialTicks, true)
                        }
                    }
                } catch (ex: Exception) {
                    LOGGER.error("An error occurred while rendering all entities for shader esp", ex)
                }
            }
        }

        GlowShader.stopDraw(color, glowRadius, glowFade, glowTargetAlpha)
    }
}
