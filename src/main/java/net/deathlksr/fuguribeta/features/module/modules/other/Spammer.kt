/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import net.deathlksr.fuguribeta.FuguriBeta.CLIENT_NAME
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.nextFloat
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.nextInt
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.randomString
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.utils.timing.TimeUtils.randomDelay
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.TextValue

object Spammer : Module("Spammer", Category.OTHER, subjective = true, hideModule = false) {
    private val maxDelayValue: IntegerValue = object : IntegerValue("MaxDelay", 1000, 0..5000) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minDelay)

        override fun onChanged(oldValue: Int, newValue: Int) {
            delay = randomDelay(minDelay, get())
        }
    }
    private val maxDelay by maxDelayValue

    private val minDelay: Int by object : IntegerValue("MinDelay", 500, 0..5000) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxDelay)

        override fun onChanged(oldValue: Int, newValue: Int) {
            delay = randomDelay(get(), maxDelay)
        }

        override fun isSupported() = !maxDelayValue.isMinimal()
    }

    private val message by
        TextValue("Message", "$CLIENT_NAME Client")

    private val custom by BoolValue("Custom", false)

    private val msTimer = MSTimer()
    private var delay = randomDelay(minDelay, maxDelay)

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (msTimer.hasTimePassed(delay)) {
            mc.thePlayer.sendChatMessage(
                if (custom) replace(message)
                else message + " >" + randomString(nextInt(5, 11)) + "<"
            )
            msTimer.reset()
            delay = randomDelay(minDelay, maxDelay)
        }
    }

    private fun replace(text: String): String {
        var replacedStr = text

        replaceMap.forEach { (key, valueFunc) ->
            while (key in replacedStr) {
                // You have to replace them one by one, otherwise all parameters like %s would be set to the same random string.
                replacedStr = replacedStr.replaceFirst(key, valueFunc())
            }
        }

        return replacedStr
    }

    private fun randomPlayer() =
        mc.netHandler.playerInfoMap
            .map { playerInfo -> playerInfo.gameProfile.name }
            .filter { name -> name != mc.thePlayer.name }
            .randomOrNull() ?: "none"

    private val replaceMap = mapOf(
        "%f" to { nextFloat().toString() },
        "%i" to { nextInt(0, 10000).toString() },
        "%ss" to { randomString(nextInt(1, 6)) },
        "%s" to { randomString(nextInt(1, 10)) },
        "%ls" to { randomString(nextInt(1, 17)) },
        "%p" to { randomPlayer() }
    )
}