/*
 * LiquidBounce Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/CCBlueX/LiquidBounce/
 */
package net.deathlksr.fuguribeta.features.module.modules.movement

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.aac.AACHop3313
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.aac.AACHop350
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.aac.AACHop4
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.aac.AACHop5
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.hypixel.HypixelHop
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.matrix.MatrixHop
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.matrix.MatrixSlowHop
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.matrix.OldMatrixHop
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.ncp.*
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.other.*
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.spartan.SpartanYPort
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.spectre.SpectreBHop
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.spectre.SpectreLowHop
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.spectre.SpectreOnGround
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.verus.VerusHop
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.verus.VerusLowHop
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.vulcan.VulcanGround288
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.vulcan.VulcanHop
import net.deathlksr.fuguribeta.features.module.modules.movement.speedmodes.vulcan.VulcanLowHop
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue

object Speed : Module("Speed", Category.MOVEMENT, hideModule = false) {

    private val speedModes = arrayOf(

        // NCP
        NCPBHop,
        NCPFHop,
        SNCPBHop,
        NCPHop,
        NCPYPort,
        UNCPHop,

        // AAC
        AACHop3313,
        AACHop350,
        AACHop4,
        AACHop5,

        // Spartan
        SpartanYPort,

        // Spectre
        SpectreLowHop,
        SpectreBHop,
        SpectreOnGround,

        // Verus
        VerusHop,
        VerusLowHop,

        // Vulcan
        VulcanHop,
        VulcanLowHop,
        VulcanGround288,

        // Matrix
        OldMatrixHop,
        MatrixHop,
        MatrixSlowHop,

        // Server specific
        TeleportCubeCraft,
        HypixelHop,

        // Other
        Boost,
        Frame,
        MiJump,
        OnGround,
        SlowHop,
        Legit,
        CustomSpeed,
    )

    private val modes = speedModes.map { it.modeName }.toTypedArray()

    val mode by object : ListValue("Mode", modes, "NCPBHop") {
        override fun onChange(oldValue: String, newValue: String): String {
            if (state)
                onDisable()

            return super.onChange(oldValue, newValue)
        }

        override fun onChanged(oldValue: String, newValue: String) {
            if (state)
                onEnable()
        }
    }

    // Custom Speed
    val customY by FloatValue("CustomY", 0.42f, 0f..4f) { mode == "Custom" }
    val customGroundStrafe by FloatValue("CustomGroundStrafe", 1.6f, 0f..2f) { mode == "Custom" }
    val customAirStrafe by FloatValue("CustomAirStrafe", 0f, 0f..2f) { mode == "Custom" }
    val customGroundTimer by FloatValue("CustomGroundTimer", 1f, 0.1f..2f) { mode == "Custom" }
    val customAirTimerTick by IntegerValue("CustomAirTimerTick", 5, 1..20) { mode == "Custom" }
    val customAirTimer by FloatValue("CustomAirTimer", 1f, 0.1f..2f) { mode == "Custom" }

    // Extra options
    val resetXZ by BoolValue("ResetXZ", false) { mode == "Custom" }
    val resetY by BoolValue("ResetY", false) { mode == "Custom" }
    val notOnConsuming by BoolValue("NotOnConsuming", false) { mode == "Custom" }
    val notOnFalling by BoolValue("NotOnFalling", false) { mode == "Custom" }
    val notOnVoid by BoolValue("NotOnVoid", true) { mode == "Custom" }

    // TeleportCubecraft Speed
    val cubecraftPortLength by FloatValue("CubeCraft-PortLength", 1f, 0.1f..2f) { mode == "TeleportCubeCraft" }

    // MineBlaze Speed
    val boost by BoolValue("Boost", true) { mode == "MineBlazeHop" }
    val strafeStrength by FloatValue("StrafeStrength", 0.29f, 0.1f..0.29f) { mode == "MineBlazeHop" }
    val groundTimer by FloatValue("GroundTimer", 0.5f, 0.1f..5f) { mode == "MineBlazeHop" }
    val airTimer by FloatValue("AirTimer", 1.09f, 0.1f..5f) { mode == "MineBlazeHop" }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isSneaking)
            return

        if (isMoving && !sprintManually)
            thePlayer.isSprinting = true

        modeModule.onUpdate()
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val thePlayer = mc.thePlayer ?: return

        if (thePlayer.isSneaking || event.eventState != EventState.PRE)
            return

        if (isMoving && !sprintManually)
            thePlayer.isSprinting = true

        modeModule.onMotion()
    }

    @EventTarget
    fun onMove(event: MoveEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onMove(event)
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onTick()
    }

    @EventTarget
    fun onStrafe(event: StrafeEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onStrafe()
    }

    @EventTarget
    fun onJump(event: JumpEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onJump(event)
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        if (mc.thePlayer?.isSneaking == true)
            return

        modeModule.onPacket(event)
    }

    override fun onEnable() {
        if (mc.thePlayer == null)
            return

        mc.timer.timerSpeed = 1f

        modeModule.onEnable()
    }

    override fun onDisable() {
        if (mc.thePlayer == null)
            return

        mc.timer.timerSpeed = 1f
        mc.thePlayer.speedInAir = 0.02f

        modeModule.onDisable()
    }

    private val modeModule
        get() = speedModes.find { it.modeName == mode }!!

    private val sprintManually
        // Maybe there are more but for now there's the Legit mode.
        get() = modeModule in arrayOf(Legit)
}
