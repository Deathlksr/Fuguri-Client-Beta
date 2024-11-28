package net.ccbluex.liquidbounce.ui.client.gui

import net.ccbluex.liquidbounce.ui.client.altmanager.GuiAltManager
import net.ccbluex.liquidbounce.ui.client.gui.button.ImageButton
import net.ccbluex.liquidbounce.ui.client.gui.button.QuitButton
import net.ccbluex.liquidbounce.utils.login.UserUtils.isValidTokenOffline
import net.ccbluex.liquidbounce.utils.misc.MiscUtils
import net.minecraft.client.gui.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.client.GuiModList

class GuiMainMenu : GuiScreen(), GuiYesNoCallback {
    private var logo: ResourceLocation? = null
    private lateinit var btnSinglePlayer: GuiButton
    private lateinit var btnMultiplayer: GuiButton
    private lateinit var btnClientOptions: GuiButton
    private lateinit var btnAltManager: GuiButton
    private lateinit var btnMinecraftOptions: GuiButton
    private lateinit var btnForgeModList: ImageButton
    private lateinit var btnGitHub: ImageButton
    private lateinit var btnQuit: QuitButton

    override fun initGui() {
        logo = ResourceLocation("fuguribeta/mainmenu/logo.png")
        val yPos = height - 20
        val buttonWidth = 133
        val buttonHeight = 20

        btnSinglePlayer = GuiButton(0, width / 2 - 66, height / 2 - 80 + 70, buttonWidth, buttonHeight, "Single Player")
        btnMultiplayer = GuiButton(1, width / 2 - 66, height / 2 - 80 + 95 - 2, buttonWidth, buttonHeight, "Multi Player")
        btnAltManager = GuiButton(2, width / 2 - 66, height / 2 - 80 + 120 - 4, buttonWidth, buttonHeight, "Alt Manager")
        btnClientOptions = GuiButton(3, width / 2 - 66, height / 2 - 80 + 145 - 6, buttonWidth, buttonHeight, "Client Settings")
        btnMinecraftOptions = GuiButton(4, width / 2 - 66, height / 2 - 80 + 170 - 8, buttonWidth, buttonHeight, "Minecraft Settings")

        btnForgeModList = ImageButton(
            "ForgeMods",
            ResourceLocation("fuguribeta/mainmenu/forge.png"),
            width / 2 + 5,
            yPos
        )
        btnGitHub = ImageButton(
            "GitHub",
            ResourceLocation("fuguribeta/mainmenu/github.png"),
            width / 2 - 10,
            yPos
        )
        btnQuit = QuitButton(width - 17, 7)

        buttonList.addAll(listOf(btnSinglePlayer, btnMultiplayer, btnAltManager, btnClientOptions, btnMinecraftOptions))
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, button: Int) {
        buttonList.forEach { guiButton ->
            if (guiButton.mousePressed(mc, mouseX, mouseY)) {
                actionPerformed(guiButton)
            }

            when {
                btnQuit.hoverFade > 0 -> mc.shutdown()
                btnForgeModList.hoverFade > 0 -> mc.displayGuiScreen(GuiModList(mc.currentScreen))
                btnGitHub.hoverFade > 0 -> MiscUtils.showURL("https://github.com/VerLouF/Fuguri-Client-Beta")
            }
        }
    }

    override fun actionPerformed(button: GuiButton) {
        when (button.id) {
            0 -> mc.displayGuiScreen(GuiSelectWorld(this))
            1 -> mc.displayGuiScreen(GuiMultiplayer(this))
            2 -> mc.displayGuiScreen(GuiAltManager(this))
            3 -> mc.displayGuiScreen(GuiInfo(this))
            4 -> mc.displayGuiScreen(GuiOptions(this, mc.gameSettings))
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
        this.drawString(
            mc.fontRendererObj, "§7Session: §5${mc.getSession().username}",
            6,
            6,
            0xffffff
        )
        this.drawString(
            mc.fontRendererObj, "§7Type: §5${
                if (isValidTokenOffline(
                        mc.getSession().token
                    )
                ) "Microsoft" else "Cracked"
            }", 6, 15, 0xffffff
        )

        listOf(btnForgeModList, btnGitHub, btnQuit).forEach {
            it.drawButton(mouseX, mouseY)
        }

        GlStateManager.popMatrix()

        super.drawScreen(mouseX, mouseY, partialTicks)
    }
}