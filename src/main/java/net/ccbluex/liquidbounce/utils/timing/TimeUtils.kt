/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.timing

import net.ccbluex.liquidbounce.utils.extensions.safeDiv
import net.ccbluex.liquidbounce.utils.misc.RandomUtils.nextInt
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

    fun hasReached(time: Double): Boolean {
        return (System.currentTimeMillis() - last) > time
    }

    fun reset() {
        last = System.currentTimeMillis()
    }
}