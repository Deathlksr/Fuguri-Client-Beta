package net.deathlksr.fuguribeta

import kotlinx.coroutines.runBlocking
import net.deathlksr.fuguribeta.event.ClientShutdownEvent
import net.deathlksr.fuguribeta.event.EventManager
import net.deathlksr.fuguribeta.event.EventManager.callEvent
import net.deathlksr.fuguribeta.event.EventManager.registerListener
import net.deathlksr.fuguribeta.event.StartUpEvent
import net.deathlksr.fuguribeta.features.command.CommandManager
import net.deathlksr.fuguribeta.features.command.CommandManager.registerCommands
import net.deathlksr.fuguribeta.features.module.ModuleManager
import net.deathlksr.fuguribeta.features.module.ModuleManager.registerModules
import net.deathlksr.fuguribeta.features.module.modules.client.ClickGUIModule.volume
import net.deathlksr.fuguribeta.features.module.modules.player.scaffolds.Tower
import net.deathlksr.fuguribeta.features.module.modules.visual.NameTags.getListsFromGitHub
import net.deathlksr.fuguribeta.file.FileManager
import net.deathlksr.fuguribeta.file.FileManager.loadAllConfigs
import net.deathlksr.fuguribeta.file.FileManager.saveAllConfigs
import net.deathlksr.fuguribeta.handler.api.ClientUpdate.gitInfo
import net.deathlksr.fuguribeta.handler.api.loadSettings
import net.deathlksr.fuguribeta.handler.cape.CapeService
import net.deathlksr.fuguribeta.handler.combat.CombatManager
import net.deathlksr.fuguribeta.handler.discord.DiscordRPC
import net.deathlksr.fuguribeta.handler.discord.DiscordRPC.ClientRPCStarted
import net.deathlksr.fuguribeta.handler.lang.LanguageManager.loadLanguages
import net.deathlksr.fuguribeta.handler.macro.MacroManager
import net.deathlksr.fuguribeta.handler.payload.ClientFixes
import net.deathlksr.fuguribeta.handler.tabs.BlocksTab
import net.deathlksr.fuguribeta.handler.tabs.ExploitsTab
import net.deathlksr.fuguribeta.handler.tabs.HeadsTab
import net.deathlksr.fuguribeta.script.ScriptManager
import net.deathlksr.fuguribeta.script.ScriptManager.enableScripts
import net.deathlksr.fuguribeta.script.ScriptManager.loadScripts
import net.deathlksr.fuguribeta.script.remapper.Remapper
import net.deathlksr.fuguribeta.script.remapper.Remapper.loadSrg
import net.deathlksr.fuguribeta.ui.client.altmanager.GuiAltManager.Companion.loadActiveGenerators
import net.deathlksr.fuguribeta.ui.client.clickgui.ClickGui
import net.deathlksr.fuguribeta.ui.client.gui.GuiClientConfiguration.Companion.updateClientWindow
import net.deathlksr.fuguribeta.ui.client.hud.HUD
import net.deathlksr.fuguribeta.ui.client.keybind.KeyBindManager
import net.deathlksr.fuguribeta.ui.font.Fonts.loadFonts
import net.deathlksr.fuguribeta.utils.*
import net.deathlksr.fuguribeta.utils.ClassUtils.hasForge
import net.deathlksr.fuguribeta.utils.ClientUtils.LOGGER
import net.deathlksr.fuguribeta.utils.ClientUtils.disableFastRender
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils
import net.deathlksr.fuguribeta.utils.misc.sound.TipSoundManager
import net.deathlksr.fuguribeta.utils.render.MiniMapRegister
import net.deathlksr.fuguribeta.utils.timing.TickedActions
import net.deathlksr.fuguribeta.utils.timing.WaitTickUtils
import kotlin.concurrent.thread

object FuguriBeta {

    /**
     * Client Information
     *
     * This has all the basic information.
     */

    const val CLIENT_NAME = "FuguriBeta"
    const val CLIENT_AUTHOR = "Deathlksr"
    const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"
    const val CLIENT_WEBSITE = "fuguri.top"
    const val CLIENT_VERSION = "B4.0"

    val clientVersionText = gitInfo["git.build.version"]?.toString() ?: "unknown"
    val clientVersionNumber = clientVersionText.substring(1).toIntOrNull() ?: 0 // version format: "b<VERSION>" on legacy
    val clientCommit = gitInfo["git.commit.id.abbrev"]?.let { "git-$it" } ?: "unknown"
    val clientBranch = gitInfo["git.branch"]?.toString() ?: "unknown"

