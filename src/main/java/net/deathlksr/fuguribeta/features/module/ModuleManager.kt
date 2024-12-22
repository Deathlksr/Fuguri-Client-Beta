/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.deathlksr.fuguribeta.features.module

import net.deathlksr.fuguribeta.event.EventManager.registerListener
import net.deathlksr.fuguribeta.event.EventManager.unregisterListener
import net.deathlksr.fuguribeta.event.EventTarget
import net.deathlksr.fuguribeta.event.KeyEvent
import net.deathlksr.fuguribeta.event.Listenable
import net.deathlksr.fuguribeta.features.command.CommandManager.registerCommand
import net.deathlksr.fuguribeta.utils.ClassUtils
import net.deathlksr.fuguribeta.utils.ClientUtils.LOGGER
import net.deathlksr.fuguribeta.utils.inventory.InventoryManager
import java.util.*

object ModuleManager : Listenable {

    val modules = TreeSet<Module> { module1, module2 -> module1.name.compareTo(module2.name) }
    private val moduleClassMap = hashMapOf<Class<*>, Module>()

    init {
        registerListener(this)
    }

    /**
     * Register all modules
     */
    fun registerModules() {
        LOGGER.info("[ModuleManager] Loading modules...")

        // Register modules
        ClassUtils.resolvePackage("${this.javaClass.`package`.name}.modules", Module::class.java)
            .forEach(this::registerModule)

        modules.forEach {
            it.onInitialize()
            //  SplashProgress.setSecondary("Initializing Module " + it.name)
        }

        InventoryManager.startCoroutine()

        LOGGER.info("[ModuleManager] Loaded ${modules.size} modules.")
    }

    /**
     * Register [module]
     */
    fun registerModule(module: Module) {
        modules += module
        moduleClassMap[module.javaClass] = module

        generateCommand(module)
        registerListener(module)
    }

    /**
     * Register [moduleClass] with new instance
     */
    private fun registerModule(moduleClass: Class<out Module>) {
        try {
            registerModule(moduleClass.newInstance())
        } catch (e: IllegalAccessException) {
            // this module is a kotlin object
            registerModule(ClassUtils.getObjectInstance(moduleClass) as Module)
        } catch (e: Throwable) {
            LOGGER.error("Failed to load module: ${moduleClass.name} (${e.javaClass.name}: ${e.message})")
        }
    }

    /**
     * Register a list of modules
     */
    @SafeVarargs
    fun registerModules(vararg modules: Class<out Module>) = modules.forEach(this::registerModule)

    /**
     * Register a list of modules
     */
    @SafeVarargs
    fun registerModules(vararg modules: Module) = modules.forEach(this::registerModule)

    /**
     * Unregister module
     */
    fun unregisterModule(module: Module) {
        modules.remove(module)
        moduleClassMap.remove(module::class.java)
        unregisterListener(module)
    }

    /**
     * Generate command for [module]
     */
    internal fun generateCommand(module: Module) {
        val values = module.values

        if (values.isEmpty())
            return

        registerCommand(ModuleCommand(module, values))
    }

    /**
     * Get module by [moduleClass]
     */
    fun <T : Module> getModule(moduleClass: Class<T>): T? {
        return moduleClassMap[moduleClass] as T?
    }

    operator fun <T : Module> get(clazz: Class<T>) = getModule(clazz)

    /**
     * Get module by [moduleName]
     */
    fun getModule(moduleName: String?) = modules.find { it.name.equals(moduleName, ignoreCase = true) }

    fun getKeyBind(key: Int) = modules.filter { it.keyBind == key }

    operator fun get(name: String) = getModule(name)

    /**
     * Module related events
     */

    /**
     * Handle incoming key presses
     */
    @EventTarget
    private fun onKey(event: KeyEvent) = modules.forEach { if (it.keyBind == event.key) it.toggle() }

    override fun handleEvents() = true
}