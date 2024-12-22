/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.ui.client.clickgui.elements

import net.deathlksr.fuguribeta.FuguriBeta.clickGui
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly

@SideOnly(Side.CLIENT)
open class ButtonElement(
    open val displayName: String,
    val stateDependingColor: () -> Int = { Int.MAX_VALUE },
    val buttonAction: () -> Unit
) : Element() {

    val color
        get() = stateDependingColor()

    open var hoverText: String = ""

    var hoverTime = 0
        set(value) {
            field = value.coerceIn(0, 7)
        }

    override val height = 16

    override fun drawScreenAndClick(mouseX: Int, mouseY: Int, mouseButton: Int?): Boolean {
        clickGui.style.drawButtonElement(mouseX, mouseY, this)
        return false
    }

    override fun mouseClicked(mouseX: Int, mouseY: Int, mouseButton: Int): Boolean {
        if (mouseButton == 0 && isHovered(mouseX, mouseY)) {
            buttonAction()
            return true
        }

        return super.mouseClicked(mouseX, mouseY, mouseButton)
    }


}