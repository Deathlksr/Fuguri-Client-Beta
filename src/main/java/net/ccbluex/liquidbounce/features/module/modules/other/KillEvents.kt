package net.ccbluex.liquidbounce.features.module.modules.other

import net.ccbluex.liquidbounce.FuguriBeta
import net.ccbluex.liquidbounce.event.EntityKilledEvent
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.effect.EntityLightningBolt
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.network.play.server.S2CPacketSpawnGlobalEntity
import java.util.*

object KillEvents: Module("KillEvents", Category.PLAYER) {

    private var insult by BoolValue("Insult", false)
    private var BedWars by BoolValue("Bedwars Mode", false) { insult }

    private val effects by BoolValue("Effect", false)

    private val effect by ListValue("Effects", arrayOf("Lighting"), "Lighting") { effects }

    private var killSound by BoolValue("Sound", false)
    private val sound by ListValue("Sounds", arrayOf("None", "Hit", "Amogus", "Totem", "Edogawa", "Explode", "Orb", "Pop", "Splash", "Lightning"), "Pop") { killSound }
    private val volume by FloatValue("Volume", 1f, 0.1f.. 5f) { killSound }
    private val pitch by FloatValue("Pitch", 1f, 0.1f..5f) { killSound }

    private var lastUsed = 0

    private val phraseList = arrayOf(
        ", Я тебе мать ебал",
        ", Твоя мать шалава понял меня да?",
        ", Ты сын шлюхи ебаной!",
        ", Ебало свое закрой я те мамашу ебал!",
        ", Твоя мать на моем хуе!!!",
        ", Донской тебе мать хуем разрезал на пополам",
        ", Коля разрелзал вам клитор на 4 части",
        ", Ты мне отсоси давай позорница ебаная",
        ", Луф убил твоего отца когда он работал на трассе",
        ", Андрей убил твою мертвую бабку на том свете и теперь она работает как ебаный светофор",
        ", Ты инвалидка ебаная соси мне тут яйца",
        ", Вчера побил твою обьебанную бабку под столом",
        ", Всей шайкай мать твою уебали жирним дилдаком дилика который у него в жопе",
        ", MASHA ULTRAFUNK Ебала твоего деда",
        ", Выебал твою дохлую мать используя фугури клиент",
        ", GET GOOD GET FUGURI AUTO VIEBAL MAT",
        ", Мамашу те ебашу терпила ты ебаная моего члена",
        ", Я расчленил твою мать на 400 кусочков и продал под видом свиньи",
        ", Я твоего деда уебал монтировкой в афгане",
        ", Владимир Владимирович Путин убил твою маму не отрицай этого сынок хуйни",
        ", Я твоей мамаше хуем перекрыл сонную артерию и она сдохла нахуй от спида в муках",
        ", Ты - Лучшая реклама Дюрекса",
        ", Тупое жирное животное завали свое ебало никчомный хуисас",
        ", Кожанный мешок с дерьмом у тебя хуев в жопе больше чем молекул во всей вселенной",
        ", Почему я членом вожу по твоей физиономией которая треснула от жира",
        ", У меня самая лучшая шаверма гурма в горадэ",
        ", Я насадил твою мамашу на дуло Доры(800-мм пушка) и она улетела в космос",
        ", Я тапнул топором по ебалу твоей мамаши сын дуба ебаного",
        ", Я в анус твоей мамаши запихал снаряд 80 см размером как мой хуй",
        ", Я насадил твою дохлую маманю на шампур и сделал из нее кэбаб",
        ", Я твою мамашу посадил на электрический стул и у нее пизда нахуй поджарилась",
        ", Чо бабки в тапки",
        ", Бабка старая с мешком похуярила пешком до твоего дома чтобы заминировать его нахуй",
        ", Ебаный ты жиртрес тебя пришлось с вертолета фоткать на выпускной",
        ", Мне не хватит бензина со всех пенис что были у тебя в жопе чтобы обьехать твою долхую мать",
        ", Я выебал твою матушу ты ебаный обплеванный хуесос",
        ", Ошибка природы с 47 хромосомами не разговаривай когда ротик твой забит членами твоих отцов",
        ", Слава доте!!!",
        ", Я обоссал твою матушку хуесос ебаный",
        ", Слышь ты ебаный интернет герой у тебя уже горб как верблюда нахуй",
        ", Я обоссал твое еблище хуесос ты ебаный отсталый сын хуйни",
        ", Пенис по пизде твоей матери провел она так возбудилась что аж взорвалась нахуй",
        ", Овнер фугури передает твоей матери привет и чтобы ее метеором уебало пока она читала это",
        ", Слышь животное ебаное купи себе мивину и пойди поешь бомж ебаный",
        ", Я ебал юки кавая",
        ", Морской Коржик твоей матери надавал в ротикс она была не против<3",
        ", Закрой ебало сынок твари бездомной",
        ", Я твою маму блен целовал",
        ", Тобе выибать фугурэ клиент в жэпу",
        ", Я заварил твой жирний мама как мивину и сьел ее нахуй",
        ", целестиал запенил твою жирную мамашу своими модулями и визуалом",
        ", Я сделал ArrayList из твоих отчимов и у меня вылетело с ошибкой -Out of memory-",
        ", Мой кот своим волосатым членом твою мать убил нахуй",
        ", Я в пизде твоей мамаши заварил мивину с тефтельками и мешал своим хуем как бетономешалкой",
        ", Яж твою мамашу уебал прикладом AK-47",
        ", В твоей мамаше было столько хуев что обозримой вселенной звезд будет меньше",
        ", Я насрал на еблет твоей тупой ебаной мамаши нахуй",
        ", В фугури есть евент onDeathYourMother он вызывается быстрее чем onTick",
        ", Я твою мамашу оттрахаю своим членом чурка ты ебаная сын хуйни",
        ", Я те мамашу ебашу своим членом дура ты ебучая",
        ", Гуд гейм убил твоего отца и посадил его на буж халиф",
        ", Кишки твоей матери на свой адепт член намотал и после все это дело вставил в анал твоего отца",
        ", Я манипулирую временем как пиздой твоей матери",
        ", А ну быстро нахуй делай акробатический элемент на моем адепт члене",
        ", Я твою ебучку тут зассу шуганый сын шлюхи ты на кого тут вздумал рыпатся",
        ", Ебучку закрой и соси мне тут слоняра паршивая",
        )

