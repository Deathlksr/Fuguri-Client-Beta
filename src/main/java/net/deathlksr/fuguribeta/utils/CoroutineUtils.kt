/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.utils

@Suppress("ControlFlowWithEmptyBody")
object CoroutineUtils {
	fun waitUntil(condition: () -> Boolean) {
		while (!condition()) {}
	}
}