    /**
     * Defines if the client is in development mode.
     * This will enable update checking on commit time instead of regular legacy versioning.
     */
    const val IN_DEV = false

    const val clientTitle = "Fuguri Beta $CLIENT_VERSION"

    var isStarting = true
    var isLoadingConfig = true

    // Managers
    val moduleManager = ModuleManager
    val commandManager = CommandManager
    val eventManager = EventManager
    val fileManager = FileManager
    val scriptManager = ScriptManager
    var combatManager = CombatManager
    val keyBindManager = KeyBindManager
    val macroManager = MacroManager
    lateinit var tipSoundManager: TipSoundManager

    // HUD & ClickGUI
    val hud = HUD
    val clickGui = ClickGui

    // Menu Background
    var background: Background? = null

    // Discord RPC
    var discordRPC = DiscordRPC

    /**
     * Execute if client will be started
     */
    fun startClient() {
        isStarting = true
        isLoadingConfig = true

        LOGGER.info("Starting Fuguri Beta")

        // Init SoundManager
        tipSoundManager = TipSoundManager()

        // Load languages
        loadLanguages()

        runCatching {

            // Register listeners
            registerListener(RotationUtils)
            registerListener(ClientFixes)

            registerListener(CapeService)
            registerListener(combatManager)
            registerListener(macroManager)
            registerListener(InventoryUtils)
            registerListener(MiniMapRegister)
            registerListener(TickedActions)
            registerListener(MovementUtils)
            registerListener(PacketUtils)
            registerListener(TimerBalanceUtils)
            registerListener(BPSUtils)
            registerListener(Tower)
            registerListener(WaitTickUtils)

            // Get Repository GitHub
            runBlocking {
                getListsFromGitHub()
            }

            // Load client fonts
            loadFonts()

            // Load settings
            loadSettings(false) {
                LOGGER.info("Successfully loaded ${it.size} settings.")
            }

            // Register commands
            registerCommands()
            KeyBindManager

            // Setup module manager and register modules
            registerModules()

            APIConnecter.checkStatus()
            APIConnecter.checkChangelogs()
            APIConnecter.checkBugs()
            APIConnecter.loadPictures()
            APIConnecter.loadDonors()

            runCatching {
                // Remapper
                loadSrg()

                if (!Remapper.mappingsLoaded) {
                    error("Failed to load SRG mappings.")
                }

                // ScriptManager
                loadScripts()
                enableScripts()
            }.onFailure {
                LOGGER.error("Failed to load scripts.", it)
            }

            // Load configs
            loadAllConfigs()

            // Update client window
            updateClientWindow()

            // Tabs (Only for Forge!)
            if (hasForge()) {
                BlocksTab()
                ExploitsTab()
                HeadsTab()
            }

            // Disable optifine fastrender
            disableFastRender()

            // Load alt generators
            loadActiveGenerators()

            // Setup Discord RPC
            if (ClientRPCStarted) {
                thread {
                    try {
                        DiscordRPC.setup()
                    } catch (throwable: Throwable) {
                        LOGGER.error("Failed to setup Discord RPC.", throwable)
                    }
                }
            }

            // init discord rpc
            discordRPC = DiscordRPC

            // Login into known token if not empty
            if (CapeService.knownToken.isNotBlank()) {
                runCatching {
                    CapeService.login(CapeService.knownToken)
                }.onFailure {
                    LOGGER.error("Failed to login into known cape token.", it)
                }.onSuccess {
                    LOGGER.info("Successfully logged in into known cape token.")
                }
            }

            // Refresh cape service
            CapeService.refreshCapeCarriers {
                LOGGER.info("Successfully loaded ${CapeService.capeCarriers.size} cape carriers.")
            }

            // Load background
            FileManager.loadBackground()

        }.onFailure {
            LOGGER.error("Failed to start client ${it.message}")
        }.onSuccess {
            // Set is starting status
            isStarting = false

            callEvent(StartUpEvent())
            LOGGER.info("Successfully started client")
            tipSoundManager.startUpSound.asyncPlay(volume)
        }
    }

    /**
     * Execute if client will be stopped
     */
    fun stopClient() {
        // Call client shutdown
        callEvent(ClientShutdownEvent())

        tipSoundManager.shutdownSound.asyncPlay(volume)

        // Save all available configs
        saveAllConfigs()

        // Shutdown discord rpc
        discordRPC.shutdown()
    }
}