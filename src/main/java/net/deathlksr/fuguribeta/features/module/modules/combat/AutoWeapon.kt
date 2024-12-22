/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.event.AttackEvent
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils.serverSlot
import net.deathlksr.fuguribeta.utils.inventory.attackDamage
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.item.ItemSword
import net.minecraft.item.ItemTool
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C02PacketUseEntity.Action.ATTACK

object AutoWeapon : Module("AutoWeapon", Category.COMBAT, subjective = true, hideModule = false) {

    private val onlySword by BoolValue("OnlySword", false)

    private val spoof by BoolValue("SpoofItem", false)
        private val spoofTicks by IntegerValue("SpoofTicks", 10, 1..20) { spoof }

    private var attackEnemy = false

    private var ticks = 0

    @EventTarget
    fun onAttack(event: AttackEvent) {
        attackEnemy = true
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (event.packet is C02PacketUseEntity && event.packet.action == ATTACK && attackEnemy) {
            attackEnemy = false

            // Find the best weapon in hotbar (#Kotlin Style)
            val (slot, _) = (0..8)
                .map { it to mc.thePlayer.inventory.getStackInSlot(it) }
                .filter { it.second != null && ((onlySword && it.second.item is ItemSword)
                        || (!onlySword && (it.second.item is ItemSword || it.second.item is ItemTool))) }
                .maxByOrNull { it.second.attackDamage } ?: return

            if (slot == mc.thePlayer.inventory.currentItem) // If in hand no need to swap
                return

            // Switch to best weapon
            if (spoof) {
                serverSlot = slot
                ticks = spoofTicks
            } else {
                mc.thePlayer.inventory.currentItem = slot
                mc.playerController.updateController()
            }

            // Resend attack packet
            sendPacket(event.packet)
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onUpdate(update: UpdateEvent) {
        // Switch back to old item after some time
        if (ticks > 0) {
            if (ticks == 1)
                serverSlot = mc.thePlayer.inventory.currentItem

            ticks--
        }
    }
}