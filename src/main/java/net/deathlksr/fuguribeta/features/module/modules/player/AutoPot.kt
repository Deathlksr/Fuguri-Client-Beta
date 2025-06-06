/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player

import net.deathlksr.fuguribeta.event.EventState.POST
import net.deathlksr.fuguribeta.event.EventState.PRE
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.MotionEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPackets
import net.deathlksr.fuguribeta.utils.Rotation
import net.deathlksr.fuguribeta.utils.RotationUtils.serverRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.setTargetRotation
import net.deathlksr.fuguribeta.utils.extensions.tryJump
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils.serverOpenInventory
import net.deathlksr.fuguribeta.utils.inventory.isSplashPotion
import net.deathlksr.fuguribeta.utils.misc.FallingPlayer
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.nextFloat
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.item.ItemPotion
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.potion.Potion

object AutoPot : Module("AutoPot", Category.PLAYER, hideModule = false) {

    private val health by FloatValue("Health", 15F, 1F..20F) { healPotion || regenerationPotion }
    private val delay by IntegerValue("Delay", 500, 500..1000)

    // Useful potion options
    private val healPotion by BoolValue("HealPotion", true)
    private val regenerationPotion by BoolValue("RegenPotion", true)
    private val fireResistancePotion by BoolValue("FireResPotion", true)
    private val strengthPotion by BoolValue("StrengthPotion", true)
    private val jumpPotion by BoolValue("JumpPotion", true)
    private val speedPotion by BoolValue("SpeedPotion", true)

    private val openInventory by BoolValue("OpenInv", false)
    private val simulateInventory by BoolValue("SimulateInventory", true) { !openInventory }

    private val groundDistance by FloatValue("GroundDistance", 2F, 0F..5F)
    private val mode by ListValue("Mode", arrayOf("Normal", "Jump", "Port"), "Normal")

    private val msTimer = MSTimer()
    private var potion = -1

    @EventTarget
    fun onMotion(motionEvent: MotionEvent) {
        if (!msTimer.hasTimePassed(delay) || mc.playerController.isInCreativeMode)
            return

        val thePlayer = mc.thePlayer ?: return

        when (motionEvent.eventState) {
            PRE -> {
                // Hotbar Potion
                val potionInHotbar = findPotion(36, 45)

                if (potionInHotbar != null) {
                    if (thePlayer.onGround) {
                        when (mode.lowercase()) {
                            "jump" -> thePlayer.tryJump()
                            "port" -> thePlayer.moveEntity(0.0, 0.42, 0.0)
                        }
                    }

                    // Prevent throwing potions into the void
                    val fallingPlayer = FallingPlayer(thePlayer)

                    val collisionBlock = fallingPlayer.findCollision(20)?.pos

                    if (thePlayer.posY - (collisionBlock?.y ?: return) - 1 > groundDistance)
                        return

                    potion = potionInHotbar
                    sendPacket(C09PacketHeldItemChange(potion - 36))

                    if (thePlayer.rotationPitch <= 80F) {
                        setTargetRotation(Rotation(thePlayer.rotationYaw, nextFloat(80F, 90F)).fixedSensitivity(),
                            immediate = true
                        )
                    }
                    return
                }

                // Inventory Potion -> Hotbar Potion
                val potionInInventory = findPotion(9, 36) ?: return
                if (InventoryUtils.hasSpaceInHotbar()) {
                    if (openInventory && mc.currentScreen !is GuiInventory)
                        return

                    if (simulateInventory)
                        serverOpenInventory = true

                    mc.playerController.windowClick(0, potionInInventory, 0, 1, thePlayer)

                    if (simulateInventory && mc.currentScreen !is GuiInventory)
                        serverOpenInventory = false

                    msTimer.reset()
                }
            }

            POST -> {
                if (potion >= 0 && serverRotation.pitch >= 75F) {
                    val itemStack = thePlayer.inventoryContainer.getSlot(potion).stack

                    if (itemStack != null) {
                        sendPackets(
                            C08PacketPlayerBlockPlacement(itemStack),
                            C09PacketHeldItemChange(thePlayer.inventory.currentItem)
                        )

                        msTimer.reset()
                    }

                    potion = -1
                }
            }

            else -> {}
        }
    }

    private fun findPotion(startSlot: Int, endSlot: Int): Int? {
        val thePlayer = mc.thePlayer

        for (i in startSlot until endSlot) {
            val stack = thePlayer.inventoryContainer.getSlot(i).stack

            if (stack == null || stack.item !is ItemPotion || !stack.isSplashPotion())
                continue

            val itemPotion = stack.item as ItemPotion

            for (potionEffect in itemPotion.getEffects(stack))
                if (thePlayer.health <= health && healPotion && potionEffect.potionID == Potion.heal.id)
                    return i

            if (!thePlayer.isPotionActive(Potion.regeneration))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (thePlayer.health <= health && regenerationPotion && potionEffect.potionID == Potion.regeneration.id)
                        return i

            if (!thePlayer.isPotionActive(Potion.fireResistance))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (fireResistancePotion && potionEffect.potionID == Potion.fireResistance.id)
                        return i

            if (!thePlayer.isPotionActive(Potion.moveSpeed))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (speedPotion && potionEffect.potionID == Potion.moveSpeed.id)
                        return i

            if (!thePlayer.isPotionActive(Potion.jump))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (jumpPotion && potionEffect.potionID == Potion.jump.id)
                        return i

            if (!thePlayer.isPotionActive(Potion.damageBoost))
                for (potionEffect in itemPotion.getEffects(stack))
                    if (strengthPotion && potionEffect.potionID == Potion.damageBoost.id)
                        return i
        }

        return null
    }

    override val tag
        get() = health.toString()

}