/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.client

import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.modules.client.button.*
import net.deathlksr.fuguribeta.value.ListValue
import net.deathlksr.fuguribeta.value.TextValue
import net.minecraft.client.gui.GuiButton
import java.util.*

/**
 * The type Client spoof.
 */
object ClientSpoofer : Module("ClientSpoofer", Category.CLIENT, hideModule = false) {
    /**
     * The Mode value.
     */
    val possibleBrands = ListValue(
        "Mode", arrayOf(
            "Vanilla",
            "OptiFine",
            "Fabric",
            "Feather",
            "LunarClient",
            "LabyMod",
            "CheatBreaker",
            "PvPLounge",
            "Minebuilders",
            "FML",
            "Geyser",
            "Log4j",
            "Custom",
        ), "FDP"
    )

    val customValue = TextValue("Custom-Brand", "WTF") { possibleBrands.get().equals("Custom", true) }

    private val buttonValue = ListValue(
        "Button",
        arrayOf(
            "Dark",
            "Light",
            "Rounded",
            "LiquidBounce",
            "Fline",
            "FDP",
            "PVP",
            "Vanilla"
        ),
        "FDP"
    )

    fun getButtonRenderer(button: GuiButton?): AbstractButtonRenderer? {
        val lowerCaseButtonValue = buttonValue.get().lowercase(Locale.getDefault())
        return when (lowerCaseButtonValue) {
            "rounded" -> RoundedButtonRenderer(button!!)
            "fdp" -> HyperiumButtonRenderer(button!!)
            "pvp" -> PvPClientButtonRenderer(button!!)
            "fuguribeta" -> LiquidButtonRenderer(button!!)
            "light" -> LunarButtonRenderer(button!!)
            "dark" -> BlackoutButtonRenderer(button!!)
            else -> null // vanilla or unknown
        }
    }
}