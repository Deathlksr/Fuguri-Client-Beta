/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.visual

import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.event.UpdateEvent
import net.deathlksr.fuguribeta.event.WorldEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.utils.MovementUtils.strafe
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.FloatValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.client.C03PacketPlayer.C06PacketPlayerPosLook
import net.minecraft.network.play.server.S08PacketPlayerPosLook

object FreeCam : Module("FreeCam", Category.VISUAL, gameDetecting = false, hideModule = false) {

    private val speed by FloatValue("Speed", 0.8f, 0.1f..2f)
    private val fly by BoolValue("Fly", true)
    private val noClip by BoolValue("NoClip", true)
    private val motion by BoolValue("RecordMotion", true)
    private val c03Spoof by BoolValue("C03Spoof", false)

    private lateinit var fakePlayer: EntityOtherPlayerMP
    private var motionX = 0.0
    private var motionY = 0.0
    private var motionZ = 0.0
    private var packetCount = 0

    override fun onEnable() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        if (motion) {
            motionX = mc.thePlayer.motionX
            motionY = mc.thePlayer.motionY
            motionZ = mc.thePlayer.motionZ
        } else {
            motionX = 0.0
            motionY = 0.0
            motionZ = 0.0
        }

        packetCount = 0
        fakePlayer = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile)
        fakePlayer.clonePlayer(mc.thePlayer, true)
        fakePlayer.rotationYawHead = mc.thePlayer.rotationYawHead
        fakePlayer.absorptionAmount = mc.thePlayer.absorptionAmount
        fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer)
        mc.theWorld.addEntityToWorld(-1000, fakePlayer)
        if (noClip) {
            mc.thePlayer.noClip = true
        }
    }

    override fun onDisable() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return
        }

        mc.thePlayer.setPositionAndRotation(fakePlayer.posX, fakePlayer.posY, fakePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch)
        mc.theWorld.removeEntityFromWorld(fakePlayer.entityId)
        mc.thePlayer.motionX = motionX
        mc.thePlayer.motionY = motionY
        mc.thePlayer.motionZ = motionZ
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (noClip) {
            mc.thePlayer.noClip = true
        }

        mc.thePlayer.fallDistance = 0f

        if (fly) {
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0

            if (mc.gameSettings.keyBindJump.isKeyDown) {
                mc.thePlayer.motionY += speed
            }

            if (mc.gameSettings.keyBindSneak.isKeyDown) {
                mc.thePlayer.motionY -= speed
            }

            strafe(speed)
        }
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet

        if (c03Spoof) {
            if (packet is C03PacketPlayer && (packet.rotating || packet.isMoving)) {
                if (packetCount >= 20) {
                    packetCount = 0
                    sendPacket(C06PacketPlayerPosLook(fakePlayer.posX, fakePlayer.posY, fakePlayer.posZ, fakePlayer.rotationYaw, fakePlayer.rotationPitch, fakePlayer.onGround), false)
                } else {
                    packetCount++
                    sendPacket(C03PacketPlayer(fakePlayer.onGround), false)
                }
                event.cancelEvent()
            }
        } else if (packet is C03PacketPlayer) {
            event.cancelEvent()
        }

        if (packet is S08PacketPlayerPosLook) {
            fakePlayer.setPosition(packet.x, packet.y, packet.z)
            // when teleported, reset motion

            motionX = 0.0
            motionY = 0.0
            motionZ = 0.0

            // apply the flag to bypass some anticheats
            sendPacket(C06PacketPlayerPosLook(fakePlayer.posX, fakePlayer.posY, fakePlayer.posZ, fakePlayer.rotationYaw, fakePlayer.rotationPitch, fakePlayer.onGround), false)

            event.cancelEvent()
        }
    }

    @EventTarget
    fun onWorldChange(event: WorldEvent) {
        // Disable when world changed
        state = false
    }

}