/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.modules.combat.KillAura
import net.deathlksr.fuguribeta.utils.BlinkUtils
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils.serverSlot
import net.deathlksr.fuguribeta.utils.timing.TickTimer
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.item.*
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.play.server.S27PacketExplosion
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

object NoSlow : Module("NoSlow", Category.MOVEMENT, gameDetecting = false, hideModule = false) {

    private val swordMode by ListValue("SwordMode", arrayOf("None", "NCP", "UpdatedNCP", "AAC5", "SwitchItem", "InvalidC08", "Blink"), "None")

    private val reblinkTicks by IntegerValue("ReblinkTicks", 10,1..20) { swordMode == "Blink" }

    private val blockForwardMultiplier by FloatValue("BlockForwardMultiplier", 1f, 0.2F..1f)
    private val blockStrafeMultiplier by FloatValue("BlockStrafeMultiplier", 1f, 0.2F..1f)

    private val consumePacket by ListValue("ConsumeMode", arrayOf("None", "UpdatedNCP", "AAC5", "SwitchItem", "InvalidC08", "Intave"), "None")

    private val consumeForwardMultiplier by FloatValue("ConsumeForwardMultiplier", 1f, 0.2F..1f)
    private val consumeStrafeMultiplier by FloatValue("ConsumeStrafeMultiplier", 1f, 0.2F..1f)
    private val consumeFoodOnly by BoolValue("ConsumeFoodOnly", true) { consumeForwardMultiplier > 0.2F || consumeStrafeMultiplier > 0.2F }
    private val consumeDrinkOnly by BoolValue("ConsumeDrinkOnly", true) { consumeForwardMultiplier > 0.2F || consumeStrafeMultiplier > 0.2F }

    private val bowPacket by ListValue("BowMode", arrayOf("None", "UpdatedNCP", "AAC5", "SwitchItem", "InvalidC08"), "None")

    private val bowForwardMultiplier by FloatValue("BowForwardMultiplier", 1f, 0.2F..1f)
    private val bowStrafeMultiplier by FloatValue("BowStrafeMultiplier", 1f, 0.2F..1f)

    // Blocks
    val soulsand by BoolValue("Soulsand", true)
    val liquidPush by BoolValue("LiquidPush", true)

    private var shouldSwap = false

    private var shouldBlink = true

    private val BlinkTimer = TickTimer()

    override fun onDisable() {
        shouldSwap = false
        shouldBlink = true
        BlinkTimer.reset()
        BlinkUtils.unblink()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val player = mc.thePlayer ?: return
        val heldItem = player.heldItem ?: return
        val currentItem = player.inventory.currentItem
        val isUsingItem = usingItemFunc()

        if (mc.thePlayer.motionX == 0.0 && mc.thePlayer.motionZ == 0.0 && !shouldSwap)
            return

        if (!consumeFoodOnly && heldItem.item is ItemFood || !consumeDrinkOnly && (heldItem.item is ItemPotion || heldItem.item is ItemBucketMilk))
            return

        if ((heldItem.item is ItemFood || heldItem.item is ItemPotion || heldItem.item is ItemBucketMilk) && (isUsingItem || shouldSwap)) {
            when (consumePacket.lowercase()) {
                "aac5" ->
                    sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f))

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        serverSlot = (serverSlot + 1) % 9
                        serverSlot = currentItem
                    }

