package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.FuguriBeta.CLIENT_NAME
import net.ccbluex.liquidbounce.FuguriBeta.clientVersionText
import net.ccbluex.liquidbounce.ui.client.gui.button.ImageButton
import net.ccbluex.liquidbounce.ui.client.gui.button.QuitButton
import net.ccbluex.liquidbounce.ui.font.Fonts.minecraftFont
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList
import java.awt.Color

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    private var logo: ResourceLocation? = null
    private lateinit var btnSinglePlayer: GuiButton
    private lateinit var btnMultiplayer: GuiButton
    private lateinit var btnClientOptions: GuiButton
    private lateinit var btnMinecraftOptions: ImageButton
    private lateinit var btnForgeModList: ImageButton
    private lateinit var btnQuit: QuitButton

    override fun initGui() {
        logo = ResourceLocation("fdpclient/mainmenu/logo.png")
        val yPos = height - 20
        val buttonWidth = 133
        val buttonHeight = 20

        btnSinglePlayer = GuiButton(0, width / 2 - 66, height / 2 - 80 + 70, buttonWidth, buttonHeight, "Single Player")
        btnMultiplayer = GuiButton(1, width / 2 - 66, height / 2 - 80 + 95 - 2, buttonWidth, buttonHeight, "Multi Player")
        btnClientOptions = GuiButton(2, width / 2 - 66, height / 2 - 80 + 120 - 4, buttonWidth, buttonHeight, "Settings")

        btnMinecraftOptions = ImageButton(
            "MINECRAFT SETTINGS",
            ResourceLocation("fdpclient/mainmenu/cog.png"),
            width / 2 - 10,
            yPos
        )
        btnForgeModList = ImageButton(
            "FORGE MODS",
            ResourceLocation("fdpclient/mainmenu/forge.png"),
            width / 2 + 5,
            yPos
        )
        btnQuit = QuitButton(width - 17, 7)

        buttonList.addAll(listOf(btnSinglePlayer, btnMultiplayer, btnClientOptions))
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        buttonList.forEach { guiButton ->
            if (guiButton.mousePressed(mc, mouseX, mouseY)) {
                actionPerformed(guiButton)
            }

            when {
                btnQuit.hoverFade > 0 -> mc.shutdown()
                btnMinecraftOptions.hoverFade > 0 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
                btnForgeModList.hoverFade > 0 -> mc.displayGuiScreen(GuiModList(mc.currentScreen))
            }
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiInfo(this))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawBackground(0)

        GlStateManager.pushMatrix()
        GlStateManager.disableAlpha()
        GlStateManager.enableAlpha()
        GlStateManager.enableBlend()
        GlStateManager.color(1.0f, 1.0f, 1.0f)
        mc.textureManager.bindTexture(logo)
        drawModalRectWithCustomSizedTexture(width / 2 - 74, height / 2 - 140, 150f, 150f, 150, 150, 150f, 150f)

        minecraftFont.drawStringWithShadow(
            CLIENT_NAME,
            ((width - 4f - minecraftFont.getStringWidth(CLIENT_NAME)).toDouble().toFloat()),
            ((height - 23f).toDouble().toFloat()),
            Color(255, 225, 255, 100).rgb
        )
        minecraftFont.drawStringWithShadow(
            "Your currently build is $clientVersionText",
            ((width - 4f - minecraftFont.getStringWidth("Your currently build is $clientVersionText")).toDouble().toFloat()),
            ((height - 12f).toDouble().toFloat()),
            Color(255, 225, 255, 100).rgb
        )

        listOf(btnMinecraftOptions, btnForgeModList, btnQuit).forEach {
            it.drawButton(mouseX, mouseY)
        }

        GlStateManager.popMatrix()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}