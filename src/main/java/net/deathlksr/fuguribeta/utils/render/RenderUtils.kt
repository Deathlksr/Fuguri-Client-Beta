package net.deathlksr.fuguribeta.utils.render

import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.features.module.modules.combat.KillAura
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.heihgtlies
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.hitcolorvalue
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liesalpha
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liesalphatwo
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liesbluehit
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liescolorBlue
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liescolorBluetwo
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liescolorGreen
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liescolorGreentwo
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liescolorRed
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liescolorRedtwo
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liesgreenhit
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.liesredhit
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.colorValue
import net.deathlksr.fuguribeta.features.module.modules.visual.TargetESP.speedcolorlies
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.MinecraftInstance
import net.deathlksr.fuguribeta.utils.UIEffectRenderer.drawTexturedRect
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlock
import net.deathlksr.fuguribeta.utils.extensions.hitBox
import net.deathlksr.fuguribeta.utils.extensions.toRadians
import net.deathlksr.fuguribeta.utils.render.ColorUtils.setColour
import net.deathlksr.fuguribeta.utils.render.animation.AnimationUtil.easeInOutQuadX
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemBow
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemSword
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL14
import java.awt.Color
import kotlin.math.*

object RenderUtils : MinecraftInstance() {
    private val glCapMap = mutableMapOf<Int, Boolean>()
    private val DISPLAY_LISTS_2D = IntArray(4)
    private const val ZLEVEL1: Float = 0f
    var deltaTime = 0

    private var startTime: Long = 0
    private var animationDuration: Int = 500

    init {
        for (i in DISPLAY_LISTS_2D.indices) {
            DISPLAY_LISTS_2D[i] = glGenLists(1)
        }

        glNewList(DISPLAY_LISTS_2D[0], GL_COMPILE)
        quickDrawRect(-7f, 2f, -4f, 3f)
        quickDrawRect(4f, 2f, 7f, 3f)
        quickDrawRect(-7f, 0.5f, -6f, 3f)
        quickDrawRect(6f, 0.5f, 7f, 3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[1], GL_COMPILE)
        quickDrawRect(-7f, 3f, -4f, 3.3f)
        quickDrawRect(4f, 3f, 7f, 3.3f)
        quickDrawRect(-7.3f, 0.5f, -7f, 3.3f)
        quickDrawRect(7f, 0.5f, 7.3f, 3.3f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[2], GL_COMPILE)
        quickDrawRect(4f, -20f, 7f, -19f)
        quickDrawRect(-7f, -20f, -4f, -19f)
        quickDrawRect(6f, -20f, 7f, -17.5f)
        quickDrawRect(-7f, -20f, -6f, -17.5f)
        glEndList()
        glNewList(DISPLAY_LISTS_2D[3], GL_COMPILE)
        quickDrawRect(7f, -20f, 7.3f, -17.5f)
        quickDrawRect(-7.3f, -20f, -7f, -17.5f)
        quickDrawRect(4f, -20.3f, 7.3f, -20f)
        quickDrawRect(-7.3f, -20.3f, -4f, -20f)
        glEndList()
    }

