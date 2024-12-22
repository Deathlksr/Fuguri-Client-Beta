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
import net.deathlksr.fuguribeta.features.module.modules.client.AntiBot.isBot
import net.deathlksr.fuguribeta.features.module.modules.client.Teams
import net.deathlksr.fuguribeta.utils.EntityUtils.isLookingOnEntities
import net.deathlksr.fuguribeta.utils.EntityUtils.isSelected
import net.deathlksr.fuguribeta.utils.RotationUtils
import net.deathlksr.fuguribeta.utils.extensions.isClientFriend
import net.deathlksr.fuguribeta.utils.extensions.toRadians
import net.deathlksr.fuguribeta.utils.render.ColorUtils
import net.deathlksr.fuguribeta.utils.render.RenderUtils.glColor
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.pow

object Tracers : Module("Tracers", Category.VISUAL, hideModule = false) {

    private val colorMode by ListValue("Color", arrayOf("Custom", "DistanceColor", "Rainbow"), "Custom")
    private val colorRed by IntegerValue("R", 0, 0..255) { colorMode == "Custom" }
    private val colorGreen by IntegerValue("G", 160, 0..255) { colorMode == "Custom" }
    private val colorBlue by IntegerValue("B", 255, 0..255) { colorMode == "Custom" }

    private val thickness by FloatValue("Thickness", 2F, 1F..5F)

    private val maxRenderDistance by object : IntegerValue("MaxRenderDistance", 100, 1..200) {
        override fun onUpdate(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
    }

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    private val bot by BoolValue("Bots", true)
    private val teams by BoolValue("Teams", false)

    private val onLook by BoolValue("OnLook", false)
    private val maxAngleDifference by FloatValue("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by BoolValue("ThruBlocks", true)

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val thePlayer = mc.thePlayer ?: return

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(thickness)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        glBegin(GL_LINES)

        for (entity in mc.theWorld.loadedEntityList) {
            val distanceSquared = thePlayer.getDistanceSqToEntity(entity)

            if (distanceSquared <= maxRenderDistanceSq) {
                if (onLook && !isLookingOnEntities(entity, maxAngleDifference.toDouble())) continue
                if (entity !is EntityLivingBase || !bot && isBot(entity)) continue
                if (!thruBlocks && !RotationUtils.isVisible(Vec3(entity.posX, entity.posY, entity.posZ))) continue

                if (entity != thePlayer && isSelected(entity, false)) {
                    val dist = (thePlayer.getDistanceToEntity(entity) * 2).toInt().coerceAtMost(255)

                    val colorMode = colorMode.lowercase()
                    val color = when {
                        entity is EntityPlayer && entity.isClientFriend() -> Color(0, 0, 255, 150)
                        teams && Teams.state && Teams.isInYourTeam(entity) -> Color(0, 162, 232)
                        colorMode == "custom" -> Color(colorRed, colorGreen, colorBlue, 150)
                        colorMode == "distancecolor" -> Color(255 - dist, dist, 0, 150)
                        colorMode == "rainbow" -> ColorUtils.rainbow()
                        else -> Color(255, 255, 255, 150)
                    }

                    drawTraces(entity, color)
                }
            }
        }

        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glColor4f(1f, 1f, 1f, 1f)
    }

    private fun drawTraces(entity: Entity, color: Color) {
        val thePlayer = mc.thePlayer ?: return

        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks
            - mc.renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks
            - mc.renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks
            - mc.renderManager.renderPosZ)

        val yaw = thePlayer.prevRotationYaw + (thePlayer.rotationYaw - thePlayer.prevRotationYaw) * mc.timer.renderPartialTicks
        val pitch = thePlayer.prevRotationPitch + (thePlayer.rotationPitch - thePlayer.prevRotationPitch) * mc.timer.renderPartialTicks

        val eyeVector = Vec3(0.0, 0.0, 1.0).rotatePitch(-pitch.toRadians()).rotateYaw(-yaw.toRadians())

        glColor(color)

        glVertex3d(eyeVector.xCoord, thePlayer.getEyeHeight() + eyeVector.yCoord, eyeVector.zCoord)
        glVertex3d(x, y, z)
        glVertex3d(x, y, z)
        glVertex3d(x, y + entity.height, z)
    }
}
