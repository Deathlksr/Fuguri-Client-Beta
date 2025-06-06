/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.ui.client.hud.element.elements

import net.deathlksr.fuguribeta.FuguriBeta.moduleManager
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.modules.client.GameDetector
import net.deathlksr.fuguribeta.ui.client.hud.designer.GuiHudDesigner
import net.deathlksr.fuguribeta.ui.client.hud.element.Border
import net.deathlksr.fuguribeta.ui.client.hud.element.Element
import net.deathlksr.fuguribeta.ui.client.hud.element.ElementInfo
import net.deathlksr.fuguribeta.ui.client.hud.element.Side
import net.deathlksr.fuguribeta.ui.client.hud.element.Side.Horizontal
import net.deathlksr.fuguribeta.ui.client.hud.element.Side.Vertical
import net.deathlksr.fuguribeta.ui.font.AWTFontRenderer
import net.deathlksr.fuguribeta.ui.font.Fonts
import net.deathlksr.fuguribeta.utils.ClientThemesUtils.getColor
import net.deathlksr.fuguribeta.utils.render.AnimationUtils
import net.deathlksr.fuguribeta.utils.render.ColorUtils.fade
import net.deathlksr.fuguribeta.utils.render.RenderUtils.deltaTime
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawArrayRect
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawRoundedRect
import net.deathlksr.fuguribeta.utils.render.animation.AnimationUtil
import net.deathlksr.fuguribeta.utils.render.shader.shaders.GradientFontShader
import net.deathlksr.fuguribeta.utils.render.shader.shaders.GradientShader
import net.deathlksr.fuguribeta.utils.render.shader.shaders.RainbowFontShader
import net.deathlksr.fuguribeta.utils.render.shader.shaders.RainbowShader
import net.deathlksr.fuguribeta.value.*
import org.lwjgl.opengl.GL11.glColor4f
import java.awt.Color

/**
 * CustomHUD Arraylist element
 *
 * Shows a list of enabled modules
 */
