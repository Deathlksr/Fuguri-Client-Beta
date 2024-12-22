/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.ui.client.clickgui.elements

import net.deathlksr.fuguribeta.FuguriBeta.clickGui
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.modules.client.ClickGUIModule.spacedModules
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
class ModuleElement(val module: Module) : ButtonElement(module.name, buttonAction = {
    // This module element handles the click action itself.
}) {
    override val displayName
        get() = module.getName(spacedModules)

    override var hoverText = ""
        get() = module.description

    var showSettings = false
    var settingsWidth = 0
        set(value) {
            if (value > settingsWidth) {
                field = value
            }
        }

    var settingsHeight = 0

    var slowlyFade = 0
        set(value) {
            field = value.coerceIn(0, 255)
        }

    override fun drawScreenAndClick(mouseX: Int, mouseY: Int, mouseButton: Int?) =
        clickGui.style.drawModuleElementAndClick(mouseX, mouseY, this, mouseButton)

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (!isHovered(mouseX, mouseY)) {
            return false
        }

        when (mouseButton) {
            0 -> {
                module.toggle()
            }
            1 -> {
                if (module.values.isNotEmpty()) {
                    showSettings = !showSettings
                }
            }
        }

        return true
    }

}