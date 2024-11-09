package net.ccbluex.liquidbounce.features.module.modules.visual

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.utils.EntityUtils.isLookingOnEntities
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.render.ColorUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.client.gui.Gui
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import org.lwjgl.opengl.GL11.*
import java.util.*
import kotlin.math.pow

object NameTags : Module("NameTags", Category.VISUAL, hideModule = false) {

    private val typeValue = ListValue("Mode", arrayOf("2DTag"), "2DTag")
    private val fontShadow by BoolValue("Shadow", true)
    private val background by BoolValue("Background", true)
    private val bot by BoolValue("Bots", true)
    private val clearNames by BoolValue("ClearNames", false)
    private val maxRenderDistance by object : IntegerValue("MaxRenderDistance", 100, 1..200) {
        override fun onUpdate(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
    }

    private val onLook by BoolValue("OnLook", false)
    private val maxAngleDifference by FloatValue("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by BoolValue("ThruBlocks", true)

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return

        glPushAttrib(GL_ENABLE_BIT)
        glPushMatrix()

        // Disable lighting and depth test
        glDisable(GL_LIGHTING)
        glDisable(GL_DEPTH_TEST)

        glEnable(GL_LINE_SMOOTH)

        // Enable blending
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        for (entity in mc.theWorld.loadedEntityList) {
            if (entity !is EntityLivingBase) continue
            if (!isSelected(entity, false)) continue
            if (onLook && !isLookingOnEntities(entity, maxAngleDifference.toDouble())) continue
            if (!thruBlocks && !RotationUtils.isVisible(Vec3(entity.posX, entity.posY, entity.posZ))) continue

            val name = entity.displayName.unformattedText ?: continue

            val distanceSquared = mc.thePlayer.getDistanceSqToEntity(entity)

            if (distanceSquared <= maxRenderDistanceSq) {
                when (typeValue.get().lowercase(Locale.getDefault())) {
                    "2dtag" -> renderNameTag2D(entity, if (clearNames) ColorUtils.stripColor(name) else name)
                }
            }
        }

        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)

        glPopMatrix()
        glPopAttrib()

        // Reset color
        glColor4f(1F, 1F, 1F, 1F)
    }

    private fun renderNameTag2D(entity: EntityLivingBase, name: String) {
        var tag = name
        val fontRenderer = mc.fontRendererObj
        var scale = (mc.thePlayer.getDistanceToEntity(entity) / 2.5f).coerceAtLeast(4.0f)
        scale /= 200f
        tag = entity.displayName.formattedText
        glPushMatrix()
        glTranslatef(
            (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX).toFloat(),
            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + entity.eyeHeight + 0.6).toFloat(),
            (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ).toFloat()
        )

        var lastUsed = 0

        val phraseListowner = arrayOf(
            "SweetAsssss",
            "Aliderequod",
            "Deathlksr",
            "DeathlksrF",
            "Deathlksrl",
            "SweetAsssss",
            "SweetAssssss",
            "SweetAsssssss",
            "SweetAssssssss",
            "SweetAsssssssss",
            "SweetAssssssssss",
            "MKY_PEDURFILE",
            "MKY_PEDOFILE",
            "Aliderequod",
            "zasolinne",
            "AsarLong",
            "TheDayUranus93",
            "TheDayUranus94",
            "VerLouF",
            "SSDxsLuFioiD",
            "waltonxcostxxx",
            "WaltonxNeverxxx",
            "KillAuraxIHVILIX",
            )
        val phraseListUser = arrayOf(
            "TestRanpo",
            "EdogawaRanpo",
            "Kikatilo_01",
            "Kikatilo_02",
            "Kikatilo_1",
            "Kikatilo_2",
            "Kikatilo_3",
            "Kikatilo_4",
            "Kikatilo_5",
            "Kikatilo_6",
            "Kikatilo_7",
            "Kikatilo_8",
            "Kikatilo_9",
            "Kikatilo_10",
            "Kikatilo_11",
            "Kikatilo_12",
            "Kikatilo_13",
            "Kikatilo_14",
            "Kikatilo_15",
            "Kikatilo_16",
            "Kikatilo_17",
            "Kikatilo_18",
            "Kikatilo_19",
            "Kikatilo_20",
            "Kikatilo_21",
            "Kikatilo_22",
            "Kikatilo_23",
            "Kikatilo_24",
            "Kikatilo_25",
            "Kikatilo_26",
            "Kikatilo_27",
            "Kikatilo_28",
            "Kikatilo_29",
            "Kikatilo_30",
            "bastard_daaoo",
            "SJ_Mealisene",
            "Wakandr",
            "ЧухЧухЧухЛиза",
            "SJ_daaoo",
            )
        fun randomPhraseowner(): String {
            val rand = Random()

            var randInt: Int
            randInt = rand.nextInt(phraseListowner.size)
            while (lastUsed == randInt) {
                randInt = rand.nextInt(phraseListowner.size)
            }

            lastUsed = randInt
            return phraseListowner[randInt]
        }

        fun randomPhraseUser(): String {
            val rand = Random()

            var randInt: Int
            randInt = rand.nextInt(phraseListUser.size)
            while (lastUsed == randInt) {
                randInt = rand.nextInt(phraseListUser.size)
            }

            lastUsed = randInt
            return phraseListUser[randInt]
        }

            var ownertext = ""
            if (entity is EntityPlayer) {
                val ownerlest: EntityPlayer = entity
                if (ownerlest.displayNameString == randomPhraseowner()) {
                    ownertext = "§5[Fuguri Owner] "
                }
            }
            var usertext = ""
            if (entity is EntityPlayer) {
                val userlest: EntityPlayer = entity
                if (userlest.displayNameString == randomPhraseUser()) {
                    usertext = "§5[Fuguri User] "
                }
            }

            var friendtext = "§2[Friend] "
            if (entity is EntityPlayer) {
                val entityPlayer: EntityPlayer = entity
                if (!entityPlayer.isClientFriend()) {
                    friendtext = ""
                }
            } else {
                friendtext = ""
            }

            glNormal3f(0.0f, 1.0f, 0.0f)
            glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
            glRotatef(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
            glScalef(-scale, -scale, scale)
            RenderUtils.setGLCap(GL_LIGHTING, false)
            RenderUtils.setGLCap(GL_DEPTH_TEST, false)
            RenderUtils.setGLCap(GL_BLEND, true)
            glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
            val text = usertext + ownertext + friendtext + tag
            val stringWidth = fontRenderer.getStringWidth(text) / 2
            if (background) {
                Gui.drawRect((-stringWidth - 1), -14, (stringWidth + 1), -4, Integer.MIN_VALUE)
            }
            fontRenderer.drawString(
                text,
                (-stringWidth).toFloat(),
                (fontRenderer.FONT_HEIGHT - 22).toFloat(),
                16777215,
                fontShadow
            )
            RenderUtils.revertAllCaps()
            glColor4f(1f, 1f, 1f, 1f)
            glPopMatrix()
        }

    fun shouldRenderNameTags(entity: Entity) =
        handleEvents() && entity is EntityLivingBase && (ESP.handleEvents() && ESP.renderNameTags || isSelected(
            entity,
            false
        ) && (bot || !isBot(entity)))
}