@ElementInfo(name = "Arraylist", single = true)
class Arraylist(
    x: Double = 1.0, y: Double = 2.0, scale: Float = 1F,
    side: Side = Side(Horizontal.RIGHT, Vertical.UP)
) : Element(x, y, scale, side) {

    private val textColorMode by ListValue("Text-Color", arrayOf("Custom", "Fade", "Random", "Rainbow", "Gradient", "Theme"), "Fade")
    private val textRed by IntegerValue("Text-R", 255, 0..255) { textColorMode in listOf("Custom", "Fade") }
    private val textGreen by IntegerValue("Text-G", 255, 0..255) { textColorMode in listOf("Custom", "Fade") }
    private val textBlue by IntegerValue("Text-B", 255, 0..255) { textColorMode in listOf("Custom", "Fade") }
    private val textAlpha by IntegerValue("Alpha", 255, 0..255) { textColorMode in listOf("Custom", "Fade") }

    private val gradientTextSpeed by FloatValue("Text-Gradient-Speed", 1f, 0.5f..10f) { textColorMode == "Gradient" }

    // TODO: Make Color picker to fix this mess :/
    private val gradientTextRed1 by FloatValue("Text-Gradient-R1", 255f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextGreen1 by FloatValue("Text-Gradient-G1", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextBlue1 by FloatValue("Text-Gradient-B1", 0f, 0f..255f) { textColorMode == "Gradient" }

    private val gradientTextRed2 by FloatValue("Text-Gradient-R2", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextGreen2 by FloatValue("Text-Gradient-G2", 255f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextBlue2 by FloatValue("Text-Gradient-B2", 0f, 0f..255f) { textColorMode == "Gradient" }

    private val gradientTextRed3 by FloatValue("Text-Gradient-R3", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextGreen3 by FloatValue("Text-Gradient-G3", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextBlue3 by FloatValue("Text-Gradient-B3", 255f, 0f..255f) { textColorMode == "Gradient" }

    private val gradientTextRed4 by FloatValue("Text-Gradient-R4", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextGreen4 by FloatValue("Text-Gradient-G4", 0f, 0f..255f) { textColorMode == "Gradient" }
    private val gradientTextBlue4 by FloatValue("Text-Gradient-B4", 0f, 0f..255f) { textColorMode == "Gradient" }

    private val rectMode by ListValue("Rect", arrayOf("None", "Left", "Right", "Outline", "Special", "Top"), "Right")
    private val roundedRectRadius by FloatValue("RoundedRect-Radius", 2F, 0F..2F)
    private val rectColorMode by ListValue(
        "Rect-Color",
        arrayOf("Custom", "Fade", "Random", "Rainbow", "Gradient", "Theme"),
        "Fade"
    ) { rectMode != "None" }
    private val isCustomRectSupported = { rectMode != "None" && rectColorMode in listOf("Custom", "Fade") }
    private val rectRed by IntegerValue("Rect-R", 255, 0..255, isSupported = isCustomRectSupported)
    private val rectGreen by IntegerValue("Rect-G", 255, 0..255, isSupported = isCustomRectSupported)
    private val rectBlue by IntegerValue("Rect-B", 255, 0..255, isSupported = isCustomRectSupported)
    private val rectAlpha by IntegerValue("Rect-Alpha", 150, 0..255, isSupported = isCustomRectSupported)

    private val gradientRectSpeed by FloatValue("Rect-Gradient-Speed", 1f, 0.5f..10f) { rectColorMode == "Gradient" }

    // TODO: Make Color picker to fix this mess :/
    private val gradientRectRed1 by FloatValue("Rect-Gradient-R1", 255f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectGreen1 by FloatValue("Rect-Gradient-G1", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectBlue1 by FloatValue("Rect-Gradient-B1", 0f, 0f..255f) { rectColorMode == "Gradient" }

    private val gradientRectRed2 by FloatValue("Rect-Gradient-R2", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectGreen2 by FloatValue("Rect-Gradient-G2", 255f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectBlue2 by FloatValue("Rect-Gradient-B2", 0f, 0f..255f) { rectColorMode == "Gradient" }

    private val gradientRectRed3 by FloatValue("Rect-Gradient-R3", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectGreen3 by FloatValue("Rect-Gradient-G3", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectBlue3 by FloatValue("Rect-Gradient-B3", 255f, 0f..255f) { rectColorMode == "Gradient" }

    private val gradientRectRed4 by FloatValue("Rect-Gradient-R4", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectGreen4 by FloatValue("Rect-Gradient-G4", 0f, 0f..255f) { rectColorMode == "Gradient" }
    private val gradientRectBlue4 by FloatValue("Rect-Gradient-B4", 0f, 0f..255f) { rectColorMode == "Gradient" }

    private val roundedBackgroundRadius by FloatValue("RoundedBackGround-Radius", 2F, 0F..5F)
    private val backgroundMode by ListValue("Background-Color", arrayOf("Custom", "Fade", "Random", "Rainbow", "Gradient", "Theme"), "Custom")
    private val backgroundRed by IntegerValue("Background-R", 0, 0..255) { backgroundMode in listOf("Custom", "Fade") }
    private val backgroundGreen by IntegerValue("Background-G", 0, 0..255) { backgroundMode in listOf("Custom", "Fade") }
    private val backgroundBlue by IntegerValue("Background-B", 0, 0..255) { backgroundMode in listOf("Custom", "Fade") }
    private val backgroundAlpha by IntegerValue("Background-Alpha", 140, 0..255) { backgroundMode in listOf("Custom", "Fade") }

    private val gradientBackgroundSpeed by FloatValue("Background-Gradient-Speed", 1f, 0.5f..10f) { backgroundMode == "Gradient" }

    // TODO: Make Color picker to fix this mess :/
    private val gradientBackgroundRed1 by FloatValue("Background-Gradient-R1", 255f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundGreen1 by FloatValue("Background-Gradient-G1", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundBlue1 by FloatValue("Background-Gradient-B1", 0f, 0f..255f) { backgroundMode == "Gradient" }

    private val gradientBackgroundRed2 by FloatValue("Background-Gradient-R2", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundGreen2 by FloatValue("Background-Gradient-G2", 255f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundBlue2 by FloatValue("Background-Gradient-B2", 0f, 0f..255f) { backgroundMode == "Gradient" }

    private val gradientBackgroundRed3 by FloatValue("Background-Gradient-R3", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundGreen3 by FloatValue("Background-Gradient-G3", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundBlue3 by FloatValue("Background-Gradient-B3", 255f, 0f..255f) { backgroundMode == "Gradient" }

    private val gradientBackgroundRed4 by FloatValue("Background-Gradient-R4", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundGreen4 by FloatValue("Background-Gradient-G4", 0f, 0f..255f) { backgroundMode == "Gradient" }
    private val gradientBackgroundBlue4 by FloatValue("Background-Gradient-B4", 0f, 0f..255f) { backgroundMode == "Gradient" }

    private fun isColorModeUsed(value: String) = textColorMode == value || rectMode == value || backgroundMode == value

    private val saturation by FloatValue("Random-Saturation", 0.9f, 0f..1f) { isColorModeUsed("Random") }
    private val brightness by FloatValue("Random-Brightness", 1f, 0f..1f) { isColorModeUsed("Random") }
    private val rainbowX by FloatValue("Rainbow-X", -1000F, -2000F..2000F) { isColorModeUsed("Rainbow") }
    private val rainbowY by FloatValue("Rainbow-Y", -1000F, -2000F..2000F) { isColorModeUsed("Rainbow") }
    private val gradientX by FloatValue("Gradient-X", -1000F, -2000F..2000F) { isColorModeUsed("Gradient") }
    private val gradientY by FloatValue("Gradient-Y", -1000F, -2000F..2000F) { isColorModeUsed("Gradient") }
    private val fadeDistanceValue by IntegerValue("Fade-Distance", 50, 0.. 100) { isColorModeUsed("Fade") }

    private val tags by BoolValue("Tags", true)
    private val tagsStyle by object : ListValue("TagsStyle", arrayOf("[]", "()", "<>", "-", "|", "Space"), "Space") {
        override fun isSupported() = tags

        // onUpdate - updates tag onInit and onChanged
        override fun onUpdate(value: String) = updateTagDetails()
    }
    private val tagsCase by ListValue("TagsCase", arrayOf("Normal", "Uppercase", "Lowercase"), "Normal") { tags }
    private val tagsArrayColor by object : BoolValue("TagsArrayColor", false) {
        override fun isSupported() = tags
        override fun onUpdate(value: Boolean) = updateTagDetails()
    }

    private val font by FontValue("Font", Fonts.minecraftFont)
    private val textShadow by BoolValue("ShadowText", true)
    private val moduleCase by ListValue("ModuleCase", arrayOf("Normal", "Uppercase", "Lowercase"), "Normal")
    private val space by FloatValue("Space", 0F, 0F..5F)
    private val textHeight by FloatValue("TextHeight", 11F, 1F..20F)
    private val textY by FloatValue("TextY", 1.5F, 0F..20F)

    private val animation by ListValue("Animation", arrayOf("Slide", "Smooth"), "Smooth") { tags }
    private val animationSpeed by FloatValue("AnimationSpeed", 0.2F, 0.01F..1F) { animation == "Smooth" }

    companion object {
        val spacedModules by BoolValue("SpacedModules", false)
        val inactiveStyle by ListValue("InactiveModulesStyle", arrayOf("Normal", "Color", "Hide"), "Color")
        { GameDetector.state }
    }

    private var x2 = 0
    private var y2 = 0F

    private lateinit var tagPrefix: String

    private lateinit var tagSuffix: String

    private var modules = emptyList<Module>()

    private val inactiveColor = Color(255, 255, 255, 100).rgb

    init {
        updateTagDetails()
    }

    fun updateTagDetails() {
        val pair: Pair<String, String> = when (tagsStyle) {
            "[]", "()", "<>" -> tagsStyle[0].toString() to tagsStyle[1].toString()
            "-", "|" -> tagsStyle[0] + " " to ""
            else -> "" to ""
        }

        tagPrefix = (if (tagsArrayColor) " " else " §7") + pair.first
        tagSuffix = pair.second
    }

    private fun getDisplayString(module: Module): String {
        val moduleName = when (moduleCase) {
            "Uppercase" -> module.getName().uppercase()
            "Lowercase" -> module.getName().lowercase()
            else -> module.getName()
        }

        var tag = module.tag ?: ""

        tag = when (tagsCase) {
            "Uppercase" -> tag.uppercase()
            "Lowercase" -> tag.lowercase()
            else -> tag
        }

        val moduleTag = if (tags && !module.tag.isNullOrEmpty()) tagPrefix + tag + tagSuffix else ""

        return moduleName + moduleTag
    }

    override fun drawElement(): Border? {
        AWTFontRenderer.assumeNonVolatile = true

        // Slide animation - update every render
        val delta = deltaTime

        for (module in moduleManager.modules) {
            val shouldShow = (module.inArray && module.state && (inactiveStyle != "Hide" || module.isActive))

            if (!shouldShow && module.slide <= 0f)
                continue

            val displayString = getDisplayString(module)

            val width = font.getStringWidth(displayString)

            when (animation) {
                "Slide" -> {
                    // If modules become inactive because they only work when in game, animate them as if they got disabled
                    module.slideStep += if (shouldShow) delta / 4F else -delta / 4F
                    if (shouldShow) {
                        if (module.slide < width) {
                            module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                        }
                    } else {
                        module.slide = AnimationUtils.easeOut(module.slideStep, width.toFloat()) * width
                    }

                    module.slide = module.slide.coerceIn(0F, width.toFloat())
                    module.slideStep = module.slideStep.coerceIn(0F, width.toFloat())
                }

                "Smooth" -> {
                    val target = if (shouldShow) width.toDouble() else -width / 5.0
                    module.slide =
                        AnimationUtil.base(module.slide.toDouble(), target, animationSpeed.toDouble()).toFloat()
                }
            }
        }
        // Draw arraylist
        val textCustomColor = Color(textRed, textGreen, textBlue, 1).rgb
        val rectCustomColor = Color(rectRed, rectGreen, rectBlue, rectAlpha).rgb
        val backgroundCustomColor = Color(backgroundRed, backgroundGreen, backgroundBlue, backgroundAlpha).rgb
        val textSpacer = textHeight + space

        val rainbowOffset = System.currentTimeMillis() % 10000 / 10000F
        val rainbowX = if (rainbowX == 0f) 0f else 1f / rainbowX
        val rainbowY = if (rainbowY == 0f) 0f else 1f / rainbowY

        val gradientOffset = System.currentTimeMillis() % 10000 / 10000F
        val gradientX = if (gradientX == 0f) 0f else 1f / gradientX
        val gradientY = if (gradientY == 0f) 0f else 1f / gradientY

        modules.forEachIndexed { index, module ->
            var yPos = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) *
                    if (side.vertical == Vertical.DOWN) index + 1 else index
            if (animation == "Smooth") {
                module.yAnim = AnimationUtil.base(module.yAnim.toDouble(), yPos.toDouble(), 0.2).toFloat()
                yPos = module.yAnim
            }
            val moduleColor = Color.getHSBColor(module.hue, saturation, brightness).rgb

            val markAsInactive = inactiveStyle == "Color" && !module.isActive

            val displayString = getDisplayString(module)

            when (side.horizontal) {
                Horizontal.RIGHT, Horizontal.MIDDLE -> {
                    val xPos = -module.slide - 2

                    GradientShader.begin(
                        !markAsInactive && backgroundMode == "Gradient",
                        gradientX,
                        gradientY,
                        floatArrayOf(
                            gradientBackgroundRed1 / 255f,
                            gradientBackgroundGreen1 / 255f,
                            gradientBackgroundBlue1 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientBackgroundRed2 / 255f,
                            gradientBackgroundGreen2 / 255f,
                            gradientBackgroundBlue2 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientBackgroundRed3 / 255f,
                            gradientBackgroundGreen3 / 255f,
                            gradientBackgroundBlue3 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientBackgroundRed4 / 255f,
                            gradientBackgroundGreen4 / 255f,
                            gradientBackgroundBlue4 / 255f,
                            1f
                        ),
                        gradientBackgroundSpeed,
                        gradientOffset
                    ).use {
                        RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                            val fadeColor = fade(
                                Color(
                                    backgroundRed,
                                    backgroundGreen,
                                    backgroundBlue,
                                    backgroundAlpha
                                ), index * fadeDistanceValue, 100
                            ).rgb

                            val themeColor = getColor(index).rgb

                            drawRoundedRect(
                                xPos - if (rectMode == "Right") 5 else 2,
                                yPos,
                                if (rectMode == "Right") -3F else 0F,
                                yPos + textHeight,
                                when (backgroundMode) {
                                    "Gradient" -> 0
                                    "Rainbow" -> 0
                                    "Random" -> moduleColor
                                    "Fade" -> fadeColor
                                    "Theme" -> themeColor
                                    else -> backgroundCustomColor
                                },
                                roundedBackgroundRadius
                            )
                        }
                    }

                    GradientFontShader.begin(
                        !markAsInactive && textColorMode == "Gradient",
                        gradientX,
                        gradientY,
                        floatArrayOf(
                            gradientTextRed1 / 255f,
                            gradientTextGreen1 / 255f,
                            gradientTextBlue1 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientTextRed2 / 255f,
                            gradientTextGreen2 / 255f,
                            gradientTextBlue2 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientTextRed3 / 255f,
                            gradientTextGreen3 / 255f,
                            gradientTextBlue3 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientTextRed4 / 255f,
                            gradientTextGreen4 / 255f,
                            gradientTextBlue4 / 255f,
                            1f
                        ),
                        gradientTextSpeed,
                        gradientOffset
                    ).use {
                        RainbowFontShader.begin(
                            !markAsInactive && textColorMode == "Rainbow",
                            rainbowX,
                            rainbowY,
                            rainbowOffset
                        ).use {
                            val fadeColor = fade(
                                Color(
                                    textRed,
                                    textGreen,
                                    textBlue,
                                    textAlpha
                                ), index * fadeDistanceValue, 100
                            ).rgb

                            val themeTextColor = getColor(index).rgb

                            font.drawString(
                                displayString, xPos - if (rectMode == "Right") 3 else 0, yPos + textY,
                                if (markAsInactive) inactiveColor
                                else when (textColorMode) {
                                    "Gradient" -> 0
                                    "Rainbow" -> 0
                                    "Random" -> moduleColor
                                    "Fade" -> fadeColor
                                    "Theme" -> themeTextColor
                                    else -> textCustomColor
                                },
                                textShadow
                            )
                        }
                    }

                    GradientShader.begin(
                        !markAsInactive && rectColorMode == "Gradient",
                        gradientX,
                        gradientY,
                        floatArrayOf(
                            gradientRectRed1 / 255f,
                            gradientRectGreen1 / 255f,
                            gradientRectBlue1 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientRectRed2 / 255f,
                            gradientRectGreen2 / 255f,
                            gradientRectBlue2 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientRectRed3 / 255f,
                            gradientRectGreen3 / 255f,
                            gradientRectBlue3 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientRectRed4 / 255f,
                            gradientRectGreen4 / 255f,
                            gradientRectBlue4 / 255f,
                            1f
                        ),
                        gradientRectSpeed,
                        gradientOffset
                    ).use {
                        if (rectMode != "None") {
                            RainbowShader.begin(
                                !markAsInactive && rectColorMode == "Rainbow",
                                rainbowX,
                                rainbowY,
                                rainbowOffset
                            ).use {
                                val fadeColor = fade(
                                    Color(
                                        rectRed,
                                        rectGreen,
                                        rectBlue,
                                        rectAlpha
                                    ), index * fadeDistanceValue, 100
                                ).rgb

                                val themeColor = getColor(index).rgb

                                val rectColor =
                                    if (markAsInactive) inactiveColor
                                    else when (rectColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        "Fade" -> fadeColor
                                        "Theme" -> themeColor
                                        else -> rectCustomColor
                                    }

                                when (rectMode) {
                                    "Left" -> drawRoundedRect(
                                        xPos - 5,
                                        yPos + 0.8F,
                                        xPos - 2,
                                        yPos + textHeight,
                                        rectColor,
                                        roundedRectRadius
                                    )

                                    "Right" -> drawRoundedRect(
                                        -3F,
                                        yPos,
                                        0F,
                                        yPos + textHeight,
                                        rectColor,
                                        roundedRectRadius
                                    )

                                    "Outline" -> drawArrayRect(
                                        -1F, yPos - 1F, 0F,
                                        yPos + textHeight, rectColor
                                    ).also {
                                        drawArrayRect(
                                            xPos - 3, yPos, xPos - 2, yPos + textHeight,
                                            rectColor
                                        )

                                        if (module != modules[0]) {
                                            val displayStrings = getDisplayString(modules[index - 1])
                                            drawArrayRect(
                                                xPos - 3 - (font.getStringWidth(displayStrings) - font.getStringWidth(displayString)),
                                                yPos,
                                                xPos - 2,
                                                yPos + 1,
                                                rectColor
                                            )
                                            if (module == modules[modules.size - 1]) {
                                                drawArrayRect(
                                                    xPos - 3, yPos + textHeight, 0.0F,
                                                    yPos + textHeight + 1,
                                                    rectColor
                                                )
                                            }
                                        } else {
                                            drawArrayRect(
                                                xPos - 3, yPos, 0F,
                                                yPos - 1,
                                                rectColor
                                            )
                                        }
                                    }

                                    "Special" -> {
                                        if (module == modules[0]) {
                                            drawRoundedRect(xPos - 2, yPos, 0F, yPos - 1, rectColor, roundedRectRadius)
                                        }
                                        if (module == modules[modules.size - 1]) {
                                            drawRoundedRect(xPos - 2, yPos + textHeight, 0F, yPos + textHeight + 1, rectColor, roundedRectRadius)
                                        }
                                    }

                                    "Top" -> {
                                        if (module == modules[0]) {
                                            drawRoundedRect(xPos - 2, yPos, 0F, yPos - 1, rectColor, roundedRectRadius)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Horizontal.LEFT -> {
                    val width = font.getStringWidth(displayString)
                    val xPos = -(width - module.slide) + if (rectMode == "Left") 5 else 2

                    GradientShader.begin(
                        !markAsInactive && backgroundMode == "Gradient",
                        gradientX,
                        gradientY,
                        floatArrayOf(
                            gradientBackgroundRed1 / 255f,
                            gradientBackgroundGreen1 / 255f,
                            gradientBackgroundBlue1 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientBackgroundRed2 / 255f,
                            gradientBackgroundGreen2 / 255f,
                            gradientBackgroundBlue2 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientBackgroundRed3 / 255f,
                            gradientBackgroundGreen3 / 255f,
                            gradientBackgroundBlue3 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientBackgroundRed4 / 255f,
                            gradientBackgroundGreen4 / 255f,
                            gradientBackgroundBlue4 / 255f,
                            1f
                        ),
                        gradientBackgroundSpeed,
                        gradientOffset
                    ).use {
                        RainbowShader.begin(backgroundMode == "Rainbow", rainbowX, rainbowY, rainbowOffset).use {
                            val fadeColor = fade(
                                Color(
                                    textRed,
                                    textGreen,
                                    textBlue,
                                    textAlpha
                                ), index * fadeDistanceValue, 100
                            ).rgb

                            val themeColor = getColor(index).rgb

                            drawRoundedRect(
                                0F, yPos, xPos + width + if (rectMode == "Right") 5 else 2, yPos + textHeight,
                                when (backgroundMode) {
                                    "Gradient" -> 0
                                    "Rainbow" -> 0
                                    "Random" -> moduleColor
                                    "Fade" -> fadeColor
                                    "Theme" -> themeColor
                                    else -> backgroundCustomColor
                                },
                                roundedBackgroundRadius
                            )
                        }
                    }

                    GradientFontShader.begin(
                        !markAsInactive && textColorMode == "Gradient",
                        gradientX,
                        gradientY,
                        floatArrayOf(
                            gradientTextRed1 / 255f,
                            gradientTextGreen1 / 255f,
                            gradientTextBlue1 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientTextRed2 / 255f,
                            gradientTextGreen2 / 255f,
                            gradientTextBlue2 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientTextRed3 / 255f,
                            gradientTextGreen3 / 255f,
                            gradientTextBlue3 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientTextRed4 / 255f,
                            gradientTextGreen4 / 255f,
                            gradientTextBlue4 / 255f,
                            1f
                        ),
                        gradientTextSpeed,
                        gradientOffset
                    ).use {
                        RainbowFontShader.begin(
                            !markAsInactive && textColorMode == "Rainbow",
                            rainbowX,
                            rainbowY,
                            rainbowOffset
                        ).use {
                            val fadeColor = fade(
                                Color(
                                    textRed,
                                    textGreen,
                                    textBlue,
                                    textAlpha
                                ), index * fadeDistanceValue, 100
                            ).rgb

                            val themeColor = getColor(index).rgb

                            font.drawString(
                                displayString, xPos, yPos + textY,
                                if (markAsInactive) inactiveColor
                                else when (textColorMode) {
                                    "Gradient" -> 0
                                    "Rainbow" -> 0
                                    "Random" -> moduleColor
                                    "Fade" -> fadeColor
                                    "Theme" -> themeColor
                                    else -> textCustomColor
                                },
                                textShadow
                            )
                        }
                    }

                    GradientShader.begin(
                        !markAsInactive && rectColorMode == "Gradient",
                        gradientX,
                        gradientY,
                        floatArrayOf(
                            gradientRectRed1 / 255f,
                            gradientRectGreen1 / 255f,
                            gradientRectBlue1 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientRectRed2 / 255f,
                            gradientRectGreen2 / 255f,
                            gradientRectBlue2 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientRectRed3 / 255f,
                            gradientRectGreen3 / 255f,
                            gradientRectBlue3 / 255f,
                            1f
                        ),
                        floatArrayOf(
                            gradientRectRed4 / 255f,
                            gradientRectGreen4 / 255f,
                            gradientRectBlue4 / 255f,
                            1f
                        ),
                        gradientRectSpeed,
                        gradientOffset
                    ).use {
                        RainbowShader.begin(
                            !markAsInactive && rectColorMode == "Rainbow",
                            rainbowX,
                            rainbowY,
                            rainbowOffset
                        ).use {
                            if (rectMode != "None") {
                                val fadeColor = fade(
                                    Color(
                                        rectRed,
                                        rectGreen,
                                        rectBlue,
                                        rectAlpha
                                    ), index * fadeDistanceValue, 100
                                ).rgb

                                val themeColor = getColor(index).rgb

                                val rectColor =
                                    if (markAsInactive) inactiveColor
                                    else when (rectColorMode) {
                                        "Gradient" -> 0
                                        "Rainbow" -> 0
                                        "Random" -> moduleColor
                                        "Fade" -> fadeColor
                                        "Theme" -> themeColor
                                        else -> rectCustomColor
                                    }

                                when (rectMode) {
                                    "Left" -> drawRoundedRect(
                                        0F,
                                        yPos - 1,
                                        3F,
                                        yPos + textHeight,
                                        rectColor,
                                        roundedRectRadius
                                    )

                                    "Right" -> drawRoundedRect(
                                        xPos + width + 2,
                                        yPos,
                                        xPos + width + 2 + 3,
                                        yPos + textHeight,
                                        rectColor,
                                        roundedRectRadius
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Draw border
        if (mc.currentScreen is GuiHudDesigner) {
            x2 = Int.MIN_VALUE

            if (modules.isEmpty()) {
                return if (side.horizontal == Horizontal.LEFT)
                    Border(0F, -1F, 20F, 20F)
                else
                    Border(0F, -1F, -20F, 20F)
            }

            for (module in modules) {
                when (side.horizontal) {
                    Horizontal.RIGHT, Horizontal.MIDDLE -> {
                        val xPos = -module.slide.toInt() - 2
                        if (x2 == Int.MIN_VALUE || xPos < x2) x2 = xPos
                    }

                    Horizontal.LEFT -> {
                        val xPos = module.slide.toInt() + 14
                        if (x2 == Int.MIN_VALUE || xPos > x2) x2 = xPos
                    }
                }
            }

            y2 = (if (side.vertical == Vertical.DOWN) -textSpacer else textSpacer) * modules.size

            return Border(0F, 0F, x2 - 7F, y2 - if (side.vertical == Vertical.DOWN) 1F else 0F)
        }

        AWTFontRenderer.assumeNonVolatile = false
        glColor4f(1f, 1f, 1f, 1f)
        return null
    }

    override fun updateElement() {
        modules = moduleManager.modules
            .filter { it.inArray && it.slide > 0 && !it.hideModuleValues.get() }
            .sortedBy { -font.getStringWidth(getDisplayString(it)) }
    }
}