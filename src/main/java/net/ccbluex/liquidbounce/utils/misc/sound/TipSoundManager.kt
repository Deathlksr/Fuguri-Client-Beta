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
    var buttonpressSound: TipSoundPlayer
    var fatalitySound: TipSoundPlayer
    var loginSuccessfulSound: TipSoundPlayer

    init {
        val enableSoundFile = File(FuguriBeta.fileManager.soundsDir, "enable.wav")
        val popSoundFile = File(FuguriBeta.fileManager.soundsDir, "pop.wav")
        val disableSoundFile = File(FuguriBeta.fileManager.soundsDir, "disable.wav")
        val startUpSoundFile = File(FuguriBeta.fileManager.soundsDir, "startup.wav")
        val shutDownSoundFile = File(FuguriBeta.fileManager.soundsDir,  "shutdown.wav")
        val amogusSoundFile = File(FuguriBeta.fileManager.soundsDir, "imposter.wav")
        val totemSoundFile = File(FuguriBeta.fileManager.soundsDir, "totem.wav")
        val buttonpressSoundFile = File(FuguriBeta.fileManager.soundsDir, "buttonpress.wav")
        val fatalitySoundFile = File(FuguriBeta.fileManager.soundsDir, "fatality.wav")
        val loginSuccessfulSoundFile = File(FuguriBeta.fileManager.soundsDir, "loginSuccessful.wav")

        if (!enableSoundFile.exists())
            FileUtils.unpackFile(enableSoundFile, "assets/minecraft/fuguribeta/sound/enable.wav")
        if (!popSoundFile.exists())
            FileUtils.unpackFile(popSoundFile, "assets/minecraft/fuguribeta/sound/pop.wav")
        if (!disableSoundFile.exists())
            FileUtils.unpackFile(disableSoundFile, "assets/minecraft/fuguribeta/sound/disable.wav")
        if (!startUpSoundFile.exists())
            FileUtils.unpackFile(startUpSoundFile, "assets/minecraft/fuguribeta/sound/startup.wav")
        if (!shutDownSoundFile.exists())
            FileUtils.unpackFile(shutDownSoundFile, "assets/minecraft/fuguribeta/sound/shutdown.wav")
        if (!amogusSoundFile.exists())
            FileUtils.unpackFile(amogusSoundFile, "assets/minecraft/fuguribeta/sound/imposter.wav")
        if (!totemSoundFile.exists())
            FileUtils.unpackFile(totemSoundFile, "assets/minecraft/fuguribeta/sound/totem.wav")
        if (!buttonpressSoundFile.exists())
            FileUtils.unpackFile(buttonpressSoundFile, "assets/minecraft/fuguribeta/sound/buttonpress.wav")
        if (!fatalitySoundFile.exists())
            FileUtils.unpackFile(fatalitySoundFile, "assets/minecraft/fuguribeta/sound/fatality.wav")
        if (!loginSuccessfulSoundFile.exists())
            FileUtils.unpackFile(loginSuccessfulSoundFile, "assets/minecraft/fuguribeta/sound/loginSuccessful.wav")

        enableSound = TipSoundPlayer(enableSoundFile)
        popSound = TipSoundPlayer(popSoundFile)
        disableSound = TipSoundPlayer(disableSoundFile)
        startUpSound = TipSoundPlayer(startUpSoundFile)
        shutdownSound = TipSoundPlayer(shutDownSoundFile)
        amogusSound = TipSoundPlayer(amogusSoundFile)
        totemSound = TipSoundPlayer(totemSoundFile)
        buttonpressSound = TipSoundPlayer(buttonpressSoundFile)
        fatalitySound = TipSoundPlayer(fatalitySoundFile)
        loginSuccessfulSound = TipSoundPlayer(loginSuccessfulSoundFile)
    }
}