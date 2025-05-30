/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import me.liuli.elixir.account.CrackedAccount
import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.event.EventManager.callEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.file.FileManager.accountsConfig
import net.deathlksr.fuguribeta.ui.client.hud.HUD.addNotification
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Notification
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Type
import net.deathlksr.fuguribeta.utils.ClientUtils.displayChatMessage
import net.deathlksr.fuguribeta.utils.ServerUtils
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.randomAccount
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.deathlksr.fuguribeta.value.TextValue
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S40PacketDisconnect
import net.minecraft.network.play.server.S45PacketTitle
import net.minecraft.util.ChatComponentText
import net.minecraft.util.Session
import java.util.*
import kotlin.concurrent.schedule

object AutoAccount : Module("AutoAccount", Category.OTHER, subjective = true, gameDetecting = false, hideModule = false) {

    private val register by BoolValue("AutoRegister", true)
    private val login by BoolValue("AutoLogin", true)

        // Gamster requires 8 chars+
        private val passwordValue = object : TextValue("Password", "zywl1337") {
            override fun onChange(oldValue: String, newValue: String) =
                when {
                    " " in newValue -> {
                        displayChatMessage("§7[§a§lAutoAccount§7] §cPassword cannot contain a space!")
                        oldValue
                    }
                    newValue.equals("reset", true) -> {
                        displayChatMessage("§7[§a§lAutoAccount§7] §3Password reset to its default value.")
                        "zywl1337"
                    }
                    newValue.length < 4 -> {
                        displayChatMessage("§7[§a§lAutoAccount§7] §cPassword must be longer than 4 characters!")
                        oldValue
                    }
                    else -> super.onChange(oldValue, newValue)
                }

            override fun isSupported() = register || login
        }
        private val password by passwordValue

    // Needed for Gamster
    private val sendDelay by IntegerValue("SendDelay", 250, 0..500) { passwordValue.isSupported() }

    private val autoSession by BoolValue("AutoSession", false)
        private val startupValue = BoolValue("RandomAccountOnStart", false) { autoSession }
        private val relogInvalidValue = BoolValue("RelogWhenPasswordInvalid", true) { autoSession }
        private val relogKickedValue = BoolValue("RelogWhenKicked", false) { autoSession }

            private val reconnectDelayValue = IntegerValue("ReconnectDelay", 1000, 0..2500)
                { relogInvalidValue.isActive() || relogKickedValue.isActive() }
            private val reconnectDelay by reconnectDelayValue

            private val accountModeValue = object : ListValue("AccountMode", arrayOf("RandomName", "RandomAlt"), "RandomName") {
                override fun isSupported() = reconnectDelayValue.isSupported() || startupValue.isActive()

                override fun onChange(oldValue: String, newValue: String): String {
                    if (newValue == "RandomAlt" && accountsConfig.accounts.filterIsInstance<CrackedAccount>().size <= 1) {
                        displayChatMessage("§7[§a§lAutoAccount§7] §cAdd more cracked accounts in AltManager to use RandomAlt option!")
                        return oldValue
                    }

                    return super.onChange(oldValue, newValue)
                }
            }
            private val accountMode by accountModeValue

                private val saveValue = BoolValue("SaveToAlts", false) {
                    accountModeValue.isSupported() && accountMode != "RandomAlt"
                }

    private var status = Status.WAITING

    private fun relog(info: String = "") {
        // Disconnect from server
        if (mc.currentServerData != null && mc.theWorld != null)
             mc.netHandler.networkManager.closeChannel(
                 ChatComponentText("$info\n\nReconnecting with a random account in ${reconnectDelay}ms")
             )

        // Log in to account with a random name, optionally save it
        changeAccount()

        // Reconnect normally with OpenGL context
        if (reconnectDelayValue.isMinimal()) return ServerUtils.connectToLastServer()

        // Delay the reconnect, connectToLastServer gets called from a TimerThread with no OpenGL context
        Timer().schedule(reconnectDelay.toLong()) {
            ServerUtils.connectToLastServer(true)
        }
    }

