/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.client

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.MotionEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.utils.RotationUtils.currentRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.serverRotation
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue

object RotationHandler : Module("RotationHandler", Category.CLIENT, gameDetecting = false, hideModule = false) {

    private val realistic by BoolValue("Realistic", false)
    private val body by BoolValue("Body", true) { !realistic }
    val debugRotations by BoolValue("DebugRotations", false)

    val ghost by BoolValue("Ghost", true)

    val colorRedValue by IntegerValue("R", 0, 0..255) { ghost }
    val colorGreenValue by IntegerValue("G", 160, 0..255) { ghost }
    val colorBlueValue by IntegerValue("B", 255, 0..255) { ghost }
    val alphaValue by IntegerValue("Alpha", 255, 0..255) { ghost }
    val rainbow by BoolValue("RainBow", false) { ghost }

    var prevHeadPitch = 0f
    var headPitch = 0f

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        prevHeadPitch = headPitch
        headPitch = serverRotation.pitch

        if (!shouldRotate() || realistic) {
            return
        }

        thePlayer.rotationYawHead = serverRotation.yaw

        if (body) {
            thePlayer.renderYawOffset = thePlayer.rotationYawHead
        }
    }

    fun lerp(tickDelta: Float, old: Float, new: Float): Float {
        return old + (new - old) * tickDelta
    }

    /**
     * Rotate when current rotation is not null or special modules which do not make use of RotationUtils like Derp are enabled.
     */
    fun shouldRotate() = state || currentRotation != null

    /**
     * Imitate the game's head and body rotation logic
     */
    fun shouldUseRealisticMode() = realistic && shouldRotate()

    /**
     * Which rotation should the module use?
     */
    fun getRotation(useServerRotation: Boolean) = if (useServerRotation) serverRotation else currentRotation
}
