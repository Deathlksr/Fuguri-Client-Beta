/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.ui.client.clickgui

import kotlinx.coroutines.*
import net.deathlksr.fuguribeta.FuguriBeta.CLIENT_NAME
import net.deathlksr.fuguribeta.FuguriBeta.moduleManager
import net.deathlksr.fuguribeta.handler.api.ClientApi
import net.deathlksr.fuguribeta.handler.api.autoSettingsList
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.modules.client.ClickGUIModule
import net.deathlksr.fuguribeta.features.module.modules.client.ClickGUIModule.scale
import net.deathlksr.fuguribeta.features.module.modules.client.ClickGUIModule.scrolls
import net.deathlksr.fuguribeta.file.FileManager.clickGuiConfig
import net.deathlksr.fuguribeta.file.FileManager.saveConfig
import net.deathlksr.fuguribeta.ui.client.clickgui.elements.ButtonElement
import net.deathlksr.fuguribeta.ui.client.clickgui.elements.ModuleElement
import net.deathlksr.fuguribeta.ui.client.clickgui.style.Style
import net.deathlksr.fuguribeta.ui.client.clickgui.style.styles.BlackStyle
import net.deathlksr.fuguribeta.ui.client.hud.HUD
import net.deathlksr.fuguribeta.ui.client.hud.designer.GuiHudDesigner
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Notification
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Type
import net.deathlksr.fuguribeta.ui.font.AWTFontRenderer.Companion.assumeNonVolatile
import net.deathlksr.fuguribeta.utils.ClientUtils
import net.deathlksr.fuguribeta.utils.ClientUtils.displayChatMessage
import net.deathlksr.fuguribeta.utils.SettingsUtils
import net.deathlksr.fuguribeta.utils.render.RenderUtils.deltaTime
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawImage
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.gui.GuiScreen
import net.minecraft.client.renderer.GlStateManager.disableLighting
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.util.ResourceLocation
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11.glScaled
import kotlin.math.roundToInt

object ClickGui : GuiScreen() {

    val panels = mutableListOf<Panel>()
    private val hudIcon = ResourceLocation("${CLIENT_NAME.lowercase()}/hud_designer.png")
    var style: Style = BlackStyle
    private var mouseX = 0
        set(value) {
            field = value.coerceAtLeast(0)
        }
    private var mouseY = 0
        set(value) {
            field = value.coerceAtLeast(0)
        }

    // Used when closing ClickGui using its key bind, prevents it from getting closed instantly after getting opened.
    // Caused by keyTyped being called along with onKey that opens the ClickGui.
    private var ignoreClosing = false

    fun setDefault() {
        panels.clear()

        val width = 100
        val height = 18
        var yPos = 5

        for (category in Category.values()) {
            panels += object : Panel(category.displayName, 100, yPos, width, height, false) {
                override val elements =
                    moduleManager.modules.filter { it.category == category }.map { ModuleElement(it) }
            }

            yPos += 20
        }

        yPos += 20

        // Settings Panel
        yPos += 20
        panels += setupSettingsPanel(100, yPos, width, height)
    }

    private fun setupSettingsPanel(xPos: Int = 100, yPos: Int, width: Int, height: Int) =
        object : Panel("Auto Settings", xPos, yPos, width, height, false) {

            /**
             * Auto settings list
             */
            override val elements = runBlocking {
                async(Dispatchers.IO) {
                    autoSettingsList?.map { setting ->
                        ButtonElement(setting.name, { Integer.MAX_VALUE }) {
                            GlobalScope.launch {
                                try {
                                    displayChatMessage("Loading settings...")

                                    // Load settings and apply them
                                    val settings = ClientApi.requestSettingsScript(setting.settingId)

                                    displayChatMessage("Applying settings...")
                                    SettingsUtils.applyScript(settings)

                                    displayChatMessage("§6Settings applied successfully")
                                    HUD.addNotification(Notification("Updated Settings", "!!!", Type.INFO, 60))
                                    mc.soundHandler.playSound(
                                        PositionedSoundRecord.create(
                                            ResourceLocation("random.anvil_use"), 1F
                                        )
                                    )
                                } catch (e: Exception) {
                                    ClientUtils.LOGGER.error("Failed to load settings", e)
                                    displayChatMessage("Failed to load settings: ${e.message}")
                                }
                            }
                        }.apply {
                            this.hoverText = buildString {
                                appendLine("§7Description: §e${setting.description.ifBlank { "No description available" }}")
                                appendLine("§7Type: §e${setting.type.displayName}")
                                appendLine("§7Contributors: §e${setting.contributors}")
                                appendLine("§7Last updated: §e${setting.date}")
                                append("§7Status: §e${setting.statusType.displayName} §a(${setting.statusDate})")
                            }
                        }
                    } ?: emptyList()
                }.await()
            }
        }

