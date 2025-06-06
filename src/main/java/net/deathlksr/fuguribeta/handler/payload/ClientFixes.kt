/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.handler.payload

import io.netty.buffer.Unpooled
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.Listenable
import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.features.module.modules.client.ClientSpoofer.customValue
import net.deathlksr.fuguribeta.features.module.modules.client.ClientSpoofer.possibleBrands
import net.deathlksr.fuguribeta.utils.ClientUtils.LOGGER
import net.deathlksr.fuguribeta.utils.MinecraftInstance
import net.minecraft.network.PacketBuffer
import net.minecraft.network.play.client.C17PacketCustomPayload

object ClientFixes : MinecraftInstance(), Listenable {

    var fmlFixesEnabled = true
    var blockFML = true
    var blockProxyPacket = true
    var blockPayloadPackets = true
    var blockResourcePackExploit = true

    @EventTarget
    fun onPacket(event: PacketEvent) = runCatching {
        val packet = event.packet

        if (mc.isIntegratedServerRunning || !fmlFixesEnabled) {
            return@runCatching
        }

        when {
            blockProxyPacket && packet.javaClass.name == "net.minecraftforge.fml.common.network.internal.FMLProxyPacket" -> {
                event.cancelEvent()
                return@runCatching
            }

            packet is C17PacketCustomPayload -> {
                if (blockPayloadPackets && !packet.channelName.startsWith("MC|")) {
                    event.cancelEvent()
                } else if (packet.channelName == "MC|Brand") {
                    packet.data = PacketBuffer(Unpooled.buffer()).writeString(
                        when (possibleBrands.get()) {
                            "Vanilla" -> "vanilla"
                            "LunarClient" -> "lunarclient:v2.15.6-2422"
                            "OptiFine" -> "optifine"
                            "CheatBreaker" -> "CB"
                            "Fabric" -> "fabric"
                            "PvPLounge" -> "PLC18"
                            "Geyser" -> "geyser"
                            "Minebuilders" -> "minebuilders9"
                            "Feather" -> "feather"
                            "FML" -> "fml,forge"
                            "Log4j" -> "LOLG4J"
                            "Custom" -> customValue.get()
                            else -> {
                                // do nothing
                                return@runCatching
                            }
                        }
                    )
                }
            }
        }
    }.onFailure {
        LOGGER.error("Failed to handle packet on client fixes.", it)
    }

    @JvmStatic
    fun getClientModName(): String {
        return possibleBrands.get()
    }

    override fun handleEvents() = true
}