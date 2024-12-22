/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.player

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.ui.client.hud.HUD.addNotification
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Notification
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Type
import net.deathlksr.fuguribeta.value.IntegerValue

object HealthWarn: Module("HealthWarn", Category.VISUAL, hideModule = false) {

    private val healthValue by IntegerValue("Health", 7, 1.. 20)

    private var canWarn = true

    override fun onEnable() {
        canWarn = true
    }

    override fun onDisable() {
        canWarn = true
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (mc.thePlayer.health <= healthValue) {
            if (canWarn) {
                addNotification(Notification("HP Warning","YOU ARE AT LOW HP!", Type.ERROR, 3000))

                canWarn = false
            }
        } else {
            canWarn = true
        }
    }
}
