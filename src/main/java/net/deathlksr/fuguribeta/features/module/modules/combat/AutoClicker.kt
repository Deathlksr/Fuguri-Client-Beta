/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.combat

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.EntityUtils.isLookingOnEntities
import net.deathlksr.fuguribeta.utils.EntityUtils.isSelected
import net.deathlksr.fuguribeta.utils.extensions.fixedSensitivityPitch
import net.deathlksr.fuguribeta.utils.extensions.fixedSensitivityYaw
import net.deathlksr.fuguribeta.utils.extensions.getDistanceToEntityBox
import net.deathlksr.fuguribeta.utils.extensions.isBlock
import net.deathlksr.fuguribeta.utils.misc.RandomUtils
import net.deathlksr.fuguribeta.utils.misc.RandomUtils.nextFloat
import net.deathlksr.fuguribeta.utils.timing.TimeUtils.randomClickDelay
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.client.settings.KeyBinding
import net.minecraft.entity.Entity
import net.minecraft.item.EnumAction
import kotlin.random.Random.Default.nextBoolean

object AutoClicker : Module("AutoClicker", Category.COMBAT, hideModule = false) {

    private val simulateDoubleClicking by BoolValue("SimulateDoubleClicking", false)

    private val maxCPSValue: IntegerValue = object : IntegerValue("MaxCPS", 8, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minCPS)
    }
    private val maxCPS by maxCPSValue

    private val minCPS by object : IntegerValue("MinCPS", 5, 1..20) {
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxCPS)

        override fun isSupported() = !maxCPSValue.isMinimal()
    }

    private val right by BoolValue("Right", true)
    private val left by BoolValue("Left", true)
    private val jitter by BoolValue("Jitter", false)
    private val block by BoolValue("AutoBlock", false) { left }
    private val blockDelay by IntegerValue("BlockDelay", 50, 0..100) { block }

    private val requiresNoInput by BoolValue("RequiresNoInput", false) { left }
    private val maxAngleDifference by FloatValue("MaxAngleDifference", 30f, 10f..180f) { left && requiresNoInput }
    private val range by FloatValue("Range", 3f, 0.1f..5f) { left && requiresNoInput }

    private var rightDelay = randomClickDelay(minCPS, maxCPS)
    private var rightLastSwing = 0L
    private var leftDelay = randomClickDelay(minCPS, maxCPS)
    private var leftLastSwing = 0L

    private var lastBlocking = 0L

    private val shouldAutoClick
        get() = mc.thePlayer.capabilities.isCreativeMode || !mc.objectMouseOver.typeOfHit.isBlock

    private var shouldJitter = false

    override fun onDisable() {
        rightLastSwing = 0L
        leftLastSwing = 0L
        lastBlocking = 0L
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        mc.thePlayer?.let { thePlayer ->
            val time = System.currentTimeMillis()
            val doubleClick = if (simulateDoubleClicking) RandomUtils.nextInt(-1, 1) else 0

            if (block && thePlayer.swingProgress > 0 && !mc.gameSettings.keyBindUseItem.isKeyDown) {
                mc.gameSettings.keyBindUseItem.pressTime = 0
            }

            if (right && mc.gameSettings.keyBindUseItem.isKeyDown && time - rightLastSwing >= rightDelay) {
                handleRightClick(time, doubleClick)
            }

            if (requiresNoInput) {
                val nearbyEntity = getNearestEntityInRange() ?: return
                if (!isLookingOnEntities(nearbyEntity, maxAngleDifference.toDouble())) return

                if (left && shouldAutoClick && time - leftLastSwing >= leftDelay) {
                    handleLeftClick(time, doubleClick)
                } else if (block && !mc.gameSettings.keyBindUseItem.isKeyDown && shouldAutoClick && shouldAutoRightClick() && mc.gameSettings.keyBindAttack.pressTime != 0) {
                    handleBlock(time)
                }
            } else {
                if (left && mc.gameSettings.keyBindAttack.isKeyDown && !mc.gameSettings.keyBindUseItem.isKeyDown && shouldAutoClick && time - leftLastSwing >= leftDelay) {
                    handleLeftClick(time, doubleClick)
                } else if (block && mc.gameSettings.keyBindAttack.isKeyDown && !mc.gameSettings.keyBindUseItem.isKeyDown && shouldAutoClick && shouldAutoRightClick() && mc.gameSettings.keyBindAttack.pressTime != 0) {
                    handleBlock(time)
                }
            }
        }
    }

    @EventTarget
    fun onTick(event: UpdateEvent) {
        mc.thePlayer?.let { thePlayer ->
            shouldJitter = !mc.objectMouseOver.typeOfHit.isBlock && (thePlayer.isSwingInProgress || mc.gameSettings.keyBindAttack.pressTime != 0)

            if (jitter && ((left && shouldAutoClick && shouldJitter) || (right && !mc.thePlayer.isUsingItem && mc.gameSettings.keyBindUseItem.isKeyDown))) {
                if (nextBoolean()) thePlayer.fixedSensitivityYaw += nextFloat(-1F, 1F)
                if (nextBoolean()) thePlayer.fixedSensitivityPitch += nextFloat(-1F, 1F)
            }
        }
    }

    private fun getNearestEntityInRange(): Entity? {
        val player = mc.thePlayer ?: return null

        return mc.theWorld?.loadedEntityList?.filter { isSelected(it, true) }
            ?.filter { player.getDistanceToEntityBox(it) <= range }
            ?.minByOrNull { player.getDistanceToEntityBox(it) }
    }

    private fun shouldAutoRightClick() = mc.thePlayer.heldItem?.itemUseAction in arrayOf(EnumAction.BLOCK)

    private fun handleLeftClick(time: Long, doubleClick: Int) {
        repeat(1 + doubleClick) {
            KeyBinding.onTick(mc.gameSettings.keyBindAttack.keyCode)

            leftLastSwing = time
            leftDelay = randomClickDelay(minCPS, maxCPS)
        }
    }

    private fun handleRightClick(time: Long, doubleClick: Int) {
        repeat(1 + doubleClick) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)

            rightLastSwing = time
            rightDelay = randomClickDelay(minCPS, maxCPS)
        }
    }

    private fun handleBlock(time: Long) {
        if (time - lastBlocking >= blockDelay) {
            KeyBinding.onTick(mc.gameSettings.keyBindUseItem.keyCode)

            lastBlocking = time
        }
    }
}
