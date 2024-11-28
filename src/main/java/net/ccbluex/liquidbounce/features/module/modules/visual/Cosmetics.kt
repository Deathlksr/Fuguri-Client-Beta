/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.APIConnecter
import net.ccbluex.liquidbounce.utils.RenderWings
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.ResourceLocation
import java.util.*

object Cosmetics : Module("Cosmetics", Category.VISUAL, hideModule = false) {
    private val wings by BoolValue("Wings", false)
    val wingStyle by ListValue("WingsMode", arrayOf("Dragon", "Simple"), "Dragon") { wings }
    val colorType by ListValue("ColorType", arrayOf("Custom", "Chroma", "None"), "Chroma") { wings }
    val customRed by IntegerValue("Red", 255, 0.. 255) { colorType == "Custom" && wings }
    val customGreen by IntegerValue("Green", 255, 0.. 255) { colorType == "Custom" && wings  }
    val customBlue by IntegerValue("Blue", 255, 0.. 255) { colorType == "Custom" && wings  }
    private val onlyThirdPerson by BoolValue("OnlyThirdPerson", true) { wings }

    val customCape by BoolValue("Cape", false)
    val styleValue = ListValue(
        "CapeMode",
        arrayOf(
            "FuguriBeta", "Augustus", "AugustusAmethyst", "AugustusCandy", "AugustusClassic",
            "AugustusMagic", "AugustusMagma", "AugustusMango", "AugustusRose", "AugustusTitanium", "DiscordLoading", "ESound", "Pyatero4ka", "RickRoll"
        ),
        "FuguriBeta"
    ) { customCape }

    private val capeCache = hashMapOf<String, CapeStyle>()

    fun getCapeLocation(value: String): ResourceLocation? {
        val upperValue = value.uppercase(Locale.getDefault())
        if (capeCache[upperValue] == null) {
            capeCache[upperValue] = CapeStyle.valueOf(upperValue)
        }
        return capeCache[upperValue]?.location
    }
    enum class CapeStyle(val location: ResourceLocation) {
        NONE(APIConnecter.callImage("none", "cape")),
        FUGURIBETA(ResourceLocation("fuguribeta/cape/classic.png")),
        AUGUSTUS(ResourceLocation("fuguribeta/cape/Augustus.png")),
        AUGUSTUSAMETHYST(ResourceLocation("fuguribeta/cape/AugustusAmethyst.png")),
        AUGUSTUSCANDY(ResourceLocation("fuguribeta/cape/AugustusCandy.png")),
        AUGUSTUSCLASSIC(ResourceLocation("fuguribeta/cape/AugustusClassic.png")),
        AUGUSTUSMAGIC(ResourceLocation("fuguribeta/cape/AugustusMagic.png")),
        AUGUSTUSMAGMA(ResourceLocation("fuguribeta/cape/AugustusMagma.png")),
        AUGUSTUSMANGO(ResourceLocation("fuguribeta/cape/AugustusMango.png")),
        AUGUSTUSROSE(ResourceLocation("fuguribeta/cape/AugustusRose.png")),
        AUGUSTUSTITANIUM(ResourceLocation("fuguribeta/cape/AugustusTitanium.png")),
        PYATERO4KA(ResourceLocation("fuguribeta/cape/Pyatero4ka.png")),
        RICKROLL(ResourceLocation("fuguribeta/cape/RickRoll.png")),
        ESOUND(ResourceLocation("fuguribeta/cape/ESound.png")),
        DISCORDLOADING(ResourceLocation("fuguribeta/cape/DiscordLoading.gif"));
    }

    @EventTarget
    fun onRenderPlayer(event: Render3DEvent) {
        if (onlyThirdPerson && mc.gameSettings.thirdPersonView == 0) return
        if (wings) {
            val renderWings = RenderWings()
            renderWings.renderWings(event.partialTicks)
        }
    }
}