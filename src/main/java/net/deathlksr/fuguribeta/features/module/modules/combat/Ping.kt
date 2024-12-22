package net.deathlksr.fuguribeta.features.module.modules.combat

import com.google.common.collect.Queues
import net.deathlksr.fuguribeta.event.*
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.ui.client.clickgui.ClickGui
import net.deathlksr.fuguribeta.utils.MovementUtils
import net.deathlksr.fuguribeta.utils.PacketUtils.sendPacket
import net.deathlksr.fuguribeta.utils.kotlin.removeEach
import net.deathlksr.fuguribeta.utils.misc.RandomUtils
import net.deathlksr.fuguribeta.utils.pos
import net.deathlksr.fuguribeta.utils.timing.MSTimer
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.gui.GuiChat
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.network.status.server.S01PacketPong
import net.minecraft.util.Vec3

object Ping : Module("Ping", Category.COMBAT, gameDetecting = false, hideModule = false) {

    private val randomdelay by BoolValue("RandomDelay", false)
    private val delay by IntegerValue("Delay", 500, 0..1000) { !randomdelay }
    private val mindelay = IntegerValue("MinDelay", 500, 0..1000) { randomdelay }
    private val maxdelay = IntegerValue("MaxDelay", 520, 0..1000) { randomdelay }
    private val minrecoildelay by IntegerValue("MinRecoilDelay", 0, 0..1000)
    private val maxrecoildelay by IntegerValue("MaxRecoilDelay", 0, 0..1000)
    private val ticksexxisted by BoolValue("TicksExisted", true)
    private val minticksalive by IntegerValue("MaxTicksExisted", 15, 0..60) { ticksexxisted }
    private val flushflag by BoolValue("FlushFlag", true)
    private val tickflag by IntegerValue("TicksFlag", 5, 0..60) { flushflag }
    private val flushoninv by BoolValue("FlushInventory", true)
    private val flushchat by BoolValue("FlushChat", true)
    private val flushoncontainer by BoolValue("FlushContainer", false)
    private val flushonattack by BoolValue("FlushAttack", true)
    private val flushusingitem by BoolValue("FlushUsingItem", true)
    private val flushdamage by BoolValue("FlushGotDamaged", false)
    private val flushvelocity by BoolValue("FlushVelocity", false)
    private val flushblockplacement by BoolValue("FlushBlockPlacement", false)
    private val flushback by BoolValue("FlushBack", false)
    private val flushclickgui by BoolValue("FlushClickGui", false)
    private val flushsprintreset by BoolValue("FlushSprintReset", false)
    private val flushstand by BoolValue("FlushStandingStill", false)
    private val packetQueue = Queues.newArrayDeque<QueueData>()
    private val positions = Queues.newArrayDeque<PositionData>()
    private var Player: EntityOtherPlayerMP? = null
    private val resetTimer = MSTimer()
    private var ignoreWholeTick = false
    private var render by BoolValue("Render", true)

    private var ticksFlag = 0
    private var randomDelays = 0
    private var recoil = 0

    override fun onDisable() {
        Player?.let {
            mc.theWorld.removeEntityFromWorld(it.entityId)
        }
        Player = null
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val serverData = mc.currentServerData

        if (!handleEvents())
            return

        if (mc.thePlayer == null || mc.thePlayer.isDead)
            return

        if (MovementUtils.isMoving && !mc.thePlayer.isSprinting && flushsprintreset) {
            blink()
            return
        }

        if (mc.isIntegratedServerRunning || serverData == null) {
            return
        }

        if (ticksexxisted) {
            if (mc.thePlayer.ticksExisted < minticksalive) {
                blink()
                return
            }
        }

        if (ticksFlag > 0) {
            blink()
            return
        }

        if (event.isCancelled)
            return

        if (ignoreWholeTick)
            return

        // Flush
        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is C01PacketChatMessage, is S01PacketPong -> {
                return
            }

            is C02PacketUseEntity -> {
                if (flushonattack) {
                    blink()
                    return
                }
            }

            is C0EPacketClickWindow, is C0DPacketCloseWindow -> {
                if (flushoninv) {
                    blink()
                    return
                }
            }

            is S08PacketPlayerPosLook -> {
                if (flushflag) {
                    ticksFlag = tickflag
                    return
                }
            }

            is C08PacketPlayerBlockPlacement -> {
                if (flushblockplacement) {
                    blink()
                    return
                }
            }

            is S12PacketEntityVelocity -> {
                if (mc.thePlayer.entityId == packet.entityID) {
                    if (flushvelocity) {
                        blink()
                        return
                    }
                }
            }
        }

        val screen = mc.currentScreen

        if (flushchat) {
            if (screen is GuiChat) {
                blink()
                return
            }
        }

