/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import kotlinx.coroutines.*
import net.deathlksr.fuguribeta.FuguriBeta.hud
import net.deathlksr.fuguribeta.FuguriBeta.CLIENT_CLOUD
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.event.WorldEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.script.api.global.Chat
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Notification
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Type
import net.deathlksr.fuguribeta.utils.misc.HttpUtils
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.entity.Entity
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.network.Packet
import net.minecraft.network.play.server.*
import java.util.concurrent.ConcurrentHashMap

object StaffDetector : Module("StaffDetector", Category.OTHER, gameDetecting = false, hideModule = false) {

    private val staffMode by object : ListValue("StaffMode", arrayOf("BlocksMC", "CubeCraft", "Gamster", "AgeraPvP", "HypeMC", "Hypixel", "SuperCraft"), "BlocksMC") {
        override fun onUpdate(value: String) {
            loadStaffData()
        }
    }

    private val tab by BoolValue("TAB", true)
    private val packet by BoolValue("Packet", true)

    private val autoLeave by ListValue("AutoLeave", arrayOf("Off", "Leave", "Lobby", "Quit"), "Off") { tab || packet }

    private val spectator by BoolValue("StaffSpectator", false) { tab || packet }
    private val otherSpectator by BoolValue("OtherSpectator", false) { tab || packet }

    private val inGame by BoolValue("InGame", true) { autoLeave != "Off" }
    private val warn by ListValue("Warn", arrayOf("Chat", "Notification"), "Chat")

    private val checkedStaff = ConcurrentHashMap.newKeySet<String>()
    private val checkedSpectator = ConcurrentHashMap.newKeySet<String>()
    private val playersInSpectatorMode = ConcurrentHashMap.newKeySet<String>()

    private var attemptLeave = false

    private var staffList = mapOf<String, Set<String>?>()
    private var serverIp = ""

    private val moduleJob = SupervisorJob()
    private val moduleScope = CoroutineScope(Dispatchers.IO + moduleJob)

    override fun onDisable() {
        serverIp = ""
        moduleJob.cancel()
        checkedStaff.clear()
        checkedSpectator.clear()
        playersInSpectatorMode.clear()
        attemptLeave = false
    }

    /**
     * Reset on World Change
     */
    @EventTarget
    fun onWorld(event: WorldEvent) {
        checkedStaff.clear()
        checkedSpectator.clear()
        playersInSpectatorMode.clear()
    }

    private fun loadStaffData() {
        val serverIpMap = mapOf(
            "blocksmc" to "blocksmc.com",
            "cubecraft" to "cubecraft.net",
            "agerapvp" to "agerapvp.club",
            "hypemc" to "hypemc.pro",
            "hypixel" to "hypixel.net",
            "supercraft" to "supercraft.es"
        )

        serverIp = serverIpMap[staffMode.lowercase()] ?: return

        moduleScope.launch {
            staffList = loadStaffList("$CLIENT_CLOUD/staffs/$serverIp")
        }
    }

