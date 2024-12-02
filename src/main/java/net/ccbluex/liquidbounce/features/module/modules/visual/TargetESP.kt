package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawLies
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawLiesNew
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.potion.Potion
import java.util.*

object TargetESP : Module("TargetESP", Category.VISUAL, hideModule = false, subjective = true) {

    init {
        state = true
    }

    // Mark - TargetESP
    private val markValue by ListValue("TargetMode", arrayOf("None", "Lies", "New"), "New")

    override val tag
        get() = markValue

    private val onlykillauratargetesp by BoolValue("Only-KillAura", false)

    val penislistcolor by ListValue("ColorType", arrayOf("Gradient", "Custom"), "Gradient") { markValue in arrayOf("New") }
    val hitcolorvalue by BoolValue("HitColorChange", false) { penislistcolor in arrayOf("Custom") }
    val liesredhit by FloatValue("HitRed", 1F, 0F..1F) { markValue in arrayOf("New") && penislistcolor in arrayOf("Custom") && hitcolorvalue }
    val liesgreenhit by FloatValue("HitGreen", 0F, 0F..1F) { markValue in arrayOf("New") && penislistcolor in arrayOf("Custom") && hitcolorvalue }
    val liesbluehit by FloatValue("HitBlue", 0F, 0F..1F) { markValue in arrayOf("New") && penislistcolor in arrayOf("Custom") && hitcolorvalue }
    val liescolorRed by FloatValue("Red", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") && penislistcolor in arrayOf("Gradient", "Custom") }
    val liescolorGreen by FloatValue("Green", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") && penislistcolor in arrayOf("Gradient", "Custom") }
    val liescolorBlue by FloatValue("Blue", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") && penislistcolor in arrayOf("Gradient", "Custom") }
    val liesalpha by FloatValue("Alpha", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") && penislistcolor in arrayOf("Gradient", "Custom") }
    val liescolorRedtwo by FloatValue("Red2", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") && penislistcolor in arrayOf("Gradient") }
    val liescolorGreentwo by FloatValue("Green2", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") && penislistcolor in arrayOf("Gradient") }
    val liescolorBluetwo by FloatValue("Blue2", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") && penislistcolor in arrayOf("Gradient") }
    val liesalphatwo by FloatValue("Alpha2", 0F, 0F..1F) { markValue in arrayOf("Lies", "New") && penislistcolor in arrayOf("Gradient", "Custom") }
    private val speedlies by FloatValue("Speed", 1.0F, 0.5F..3.0F) { markValue in arrayOf("Lies", "New") }
    private val lenghtlies by FloatValue("Length", 1.0F, 0F..1F) { markValue in arrayOf("Lies", "New") }
    private val radiuslies by FloatValue("Radius", 0.5F, 0.0F..3.0F) { markValue in arrayOf("Lies") }
    val speedcolorlies by IntegerValue("Color-Value", 9, 1..9) { markValue in arrayOf("New") && penislistcolor in arrayOf("Gradient") }
    private val liesstepvalue by IntegerValue("Step-Value", 10, 10..180) { markValue in arrayOf("New") }
    val heihgtlies by BoolValue("HeightFixUseOnlyAnimationLinear", false) { markValue in arrayOf("Lies") }

    // fake sharp
    private val fakeSharp by BoolValue("AlwaysSharp", true, subjective = true)

    // Sound
    private val sound by ListValue("Sound", arrayOf("None", "Hit", "Explode", "Orb", "Pop", "Splash", "Lightning"), "Pop")

    private val volume by FloatValue("Volume", 1f, 0.1f.. 5f) { sound != "None" }
    private val pitch by FloatValue("Pitch", 1f, 0.1f..5f) { sound != "None" }

    // variables
    private val targetList = HashMap<EntityLivingBase, Long>()
    private val combat = CombatManager
    private val killaura = KillAura
    var random = Random()
    var start = 0.0

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        targetList.clear()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val renderManager = mc.renderManager
        val entityLivingBase = if (onlykillauratargetesp) {
            killaura.target ?: return
        } else {
            combat.target ?: return
        }
        (entityLivingBase.lastTickPosX + (entityLivingBase.posX - entityLivingBase.lastTickPosX) * mc.timer.renderPartialTicks
                - renderManager.renderPosX)
        (entityLivingBase.lastTickPosY + (entityLivingBase.posY - entityLivingBase.lastTickPosY) * mc.timer.renderPartialTicks
                - renderManager.renderPosY)
        (entityLivingBase.lastTickPosZ + (entityLivingBase.posZ - entityLivingBase.lastTickPosZ) * mc.timer.renderPartialTicks
                - renderManager.renderPosZ)
        when (markValue.lowercase()) {
            "lies" -> drawLies(
                entityLivingBase,
                event,
                speedlies.toDouble(),
                lenghtlies.toDouble(),
                radiuslies,
            )
            "new" -> drawLiesNew(
                entityLivingBase,
                event,
                speedlies.toDouble(),
                lenghtlies.toDouble(),
                liesstepvalue,
            )
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity as? EntityLivingBase ?: return
        attackEntity(target)
    }


    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (combat.target?.hurtTime == 10) {
            doSound()
        }
    }

    @EventTarget
    private fun attackEntity(entity: EntityLivingBase) {
        val thePlayer = mc.thePlayer

        // Extra critical effects
        repeat(3) {
            // Critical Effect
            if (thePlayer.fallDistance > 0F && !thePlayer.onGround && !thePlayer.isOnLadder && !thePlayer.isInWater && !thePlayer.isPotionActive(
                    Potion.blindness
                ) && thePlayer.ridingEntity == null || handleEvents() && Criticals.msTimer.hasTimePassed(
                    Criticals.delay
                ) && !thePlayer.isInWater && !thePlayer.isInLava && !thePlayer.isInWeb) {
                thePlayer.onCriticalHit(entity)
            }

            // Enchant Effect
            if (EnchantmentHelper.getModifierForCreature(thePlayer.heldItem,
                    entity.creatureAttribute
                ) > 0f || fakeSharp
            ) {
                thePlayer.onEnchantmentCritical(entity)
            }
        }
    }

    private fun doSound() {
        val player = mc.thePlayer

        when (sound) {
            "Hit" -> player.playSound("random.bowhit", volume, pitch)
            "Orb" -> player.playSound("random.orb", volume, pitch)
            "Pop" -> player.playSound("random.pop", volume, pitch)
            "Splash" -> player.playSound("random.splash", volume, pitch)
            "Lightning" -> player.playSound("ambient.weather.thunder", volume, pitch)
            "Explode" -> player.playSound("random.explode", volume, pitch)
        }
    }
}
