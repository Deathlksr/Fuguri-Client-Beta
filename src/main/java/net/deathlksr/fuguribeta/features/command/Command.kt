/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.command

import net.deathlksr.fuguribeta.FuguriBeta
import net.deathlksr.fuguribeta.FuguriBeta.commandManager
import net.deathlksr.fuguribeta.features.module.modules.client.ClickGUIModule
import net.deathlksr.fuguribeta.utils.ClientUtils
import net.deathlksr.fuguribeta.utils.ClientUtils.displayChatMessage
import net.deathlksr.fuguribeta.utils.MinecraftInstance

abstract class Command(val command: String, vararg val alias: String) : MinecraftInstance() {
    /**
     * Execute commands with provided [args]
     */
    abstract fun execute(args: Array<String>)

    /**
     * Returns a list of command completions based on the provided [args].
     * If a command does not implement [tabComplete] an [EmptyList] is returned by default.
     *
     * @param args an array of command arguments that the player has passed to the command so far
     * @return a list of matching completions for the command the player is trying to autocomplete
     * @author NurMarvin
     */
    open fun tabComplete(args: Array<String>) = emptyList<String>()


    /**
     * Print [msg] to chat as alert
     */
    protected fun alert(msg: String) = ClientUtils.displayAlert(msg)

    /**
     * Print [msg] to chat
     */
    protected fun chat(msg: String) = displayChatMessage("§3$msg")

    /**
     * Print [syntax] of command to chat
     */
    protected fun chatSyntax(syntax: String) = displayChatMessage("§3Syntax: §7${commandManager.prefix}$syntax")

    /**
     * Print [syntaxes] of command to chat
     */
    protected fun chatSyntax(syntaxes: Array<String>) {
        displayChatMessage("§3Syntax:")

        for (syntax in syntaxes)
            displayChatMessage("§8> §7${commandManager.prefix}$command ${syntax.lowercase()}")
    }

    /**
     * Print a syntax error to chat
     */
    protected fun chatSyntaxError() = displayChatMessage("§3Syntax error")

    /**
     * Play edit sound
     */
    protected fun playEdit() = FuguriBeta.tipSoundManager.loginSuccessfulSound.asyncPlay(ClickGUIModule.volume)
}