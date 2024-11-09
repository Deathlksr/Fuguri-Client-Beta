package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.value.FloatValue

object KeepSprint : Module("KeepSprint", Category.COMBAT, hideModule = false) {
    private val motionAfterAttackOnGround by FloatValue("MotionAfterAttackOnGround", 0.6f, 0.0f..1f)
    private val motionAfterAttackInAir by FloatValue("MotionAfterAttackInAir", 1.0f, 0.0f..1f)

    val motionAfterAttack
        get() = if (mc.gameSettings.keyBindJump.isKeyDown) motionAfterAttackInAir else motionAfterAttackOnGround
}