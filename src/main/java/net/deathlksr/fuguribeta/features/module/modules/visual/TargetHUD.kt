package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.modules.combat.KillAura
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.EntityUtils
import net.deathlksr.fuguribeta.utils.extensions.hurtPercent
import net.deathlksr.fuguribeta.utils.extensions.skin
import net.deathlksr.fuguribeta.utils.render.RenderUtils.disableGlCap
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawImage
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawRect
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawShadow
import net.deathlksr.fuguribeta.utils.render.RenderUtils.enableGlCap
import net.deathlksr.fuguribeta.utils.render.RenderUtils.quickDrawHead
import net.deathlksr.fuguribeta.utils.render.RenderUtils.resetCaps
import net.deathlksr.fuguribeta.value.*
import net.minecraft.client.renderer.GlStateManager.resetColor
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object TargetHUD : Module("TargetHUD", Category.VISUAL, hideModule = false) {

    private val zoomIn = BoolValue("ZoomIn", true)
    private val zoomTicks = IntegerValue("ZoomInTicks", 4, 2..15) { zoomIn.get() }
    private val modeValue = ListValue("Mode", arrayOf("FDP", "Dilik_Penis", "Amogus", "Q1nny", "Vedma", "666Penis", "Markva", "Dicves"), "Markva")
    private val fontValue = FontValue("Font", Fonts.font40)
    private val smoothMove = BoolValue("SmoothHudMove", true)
    private val smoothValue = FloatValue("SmoothHudMoveValue", 5.2f, 1f..8f) { smoothMove.get() }
    private val smoothRot = BoolValue("SmoothHudRotations", true)
    private val fdpred by IntegerValue("FDPRed", 0, 0..255) { modeValue.get() == "FDP" }
    private val fdpgreen by IntegerValue("FDPGreen", 0, 0..255) { modeValue.get() == "FDP" }
    private val fdpblue by IntegerValue("FDPBlue", 0, 0..255) { modeValue.get() == "FDP" }
    private val fdpalpha by IntegerValue("FDPAlpha", 0, 0..255) { modeValue.get() == "FDP" }

    private val rotSmoothValue = FloatValue("SmothHudRotationValue", 2.1f, 1f..6f) { smoothRot.get() }
    private val scaleValue = FloatValue("Scale", 1F, 0.1F..4F)
    private val staticScale = BoolValue("StaticScale", false)
    private val ONLYONDELEKPENESNAHUI by BoolValue("OnlyOndilik_tot", false) { modeValue.get() == "Dilik_Penis" }
    private val onlymark by BoolValue("Only-Whytrxng", false) { modeValue.get() == "Markva" }
    private val translateY = FloatValue("TranslateY", 0.55F,-5F..5F)
    private val translateX = FloatValue("TranslateX", 0F, -15F.. 5F)
    private var xChange = translateX.get() * 20

    private var targetTicks = 0
    private var entityKeep = "yes"

    private var lastX = 0.0
    private var lastY = 0.0
    private var lastZ = 0.0

    private var lastYaw = 0.0f
    private var lastPitch = 0.0f

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if(mc.thePlayer == null)
            return
        for (entity in mc.theWorld.loadedEntityList) {
            if (EntityUtils.isSelected(entity, false)) {
                renderNameTag(entity as EntityLivingBase, entity.name)
            }
        }
    }

    private fun renderNameTag(entity: EntityLivingBase, tag: String) {
        xChange = translateX.get() * 20

        if (modeValue.get() == "Dilik_Penis" && ONLYONDELEKPENESNAHUI && (entity.name.lowercase() != "dilik_tot") && entity.name.lowercase() != "bestattacking")
            return

        if (modeValue.get() == "Markva" && onlymark && (entity.name.lowercase() != "whytrxng") && entity.name.lowercase() != "det0xyc0deine")
            return

        if (entity != KillAura.target && entity.name != entityKeep) {
            return
        } else if ( entity == KillAura.target) {
            entityKeep = entity.name
            targetTicks++
            if (targetTicks >= zoomTicks.get() + 2) {
                targetTicks = zoomTicks.get() + 1
            }
        } else if (KillAura.target == null) {
            targetTicks--
            if (targetTicks <= -1) {
                targetTicks = 0
                entityKeep = "dg636 top"
            }
        }

        if (targetTicks == 0) {
            return
        }

        // Push
        glPushMatrix()

        // Translate to player position
        val renderManager = mc.renderManager
        val timer = mc.timer

        if (smoothMove.get()) {
            lastX += ((entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX) - lastX) / smoothValue.get().toDouble()
            lastY += ((entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY + entity.eyeHeight.toDouble() + translateY.get().toDouble()) - lastY) / smoothValue.get().toDouble()
            lastZ += ((entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ) - lastZ) / smoothValue.get().toDouble()
            glTranslated( lastX, lastY, lastZ )
        } else {
            glTranslated( // Translate to player position with render pos and interpolate it
                entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX,
                entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY + entity.eyeHeight.toDouble() + translateY.get().toDouble(),
                entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
            )
        }

        // Rotate view to player
        if (smoothRot.get()) {
            lastYaw += (-mc.renderManager.playerViewY - lastYaw) / rotSmoothValue.get()
            lastPitch += (mc.renderManager.playerViewX - lastPitch) / rotSmoothValue.get()

            glRotatef(lastYaw, 0F, 1F, 0F)
            glRotatef(lastPitch, 1F, 0F, 0F)
        } else {
            glRotatef(-mc.renderManager.playerViewY, 0F, 1F, 0F)
            glRotatef(mc.renderManager.playerViewX, 1F, 0F, 0F)
        }

        // Scale
        var distance = mc.thePlayer.getDistanceToEntity(entity) / 4F

        if (distance < 1F)
            distance = 1F

        if (staticScale.get())
            distance = 1F

        var scale = (distance / 150F) * scaleValue.get()
        if (zoomIn.get()) {
            scale *= (targetTicks.coerceAtMost(zoomTicks.get()) / zoomTicks.get()).toFloat()
        }

        // Disable lightning and depth test
        disableGlCap(GL_LIGHTING, GL_DEPTH_TEST)

        // Enable blend
        enableGlCap(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Draw nametag
        when (modeValue.get().lowercase()) {

            "amogus" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                val rl = ResourceLocation("fuguribeta/textures/augustus.png")
                drawImage(rl, xChange.toInt(), 0, 250, 250)
            }

            "dilik_penis" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                val rl = ResourceLocation("fuguribeta/textures/dilikpenis.png")
                drawImage(rl, xChange.toInt(), 0, 250, 250)
                drawShadow(xChange, 0f, 250f, 250f)
            }

            "vedma" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                val rl = ResourceLocation("fuguribeta/textures/mybitches.png")
                drawImage(rl, xChange.toInt(), 0, 250, 250)
                drawShadow(xChange, 0f, 250f, 250f)
            }

            "q1nny" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                val rl = ResourceLocation("fuguribeta/textures/q1nnyotsos.png")
                drawImage(rl, xChange.toInt(), 0, 250, 250)
                drawShadow(xChange, 0f, 250f, 250f)
            }

            "666penis" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                val rl = ResourceLocation("fuguribeta/textures/666penis.png")
                drawImage(rl, xChange.toInt(), 0, 250, 250)
                drawShadow(xChange, 0f, 250f, 250f)
            }

            "markva" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                val rl = ResourceLocation("fuguribeta/textures/бостартпоссттаможня.png")
                drawImage(rl, xChange.toInt(), 0, 250, 250)
                drawShadow(xChange, 0f, 250f, 250f)
            }

            "dicves" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                val rl = ResourceLocation("fuguribeta/textures/disvisXyesosEbaniy.png")
                drawImage(rl, xChange.toInt(), 0, 442, 126)
                drawShadow(xChange, 0f, 442f, 126f)
            }

            "fdp" -> {
                glScalef(-scale * 2, -scale * 2, scale * 2)
                val font = fontValue.get()
                val addedLen = (60 + font.getStringWidth(entity.name) * 1.60f)

                drawRect(xChange, 0f, addedLen + -xChange, 47f, Color(fdpred, fdpgreen, fdpblue, fdpalpha).rgb)

                drawShadow(xChange, 0f, addedLen, 47f)

                val hurtPercent = entity.hurtPercent
                val scales = if (hurtPercent == 0f) { 1f } else if (hurtPercent < 0.5f) {
                    1 - (0.1f * hurtPercent * 2)
                } else {
                    0.9f + (0.1f * (hurtPercent - 0.5f) * 2)
                }
                val size = 35

                glPushMatrix()
                glTranslatef(5f, 5f, 0f)
                glScalef(scales, scales, scales)
                glTranslatef(((size * 0.5f * (1 - scales)) / scales), ((size * 0.5f * (1 - scales)) / scales), 0f)
                glColor4f(1f, 1 - hurtPercent, 1 - hurtPercent, 1f)
                quickDrawHead(entity.skin, 0, 0, size, size)
                glPopMatrix()

                glPushMatrix()
                glScalef(1.5f, 1.5f, 1.5f)
                font.drawString(entity.name, 39, 8, Color.WHITE.rgb)
                glPopMatrix()
            }
        }

        // Reset caps
        resetCaps()

        // Reset color
        resetColor()
        glColor4f(1F, 1F, 1F, 1F)

        // Pop
        glPopMatrix()
    }
}