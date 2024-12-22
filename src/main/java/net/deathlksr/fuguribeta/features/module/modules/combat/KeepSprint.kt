package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.value.FloatValue

object KeepSprint : Module("KeepSprint", Category.COMBAT, hideModule = false) {
    private val motionAfterAttackOnGround by FloatValue("MotionAfterAttackOnGround", 0.6f, 0.0f..1f)
    private val motionAfterAttackInAir by FloatValue("MotionAfterAttackInAir", 1.0f, 0.0f..1f)

    val motionAfterAttack
        get() = if (!mc.thePlayer.onGround) motionAfterAttackInAir else motionAfterAttackOnGround
}