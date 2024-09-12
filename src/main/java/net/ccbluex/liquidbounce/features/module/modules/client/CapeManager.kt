package net.ccbluex.liquidbounce.features.module.modules.client

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.APIConnecter
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.util.ResourceLocation
import java.util.*

object CapeManager : Module("CapeManager", Category.CLIENT, hideModule = false) {

    val customCape = BoolValue("CustomCape", true)
    val styleValue = ListValue(
        "Mode",
        arrayOf(
            "FuguriBeta", "Augustus", "AugustusAmethyst", "AugustusCandy", "AugustusClassic",
            "AugustusMagic", "AugustusMagma", "AugustusMango", "AugustusRose", "AugustusTitanium", "DiscordLoading", "ESound", "Pyatero4ka", "RickRoll"
        ),
        "FuguriBeta"
    )
    { customCape.get() }

    private val capeCache = hashMapOf<String, CapeStyle>()
    private var nowCape: CapeStyle? = null

    fun getCapeLocation(value: String): ResourceLocation? {
        val upperValue = value.uppercase(Locale.getDefault())
        if (capeCache[upperValue] == null) {
            capeCache[upperValue] = CapeStyle.valueOf(upperValue)
        }
        return capeCache[upperValue]?.location
    }

    enum class CapeStyle(val location: ResourceLocation) {
        NONE(APIConnecter.callImage("none", "cape")),
        FUGURIBETA(ResourceLocation("fdpclient/cape/FuguriBeta.png")),
        AUGUSTUS(ResourceLocation("fdpclient/cape/Augustus.png")),
        AUGUSTUSAMETHYST(ResourceLocation("fdpclient/cape/AugustusAmethyst.png")),
        AUGUSTUSCANDY(ResourceLocation("fdpclient/cape/AugustusCandy.png")),
        AUGUSTUSCLASSIC(ResourceLocation("fdpclient/cape/AugustusClassic.png")),
        AUGUSTUSMAGIC(ResourceLocation("fdpclient/cape/AugustusMagic.png")),
        AUGUSTUSMAGMA(ResourceLocation("fdpclient/cape/AugustusMagma.png")),
        AUGUSTUSMANGO(ResourceLocation("fdpclient/cape/AugustusMango.png")),
        AUGUSTUSROSE(ResourceLocation("fdpclient/cape/AugustusRose.png")),
        AUGUSTUSTITANIUM(ResourceLocation("fdpclient/cape/AugustusTitanium.png")),
        PYATERO4KA(ResourceLocation("fdpclient/cape/Pyatero4ka.png")),
        RICKROLL(ResourceLocation("fdpclient/cape/RickRoll.png")),
        ESOUND(ResourceLocation("fdpclient/cape/ESound.png")),
        DISCORDLOADING(ResourceLocation("fdpclient/cape/DiscordLoading.gif"));
    }

    private fun updateCapeStyle() {
        nowCape = CapeStyle.valueOf(styleValue.value.uppercase(Locale.getDefault()))
    }
}