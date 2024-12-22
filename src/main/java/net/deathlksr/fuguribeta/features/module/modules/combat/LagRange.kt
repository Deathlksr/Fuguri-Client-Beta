package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.GameLoopEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Text.Companion.DECIMAL_FORMAT
import net.deathlksr.fuguribeta.utils.EntityUtils
import net.deathlksr.fuguribeta.utils.MovementUtils
import net.deathlksr.fuguribeta.utils.extensions.getDistanceToEntityBox
import net.deathlksr.fuguribeta.utils.misc.RandomUtils
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue

object LagRange : Module("LagRange", Category.COMBAT, hideModule = false, forcedDescription = "ПРЕДИКТЫ ХУЕТА НЕ ЮЗАЙ ПОКА ЧТО ОКЕЙ ДА!!!!!!!!!!!!!!") {
    private val lagTime = IntegerValue("LagTime", 50, 0..500)
    private val min = FloatValue("MinRange", 3f, 0f..3f)

    private val max = FloatValue("MaxRange", 6f, 3f..6f)
    private val delay = IntegerValue("Delay", 150, 50..2000)
    private val fov = IntegerValue("Fov", 90, 0..180)
    private val onGround = BoolValue("OnGround", false)
    private val onlyKillAura = BoolValue("OnlyKillAura", true)

    private var lastLagTime: Long = 0
    private var reach: Float = 0F

    @EventTarget
    private fun onGameLoop(event: GameLoopEvent) {
        if (!shouldStart()) return

        Thread.sleep(lagTime.get().toLong())
        lastLagTime = System.currentTimeMillis()
    }

    private fun shouldStart(): Boolean {
        if (mc.thePlayer == null || mc.theWorld == null) return false
        if (onGround.get() && !mc.thePlayer.onGround) return false
        if (!MovementUtils.isMoving) return false
        if (onlyKillAura.get()) if (KillAura.target == null) return false
        if (fov.get() == 0) return false
        if (System.currentTimeMillis() - lastLagTime < delay.get()) return false

        for (entity in mc.theWorld.loadedEntityList) {
            reach = RandomUtils.nextFloat(min.get(), max.get())
            if (EntityUtils.isSelected(
                    entity,
                    true
                ) && mc.thePlayer.getDistanceToEntityBox(entity) <= reach && entity != mc.thePlayer && EntityUtils.isLookingOnEntities(
                    entity,
                    fov.get().toDouble()
                )
            ) {
                return true
            }
        }
        return false
    }

    override val tag: String
        get() = DECIMAL_FORMAT.format(reach)

}