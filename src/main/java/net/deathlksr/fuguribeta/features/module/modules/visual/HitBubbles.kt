/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.FuguriBeta.CLIENT_NAME
import net.deathlksr.fuguribeta.event.AttackEvent
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.utils.ClientThemesUtils
import net.deathlksr.fuguribeta.utils.render.RenderUtils.customRotatedObject2D
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object HitBubbles : Module("HitBubbles", Category.VISUAL, hideModule = false) {

    private val followHit by BoolValue("FollowHit", true)

    private val dynamicRotation by BoolValue("DynamicRotation", false)

    private val lifeTime by IntegerValue("LifeTime", 1000, 1000..5000)

    private val colorMode by ListValue("ColorMode", arrayOf("Custom", "Theme"), "Custom")

    private val customRed by FloatValue("CustomRed", 1f, 0f..1f) { colorMode == "Custom" }
    private val customGreen by FloatValue("CustomGreen", 1f, 0f..1f) { colorMode == "Custom" }
    private val customBlue by FloatValue("CustomBlue", 1f, 0f..1f) { colorMode == "Custom" }
    private val customAlpha by FloatValue("CustomAlpha", 1f, 0f..1f) { colorMode == "Custom" }

    private val bubbles = ArrayList<Bubble>()

    private val tessellator = Tessellator.getInstance()
    private val buffer = tessellator.worldRenderer

    private val alphaPercentage: Float
        get() = customAlpha

    private val bubbleColor: Int
        get() =
            when(colorMode) {
                "Custom" -> java.awt.Color(customRed, customGreen, customBlue).rgb
                "Theme" -> ClientThemesUtils.getColor().rgb
                else -> java.awt.Color(1f, 1f, 1f).rgb
            }

    private val icon = ResourceLocation("${CLIENT_NAME.lowercase()}/bubble.png")

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity as? EntityLivingBase ?: return

        val bubblePosition = target.positionVector
            .addVector(0.0, target.height / 1.6, 0.0)

        val hitLocation = if (followHit) {
            val playerEyes = mc.thePlayer.getPositionEyes(1.0f)
            val playerLook = mc.thePlayer.getLook(1.0f)
            playerEyes.addVector(
                playerLook.xCoord,
                playerLook.yCoord,
                playerLook.zCoord
            )
        } else {
            bubblePosition
        }

        addBubble(bubblePosition, hitLocation)
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent?) {
        val alpha = alphaPercentage
        if (alpha < 0.05 || bubbles.isEmpty()) return

        removeExpiredBubbles()

        setupBubbleRendering {
            bubbles.forEach { bubble ->
                if (bubble.deltaTime <= 1.0f) {
                    drawBubble(bubble, alpha)
                }
            }
        }
    }

    private fun setupBubbleRendering(render: Runnable) {
        val renderManager = mc.renderManager
        val offset = Vec3(renderManager.renderPosX, renderManager.renderPosY, renderManager.renderPosZ)
        val isLightingEnabled = glIsEnabled(GL_LIGHTING)

        pushMatrix()
        enableBlend()
        disableAlpha()
        depthMask(false)
        disableCull()
        if (isLightingEnabled) disableLighting()
        glShadeModel(GL_SMOOTH)
        tryBlendFuncSeparate(770, 32772, 1, 0)

        glTranslated(-offset.xCoord, -offset.yCoord, -offset.zCoord)
        mc.textureManager.bindTexture(icon)

        render.run()

        glTranslated(offset.xCoord, offset.yCoord, offset.zCoord)
        resetColor()
        enableCull()
        depthMask(true)
        enableAlpha()
        popMatrix()
    }

    private fun drawBubble(bubble: Bubble, alpha: Float) {
        glPushMatrix()

        glTranslated(bubble.position.xCoord, bubble.position.yCoord, bubble.position.zCoord)

        val expansion = bubble.deltaTime
        translate(
            -sin(Math.toRadians(bubble.viewPitch.toDouble())) * expansion / 3.0,
            sin(Math.toRadians(bubble.viewYaw.toDouble())) * expansion / 2.0,
            -cos(Math.toRadians(bubble.viewPitch.toDouble())) * expansion / 3.0
        )

        glNormal3d(1.0, 1.0, 1.0)
        glRotated(bubble.viewPitch.toDouble(), 0.0, 1.0, 0.0)
        glRotated(bubble.viewYaw.toDouble(), if (mc.gameSettings.thirdPersonView == 2) -1.0 else 1.0, 0.0, 0.0)
        glScaled(-0.1, -0.1, 0.1)

        drawBubbleGraphics(bubble, alpha)

        glPopMatrix()
    }

    private fun calculateDynamicRotation(bubble: Bubble): Double {
        val player = mc.thePlayer ?: return 0.0
        val entityPos = bubble.position
        val deltaX = entityPos.xCoord - player.posX
        val deltaZ = entityPos.zCoord - player.posZ

        val angle = Math.toDegrees(atan2(deltaZ, deltaX))
        return angle - player.rotationYaw
    }

    private fun drawBubbleGraphics(bubble: Bubble, alpha: Float) {
        val radius = 50.0f * bubble.deltaTime * (1.0f - bubble.deltaTime)
        val rotationAngle = if (dynamicRotation) calculateDynamicRotation(bubble) else 0.0

        customRotatedObject2D(-radius / 2, -radius / 2, radius, radius, rotationAngle)
        buffer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR)

        val red = (bubbleColor shr 16 and 0xFF) / 255.0f
        val green = (bubbleColor shr 8 and 0xFF) / 255.0f
        val blue = (bubbleColor and 0xFF) / 255.0f

        buffer.pos(0.0, 0.0, 0.0).tex(0.0, 0.0).color(red, green, blue, alpha).endVertex()
        buffer.pos(0.0, radius.toDouble(), 0.0).tex(0.0, 1.0).color(red, green, blue, alpha).endVertex()
        buffer.pos(radius.toDouble(), radius.toDouble(), 0.0).tex(1.0, 1.0).color(red, green, blue, alpha).endVertex()
        buffer.pos(radius.toDouble(), 0.0, 0.0).tex(1.0, 0.0).color(red, green, blue, alpha).endVertex()

        tessellator.draw()
    }

    private fun removeExpiredBubbles() {
        bubbles.removeIf { it.deltaTime >= 1.0f }
    }

    private fun addBubble(position: Vec3, hitLocation: Vec3? = null) {
        val renderManager = mc.renderManager
        val finalPosition = if (followHit && hitLocation != null) hitLocation else position

        bubbles.add(
            Bubble(
                viewYaw = renderManager.playerViewX,
                viewPitch = -renderManager.playerViewY,
                position = finalPosition
            )
        )
    }

    class Bubble(var viewYaw: Float, var viewPitch: Float, var position: Vec3) {
        private val creationTime: Long = System.currentTimeMillis()

        val deltaTime: Float
            get() = (System.currentTimeMillis() - creationTime).toFloat() / lifeTime.toFloat()
    }
}