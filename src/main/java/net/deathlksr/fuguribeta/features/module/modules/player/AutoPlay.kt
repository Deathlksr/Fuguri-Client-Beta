/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.init.Items
import net.minecraft.item.ItemStack

object AutoPlay : Module("AutoPlay", Category.PLAYER, gameDetecting = false, hideModule = false) {

    private val mode by ListValue("Mode", arrayOf("Paper", "Hypixel"), "Paper")

    // Hypixel Settings
    private val hypixelMode by ListValue("HypixelMode", arrayOf("Skywars", "Bedwars"), "Skywars") {
        mode == "Hypixel"
    }
    private val skywarsMode by ListValue("SkywarsMode", arrayOf("SoloNormal", "SoloInsane"), "SoloNormal") {
        hypixelMode == "Skywars"
    }
    private val bedwarsMode by ListValue("BedwarsMode", arrayOf("Solo", "Double", "Trio", "Quad"), "Solo") {
        hypixelMode == "Bedwars"
    }

    private val delay by IntegerValue("Delay", 50, 0..200)

    private var delayTick = 0

    /**
     * Update Event
     */
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return

        if (!playerInGame() || !player.inventory.hasItemStack(ItemStack(Items.paper))) {
            if (delayTick > 0)
                delayTick = 0

            return
        } else {
            delayTick++
        }

        when (mode) {
            "Paper" -> {
                val paper = InventoryUtils.findItem(36, 44, Items.paper) ?: return

                player.inventory.currentItem = (paper - 36)
                mc.playerController.updateController()

                if (delayTick >= delay) {
                    mc.playerController.sendUseItem(player, mc.theWorld, player.inventoryContainer.getSlot(paper).stack)
                    delayTick = 0
                }
            }

            "Hypixel" -> {
                if (delayTick >= delay) {
                    if (hypixelMode == "Skywars") {
                        when (skywarsMode) {
                            "SoloNormal" -> player.sendChatMessage("/play solo_normal")
                            "SoloInsane" -> player.sendChatMessage("/play solo_insane")
                        }
                    } else {
                        when (bedwarsMode) {
                            "Solo" -> player.sendChatMessage("/play bedwars_eight_one")
                            "Double" -> player.sendChatMessage("/play bedwars_eight_two")
                            "Trio" -> player.sendChatMessage("/play bedwars_four_three")
                            "Quad" -> player.sendChatMessage("/play bedwars_four_four")
                        }
                    }
                    delayTick = 0
                }
            }
        }
    }

    /**
     * Check whether player is in game or not
     */
    private fun playerInGame(): Boolean {
        val player = mc.thePlayer ?: return false

        return player.ticksExisted >= 20
                && (player.capabilities.isFlying
                || player.capabilities.allowFlying
                || player.capabilities.disableDamage)
    }

    /**
     * HUD Tag
     */
    override val tag
        get() = mode
}