package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.combat.Criticals
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.handler.combat.CombatManager
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawCrystal
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBox
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawEntityBoxESP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawFDP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawJello
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawLies
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatform
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawPlatformESP
import net.ccbluex.liquidbounce.utils.render.RenderUtils.drawZavz
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
import net.minecraft.block.Block
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.init.Blocks
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import net.minecraft.potion.Potion
import net.minecraft.util.EnumParticleTypes
import java.awt.Color
import java.util.*

object TargetESP : Module("TargetESP", Category.VISUAL, hideModule = false, subjective = true) {

    init {
        state = true
    }

    // Mark - TargetESP
    private val markValue by ListValue("MarkMode", arrayOf("None", "Zavz", "Jello", "Lies", "FDP", "Sims", "Box", "RoundBox", "Head", "Mark"), "Lies")
    private val isMarkMode: Boolean
        get() = markValue != "None" && markValue != "Sims" && markValue != "FDP"  && markValue != "Lies" && markValue != "Jello"

    override val tag
        get() = markValue

    private val onlykillauratargetesp by BoolValue("Only-KillAura", false)
    val colorRedValue by IntegerValue("Mark-Red", 0, 0.. 255) { isMarkMode }
    val colorGreenValue by IntegerValue("Mark-Green", 160, 0..255) { isMarkMode }
    val colorBlueValue by IntegerValue("Mark-Blue", 255, 0.. 255) { isMarkMode }
    private val zavzdouble by BoolValue("Zavz-Dual", false) { markValue in arrayOf("Zavz") }

    val jelloRedValue by FloatValue("Jello-Red", 1F, 0F..1F) { markValue in arrayOf("Jello") }
    val jelloGreenValue by FloatValue("Jello-Green", 1F, 0F..1F) { markValue in arrayOf("Jello") }
    val jelloBlueValue by FloatValue("Jello-Blue", 1F, 0F..1F) { markValue in arrayOf("Jello") }
    val liescolorRed by FloatValue("Lies-Red", 1F, 0F..1F) { markValue in arrayOf("Lies") }
    val liescolorGreen by FloatValue("Lies-Green", 1F, 0F..1F) { markValue in arrayOf("Lies") }
    val liescolorBlue by FloatValue("Lies-Blue", 1F, 0F..1F) { markValue in arrayOf("Lies") }
    val liesalpha by FloatValue("Lies-Alpha", 1F, 0F..1F) { markValue in arrayOf("Lies") }
    val liescolorRedtwo by FloatValue("Lies-Red2", 1F, 0F..1F) { markValue in arrayOf("Lies") }
    val liescolorGreentwo by FloatValue("Lies-Green2", 1F, 0F..1F) { markValue in arrayOf("Lies") }
    val liescolorBluetwo by FloatValue("Lies-Blue2", 1F, 0F..1F) { markValue in arrayOf("Lies") }
    val liesalphatwo by FloatValue("Lies-Alpha2", 0F, 0F..1F) { markValue in arrayOf("Lies") }
    private val speedlies by FloatValue("Lies-Speed", 1.0F, 0.5F..3.0F) { markValue in arrayOf("Lies") }
    private val lenghtlies by FloatValue("Lies-Lenght", 1.0F, 0.5F..3.0F) { markValue in arrayOf("Lies") }
    private val radiuslies by FloatValue("Lies-Radius", 0.5F, 0.0F..3.0F) { markValue in arrayOf("Lies") }
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

    private val alphaValue by IntegerValue("Alpha", 255, 0..255) { isMarkMode && markValue == "Zavz"}

    val colorRedTwoValue by IntegerValue("Mark-Red 2", 0, 0.. 255) { isMarkMode && markValue == "Zavz" }
    val colorGreenTwoValue by IntegerValue("Mark-Green 2", 160, 0..255) { isMarkMode && markValue == "Zavz" }
    val colorBlueTwoValue by IntegerValue("Mark-Blue 2", 255, 0.. 255) { isMarkMode && markValue == "Zavz" }

    private val rainbow by BoolValue("Mark-RainBow", false) { isMarkMode }
    private val hurt by BoolValue("Mark-HurtTime", true) { isMarkMode }
    private val boxOutline by BoolValue("Mark-Outline", true, subjective = true) { isMarkMode && markValue == "RoundBox" }

