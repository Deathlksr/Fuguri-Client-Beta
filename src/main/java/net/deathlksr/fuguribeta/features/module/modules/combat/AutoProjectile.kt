/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.EntityUtils.isSelected
import net.deathlksr.fuguribeta.utils.RaycastUtils.raycastEntity
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils
import net.deathlksr.fuguribeta.utils.misc.RandomUtils
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.init.Items.egg
import net.minecraft.init.Items.snowball

object AutoProjectile : Module("AutoProjectile", Category.COMBAT, hideModule = false) {
    private val facingEnemy by BoolValue("FacingEnemy", true)

    private val mode by ListValue("Mode", arrayOf("Normal", "Smart"), "Normal")
    private val range by FloatValue("Range", 8F, 1F..20F)
    private val throwDelay by IntegerValue("ThrowDelay", 1000, 50..2000) { mode != "Smart" }

    private val minThrowDelay: IntegerValue = object : IntegerValue("MinThrowDelay", 1000, 50..2000) {
        override fun isSupported() = mode == "Smart"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxThrowDelay.get())
    }

    private val maxThrowDelay: IntegerValue = object : IntegerValue("MaxThrowDelay", 1500, 50..2000) {
        override fun isSupported() = mode == "Smart"
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minThrowDelay.get())
    }

    private val switchBackDelay by IntegerValue("SwitchBackDelay", 500, 50..2000)

    private val throwTimer = MSTimer()
    private val projectilePullTimer = MSTimer()

    private var projectileInUse = false
    private var switchBack = -1

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val player = mc.thePlayer ?: return
        val usingProjectile = (player.isUsingItem && (player.heldItem?.item == snowball || player.heldItem?.item == egg)) || projectileInUse

        if (usingProjectile) {
            if (projectilePullTimer.hasTimePassed(switchBackDelay)) {
                if (switchBack != -1 && player.inventory.currentItem != switchBack) {
                    player.inventory.currentItem = switchBack

                    mc.playerController.updateController()
                } else {
                    player.stopUsingItem()
                }

                switchBack = -1
                projectileInUse = false

                throwTimer.reset()
            }
        } else {
            var throwProjectile = false

            if (facingEnemy) {
                var facingEntity = mc.objectMouseOver?.entityHit

                if (facingEntity == null) {
                    facingEntity = raycastEntity(range.toDouble()) { isSelected(it, true) }
                }

                if (isSelected(facingEntity, true)) {
                    throwProjectile = true
                }
            } else {
                throwProjectile = true
            }

            if (throwProjectile) {
                if (mode == "Normal" && throwTimer.hasTimePassed(throwDelay)) {
                    if (player.heldItem?.item != snowball && player.heldItem?.item != egg) {
                        val projectile = InventoryUtils.findItemArray(36, 44, arrayOf(snowball, egg)) ?: return

                        switchBack = player.inventory.currentItem

                        player.inventory.currentItem = projectile - 36
                        mc.playerController.updateController()
                    }

                    throwProjectile()
                }

                val randomThrowDelay = RandomUtils.nextInt(minThrowDelay.get(), maxThrowDelay.get())
                if (mode == "Smart" && throwTimer.hasTimePassed(randomThrowDelay)) {
                    if (player.heldItem?.item != snowball && player.heldItem?.item != egg) {
                        val projectile = InventoryUtils.findItemArray(36, 44, arrayOf(snowball, egg)) ?: return

                        switchBack = player.inventory.currentItem

                        player.inventory.currentItem = projectile - 36
                        mc.playerController.updateController()
                    }

                    throwProjectile()
                }
            }
        }
    }

    /**
     * Throw projectile (snowball/egg)
     */
    private fun throwProjectile() {
        val player = mc.thePlayer ?: return
        val projectile = InventoryUtils.findItemArray(36, 44, arrayOf(snowball, egg)) ?: return

        player.inventory.currentItem = projectile - 36

        mc.playerController.sendUseItem(player, mc.theWorld, player.inventoryContainer.getSlot(projectile).stack)

        projectileInUse = true
        projectilePullTimer.reset()
    }

    /**
     * Reset everything when disabled
     */
    override fun onDisable() {
        throwTimer.reset()
        projectilePullTimer.reset()
        projectileInUse = false
        switchBack = -1
    }

    /**
     * HUD Tag
     */
    override val tag
        get() = mode
}