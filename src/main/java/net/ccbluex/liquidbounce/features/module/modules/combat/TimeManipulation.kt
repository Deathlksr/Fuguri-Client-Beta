package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.event.EventState
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.MotionEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.player.Blink
import net.ccbluex.liquidbounce.features.module.modules.player.scaffolds.Scaffold
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.RaycastTimerRange
import net.ccbluex.liquidbounce.utils.extensions.expands
import net.ccbluex.liquidbounce.utils.extensions.getNearestPointBB
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import kotlin.math.min

object TimeManipulation : Module("TimeManipulation", Category.COMBAT, hideModule = false) {

    private val mode = ListValue("Mode", arrayOf("RayCast", "Radius"), "RayCast")
    private val minDistance: FloatValue = object : FloatValue("MinDistance", 3F, 2F..3F) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue > maxDistance.get()) set(maxDistance.get())
        }
    }

    private val maxDistance: FloatValue = object : FloatValue("MaxDistance", 4F, 3F..6F) {
        override fun onChanged(oldValue: Float, newValue: Float) {
            if (newValue < minDistance.get()) set(minDistance.get())
        }
    }

    private val rangeMode = ListValue("RangeMode", arrayOf("Setting", "Smart"), "Smart")
    private val maxTimeValue = IntegerValue("MaxTime", 3, 0..20)
    private val delayValue = IntegerValue("Delay", 5, 0..20)
    private val maxHurtTimeValue = IntegerValue("TargetMaxHurtTime", 2, 0..10)
    private val onlyKillAura = BoolValue("OnlyKillAura", true)
    private val onlyPlayer = BoolValue("OnlyPlayer", true)
    private val blink = BoolValue("Blink", false)
    private val debug = BoolValue("Debug", false)

    private val maxReverseRange by FloatValue("MaxReverseRange", 3f, 3f..4f)

    private val minReverseRange by FloatValue("MinReverseRange", 2.5f, 2f..3f)

    private val reverseTickTime by IntegerValue("ReverseTickTime", 2, 0..10)

    private val reverseDelay = IntegerValue("ReverseDelay", 5, 0..20)
    private val reverseTargetMaxHurtTime = IntegerValue("ReverseTargetMaxHurtTime", 10, 0..10)
    private var reverseAuraClicks = BoolValue("ReverseAuraClick",  true)
    private val reverseAuraClick = ListValue("ReverseAuraMode", arrayOf("BeforeTeleport", "AfterTeleport"), "BeforeTeleport") { reverseAuraClicks.get() }

    private val killAura: KillAura = KillAura

    @JvmStatic
    private var working = false
    private var stopWorking = false
    private var lastNearest = 10.0
    private var cooldown = 0
    private var freezeTicks = 0
    private var reverseFreeze = true
    private var firstAnimation = true
    private var betterAnimation = true
    private var reverseValue = true
    private var auraClick = true
    private var modeAuraClick = true

    override fun onDisable() {
        Blink.state = false
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        val screen = mc.currentScreen
        if (screen is GuiInventory) return
        if (screen is GuiContainer) return
        if (mc.thePlayer.isInWater) return
        if (mc.thePlayer.isInLava) return
        if (mc.thePlayer.isInWeb) return
        if (!MovementUtils.isMoving) return
        if (Scaffold.state) return
        Blink.state = blink.get()
        if (event.eventState == EventState.PRE) return // post event mean player's tick is done
        val thePlayer = mc.thePlayer ?: return
        if (onlyKillAura.get() && !killAura.state) return
        if (mode.get() == "RayCast") {
            val entity =
                RaycastTimerRange.raycastEntity(maxDistance.get() + 1.0, object : RaycastTimerRange.IEntityFilter {
                    override fun canRaycast(entity: Entity?): Boolean {
                        return entity != null && entity is EntityLivingBase && (!onlyPlayer.get() || entity is EntityPlayer)
                    }
                })
            if (entity == null || entity !is EntityLivingBase) {
                lastNearest = 10.0
                return
            }
            if (!EntityUtils.isSelected(entity, true)) return
            val vecEyes = thePlayer.getPositionEyes(1f)
            val predictEyes = if (rangeMode.get() == "Smart") {
                thePlayer.getPositionEyes(maxTimeValue.get() + 1f)
            } else thePlayer.getPositionEyes(3f)
            val entityBox = entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())
            val box = getNearestPointBB(
                vecEyes,
                entityBox
            )
            val box2 = getNearestPointBB(
                predictEyes,
                if (entity is EntityOtherPlayerMP) {
                    entityBox.offset(
                        entity.otherPlayerMPX - entity.posX,
                        entity.otherPlayerMPY - entity.posY,
                        entity.otherPlayerMPZ - entity.posZ
                    )
                } else entityBox
            )
            val range = box.distanceTo(vecEyes)
            val afterRange = box2.distanceTo(predictEyes)
            if (!working && reverseValue) {
                if (range in minReverseRange..maxReverseRange && cooldown <= 0 && entity.hurtTime <= reverseTargetMaxHurtTime.get()) {
                    firstAnimation = false
                    reverseFreeze = false
                    return
                }
            }
            if (range < minDistance.get()) {
                stopWorking = true
            } else if (((rangeMode.get() == "Smart" && range > minDistance.get() && afterRange < minDistance.get() && afterRange < range) || (rangeMode.get() == "Setting" && range <= maxDistance.get() && range < lastNearest && afterRange < range)) && entity.hurtTime <= maxHurtTimeValue.get()) {
                stopWorking = false
                foundTarget()
            }
            lastNearest = range
        } else {
            val entityList = mc.theWorld.getEntitiesWithinAABBExcludingEntity(
                thePlayer,
                thePlayer.entityBoundingBox.expands(maxDistance.get() + 1.0)
            )
            if (entityList.isNotEmpty()) {
                val vecEyes = thePlayer.getPositionEyes(1f)
                val afterEyes = if (rangeMode.get() == "Smart") {
                    thePlayer.getPositionEyes(maxTimeValue.get() + 1f)
                } else thePlayer.getPositionEyes(3f)
                var targetFound = false
                var targetInRange = false
                var nearest = 10.0
                for (entity in entityList) {
                    if (entity !is EntityLivingBase) continue
                    if (onlyPlayer.get() && entity !is EntityPlayer) continue
                    if (!EntityUtils.isSelected(entity, true)) continue
                    val entityBox = entity.entityBoundingBox.expands(entity.collisionBorderSize.toDouble())
                    val box = getNearestPointBB(
                        vecEyes,
                        entityBox
                    )
                    val box2 = getNearestPointBB(
                        afterEyes,
                        if (entity is EntityOtherPlayerMP) {
                            entityBox.offset(
                                entity.otherPlayerMPX - entity.posX,
                                entity.otherPlayerMPY - entity.posY,
                                entity.otherPlayerMPZ - entity.posZ
                            )
                        } else entityBox
                    )
                    val range = box.distanceTo(vecEyes)
                    val afterRange = box2.distanceTo(afterEyes)
                    if (!working && reverseValue) {
                        if (range in minReverseRange..maxReverseRange && cooldown <= 0 && entity.hurtTime <= reverseTargetMaxHurtTime.get()) {
                            firstAnimation = false
                            reverseFreeze = false
                            return
                        }
                    }
                    if (range < minDistance.get()) {
                        targetInRange = true
                        break
                    } else if (range <= maxDistance.get() && afterRange < range && entity.hurtTime <= maxHurtTimeValue.get()) {
                        targetFound = true
                    }
                    nearest = min(nearest, range)
                }
                if (targetInRange) {
                    stopWorking = true
                } else if (targetFound && nearest < lastNearest) {
                    stopWorking = false
                    foundTarget()
                }
                lastNearest = nearest
            } else {
                lastNearest = 10.0
            }
        }
    }

    private fun foundTarget() {
        if (cooldown > 0 || freezeTicks != 0 || maxTimeValue.get() == 0) return
        cooldown = delayValue.get()
        working = true
        freezeTicks = 0
        if (betterAnimation) firstAnimation = false
        while (freezeTicks <= maxTimeValue.get() - (if (auraClick) 1 else 0) && !stopWorking) {
            ++freezeTicks
            mc.runTick()
        }
        if (debug.get()) ClientUtils.displayChatMessage("Teleported: $freezeTicks ticks")
        if (auraClick) {
            if (modeAuraClick) killAura.clicks += 1
            ++freezeTicks
            mc.runTick()
        }
        stopWorking = false
        working = false
    }

    @JvmStatic
    fun handleTick(): Boolean {
        if (working || freezeTicks < 0) return true
        if (state && freezeTicks > 0) {
            --freezeTicks
            return true
        }

        if (!reverseFreeze) {
            freezeTicks = 0
            var time = reverseTickTime
            working = true
            if (reverseAuraClicks.get()) {
                if (reverseAuraClick.get() === "BeforeTeleport") killAura.clicks += 1
            }
            while (time > 0) {
                freezeTicks++
                --time
                mc.runTick()
            }
            if (debug.get()) ClientUtils.displayChatMessage("Teleported: $freezeTicks ticks")
            reverseFreeze = true
            working = false
            cooldown = reverseDelay.get()
            if (reverseAuraClicks.get()) {
                if (reverseAuraClick.get() == "AfterTeleport") killAura.clicks += 1
            }
        }
        if (cooldown > 0) --cooldown
        return false
    }

    override val tag
        get() = "${maxTimeValue.get()} : $reverseTickTime"

    @JvmStatic
    fun freezeAnimation(): Boolean {
        if (freezeTicks != 0) {
            if (!firstAnimation) {
                firstAnimation = true
                return false
            }
            return true
        }
        return false
    }
}