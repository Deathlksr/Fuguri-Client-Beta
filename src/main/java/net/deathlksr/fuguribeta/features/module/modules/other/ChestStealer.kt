package net.deathlksr.fuguribeta.features.module.modules.other

import kotlinx.coroutines.delay
import net.deathlksr.fuguribeta.FuguriBeta.hud
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.utils.extensions.component1
import net.deathlksr.fuguribeta.utils.extensions.component2
import net.deathlksr.fuguribeta.event.PacketEvent
import net.deathlksr.fuguribeta.event.Render2DEvent
import net.deathlksr.fuguribeta.features.module.Module
import net.deathlksr.fuguribeta.features.module.Category
import net.deathlksr.fuguribeta.features.module.modules.combat.AutoArmor
import net.deathlksr.fuguribeta.features.module.modules.player.InventoryCleaner
import net.deathlksr.fuguribeta.features.module.modules.player.InventoryCleaner.canBeSortedTo
import net.deathlksr.fuguribeta.features.module.modules.player.InventoryCleaner.isStackUseful
import net.deathlksr.fuguribeta.script.api.global.Chat
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Notification
import net.deathlksr.fuguribeta.ui.client.hud.element.elements.Type
import net.deathlksr.fuguribeta.utils.CoroutineUtils.waitUntil
import net.deathlksr.fuguribeta.utils.extensions.shuffled
import net.deathlksr.fuguribeta.utils.inventory.InventoryManager
import net.deathlksr.fuguribeta.utils.inventory.InventoryManager.canClickInventory
import net.deathlksr.fuguribeta.utils.inventory.InventoryManager.chestStealerCurrentSlot
import net.deathlksr.fuguribeta.utils.inventory.InventoryManager.chestStealerLastSlot
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils.countSpaceInInventory
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils.hasSpaceInInventory
import net.deathlksr.fuguribeta.utils.inventory.InventoryUtils.serverSlot
import net.deathlksr.fuguribeta.utils.render.RenderUtils.drawRect
import net.deathlksr.fuguribeta.utils.timing.TimeUtils.randomDelay
import net.deathlksr.fuguribeta.value.BoolValue
import net.deathlksr.fuguribeta.value.IntegerValue
import net.deathlksr.fuguribeta.value.ListValue
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.entity.EntityLiving.getArmorPosition
import net.minecraft.init.Blocks
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.C0DPacketCloseWindow
import net.minecraft.network.play.server.S2DPacketOpenWindow
import net.minecraft.network.play.server.S2EPacketCloseWindow
import net.minecraft.network.play.server.S30PacketWindowItems
import java.awt.Color
import kotlin.math.sqrt

object ChestStealer : Module("ChestStealer", Category.OTHER, hideModule = false) {

    private val smartDelay by BoolValue("SmartDelay", false)
    private val multiplier by IntegerValue("DelayMultiplier", 120, 0..500) { smartDelay }
    private val smartOrder by BoolValue("SmartOrder", true) { smartDelay }

    private val simulateShortStop by BoolValue("SimulateShortStop", false)

    private val maxDelay: Int by object : IntegerValue("MaxDelay", 50, 0..500) {
        override fun isSupported() = !smartDelay
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtLeast(minDelay)
    }
    private val minDelay by object : IntegerValue("MinDelay", 50, 0..500) {
        override fun isSupported() = maxDelay > 0 && !smartDelay
        override fun onChange(oldValue: Int, newValue: Int) = newValue.coerceAtMost(maxDelay)
    }

    private val startDelay by IntegerValue("StartDelay", 50, 0..500)
    private val closeDelay by IntegerValue("CloseDelay", 50, 0..500)

    private val noMove by InventoryManager.noMoveValue
    private val noMoveAir by InventoryManager.noMoveAirValue
    private val noMoveGround by InventoryManager.noMoveGroundValue

    private val chestTitle by BoolValue("ChestTitle", true)

    private val randomSlot by BoolValue("RandomSlot", true)

    private val progressBar by BoolValue("ProgressBar", true, subjective = true)

    val silentGUI by BoolValue("SilentGUI", false, subjective = true)

    val highlightSlot by BoolValue("Highlight-Slot", false, subjective = true) { !silentGUI }

