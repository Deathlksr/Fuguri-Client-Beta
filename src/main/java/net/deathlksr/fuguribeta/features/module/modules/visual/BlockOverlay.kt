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
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.block.BlockUtils.canBeClicked
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlock
import net.deathlksr.fuguribeta.utils.extensions.component1
import net.deathlksr.fuguribeta.utils.extensions.component2
import net.deathlksr.fuguribeta.utils.render.ColorUtils.rainbow
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawBorderedRect
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawFilledBox
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawSelectionBoundingBox
import net.deathlksr.fuguribeta.utils.render.RenderUtils.glColor
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.block.Block
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager.*
import net.minecraft.util.BlockPos
import org.lwjgl.opengl.GL11.*
import java.awt.Color

object BlockOverlay : Module("BlockOverlay", Category.VISUAL, gameDetecting = false, hideModule = false) {
    val info by BoolValue("Info", false)

    private val colorRainbow by BoolValue("Rainbow", false)
        private val colorRed by IntegerValue("R", 68, 0..255) { !colorRainbow }
        private val colorGreen by IntegerValue("G", 117, 0..255) { !colorRainbow }
        private val colorBlue by IntegerValue("B", 255, 0..255) { !colorRainbow }

    val currentBlock: BlockPos?
        get() {
            val blockPos = mc.objectMouseOver?.blockPos ?: return null

            if (canBeClicked(blockPos) && mc.theWorld.worldBorder.contains(blockPos))
                return blockPos

            return null
        }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val blockPos = currentBlock ?: return

        val block = getBlock(blockPos) ?: return
        val partialTicks = event.partialTicks

        val color = if (colorRainbow) rainbow(alpha = 0.4F) else Color(colorRed,
                colorGreen, colorBlue, (0.4F * 255).toInt())

        enableBlend()
        tryBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ZERO)
        glColor(color)
        glLineWidth(2F)
        disableTexture2D()
        glDepthMask(false)

        block.setBlockBoundsBasedOnState(mc.theWorld, blockPos)


        val thePlayer = mc.thePlayer ?: return

        val x = thePlayer.lastTickPosX + (thePlayer.posX - thePlayer.lastTickPosX) * partialTicks
        val y = thePlayer.lastTickPosY + (thePlayer.posY - thePlayer.lastTickPosY) * partialTicks
        val z = thePlayer.lastTickPosZ + (thePlayer.posZ - thePlayer.lastTickPosZ) * partialTicks

        val axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, blockPos)
            .expand(0.0020000000949949026, 0.0020000000949949026, 0.0020000000949949026)
            .offset(-x, -y, -z)

        drawSelectionBoundingBox(axisAlignedBB)
        drawFilledBox(axisAlignedBB)
        glDepthMask(true)
        enableTexture2D()
        disableBlend()
        resetColor()
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (info) {
            val blockPos = currentBlock ?: return
            val block = getBlock(blockPos) ?: return

            val info = "${block.localizedName} §7ID: ${Block.getIdFromBlock(block)}"
            val (width, height) = ScaledResolution(mc)

            drawBorderedRect(
                    width / 2 - 2F,
                    height / 2 + 5F,
                    width / 2 + Fonts.font40.getStringWidth(info) + 2F,
                    height / 2 + 16F,
                    3F, Color.BLACK.rgb, Color.BLACK.rgb
            )

            resetColor()
            Fonts.font40.drawString(info, width / 2f, height / 2f + 7f, Color.WHITE.rgb, false)
        }
    }
}