package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.event.EventManager.callEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.modules.other.Fucker
import net.deathlksr.fuguribeta.features.module.modules.other.Nuker
import net.deathlksr.fuguribeta.features.module.modules.player.Blink
import net.deathlksr.fuguribeta.features.module.modules.player.scaffolds.Scaffold
import net.deathlksr.fuguribeta.features.module.modules.player.scaffolds.Tower
import net.deathlksr.fuguribeta.features.module.modules.visual.FreeCam
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Text
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.*
import net.deathlksr.fuguribeta.utils.ClientUtils.runTimeTicks
import net.deathlksr.fuguribeta.utils.CooldownHelper.getAttackCooldownProgress
import net.deathlksr.fuguribeta.utils.CooldownHelper.resetLastAttackedTicks
import net.deathlksr.fuguribeta.utils.EntityUtils.isLookingOnEntities
import net.deathlksr.fuguribeta.utils.EntityUtils.isSelected
import net.deathlksr.fuguribeta.utils.MovementUtils.isMoving
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPackets
import net.deathlksr.fuguribeta.utils.RaycastUtils.raycastEntity
import net.deathlksr.fuguribeta.utils.RaycastUtils.runWithModifiedRaycastResult
import net.deathlksr.fuguribeta.utils.RotationUtils.currentRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.getRotationDifference
import net.deathlksr.fuguribeta.utils.RotationUtils.getVectorForRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.isRotationFaced
import net.deathlksr.fuguribeta.utils.RotationUtils.isVisible
import net.deathlksr.fuguribeta.utils.RotationUtils.searchCenter
import net.deathlksr.fuguribeta.utils.RotationUtils.setTargetRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.toRotation
import net.deathlksr.fuguribeta.utils.extensions.*
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils.serverOpenInventory
import net.deathlksr.fuguribeta.utils.inventory.ItemUtils.isConsumingItem
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.nextInt
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.utils.timing.TimeUtils.randomClickDelay
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityArmorStand
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemAxe
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.C02PacketUseEntity
import net.minecraft.network.play.client.C02PacketUseEntity.Action.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C07PacketPlayerDigging.Action.RELEASE_USE_ITEM
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.potion.Potion
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.world.WorldSettings
import org.lwjgl.input.Keyboard
import java.awt.Color
import kotlin.math.max

object KillAura : Module("KillAura", Category.COMBAT, Keyboard.KEY_R, hideModule = false) {

    /**
     * OPTIONS
     */

    private val simulateCooldown by BoolValue("SimulateCooldown", false)
    private val simulateDoubleClicking by BoolValue("SimulateDoubleClicking", false) { !simulateCooldown }

    private val clickValue by ListValue("ClickMode", arrayOf("Delay", "SmartDelay", "HurtTime"), "HurtTime")
    private val attackMode by ListValue("AttackMode", arrayOf("Legit", "Packet"), "Legit")

    // CPS - Attack speed
    private val maxCPSValue = object : IntegerValue("MaxCPS", 8, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minCPS)

        override fun onChanged(oldValue: Int, newValue: Int) {
            attackDelay = randomClickDelay(minCPS, newValue)
        }

