package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.utils.extensions.safeDiv
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
import java.util.concurrent.atomic.AtomicInteger
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import kotlin.math.roundToInt

object TimeUtils {
    private var last = System.currentTimeMillis()

    fun randomDelay(minDelay: Int, maxDelay: Int) = nextInt(minDelay, maxDelay + 1)

    fun randomClickDelay(minCPS: Int, maxCPS: Int): Int {
        val minDelay = 1000 safeDiv minCPS
        val maxDelay = 1000 safeDiv maxCPS
        return (Math.random() * (minDelay - maxDelay) + maxDelay).roundToInt()
    }

    fun hasReached(time: Double, reset: Boolean): Boolean {
        if ((System.currentTimeMillis() - time) > time) {
            if (reset) {
                last = System.currentTimeMillis()
            }

            return true
        } else {
            return false
        }
    }

    private val tickCount = AtomicInteger(0)
    private val scheduledActions = mutableListOf<Pair<Int, () -> Unit>>()

    init {
        MinecraftForge.EVENT_BUS.register(this)
    }

    fun delay(ticks: Int, action: () -> Unit) {
        val targetTick = tickCount.get() + ticks
        scheduledActions.add(Pair(targetTick, action))
    }

    @SubscribeEvent
    fun onTick(event: net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent) {
        if (event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) {
            val currentTick = tickCount.incrementAndGet()

            scheduledActions.removeAll {
                val (targetTick, action) = it
                if (targetTick <= currentTick) {
                    action()
                    true
                } else {
                    false
                }
            }
        }
    }

    fun hasReached(time: Double): Boolean {
        return (System.currentTimeMillis() - last) > time
    }

    fun reset() {
        last = System.currentTimeMillis()
    }
}