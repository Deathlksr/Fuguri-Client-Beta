/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.JumpEvent
import net.deathlksr.fuguribeta.event.MoveEvent
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.aac.AACv1
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.aac.AACv2
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.aac.AACv3
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.ncp.NCP
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.other.Hycraft
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.other.Redesky
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.other.Buzz
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.other.VerusDamage
import net.deathlksr.fuguribeta.features.module.modules.movement.longjumpmodes.other.VerusDamage.damaged
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.extensions.tryJump
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.ListValue

object LongJump : Module("LongJump", Category.MOVEMENT) {

    private val longJumpModes = arrayOf(
        // NCP
        NCP,

        // AAC
        AACv1, AACv2, AACv3,

        // Other
        Redesky, Hycraft, Buzz, VerusDamage
    )

    private val modes = longJumpModes.map { it.modeName }.toTypedArray()

    val mode by ListValue("Mode", modes, "NCP")
        val ncpBoost by FloatValue("NCPBoost", 4.25f, 1f..10f) { mode == "NCP" }

    private val autoJump by BoolValue("AutoJump", true)

    val autoDisable by BoolValue("AutoDisable", true) { mode == "VerusDamage" }

    var jumped = false
    var canBoost = false
    var teleported = false

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (jumped) {
            val mode = mode

            if (mc.thePlayer.onGround || mc.thePlayer.capabilities.isFlying) {
                jumped = false

                if (mode == "NCP") {
                    mc.thePlayer.motionX = 0.0
                    mc.thePlayer.motionZ = 0.0
                }
                return
            }

            modeModule.onUpdate()
        }
        if (autoJump && mc.thePlayer.onGround && isMoving) {
            if (autoDisable && !damaged) {
                return
            }

            jumped = true
            mc.thePlayer.tryJump()
        }
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        modeModule.onMove(event)
    }

    @EventTarget
    override fun onEnable() {
        modeModule.onEnable()
    }

    @EventTarget
    override fun onDisable() {
        modeModule.onDisable()
    }

    @EventTarget(ignoreCondition = true)
    fun onJump(event: JumpEvent) {
        jumped = true
        canBoost = true
        teleported = false

        if (handleEvents()) {
            modeModule.onJump(event)
        }
    }

    override val tag
        get() = mode

    private val modeModule
        get() = longJumpModes.find { it.modeName == mode }!!
}