    private fun respond(msg: String) = when {
        register && "/reg" in msg -> {
            addNotification(Notification("Trying to register.", "Trying to Register", Type.INFO))
            Timer().schedule(sendDelay.toLong()) {
                mc.thePlayer.sendChatMessage("/register $password $password")
            }
            true
        }
        login && "/log" in msg -> {
            addNotification(Notification("Trying to log in.", "Trying to log in.", Type.INFO))
            Timer().schedule(sendDelay.toLong()) {
                mc.thePlayer.sendChatMessage("/login $password")
            }
            true
        }
        else -> false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        when (val packet = event.packet) {
            is S02PacketChat, is S45PacketTitle -> {
                // Don't respond to register / login prompts when failed once
                if (!passwordValue.isSupported() || status == Status.STOPPED) return

                val msg = when (packet) {
                    is S02PacketChat -> packet.chatComponent?.unformattedText?.lowercase()
                    is S45PacketTitle -> packet.message?.unformattedText?.lowercase()
                    else -> return
                } ?: return

                if (status == Status.WAITING) {
                    // Try to register / log in, return if invalid message
                    if (!respond(msg)) return

                    event.cancelEvent()
                    status = Status.SENT_COMMAND
                } else {
                    // Check response from server
                    when {
                        // Logged in
                        "success" in msg || "logged" in msg || "registered" in msg -> {
                            success()
                            event.cancelEvent()
                        }
                        // Login failed, possibly relog
                        "incorrect" in msg || "wrong" in msg || "spatne" in msg -> fail()
                        "unknown" in msg || "command" in msg || "allow" in msg || "already" in msg -> {
                            // Tried executing /login or /register from lobby, stop trying
                            status = Status.STOPPED
                            event.cancelEvent()
                        }
                    }
                }
            }
            is S40PacketDisconnect -> {
                if (relogKickedValue.isActive() && status != Status.SENT_COMMAND) {
                    val reason = packet.reason.unformattedText
                    if ("ban" in reason) return

                    relog(packet.reason.unformattedText)
                }
            }
        }

    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (!passwordValue.isSupported()) return

        // Reset status if player wasn't in a world before
        if (mc.theWorld == null) {
            status = Status.WAITING
            return
        }

        if (status == Status.SENT_COMMAND) {
            // Server redirected the player to a lobby, success
            if (event.worldClient != null && mc.theWorld != event.worldClient) success()
            // Login failed, possibly relog
            else fail()
        }
    }

    @EventTarget
    fun onStartup(startupEvent: StartUpEvent) {
        // Log in to account with a random name after startup, optionally save it
        if (startupValue.isActive()) changeAccount()
    }

    // Login succeeded
    private fun success() {
        if (status == Status.SENT_COMMAND) {
            addNotification(Notification("Logged in as ${mc.session.username}", "Logged", Type.SUCCESS))

            // Stop waiting for response
            status = Status.STOPPED
        }
    }

    // Login failed
    private fun fail() {
        if (status == Status.SENT_COMMAND) {
            addNotification(Notification("Failed to log in as ${mc.session.username}", "ERROR", Type.ERROR))

            // Stop waiting for response
            status = Status.STOPPED

            // Trigger relog task
            if (relogInvalidValue.isActive()) relog()
        }
    }

    private fun changeAccount() {
        if (accountMode == "RandomAlt") {
            val account = accountsConfig.accounts.filter { it is CrackedAccount && it.name != mc.session.username }
                .randomOrNull() ?: return
            mc.session = Session(
                account.session.username, account.session.uuid,
                account.session.token, account.session.type
            )
            callEvent(SessionEvent())
            return
        }

        // Log in to account with a random name
        val account = randomAccount()

        // Save as a new account if SaveToAlts is enabled
        if (saveValue.isActive() && !accountsConfig.accountExists(account)) {
            accountsConfig.addAccount(account)
            accountsConfig.saveConfig()

            addNotification(Notification("Saved alt ${account.name}", "Sucess", Type.SUCCESS))
        }
    }

    private enum class Status {
        WAITING, SENT_COMMAND, STOPPED
    }
}