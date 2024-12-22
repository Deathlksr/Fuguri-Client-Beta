/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.utils.timing

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.GameTickEvent
import net.deathlksr.fuguribeta.event.Listenable
import net.deathlksr.fuguribeta.utils.ClientUtils
import net.deathlksr.fuguribeta.utils.MinecraftInstance

object WaitTickUtils : MinecraftInstance(), Listenable {

    private val scheduledActions = mutableListOf<ScheduledAction>()

    fun scheduleTicks(ticks: Int, action: () -> Unit) {
        scheduledActions.add(ScheduledAction(ClientUtils.runTimeTicks + ticks, action))
    }

    @EventTarget(priority = -1)
    fun onTick(event: GameTickEvent) {
        val currentTick = ClientUtils.runTimeTicks
        val iterator = scheduledActions.iterator()

        while (iterator.hasNext()) {
            val scheduledAction = iterator.next()
            if (currentTick >= scheduledAction.ticks) {
                scheduledAction.action.invoke()
                iterator.remove()
            }
        }
    }

    private data class ScheduledAction(val ticks: Int, val action: () -> Unit)

    override fun handleEvents() = true
}