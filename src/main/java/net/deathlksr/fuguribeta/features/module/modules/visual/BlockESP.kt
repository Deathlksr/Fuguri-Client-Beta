/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlockName
import net.deathlksr.fuguribeta.utils.block.BlockUtils.searchBlocks
import net.deathlksr.fuguribeta.utils.render.ColorUtils.rainbow
import net.deathlksr.fuguribeta.utils.render.RenderUtils.draw2D
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawBlockBox
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.value.BlockValue
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.block.Block
import net.minecraft.init.Blocks.air
import net.minecraft.util.BlockPos
import java.awt.Color

object BlockESP : Module("BlockESP", Category.VISUAL, hideModule = false) {
    private val mode by ListValue("Mode", arrayOf("Box", "2D"), "Box")
    private val block by BlockValue("Block", 168)
    private val radius by IntegerValue("Radius", 40, 5..120)
    private val blockLimit by IntegerValue("BlockLimit", 256, 0..2056)

    private val colorRainbow by BoolValue("Rainbow", false)
        private val colorRed by IntegerValue("R", 255, 0..255) { !colorRainbow }
        private val colorGreen by IntegerValue("G", 179, 0..255) { !colorRainbow }
        private val colorBlue by IntegerValue("B", 72, 0..255) { !colorRainbow }

    private val searchTimer = MSTimer()
    private val posList = mutableListOf<BlockPos>()
    private var thread: Thread? = null

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (searchTimer.hasTimePassed(1000) && (thread?.isAlive != true)) {
            val radius = radius
            val selectedBlock = Block.getBlockById(block)
            val blockLimit = blockLimit

            if (selectedBlock == null || selectedBlock == air)
                return

            thread = Thread({
                val blocks = searchBlocks(radius, setOf(selectedBlock), blockLimit)
                searchTimer.reset()

                synchronized(posList) {
                    posList.clear()
                    posList += blocks.keys
                }
            }, "BlockESP-BlockFinder")

            thread!!.start()
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        synchronized(posList) {
            val color = if (colorRainbow) rainbow() else Color(colorRed, colorGreen, colorBlue)
            for (blockPos in posList) {
                when (mode.lowercase()) {
                    "box" -> drawBlockBox(blockPos, color, true)
                    "2d" -> draw2D(blockPos, color.rgb, Color.BLACK.rgb)
                }
            }
        }
    }

    override val tag
        get() = getBlockName(block)
}