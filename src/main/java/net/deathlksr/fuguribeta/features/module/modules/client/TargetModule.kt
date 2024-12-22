package net.deathlksr.fuguribeta.features.module.modules.client

import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.value.BoolValue

object TargetModule : Module("Target", Category.CLIENT, defaultInArray = false, gameDetecting = false, hideModule = true, canBeEnabled = false) {
    var playerValue by BoolValue("Player", true)
    var animalValue by BoolValue("Animal", true)
    var mobValue by BoolValue("Mob", true)
    var invisibleValue by BoolValue("Invisible", false)
    var deadValue by BoolValue("Dead", false)

    override fun handleEvents() = true
}