    // fake sharp
    private val fakeSharp by BoolValue("FakeSharp", true, subjective = true)

    private val particle by ListValue("Particle",
        arrayOf("None", "Blood", "Lighting", "Fire", "Heart", "Water", "Smoke", "Magic", "Crits"), "Blood")

    private val amount by IntegerValue("ParticleAmount", 5, 1..20) { particle != "None" }

    // Sound
    private val sound by ListValue("Sound", arrayOf("None", "Hit", "Explode", "Orb", "Pop", "Splash", "Lightning"), "Pop")

    private val volume by FloatValue("Volume", 1f, 0.1f.. 5f) { sound != "None" }
    private val pitch by FloatValue("Pitch", 1f, 0.1f..5f) { sound != "None" }

    // variables
    private val targetList = HashMap<EntityLivingBase, Long>()
    private val combat = CombatManager
    private val killaura = KillAura
    var random = Random()
    const val DOUBLE_PI = Math.PI * 2
    var start = 0.0

    @EventTarget
    fun onWorld(event: WorldEvent?) {
        targetList.clear()
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        val color: Color = if (rainbow) ColorUtils.rainbow() else Color(
            colorRedValue,
            colorGreenValue,
            colorBlueValue,
            alphaValue
        )
        val renderManager = mc.renderManager
        val entityLivingBase = if (onlykillauratargetesp.takeIf { isActive } == true) {
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
            "box" -> drawEntityBoxESP(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime > 3)) Color(255, 50, 50, 75) else color
            )

            "roundbox" -> drawEntityBox(
                entityLivingBase,
                if (hurt && entityLivingBase.hurtTime > 3)
                    Color(37, 126, 255, 70)
                else
                    Color(255, 0, 0, 70),
                boxOutline
            )

            "head" -> drawPlatformESP(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime > 3)) Color(255, 50, 50, 75) else color
            )

            "mark" -> drawPlatform(
                entityLivingBase,
                if ((hurt && entityLivingBase.hurtTime > 3)) Color(37, 126, 255, 70) else color
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

            "zavz" -> drawZavz(
                entityLivingBase,
                event,
                dual = zavzdouble,
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
        }
    }

    @EventTarget
    fun onAttack(event: AttackEvent) {
        val target = event.targetEntity as? EntityLivingBase ?: return

        repeat(amount) {
            doEffect(target)
        }

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

    private fun doEffect(target: EntityLivingBase) {
        when (particle) {
            "Blood" -> spawnBloodParticle(EnumParticleTypes.BLOCK_CRACK, target)
            "Crits" -> spawnEffectParticle(EnumParticleTypes.CRIT, target)
            "Magic" -> spawnEffectParticle(EnumParticleTypes.CRIT_MAGIC, target)
            "Lighting" -> spawnLightning(target)
            "Smoke" -> spawnEffectParticle(EnumParticleTypes.SMOKE_NORMAL, target)
            "Water" -> spawnEffectParticle(EnumParticleTypes.WATER_DROP, target)
            "Heart" -> spawnEffectParticle(EnumParticleTypes.HEART, target)
            "Fire" -> spawnEffectParticle(EnumParticleTypes.LAVA, target)
        }
    }

    private fun spawnBloodParticle(particleType: EnumParticleTypes, target: EntityLivingBase) {
        mc.theWorld.spawnParticle(particleType,
            target.posX, target.posY + target.height - 0.75, target.posZ,
            0.0, 0.0, 0.0,
            Block.getStateId(Blocks.redstone_block.defaultState)
        )
    }

    private fun spawnEffectParticle(particleType: EnumParticleTypes, target: EntityLivingBase) {
        mc.effectRenderer.spawnEffectParticle(particleType.particleID,
            target.posX, target.posY, target.posZ,
            target.posX, target.posY, target.posZ
        )
    }

    private fun spawnLightning(target: EntityLivingBase) {
        mc.netHandler.handleSpawnGlobalEntity(S2CPacketSpawnGlobalEntity(
            EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ)
        ))
    }
}