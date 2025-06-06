package net.deathlksr.fuguribeta.ui.client.clickgui.style.styles

import net.deathlksr.fuguribeta.features.module.modules.client.ClickGUIModule.guiColor
import net.deathlksr.fuguribeta.features.module.modules.client.ClickGUIModule.scale
import net.deathlksr.fuguribeta.ui.client.clickgui.ClickGui.clamp
import net.deathlksr.fuguribeta.ui.client.clickgui.Panel
import net.deathlksr.fuguribeta.ui.client.clickgui.elements.ButtonElement
import net.deathlksr.fuguribeta.ui.client.clickgui.elements.ModuleElement
import net.deathlksr.fuguribeta.ui.client.clickgui.style.Style
import net.deathlksr.fuguribeta.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.deathlksr.fuguribeta.ui.font.Fonts.font35
import net.deathlksr.fuguribeta.utils.block.BlockUtils.getBlockName
import net.deathlksr.fuguribeta.utils.extensions.component1
import net.deathlksr.fuguribeta.utils.extensions.component2
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawBorderedRect
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawRect
import net.deathlksr.fuguribeta.value.*
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.StringUtils
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import java.awt.Color
import kotlin.math.roundToInt

@SideOnly(Side.CLIENT)
object NullStyle : Style() {
    private fun getNegatedColor() = guiColor.inv()
    override fun drawPanel(mouseX: Int, mouseY: Int, panel: Panel) {
        drawRect(panel.x - 3, panel.y, panel.x + panel.width + 3, panel.y + 19, guiColor)

        if (panel.fade > 0)
            drawBorderedRect(panel.x, panel.y + 19, panel.x + panel.width, panel.y + 19 + panel.fade, 1, Int.MIN_VALUE, Int.MIN_VALUE)

        val xPos = panel.x - (font35.getStringWidth("§f" + StringUtils.stripControlCodes(panel.name)) - 100) / 2
        font35.drawString(panel.name, xPos, panel.y + 7, getNegatedColor())
    }

    override fun drawHoverText(mouseX: Int, mouseY: Int, text: String) {
        val lines = text.lines()

        val width = lines.maxOfOrNull { font35.getStringWidth(it) + 14 } ?: return // Makes no sense to render empty lines
        val height = (font35.fontHeight * lines.size) + 3

        // Don't draw hover text beyond window boundaries
        val (scaledWidth, scaledHeight) = ScaledResolution(mc)
        val x = mouseX.clamp(0, (scaledWidth / scale - width).roundToInt())
        val y = mouseY.clamp(0, (scaledHeight / scale - height).roundToInt())

        drawRect(x + 9, y, x + width, y + height, guiColor)
        lines.forEachIndexed { index, text ->
            font35.drawString(text, x + 12, y + 3 + (font35.fontHeight) * index, getNegatedColor())
        }
    }

    override fun drawButtonElement(mouseX: Int, mouseY: Int, buttonElement: ButtonElement) {
        val xPos = buttonElement.x - (font35.getStringWidth(buttonElement.displayName) - 100) / 2
        font35.drawString(buttonElement.displayName, xPos, buttonElement.y + 6, buttonElement.color)
    }

