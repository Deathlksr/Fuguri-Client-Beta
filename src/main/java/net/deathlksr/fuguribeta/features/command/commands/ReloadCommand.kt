/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.command.commands

import net.deathlksr.fuguribeta.FuguriBeta.isStarting
import net.deathlksr.fuguribeta.FuguriBeta.moduleManager
import net.deathlksr.fuguribeta.features.command.Command
import net.deathlksr.fuguribeta.features.command.CommandManager
import net.deathlksr.fuguribeta.file.FileManager.accountsConfig
import net.deathlksr.fuguribeta.file.FileManager.clickGuiConfig
import net.deathlksr.fuguribeta.file.FileManager.friendsConfig
import net.deathlksr.fuguribeta.file.FileManager.hudConfig
import net.deathlksr.fuguribeta.file.FileManager.loadConfig
import net.deathlksr.fuguribeta.file.FileManager.modulesConfig
import net.deathlksr.fuguribeta.file.FileManager.valuesConfig
import net.deathlksr.fuguribeta.script.ScriptManager.disableScripts
import net.deathlksr.fuguribeta.script.ScriptManager.reloadScripts
import net.deathlksr.fuguribeta.script.ScriptManager.unloadScripts
import net.deathlksr.fuguribeta.ui.font.Fonts

object ReloadCommand : Command("reload", "configreload") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        chat("Reloading...")
        isStarting = true

        chat("§c§lReloading commands...")
        CommandManager.registerCommands()

        disableScripts()
        unloadScripts()

        for (module in moduleManager.modules)
            moduleManager.generateCommand(module)

        chat("§c§lReloading scripts...")
        reloadScripts()

        chat("§c§lReloading fonts...")
        Fonts.loadFonts()

        chat("§c§lReloading modules...")
        loadConfig(modulesConfig)


        chat("§c§lReloading values...")
        loadConfig(valuesConfig)

        chat("§c§lReloading accounts...")
        loadConfig(accountsConfig)

        chat("§c§lReloading friends...")
        loadConfig(friendsConfig)

        chat("§c§lReloading HUD...")
        loadConfig(hudConfig)

        chat("§c§lReloading ClickGUI...")
        loadConfig(clickGuiConfig)

        isStarting = false
        chat("Reloaded.")
    }
}