        override fun isSupported() = !simulateCooldown
    }

    private val maxCPS by maxCPSValue

    private val minCPS: Int by object : IntegerValue("MinCPS", 5, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxCPS)

        override fun onChanged(oldValue: Int, newValue: Int) {
            attackDelay = randomClickDelay(newValue, maxCPS)
        }

        override fun isSupported() = !maxCPSValue.isMinimal() && !simulateCooldown
    }

    private val minHurtTime by IntegerValue("MinHurtTime", 2, 0..10) { !simulateCooldown && clickValue == "HurtTime" }
    private val maxHurtTime by IntegerValue("MaxHurtTime", 3, 0..10) { !simulateCooldown && clickValue == "HurtTime" }

    private val minAfterDelay by IntegerValue("MinAfterDelay", 9, 0..10) { !simulateCooldown && clickValue == "Delay" }
    private val maxAfterDelay by IntegerValue("MaxAfterDelay", 9, 0..10) { !simulateCooldown && clickValue == "Delay" }

    private val minHurtTimeToClick by IntegerValue("MinHurtTimePlayerToHit", 9, 0..9) { !simulateCooldown && clickValue == "SmartDelay" }

    private val SmartAttack by BoolValue("SmartClicking", false) { !simulateCooldown && clickValue == "HurtTime" }

    private val clickOnly by BoolValue("ClickOnly", false)

    private var afterDelay = 0

    // Range
    // TODO: Make block range independent from attack range
    private val range: Float by object : FloatValue("Range", 3f, 1f..3f) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            blockRange = blockRange.coerceAtMost(newValue)
        }
    }
    private val scanRange by FloatValue("MaxRange", 6f, 0f..6f)
    private val throughWallsRange by FloatValue("ThroughWallsRange", 3f, 0f..8f)
    private val rangeSprintReduction by FloatValue("RangeSprintReduction", 0f, 0f..0.4f)

    // Modes
    private val priority by ListValue(
        "Priority", arrayOf(
            "Health",
            "Distance",
            "Fov",
            "LivingTime",
            "Armor",
            "HurtResistance",
            "HurtTime",
            "HealthAbsorption",
            "RegenAmplifier",
            "OnLadder",
            "InLiquid",
            "InWeb"
        ), "Distance"
    )
    private val targetMode by ListValue("TargetMode", arrayOf("Single", "Switch", "Multi"), "Switch")
    private val limitedMultiTargets by IntegerValue("LimitedMultiTargets", 0, 0..50) { targetMode == "Multi" }
    private val maxSwitchFOV by FloatValue("MaxSwitchFOV", 90f, 30f..180f) { targetMode == "Switch" }

    // Delay
    private val switchDelay by IntegerValue("SwitchDelay", 15, 1..1000) { targetMode == "Switch" }

    // Bypass
    private val swing by BoolValue("Swing", true)
    private val keepSprint by BoolValue("KeepSprint", true)

    // Settings
    private val autoF5 by BoolValue("AutoF5", false, subjective = true)
    private val onScaffold by BoolValue("OnScaffold", false)
    private val onDestroyBlock by BoolValue("OnDestroyBlock", false)

    // AutoBlock
    val autoBlock by ListValue("AutoBlock", arrayOf("Off", "Packet", "Fake"), "Packet")
    private val blockMaxRange by FloatValue("BlockMaxRange", 3f, 0f..8f) { autoBlock != "Off" }
    private val unblockMode by ListValue("UnblockMode",
        arrayOf("Stop", "Switch", "Empty"),
        "Stop"
    ) { autoBlock != "Off" }
    private val releaseAutoBlock by BoolValue("ReleaseAutoBlock", true)
    { autoBlock !in arrayOf("Off", "Fake") }
    var forceBlockRender by BoolValue("ForceBlockRender", true)
    { autoBlock in arrayOf("Off", "Fake") }
    private val ignoreTickRule by BoolValue("IgnoreTickRule", false)
    { autoBlock !in arrayOf("Off", "Fake") && releaseAutoBlock }
    private val blockRate by IntegerValue("BlockRate", 100, 1..100)
    { autoBlock !in arrayOf("Off", "Fake") && releaseAutoBlock }

    private val uncpAutoBlock by BoolValue("UpdatedNCPAutoBlock", false)
    { autoBlock !in arrayOf("Off", "Fake") && !releaseAutoBlock }

    private val switchStartBlock by BoolValue("SwitchStartBlock", false)
    { autoBlock !in arrayOf("Off", "Fake") }

    private val interactAutoBlock by BoolValue("InteractAutoBlock", true)
    { autoBlock !in arrayOf("Off", "Fake") }

    val blinkAutoBlock by BoolValue("BlinkAutoBlock", false)
    { autoBlock !in arrayOf("Off", "Fake") }

    private val blinkBlockTicks by IntegerValue("BlinkBlockTicks", 3, 2..5)
    { autoBlock !in arrayOf("Off", "Fake") && blinkAutoBlock }

    // AutoBlock conditions
    private val smartAutoBlock by BoolValue("SmartAutoBlock", false) { autoBlock != "Off" }

    // Ignore all blocking conditions, except for block rate, when standing still
    private val forceBlock by BoolValue("ForceBlockWhenStill", true)
    { autoBlock != "Off" && smartAutoBlock }

    // Don't block if target isn't holding a sword or an axe
    private val checkWeapon by BoolValue("CheckEnemyWeapon", true)
    { autoBlock != "Off" && smartAutoBlock }

    // TODO: Make block range independent from attack range
    private var blockRange by object : FloatValue("BlockRange", range, 1f..8f) {
        override fun isSupported() = autoBlock != "Off" && smartAutoBlock

        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(this@KillAura.range)
    }

    // Don't block when you can't get damaged
    private val maxOwnHurtTime by IntegerValue("MaxOwnHurtTime", 3, 0..10)
    { autoBlock != "Off" && smartAutoBlock }

    // Don't block if target isn't looking at you
    private val maxDirectionDiff by FloatValue("MaxOpponentDirectionDiff", 60f, 30f..180f)
    { autoBlock != "Off" && smartAutoBlock }

    // Don't block if target is swinging an item and therefore cannot attack
    private val maxSwingProgress by IntegerValue("MaxOpponentSwingProgress", 1, 0..5)
    { autoBlock != "Off" && smartAutoBlock }

    // Turn Speed
    private val noRotation by BoolValue("NoRotation", false)
    private val startRotatingSlow by BoolValue("StartRotatingSlow", false) { !noRotation }
    private val slowDownOnDirectionChange by BoolValue("SlowDownOnDirectionChange", false) { !noRotation }
    private val useStraightLinePath by BoolValue("UseStraightLinePath", true) { !noRotation }
    private val maxHorizontalSpeedValue = object : FloatValue("MaxHorizontalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minHorizontalSpeed)
        override fun isSupported() = !noRotation
    }
    private val maxHorizontalSpeed by maxHorizontalSpeedValue

    private val minHorizontalSpeed: Float by object : FloatValue("MinHorizontalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxHorizontalSpeed)
        override fun isSupported() = !maxHorizontalSpeedValue.isMinimal() && !noRotation
    }

    private val maxVerticalSpeedValue = object : FloatValue("MaxVerticalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minVerticalSpeed)
        override fun isSupported() = !noRotation
    }
    private val maxVerticalSpeed by maxVerticalSpeedValue

    private val minVerticalSpeed: Float by object : FloatValue("MinVerticalSpeed", 180f, 1f..180f) {
        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxVerticalSpeed)
        override fun isSupported() = !maxVerticalSpeedValue.isMinimal() && !noRotation
    }

    // Raycast
    private val raycastValue = BoolValue("RayCast", true) { !noRotation }
    private val raycast by raycastValue
    private val raycastIgnored by BoolValue("RayCastIgnored", false) { raycastValue.isActive() && !noRotation }
    private val livingRaycast by BoolValue("LivingRayCast", true) { raycastValue.isActive() && !noRotation }

    // Hit delay
    private val useHitDelay by BoolValue("UseHitDelay", false)
    private val hitDelayTicks by IntegerValue("HitDelayTicks", 1, 1..5) { useHitDelay }

    // RotationHandler
    private val keepRotationTicks by object : IntegerValue("KeepRotationTicks", 5, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minimum)
        override fun isSupported() = !noRotation
    }
    private val angleThresholdUntilReset by FloatValue("AngleThresholdUntilReset", 5f, 0.1f..180f) { !noRotation }
    private val minRotationDifference by FloatValue("MinRotationDifference", 0f, 0f..1f) { !noRotation }
    private val silentRotationValue = BoolValue("SilentRotation", true) { !noRotation }
    private val silentRotation by silentRotationValue
    private val rotationStrafe by ListValue("Strafe",
        arrayOf("Off", "Strict", "Silent"),
        "Off"
    ) { silentRotationValue.isActive() && !noRotation }
    private val smootherMode by ListValue("SmootherMode", arrayOf("Linear", "Relative"), "Relative") { !noRotation }

    private val simulateShortStop by BoolValue("SimulateShortStop", false) { !noRotation }
    private val randomizeRotations by BoolValue("RandomizeRotations", true) { !noRotation }
    private val outborder by BoolValue("Outborder", false) { !noRotation }

    private val highestBodyPointToTargetValue: ListValue = object : ListValue("HighestBodyPointToTarget",
        arrayOf("Head", "Body", "Feet"),
        "Head"
    ) {
        override fun isSupported() = !noRotation

        override fun onChange(oldValue: String, newValue: String): String {
            val newPoint = RotationUtils.BodyPoint.fromString(newValue)
            val lowestPoint = RotationUtils.BodyPoint.fromString(lowestBodyPointToTarget)
            val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, lowestPoint, RotationUtils.BodyPoint.HEAD)
            return coercedPoint.name
        }
    }
    private val highestBodyPointToTarget by highestBodyPointToTargetValue

    private val lowestBodyPointToTargetValue: ListValue = object : ListValue("LowestBodyPointToTarget",
        arrayOf("Head", "Body", "Feet"),
        "Feet"
    ) {
        override fun isSupported() = !noRotation

        override fun onChange(oldValue: String, newValue: String): String {
            val newPoint = RotationUtils.BodyPoint.fromString(newValue)
            val highestPoint = RotationUtils.BodyPoint.fromString(highestBodyPointToTarget)
            val coercedPoint = RotationUtils.coerceBodyPoint(newPoint, RotationUtils.BodyPoint.FEET, highestPoint)
            return coercedPoint.name
        }
    }

    private val lowestBodyPointToTarget by lowestBodyPointToTargetValue

    private val maxHorizontalBodySearch: FloatValue = object : FloatValue("MaxHorizontalBodySearch", 1f, 0f..1f) {
        override fun isSupported() = !noRotation

        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtLeast(minHorizontalBodySearch.get())
    }

    private val minHorizontalBodySearch: FloatValue = object : FloatValue("MinHorizontalBodySearch", 0f, 0f..1f) {
        override fun isSupported() = !noRotation

        override fun onChange(oldValue: Float, newValue: Float) = newValue.coerceAtMost(maxHorizontalBodySearch.get())
    }

    private val fov by FloatValue("FOV", 180f, 0f..180f)

    // Prediction
    private val predictClientMovement by IntegerValue("PredictClientMovement", 2, 0..5)
    private val predictOnlyWhenOutOfRange by BoolValue("PredictOnlyWhenOutOfRange",
        false
    ) { predictClientMovement != 0 }
    private val predictEnemyPosition by FloatValue("PredictEnemyPosition", 1.5f, -1f..2f)

    // Extra swing
    private val failSwing by BoolValue("FailSwing", true) { swing && !noRotation }
    private val respectMissCooldown by BoolValue("RespectMissCooldown", false) { swing && failSwing && !noRotation }
    private val swingOnlyInAir by BoolValue("SwingOnlyInAir", true) { swing && failSwing && !noRotation }
    private val maxRotationDifferenceToSwing by FloatValue("MaxRotationDifferenceToSwing", 180f, 0f..180f)
    { swing && failSwing && !noRotation }
    private val swingWhenTicksLate = object : BoolValue("SwingWhenTicksLate", false) {
        override fun isSupported() = swing && failSwing && maxRotationDifferenceToSwing != 180f && !noRotation
    }
    private val ticksLateToSwing by IntegerValue("TicksLateToSwing", 4, 0..20)
    { swing && failSwing && swingWhenTicksLate.isActive() && !noRotation }

    // Inventory
    private val simulateClosingInventory by BoolValue("SimulateClosingInventory", false) { !noInventoryAttack }
    private val noInventoryAttack by BoolValue("NoInvAttack", false)
    private val noInventoryDelay by IntegerValue("NoInvDelay", 200, 0..500) { noInventoryAttack }
    private val noConsumeAttack by ListValue("NoConsumeAttack",
        arrayOf("Off", "NoHits", "NoRotation"),
        "Off",
        subjective = true
    )

    // Visuals
    private val displayDebug = BoolValue("Debug", false)

    /**
     * MODULE
     */

    // Target
    var target: EntityLivingBase? = null
    private var hittable = false
    private val prevTargetEntities = mutableListOf<Int>()

    // Attack delay
    private val attackTimer = MSTimer()
    private var attackDelay = 0
    var clicks = 0
    private var attackTickTimes = mutableListOf<Pair<MovingObjectPosition, Int>>()

    // Container Delay
    private var containerOpen = -1L

    // Block status
    var renderBlocking = false
    var blockStatus = false
    private var blockStopInDead = false

    // Switch Delay
    private val switchTimer = MSTimer()

    // Blink AutoBlock
    private var blinked = false

    // text
    private val textElement = Text()

    /**
     * Disable kill aura module
     */
    override fun onToggle(state: Boolean) {
        target = null
        hittable = false
        prevTargetEntities.clear()
        attackTickTimes.clear()
        attackTimer.reset()
        clicks = 0

        if (blinkAutoBlock) {
            BlinkUtils.unblink()
            blinked = false
        }

        if (autoF5)
            mc.gameSettings.thirdPersonView = 0

        stopBlocking(true)
    }

    /**
     * Motion event
     */
    @EventTarget
    fun onRotationUpdate(event: RotationUpdateEvent) {
        update()
    }

    fun update() {
        if (cancelRun || (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay))) return

        // Update target
        updateTarget()

        if (autoF5) {
            if (mc.gameSettings.thirdPersonView != 1 && (target != null || mc.thePlayer.swingProgress > 0)) {
                mc.gameSettings.thirdPersonView = 1
            }
        }
    }

    @EventTarget
    fun onWorldChange(event: WorldEvent) {
        attackTickTimes.clear()

        if (blinkAutoBlock && BlinkUtils.isBlinking)
            BlinkUtils.unblink()
    }

    /**
     * Tick event
     */
    @EventTarget
    fun onTick(event: GameTickEvent) {
        if (clickOnly && !mc.gameSettings.keyBindAttack.isKeyDown) return

        if (blockStatus && autoBlock == "Packet" && releaseAutoBlock && !ignoreTickRule) {
            clicks = 0
            stopBlocking()
            return
        }

        if (cancelRun) {
            target = null
            hittable = false
            stopBlocking()
            return
        }

        if (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay)) {
            target = null
            hittable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        if (simulateCooldown && getAttackCooldownProgress() < 1f) {
            return
        }

        if (target == null && !blockStopInDead) {
            blockStopInDead = true
            stopBlocking()
            return
        }

        if (blinkAutoBlock) {
            when (mc.thePlayer.ticksExisted % (blinkBlockTicks + 1)) {
                0 -> {
                    if (blockStatus && !blinked && !BlinkUtils.isBlinking) {
                        blinked = true
                    }
                }

                1 -> {
                    if (blockStatus && blinked && BlinkUtils.isBlinking) {
                        stopBlocking()
                    }
                }

                blinkBlockTicks -> {
                    if (!blockStatus && blinked && BlinkUtils.isBlinking) {
                        BlinkUtils.unblink()
                        blinked = false

                        startBlocking(target!!, interactAutoBlock, autoBlock == "Fake") // block again
                    }
                }
            }
        }

        if (target != null) {
            if (mc.thePlayer.getDistanceToEntityBox(target!!) > blockMaxRange && blockStatus) {
                stopBlocking(true)
                return
            } else {
                if (autoBlock != "Off" && !releaseAutoBlock) {
                    renderBlocking = true
                }
            }

            // Usually when you butterfly click, you end up clicking two (and possibly more) times in a single tick.
            // Sometimes you also do not click. The positives outweigh the negatives, however.
            val extraClicks = if (simulateDoubleClicking && !simulateCooldown) nextInt(-1, 1) else 0

            val maxClicks = clicks + extraClicks

            when (clickValue) {
                "Delay" -> {
                    if (afterDelay > 0) {
                        afterDelay--
                    }
                    if (target?.hurtResistantTime!! <= 10 || afterDelay == 0 || mc.thePlayer.hurtTime != 0) {
                        repeat(maxClicks) {
                            val wasBlocking = blockStatus
                            runAttack(it + 1 == maxClicks)

                            if (wasBlocking && !blockStatus && (releaseAutoBlock && !ignoreTickRule || autoBlock == "Off")) {
                                return
                            }
                        }
                    }
                    if (mc.thePlayer.hurtTime != 0)
                        afterDelay = 0
                    clicks = 0
                }

                "HurtTime" -> {
                    repeat(maxClicks) {
                        val wasBlocking = blockStatus

                        runAttack(it + 1 == maxClicks)
                        clicks--

                        if (wasBlocking && !blockStatus && (releaseAutoBlock && !ignoreTickRule || autoBlock == "Off")) {
                            return
                        }
                    }
                }

                "SmartDelay" -> {
                    if (target?.hurtTime == 0) {
                        afterDelay = 0
                    }
                    if (afterDelay > 0) {
                        afterDelay--
                    }
                    if (target?.hurtTime!! == 0 || afterDelay == 0 || mc.thePlayer.hurtTime > minHurtTimeToClick) {
                        repeat(maxClicks) {
                            val wasBlocking = blockStatus
                            runAttack(it + 1 == maxClicks)

                            if (wasBlocking && !blockStatus && (releaseAutoBlock && !ignoreTickRule || autoBlock == "Off")) {
                                return
                            }
                        }
                    }
                    if (mc.thePlayer.hurtTime > minHurtTimeToClick) {
                        afterDelay = 0
                    }
                    clicks = 0
                }
            }
        } else {
            renderBlocking = false
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (cancelRun) {
            target = null
            hittable = false
            return
        }

        if (noInventoryAttack && (mc.currentScreen is GuiContainer || System.currentTimeMillis() - containerOpen < noInventoryDelay)) {
            target = null
            hittable = false
            if (mc.currentScreen is GuiContainer) containerOpen = System.currentTimeMillis()
            return
        }

        target ?: return

        if (attackTimer.hasTimePassed(attackDelay)) {
            if (maxCPS > 0)
                clicks++
            attackTimer.reset()
            attackDelay = randomClickDelay(minCPS, maxCPS)
        }
    }

    /**
     * Render event
     */
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (displayDebug.get()) {
            val sr = ScaledResolution(mc)
            val blockingStatus = blockStatus
            val maxRange = this.maxRange

            val reach = if (target != null) {
                mc.thePlayer.getDistanceToEntityBox(target!!)
            } else {
                0.0
            }

            val formattedReach = String.format("%.2f", reach)

            val rangeString = "Range: $maxRange"
            val reachString = "Reach: $formattedReach"

            val cpsString = textElement.getReplacement("cps")
            val status = "Blocking: ${if (blockingStatus) "Yes" else "No"}, CPS: $cpsString, $reachString, $rangeString"
            Fonts.minecraftFont.drawStringWithShadow(
                status,
                sr.scaledWidth / 2f - Fonts.minecraftFont.getStringWidth(status) / 2f,
                sr.scaledHeight / 2f - 60f,
                Color.orange.rgb
            )
        }
    }

    /**
     * Attack enemy
     */
    @Suppress("VARIABLE_WITH_REDUNDANT_INITIALIZER")
    private fun runAttack(isLastClick: Boolean) {
        var currentTarget = this.target ?: return

        val thePlayer = mc.thePlayer ?: return
        val theWorld = mc.theWorld ?: return

        if (noConsumeAttack == "NoHits" && isConsumingItem()) {
            return
        }

        // Settings
        val multi = targetMode == "Multi"
        val manipulateInventory = simulateClosingInventory && !noInventoryAttack && serverOpenInventory

        // Close inventory when open
        if (manipulateInventory) serverOpenInventory = false

        updateHittable()

        currentTarget = this.target ?: return

        // Randomize cooldown
        val hurtTime = nextInt(minHurtTime, maxHurtTime)
        afterDelay = nextInt(minAfterDelay, maxAfterDelay)

        if (hittable) {
            if (clickValue == "HurtTime") {
                if (currentTarget.hurtTime < hurtTime || (mc.thePlayer.hurtTime > 0 && SmartAttack)) {
                } else {
                    return
                }
            }
        }

        // Check if enemy is not hittable
        if (!hittable && !noRotation) {
            if (swing && failSwing) {
                val rotation = currentRotation ?: thePlayer.rotation

                // Can humans keep click consistency when performing massive rotation changes?
                // (10-30 rotation difference/doing large mouse movements for example)
                // Maybe apply to attacks too?
                if (getRotationDifference(rotation) > maxRotationDifferenceToSwing) {
                    // At the same time there is also a chance of the user clicking at least once in a while
                    // when the consistency has dropped a lot.
                    val shouldIgnore = swingWhenTicksLate.isActive() && ticksSinceClick() >= ticksLateToSwing

                    if (!shouldIgnore) {
                        return
                    }
                }

                runWithModifiedRaycastResult(rotation, range.toDouble(), throughWallsRange.toDouble()) {
                    if (swingOnlyInAir && !it.typeOfHit.isMiss) {
                        return@runWithModifiedRaycastResult
                    }

                    // Left click miss cool-down logic:
                    // When you click and miss, you receive a 10 tick cool down.
                    // It decreases gradually (tick by tick) when you hold the button.
                    // If you click and then release the button, the cool down drops from where it was immediately to 0.
                    // Most humans will release the button 1-2 ticks max after clicking, leaving them with an average of 10 CPS.
                    // The maximum CPS allowed when your miss is 20 CPS, if you click and release immediately, which is highly unlikely.
                    // With that being said, we force an average of 10 CPS by doing this below, since 10 CPS when missing is possible.
                    if (respectMissCooldown && ticksSinceClick() <= 1 && it.typeOfHit.isMiss) {
                        return@runWithModifiedRaycastResult
                    }

                    if (!shouldDelayClick(it.typeOfHit)) {
                        if (attackMode == "Packet") {
                            if (it.typeOfHit.isEntity) {
                                val entity = it.entityHit

                                // Use own function instead of clickMouse() to maintain keep sprint, auto block, etc
                                if (entity is EntityLivingBase) {
                                    attackEntity(entity, isLastClick)
                                }
                            } else {
                                // Imitate game click
                                mc.clickMouse()
                            }
                        }
                        if (attackMode == "Legit") {
                            mc.clickMouse()
                        }
                        attackTickTimes += it to runTimeTicks
                    }

                    if (isLastClick) {
                        // We return false because when you click once then immediately release, the attack key's [pressed] status is false.
                        // Since we simulate clicks, we are supposed to respect that behavior.
                        mc.sendClickBlockToController(false)
                    }
                }
            }
        } else {
            blockStopInDead = false
            // Attack
            if (!multi) {
                attackEntity(currentTarget, isLastClick)
            } else {
                var targets = 0

                for (entity in theWorld.loadedEntityList) {
                    val distance = thePlayer.getDistanceToEntityBox(entity)

                    if (entity is EntityLivingBase && isEnemy(entity) && distance <= getRange(entity)) {
                        attackEntity(entity, isLastClick)

                        targets += 1

                        if (limitedMultiTargets != 0 && limitedMultiTargets <= targets) break
                    }
                }
            }

            val switchMode = targetMode == "Switch"

            if (!switchMode || switchTimer.hasTimePassed(switchDelay)) {
                prevTargetEntities += currentTarget.entityId

                if (switchMode) {
                    switchTimer.reset()
                }
            }
        }

        // Open inventory
        if (manipulateInventory) serverOpenInventory = true
    }

    /**
     * Update current target
     */
    private fun updateTarget() {
        if (!onScaffold && Scaffold.handleEvents() && (Tower.placeInfo != null || Scaffold.placeRotation != null))
            return

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return

        // Reset fixed target to null
        target = null

        // Settings
        val fov = fov
        val switchMode = targetMode == "Switch"

        // Find possible targets
        val targets = mutableListOf<EntityLivingBase>()

        val theWorld = mc.theWorld
        val thePlayer = mc.thePlayer

        for (entity in theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase || !isEnemy(entity) || (switchMode && entity.entityId in prevTargetEntities)) continue

            // Will skip new target nearby if fail to hit/couldn't be hit.
            // Since without this check, it seems killaura (Switch) will get stuck.
            // Temporary fix
            if (switchMode && !hittable && prevTargetEntities.isNotEmpty()) continue

            var distance = thePlayer.getDistanceToEntityBox(entity)

            if (Backtrack.handleEvents()) {
                val trackedDistance = Backtrack.getNearestTrackedDistance(entity)

                if (distance > trackedDistance) {
                    distance = trackedDistance
                }
            }

            val entityFov = getRotationDifference(entity)

            if (distance <= maxRange && (fov == 180F || entityFov <= fov)) {
                if (switchMode && isLookingOnEntities(entity, maxSwitchFOV.toDouble()) || !switchMode) {
                    targets += entity
                }
            }
        }

        // Sort targets by priority
        when (priority.lowercase()) {
            "distance" -> {
                targets.sortBy {
                    var result = 0.0

                    Backtrack.runWithNearestTrackedDistance(it) {
                        result = thePlayer.getDistanceToEntityBox(it) // Sort by distance
                    }

                    result
                }
            }

            "fov" -> targets.sortBy {
                var result = 0f

                Backtrack.runWithNearestTrackedDistance(it) {
                    result = getRotationDifference(it) // Sort by FOV
                }

                result
            }

            "health" -> targets.sortBy { it.health } // Sort by health
            "livingtime" -> targets.sortBy { -it.ticksExisted } // Sort by existence
            "armor" -> targets.sortBy { it.totalArmorValue } // Sort by armor
            "hurtresistance" -> targets.sortBy { it.hurtResistantTime } // Sort by armor hurt time
            "hurttime" -> targets.sortBy { it.hurtTime } // Sort by hurt time
            "healthabsorption" -> targets.sortBy { it.health + it.absorptionAmount } // Sort by full health with absorption effect
            "regenamplifier" -> targets.sortBy {
                if (it.isPotionActive(Potion.regeneration)) it.getActivePotionEffect(Potion.regeneration).amplifier else -1
            }
            "inweb" -> targets.sortBy { if (it.isInWeb) -1 else 1 } // Sort by whether the target is inside a web block
            "onladder" -> targets.sortBy { if (it.isOnLadder) -1 else 1 } // Sort by on a ladder
            "inliquid" -> targets.sortBy { if (it.isInWater || it.isInLava) -1 else 1 } // Sort by whether the target is in water or lava
        }


        // Find best target
        for (entity in targets) {
            // Update rotations to current target
            var success = false

            Backtrack.runWithNearestTrackedDistance(entity) {
                success = updateRotations(entity)
            }

            if (!success) {
                // when failed then try another target
                continue
            }

            // Set target to current entity
            target = entity
            return
        }

        // Cleanup last targets when no target found and try again
        if (prevTargetEntities.isNotEmpty()) {
            prevTargetEntities.clear()
            updateTarget()
        }
    }

    /**
     * Check if [entity] is selected as enemy with current target options and other modules
     */
    private fun isEnemy(entity: Entity?): Boolean {
        return isSelected(entity, true)
    }

    /**
     * Attack [entity]
     */
    private fun attackEntity(entity: EntityLivingBase, isLastClick: Boolean) {
        // Stop blocking
        val thePlayer = mc.thePlayer

        if (!onScaffold && Scaffold.handleEvents() && (Tower.placeInfo != null || Scaffold.placeRotation != null))
            return

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return

        if (thePlayer.isBlocking && (autoBlock == "Off" && blockStatus || autoBlock == "Packet" && releaseAutoBlock)) {
            stopBlocking()

            if (!ignoreTickRule || autoBlock == "Off") {
                return
            }
        }

        // The function is only called when we are facing an entity
        if (shouldDelayClick(MovingObjectPosition.MovingObjectType.ENTITY)) {
            return
        }

        if (!blinkAutoBlock || blinkAutoBlock && !BlinkUtils.isBlinking) {
            // Call attack event
            callEvent(AttackEvent(entity))

            // Attack target
            if (swing) thePlayer.swingItem()

            sendPacket(C02PacketUseEntity(entity, ATTACK))
        }

        if (keepSprint && !KeepSprint.state) {
            // Critical Effect
            if (thePlayer.fallDistance > 0F && !thePlayer.onGround && !thePlayer.isOnLadder && !thePlayer.isInWater && !thePlayer.isPotionActive(
                    Potion.blindness
                ) && !thePlayer.isRiding) {
                thePlayer.onCriticalHit(entity)
            }

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(thePlayer.heldItem, entity.creatureAttribute) > 0F) {
                thePlayer.onEnchantmentCritical(entity)
            }
        } else {
            if (mc.playerController.currentGameType != WorldSettings.GameType.SPECTATOR) {
                thePlayer.attackTargetEntityWithCurrentItem(entity)
            }
        }

        CPSCounter.registerClick(CPSCounter.MouseButton.LEFT)

        // Start blocking after attack
        if (autoBlock != "Off" && (thePlayer.isBlocking || canBlock) && (!blinkAutoBlock && isLastClick || blinkAutoBlock && (!blinked || !BlinkUtils.isBlinking))) {
            startBlocking(entity, interactAutoBlock, autoBlock == "Fake")
        }

        resetLastAttackedTicks()
    }

    /**
     * Update killaura rotations to enemy
     */
    private fun updateRotations(entity: Entity): Boolean {
        val player = mc.thePlayer ?: return false

        if (!onScaffold && Scaffold.handleEvents() && (Tower.placeInfo != null || Scaffold.placeRotation != null))
            return false

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return false

        if (noRotation) {
            return player.getDistanceToEntityBox(entity) <= range
        }

        val (predictX, predictY, predictZ) = entity.currPos.subtract(entity.prevPos)
            .times(2 + predictEnemyPosition.toDouble())

        val boundingBox = entity.hitBox.offset(predictX, predictY, predictZ)
        val (currPos, oldPos) = player.currPos to player.prevPos

        val simPlayer = SimulatedPlayer.fromClientPlayer(player.movementInput)

        var pos = currPos

        for (i in 0..predictClientMovement + 1) {
            val previousPos = simPlayer.pos

            simPlayer.tick()

            if (predictOnlyWhenOutOfRange) {
                player.setPosAndPrevPos(simPlayer.pos)

                val currDist = player.getDistanceToEntityBox(entity)

                player.setPosAndPrevPos(previousPos)

                val prevDist = player.getDistanceToEntityBox(entity)

                player.setPosAndPrevPos(currPos, oldPos)
                pos = simPlayer.pos

                if (currDist <= range && currDist <= prevDist) {
                    continue
                }
            }

            pos = previousPos
        }

        player.setPosAndPrevPos(pos)

        val rotation = searchCenter(
            boundingBox,
            outborder && !attackTimer.hasTimePassed(attackDelay / 2),
            randomizeRotations,
            predict = false,
            lookRange = scanRange,
            attackRange = range,
            throughWallsRange = throughWallsRange,
            bodyPoints = listOf(highestBodyPointToTarget, lowestBodyPointToTarget),
            horizontalSearch = minHorizontalBodySearch.get()..maxHorizontalBodySearch.get()
        )

        if (rotation == null) {
            player.setPosAndPrevPos(currPos, oldPos)

            return false
        }

        setTargetRotation(
            rotation,
            keepRotationTicks,
            silentRotation && rotationStrafe != "Off",
            silentRotation && rotationStrafe == "Strict",
            !silentRotation,
            minHorizontalSpeed..maxHorizontalSpeed to minVerticalSpeed..maxVerticalSpeed,
            angleThresholdUntilReset,
            smootherMode,
            simulateShortStop,
            startRotatingSlow,
            slowDownOnDirChange = slowDownOnDirectionChange,
            useStraightLinePath = useStraightLinePath,
            minRotationDifference = minRotationDifference
        )

        player.setPosAndPrevPos(currPos, oldPos)

        return true
    }

    private fun ticksSinceClick() = runTimeTicks - (attackTickTimes.lastOrNull()?.second ?: 0)

    /**
     * Check if enemy is hittable with current rotations
     */
    private fun updateHittable() {
        val eyes = mc.thePlayer.eyes

        val currentRotation = currentRotation ?: mc.thePlayer.rotation
        val target = this.target ?: return

        if (!onScaffold && Scaffold.handleEvents() && (Tower.placeInfo != null || Scaffold.placeRotation != null))
            return

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return

        if (noRotation) {
            hittable = mc.thePlayer.getDistanceToEntityBox(target) <= range
            return
        }

        var chosenEntity: Entity? = null

        if (raycast) {
            chosenEntity = raycastEntity(range.toDouble(),
                currentRotation.yaw,
                currentRotation.pitch
            ) { entity -> !livingRaycast || entity is EntityLivingBase && entity !is EntityArmorStand }

            if (chosenEntity != null && chosenEntity is EntityLivingBase && (!(chosenEntity is EntityPlayer && chosenEntity.isClientFriend()))) {
                if (raycastIgnored && target != chosenEntity) {
                    this.target = chosenEntity
                }
            }

            hittable = this.target == chosenEntity
        } else {
            hittable = isRotationFaced(target, range.toDouble(), currentRotation)
        }

        if (!hittable) {
            return
        }

        val targetToCheck = chosenEntity ?: this.target ?: return

        // If player is inside entity, automatic yes because the intercept below cannot check for that
        // Minecraft does the same, see #EntityRenderer line 353
        if (targetToCheck.hitBox.isVecInside(eyes)) {
            return
        }

        var checkNormally = true

        if (Backtrack.handleEvents()) {
            Backtrack.loopThroughBacktrackData(targetToCheck) {
                if (targetToCheck.hitBox.isVecInside(eyes)) {
                    checkNormally = false
                    return@loopThroughBacktrackData true
                }

                // Recreate raycast logic
                val intercept = targetToCheck.hitBox.calculateIntercept(eyes,
                    eyes + getVectorForRotation(currentRotation) * range.toDouble()
                )

                if (intercept != null) {
                    // Is the entity box raycast vector visible? If not, check through-wall range
                    hittable = isVisible(intercept.hitVec) || mc.thePlayer.getDistanceToEntityBox(targetToCheck) <= throughWallsRange

                    if (hittable) {
                        checkNormally = false
                        return@loopThroughBacktrackData true
                    }
                }

                return@loopThroughBacktrackData false
            }
        }

        if (!checkNormally) {
            return
        }

        // Recreate raycast logic
        val intercept = targetToCheck.hitBox.calculateIntercept(eyes,
            eyes + getVectorForRotation(currentRotation) * range.toDouble()
        )

        // Is the entity box raycast vector visible? If not, check through-wall range
        hittable = isVisible(intercept.hitVec) || mc.thePlayer.getDistanceToEntityBox(targetToCheck) <= throughWallsRange
    }

    /**
     * Start blocking
     */
    private fun startBlocking(interactEntity: Entity, interact: Boolean, fake: Boolean = false) {
        val player = mc.thePlayer ?: return

        if (blockStatus && (!uncpAutoBlock || !blinkAutoBlock))
            return

        if (!onScaffold && Scaffold.handleEvents() && (Tower.placeInfo != null || Scaffold.placeRotation != null))
            return

        if (!onDestroyBlock && ((Fucker.handleEvents() && !Fucker.noHit && Fucker.pos != null) || Nuker.handleEvents()))
            return

        if (mc.thePlayer.isBlocking) {
            blockStatus = true
            renderBlocking = true
            return
        }

        if (!fake) {
            if (!(blockRate > 0 && nextInt(endExclusive = 100) <= blockRate)) return

            if (interact) {
                val positionEye = player.eyes

                val boundingBox = interactEntity.hitBox

                val (yaw, pitch) = currentRotation ?: player.rotation

                val vec = getVectorForRotation(Rotation(yaw, pitch))

                val lookAt = positionEye.add(vec * maxRange.toDouble())

                val movingObject = boundingBox.calculateIntercept(positionEye, lookAt) ?: return
                val hitVec = movingObject.hitVec

                sendPackets(
                    C02PacketUseEntity(interactEntity, hitVec - interactEntity.positionVector),
                    C02PacketUseEntity(interactEntity, INTERACT)
                )

            }

            if (switchStartBlock) {
                InventoryUtils.serverSlot = (InventoryUtils.serverSlot + 1) % 9
                InventoryUtils.serverSlot = player.inventory.currentItem
            }

            sendPacket(C08PacketPlayerBlockPlacement(player.heldItem))
            blockStatus = true
        }

        renderBlocking = true

        CPSCounter.registerClick(CPSCounter.MouseButton.RIGHT)
    }

    /**
     * Stop blocking
     */
    private fun stopBlocking(forceStop: Boolean = false) {
        val player = mc.thePlayer ?: return
        val currentItem = player.inventory?.currentItem ?: return

        if (!forceStop) {
            if (blockStatus && !mc.thePlayer.isBlocking) {

                when (unblockMode.lowercase()) {
                    "stop" -> sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
                    "switch" -> {
                        InventoryUtils.serverSlot = (InventoryUtils.serverSlot + 1) % 9
                        InventoryUtils.serverSlot = currentItem
                    }

                    "empty" -> {
                        InventoryUtils.serverSlot = player.inventory.firstEmptyStack
                        InventoryUtils.serverSlot = currentItem
                    }
                }

                blockStatus = false
            }
        } else {
            if (blockStatus) {
                sendPacket(C07PacketPlayerDigging(RELEASE_USE_ITEM, BlockPos.ORIGIN, EnumFacing.DOWN))
            }
            blockStatus = false
        }

        renderBlocking = false
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val player = mc.thePlayer ?: return
        val packet = event.packet

        if (autoBlock == "Off" || !blinkAutoBlock || !blinked)
            return

        if (player.isDead || player.ticksExisted < 20) {
            BlinkUtils.unblink()
            return
        }

        if (Blink.blinkingSend() || Blink.blinkingReceive()) {
            BlinkUtils.unblink()
            return
        }

        BlinkUtils.blink(packet, event)
    }

    /**
     * Checks if raycast landed on a different object
     *
     * The game requires at least 1 tick of cool-down on raycast object type change (miss, block, entity)
     * We are doing the same thing here but allow more cool-down.
     */
    private fun shouldDelayClick(currentType: MovingObjectPosition.MovingObjectType): Boolean {
        if (!useHitDelay) {
            return false
        }

        val lastAttack = attackTickTimes.lastOrNull()

        return lastAttack != null && lastAttack.first.typeOfHit != currentType && runTimeTicks - lastAttack.second <= hitDelayTicks
    }

    /**
     * Check if run should be cancelled
     */
    private val cancelRun
        inline get() = mc.thePlayer.isSpectator || !isAlive(mc.thePlayer) || FreeCam.handleEvents() || (noConsumeAttack == "NoRotation" && isConsumingItem())

    /**
     * Check if [entity] is alive
     */
    private fun isAlive(entity: EntityLivingBase) = entity.isEntityAlive && entity.health > 0

    /**
     * Check if player is able to block
     */
    private val canBlock: Boolean
        get() {
            if (target != null && mc.thePlayer?.heldItem?.item is ItemSword) {
                if (smartAutoBlock) {
                    if (!isMoving && forceBlock) return true

                    if (checkWeapon && (target!!.heldItem?.item !is ItemSword && target!!.heldItem?.item !is ItemAxe))
                        return false

                    if (mc.thePlayer.hurtTime > maxOwnHurtTime) return false

                    val rotationToPlayer = toRotation(mc.thePlayer.hitBox.center, true, target!!)

                    if (getRotationDifference(rotationToPlayer, target!!.rotation) > maxDirectionDiff)
                        return false

                    if (target!!.swingProgressInt > maxSwingProgress) return false

                    if (target!!.getDistanceToEntityBox(mc.thePlayer) > blockRange) return false
                }

                if (mc.thePlayer.getDistanceToEntityBox(target!!) > blockMaxRange) return false

                return true
            }

            return false
        }

    /**
     * Range
     */
    private val maxRange
        get() = max(range + scanRange, throughWallsRange)

    private fun getRange(entity: Entity) =
        (if (mc.thePlayer.getDistanceToEntityBox(entity) >= throughWallsRange) range + scanRange else throughWallsRange) - if (mc.thePlayer.isSprinting) rangeSprintReduction else 0F

    /**
     * HUD Tag
     */
    override val tag
        get() = target?.name

    val isBlockingChestAura
        get() = handleEvents() && target != null
}