    private fun checkedStaffRemoved() {
        val onlinePlayers = mc.netHandler?.playerInfoMap?.mapNotNull { it?.gameProfile?.name }

        synchronized(checkedStaff) {
            onlinePlayers?.toSet()?.let { checkedStaff.retainAll(it) }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val packet = event.packet

        /**
         * OLD BlocksMC Staff Spectator Check
         * Original By HU & Modified by Eclipses
         *
         * NOTE: Doesn't detect staff spectator all the time.
         */
        if (spectator) {
            if (packet is S3EPacketTeams) {
                val teamName = packet.name

                if (teamName.equals("Z_Spectator", true)) {
                    val players = packet.players ?: return

                    val staffSpectateList = players.filter { it in staffList.keys } - checkedSpectator
                    val nonStaffSpectateList = players.filter { it !in staffList.keys } - checkedSpectator

                    // Check for players who are using spectator menu
                    val miscSpectatorList = playersInSpectatorMode - players.toSet()

                    staffSpectateList.forEach { player ->
                        notifySpectators(player!!)
                    }

                    nonStaffSpectateList.forEach { player ->
                        if (otherSpectator) {
                            notifySpectators(player!!)
                        }
                    }

                    miscSpectatorList.forEach { player ->
                        val isStaff = player in staffList

                        if (isStaff && spectator) {
                            Chat.print("§c[STAFF] §d${player} §3is using the spectator menu §e(compass/left)")
                        }

                        if (!isStaff && otherSpectator) {
                            Chat.print("§d${player} §3is using the spectator menu §e(compass/left)")
                        }
                        checkedSpectator.remove(player)
                    }

                    // Update the set of players in spectator mode
                    playersInSpectatorMode.clear()
                    playersInSpectatorMode.addAll(players)
                }
            }

            // Handle other packets
            handleOtherChecks(packet)
        }
    }

    private fun notifySpectators(player: String) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val isStaff = staffList.any { entry ->
            entry.value?.any { staffName -> player.contains(staffName) } == true
        }

        if (isStaff && spectator) {
            if (warn == "Chat") {
                Chat.print("§c[STAFF] §d${player} §3is a spectators")
            } else {
                hud.addNotification(Notification("§c[STAFF] §d${player} §3is a spectators", "!!!", Type.INFO, 1000))
            }
        }

        if (!isStaff && otherSpectator) {
            if (warn == "Chat") {
                Chat.print("§d${player} §3is a spectators")
            } else {
                hud.addNotification(Notification("§d${player} §3is a spectators", "!!!", Type.INFO, 60))
            }
        }

        attemptLeave = false
        checkedSpectator.add(player)

        if (isStaff) {
            autoLeave()
        }
    }

    /**
     * Check staff using TAB
     */
    private fun notifyStaff() {
        if (!tab)
            return

        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val playerInfoMap = mc.netHandler?.playerInfoMap ?: return

        val playerInfos = synchronized(playerInfoMap) {
            playerInfoMap.mapNotNull { playerInfo ->
                playerInfo?.gameProfile?.name?.let { playerName ->
                    playerName to playerInfo.responseTime
                }
            }
        }

        playerInfos.forEach { (player, responseTime) ->
            val isStaff = staffList.any { entry ->
                entry.value?.any { staffName -> player.contains(staffName) } == true
            }

            val condition = when {
                responseTime > 0 -> "§e(${responseTime}ms)"
                responseTime == 0 -> "§a(Joined)"
                else -> "§c(Ping error)"
            }

            val warnings = "§c[STAFF] §d${player} §3is a staff §b(TAB) $condition"

            synchronized(checkedStaff) {
                if (isStaff && player !in checkedStaff) {
                    if (warn == "Chat") {
                        Chat.print(warnings)
                    } else {
                        hud.addNotification(Notification(warnings, "!!!", Type.WARNING, 60))
                    }

                    attemptLeave = false
                    checkedStaff.add(player)

                    autoLeave()
                }
            }
        }
    }

    /**
     * Check staff using Packet
     */
    private fun notifyStaffPacket(staff: Entity) {
        if (!packet)
            return

        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        val isStaff = if (staff is EntityPlayer) {
            val playerName = staff.gameProfile.name

            staffList.any { entry ->
                entry.value?.any { staffName -> playerName.contains(staffName) } == true
            }
        } else {
            false
        }

        val condition = when (staff) {
            is EntityPlayer -> {
                val responseTime = mc.netHandler?.getPlayerInfo(staff.uniqueID)?.responseTime ?: 0
                when {
                    responseTime > 0 -> "§e(${responseTime}ms)"
                    responseTime == 0 -> "§a(Joined)"
                    else -> "§c(Ping error)"
                }
            }
            else -> ""
        }

        val playerName = if (staff is EntityPlayer) staff.gameProfile.name else ""

        val warnings = "§c[STAFF] §d${playerName} §3is a staff §b(Packet) $condition"

        synchronized(checkedStaff) {
            if (isStaff && playerName !in checkedStaff) {
                if (warn == "Chat") {
                    Chat.print(warnings)
                } else {
                    hud.addNotification(Notification(warnings, "!!!", Type.WARNING, 60))
                }

                attemptLeave = false
                checkedStaff.add(playerName)

                autoLeave()
            }
        }
    }

