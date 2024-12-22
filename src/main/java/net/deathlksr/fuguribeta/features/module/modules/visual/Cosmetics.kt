package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Render3DEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.utils.RenderWings
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.util.ResourceLocation
import net.deathlksr.fuguribeta.utils.EntityUtils
import net.deathlksr.fuguribeta.utils.ClientThemesUtils
import net.deathlksr.fuguribeta.utils.render.ColorUtils
import net.deathlksr.fuguribeta.value.*
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin

import java.util.*

object Cosmetics : Module("Cosmetics", Category.VISUAL, hideModule = false) {
    private val wings by BoolValue("Wings", false)
    val wingStyle by ListValue("WingsMode", arrayOf("Dragon", "Simple"), "Dragon") { wings }
    val colorType by ListValue("ColorType", arrayOf("Custom", "Chroma", "None"), "Chroma") { wings }
    val customRed by IntegerValue("WingRed", 255, 0.. 255) { colorType == "Custom" && wings }
    val customGreen by IntegerValue("WingGreen", 255, 0.. 255) { colorType == "Custom" && wings  }
    val customBlue by IntegerValue("WingBlue", 255, 0.. 255) { colorType == "Custom" && wings  }
    private val onlyThirdPerson by BoolValue("OnlyThirdPerson", true) { wings }

    private val hatsBool by BoolValue("Hat", false)
    private val heightValue by FloatValue("Height", 0.3f, 0.1f.. 0.7f) { hatsBool }
    private val radiusValue by FloatValue("Radius", 0.7f, 0.3f.. 1.5f) { hatsBool }
    private val yPosValue by FloatValue("YPos", 0f, -1f.. 1f) { hatsBool }
    private val rotateSpeedValue by FloatValue("RotateSpeed", 2f, 0f.. 5f) { hatsBool }
    private val drawThePlayerValue by BoolValue("DrawThePlayer", true) { hatsBool }
    private val onlyThirdPersonValue by BoolValue("OnlyThirdPerson", true) { drawThePlayerValue }
    private val drawTargetsValue by BoolValue("DrawTargets", true) { hatsBool }
    private val colorMode by ListValue("ColorModeHat", arrayOf("Custom", "Theme", "Rainbow"), "Theme") { hatsBool }
    private val colorRedValue by IntegerValue("HatRed", 255, 0..255) { colorMode == "Custom" && hatsBool }
    private val colorGreenValue by IntegerValue("HatGreen", 179, 0..255) { colorMode == "Custom" && hatsBool }
    private val colorBlueValue by IntegerValue("HatBlue", 72, 0..255) { colorMode == "Custom" && hatsBool }
    private val rainbowSpeed by FloatValue("RainbowSpeed", 1.0f, 0.5f..5.0f) { colorMode == "Rainbow" && hatsBool }
    private val colorAlphaValue by IntegerValue("Alpha", 255, 0..255) { hatsBool }

    val customCape by BoolValue("Cape", false)
    val styleValue = ListValue(
        "CapeMode",
        arrayOf(
            "FuguriBeta", "Augustus", "AugustusAmethyst", "AugustusCandy", "AugustusClassic",
            "AugustusMagic", "AugustusMagma", "AugustusMango", "AugustusRose", "AugustusTitanium", "DiscordLoading", "ESound", "Pyatero4ka", "RickRoll"
        ),
        "FuguriBeta"
    ) { customCape }

    private val capeCache = hashMapOf<String, CapeStyle>()

    private val capeLocate = java.lang.String("fuguribeta/cape")

    fun getCapeLocation(value: String): ResourceLocation? {
        val upperValue = value.uppercase(Locale.getDefault())
        if (capeCache[upperValue] == null) {
            capeCache[upperValue] = CapeStyle.valueOf(upperValue)
        }
        return capeCache[upperValue]?.location
    }

    enum class CapeStyle(val location: ResourceLocation) {
        FUGURIBETA(ResourceLocation("$capeLocate/classic.png")),
        AUGUSTUS(ResourceLocation("$capeLocate/Augustus.png")),
        AUGUSTUSAMETHYST(ResourceLocation("$capeLocate/AugustusAmethyst.png")),
        AUGUSTUSCANDY(ResourceLocation("$capeLocate/AugustusCandy.png")),
        AUGUSTUSCLASSIC(ResourceLocation("$capeLocate/AugustusClassic.png")),
        AUGUSTUSMAGIC(ResourceLocation("$capeLocate/AugustusMagic.png")),
        AUGUSTUSMAGMA(ResourceLocation("$capeLocate/AugustusMagma.png")),
        AUGUSTUSMANGO(ResourceLocation("$capeLocate/AugustusMango.png")),
        AUGUSTUSROSE(ResourceLocation("$capeLocate/AugustusRose.png")),
        AUGUSTUSTITANIUM(ResourceLocation("$capeLocate/AugustusTitanium.png")),
        PYATERO4KA(ResourceLocation("$capeLocate/Pyatero4ka.png")),
        RICKROLL(ResourceLocation("$capeLocate/RickRoll.png")),
        ESOUND(ResourceLocation("$capeLocate/ESound.png")),
        DISCORDLOADING(ResourceLocation("$capeLocate/DiscordLoading.gif"));
    }

    @EventTarget
    fun onRenderPlayer(event: Render3DEvent) {
        if (onlyThirdPerson && mc.gameSettings.thirdPersonView == 0) return
        if (wings) {
            val renderWings = RenderWings()
            renderWings.renderWings(event.partialTicks)
        }
    }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (!hatsBool) return
        if (drawThePlayerValue && !(onlyThirdPersonValue && mc.gameSettings.thirdPersonView == 0)) {
            drawChinaHatFor(mc.thePlayer)
        }
        if (drawTargetsValue) {
            mc.theWorld.loadedEntityList.forEach {
                if (EntityUtils.isSelected(it, true)) {
                    drawChinaHatFor(it as EntityLivingBase)
                }
            }
        }
    }

    private fun drawChinaHatFor(entity: EntityLivingBase) {
        if (!hatsBool) return
        glPushMatrix()
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        glDisable(GL_CULL_FACE)

        val color = when (colorMode) {
            "Custom" -> Color(colorRedValue, colorGreenValue, colorBlueValue, colorAlphaValue)
            "Theme" -> ClientThemesUtils.getColorWithAlpha(1, colorAlphaValue)
            "Rainbow" -> ColorUtils.rainbow(rainbowSpeed)
            else -> Color(255, 255, 255, colorAlphaValue)
        }

        glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)

        glTranslated(
            entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX,
            entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + entity.height + yPosValue,
            entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ
        )
        glRotatef((entity.ticksExisted + mc.timer.renderPartialTicks) * rotateSpeedValue, 0f, 1f, 0f)

        glBegin(GL_TRIANGLE_FAN)
        glVertex3d(0.0, heightValue.toDouble(), 0.0)
        val radius = radiusValue.toDouble()
        for (i in 0..360 step 5) {
            glVertex3d(
                cos(i.toDouble() * Math.PI / 180.0) * radius,
                0.0,
                sin(i.toDouble() * Math.PI / 180.0) * radius
            )
        }
        glVertex3d(0.0, heightValue.toDouble(), 0.0)
        glEnd()

        glEnable(GL_CULL_FACE)
        GlStateManager.resetColor()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_BLEND)
        glPopMatrix()
    }
}