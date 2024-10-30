package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCrystal
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFDP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawJello
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawLies
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawLiesNew
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInSine
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutSine
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutSine
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInQuad
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutQuad
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutQuad
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInCubic
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutCubic
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutCubic
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInQuart
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutQuart
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutQuart
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInQuint
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutQuint
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutQuint
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInExpo
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutExpo
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutExpo
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInCirc
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutCirc
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutCirc
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInBack
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutBack
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutBack
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInElastic
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutElastic
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutElastic
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInBounce
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeOutBounce
import net.ccbluex.liquidbounce.utils.render.RenderUtils.easeInOutBounce
import net.ccbluex.liquidbounce.utils.render.RenderUtils.linear
import net.ccbluex.liquidbounce.utils.render.animation.AnimationUtil.easeInOutQuadX
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.IntegerValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.potion.Potion
import java.awt.Color
import java.util.*

object TargetESP : Module("TargetESP", Category.VISUAL, hideModule = false, subjective = true) {

    init {
        state = true
    }

    // Mark - TargetESP
    private val markValue by ListValue("MarkMode", arrayOf("None", "Jello", "Lies", "New", "New2", "FDP", "Sims", "RoundBox",), "LiesNew")

    override val tag
        get() = markValue

    private val onlykillauratargetesp by BoolValue("Only-KillAura", false)