    @EventTarget
    fun onKill(event: EntityKilledEvent) {
        val target = event.targetEntity

        if (target !is EntityPlayer)
            return

        if (insult) {
            if (BedWars) {
                mc.thePlayer.sendChatMessage("!" + target.name + randomPhrase())
            } else {
                mc.thePlayer.sendChatMessage(target.name + randomPhrase())
            }
        }

        if (effects) doEffect(target)

        if (killSound) doSound()
    }

    private fun randomPhrase(): String {
        val rand = Random()

        var randInt: Int
        randInt = rand.nextInt(phraseList.size)
        while (lastUsed == randInt) {
            randInt = rand.nextInt(phraseList.size)
        }

        lastUsed = randInt
        return phraseList[randInt]
    }

    private fun doSound() {
        val player = mc.thePlayer
        val tsp = FuguriBeta.tipSoundManager

        when (sound) {
            "Hit" -> player.playSound("random.bowhit", volume, pitch)
            "Orb" -> player.playSound("random.orb", volume, pitch)
            "Pop" -> player.playSound("random.pop", volume, pitch)
            "Splash" -> player.playSound("random.splash", volume, pitch)
            "Lightning" -> player.playSound("ambient.weather.thunder", volume, pitch)
            "Explode" -> player.playSound("random.explode", volume, pitch)
            "Amogus" -> tsp.amogusSound.asyncPlay(volume)
            "Totem" -> tsp.totemSound.asyncPlay(volume)
            "Edogawa" -> tsp.edogawaSound.asyncPlay(volume)
        }
    }

    private fun doEffect(target: EntityLivingBase) {
        when (effect) {
            "Lighting" -> spawnLightning(target)
        }
    }

    private fun spawnLightning(target: EntityLivingBase) {
        mc.netHandler.handleSpawnGlobalEntity(
            S2CPacketSpawnGlobalEntity(
                EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ)
            )
        )
    }
}