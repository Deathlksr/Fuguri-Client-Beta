/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.handler.api

import net.deathlksr.fuguribeta.FuguriBeta
import net.deathlksr.fuguribeta.FuguriBeta.clientVersionNumber
import net.deathlksr.fuguribeta.FuguriBeta.IN_DEV
import net.deathlksr.fuguribeta.utils.ClientUtils.LOGGER
import java.text.SimpleDateFormat
import java.util.*
import net.deathlksr.fuguribeta.handler.api.ClientApi.requestNewestBuildEndpoint

object ClientUpdate {

    val gitInfo = Properties().also {
        val inputStream = FuguriBeta::class.java.classLoader.getResourceAsStream("git.properties")

        if (inputStream != null) {
            it.load(inputStream)
        } else {
            it["git.build.version"] = "unofficial"
        }
    }

    val newestVersion by lazy {
        // https://api.liquidbounce.net/api/v1/version/builds/legacy
        try {
            requestNewestBuildEndpoint(branch = FuguriBeta.clientBranch, release = !IN_DEV)
        } catch (e: Exception) {
            LOGGER.error("Unable to receive update information", e)
            return@lazy null
        }
    }

    fun hasUpdate(): Boolean {
        try {
            val newestVersion = newestVersion ?: return false
            val actualVersionNumber = newestVersion.lbVersion.substring(1).toIntOrNull() ?: 0 // version format: "b<VERSION>" on legacy

            return if (IN_DEV) { // check if new build is newer than current build
                val newestVersionDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(newestVersion.date)
                val currentVersionDate = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(gitInfo["git.commit.time"].toString())

                newestVersionDate.after(currentVersionDate)
            } else {
                // check if version number is higher than current version number (on release builds only!)
                newestVersion.release && actualVersionNumber > clientVersionNumber
            }
        } catch (e: Exception) {
            LOGGER.error("Unable to check for update", e)
            return false
        }
    }

}

