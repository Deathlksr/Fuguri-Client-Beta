/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.utils.kotlin

inline fun <T> MutableCollection<T>.removeEach(max: Int = this.size, predicate: (T) -> Boolean) {
    var i = 0
    val iterator = iterator()
    while (iterator.hasNext()) {
        if (i > max) {
            break
        }
        val next = iterator.next()
        if (predicate(next)) {
            iterator.remove()
            i++
        }
    }
}