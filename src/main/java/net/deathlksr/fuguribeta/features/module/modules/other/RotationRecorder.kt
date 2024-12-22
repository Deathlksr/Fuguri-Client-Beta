/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module.modules.other

import net.deathlksr.fuguribeta.FuguriBeta.CLIENT_NAME
import net.deathlksr.fuguribeta.FuguriBeta.CLIENT_VERSION
import net.deathlksr.fuguribeta.event.EventState
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.MotionEvent
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.script.api.global.Chat
import net.deathlksr.fuguribeta.utils.ClientUtils
import net.deathlksr.fuguribeta.utils.Rotation
import net.deathlksr.fuguribeta.utils.RotationUtils.getAngleDifference
import net.deathlksr.fuguribeta.utils.RotationUtils.lastServerRotation
import net.deathlksr.fuguribeta.utils.RotationUtils.serverRotation
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object RotationRecorder : Module("RotationRecorder", Category.OTHER) {

    private val rotationList: MutableList<Pair<Rotation, Int>> = mutableListOf()

    override fun onEnable() {
        rotationList.clear()

        Chat.print("Started recording rotations.")
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (event.eventState != EventState.POST)
            return

        rotationList.add(Rotation(getAngleDifference(serverRotation.yaw, lastServerRotation.yaw),
            getAngleDifference(serverRotation.pitch, lastServerRotation.pitch)
        ) to ClientUtils.runTimeTicks
        )
    }

    override fun onDisable() {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        val formattedDateTime = currentDateTime.format(formatter)

        writeToFile("rotations_$formattedDateTime.txt", rotationList)
    }

    private fun writeToFile(fileName: String, content: List<Pair<Rotation, Int>>) {
        // Get the Minecraft directory
        val mcDir = File(mc.mcDataDir, "$CLIENT_NAME-$CLIENT_VERSION")
        // Create the file object in the Minecraft directory
        val file = File(mcDir, fileName)
        try {
            BufferedWriter(FileWriter(file)).use { writer ->
                content.forEach {
                    writer.write("YAW: ${it.first.yaw}, PITCH: ${it.first.pitch} in tick ${it.second}")
                    writer.newLine()
                }
                writer.flush()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            Chat.print("Saved as $fileName in $mcDir")
        }
    }

}