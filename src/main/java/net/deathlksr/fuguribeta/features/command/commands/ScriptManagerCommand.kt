/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.command.commands

import net.deathlksr.fuguribeta.FuguriBeta.isStarting
import net.deathlksr.fuguribeta.FuguriBeta.moduleManager
import net.deathlksr.fuguribeta.FuguriBeta.scriptManager
import net.deathlksr.fuguribeta.features.command.Command
import net.deathlksr.fuguribeta.features.command.CommandManager
import net.deathlksr.fuguribeta.file.FileManager.clickGuiConfig
import net.deathlksr.fuguribeta.file.FileManager.hudConfig
import net.deathlksr.fuguribeta.file.FileManager.loadConfig
import net.deathlksr.fuguribeta.file.FileManager.loadConfigs
import net.deathlksr.fuguribeta.file.FileManager.modulesConfig
import net.deathlksr.fuguribeta.file.FileManager.valuesConfig
import net.deathlksr.fuguribeta.script.ScriptManager.reloadScripts
import net.deathlksr.fuguribeta.script.ScriptManager.scripts
import net.deathlksr.fuguribeta.script.ScriptManager.scriptsFolder
import net.deathlksr.fuguribeta.utils.ClientUtils.LOGGER
import net.deathlksr.fuguribeta.utils.misc.MiscUtils
import org.apache.commons.io.IOUtils
import java.awt.Desktop
import java.io.File
import java.util.zip.ZipFile

object ScriptManagerCommand : Command("scriptmanager", "scripts") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        val usedAlias = args[0].lowercase()

        if (args.size < 2) {
            chatSyntax("$usedAlias <import/delete/reload/folder>")
            return
        }

        when (args[1].lowercase()) {
            "import" -> {
                try {
                    val file = MiscUtils.openFileChooser() ?: return
                    val fileName = file.name

                    if (fileName.endsWith(".js")) {
                        scriptManager.importScript(file)

                        loadConfig(clickGuiConfig)

                        chat("Successfully imported script.")
                        return
                    } else if (fileName.endsWith(".zip")) {
                        val zipFile = ZipFile(file)
                        val entries = zipFile.entries()
                        val scriptFiles = arrayListOf<File>()

                        while (entries.hasMoreElements()) {
                            val entry = entries.nextElement()
                            val entryName = entry.name
                            val entryFile = File(scriptsFolder, entryName)

                            if (entry.isDirectory) {
                                entryFile.mkdir()
                                continue
                            }

                            val fileStream = zipFile.getInputStream(entry)
                            val fileOutputStream = entryFile.outputStream()

                            IOUtils.copy(fileStream, fileOutputStream)
                            fileOutputStream.close()
                            fileStream.close()

                            if ("/" in entryName)
                                scriptFiles += entryFile
                        }

                        scriptFiles.forEach { scriptFile -> scriptManager.loadScript(scriptFile) }

                        loadConfigs(clickGuiConfig, hudConfig)

                        chat("Successfully imported script.")
                        return
                    }

                    chat("The file extension has to be .js or .zip")
                } catch (t: Throwable) {
                    LOGGER.error("Something went wrong while importing a script.", t)
                    chat("${t.javaClass.name}: ${t.message}")
                }
            }

            "delete" -> {
                try {
                    if (args.size <= 2) {
                        chatSyntax("$usedAlias delete <index>")
                        return
                    }

                    val scriptIndex = args[2].toInt()

                    if (scriptIndex >= scripts.size) {
                        chat("Index $scriptIndex is too high.")
                        return
                    }

                    val script = scripts[scriptIndex]

                     scriptManager.deleteScript(script)

                    loadConfigs(clickGuiConfig, hudConfig)

                    chat("Successfully deleted script.")
                } catch (numberFormat: NumberFormatException) {
                    chatSyntaxError()
                } catch (t: Throwable) {
                    LOGGER.error("Something went wrong while deleting a script.", t)
                    chat("${t.javaClass.name}: ${t.message}")
                }
            }

            "reload" -> {
                try {
                    CommandManager.registerCommands()

                    isStarting = true

                    reloadScripts()

                    for (module in moduleManager.modules) moduleManager.generateCommand(module)
                    loadConfig(modulesConfig)

                    isStarting = false
                    loadConfigs(valuesConfig, clickGuiConfig, hudConfig)

                    chat("Successfully reloaded all scripts.")
                } catch (t: Throwable) {
                    LOGGER.error("Something went wrong while reloading all scripts.", t)
                    chat("${t.javaClass.name}: ${t.message}")
                }
            }

            "folder" -> {
                try {
                    Desktop.getDesktop().open(scriptsFolder)
                    chat("Successfully opened scripts folder.")
                } catch (t: Throwable) {
                    LOGGER.error("Something went wrong while trying to open your scripts folder.", t)
                    chat("${t.javaClass.name}: ${t.message}")
                }
            }
        }

        return

        val scriptManager = scriptManager

        if (scriptManager.scripts.isNotEmpty()) {
            chat("§c§lScripts")
            scriptManager.scripts.forEachIndexed { index, script -> chat("$index: §a§l${script.scriptName} §a§lv${script.scriptVersion} §3by §a§l${script.scriptAuthors.joinToString(", ")}") }
        }

        chatSyntax("$usedAlias <import/delete/reload/folder>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("delete", "import", "folder", "reload")
                .filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}
