package net.ccbluex.liquidbounce.features.module.modules.visual

import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Render3DEvent
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.modules.client.AntiBot.isBot
import net.ccbluex.liquidbounce.features.module.modules.client.IRCModule
import net.ccbluex.liquidbounce.ui.font.Fonts
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.EntityUtils.isLookingOnEntities
import net.ccbluex.liquidbounce.utils.EntityUtils.isSelected
import net.ccbluex.liquidbounce.utils.RotationUtils
import net.ccbluex.liquidbounce.utils.extensions.isClientFriend
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.ccbluex.liquidbounce.value.*
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.Vec3
import okhttp3.OkHttpClient
import okhttp3.Request
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.io.IOException
import kotlin.math.pow

object NameTags : Module("NameTags", Category.VISUAL, hideModule = false) {

    private val fontShadow by BoolValue("FontShadow", true)
    private val shadow by BoolValue("Shadow", true)
    private val background by BoolValue("Background", true)
    private val red by IntegerValue("Red", 10, 0..255) { background }
    private val green by IntegerValue("Green", 10, 0..255) { background }
    private val blue by IntegerValue("Blue", 10, 0..255) { background }
    private val alpha by IntegerValue("Alpha", 100, 0..255) { background }
    private val radius by FloatValue("BorderRadius", 0f, 0f..5f) { background }

    private val bot by BoolValue("Bots", true)
    private val maxRenderDistance by object : IntegerValue("MaxRenderDistance", 200, 1..200) {
        override fun onUpdate(value: Int) {
            maxRenderDistanceSq = value.toDouble().pow(2.0)
        }
    }

    private var updateRepository by BoolValue("UpdateRepository", false)
    private val onLook by BoolValue("OnLook", false)
    private val maxAngleDifference by FloatValue("MaxAngleDifference", 90f, 5.0f..90f) { onLook }

    private val thruBlocks by BoolValue("ThruBlocks", true)

    private const val GITHUBRAWURL = "https://raw.githubusercontent.com/VerLouF/Lists-Furugi/refs/heads/main"

    lateinit var ownerList: List<String>
    lateinit var bonanList: List<String>
    lateinit var userList: List<String>

    private var maxRenderDistanceSq = 0.0
        set(value) {
            field = if (value <= 0.0) maxRenderDistance.toDouble().pow(2.0) else value
        }

    @EventTarget
    fun onRender3D(event: Render3DEvent) {
        if (mc.theWorld == null || mc.thePlayer == null) return

        if (updateRepository) {
            runBlocking{
                getListsFromGitHub()
            }

            updateRepository = false
        }

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

            val distanceSquared = mc.thePlayer.getDistanceSqToEntity(entity)

            if (distanceSquared <= maxRenderDistanceSq) {
               renderNameTag2D(entity)
            }
        }

        glDisable(GL_BLEND)
        glDisable(GL_LINE_SMOOTH)

        glPopMatrix()
        glPopAttrib()

        // Reset color
        glColor4f(1F, 1F, 1F, 1F)
    }

    suspend fun getListsFromGitHub() {
        ClientUtils.displayChatMessage("Connecting to GitHub!")
        withContext(Dispatchers.IO) {
            try {
                ownerList = getListFromGitHub("$GITHUBRAWURL/ownerList.txt")
                bonanList = getListFromGitHub("$GITHUBRAWURL/bonanList.txt")
                userList = getListFromGitHub("$GITHUBRAWURL/userList.txt")
                ClientUtils.displayChatMessage("Successful connect to GitHub!")
            } catch (e: IOException) {
                ClientUtils.displayChatMessage("Error connection to GitHud, Code: ${e.message}!")
                ownerList = emptyList()
                bonanList = emptyList()
                userList = emptyList()
            }
        }
    }

    private fun getListFromGitHub(url: String): List<String> {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Error: ${response.code}")
            }
            return response.body?.string()?.lines()?.map { it.trim().lowercase() } ?: emptyList()
        }
    }

    private fun renderNameTag2D(entity: EntityLivingBase) {
        val fontRenderer = mc.fontRendererObj
        var scale = (mc.thePlayer.getDistanceToEntity(entity) / 2.5f).coerceAtLeast(4.0f)
        scale /= 200f
        glPushMatrix()
        glTranslatef(
            (entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks - mc.renderManager.renderPosX).toFloat(),
            (entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks - mc.renderManager.renderPosY + entity.eyeHeight + 0.6).toFloat(),
            (entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks - mc.renderManager.renderPosZ).toFloat()
        )

        var ownerText = ""
        if (entity is EntityPlayer) {
            if (ownerList.contains(entity.displayNameString.lowercase())) {
                ownerText = "§4[Fuguri Owner] "
            }
        }

        var userText = ""
        if (entity is EntityPlayer) {
            if (userList.contains(entity.displayNameString.lowercase())) {
                userText = "§5[Fuguri User] "
            }
        }

        var penisBonan = ""
        if (entity is EntityPlayer) {
            if (bonanList.contains(entity.displayNameString.lowercase())) {
                penisBonan = "§6[Bonan Entwickler] "
            }
        }

        var friendText = "§2[Friend] "
        if (entity is EntityPlayer) {
            val entityPlayer: EntityPlayer = entity
            if (!entityPlayer.isClientFriend()) {
                friendText = ""
            }
        } else {
            friendText = ""
        }

        glNormal3f(0.0f, 1.0f, 0.0f)
        glRotatef(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
        glRotatef(mc.renderManager.playerViewX, 1.0f, 0.0f, 0.0f)
        glScalef(-scale, -scale, scale)
        RenderUtils.setGLCap(GL_LIGHTING, false)
        RenderUtils.setGLCap(GL_DEPTH_TEST, false)
        RenderUtils.setGLCap(GL_BLEND, true)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        val text = if (IRCModule.state) { userText + penisBonan + ownerText + friendText + entity.displayName.formattedText } else { friendText + entity.displayName.formattedText }
        val stringWidth = fontRenderer.getStringWidth(text) / 2
        val color = Color(red, green, blue, alpha).rgb
        if (background) {
            RenderUtils.drawRoundedRectInt((-stringWidth - 1), -14, (stringWidth + 1), -4, color, radius)
        }
        if (shadow) {
            RenderUtils.drawShadow((-stringWidth - 1f), -14f, (stringWidth * 2f) + 1f, 10f)
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