    val backgroundRed by IntegerValue("Background-R", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val backgroundGreen by IntegerValue("Background-G", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val backgroundBlue by IntegerValue("Background-B", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val backgroundAlpha by IntegerValue("Background-Alpha", 255, 0..255, subjective = true) { highlightSlot && !silentGUI }

    val borderStrength by IntegerValue("Border-Strength", 3, 1..5, subjective = true) { highlightSlot && !silentGUI }
    val borderRed by IntegerValue("Border-R", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val borderGreen by IntegerValue("Border-G", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val borderBlue by IntegerValue("Border-B", 128, 0..255, subjective = true) { highlightSlot && !silentGUI }
    val borderAlpha by IntegerValue("Border-Alpha", 255, 0..255, subjective = true) { highlightSlot && !silentGUI }

    private val chestDebug by ListValue("Chest-Debug", arrayOf("Off", "Text", "Notification"), "Off", subjective = true)
    private val itemStolenDebug by BoolValue("ItemStolen-Debug", false, subjective = true) { chestDebug != "Off" }

    private var progress: Float? = null
        set(value) {
            field = value?.coerceIn(0f, 1f)

            if (field == null)
                easingProgress = 0f
        }

    private var easingProgress = 0f

    private var receivedId: Int? = null

    private var stacks = emptyList<ItemStack?>()

    private suspend fun shouldOperate(): Boolean {
        while (true) {
            if (!handleEvents())
                return false

            if (mc.playerController?.currentGameType?.isSurvivalOrAdventure != true)
                return false

            if (mc.currentScreen !is GuiChest)
                return false

            if (mc.thePlayer?.openContainer?.windowId != receivedId)
                return false

            // Wait till NoMove check isn't violated
            if (canClickInventory())
                return true

            // If NoMove is violated, wait a tick and check again
            // If there is no delay, very weird things happen: https://www.guilded.gg/CCBlueX/groups/1dgpg8Jz/channels/034be45e-1b72-4d5a-bee7-d6ba52ba1657/chat?messageId=94d314cd-6dc4-41c7-84a7-212c8ea1cc2a
            delay(50)
        }
    }

    suspend fun stealFromChest() {
        if (!handleEvents())
            return

        val thePlayer = mc.thePlayer ?: return

        val screen = mc.currentScreen ?: return

        if (screen !is GuiChest || !shouldOperate())
            return

        // Check if chest isn't a custom gui
        if (chestTitle && Blocks.chest.localizedName !in (screen.lowerChestInventory ?: return).name)
            return

        progress = 0f

        delay(startDelay.toLong())

        debug("Stealing items..")

        // Go through the chest multiple times, till there are no useful items anymore
        while (true) {
            if (!shouldOperate())
                return

            if (!hasSpaceInInventory())
                return

            var hasTaken = false

            val itemsToSteal = getItemsToSteal()

            run scheduler@{
                itemsToSteal.forEachIndexed { index, (slot, stack, sortableTo) ->
                    // Wait for NoMove or cancel click
                    if (!shouldOperate()) {
                        TickScheduler += { serverSlot = thePlayer.inventory.currentItem }
                        chestStealerCurrentSlot = -1
                        chestStealerLastSlot = -1
                        return
                    }

                    if (!hasSpaceInInventory()) {
                        chestStealerCurrentSlot = -1
                        chestStealerLastSlot = -1
                        return@scheduler
                    }

                    hasTaken = true

                    // Set current slot being stolen for highlighting
                    chestStealerCurrentSlot = slot

                    val stealingDelay = if (smartDelay && index + 1 < itemsToSteal.size) {
                        val dist = getSquaredDistanceBwSlots(getCords(slot), getCords(itemsToSteal[index + 1].first))
                        val trueDelay = sqrt(dist.toDouble()) * multiplier
                        randomDelay(trueDelay.toInt(), trueDelay.toInt() + 20)
                    } else {
                        randomDelay(minDelay, maxDelay)
                    }

                    if (itemStolenDebug) debug("item: ${stack.displayName.lowercase()} | slot: $slot | delay: ${stealingDelay}ms")

                    // If target is sortable to a hotbar slot, steal and sort it at the same time, else shift + left-click
                    TickScheduler.scheduleClick(slot, sortableTo ?: 0, if (sortableTo != null) 2 else 1) {
                        progress = (index + 1) / itemsToSteal.size.toFloat()

                        if (!AutoArmor.canEquipFromChest())
                            return@scheduleClick

                        val item = stack.item

                        if (item !is ItemArmor || thePlayer.inventory.armorInventory[getArmorPosition(stack) - 1] != null)
                            return@scheduleClick

                        // TODO: should the stealing be suspended until the armor gets equipped and some delay on top of that, maybe toggleable?
                        // Try to equip armor piece from hotbar 1 tick after stealing it
                        TickScheduler += {
                            val hotbarStacks = thePlayer.inventory.mainInventory.take(9)

                            // Can't get index of stack instance, because it is different even from the one returned from windowClick()
                            val newIndex = hotbarStacks.indexOfFirst { it?.getIsItemStackEqual(stack) ?: false }

                            if (newIndex != -1)
                                AutoArmor.equipFromHotbarInChest(newIndex, stack)
                        }
                    }

                    delay(stealingDelay.toLong())

                    if (simulateShortStop && Math.random() > 0.75) {
                        val minDelays = randomDelay(150, 300)
                        val maxDelays = randomDelay(minDelays, 500)
                        val randomDelay = (Math.random() * (maxDelays - minDelays) + minDelays).toLong()

                        delay(randomDelay)
                    }
                }
            }

            // If no clicks were sent in the last loop stop searching
            if (!hasTaken) {
                progress = 1f
                delay(closeDelay.toLong())

                TickScheduler += { serverSlot = thePlayer.inventory.currentItem }
                break
            }

            // Wait till all scheduled clicks were sent
            waitUntil(TickScheduler::isEmpty)

            // Before closing the chest, check all items once more, whether server hadn't cancelled some of the actions.
            stacks = thePlayer.openContainer.inventory
        }

        // Wait before the chest gets closed (if it gets closed out of tick loop it could throw npe)
        TickScheduler.scheduleAndSuspend {
            chestStealerCurrentSlot = -1
            chestStealerLastSlot = -1
            thePlayer.closeScreen()
            progress = null

            debug("Chest closed")
        }
    }

    private fun getCords(slot: Int): Pair<Int, Int> {
        val x = slot % 9
        val y = slot / 9
        return Pair(x, y)
    }

    private fun getSquaredDistanceBwSlots(from: Pair<Int, Int>, to: Pair<Int, Int>): Int {
        return (from.first - to.first) * (from.first - to.first) + (from.second - to.second) * (from.second - to.second)
    }

    private fun getItemsToSteal(): MutableList<Triple<Int, ItemStack, Int?>> {
        val sortBlacklist = BooleanArray(9)

        var spaceInInventory = countSpaceInInventory()

        val itemsToSteal = stacks.dropLast(36)
            .mapIndexedNotNull { index, stack ->
                stack ?: return@mapIndexedNotNull null

                if (index in TickScheduler) return@mapIndexedNotNull null

                val mergeableCount = mc.thePlayer.inventory.mainInventory.sumOf { otherStack ->
                    otherStack ?: return@sumOf 0

                    if (otherStack.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, otherStack))
                        otherStack.maxStackSize - otherStack.stackSize
                    else 0
                }

                val canMerge = mergeableCount > 0
                val canFullyMerge = mergeableCount >= stack.stackSize

                // Clicking this item wouldn't take it from chest or merge it
                if (!canMerge && spaceInInventory <= 0) return@mapIndexedNotNull null

                // If stack can be merged without occupying any additional slot, do not take stack limits into account
                // TODO: player could theoretically already have too many stacks in inventory before opening the chest so no more should even get merged
                // TODO: if it can get merged but would also need another slot, it could simulate 2 clicks, one which maxes out the stack in inventory and second that puts excess items back
                if (InventoryCleaner.handleEvents() && !isStackUseful(stack, stacks, noLimits = canFullyMerge))
                    return@mapIndexedNotNull null

                var sortableTo: Int? = null

                // If stack can get merged, do not try to sort it, normal shift + left-click will merge it
                if (!canMerge && InventoryCleaner.handleEvents() && InventoryCleaner.sort) {
                    for (hotbarIndex in 0..8) {
                        if (sortBlacklist[hotbarIndex])
                            continue

                        if (!canBeSortedTo(hotbarIndex, stack.item))
                            continue

                        val hotbarStack = stacks.getOrNull(stacks.size - 9 + hotbarIndex)

                        // If occupied hotbar slot isn't already sorted or isn't strictly best, sort to it
                        if (!canBeSortedTo(hotbarIndex, hotbarStack?.item) || !isStackUseful(hotbarStack,
                                stacks,
                                strictlyBest = true
                            )) {
                            sortableTo = hotbarIndex
                            sortBlacklist[hotbarIndex] = true
                            break
                        }
                    }
                }

                // If stack gets fully merged, no slot in inventory gets occupied
                if (!canFullyMerge) spaceInInventory--

                Triple(index, stack, sortableTo)
            }.shuffled(randomSlot)

            // Prioritise armor pieces with lower priority, so that as many pieces can get equipped from hotbar after chest gets closed
            .sortedByDescending { it.second.item is ItemArmor }

            // Prioritize items that can be sorted
            .sortedByDescending { it.third != null }

            .toMutableList()
            .also { it ->
                // Fully prioritise armor pieces when it is possible to equip armor while in chest
                if (AutoArmor.canEquipFromChest())
                    it.sortByDescending { it.second.item is ItemArmor }
            }
        if (smartOrder) {
            sortBasedOnOptimumPath(itemsToSteal)
        }
        return itemsToSteal
    }

    private fun sortBasedOnOptimumPath(itemsToSteal: MutableList<Triple<Int, ItemStack, Int?>>) {
        for (i in itemsToSteal.indices) {
            var nextIndex = i
            var minDistance = Double.MAX_VALUE
            var next: Triple<Int, ItemStack, Int?>? = null
            for (j in i + 1 until itemsToSteal.size) {
                val distance =
                    getSquaredDistanceBwSlots(getCords(itemsToSteal[i].first), getCords(itemsToSteal[j].first))
                if (distance < minDistance) {
                    minDistance = distance.toDouble()
                    next = itemsToSteal[j]
                    nextIndex = j
                }
            }
            next?.let {
                itemsToSteal[nextIndex] = itemsToSteal[i + 1]
                itemsToSteal[i + 1] = next
            }
        }
    }

    // Progress bar
    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (!progressBar || mc.currentScreen !is GuiChest)
            return

        val progress = progress ?: return

        val (scaledWidth, scaledHeight) = ScaledResolution(mc)

        val minX = scaledWidth * 0.3f
        val maxX = scaledWidth * 0.7f
        val minY = scaledHeight * 0.75f
        val maxY = minY + 10f

        easingProgress += (progress - easingProgress) / 6f * event.partialTicks

        drawRect(minX - 2, minY - 2, maxX + 2, maxY + 2, Color(200, 200, 200).rgb)
        drawRect(minX, minY, maxX, maxY, Color(50, 50, 50).rgb)
        drawRect(minX,
            minY,
            minX + (maxX - minX) * easingProgress,
            maxY,
            Color.HSBtoRGB(easingProgress / 5, 1f, 1f) or 0xFF0000
        )
    }

    @EventTarget
    fun onPacket(event: PacketEvent) {
        when (val packet = event.packet) {
            is C0DPacketCloseWindow, is S2DPacketOpenWindow, is S2EPacketCloseWindow -> {
                receivedId = null
                progress = null
            }

            is S30PacketWindowItems -> {
                // Chests never have windowId 0
                if (packet.func_148911_c() == 0)
                    return

                if (receivedId != packet.func_148911_c()) {
                    debug("Chest opened with ${stacks.size} items")
                }

                receivedId = packet.func_148911_c()

                stacks = packet.itemStacks.toList()
            }
        }
    }

    private fun debug(message: String) {
        if (chestDebug == "Off") return

        when (chestDebug.lowercase()) {
            "text" -> Chat.print(message)
            "notification" -> hud.addNotification(Notification(message, "debug", Type.INFO, 500))
        }
    }
}