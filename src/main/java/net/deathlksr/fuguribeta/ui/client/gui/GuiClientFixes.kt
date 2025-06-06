package net.deathlksr.fuguribeta.ui.client.gui

import net.deathlksr.fuguribeta.features.module.modules.client.ClientSpoofer.possibleBrands
import net.deathlksr.fuguribeta.handler.payload.ClientFixes.blockFML
import net.deathlksr.fuguribeta.handler.payload.ClientFixes.blockPayloadPackets
import net.deathlksr.fuguribeta.handler.payload.ClientFixes.blockProxyPacket
import net.deathlksr.fuguribeta.handler.payload.ClientFixes.blockResourcePackExploit
import net.deathlksr.fuguribeta.handler.payload.ClientFixes.fmlFixesEnabled
import net.deathlksr.fuguribeta.file.FileManager.saveConfig
import net.deathlksr.fuguribeta.file.FileManager.valuesConfig
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import org.lwjgl.input.Keyboard
import java.io.IOException
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList

class GuiClientFixes(private val prevGui: GuiScreen) : GuiScreen() {

    private lateinit var enabledButton: GuiButton
    private lateinit var fmlButton: GuiButton
    private lateinit var proxyButton: GuiButton
    private lateinit var payloadButton: GuiButton
    private lateinit var customBrandButton: GuiButton
    private lateinit var resourcePackButton: GuiButton

    override fun initGui() {
        enabledButton = GuiButton(1, width / 2 - 100, height / 4 + 35, "AntiForge (" + (if (fmlFixesEnabled) "On" else "Off") + ")")
        fmlButton = GuiButton(2, width / 2 - 100, height / 4 + 35 + 25, "Block FML (" + (if (blockFML) "On" else "Off") + ")")
        proxyButton = GuiButton(3, width / 2 - 100, height / 4 + 35 + 25 * 2, "Block FML Proxy Packet (" + (if (blockProxyPacket) "On" else "Off") + ")")
        payloadButton = GuiButton(4, width / 2 - 100, height / 4 + 35 + 25 * 3, "Block Non-MC Payloads (" + (if (blockPayloadPackets) "On" else "Off") + ")")
        customBrandButton = GuiButton(5, width / 2 - 100, height / 4 + 35 + 25 * 4, "Brand (${possibleBrands.get()})")
        resourcePackButton = GuiButton(6, width / 2 - 100, height / 4 + 50 + 25 * 5, "Block Resource Pack Exploit (" + (if (blockResourcePackExploit) "On" else "Off") + ")")

        buttonList = CopyOnWriteArrayList(
            listOf(
                enabledButton, fmlButton, proxyButton, payloadButton, customBrandButton, resourcePackButton,
                GuiButton(0, width / 2 - 100, height / 4 + 55 + 25 * 6 + 5, "Back")
            )
        )
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            1 -> {
                fmlFixesEnabled = !fmlFixesEnabled
                enabledButton.displayString = "AntiForge (${if (fmlFixesEnabled) "On" else "Off"})"
            }
            2 -> {
                blockFML = !blockFML
                fmlButton.displayString = "Block FML (${if (blockFML) "On" else "Off"})"
            }
            3 -> {
                blockProxyPacket = !blockProxyPacket
                proxyButton.displayString = "Block FML Proxy Packet (${if (blockProxyPacket) "On" else "Off"})"
            }
            4 -> {
                blockPayloadPackets = !blockPayloadPackets
                payloadButton.displayString = "Block Non-MC Payloads (${if (blockPayloadPackets) "On" else "Off"})"
            }
            5 -> {
                val brands = possibleBrands.values

                // Switch to next client brand
                val currentBrand = possibleBrands.get()
                val nextBrand = brands[(brands.indexOf(currentBrand) + 1) % brands.size]
                possibleBrands.set(nextBrand)

                customBrandButton.displayString = "Brand ($nextBrand)"
            }
            6 -> {
                blockResourcePackExploit = !blockResourcePackExploit
                resourcePackButton.displayString = "Block Resource Pack Exploit (${if (blockResourcePackExploit) "On" else "Off"})"
            }
            0 -> mc.displayGuiScreen(prevGui)
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)
        Fonts.fontBold180.drawCenteredString("Fixes", width / 2f, height / 8f + 5f, 1470121, true)

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun keyTyped(typedChar: Char, keyCode: Int) {
        if (Keyboard.KEY_ESCAPE == keyCode) {
            mc.displayGuiScreen(prevGui)
            return
        }

        super.keyTyped(typedChar, keyCode)
    }

    override fun onGuiClosed() {
        saveConfig(valuesConfig)
        super.onGuiClosed()
    }
}
