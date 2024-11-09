package net.ccbluex.liquidbounce.utils.misc.sound

import net.ccbluex.liquidbounce.FuguriBeta
import net.ccbluex.liquidbounce.utils.FileUtils
import java.io.File

class TipSoundManager {
    var enableSound: TipSoundPlayer
    var popSound: TipSoundPlayer
    var disableSound: TipSoundPlayer
    var startUpSound: TipSoundPlayer
    var shutdownSound: TipSoundPlayer
    var amogusSound: TipSoundPlayer
    var totemSound: TipSoundPlayer

    init {
        val enableSoundFile = File(FuguriBeta.fileManager.soundsDir, "enable.wav")
        val popSoundFile = File(FuguriBeta.fileManager.soundsDir, "pop.wav")
        val disableSoundFile = File(FuguriBeta.fileManager.soundsDir, "disable.wav")
        val startUpSoundFile = File(FuguriBeta.fileManager.soundsDir, "startup.wav")
        val shutDownSoundFile = File(FuguriBeta.fileManager.soundsDir,  "shutdown.wav")
        val amogusSoundFile = File(FuguriBeta.fileManager.soundsDir, "imposter.wav")
        val totemSoundFile = File(FuguriBeta.fileManager.soundsDir, "totem.wav")

        if (!enableSoundFile.exists())
            FileUtils.unpackFile(enableSoundFile, "assets/minecraft/fdpclient/sound/enable.wav")
        if (!popSoundFile.exists())
            FileUtils.unpackFile(popSoundFile, "assets/minecraft/fdpclient/sound/pop.wav")
        if (!disableSoundFile.exists())
            FileUtils.unpackFile(disableSoundFile, "assets/minecraft/fdpclient/sound/disable.wav")
        if (!startUpSoundFile.exists())
            FileUtils.unpackFile(startUpSoundFile, "assets/minecraft/fdpclient/sound/startup.wav")
        if (!shutDownSoundFile.exists())
            FileUtils.unpackFile(shutDownSoundFile, "assets/minecraft/fdpclient/sound/shutdown.wav")
        if (!amogusSoundFile.exists())
            FileUtils.unpackFile(amogusSoundFile, "assets/minecraft/fdpclient/sound/imposter.wav")
        if (!totemSoundFile.exists())
            FileUtils.unpackFile(totemSoundFile, "assets/minecraft/fdpclient/sound/totem.wav")

        enableSound = TipSoundPlayer(enableSoundFile)
        popSound = TipSoundPlayer(popSoundFile)
        disableSound = TipSoundPlayer(disableSoundFile)
        startUpSound = TipSoundPlayer(startUpSoundFile)
        shutdownSound = TipSoundPlayer(shutDownSoundFile)
        amogusSound = TipSoundPlayer(amogusSoundFile)
        totemSound = TipSoundPlayer(totemSoundFile)
    }
}