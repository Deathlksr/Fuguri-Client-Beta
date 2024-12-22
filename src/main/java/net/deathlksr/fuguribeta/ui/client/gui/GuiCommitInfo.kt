/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.ui.client.gui

import net.deathlksr.fuguribeta.FuguriBeta
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.GitUtils
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawImage
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.GuiScreen
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.io.IOException
import kotlin.Float
import kotlin.Int
import kotlin.Throws

class GuiCommitInfo : GuiScreen() {

    override fun initGui() {
        val buttonWidth = 200
        val buttonHeight = 20
        val buttonX = width / 2 - buttonWidth / 2
        val buttonY = height - buttonHeight - 10

        buttonList.add(GuiButton(0, buttonX, buttonY, buttonWidth, buttonHeight, "Back"))

        super.initGui()
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        drawDefaultBackground()
        drawImage(gitImage, 30, 30, 30, 30)

        val font = Fonts.minecraftFont
        val textColor = Color(255, 255, 255).rgb
        val startX = 70
        val startY = 30

        val lines = listOf(
            "Git Info",
            "${FuguriBeta.CLIENT_NAME} built by ${GitUtils.gitInfo.getProperty("git.build.user.name")}",
            "Version: ${GitUtils.gitInfo.getProperty("git.build.version")}",
            "CommitId: ${GitUtils.gitInfo.getProperty("git.commit.id")} (${GitUtils.gitInfo.getProperty("git.commit.id.abbrev")})",
            "CommitMessage: ${GitUtils.gitInfo.getProperty("git.commit.message.short")}",
            "Branch: ${GitUtils.gitInfo.getProperty("git.branch")}",
            "Remote origin: ${GitUtils.gitInfo.getProperty("git.remote.origin.url")}",
            "Developers: ${FuguriBeta.CLIENT_AUTHOR}"
        )

        lines.forEachIndexed { index, line ->
            drawString(font, line, startX, startY + font.FONT_HEIGHT * index + index * 5, textColor)
        }

        super.drawScreen(mouseX, mouseY, partialTicks)
    }

    @Throws(IOException::class)
    override fun actionPerformed(button: GuiButton) {
        if (button.id == 0) {
            mc.displayGuiScreen(null)
        }
    }

    companion object {
        val gitImage: ResourceLocation = ResourceLocation("fuguribeta/mainmenu/github.png")
    }
}