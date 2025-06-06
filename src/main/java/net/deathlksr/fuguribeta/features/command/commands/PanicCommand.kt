/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.command.commands

import net.deathlksr.fuguribeta.FuguriBeta.moduleManager
import net.deathlksr.fuguribeta.features.command.Command
import net.deathlksr.fuguribeta.features.module.Category

object PanicCommand : Command("panic") {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        var modules = moduleManager.modules.filter { it.state }
        val msg: String

        if (args.size > 1 && args[1].isNotEmpty()) {
            when (args[1].lowercase()) {
                "all" -> msg = "all"

                "nonrender" -> {
                    modules = modules.filter { it.category != Category.VISUAL }
                    msg = "all non-render"
                }

                else -> {
                    val categories = Category.values().filter { it.displayName.equals(args[1], true) }

                    if (categories.isEmpty()) {
                        chat("Category ${args[1]} not found")
                        return
                    }

                    val category = categories[0]
                    modules = modules.filter { it.category == category }
                    msg = "all ${category.displayName}"
                }
            }
        } else {
            chatSyntax("panic <all/nonrender/combat/player/movement/render/world/misc/exploit/fun>")
            return
        }

        for (module in modules)
            module.state = false

        chat("Disabled $msg modules.")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("all", "nonrender", "combat", "player", "movement", "render", "world", "misc", "exploit", "fun")
                .filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}