    val jelloRedValue by FloatValue("Jello-Red", 1F, 0F..1F) { markValue in arrayOf("Jello") }
    val jelloGreenValue by FloatValue("Jello-Green", 1F, 0F..1F) { markValue in arrayOf("Jello") }
    val jelloBlueValue by FloatValue("Jello-Blue", 1F, 0F..1F) { markValue in arrayOf("Jello") }
    val liescolorRed by FloatValue("Lies-Red", 1F, 0F..1F) { markValue in arrayOf("Lies", "New", ) }
    val liescolorGreen by FloatValue("Lies-Green", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") }
    val liescolorBlue by FloatValue("Lies-Blue", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") }
    val liesalpha by FloatValue("Lies-Alpha", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") }
    val liescolorRedtwo by FloatValue("Lies-Red2", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") }
    val liescolorGreentwo by FloatValue("Lies-Green2", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") }
    val liescolorBluetwo by FloatValue("Lies-Blue2", 1F, 0F..1F) { markValue in arrayOf("Lies", "New") }
    val liesalphatwo by FloatValue("Lies-Alpha2", 0F, 0F..1F) { markValue in arrayOf("Lies", "New") }
    private val speedlies by FloatValue("Lies-Speed", 1.0F, 0.5F..3.0F) { markValue in arrayOf("Lies", "New") }
    private val lenghtlies by FloatValue("Lies-Length", 1.0F, 0F..1F) { markValue in arrayOf("Lies", "New") }
    private val radiuslies by FloatValue("Lies-Radius", 0.5F, 0.0F..3.0F) { markValue in arrayOf("Lies", "New") }
    val gradientlies by BoolValue("Lies-Gradient", false) { markValue in arrayOf("New") }
    private val speedcolorlies by IntegerValue("Lies-Color-Value", 9, 1..9) { markValue in arrayOf("New") }
    val liescolorgix by BoolValue("Lies-Color-Fix", true) { markValue in arrayOf("New") }
    private val liesstepvalue by IntegerValue("Lies-Step-Value", 10, 10..180) { markValue in arrayOf("New") }
    val heihgtlies by BoolValue("Lies-Height-Fix-UseOnlyAnimationLinear", false) { markValue in arrayOf("Lies") }
    val mode by ListValue("AnimationType", arrayOf(
        "easeInSine", "easeOutSine", "easeInOutSine",
        "easeInQuad", "easeOutQuad", "easeInOutQuad",
        "easeInCubic", "easeOutCubic", "easeInOutCubic",
        "easeInQuart", "easeOutQuart", "easeInOutQuart",
        "easeInQuint", "easeOutQuint", "easeInOutQuint",
        "easeInExpo", "easeOutExpo", "easeInOutExpo",
        "easeInCirc", "easeOutCirc", "easeInOutCirc",
        "easeInBack", "easeOutBack", "easeInOutBack",
        "easeInElastic", "easeOutElastic", "easeInOutElastic",
        "easeInBounce", "easeOutBounce", "easeInOutBounce",
        "easeInOutQuadX", "linear"
    ), "easeInOutQuadX") { markValue in arrayOf("Lies", "Jello", "FDP") }

    private val hurt by BoolValue("Mark-HurtTime", true) { markValue in arrayOf("Sims", "RoundBox") }
    private val boxOutline by BoolValue("Mark-Outline", true, subjective = true) { markValue == "RoundBox" }

    // fake sharp
    private val fakeSharp by BoolValue("FakeSharp", true, subjective = true)

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
            "roundbox" -> drawEntityBox(
                entityLivingBase,
                if (hurt && entityLivingBase.hurtTime > 3)
                    Color(37, 126, 255, 70)
                else
                    Color(255, 0, 0, 70),
                boxOutline
            )

            "sims" -> drawCrystal(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime <= 0)) Color(80, 255, 80, 200).rgb else Color(
                    255,
                    0,
                    0,
                    200
                ).rgb,
                event
            )

            "jello" -> drawJello(
                entityLivingBase
            )

            "fdp" -> drawFDP(
                entityLivingBase,
                event
            )

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
                radiuslies,
                speedcolorlies,
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
        if (killaura.target?.hurtTime == 10) {
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

    fun doAnimation(drawPercent: Double): Double {
        return when (mode) {
            "easeInSine" -> easeInSine(drawPercent)
            "easeOutSine" -> easeOutSine(drawPercent)
            "easeInOutSine" -> easeInOutSine(drawPercent)
            "easeInQuad" -> easeInQuad(drawPercent)
            "easeOutQuad" -> easeOutQuad(drawPercent)
            "easeInOutQuad" -> easeInOutQuad(drawPercent)
            "easeInCubic" -> easeInCubic(drawPercent)
            "easeOutCubic" -> easeOutCubic(drawPercent)
            "easeInOutCubic" -> easeInOutCubic(drawPercent)
            "easeInQuart" -> easeInQuart(drawPercent)
            "easeOutQuart" -> easeOutQuart(drawPercent)
            "easeInOutQuart" -> easeInOutQuart(drawPercent)
            "easeInQuint" -> easeInQuint(drawPercent)
            "easeOutQuint" -> easeOutQuint(drawPercent)
            "easeInOutQuint" -> easeInOutQuint(drawPercent)
            "easeInExpo" -> easeInExpo(drawPercent)
            "easeOutExpo" -> easeOutExpo(drawPercent)
            "easeInOutExpo" -> easeInOutExpo(drawPercent)
            "easeInCirc" -> easeInCirc(drawPercent)
            "easeOutCirc" -> easeOutCirc(drawPercent)
            "easeInOutCirc" -> easeInOutCirc(drawPercent)
            "easeInBack" -> easeInBack(drawPercent)
            "easeOutBack" -> easeOutBack(drawPercent)
            "easeInOutBack" -> easeInOutBack(drawPercent)
            "easeInElastic" -> easeInElastic(drawPercent)
            "easeOutElastic" -> easeOutElastic(drawPercent)
            "easeInOutElastic" -> easeInOutElastic(drawPercent)
            "easeInBounce" -> easeInBounce(drawPercent)
            "easeOutBounce" -> easeOutBounce(drawPercent)
            "easeInOutBounce" -> easeInOutBounce(drawPercent)
            "easeInOutQuadX" -> easeInOutQuadX(drawPercent)
            "linear" -> linear(drawPercent)
            else -> easeInOutQuadX(drawPercent)
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