        if (flushoninv) {
            if (screen is GuiInventory) {
                blink()
                return
            }
        }

        if (flushoncontainer) {
            if (screen is GuiContainer) {
                blink()
                return
            }
        }

        if (flushclickgui) {
            if (screen is ClickGui) {
                blink()
                return
            }
        }

        if (flushusingitem) {
            if (mc.thePlayer.isUsingItem) {
                blink()
                return
            }
        }

        if (flushback) {
            if (mc.gameSettings.keyBindBack.pressed) {
                blink()
                return
            }
        }

        if (flushdamage) {
            if (mc.thePlayer.health < mc.thePlayer.maxHealth) {
                if (mc.thePlayer.hurtTime != 0) {
                    blink()
                    return
                }
            }
        }

        if (flushstand) {
            if (!MovementUtils.isMoving) {
                blink()
                return
            }
        }

        // Passed time
        if (!resetTimer.hasTimePassed(recoil))
            return

        if (event.eventType == EventState.SEND) {
            event.cancelEvent()

            if (packet is C03PacketPlayer && packet.isMoving) {
                synchronized(positions) {
                    positions += PositionData(packet.pos, System.currentTimeMillis())
                }
            }

            synchronized(packetQueue) {
                packetQueue += QueueData(packet, System.currentTimeMillis())
            }
        }
    }

    @EventTarget
    fun onTick(event: GameTickEvent) {
        if (!render) return
        if (Player == null && mc.gameSettings.thirdPersonView != 0) {
            Player = EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.gameProfile).apply {
                clonePlayer(mc.thePlayer, true)
            }

            mc.theWorld.addEntityToWorld(-1000, Player)
        }

        if (mc.gameSettings.thirdPersonView == 0) {
            Player?.let {
                mc.theWorld.removeEntityFromWorld(it.entityId)
            }

            Player = null
        }

        val pos = positions.first.pos
        val posx = pos.xCoord
        val posy = pos.yCoord
        val posz = pos.zCoord

        if (Player != null) {
            Player?.posX = posx
            Player?.posY = posy
            Player?.posZ = posz
            Player?.prevPosX = posx
            Player?.prevPosY = posy
            Player?.prevPosZ = posz

            Player?.rotationYaw = mc.thePlayer.rotationYaw
            Player?.rotationYawHead = mc.thePlayer.rotationYawHead
            Player?.prevRotationYaw = mc.thePlayer.prevRotationYaw
            Player?.rotationPitch = mc.thePlayer.rotationPitch
            Player?.prevRotationPitch = mc.thePlayer.prevRotationPitch

            Player?.prevLimbSwingAmount = mc.thePlayer.prevLimbSwingAmount
            Player?.prevSwingProgress = mc.thePlayer.prevSwingProgress
            Player?.swingProgress = mc.thePlayer.swingProgress
            Player?.limbSwingAmount = mc.thePlayer.limbSwingAmount
            Player?.limbSwing = mc.thePlayer.limbSwing
        }
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (ticksFlag > 0) {
            ticksFlag--
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (event.worldClient == null) blink(false)
        Player?.let {
            mc.theWorld.removeEntityFromWorld(it.entityId)
        }
        Player = null
    }

    @EventTarget
    fun onGameLoop(event: GameLoopEvent) {
        mc.thePlayer ?: return

        if (!randomdelay) randomDelays = delay

        if (!resetTimer.hasTimePassed(recoil))
            return

        handlePackets()
        ignoreWholeTick = false
    }

    private fun blink(handlePackets: Boolean = true) {
        mc.addScheduledTask {
            if (handlePackets) {
                resetTimer.reset()
            }

            handlePackets(true)
            ignoreWholeTick = true
        }

        if (randomdelay) randomDelays = RandomUtils.nextInt(mindelay.get(), maxdelay.get())
        recoil = RandomUtils.nextInt(minrecoildelay, maxrecoildelay)
        if (Player != null) {
            Player?.posX = mc.thePlayer.posX
            Player?.posY = mc.thePlayer.posY
            Player?.posZ = mc.thePlayer.posZ
        }
    }

    private fun handlePackets(clear: Boolean = false) {
        synchronized(packetQueue) {
            packetQueue.removeEach { (packet, timestamp) ->
                if (timestamp <= System.currentTimeMillis() - randomDelays || clear) {
                    sendPacket(packet, false)
                    true
                } else false
            }
        }

        synchronized(positions) {
            positions.removeEach { (_, timestamp) -> timestamp <= System.currentTimeMillis() - randomDelays || clear }
        }
    }
}

data class QueueData(val packet: Packet<*>, val time: Long)
data class PositionData(val pos: Vec3, val time: Long)