    private fun autoLeave() {
        val firstSlotItemStack = mc.thePlayer.inventory.mainInventory[0] ?: return

        if (inGame && (firstSlotItemStack.item == Items.compass || firstSlotItemStack.item == Items.bow)) {
            return
        }

        if (!attemptLeave && autoLeave != "Off") {
            when (autoLeave.lowercase()) {
                "leave" -> mc.thePlayer.sendChatMessage("/leave")
                "lobby" -> mc.thePlayer.sendChatMessage("/lobby")
                "quit" -> mc.theWorld.sendQuittingDisconnectingPacket()
            }
            attemptLeave = true
        }
    }

    private fun handleOtherChecks(packet: Packet<*>?) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        fun handlePlayer(player: Entity?) {
            player ?: return
            handleStaff(player)
        }

        when (packet) {
            is S01PacketJoinGame -> handlePlayer(mc.theWorld.getEntityByID(packet.entityId))
            is S0CPacketSpawnPlayer -> handlePlayer(mc.theWorld.getEntityByID(packet.entityID))
            is S18PacketEntityTeleport -> handlePlayer(mc.theWorld.getEntityByID(packet.entityId))
            is S1CPacketEntityMetadata -> handlePlayer(mc.theWorld.getEntityByID(packet.entityId))
            is S1DPacketEntityEffect -> handlePlayer(mc.theWorld.getEntityByID(packet.entityId))
            is S1EPacketRemoveEntityEffect -> handlePlayer(mc.theWorld.getEntityByID(packet.entityId))
            is S19PacketEntityStatus -> handlePlayer(mc.theWorld.getEntityByID(packet.entityId))
            is S19PacketEntityHeadLook -> handlePlayer(packet.getEntity(mc.theWorld))
            is S49PacketUpdateEntityNBT -> handlePlayer(packet.getEntity(mc.theWorld))
            is S1BPacketEntityAttach -> handlePlayer(mc.theWorld.getEntityByID(packet.entityId))
            is S04PacketEntityEquipment -> handlePlayer(mc.theWorld.getEntityByID(packet.entityID))
        }
    }

    private fun handleStaff(staff: Entity) {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        checkedStaffRemoved()

        notifyStaff()
        notifyStaffPacket(staff)
    }

    private suspend fun loadStaffList(url: String): Map<String, Set<String>> {
        return try {
            val (response, code) = fetchDataAsync(url)

            when (code) {
                200 -> {
                    val staffList = response.lineSequence()
                        .filter { it.isNotBlank() }
                        .map { it.trim() }
                        .toSet()

                    Chat.print("§aSuccessfully loaded §9${staffList.size} §astaff names.")
                    mapOf(url to staffList)
                }
                404 -> {
                    Chat.print("§cFailed to load staff list. §9(§3Doesn't exist in LiquidCloud§9)")
                    emptyMap()
                }
                else -> {
                    Chat.print("§cFailed to load staff list. §9(§3ERROR CODE: $code§9)")
                    emptyMap()
                }
            }
        } catch (e: Exception) {
            Chat.print("§cFailed to load staff list. §9(${e.message})")
            e.printStackTrace()
            emptyMap()
        }
    }

    private suspend fun fetchDataAsync(url: String): Pair<String, Int> {
        return withContext(Dispatchers.IO) {
            HttpUtils.request(url, "GET").let { Pair(it.first, it.second) }
        }
    }

    /**
     * HUD TAG
     */
    override val tag
        get() = staffMode
}