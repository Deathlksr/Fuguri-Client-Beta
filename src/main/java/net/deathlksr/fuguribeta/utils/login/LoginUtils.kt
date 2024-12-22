/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.utils.login

import com.google.gson.JsonParser
import net.deathlksr.fuguribeta.event.EventManager.callEvent
import net.deathlksr.fuguribeta.event.SessionEvent
import net.deathlksr.fuguribeta.utils.MinecraftInstance
import net.minecraft.util.Session
import java.util.*

fun me.liuli.elixir.compat.Session.intoMinecraftSession() = Session(username, uuid, token, type)

object LoginUtils : MinecraftInstance() {

    fun loginSessionId(sessionId: String): LoginResult {
        val decodedSessionData = try {
            String(Base64.getDecoder().decode(sessionId.split(".")[1]), Charsets.UTF_8)
        } catch (e: Exception) {
            return LoginResult.FAILED_PARSE_TOKEN
        }

        val sessionObject = try {
            JsonParser().parse(decodedSessionData).asJsonObject
        } catch (e: java.lang.Exception) {
            return LoginResult.FAILED_PARSE_TOKEN
        }
        val uuid = sessionObject["spr"].asString
        val accessToken = sessionObject["yggt"].asString

        if (!UserUtils.isValidToken(accessToken)) {
            return LoginResult.INVALID_ACCOUNT_DATA
        }

        val username = UserUtils.getUsername(uuid) ?: return LoginResult.INVALID_ACCOUNT_DATA

        mc.session = Session(username, uuid, accessToken, "mojang")
        callEvent(SessionEvent())

        return LoginResult.LOGGED
    }

    enum class LoginResult {
        INVALID_ACCOUNT_DATA, LOGGED, FAILED_PARSE_TOKEN
    }

}