    override fun drawModuleElementAndClick(mouseX: Int, mouseY: Int, moduleElement: ModuleElement, mouseButton: Int?): Boolean {
        val xPos = moduleElement.x - (font35.getStringWidth(moduleElement.displayName) - 100) / 2
        font35.drawString(
            moduleElement.displayName,
            xPos,
            moduleElement.y + 6,
            if (moduleElement.module.state) {
                if (moduleElement.module.isActive) guiColor
                // Make inactive modules have alpha set to 100
                else (guiColor and 0x00FFFFFF) or (0x64 shl 24)
            } else Int.MAX_VALUE
        )

        val moduleValues = moduleElement.module.values.filter { it.isSupported() }
        if (moduleValues.isNotEmpty()) {
            font35.drawString(
                if (moduleElement.showSettings) "-" else "+",
                moduleElement.x + moduleElement.width - 8, moduleElement.y + moduleElement.height / 2, Color.WHITE.rgb
            )

            if (moduleElement.showSettings) {
                var yPos = moduleElement.y + 4

                val minX = moduleElement.x + moduleElement.width + 4
                val maxX = moduleElement.x + moduleElement.width + moduleElement.settingsWidth

                for (value in moduleValues) {
                    assumeNonVolatile = value.get() is Number

                    when (value) {
                        is BoolValue -> {
                            val text = value.name

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            if (mouseButton == 0
                                && mouseX in minX..maxX
                                && mouseY in yPos + 2..yPos + 14
                            ) {
                                value.toggle()
                                
                                return true
                            }

                            drawRect(minX, yPos + 2, maxX, yPos + 14, Int.MIN_VALUE)

                            font35.drawString(text, minX + 2, yPos + 4,
                                if (value.get()) guiColor else Int.MAX_VALUE
                            )

                            yPos += 12
                        }
                        is ListValue -> {
                            val text = value.name

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 16

                            if (mouseButton == 0
                                && mouseX in minX..maxX
                                && mouseY in yPos + 2..yPos + 14
                            ) {
                                value.openList = !value.openList
                                
                                return true
                            }

                            drawRect(minX, yPos + 2, maxX, yPos + 14, Int.MIN_VALUE)

                            font35.drawString("§c$text", minX + 2, yPos + 4, Color.WHITE.rgb)
                            font35.drawString(
                                if (value.openList) "-" else "+",
                                (maxX - if (value.openList) 5 else 6), yPos + 4, Color.WHITE.rgb
                            )

                            yPos += 12

                            for (valueOfList in value.values) {
                                moduleElement.settingsWidth = font35.getStringWidth(valueOfList) + 16

                                if (value.openList) {
                                    if (mouseButton == 0
                                        && mouseX in minX..maxX
                                        && mouseY in yPos + 2..yPos + 14
                                    ) {
                                        value.set(valueOfList)
                                        
                                        return true
                                    }

                                    drawRect(minX, yPos + 2, maxX, yPos + 14, Int.MIN_VALUE)

                                    font35.drawString(">", minX + 2, yPos + 4,
                                        if (value.get() == valueOfList) guiColor else Int.MAX_VALUE
                                    )
                                    font35.drawString(valueOfList, minX + 10, yPos + 4,
                                        if (value.get() == valueOfList) guiColor else Int.MAX_VALUE
                                    )

                                    yPos += 12
                                }
                            }
                        }
                        is FloatValue -> {
                            val text = value.name + "§f: §c" + round(value.get())

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            if ((mouseButton == 0 || sliderValueHeld == value)
                                && mouseX in minX..maxX
                                && mouseY in yPos + 15..yPos + 21
                            ) {
                                val percentage = (mouseX - minX - 4) / (maxX - minX - 8).toFloat()
                                value.set(round(value.minimum + (value.maximum - value.minimum) * percentage).coerceIn(value.range))

                                // Keep changing this slider until mouse is unpressed.
                                sliderValueHeld = value

                                // Stop rendering and interacting only when this event was triggered by a mouse click.
                                if (mouseButton == 0) return true
                            }

                            drawRect(minX, yPos + 2, maxX, yPos + 24, Int.MIN_VALUE)
                            drawRect(minX + 4, yPos + 18, maxX - 4, yPos + 19, Int.MAX_VALUE)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue = (moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (displayValue - value.minimum) / (value.maximum - value.minimum)).roundToInt()
                            drawRect(8 + sliderValue, yPos + 15, sliderValue + 11, yPos + 21, guiColor)

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 22
                        }
                        is IntegerValue -> {
                            val text = value.name + "§f: §c" + if (value is BlockValue) getBlockName(value.get()) + " (" + value.get() + ")" else value.get()

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            if ((mouseButton == 0 || sliderValueHeld == value)
                                && mouseX in minX..maxX
                                && mouseY in yPos + 15..yPos + 21
                            ) {
                                val percentage = (mouseX - minX - 4) / (maxX - minX - 8).toFloat()
                                value.set((value.minimum + (value.maximum - value.minimum) * percentage).roundToInt().coerceIn(value.range))

                                // Keep changing this slider until mouse is unpressed.
                                sliderValueHeld = value

                                // Stop rendering and interacting only when this event was triggered by a mouse click.
                                if (mouseButton == 0) return true
                            }

                            drawRect(minX, yPos + 2, maxX, yPos + 24, Int.MIN_VALUE)
                            drawRect(minX + 4, yPos + 18, maxX - 4, yPos + 19, Int.MAX_VALUE)

                            val displayValue = value.get().coerceIn(value.range)
                            val sliderValue = moduleElement.x + moduleElement.width + (moduleElement.settingsWidth - 12) * (displayValue - value.minimum) / (value.maximum - value.minimum)
                            drawRect(8 + sliderValue, yPos + 15, sliderValue + 11, yPos + 21, guiColor)

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 22
                        }
                        is FontValue -> {
                            val displayString = value.displayName
                            moduleElement.settingsWidth = font35.getStringWidth(displayString) + 8

                            if (mouseButton != null
                                && mouseX in minX..maxX
                                && mouseY in yPos + 4 ..yPos + 12
                            ) {
                                // Cycle to next font when left-clicked, previous when right-clicked.
                                if (mouseButton == 0) value.next()
                                else value.previous()
                                
                                return true
                            }

                            drawRect(minX, yPos + 2, maxX, yPos + 14, Int.MIN_VALUE)

                            font35.drawString(displayString, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 11
                        }
                        else -> {
                            val text = value.name + "§f: §c" + value.get()

                            moduleElement.settingsWidth = font35.getStringWidth(text) + 8

                            drawRect(minX, yPos + 2, maxX, yPos + 14, Int.MIN_VALUE)

                            font35.drawString(text, minX + 2, yPos + 4, Color.WHITE.rgb)

                            yPos += 12
                        }
                    }
                }

                moduleElement.settingsHeight = yPos - moduleElement.y - 4

                if (moduleElement.settingsWidth > 0 && yPos > moduleElement.y + 4) {
                    if (mouseButton != null
                        && mouseX in minX..maxX
                        && mouseY in moduleElement.y + 6..yPos + 2) return true

                    drawBorderedRect(minX, moduleElement.y + 6, maxX, yPos + 2, 1, Int.MIN_VALUE, 0)
                }
            }
        }
        return false
    }
}