                "updatedncp" ->
                    if (event.eventState == EventState.PRE && shouldSwap) {
                        serverSlot = (serverSlot + 1) % 9
                        serverSlot = currentItem
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, heldItem, 0f, 0f, 0f))
                        shouldSwap = false
                    }

                "invalidc08" -> {
                    if (event.eventState == EventState.PRE) {
                        if (InventoryUtils.hasSpaceInInventory()) {
                            if (player.ticksExisted % 3 == 0)
                                sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 1, null, 0f, 0f, 0f))
                        }
                    }
                }

                "intave" -> {
                    if (event.eventState == EventState.PRE) {
                        sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.UP))
                    }
                }

                else -> return
            }
        }

        if (heldItem.item is ItemBow && (isUsingItem || shouldSwap)) {
            when (bowPacket.lowercase()) {
                "aac5" ->
                    sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f))

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        serverSlot = (serverSlot + 1) % 9
                        serverSlot = currentItem
                    }

                "updatedncp" ->
                    if (event.eventState == EventState.PRE && shouldSwap) {
                        serverSlot = (serverSlot + 1) % 9
                        serverSlot = currentItem
                        sendPacket(C08PacketPlayerBlockPlacement(BlockPos.ORIGIN, 255, heldItem, 0f, 0f, 0f))
                        shouldSwap = false
                    }

                "invalidc08" -> {
                    if (event.eventState == EventState.PRE) {
                        if (InventoryUtils.hasSpaceInInventory()) {
                            if (player.ticksExisted % 3 == 0)
                                sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 1, null, 0f, 0f, 0f))
                        }
                    }
                }

                else -> return
            }
        }

        if (heldItem.item is ItemSword && isUsingItem) {
            when (swordMode.lowercase()) {
                "none" -> return

                "ncp" ->
                    when (event.eventState) {
                        EventState.PRE -> sendPacket(
                            C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN)
                        )

                        EventState.POST -> sendPacket(
                            C08PacketPlayerBlockPlacement(
                                BlockPos(-1, -1, -1), 255, heldItem, 0f, 0f, 0f
                            )
                        )

                        else -> return
                    }

                "updatedncp" ->
                    if (event.eventState == EventState.POST) {
                        sendPacket(
                            C08PacketPlayerBlockPlacement(
                                BlockPos.ORIGIN, 255, heldItem, 0f, 0f, 0f
                            )
                        )
                    }

                "aac5" ->
                    if (event.eventState == EventState.POST) {
                        sendPacket(
                            C08PacketPlayerBlockPlacement(
                                BlockPos(-1, -1, -1), 255, player.heldItem, 0f, 0f, 0f
                            )
                        )
                    }

                "switchitem" ->
                    if (event.eventState == EventState.PRE) {
                        serverSlot = (serverSlot + 1) % 9
                        serverSlot = currentItem
                    }

                "invalidc08" -> {
                    if (event.eventState == EventState.PRE) {
                        if (InventoryUtils.hasSpaceInInventory()) {
                            if (player.ticksExisted % 3 == 0)
                                sendPacket(C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 1, null, 0f, 0f, 0f))
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val player = mc.thePlayer ?: return

        if (event.isCancelled || shouldSwap)
            return

        if (swordMode == "Blink") {
            when (packet) {
                is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> return

                is C07PacketPlayerDigging, is C02PacketUseEntity, is C12PacketUpdateSign, is C19PacketResourcePackStatus -> {
                    BlinkTimer.update()
                    if (shouldBlink && BlinkTimer.hasTimePassed(reblinkTicks) && (BlinkUtils.packetsReceived.isNotEmpty() || BlinkUtils.packets.isNotEmpty())) {
                        BlinkUtils.unblink()
                        BlinkTimer.reset()
                        shouldBlink = false
                    } else if (!BlinkTimer.hasTimePassed(reblinkTicks)) {
                        shouldBlink = true
                    }
                    return
                }

                // Flush on kb
                is S12PacketEntityVelocity -> {
                    if (mc.thePlayer.entityId == packet.entityID) {
                        BlinkUtils.unblink()
                        return
                    }
                }

                // Flush on explosion
                is S27PacketExplosion -> {
                    if (packet.field_149153_g != 0f || packet.field_149152_f != 0f || packet.field_149159_h != 0f) {
                        BlinkUtils.unblink()
                        return
                    }
                }

                is C03PacketPlayer -> {
                    if (swordMode == "Blink") {
                        if (isMoving) {
                            if (player.heldItem?.item is ItemSword && usingItemFunc()) {
                                if (shouldBlink)
                                    BlinkUtils.blink(packet, event)
                            } else {
                                shouldBlink = true
                                BlinkUtils.unblink()
                            }
                        }
                    }
                }
            }
        }

        when (packet) {
            is C08PacketPlayerBlockPlacement -> {
                if (packet.stack?.item != null && player.heldItem?.item != null && packet.stack.item == mc.thePlayer.heldItem?.item) {
                    if ((consumePacket == "UpdatedNCP" && (packet.stack.item is ItemFood || packet.stack.item is ItemPotion || packet.stack.item is ItemBucketMilk)) || (bowPacket == "UpdatedNCP" && packet.stack.item is ItemBow)) {
                        shouldSwap = true;
                    }
                }
            }
        }
    }
    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer.heldItem?.item

        if (!consumeFoodOnly && heldItem is ItemFood || !consumeDrinkOnly && (heldItem is ItemPotion || heldItem is ItemBucketMilk))
            return

        event.forward = getMultiplier(heldItem, true)
        event.strafe = getMultiplier(heldItem, false)
    }

    private fun getMultiplier(item: Item?, isForward: Boolean) = when (item) {
        is ItemFood, is ItemPotion, is ItemBucketMilk -> if (isForward) consumeForwardMultiplier else consumeStrafeMultiplier

        is ItemSword -> if (isForward) blockForwardMultiplier else blockStrafeMultiplier

        is ItemBow -> if (isForward) bowForwardMultiplier else bowStrafeMultiplier

        else -> 0.2F
    }

    fun isUNCPBlocking() = swordMode == "UpdatedNCP" && mc.gameSettings.keyBindUseItem.isKeyDown && (mc.thePlayer.heldItem?.item is ItemSword)
    fun usingItemFunc() = mc.thePlayer?.heldItem != null && (mc.thePlayer.isUsingItem || (mc.thePlayer.heldItem?.item is ItemSword && KillAura.blockStatus) || isUNCPBlocking())
}