    override fun drawScreen(x: Int, y: Int, partialTicks: Float) {
        // Enable DisplayList optimization
        assumeNonVolatile = true

        mouseX = (x / scale).roundToInt()
        mouseY = (y / scale).roundToInt()

        drawDefaultBackground()
        drawImage(hudIcon, 9, height - 41, 32, 32)

        val scale = scale.toDouble()
        glScaled(scale, scale, scale)

        for (panel in panels) {
            panel.updateFade(deltaTime)
            panel.drawScreenAndClick(mouseX, mouseY)
        }

        descriptions@ for (panel in panels.reversed()) {
            // Don't draw hover text when hovering over a panel header.
            if (panel.isHovered(mouseX, mouseY)) break

            for (element in panel.elements) {
                if (element is ButtonElement) {
                    if (element.isVisible && element.hoverText.isNotBlank() && element.isHovered(
                            mouseX, mouseY
                        ) && element.y <= panel.y + panel.fade
                    ) {
                        style.drawHoverText(mouseX, mouseY, element.hoverText)
                        // Don't draw hover text for any elements below.
                        break@descriptions
                    }
                }
            }
        }

        if (Mouse.hasWheel()) {
            val wheel = Mouse.getDWheel()
            if (wheel != 0) {
                var handledScroll = false

                // Handle foremost panel.
                for (panel in panels.reversed()) {
                    if (panel.handleScroll(mouseX, mouseY, wheel)) {
                        handledScroll = true
                        break
                    }
                }

                if (!handledScroll) handleScroll(wheel)
            }
        }

        disableLighting()
        RenderHelper.disableStandardItemLighting()
        glScaled(1.0, 1.0, 1.0)

        assumeNonVolatile = false

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    private fun handleScroll(wheel: Int) {
        if (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)) {
            scale += wheel * 0.0001f

            for (panel in panels) {
                panel.x = panel.parseX()
                panel.y = panel.parseY()
            }

        } else if (scrolls) {
            for (panel in panels) panel.y = panel.parseY(panel.y + wheel / 10)
        }
    }

    public override fun mouseClicked(x: Int, y: Int, mouseButton: Int) {
        if (mouseButton == 0 && x in 5..50 && y in height - 50..height - 5) {
            mc.displayGuiScreen(GuiHudDesigner())
            return
        }

        mouseX = (x / scale).roundToInt()
        mouseY = (y / scale).roundToInt()

        // Handle foremost panel.
        panels.reversed().forEachIndexed { index, panel ->
            if (panel.mouseClicked(mouseX, mouseY, mouseButton)) return

            panel.drag = false

            if (mouseButton == 0 && panel.isHovered(mouseX, mouseY)) {
                panel.x2 = panel.x - mouseX
                panel.y2 = panel.y - mouseY
                panel.drag = true

                // Move dragged panel to top.
                panels.removeAt(panels.lastIndex - index)
                panels += panel
                return
            }
        }
    }

    public override fun mouseReleased(x: Int, y: Int, state: Int) {
        mouseX = (x / scale).roundToInt()
        mouseY = (y / scale).roundToInt()

        for (panel in panels) panel.mouseReleased(mouseX, mouseY, state)
    }

    override fun updateScreen() {
        if (style is BlackStyle) {
            for (panel in panels) {
                for (element in panel.elements) {
                    if (element is ButtonElement) element.hoverTime += if (element.isHovered(mouseX, mouseY)) 1 else -1

                    if (element is ModuleElement) element.slowlyFade += if (element.module.state) 20 else -20
                }
            }
        }

        super.updateScreen()
    }

    override fun keyTyped(typedChar: Char, keyCode: Int) {
        // Close ClickGUI by using its key bind.
        if (keyCode == ClickGUIModule.keyBind) {
            if (ignoreClosing) ignoreClosing = false
            else mc.displayGuiScreen(null)

            return
        }

        super.keyTyped(typedChar, keyCode)
    }

    override fun onGuiClosed() {
        saveConfig(clickGuiConfig)
        for (panel in panels) panel.fade = 0
    }

    override fun initGui() {
        ignoreClosing = true
    }

    fun Int.clamp(min: Int, max: Int): Int = this.coerceIn(min, max.coerceAtLeast(0))

    override fun doesGuiPauseGame() = false
}