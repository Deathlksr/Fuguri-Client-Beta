package net.deathlksr.fuguribeta.handler.discord

import com.jagrosh.discordipc.IPCClient
import com.jagrosh.discordipc.IPCListener
import com.jagrosh.discordipc.entities.RichPresence
import com.jagrosh.discordipc.entities.pipe.PipeStatus
import net.deathlksr.fuguribeta.FuguriBeta.CLIENT_VERSION
import net.deathlksr.fuguribeta.utils.ClientUtils.LOGGER
import net.deathlksr.fuguribeta.utils.MinecraftInstance
import net.deathlksr.fuguribeta.utils.ServerUtils
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
                if (mc.isSingleplayer && serverData == null) {
                    setDetails("Playing lonely in Singleplayer")
                    setState("In-game")
                } else if (!mc.isSingleplayer && serverData != null) {
                    setDetails("Maybe cheating on ${ServerUtils.hideSensitiveInformation(serverData.serverIP)}")
                    setState("Cheating")
                } else if (!mc.isSingleplayer && serverData == null) {
                    setDetails("Multiplayer menu")
                    setState("AFK")
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
