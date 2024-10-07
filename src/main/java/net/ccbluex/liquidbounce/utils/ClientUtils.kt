/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import com.google.gson.JsonObject
import net.ccbluex.liquidbounce.FuguriBeta.CLIENT_NAME
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.NetworkManager
import net.minecraft.network.login.client.C01PacketEncryptionResponse
import net.minecraft.network.login.server.S01PacketEncryptionRequest
import net.minecraft.util.IChatComponent
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.lang.reflect.Field
import java.security.PublicKey
import javax.crypto.SecretKey

@SideOnly(Side.CLIENT)
object ClientUtils : MinecraftInstance() {
    private var fastRenderField: Field? = null
    var runTimeTicks = 0

    init {
        try {
            val declaredField = GameSettings::class.java.getDeclaredField("ofFastRender")

            fastRenderField = declaredField
        } catch (ignored: NoSuchFieldException) { }
    }

    val LOGGER: Logger = LogManager.getLogger("Fuguri-Beta")

    fun disableFastRender() {
        try {
            fastRenderField?.let {
                if (!it.isAccessible)
                    it.isAccessible = true

                it.setBoolean(mc.gameSettings, false)
            }
        } catch (ignored: IllegalAccessException) {
        }
    }

    fun sendEncryption(
        networkManager: NetworkManager,
        secretKey: SecretKey?,
        publicKey: PublicKey?,
        encryptionRequest: S01PacketEncryptionRequest
    ) {
        networkManager.sendPacket(C01PacketEncryptionResponse(secretKey, publicKey, encryptionRequest.verifyToken),
            { networkManager.enableEncryption(secretKey) }
        )
    }

    fun displayAlert(message: String) {
        displayChatMessage("§7[§5$CLIENT_NAME§7] → §7$message")
    }

    fun displayChatMessage(message: String) {
        if (mc.thePlayer == null) {
            LOGGER.info("(MCChat) $message")
            return
        }

        val prefixMessage = "§7[§5$CLIENT_NAME§7] → §7$message"
        val jsonObject = JsonObject()
        jsonObject.addProperty("text", prefixMessage)
        mc.thePlayer.addChatMessage(IChatComponent.Serializer.jsonToComponent(jsonObject.toString()))
    }
}