package net.ccbluex.liquidbounce.handler.discord

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import net.ccbluex.liquidbounce.FuguriBeta.CLIENT_VERSION
import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.MinecraftInstance
import net.ccbluex.liquidbounce.utils.ServerUtils
import net.minecraft.client.gui.GuiMultiplayer
import java.time.OffsetDateTime
import kotlin.concurrent.thread

object DiscordRPC : MinecraftInstance() {

    var ClientRPCStarted = false

    // IPC Client
    private var ipcClient : IPCClient? = null

    private var appID = 1282692956700016640
    private val timestamp = OffsetDateTime.now()

    var running = false

    fun setup() {
        try {
            running = true

            loadConfiguration()

            ipcClient = IPCClient(appID).apply {
                setListener(object : IPCListener {

                    override fun onReady(client : IPCClient?) {
                        thread {
                            while (running) {
                                update()
                                Thread.sleep(1000L)
                            }
                        }
                    }
                })
            }
            ipcClient?.connect()
        } catch (e : Throwable) {
            LOGGER.error("Failed to setup Discord RPC.", e)
        }

    }

    fun update() {
        if (ipcClient?.status != PipeStatus.CONNECTED) return

        val builder = RichPresence.Builder().apply {
            // Set playing time
            setStartTimestamp(timestamp)

            // Set logo
            setLargeImage("logo", "Fuguri-Beta $CLIENT_VERSION")

            // Check user is in-game
            mc.thePlayer?.let {
                val serverData = mc.currentServerData

                // Set display info
                if (mc.isIntegratedServerRunning || serverData == null) {
                    setDetails("Playing lonely in Singleplayer")
                    setState("In-game")
                } else {
                    setDetails("Maybe cheating on ${ServerUtils.hideSensitiveInformation(serverData.serverIP)}")
                    setState("Cheating")
                }

                val screen = mc.currentScreen

                if (screen is GuiMultiplayer) {
                    setDetails("Idle")
                    setState("Multiplayer-menu")
                }
            }
        }

        // Check ipc client is connected and send rpc
        if (ipcClient?.status == PipeStatus.CONNECTED)
            ipcClient?.sendRichPresence(builder.build())
    }

    /**
     * Shutdown ipc client
     */
    fun shutdown() {
        if (ipcClient?.status != PipeStatus.CONNECTED) {
            return
        }

        try {
            ipcClient?.close()
        } catch (e : Throwable) {
            LOGGER.error("Failed to close Discord RPC.", e)
        }
    }

    private fun loadConfiguration() {
        appID = 1282692956700016640
    }
}