    fun drawBlockBox(blockPos: BlockPos, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager
        val timer = mc.timer

        val x = blockPos.x - renderManager.renderPosX
        val y = blockPos.y - renderManager.renderPosY
        val z = blockPos.z - renderManager.renderPosZ

        var axisAlignedBB = AxisAlignedBB.fromBounds(x, y, z, x + 1.0, y + 1.0, z + 1.0)
        val block = getBlock(blockPos)
        if (block != null) {
            val player = mc.thePlayer
            val posX = player.lastTickPosX + (player.posX - player.lastTickPosX) * timer.renderPartialTicks.toDouble()
            val posY = player.lastTickPosY + (player.posY - player.lastTickPosY) * timer.renderPartialTicks.toDouble()
            val posZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * timer.renderPartialTicks.toDouble()
            block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)
            axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
                .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
                .offset(-posX, -posY, -posZ)
        }

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color.red, color.green, color.blue, if (color.alpha != 255) color.alpha else if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)

        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL_LINE_SMOOTH)
            glColor(color)
            drawSelectionBoundingBox(axisAlignedBB)
        }

        resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawSelectionBoundingBox(boundingBox: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(GL_LINE_STRIP, DefaultVertexFormats.POSITION)

        // Lower Rectangle
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()

        // Upper Rectangle
        worldRenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
        worldRenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
        tessellator.draw()
    }

    fun drawEntityBox(entity: Entity, color: Color, outline: Boolean) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        enableGlCap(GL_BLEND)
        disableGlCap(GL_TEXTURE_2D, GL_DEPTH_TEST)
        glDepthMask(false)
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)
        val entityBox = entity.hitBox
        val axisAlignedBB = AxisAlignedBB.fromBounds(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )
        if (outline) {
            glLineWidth(1f)
            enableGlCap(GL_LINE_SMOOTH)
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB)
        }
        glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glDepthMask(true)
        resetCaps()
    }

    fun drawBacktrackBox(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(2f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color.red, color.green, color.blue, 90)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun drawAxisAlignedBB(axisAlignedBB: AxisAlignedBB, color: Color) {
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(2f)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color)
        drawFilledBox(axisAlignedBB)
        resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    fun drawPlatform(y: Double, color: Color, size: Double) {
        val renderManager = mc.renderManager
        val renderY = y - renderManager.renderPosY
        drawAxisAlignedBB(AxisAlignedBB.fromBounds(size, renderY + 0.02, size, -size, renderY, -size), color)
    }

    fun drawPlatform(entity: Entity, color: Color) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks - renderManager.renderPosX
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks - renderManager.renderPosY
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks - renderManager.renderPosZ
        val axisAlignedBB = entity.entityBoundingBox
            .offset(-entity.posX, -entity.posY, -entity.posZ)
            .offset(x, y, z)
        drawAxisAlignedBB(
            AxisAlignedBB.fromBounds(
                axisAlignedBB.minX,
                axisAlignedBB.maxY + 0.2,
                axisAlignedBB.minZ,
                axisAlignedBB.maxX,
                axisAlignedBB.maxY + 0.26,
                axisAlignedBB.maxZ
            ), color
        )
    }

    fun drawLies(entity: EntityLivingBase, event: Render3DEvent, speedMultiplier: Double, trailLengthMultiplier: Double, radiuslies: Float) {
        val baseTime = 3000.0
        val everyTime = baseTime / speedMultiplier
        val drawTime = (System.currentTimeMillis() % everyTime).toInt()
        val drawMode = drawTime > (everyTime / 2)
        var drawPercent = drawTime / (everyTime / 2.0)
        if (!drawMode) {
            drawPercent = 1 - drawPercent
        } else {
            drawPercent -= 1
        }
        drawPercent = easeInOutQuadX(drawPercent)

        mc.entityRenderer.disableLightmap()
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_CULL_FACE)
        glShadeModel(7425)
        mc.entityRenderer.disableLightmap()

        val bb = entity.entityBoundingBox
        val radius = ((bb.maxX - bb.minX) + (bb.maxZ - bb.minZ)) * radiuslies
        val height = (bb.maxY - bb.minY) * if (heihgtlies) 1.1 else 1.0
        val x =
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
        val y =
            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY) + height * drawPercent
        val z =
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ

        val timeFactor = (System.currentTimeMillis() % everyTime) / everyTime.toFloat()
        val eased = (height / 3) * (if (drawPercent > 0.5) {
            1 - drawPercent
        } else {
            drawPercent
        }) * (if (drawMode) {
            -1
        } else {
            1
        }) * trailLengthMultiplier

        val oscillation = sin(timeFactor * Math.PI * 2) * (height / 5)

        val finalY = y + eased + oscillation
        glBegin(GL_QUADS)
        for (i in 5..360 step 5) {
            val x1 = x - sin(i * Math.PI / 180F) * radius
            val z1 = z + cos(i * Math.PI / 180F) * radius
            val x2 = x - sin((i - 5) * Math.PI / 180F) * radius
            val z2 = z + cos((i - 5) * Math.PI / 180F) * radius
            glColor4f(liescolorRed, liescolorGreen, liescolorBlue, liesalpha)
            glVertex3d(x1, finalY, z1)
            glVertex3d(x2, finalY, z2)
            glColor4f(liescolorRedtwo, liescolorGreentwo, liescolorBluetwo, liesalphatwo)
            glVertex3d(x2, y, z2)
            glVertex3d(x1, y, z1)
        }

        glEnd()
        glEnable(GL_CULL_FACE)
        glShadeModel(7424)
        glColor4f(0f, 0f, 0f, 0f)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    fun drawNew(entity: EntityLivingBase, event: Render3DEvent, speedMultiplier: Double, trailLengthMultiplier: Double, liesstepvalue: Int) {
        glPushMatrix()
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_BLEND)
        glDisable(GL_DEPTH_TEST)
        glDisable(GL_CULL_FACE)
        glShadeModel(7425)
        mc.entityRenderer.disableLightmap()

        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.partialTicks - mc.renderManager.viewerPosX
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.partialTicks - mc.renderManager.viewerPosY
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.partialTicks - mc.renderManager.viewerPosZ

        glBegin(GL_QUAD_STRIP)

        val animation = sin(System.currentTimeMillis() * 0.003 * speedMultiplier)

        for (i in 0..360 step liesstepvalue) {
            val x1 = x + sin(i * Math.PI / 180) * 0.7
            val z1 = z + cos(i * Math.PI / 180) * 0.7
            val y1 = y + animation

            val color1 = Color(liescolorRed, liescolorGreen, liescolorBlue)
            val color2 = Color(liescolorRedtwo, liescolorGreentwo, liescolorBluetwo)
            val gradientvalue = (cos(i * Math.PI / 180 * speedcolorlies) + 1) / 2
            val gradientColor = ColorUtils.mixColorse(color1, color2, gradientvalue.toFloat())
            when (colorValue) {

                "Gradient" -> {
                    glColor4f(gradientColor.red / 255F, gradientColor.green / 255F, gradientColor.blue / 255F, liesalpha)
                    glVertex3d(x1, y1 + entity.height / 2, z1)
                    glColor4f(gradientColor.red / 255F, gradientColor.green / 255F, gradientColor.blue / 255F, 0f)
                    glVertex3d(x1, (y1 + entity.height / 2) + (animation * trailLengthMultiplier), z1)
                }
                "Custom" -> {
                    if (KillAura.target?.hurtTime!! > 0 && hitcolorvalue) {
                        glColor4f(liesredhit, liesgreenhit, liesbluehit, liesalpha)
                        glVertex3d(x1, y1 + entity.height / 2, z1)
                        glColor4f(liesredhit, liesgreenhit, liesbluehit, 0f)
                        glVertex3d(x1, (y1 + entity.height / 2) + (animation * trailLengthMultiplier), z1)
                    } else {
                        glColor4f(liescolorRed, liescolorGreen, liescolorBlue, liesalpha)
                        glVertex3d(x1, y1 + entity.height / 2, z1)
                        glColor4f(liescolorRed, liescolorGreen, liescolorBlue, 0f)
                        glVertex3d(x1, (y1 + entity.height / 2) + (animation * trailLengthMultiplier), z1)
                    }
                }
            }
        }
        glEnd()
        glEnable(GL_CULL_FACE)
        glShadeModel(7424)
        glColor4f(0f, 0f, 0f, 0f)
        glEnable(GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glPopMatrix()
    }

    /**
     * Draws a rectangle.
     *
     * @param left The left coordinate.
     * @param top The top coordinate.
     * @param right The right coordinate.
     * @param bottom The bottom coordinate.
     * @param color The color of the rectangle.
     */
    fun drawFilledRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        var lefts = left
        var tops = top
        var rights = right
        var bottoms = bottom

        if (lefts < rights) {
            val i = lefts
            lefts = rights
            rights = i
        }

        if (tops < bottoms) {
            val j = tops
            tops = bottoms
            bottoms = j
        }

        val alpha = (color shr 24 and 255) / 255.0f
        val red = (color shr 16 and 255) / 255.0f
        val green = (color shr 8 and 255) / 255.0f
        val blue = (color and 255) / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(red, green, blue, alpha)
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(lefts.toDouble(), bottoms.toDouble(), 0.0).endVertex()
        worldRenderer.pos(rights.toDouble(), bottoms.toDouble(), 0.0).endVertex()
        worldRenderer.pos(rights.toDouble(), tops.toDouble(), 0.0).endVertex()
        worldRenderer.pos(lefts.toDouble(), tops.toDouble(), 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    fun drawFilledBox(axisAlignedBB: AxisAlignedBB) {
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION)
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).endVertex()
        worldRenderer.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).endVertex()
        tessellator.draw()
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Color) = drawRect(x, y, x2, y2, color.rgb)

    fun drawBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, borderColor: Int, rectColor: Int) {
        drawRect(x, y, x2, y2, rectColor)
        drawBorder(x, y, x2, y2, width, borderColor)
    }

    fun drawBorderedRect(x: Int, y: Int, x2: Int, y2: Int, width: Int, borderColor: Int, rectColor: Int) {
        drawRect(x, y, x2, y2, rectColor)
        drawBorder(x, y, x2, y2, width, borderColor)
    }

    @JvmStatic
    fun drawOnBorderedRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int) {
        drawRect(x, y, x2, y2, color2)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        glColor(color1)
        glLineWidth(width)
        glBegin(1)
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawRoundedBorderRect(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int, color2: Int, radius: Float) {
        drawRoundedRect(x, y, x2, y2, color1, radius)
        drawRoundedBorder(x, y, x2, y2, width, color2, radius)
    }

    fun drawRectBasedBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color1: Int) {
        drawRect(x - width / 2f, y - width / 2f, x2 + width / 2f, y + width / 2f, color1)
        drawRect(x - width / 2f, y + width / 2f, x + width / 2f, y2 + width / 2f, color1)
        drawRect(x2 - width / 2f, y + width / 2f, x2 + width / 2f, y2 + width / 2f, color1)
        drawRect(x + width / 2f, y2 - width / 2f, x2 - width / 2f, y2 + width / 2f, color1)
    }

    fun drawBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawBorder(x: Int, y: Int, x2: Int, y2: Int, width: Int, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glLineWidth(width.toFloat())
        glBegin(GL_LINE_LOOP)
        glVertex2i(x2, y)
        glVertex2i(x, y)
        glVertex2i(x, y2)
        glVertex2i(x2, y2)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawRoundedBorder(x: Float, y: Float, x2: Float, y2: Float, width: Float, color: Int, radius: Float) {
        drawRoundedBordered(x, y, x2, y2, color, width, radius)
    }

    private fun drawRoundedBordered(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, width: Float, radius: Float) {
        val alpha = (color ushr 24 and 0xFF) / 255.0f
        val red = (color ushr 16 and 0xFF) / 255.0f
        val green = (color ushr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(width)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_LINE_LOOP)

        val radiusD = radius.toDouble()

        val corners = listOf(
            Triple(newX2 - radiusD, newY2 - radiusD, 0.0),
            Triple(newX2 - radiusD, newY1 + radiusD, 90.0),
            Triple(newX1 + radiusD, newY1 + radiusD, 180.0),
            Triple(newX1 + radiusD, newY2 - radiusD, 270.0)
        )

        for ((cx, cy, startAngle) in corners) {
            for (i in 0..90 step 10) {
                val angle = Math.toRadians(startAngle + i)
                val x = cx + radiusD * sin(angle)
                val y = cy + radiusD * cos(angle)
                glVertex2d(x, y)
            }
        }

        glEnd()

        glColor4f(0f, 0f, 0f, 1f)

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
    }

    fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float) {
        glBegin(GL_QUADS)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
    }

    fun drawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2f(x2, y)
        glVertex2f(x, y)
        glVertex2f(x, y2)
        glVertex2f(x2, y2)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawRoundedRectz(x1 : Float, y1 : Float, x2 : Float, y2 : Float, radius : Float, color : Int) {
        val alpha = (color ushr 24 and 0xFF) / 255.0f
        val red = (color ushr 16 and 0xFF) / 255.0f
        val green = (color ushr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius)
    }

    fun drawRected(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        var lefts = left
        var tops = top
        var rights = right
        var bottoms = bottom
        if (lefts < right) {
            val i = lefts
            lefts = rights
            rights = i
        }

        if (tops < bottom) {
            val j = tops
            tops = bottoms
            bottoms = j
        }

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(lefts.toDouble(), bottoms.toDouble(), 0.0).endVertex()
        worldrenderer.pos(rights.toDouble(), bottoms.toDouble(), 0.0).endVertex()
        worldrenderer.pos(rights.toDouble(), tops.toDouble(), 0.0).endVertex()
        worldrenderer.pos(lefts.toDouble(), tops.toDouble(), 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    fun drawRect(x: Int, y: Int, x2: Int, y2: Int, color: Int) {
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2i(x2, y)
        glVertex2i(x, y)
        glVertex2i(x, y2)
        glVertex2i(x2, y2)
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawExhiRect(x: Float, y: Float, x2: Float, y2: Float, alpha: Float) {
        drawRect(x - 3.5f, y - 3.5f, x2 + 3.5f, y2 + 3.5f, Color(0f, 0f, 0f, alpha).rgb)
        drawRect(x - 3f, y - 3f, x2 + 3f, y2 + 3f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x - 2.5f, y - 2.5f, x2 + 2.5f, y2 + 2.5f, Color(26f / 255f, 26f / 255f, 26f / 255f, alpha).rgb)
        drawRect(x - 0.5f, y - 0.5f, x2 + 0.5f, y2 + 0.5f, Color(50f / 255f, 50f / 255f, 50f / 255f, alpha).rgb)
        drawRect(x, y, x2, y2, Color(18f / 255f, 18 / 255f, 18f / 255f, alpha).rgb)
    }

    /**
     * Like [.drawRect], but without setup
     */
    private fun quickDrawRect(x: Float, y: Float, x2: Float, y2: Float, color: Int) {
        glColor(color)
        glBegin(GL_QUADS)
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()
    }

    /**
     * Draws a rectangle with borders.
     *
     * @param x The x coordinate of the top-left corner.
     * @param y The y coordinate of the top-left corner.
     * @param x2 The x coordinate of the bottom-right corner.
     * @param y2 The y coordinate of the bottom-right corner.
     * @param borderWidth The width of the border.
     * @param borderColor The color of the border.
     * @param fillColor The color of the fill.
     */
    fun drawRectWithBorder(x: Float, y: Float, x2: Float, y2: Float, borderWidth: Float, borderColor: Int, fillColor: Int) {
        drawFilledRect(x, y, x2, y2, fillColor)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        glColor(borderColor)
        glLineWidth(borderWidth)
        glBegin(GL_LINES)
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y.toDouble())
        glVertex2d(x2.toDouble(), y.toDouble())
        glVertex2d(x.toDouble(), y2.toDouble())
        glVertex2d(x2.toDouble(), y2.toDouble())
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawLoadingCircle(x: Float, y: Float) {
        for (i in 0..3) {
            val rot = (System.nanoTime() / 5000000 * i % 360).toInt()
            drawCircle(x, y, (i * 10).toFloat(), rot - 180, rot)
        }
    }


    private fun drawRoundWordRect(left: Float, top: Float, width: Float, height: Float, color: Int) {
        val f3 = (color shr 24 and 0xFF) / 255.0f
        val f4 = (color shr 16 and 0xFF) / 255.0f
        val f5 = (color shr 8 and 0xFF) / 255.0f
        val f6 = (color and 0xFF) / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(f4, f5, f6, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(left.toDouble(), (top + height).toDouble(), 0.0).endVertex()
        worldrenderer.pos((left + width).toDouble(), (top + height).toDouble(), 0.0).endVertex()
        worldrenderer.pos((left + width).toDouble(), top.toDouble(), 0.0).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    fun drawRoundedRect(x: Float, y: Float, width: Float, height: Float, edgeRadius: Float, color: Int, borderWidth: Float, borderColor: Int) {
        var edgeRadiuss = edgeRadius
        var colors = color
        var borderColors = borderColor
        if (colors == 16777215) {
            colors = -65794
        }
        if (borderColors == 16777215) {
            borderColors = -65794
        }
        if (edgeRadiuss < 0.0f) {
            edgeRadiuss = 0.0f
        }
        if (edgeRadiuss > width / 2.0f) {
            edgeRadiuss = width / 2.0f
        }
        if (edgeRadiuss > height / 2.0f) {
            edgeRadiuss = height / 2.0f
        }
        drawRoundWordRect(x + edgeRadiuss, y + edgeRadiuss,
            width - edgeRadiuss * 2.0f,
            height - edgeRadiuss * 2.0f,
            color
        )
        drawRoundWordRect(x + edgeRadiuss, y, width - edgeRadiuss * 2.0f, edgeRadiuss, colors)
        drawRoundWordRect(x + edgeRadiuss, y + height - edgeRadiuss, width - edgeRadiuss * 2.0f, edgeRadiuss, colors)
        drawRoundWordRect(x, y + edgeRadiuss, edgeRadiuss, height - edgeRadiuss * 2.0f, colors)
        drawRoundWordRect(x + width - edgeRadiuss, y + edgeRadiuss, edgeRadiuss, height - edgeRadiuss * 2.0f, colors)
        enableRender2D()
        color(colors)
        glBegin(6)
        var centerX = x + edgeRadiuss
        var centerY = y + edgeRadiuss
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadiuss.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * (i + 180) / (vertices * 4)
                glVertex2d(
                    centerX + sin(angleRadians) * edgeRadiuss,
                    centerY + cos(angleRadians) * edgeRadiuss
                )
                ++i
            }
        }
        glEnd()
        glBegin(6)
        centerX = x + width - edgeRadiuss
        centerY = y + edgeRadiuss
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadiuss.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * (i + 90) / (vertices * 4)
                glVertex2d(
                    centerX + sin(angleRadians) * edgeRadiuss,
                    centerY + cos(angleRadians) * edgeRadiuss
                )
                ++i
            }
        }
        glEnd()
        glBegin(6)
        centerX = x + edgeRadiuss
        centerY = y + height - edgeRadiuss
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadiuss.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * (i + 270) / (vertices * 4)
                glVertex2d(
                    centerX + sin(angleRadians) * edgeRadiuss,
                    centerY + cos(angleRadians) * edgeRadiuss
                )
                ++i
            }
        }
        glEnd()
        glBegin(6)
        centerX = x + width - edgeRadiuss
        centerY = y + height - edgeRadiuss
        glVertex2d(centerX.toDouble(), centerY.toDouble())
        run {
            val vertices = min(max(edgeRadiuss.toDouble(), 10.0), 90.0).toInt()
            var i = 0
            while (i < vertices + 1) {
                val angleRadians = 6.283185307179586 * i / (vertices * 4)
                glVertex2d(
                    centerX + sin(angleRadians) * edgeRadiuss,
                    centerY + cos(angleRadians) * edgeRadiuss
                )
                ++i
            }
        }
        glEnd()
        color(borderColors)
        glLineWidth(borderWidth)
        glBegin(3)
        centerX = x + edgeRadiuss
        centerY = y + edgeRadiuss
        val vertices: Int
        var i: Int
        vertices = (min(max(edgeRadiuss.toDouble(), 10.0), 90.0).toInt().also {
            i = it
        })
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * (i + 180) / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadiuss, centerY + cos(angleRadians) * edgeRadiuss)
            --i
        }
        glVertex2d((x + edgeRadiuss).toDouble(), y.toDouble())
        glVertex2d((x + width - edgeRadiuss).toDouble(), y.toDouble())
        centerX = x + width - edgeRadiuss
        centerY = y + edgeRadiuss
        i = vertices
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * (i + 90) / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadiuss, centerY + cos(angleRadians) * edgeRadiuss)
            --i
        }
        glVertex2d((x + width).toDouble(), (y + edgeRadiuss).toDouble())
        glVertex2d((x + width).toDouble(), (y + height - edgeRadiuss).toDouble())
        centerX = x + width - edgeRadiuss
        centerY = y + height - edgeRadiuss
        i = vertices
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * i / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadiuss, centerY + cos(angleRadians) * edgeRadiuss)
            --i
        }
        glVertex2d((x + width - edgeRadiuss).toDouble(), (y + height).toDouble())
        glVertex2d((x + edgeRadiuss).toDouble(), (y + height).toDouble())
        centerX = x + edgeRadiuss
        centerY = y + height - edgeRadiuss
        i = vertices
        while (i >= 0) {
            val angleRadians = 6.283185307179586 * (i + 270) / (vertices * 4)
            glVertex2d(centerX + sin(angleRadians) * edgeRadiuss, centerY + cos(angleRadians) * edgeRadiuss)
            --i
        }
        glVertex2d(x.toDouble(), (y + height - edgeRadiuss).toDouble())
        glVertex2d(x.toDouble(), (y + edgeRadiuss).toDouble())
        glEnd()
        disableRender2D()
    }

    fun drawRoundedRect(x1: Float, y1: Float, x2: Float, y2: Float, color: Int, radius: Float) {
        val alpha = (color ushr 24 and 0xFF) / 255.0f
        val red = (color ushr 16 and 0xFF) / 255.0f
        val green = (color ushr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius)
    }

    fun drawRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float, color: Int) {
        drawRoundedRect(paramXStart, paramYStart, paramXEnd, paramYEnd, radius, color, true)
    }

    fun drawRoundedRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int,
        popPush: Boolean
    ) {
        var paramXStarts = paramXStart
        var paramYStarts = paramYStart
        var paramXEnds = paramXEnd
        var paramYEnds = paramYEnd

        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z: Float
        if (paramXStarts > paramXEnds) {
            z = paramXStarts
            paramXStarts = paramXEnds
            paramXEnds = z
        }

        if (paramYStarts > paramYEnds) {
            z = paramYStarts
            paramYStarts = paramYEnds
            paramYEnds = z
        }

        val x1 = (paramXStarts + radius).toDouble()
        val y1 = (paramYStarts + radius).toDouble()
        val x2 = (paramXEnds - radius).toDouble()
        val y2 = (paramYEnds - radius).toDouble()

        if (popPush) glPushMatrix()

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(
                    x2 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(
                    x1 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }

        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)

        if (popPush) glPopMatrix()
    }

    fun drawRoundedBindRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int,
        popPush: Boolean
    ) {
        var paramXStarts = paramXStart
        var paramYStarts = paramYStart
        var paramXEnds = paramXEnd
        var paramYEnds = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z: Float
        if (paramXStarts > paramXEnds) {
            z = paramXStarts
            paramXStarts = paramXEnds
            paramXEnds = z
        }

        if (paramYStarts > paramYEnds) {
            z = paramYStarts
            paramYStarts = paramYEnds
            paramYEnds = z
        }

        val x1 = (paramXStarts + radius).toDouble()
        val y1 = (paramYStarts + radius).toDouble()
        val x2 = (paramXEnds - radius).toDouble()
        val y2 = (paramYEnds - radius).toDouble()

        if (popPush) glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(
                    x2 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(
                    x1 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (popPush) glPopMatrix()
    }

    fun drawRoundedBindRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int
    ) {
        drawRoundedBindRect(paramXStart, paramYStart, paramXEnd, paramYEnd, radius, color, true)
    }

    fun drawArrayRect(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        var lefts = left
        var tops = top
        var rights = right
        var bottoms = bottom
        if (lefts < rights) {
            val i = lefts
            lefts = rights
            rights = i
        }

        if (tops < bottoms) {
            val j = tops
            tops = bottoms
            bottoms = j
        }

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(lefts.toDouble(), bottoms.toDouble(), 0.0).endVertex()
        worldrenderer.pos(rights.toDouble(), bottoms.toDouble(), 0.0).endVertex()
        worldrenderer.pos(rights.toDouble(), tops.toDouble(), 0.0).endVertex()
        worldrenderer.pos(lefts.toDouble(), tops.toDouble(), 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    /**
     * @author Shoroa
     * @param x : X pos
     * @param y : Y pos
     * @param x1 : X2 pos
     * @param y1 : Y2 pos
     * @param radius : round of edges;
     * @param color : color;
     * @param color2 : color2;
     * @param color3 : color3;
     * @param color4 : color4;
     */
    fun drawRoundedGradientRectCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        radius: Float,
        color: Int,
        color2: Int,
        color3: Int,
        color4: Int
    ) {
        var xs = x
        var ys = y
        var x1s = x1
        var y1s = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        xs *= 2.0.toFloat()
        ys *= 2.0.toFloat()
        x1s *= 2.0.toFloat()
        y1s *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                xs + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                ys + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                xs + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1s - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color3)
        i = 0
        while (i <= 90) {
            glVertex2d(x1s - radius + sin(i * Math.PI / 180.0) * radius, y1s - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color4)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1s - radius + sin(i * Math.PI / 180.0) * radius,
                ys + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()


        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        setColour(-1)
    }

    fun drawRoundedGradientRectCorner(
        x: Float,
        y: Float,
        x1: Float,
        y1: Float,
        radius: Float,
        color: Int,
        color2: Int
    ) {
        var xs = x
        var ys = y
        var x1s = x1
        var y1s = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        xs *= 2.0.toFloat()
        ys *= 2.0.toFloat()
        x1s *= 2.0.toFloat()
        y1s *= 2.0.toFloat()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glBegin(6)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                xs + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                ys + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                xs + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1s - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(x1s - radius + sin(i * Math.PI / 180.0) * radius, y1s - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1s - radius + sin(i * Math.PI / 180.0) * radius,
                ys + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()


        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        setColour(-1)
    }

    /**
     * Draw rounded rect.
     *
     * @param paramXStart the param x start
     * @param paramYStart the param y start
     * @param paramXEnd   the param x end
     * @param paramYEnd   the param y end
     * @param radius      the radius
     * @param color       the color
     */
    fun drawShadowRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int
    ) {
        drawShadowRect(paramXStart, paramYStart, paramXEnd, paramYEnd, radius, color, true)
    }

    /**
     * Draw rounded rect.
     *
     * @param paramXStart the param x start
     * @param paramYStart the param y start
     * @param paramXEnd   the param x end
     * @param paramYEnd   the param y end
     * @param radius      the radius
     * @param color       the color
     * @param popPush     the pop push
     */
    fun drawShadowRect(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        radius: Float,
        color: Int,
        popPush: Boolean
    ) {
        var paramXStarts = paramXStart
        var paramYStarts = paramYStart
        var paramXEnds = paramXEnd
        var paramYEnds = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z: Float
        if (paramXStarts > paramXEnds) {
            z = paramXStarts
            paramXStarts = paramXEnds
            paramXEnds = z
        }

        if (paramYStarts > paramYEnds) {
            z = paramYStarts
            paramYStarts = paramYEnds
            paramYEnds = z
        }

        val x1 = (paramXStarts + radius).toDouble()
        val y1 = (paramYStarts + radius).toDouble()
        val x2 = (paramXEnds - radius).toDouble()
        val y2 = (paramYEnds - radius).toDouble()

        if (popPush) glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(
                    x2 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(
                    x1 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        if (popPush) glPopMatrix()
    }
    fun drawRoundedRect2(x1: Float, y1: Float, x2: Float, y2: Float, color: Color, radius: Float) {
        val alpha = color.alpha / 255.0f
        val red = color.red / 255.0f
        val green = color.green / 255.0f
        val blue = color.blue / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius)
    }

    fun drawRoundedRect3(x1: Float, y1: Float, x2: Float, y2: Float, color: Float, radius: Float) {
        val intColor = color.toInt()
        val alpha = (intColor ushr 24 and 0xFF) / 255.0f
        val red = (intColor ushr 16 and 0xFF) / 255.0f
        val green = (intColor ushr 8 and 0xFF) / 255.0f
        val blue = (intColor and 0xFF) / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius)
    }

    fun customRounded(
        paramXStart: Float,
        paramYStart: Float,
        paramXEnd: Float,
        paramYEnd: Float,
        rTL: Float,
        rTR: Float,
        rBR: Float,
        rBL: Float,
        color: Int
    ) {
        var paramXStarts = paramXStart
        var paramYStarts = paramYStart
        var paramXEnds = paramXEnd
        var paramYEnds = paramYEnd
        val alpha = (color shr 24 and 0xFF) / 255.0f
        val red = (color shr 16 and 0xFF) / 255.0f
        val green = (color shr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        var z: Float
        if (paramXStarts > paramXEnds) {
            z = paramXStarts
            paramXStarts = paramXEnds
            paramXEnds = z
        }

        if (paramYStarts > paramYEnds) {
            z = paramYStarts
            paramYStarts = paramYEnds
            paramYEnds = z
        }

        val xTL = (paramXStarts + rTL).toDouble()
        val yTL = (paramYStarts + rTL).toDouble()

        val xTR = (paramXEnds - rTR).toDouble()
        val yTR = (paramYStarts + rTR).toDouble()

        val xBR = (paramXEnds - rBR).toDouble()
        val yBR = (paramYEnds - rBR).toDouble()

        val xBL = (paramXStarts + rBL).toDouble()
        val yBL = (paramYEnds - rBL).toDouble()

        glPushMatrix()
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(xBR + sin(i * degree) * rBR, yBR + cos(i * degree) * rBR)
                i += 0.25
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(xTR + sin(i * degree) * rTR, yTR + cos(i * degree) * rTR)
                i += 0.25
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(xTL + sin(i * degree) * rTL, yTL + cos(i * degree) * rTL)
                i += 0.25
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(xBL + sin(i * degree) * rBL, yBL + cos(i * degree) * rBL)
            i += 0.25
        }
        glEnd()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glPopMatrix()
    }

    fun drawRoundedRectInt(x1: Int, y1: Int, x2: Int, y2: Int, color: Int, radius: Float) {
        val alpha = (color ushr 24 and 0xFF) / 255.0f
        val red = (color ushr 16 and 0xFF) / 255.0f
        val green = (color ushr 8 and 0xFF) / 255.0f
        val blue = (color and 0xFF) / 255.0f

        val (newX1, newY1, newX2, newY2) = orderPoints(x1.toFloat(), y1.toFloat(), x2.toFloat(), y2.toFloat())

        drawRoundedRectangle(newX1, newY1, newX2, newY2, red, green, blue, alpha, radius)
    }

    private fun drawRoundedRectangle(x1: Float, y1: Float, x2: Float, y2: Float, red: Float, green: Float, blue: Float, alpha: Float, radius: Float) {
        val (newX1, newY1, newX2, newY2) = orderPoints(x1, y1, x2, y2)

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)

        glColor4f(red, green, blue, alpha)
        glBegin(GL_TRIANGLE_FAN)

        val radiusD = radius.toDouble()

        // Draw corners
        val corners = arrayOf(
            Triple(newX2 - radiusD, newY2 - radiusD, 0.0),
            Triple(newX2 - radiusD, newY1 + radiusD, 90.0),
            Triple(newX1 + radiusD, newY1 + radiusD, 180.0),
            Triple(newX1 + radiusD, newY2 - radiusD, 270.0)
        )

        for ((cx, cy, startAngle) in corners) {
            for (i in 0..90 step 10) {
                val angle = Math.toRadians(startAngle + i)
                val x = cx + radiusD * sin(angle)
                val y = cy + radiusD * cos(angle)
                glVertex2d(x, y)
            }
        }

        glEnd()

        glColor4f(0f, 0f, 0f, 1f)

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
    }

    private fun orderPoints(x1: Float, y1: Float, x2: Float, y2: Float): FloatArray {
        val newX1 = min(x1, x2)
        val newY1 = min(y1, y2)
        val newX2 = max(x1, x2)
        val newY2 = max(y1, y2)
        return floatArrayOf(newX1, newY1, newX2, newY2)
    }

    fun drawCircle(x: Float, y: Float, radius: Float, start: Int, end: Int) {
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor(Color.WHITE)
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(2f)
        glBegin(GL_LINE_STRIP)
        var i = end.toFloat()
        while (i >= start) {
            val rad = i.toRadians()
            glVertex2f(
                x + cos(rad) * (radius * 1.001f),
                y + sin(rad) * (radius * 1.001f)
            )
            i -= 360 / 90f
        }
        glEnd()
        glDisable(GL_LINE_SMOOTH)
        enableTexture2D()
        disableBlend()
    }

    fun drawFilledCircle(xx: Int, yy: Int, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float
        glPushAttrib(GL_ENABLE_BIT)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)
        for (i in 0 until sections) {
            x = (radius * sin(i * dAngle)).toFloat()
            y = (radius * cos(i * dAngle)).toFloat()
            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            glVertex2f(xx + x, yy + y)
        }
        resetColor()
        glEnd()
        glPopAttrib()
    }

    fun customRotatedObject2D(oXpos: Float, oYpos: Float, oWidth: Float, oHeight: Float, rotate: Double) {
        translate((oXpos + oWidth / 2).toDouble(), (oYpos + oHeight / 2).toDouble(), 0.0)
        glRotated(rotate, 0.0, 0.0, 1.0)
        translate(-(oXpos + oWidth / 2).toDouble(), -(oYpos + oHeight / 2).toDouble(), 0.0)
    }

    fun setupOrientationMatrix(x: Double, y: Double, z: Double) {
        translate(x - mc.renderManager.viewerPosX, y - mc.renderManager.viewerPosY, z - mc.renderManager.viewerPosZ)
    }

    fun setupDrawCircles(render: Runnable) {
        val lightingEnabled = glIsEnabled(GL_LIGHTING)
        pushMatrix()
        enableBlend()
        enableAlpha()
        alphaFunc(GL_GREATER, 0f)
        depthMask(false)
        disableCull()
        if (lightingEnabled) disableLighting()
        shadeModel(GL_SMOOTH)

        blendFunc(770, 1)
        setupOrientationMatrix(0.0, 0.0, 0.0)
        render.run()
        blendFunc(770, 771)
        color(1f, 1f, 1f)
        shadeModel(GL_FLAT)
        if (lightingEnabled) enableLighting()
        enableCull()
        depthMask(true)
        alphaFunc(GL_GREATER, .1f)
        enableAlpha()
        popMatrix()
    }

    fun drawImage(image: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDepthMask(false)
        GL14.glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        resetColor()
        mc.textureManager.bindTexture(image)
        drawModalRectWithCustomSizedTexture(
            x.toFloat(),
            y.toFloat(),
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            width.toFloat(),
            height.toFloat()
        )
        glDepthMask(true)
        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    /**
     * Draws a textured rectangle at z = 0. Args: x, y, u, v, width, height, textureWidth, textureHeight
     */
    fun drawModalRectWithCustomSizedTexture(
        x: Float,
        y: Float,
        u: Float,
        v: Float,
        width: Float,
        height: Float,
        textureWidth: Float,
        textureHeight: Float
    ) {
        val f = 1f / textureWidth
        val f1 = 1f / textureHeight
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), ((v + height) * f1).toDouble()).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + width) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    /**
     * Draws a textured rectangle at the stored z-value. Args: x, y, u, v, width, height.
     */
    fun drawTexturedModalRect(x: Int, y: Int, textureX: Int, textureY: Int, width: Int, height: Int, zLevel: Float) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldrenderer.pos(x.toDouble(), (y + height).toDouble(), zLevel.toDouble()).tex(
            (textureX.toFloat() * f).toDouble(),
            ((textureY + height).toFloat() * f1).toDouble()
        ).endVertex()
        worldrenderer.pos((x + width).toDouble(), (y + height).toDouble(), zLevel.toDouble()).tex(
            ((textureX + width).toFloat() * f).toDouble(),
            ((textureY + height).toFloat() * f1).toDouble()
        ).endVertex()
        worldrenderer.pos((x + width).toDouble(), y.toDouble(), zLevel.toDouble()).tex(
            ((textureX + width).toFloat() * f).toDouble(),
            (textureY.toFloat() * f1).toDouble()
        ).endVertex()
        worldrenderer.pos(x.toDouble(), y.toDouble(), zLevel.toDouble()).tex(
            (textureX.toFloat() * f).toDouble(),
            (textureY.toFloat() * f1).toDouble()
        ).endVertex()
        tessellator.draw()
    }

    fun glColor(red: Int, green: Int, blue: Int, alpha: Int) =
        glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f)

    fun glFloatColor(color: Color, alpha: Float) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f

        color(red, green, blue, alpha)
    }

    /**
     * Gl color.
     *
     * @param hex the hex
     */
    @JvmStatic
    fun glHexColor(hex: Int) {
        val alpha = (hex shr 24 and 0xFF) / 255f
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f

        color(red, green, blue, alpha)
    }

    fun glColor(hex: Int, alpha: Float) {
        val red = (hex shr 16 and 0xFF) / 255f
        val green = (hex shr 8 and 0xFF) / 255f
        val blue = (hex and 0xFF) / 255f

        color(red, green, blue, alpha / 255f)
    }

    fun glColor(color: Color) = glColor(color.red, color.green, color.blue, color.alpha)

    private fun glColor(hex: Int) =
        glColor(hex shr 16 and 0xFF, hex shr 8 and 0xFF, hex and 0xFF, hex shr 24 and 0xFF)

    fun draw2D(entity: EntityLivingBase, posX: Double, posY: Double, posZ: Double, color: Int, backgroundColor: Int) {
        glPushMatrix()
        glTranslated(posX, posY, posZ)
        glRotated(-mc.renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
        glScaled(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        glTranslated(0.0, 21 + -(entity.entityBoundingBox.maxY - entity.entityBoundingBox.minY) * 12, 0.0)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    fun draw2D(blockPos: BlockPos, color: Int, backgroundColor: Int) {
        val renderManager = mc.renderManager
        val posX = blockPos.x + 0.5 - renderManager.renderPosX
        val posY = blockPos.y - renderManager.renderPosY
        val posZ = blockPos.z + 0.5 - renderManager.renderPosZ
        glPushMatrix()
        glTranslated(posX, posY, posZ)
        glRotated(-mc.renderManager.playerViewY.toDouble(), 0.0, 1.0, 0.0)
        glScaled(-0.1, -0.1, 0.1)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(true)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[0])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[1])
        glTranslated(0.0, 9.0, 0.0)
        glColor(color)
        glCallList(DISPLAY_LISTS_2D[2])
        glColor(backgroundColor)
        glCallList(DISPLAY_LISTS_2D[3])

        // Stop render
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glPopMatrix()
    }

    fun renderNameTag(string: String, x: Double, y: Double, z: Double) {
        val renderManager = mc.renderManager
        glPushMatrix()
        glTranslated(x - renderManager.renderPosX, y - renderManager.renderPosY, z - renderManager.renderPosZ)
        glNormal3f(0f, 1f, 0f)
        glRotatef(-mc.renderManager.playerViewY, 0f, 1f, 0f)
        glRotatef(mc.renderManager.playerViewX, 1f, 0f, 0f)
        glScalef(-0.05f, -0.05f, 0.05f)
        setGlCap(GL_LIGHTING, false)
        setGlCap(GL_DEPTH_TEST, false)
        setGlCap(GL_BLEND, true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        val width = Fonts.font35.getStringWidth(string) / 2
        drawRect(-width - 1, -1, width + 1, Fonts.font35.FONT_HEIGHT, Int.MIN_VALUE)
        Fonts.font35.drawString(string, -width.toFloat(), 1.5f, Color.WHITE.rgb, true)
        resetCaps()
        resetColor()
        glPopMatrix()
    }

    fun drawLine(x: Double, y: Double, x1: Double, y1: Double, width: Float) {
        glDisable(GL_TEXTURE_2D)
        glLineWidth(width)
        glBegin(GL_LINES)
        glVertex2d(x, y)
        glVertex2d(x1, y1)
        glEnd()
        glEnable(GL_TEXTURE_2D)
    }

    fun makeScissorBox(x: Float, y: Float, x2: Float, y2: Float) {
        val scaledResolution = ScaledResolution(mc)
        val factor = scaledResolution.scaleFactor
        glScissor(
            (x * factor).toInt(),
            ((scaledResolution.scaledHeight - y2) * factor).toInt(),
            ((x2 - x) * factor).toInt(),
            ((y2 - y) * factor).toInt()
        )
    }

    /**
     * GL CAP MANAGER
     *
     *
     * TODO: Remove gl cap manager and replace by something better
     */

    fun resetCaps() = glCapMap.forEach { (cap, state) -> setGlState(cap, state) }

    fun enableGlCap(cap: Int) = setGlCap(cap, true)

    fun enableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, true)
    }

    fun disableGlCap(cap: Int) = setGlCap(cap, true)

    fun disableGlCap(vararg caps: Int) {
        for (cap in caps) setGlCap(cap, false)
    }

    fun setGlCap(cap: Int, state: Boolean) {
        glCapMap[cap] = glGetBoolean(cap)
        setGlState(cap, state)
    }

    fun setGlState(cap: Int, state: Boolean) = if (state) glEnable(cap) else glDisable(cap)

    fun drawScaledCustomSizeModalRect(
        x: Int,
        y: Int,
        u: Float,
        v: Float,
        uWidth: Int,
        vHeight: Int,
        width: Int,
        height: Int,
        tileWidth: Float,
        tileHeight: Float
    ) {
        val f = 1f / tileWidth
        val f1 = 1f / tileHeight
        val tessellator = Tessellator.getInstance()
        val worldRenderer = tessellator.worldRenderer
        worldRenderer.begin(7, DefaultVertexFormats.POSITION_TEX)
        worldRenderer.pos(x.toDouble(), (y + height).toDouble(), 0.0)
            .tex((u * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldRenderer.pos((x + width).toDouble(), (y + height).toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), ((v + vHeight.toFloat()) * f1).toDouble()).endVertex()
        worldRenderer.pos((x + width).toDouble(), y.toDouble(), 0.0)
            .tex(((u + uWidth.toFloat()) * f).toDouble(), (v * f1).toDouble()).endVertex()
        worldRenderer.pos(x.toDouble(), y.toDouble(), 0.0).tex((u * f).toDouble(), (v * f1).toDouble()).endVertex()
        tessellator.draw()
    }

    /**
     * Draws a gradient-filled rectangle using float coordinates.
     *
     * @param left       the x-coordinate of the rectangle's left edge
     * @param top        the y-coordinate of the rectangle's top edge
     * @param right      the x-coordinate of the rectangle's right edge
     * @param bottom     the y-coordinate of the rectangle's bottom edge
     * @param startColor the starting color of the gradient (ARGB format)
     * @param endColor   the ending color of the gradient (ARGB format)
     */
    fun drawFloatGradientRect(left: Float, top: Float, right: Float, bottom: Float, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 0xFF) / 255.0f
        val f2 = (startColor shr 16 and 0xFF) / 255.0f
        val f3 = (startColor shr 8 and 0xFF) / 255.0f
        val f4 = (startColor and 0xFF) / 255.0f
        val f5 = (endColor shr 24 and 0xFF) / 255.0f
        val f6 = (endColor shr 16 and 0xFF) / 255.0f
        val f7 = (endColor shr 8 and 0xFF) / 255.0f
        val f8 = (endColor and 0xFF) / 255.0f
        disableTexture2D()
        enableBlend()
        disableAlpha()
        tryBlendFuncSeparate(770, 771, 1, 0)
        shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), 0.0).color(f2, f3, f4, f).endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), 0.0).color(f2, f3, f4, f).endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), 0.0).color(f6, f7, f8, f5).endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), 0.0).color(f6, f7, f8, f5).endVertex()
        tessellator.draw()
        shadeModel(7424)
        disableBlend()
        enableAlpha()
        enableTexture2D()
    }

    /**
     * Fast rounded rect.
     *
     * @param paramXStart the param x start
     * @param paramYStart the param y start
     * @param paramXEnd   the param x end
     * @param paramYEnd   the param y end
     * @param radius      the radius
     */
    fun fastRoundedRect(paramXStart: Float, paramYStart: Float, paramXEnd: Float, paramYEnd: Float, radius: Float) {
        var paramXStarts = paramXStart
        var paramYStarts = paramYStart
        var paramXEnds = paramXEnd
        var paramYEnds = paramYEnd
        var z: Float
        if (paramXStarts > paramXEnds) {
            z = paramXStarts
            paramXStarts = paramXEnds
            paramXEnds = z
        }

        if (paramYStarts > paramYEnds) {
            z = paramYStarts
            paramYStarts = paramYEnds
            paramYEnds = z
        }

        val x1 = (paramXStarts + radius).toDouble()
        val y1 = (paramYStarts + radius).toDouble()
        val x2 = (paramXEnds - radius).toDouble()
        val y2 = (paramYEnds - radius).toDouble()

        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)

        glBegin(GL_POLYGON)

        val degree = Math.PI / 180
        run {
            var i = 0.0
            while (i <= 90) {
                glVertex2d(x2 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
                i += 1.0
            }
        }
        run {
            var i = 90.0
            while (i <= 180) {
                glVertex2d(
                    x2 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        run {
            var i = 180.0
            while (i <= 270) {
                glVertex2d(
                    x1 + sin(i * degree) * radius,
                    y1 + cos(i * degree) * radius
                )
                i += 1.0
            }
        }
        var i = 270.0
        while (i <= 360) {
            glVertex2d(x1 + sin(i * degree) * radius, y2 + cos(i * degree) * radius)
            i += 1.0
        }
        glEnd()
        glDisable(GL_LINE_SMOOTH)
    }


    /**
     * Draws a gradient-filled rectangle with rounded corners using float coordinates.
     *
     * @param left       the x-coordinate of the rectangle's left edge
     * @param top        the y-coordinate of the rectangle's top edge
     * @param right      the x-coordinate of the rectangle's right edge
     * @param bottom     the y-coordinate of the rectangle's bottom edge
     * @param radius     the radius of the rounded corners
     * @param startColor the starting color of the gradient
     * @param endColor   the ending color of the gradient
     */
    fun drawFloatGradientRoundedRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Int,
        startColor: Int,
        endColor: Int
    ) {
        Stencil.write(false)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        fastRoundedRect(left, top, right, bottom, radius.toFloat())
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        Stencil.erase(true)
        drawFloatGradientRect(left, top, right, bottom, startColor, endColor)
        Stencil.dispose()
    }

    /**
     * Draws a rounded rectangle with equal rounded corners on all sides.
     *
     * @param x         The x-coordinate of the top-left corner.
     * @param y         The y-coordinate of the top-left corner.
     * @param width     The width of the rounded rectangle.
     * @param height    The height of the rounded rectangle.
     * @param radius    The radius of the rounded corners.
     * @param color     The color of the rounded rectangle.
     */
    @JvmStatic
    fun drawCustomShapeWithRadius(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
        drawCustomShapeWithRadiusRetangle(x, y, width, height, radius, color,
            leftTop = true,
            leftBottom = true,
            rightBottom = true,
            rightTop = true
        )
    }

    fun color(color: Int, alpha: Float) {
        val r = (color shr 16 and 0xFF) / 255.0f
        val g = (color shr 8 and 0xFF) / 255.0f
        val b = (color and 0xFF) / 255.0f
        color(r, g, b, alpha)
    }

    fun color(color: Int) {
        color(color, (color shr 24 and 0xFF) / 255.0f)
    }

    /**
     * Draws a rounded rectangle with customizability for rounded corners.
     *
     * @param x             The x-coordinate of the top-left corner.
     * @param y             The y-coordinate of the top-left corner.
     * @param width         The width of the rounded rectangle.
     * @param height        The height of the rounded rectangle.
     * @param radius        The radius of the rounded corners.
     * @param leftTop       If true, the top-left corner will be rounded.
     * @param leftBottom    If true, the bottom-left corner will be rounded.
     * @param rightBottom   If true, the bottom-right corner will be rounded.
     * @param rightTop      If true, the top-right corner will be rounded.
     */
    fun drawCustomShapeWithRadiusRetangle(
        x: Float,
        y: Float,
        width: Float,
        height: Float,
        radius: Float,
        c: Color,
        leftTop: Boolean,
        leftBottom: Boolean,
        rightBottom: Boolean,
        rightTop: Boolean
    ) {
        var xs = x
        var ys = y
        var widths = width
        var heights = height
        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        enableBlend()
        xs *= 2.0.toFloat()
        ys *= 2.0.toFloat()
        widths *= 2.0.toFloat()
        heights *= 2.0.toFloat()
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_LINE_SMOOTH)
        ColorUtils.clearColor()
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_POLYGON)
        ColorUtils.setColor(c.rgb)
        var i: Int

        if (leftTop) {
            i = 0
            while (i <= 90) {
                glVertex2d(
                    xs + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                    ys + radius + cos(i * Math.PI / 180.0) * radius * -1.0
                )
                i += 3
            }
        } else glVertex2d(xs.toDouble(), ys.toDouble())

        if (leftBottom) {
            i = 90
            while (i <= 180) {
                glVertex2d(
                    xs + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                    ys + heights - radius + cos(i * Math.PI / 180.0) * radius * -1.0
                )
                i += 3
            }
        } else glVertex2d(xs.toDouble(), (ys + heights).toDouble())

        if (rightBottom) {
            i = 0
            while (i <= 90) {
                glVertex2d(
                    xs + widths - radius + sin(i * Math.PI / 180.0) * radius,
                    ys + heights - radius + cos(i * Math.PI / 180.0) * radius
                )
                i += 3
            }
        } else glVertex2d((xs + widths).toDouble(), (ys + heights).toDouble())

        if (rightTop) {
            i = 90
            while (i <= 180) {
                glVertex2d(
                    xs + widths - radius + sin(i * Math.PI / 180.0) * radius,
                    ys + radius + cos(i * Math.PI / 180.0) * radius
                )
                i += 3
            }
        } else glVertex2d((xs + widths).toDouble(), ys.toDouble())

        glEnd()
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_LINE_SMOOTH)
        disableBlend()
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()
        ColorUtils.clearColor()
    }

    /**
     * Draws a rounded outline with specified parameters.
     *
     * @param x      The x-coordinate of the top-left corner.
     * @param y      The y-coordinate of the top-left corner.
     * @param x2     The x-coordinate of the bottom-right corner.
     * @param y2     The y-coordinate of the bottom-right corner.
     * @param radius The radius of the rounded corners.
     * @param width  The width of the outline.
     * @param color  The color of the outline in RGBA format.
     */
    @JvmStatic
    fun drawRoundOutline(x: Int, y: Int, x2: Int, y2: Int, radius: Float, width: Float, color: Int) {
        val f1 = (color shr 24 and 0xFF) / 255.0f
        val f2 = (color shr 16 and 0xFF) / 255.0f
        val f3 = (color shr 8 and 0xFF) / 255.0f
        val f4 = (color and 0xFF) / 255.0f
        glColor4f(f2, f3, f4, f1)
        drawRoundedShapeOutline(x.toFloat(), y.toFloat(), x2.toFloat(), y2.toFloat(), radius, width)
    }

    /**
     * Draws an outlined shape with rounded corners using specified parameters.
     *
     * @param x      The starting x-coordinate of the outline.
     * @param y      The starting y-coordinate of the outline.
     * @param x2     The ending x-coordinate of the outline.
     * @param y2     The ending y-coordinate of the outline.
     * @param radius The radius of the rounded corners.
     * @param width  The width of the outline.
     */
    fun drawRoundedShapeOutline(x: Float, y: Float, x2: Float, y2: Float, radius: Float, width: Float) {
        val segments = 18 // Number of segments to approximate the rounded corners

        // Disable unnecessary features and enable needed ones
        disableTexture2D()
        enableBlend()
        disableCull()
        enableColorMaterial()
        blendFunc(770, 771)
        tryBlendFuncSeparate(770, 771, 1, 0)

        // Set line width if it's not the default value
        if (width != 1.0f) {
            glLineWidth(width)
        }

        // Draw the straight edges of the outline
        glBegin(GL_LINES)

        // Bottom edge
        glVertex2f(x + radius, y)
        glVertex2f(x2 - radius, y)

        // Right edge
        glVertex2f(x2, y + radius)
        glVertex2f(x2, y2 - radius)

        // Top edge
        glVertex2f(x2 - radius, y2)
        glVertex2f(x + radius, y2)

        // Left edge
        glVertex2f(x, y2 - radius)
        glVertex2f(x, y + radius)

        glEnd()

        // Draw the rounded corners
        drawRoundedCorner(x + radius, y + radius, radius, 270f, 360f, segments)
        drawRoundedCorner(x2 - radius, y + radius, radius, 0f, 90f, segments)
        drawRoundedCorner(x2 - radius, y2 - radius, radius, 90f, 180f, segments)
        drawRoundedCorner(x + radius, y2 - radius, radius, 180f, 270f, segments)

        // Reset line width if changed
        if (width != 1.0f) {
            glLineWidth(1.0f)
        }

        // Restore OpenGL state
        enableCull()
        disableBlend()
        disableColorMaterial()
        enableTexture2D()
    }

    /**
     * Draws a rounded corner with the given parameters.
     *
     * @param cx      Center x-coordinate of the corner
     * @param cy      Center y-coordinate of the corner
     * @param radius  Radius of the corner
     * @param start   Starting angle (degrees) of the arc
     * @param end     Ending angle (degrees) of the arc
     * @param segments Number of segments to approximate the arc
     */
    @SuppressWarnings("unchecked")
    private fun drawRoundedCorner(cx: Float, cy: Float, radius: Float, start: Float, end: Float, segments: Int) {
        val angleStep = (end - start) / segments
        glBegin(GL_LINE_STRIP)

        for (i in 0..segments) {
            val angle = Math.toRadians((start + i * angleStep).toDouble()).toFloat()
            val x = cx + radius * cos(angle.toDouble()).toFloat()
            val y = cy + radius * sin(angle.toDouble()).toFloat()
            glVertex2f(x, y)
        }

        glEnd()
    }

    /**
     * Draw rect.
     *
     * @param left   the left
     * @param top    the top
     * @param right  the right
     * @param bottom the bottom
     * @param color  the color
     */
    @SuppressWarnings("unchecked")
    fun drawRect(left: Double, top: Double, right: Double, bottom: Double, color: Int) {
        var lefts = left
        var tops = top
        var rights = right
        var bottoms = bottom
        if (lefts < rights) {
            val i = lefts
            lefts = rights
            rights = i
        }

        if (tops < bottoms) {
            val j = tops
            tops = bottoms
            bottoms = j
        }

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(lefts, bottoms, 0.0).endVertex()
        worldrenderer.pos(rights, bottoms, 0.0).endVertex()
        worldrenderer.pos(rights, tops, 0.0).endVertex()
        worldrenderer.pos(lefts, tops, 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    /**
     * Draws a rectangle with rounded corners at the specified coordinates and dimensions.
     *
     * @param x      The x-coordinate of the top-left corner of the rectangle.
     * @param y      The y-coordinate of the top-left corner of the rectangle.
     * @param x1     The x-coordinate of the bottom-right corner of the rectangle.
     * @param y1     The y-coordinate of the bottom-right corner of the rectangle.
     * @param radius The radius of the rounded corners.
     * @param color  The color of the rectangle in integer representation (e.g., 0xFF0000 for red).
     */
    fun drawRoundedCornerRect(x: Float, y: Float, x1: Float, y1: Float, radius: Float, color: Int) {
        glEnable(GL_BLEND) // Enable blending for smooth alpha transitions
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA) // Set blending function for transparency
        glDisable(GL_TEXTURE_2D) // Disable textures for pure color rendering
        val hasCull = glIsEnabled(GL_CULL_FACE) // Check if face culling is enabled
        glDisable(GL_CULL_FACE) // Disable face culling for full visibility of rounded corners

        glColor(color) // Set the color of the rectangle

        // Draw the rounded corner rectangle
        drawRoundedCornerRectWithOpenGL(x, y, x1, y1, radius)

        glEnable(GL_TEXTURE_2D) // Re-enable textures for subsequent rendering
        glDisable(GL_BLEND) // Disable blending to restore default rendering
        setGlState(GL_CULL_FACE, hasCull) // Restore the state of face culling
    }

    /**
     * Draws a rectangle with rounded corners using OpenGL.
     *
     * @param x      The x-coordinate of the top-left corner of the rectangle.
     * @param y      The y-coordinate of the top-left corner of the rectangle.
     * @param x1     The x-coordinate of the bottom-right corner of the rectangle.
     * @param y1     The y-coordinate of the bottom-right corner of the rectangle.
     * @param radius The radius of the rounded corners.
     */
    fun drawRoundedCornerRectWithOpenGL(x: Float, y: Float, x1: Float, y1: Float, radius: Float) {
        glBegin(GL_POLYGON) // Begin drawing a filled polygon (to create rounded corners)

        // Calculate the actual radius to use (limited by rectangle dimensions)
        val xRadius = min((x1 - x) * 0.5, radius.toDouble()).toFloat()
        val yRadius = min((y1 - y) * 0.5, radius.toDouble()).toFloat()

        // Draw each rounded corner using quickPolygonCircle method
        quickPolygonCircle(x + xRadius, y + yRadius, xRadius, yRadius, 180, 270) // Top-left corner
        quickPolygonCircle(x1 - xRadius, y + yRadius, xRadius, yRadius, 90, 180) // Top-right corner
        quickPolygonCircle(x1 - xRadius, y1 - yRadius, xRadius, yRadius, 0, 90) // Bottom-right corner
        quickPolygonCircle(x + xRadius, y1 - yRadius, xRadius, yRadius, 270, 360) // Bottom-left corner

        glEnd() // End drawing the polygon
    }

    fun drawRoundedGradientOutlineCorner(x: Float, y: Float, x1: Float, y1: Float, width: Float, radius: Float, color: Int, color2: Int) {
        var xs = x
        var ys = y
        var x1s = x1
        var y1s = y1
        setColour(-1)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)

        glPushAttrib(0)
        glScaled(0.5, 0.5, 0.5)
        xs *= 2.0f
        ys *= 2.0f
        x1s *= 2.0f
        y1s *= 2.0f
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        setColour(color)
        glEnable(GL_LINE_SMOOTH)
        glShadeModel(GL_SMOOTH)
        glLineWidth(width)
        glBegin(GL_LINE_LOOP)
        var i = 0
        while (i <= 90) {
            glVertex2d(
                xs + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                ys + radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color)
        i = 90
        while (i <= 180) {
            glVertex2d(
                xs + radius + sin(i * Math.PI / 180.0) * radius * -1.0,
                y1s - radius + cos(i * Math.PI / 180.0) * radius * -1.0
            )
            i += 3
        }
        setColour(color2)
        i = 0
        while (i <= 90) {
            glVertex2d(x1s - radius + sin(i * Math.PI / 180.0) * radius, y1s - radius + cos(i * Math.PI / 180.0) * radius)
            i += 3
        }
        setColour(color2)
        i = 90
        while (i <= 180) {
            glVertex2d(
                x1s - radius + sin(i * Math.PI / 180.0) * radius,
                ys + radius + cos(i * Math.PI / 180.0) * radius
            )
            i += 3
        }
        glEnd()
        glLineWidth(1f)
        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        glScaled(2.0, 2.0, 2.0)
        glPopAttrib()

        glEnable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)
        glShadeModel(GL_FLAT)
        setColour(-1)
    }

    /**
     * Draws a portion of a circle using OpenGL, approximating it with a polygon.
     *
     * @param x       The x-coordinate of the center of the circle.
     * @param y       The y-coordinate of the center of the circle.
     * @param xRadius The radius of the circle along the x-axis.
     * @param yRadius The radius of the circle along the y-axis.
     * @param start   The starting angle of the arc in degrees (0 degrees is to the right, increasing counter-clockwise).
     * @param end     The ending angle of the arc in degrees.
     */
    private fun quickPolygonCircle(x: Float, y: Float, xRadius: Float, yRadius: Float, start: Int, end: Int) {
        var i = end
        while (i >= start) {
            glVertex2d(x + sin(Math.toRadians(i.toDouble())) * xRadius, y + cos(Math.toRadians(i.toDouble())) * yRadius)
            i -= 4
        }
    }

    /**
     * Draws an axis-aligned bounding box (AABB) with the specified parameters.
     *
     * @param axisAlignedBB The axis-aligned bounding box to be drawn.
     * @param color         The color of the bounding box.
     * @param outline       Whether to draw the outline of the bounding box.
     * @param box           Whether to draw the filled box of the bounding box.
     * @param outlineWidth  The width of the outline if drawn.
     */
    fun drawOutlineAxisAlignedBB(
        axisAlignedBB: AxisAlignedBB?,
        color: Color,
        outline: Boolean,
        box: Boolean,
        outlineWidth: Float
    ) {
        // Set up OpenGL states for drawing
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glLineWidth(outlineWidth)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glColor(color)

        // Draw outline if specified
        if (outline) {
            glLineWidth(outlineWidth)
            enableGlCap(GL_LINE_SMOOTH)
            // Set outline color with alpha
            glColor(color.red, color.green, color.blue, 95)
            drawSelectionBoundingBox(axisAlignedBB!!)
        }

        // Draw filled box if specified
        if (box) {
            // Set filled box color with alpha, different alpha if outline is also drawn
            glColor(color.red, color.green, color.blue, if (outline) 26 else 35)
            drawFilledBox(axisAlignedBB!!)
        }

        // Reset OpenGL states
        resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
    }

    /**
     * Draws a shadow effect around a rectangular area using textured rectangles.
     *
     * @param x      The x-coordinate of the top-left corner of the rectangular area.
     * @param y      The y-coordinate of the top-left corner of the rectangular area.
     * @param width  The width of the rectangular area.
     * @param height The height of the rectangular area.
     */
    fun drawShadow(x: Float, y: Float, width: Float, height: Float) {
        drawTexturedRect(x - 9, y - 9, 9F, 9F, "paneltopleft")
        drawTexturedRect(x - 9, y + height, 9F, 9F, "panelbottomleft")
        drawTexturedRect(x + width, y + height, 9F, 9F, "panelbottomright")
        drawTexturedRect(x + width, y - 9, 9F, 9F, "paneltopright")
        drawTexturedRect(x - 9, y, 9F, height, "panelleft")
        drawTexturedRect(x + width, y, 9F, height, "panelright")
        drawTexturedRect(x, y - 9, width, 9F, "paneltop")
        drawTexturedRect(x, y + height, width, 9F, "panelbottom")
    }

    /**
     * Draw filled circle.
     *
     * @param xx     the xx
     * @param yy     the yy
     * @param radius the radius
     * @param color  the color
     */
    fun drawFilledForCircle(xx: Float, yy: Float, radius: Float, color: Color) {
        val sections = 50
        val dAngle = 2 * Math.PI / sections
        var x: Float
        var y: Float

        glPushAttrib(GL_ENABLE_BIT)

        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_LINE_SMOOTH)
        glBegin(GL_TRIANGLE_FAN)

        for (i in 0 until sections) {
            x = (radius * sin((i * dAngle))).toFloat()
            y = (radius * cos((i * dAngle))).toFloat()

            glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
            glVertex2f(xx + x, yy + y)
        }

        color(0f, 0f, 0f)

        glEnd()

        glPopAttrib()
    }

    /**
     * Draw gradient sideways.
     *
     * @param left   the left
     * @param top    the top
     * @param right  the right
     * @param bottom the bottom
     * @param col1   the col 1
     * @param col2   the col 2
     */
    fun drawGradientSideways(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val f = (col1 shr 24 and 0xFF) / 255.0f
        val f2 = (col1 shr 16 and 0xFF) / 255.0f
        val f3 = (col1 shr 8 and 0xFF) / 255.0f
        val f4 = (col1 and 0xFF) / 255.0f
        val f5 = (col2 shr 24 and 0xFF) / 255.0f
        val f6 = (col2 shr 16 and 0xFF) / 255.0f
        val f7 = (col2 shr 8 and 0xFF) / 255.0f
        val f8 = (col2 and 0xFF) / 255.0f
        glEnable(3042)
        glDisable(3553)
        glBlendFunc(770, 771)
        glEnable(2848)
        glShadeModel(7425)
        glPushMatrix()
        glBegin(7)
        glColor4f(f2, f3, f4, f)
        glVertex2d(left, top)
        glVertex2d(left, bottom)
        glColor4f(f6, f7, f8, f5)
        glVertex2d(right, bottom)
        glVertex2d(right, top)
        glEnd()
        glPopMatrix()
        glEnable(3553)
        glDisable(3042)
        glDisable(2848)
        glShadeModel(7424)
    }

    /**
     * Calculates a color at a given offset between two colors using linear interpolation (gradient).
     *
     * @param color1 The starting color of the gradient.
     * @param color2 The ending color of the gradient.
     * @param offset The offset value between 0.0 and 1.0, where 0.0 represents color1 and 1.0 represents color2.
     * Values outside this range are wrapped around (e.g., offset 1.5 becomes 0.5).
     * @return The interpolated color at the given offset.
     */
    fun getGradientOffset(color1: Color, color2: Color, offset: Double): Color {
        var offsets = offset
        val redPart: Int

        // Wrap offset if it's greater than 1.0
        if (offsets > 1.0) {
            val fractionalPart = offsets % 1.0
            val integerPart = offsets.toInt()
            offsets = if (integerPart % 2 == 0) fractionalPart else 1.0 - fractionalPart
        }

        // Calculate inverse percentage
        val inversePercent = 1.0 - offsets

        // Interpolate RGB components
        redPart = (color1.red.toDouble() * inversePercent + color2.red.toDouble() * offsets).toInt()
        val greenPart = (color1.green.toDouble() * inversePercent + color2.green.toDouble() * offsets).toInt()
        val bluePart = (color1.blue.toDouble() * inversePercent + color2.blue.toDouble() * offsets).toInt()

        // Return the interpolated color
        return Color(redPart, greenPart, bluePart)
    }

    @JvmStatic
    fun renderOne(lineWidth: Float) {
        checkSetupFBO()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glLineWidth(lineWidth)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)
        glClearStencil(0xF)
        glStencilFunc(GL_NEVER, 1, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    @JvmStatic
    fun renderTwo() {
        glStencilFunc(GL_NEVER, 0, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    }

    @JvmStatic
    fun renderThree() {
        glStencilFunc(GL_EQUAL, 1, 0xF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    @JvmStatic
    fun renderFour(color: Color) {
        setColor(color)
        glDepthMask(false)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_POLYGON_OFFSET_LINE)
        glPolygonOffset(1f, -2000000f)
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240f, 240f)
    }

    @JvmStatic
    fun renderFive() {
        glPolygonOffset(1f, 2000000f)
        glDisable(GL_POLYGON_OFFSET_LINE)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_STENCIL_TEST)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glEnable(GL_BLEND)
        glEnable(GL_LIGHTING)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_ALPHA_TEST)
        glPopAttrib()
    }

    @JvmStatic
    fun setColor(color: Color) {
        glColor4d(
            (color.red / 255f).toDouble(),
            (color.green / 255f).toDouble(),
            (color.blue / 255f).toDouble(),
            (color.alpha / 255f).toDouble()
        )
    }

    @JvmStatic
    fun checkSetupFBO() {
        // Gets the FBO of Minecraft
        val fbo = mc.framebuffer

        // Check if FBO isn't null
        if (fbo != null) {
            // Checks if screen has been resized or new FBO has been created
            if (fbo.depthBuffer > -1) {
                // Sets up the FBO with depth and stencil extensions (24/8 bit)
                setupFBO(fbo)
                // Reset the ID to prevent multiple FBO's
                fbo.depthBuffer = -1
            }
        }
    }

    /**
     * Sets up the FBO with depth and stencil
     *
     * @param fbo Framebuffer
     */
    @JvmStatic
    private fun setupFBO(fbo: Framebuffer) {
        // Deletes old render buffer extensions such as depth
        // Args: Render Buffer ID
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)
        // Generates a new render buffer ID for the depth and stencil extension
        val stencil_depth_buffer_ID = EXTFramebufferObject.glGenRenderbuffersEXT()
        // Binds new render buffer by ID
        // Args: Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencil_depth_buffer_ID)
        // Adds the depth and stencil extension
        // Args: Target (GL_RENDERBUFFER_EXT), Extension (GL_DEPTH_STENCIL_EXT),
        // Width, Height
        EXTFramebufferObject.glRenderbufferStorageEXT(
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT,
            mc.displayWidth,
            mc.displayHeight
        )
        // Adds the stencil attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_STENCIL_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencil_depth_buffer_ID
        )
        // Adds the depth attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_DEPTH_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(
            EXTFramebufferObject.GL_FRAMEBUFFER_EXT,
            EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT,
            EXTFramebufferObject.GL_RENDERBUFFER_EXT,
            stencil_depth_buffer_ID
        )
    }

    /**
     * Gl color.
     *
     * @param color with alpha the color
     */
    @JvmStatic
    fun glRGBColor(color: Color, alpha: Float) {
        val red = color.red / 255f
        val green = color.green / 255f
        val blue = color.blue / 255f

        color(red, green, blue, alpha)
    }

    /**
     * Draw gradient rect.
     *
     * @param left       the left
     * @param top        the top
     * @param right      the right
     * @param bottom     the bottom
     * @param startColor the start color
     * @param endColor   the end color
     */
    fun drawGradientRect(left: Int, top: Int, right: Int, bottom: Int, startColor: Int, endColor: Int) {
        val f = (startColor shr 24 and 255).toFloat() / 255.0f
        val f1 = (startColor shr 16 and 255).toFloat() / 255.0f
        val f2 = (startColor shr 8 and 255).toFloat() / 255.0f
        val f3 = (startColor and 255).toFloat() / 255.0f
        val f4 = (endColor shr 24 and 255).toFloat() / 255.0f
        val f5 = (endColor shr 16 and 255).toFloat() / 255.0f
        val f6 = (endColor shr 8 and 255).toFloat() / 255.0f
        val f7 = (endColor and 255).toFloat() / 255.0f
        pushMatrix()
        disableTexture2D()
        enableBlend()
        disableAlpha()
        tryBlendFuncSeparate(770, 771, 1, 0)
        shadeModel(7425)
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR)
        worldrenderer.pos(right.toDouble(), top.toDouble(), ZLEVEL1.toDouble()).color(f1, f2, f3, f)
            .endVertex()
        worldrenderer.pos(left.toDouble(), top.toDouble(), ZLEVEL1.toDouble()).color(f1, f2, f3, f)
            .endVertex()
        worldrenderer.pos(left.toDouble(), bottom.toDouble(), ZLEVEL1.toDouble()).color(f5, f6, f7, f4)
            .endVertex()
        worldrenderer.pos(right.toDouble(), bottom.toDouble(), ZLEVEL1.toDouble()).color(f5, f6, f7, f4)
            .endVertex()
        tessellator.draw()
        shadeModel(7424)
        disableBlend()
        enableAlpha()
        enableTexture2D()
        popMatrix()
    }

    //TAHOMA
    private fun drawExhiOutlined(text: String, x: Float, y: Float, borderColor: Int, mainColor: Int): Float {
        Fonts.fontSmall.drawString(text, x, y - 0.35.toFloat(), borderColor)
        Fonts.fontSmall.drawString(text, x, y + 0.35.toFloat(), borderColor)
        Fonts.fontSmall.drawString(text, x - 0.35.toFloat(), y, borderColor)
        Fonts.fontSmall.drawString(text, x + 0.35.toFloat(), y, borderColor)
        Fonts.fontSmall.drawString(text, x, y, mainColor)
        return x + Fonts.fontSmall.getStringWidth(text) - 2f
    }

    private fun getBorderColor(level: Int): Int {
        if (level == 2) return 0x7055FF55
        if (level == 3) return 0x7000AAAA
        if (level == 4) return 0x70AA0000
        if (level >= 5) return 0x70FFAA00
        return 0x70FFFFFF
    }

    private fun getMainColor(level: Int): Int {
        if (level == 4) return -0x560000
        return -1
    }

    fun drawExhiEnchants(stack: ItemStack, x: Float, y: Float) {
        var ys = y
        RenderHelper.disableStandardItemLighting()
        disableDepth()
        disableBlend()
        resetColor()
        val darkBorder = -0x1000000
        if (stack.item is ItemArmor) {
            val prot = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            val thorn = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack)
            if (prot > 0) {
                drawExhiOutlined(
                    prot.toString() + "",
                    drawExhiOutlined("P", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(prot),
                    getMainColor(prot)
                )
                ys += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(unb),
                    getMainColor(unb)
                )
                ys += 4f
            }
            if (thorn > 0) {
                drawExhiOutlined(
                    thorn.toString() + "",
                    drawExhiOutlined("T", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(thorn),
                    getMainColor(thorn)
                )
                ys += 4f
            }
        }
        if (stack.item is ItemBow) {
            val power = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack)
            val punch = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack)
            val flame = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (power > 0) {
                drawExhiOutlined(
                    power.toString() + "",
                    drawExhiOutlined("Pow", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(power),
                    getMainColor(power)
                )
                ys += 4f
            }
            if (punch > 0) {
                drawExhiOutlined(
                    punch.toString() + "",
                    drawExhiOutlined("Pun", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(punch),
                    getMainColor(punch)
                )
                ys += 4f
            }
            if (flame > 0) {
                drawExhiOutlined(
                    flame.toString() + "",
                    drawExhiOutlined("F", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(flame),
                    getMainColor(flame)
                )
                ys += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(unb),
                    getMainColor(unb)
                )
                ys += 4f
            }
        }
        if (stack.item is ItemSword) {
            val sharp = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack)
            val kb = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack)
            val fire = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack)
            val unb = EnchantmentHelper.getEnchantmentLevel(Enchantment.unbreaking.effectId, stack)
            if (sharp > 0) {
                drawExhiOutlined(
                    sharp.toString() + "",
                    drawExhiOutlined("S", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(sharp),
                    getMainColor(sharp)
                )
                ys += 4f
            }
            if (kb > 0) {
                drawExhiOutlined(
                    kb.toString() + "",
                    drawExhiOutlined("K", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(kb),
                    getMainColor(kb)
                )
                ys += 4f
            }
            if (fire > 0) {
                drawExhiOutlined(
                    fire.toString() + "",
                    drawExhiOutlined("F", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(fire),
                    getMainColor(fire)
                )
                ys += 4f
            }
            if (unb > 0) {
                drawExhiOutlined(
                    unb.toString() + "",
                    drawExhiOutlined("U", x, ys, darkBorder, -1),
                    ys,
                    getBorderColor(unb),
                    getMainColor(unb)
                )
            }
        }
        enableDepth()
        RenderHelper.enableGUIStandardItemLighting()
    }

    fun drawGradientRoundedRect(
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        radius: Int,
        startColor: Int,
        endColor: Int
    ) {
        Stencil.write(false)
        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        fastRoundedRect(left, top, right, bottom, radius.toFloat())
        glDisable(GL_BLEND)
        glEnable(GL_TEXTURE_2D)
        Stencil.erase(true)
        drawGradientRect(
            left.toInt(),
            top.toInt(), right.toInt(), bottom.toInt(), startColor, endColor
        )
        Stencil.dispose()
    }

    fun drawEntityBoxESP(entity: Entity, color: Color) {
        val renderManager = mc.renderManager
        val timer = mc.timer
        pushMatrix()
        glBlendFunc(770, 771)
        enableGlCap(3042)
        disableGlCap(3553, 2929)
        glDepthMask(false)
        val x = (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * timer.renderPartialTicks
                - renderManager.renderPosX)
        val y = (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * timer.renderPartialTicks
                - renderManager.renderPosY)
        val z = (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * timer.renderPartialTicks
                - renderManager.renderPosZ)
        val entityBox = entity.entityBoundingBox
        val axisAlignedBB = AxisAlignedBB(
            entityBox.minX - entity.posX + x - 0.05,
            entityBox.minY - entity.posY + y,
            entityBox.minZ - entity.posZ + z - 0.05,
            entityBox.maxX - entity.posX + x + 0.05,
            entityBox.maxY - entity.posY + y + 0.15,
            entityBox.maxZ - entity.posZ + z + 0.05
        )
        glTranslated(x, y, z)
        glRotated(-entity.rotationYawHead.toDouble(), 0.0, 1.0, 0.0)
        glTranslated(-x, -y, -z)
        glLineWidth(3.0f)
        enableGlCap(2848)
        glColor(0, 0, 0, 255)
        RenderGlobal.drawSelectionBoundingBox(axisAlignedBB)
        glLineWidth(1.0f)
        enableGlCap(2848)
        glColor(color.red, color.green, color.blue, 255)
        RenderGlobal.drawSelectionBoundingBox(axisAlignedBB)
        resetColor()
        glDepthMask(true)
        resetCaps()
        popMatrix()
    }

    fun drawHead(skin: ResourceLocation?, x: Int, y: Int, width: Int, height: Int, color: Int) {
        mc.textureManager.bindTexture(skin)
        drawScaledCustomSizeModalRect(
            x, y, 8f, 8f, 8, 8, width, height,
            64f, 64f
        )
        drawScaledCustomSizeModalRect(
            x, y, 40f, 8f, 8, 8, width, height,
            64f, 64f
        )
    }

    fun quickDrawHead(skin: ResourceLocation?, x: Int, y: Int, width: Int, height: Int) {
        mc.textureManager.bindTexture(skin)
        drawScaledCustomSizeModalRect(
            x, y, 8f, 8f, 8, 8, width, height,
            64f, 64f
        )
        drawScaledCustomSizeModalRect(
            x, y, 40f, 8f, 8, 8, width, height,
            64f, 64f
        )
    }

    fun drawEntityOnScreen(posX: Double, posY: Double, scale: Float, entity: EntityLivingBase?) {
        pushMatrix()
        enableColorMaterial()

        translate(posX, posY, 50.0)
        scale((-scale), scale, scale)
        rotate(180f, 0f, 0f, 1f)
        rotate(135f, 0f, 1f, 0f)
        RenderHelper.enableStandardItemLighting()
        rotate(-135f, 0f, 1f, 0f)
        translate(0.0, 0.0, 0.0)

        val rendermanager = mc.renderManager
        rendermanager.setPlayerViewY(180f)
        rendermanager.isRenderShadow = false
        rendermanager.renderEntityWithPosYaw(entity, 0.0, 0.0, 0.0, 0f, 1f)
        rendermanager.isRenderShadow = true

        popMatrix()
        RenderHelper.disableStandardItemLighting()
        disableRescaleNormal()
        setActiveTexture(OpenGlHelper.lightmapTexUnit)
        disableTexture2D()
        setActiveTexture(OpenGlHelper.defaultTexUnit)
    }

    fun drawEntityOnScreen(posX: Int, posY: Int, scale: Int, entity: EntityLivingBase?) {
        drawEntityOnScreen(posX.toDouble(), posY.toDouble(), scale.toFloat(), entity)
    }

    fun interpolateColors(color1: Int, color2: Int, progress: Float): Int {
        val alpha = ((1.0 - progress) * (color1 ushr 24) + progress * (color2 ushr 24)).toInt()
        val red = ((1.0 - progress) * ((color1 shr 16) and 0xFF) + progress * ((color2 shr 16) and 0xFF)).toInt()
        val green = ((1.0 - progress) * ((color1 shr 8) and 0xFF) + progress * ((color2 shr 8) and 0xFF)).toInt()
        val blue = ((1.0 - progress) * (color1 and 0xFF) + progress * (color2 and 0xFF)).toInt()

        return (alpha shl 24) or (red shl 16) or (green shl 8) or blue
    }

    fun drawAnimatedGradient(left: Double, top: Double, right: Double, bottom: Double, col1: Int, col2: Int) {
        val currentTime = System.currentTimeMillis()
        if (startTime == 0L) {
            startTime = currentTime
        }

        val elapsedTime: Long = currentTime - startTime
        val progress: Float = (elapsedTime % animationDuration).toFloat() / animationDuration

        val color1: Int
        val color2: Int

        if (elapsedTime / animationDuration % 2 == 0L) {
            // Custom Color 1 to Custom Color 2
            color1 = interpolateColors(col1, col2, progress)
            color2 = interpolateColors(col2, col1, progress)
        } else {
            // Custom Color 2 to Custom Color 1
            color1 = interpolateColors(col2, col1, progress)
            color2 = interpolateColors(col1, col2, progress)
        }

        drawGradientSideways(left, top, right, bottom, color1, color2)

        if (elapsedTime >= 2 * animationDuration) {
            // Reset the start time to continue the loop
            startTime = currentTime
        }
    }

    fun drawRectFloat(left: Float, top: Float, right: Float, bottom: Float, color: Int) {
        var lefts = left
        var tops = top
        var rights = right
        var bottoms = bottom
        if (lefts < rights) {
            val i = lefts
            lefts = rights
            rights = i
        }

        if (tops < bottoms) {
            val j = tops
            tops = bottoms
            bottoms = j
        }

        val f3 = (color shr 24 and 255).toFloat() / 255.0f
        val f = (color shr 16 and 255).toFloat() / 255.0f
        val f1 = (color shr 8 and 255).toFloat() / 255.0f
        val f2 = (color and 255).toFloat() / 255.0f
        val tessellator = Tessellator.getInstance()
        val worldrenderer = tessellator.worldRenderer
        enableBlend()
        disableTexture2D()
        tryBlendFuncSeparate(770, 771, 1, 0)
        color(f, f1, f2, f3)
        worldrenderer.begin(7, DefaultVertexFormats.POSITION)
        worldrenderer.pos(lefts.toDouble(), bottoms.toDouble(), 0.0).endVertex()
        worldrenderer.pos(rights.toDouble(), bottoms.toDouble(), 0.0).endVertex()
        worldrenderer.pos(rights.toDouble(), tops.toDouble(), 0.0).endVertex()
        worldrenderer.pos(lefts.toDouble(), tops.toDouble(), 0.0).endVertex()
        tessellator.draw()
        enableTexture2D()
        disableBlend()
    }

    // Enable Render 2D (GL11)
    fun enableRender2D() {
        glEnable(3042)
        glDisable(2884)
        glDisable(3553)
        glEnable(2848)
        glBlendFunc(770, 771)
        glLineWidth(1.0f)
    }

    // Disable Render 2D (GL11)
    fun disableRender2D() {
        glDisable(3042)
        glEnable(2884)
        glEnable(3553)
        glDisable(2848)
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f)
        shadeModel(7424)
        disableBlend()
        enableTexture2D()
    }

    fun setGLCap(cap: Int, flag: Boolean) {
        glCapMap[cap] = glGetBoolean(cap)
        if (flag) {
            glEnable(cap)
        } else {
            glDisable(cap)
        }
    }

    private fun revertGLCap(cap: Int) {
        val origCap: Boolean = glCapMap[cap] == true
        if (origCap) {
            glEnable(cap)
        } else {
            glDisable(cap)
        }
    }

    fun revertAllCaps() {
        val localIterator: Iterator<*> = glCapMap.keys.iterator()
        while (localIterator.hasNext()) {
            val cap = localIterator.next() as Int
            revertGLCap(cap)
        }
    }
}