package me.kteq.hiddenarmor

import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPIBukkitConfig
import me.kteq.hiddenarmor.db.HiddenArmorDB
import me.kteq.hiddenarmor.handler.ArmorPacketHandler
import me.kteq.hiddenarmor.listener.*
import me.kteq.hiddenarmor.listener.packet.ArmorOthersPacketListener
import me.kteq.hiddenarmor.listener.packet.ArmorSelfPacketListener
import org.bukkit.Bukkit
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.util.logging.Level

class HiddenArmorPlugin : JavaPlugin() {
    lateinit var hiddenArmorManager: HiddenArmorManager
        private set
    lateinit var temporaryIgnoreService: TemporaryIgnoreService
        private set

    private lateinit var protocolManager: ProtocolManager

    lateinit var packetHandler: ArmorPacketHandler

    lateinit var db: HiddenArmorDB
        private set

    var isOld = false
        private set

    override fun onLoad() {
        CommandAPI.onLoad(CommandAPIBukkitConfig(this).shouldHookPaperReload(true).silentLogs(true))
    }

    override fun onEnable() {
        saveDefaultConfig()
        checkConfig()

        db = HiddenArmorDB(File(dataFolder, "database.sqlite"))
        db.init()

        isOld = Bukkit.getBukkitVersion().startsWith("1.16")
        protocolManager = ProtocolLibrary.getProtocolManager()
        hiddenArmorManager = HiddenArmorManager(this)
        packetHandler = ArmorPacketHandler(this, protocolManager)
        temporaryIgnoreService = TemporaryIgnoreService(this, packetHandler)
        reloadLocale()

        // Enable commands
        CommandAPI.onEnable()
        HiddenArmorCommands(this)

        // Register ProtocolLib packet listeners
        ArmorSelfPacketListener(this, protocolManager)
        ArmorOthersPacketListener(this, protocolManager)

        // Register event listeners
        InventoryShiftClickListener(this)
        GameModeListener(this)
        PotionEffectListener(this)
        EntityToggleGlideListener(this)
        CombatListener(this)
        JoinQuitListener(this)
    }

    private fun getConfigFile(filename: String): File {
        val file = File(dataFolder, filename)
        if (!file.exists()) {
            file.parentFile.mkdirs()
            saveResource(filename, false)
        }
        return file
    }

    private fun loadYaml(filename: String): YamlConfiguration {
        val file = getConfigFile(filename)
        val config = YamlConfiguration()
        try {
            config.load(file)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InvalidConfigurationException) {
            e.printStackTrace()
        }
        return config
    }

    private fun reloadLocale() {
        Locale.load(loadYaml("locale/${config.getString("locale")}.yml"), false, HashMap())
    }

    fun reload() {
        saveDefaultConfig()
        reloadConfig()
        reloadLocale()
        db = HiddenArmorDB(File(dataFolder, "database.sqlite"))
        db.init()
    }

    override fun onDisable() {
        CommandAPI.onDisable()
    }

    private fun checkConfig() {
        if (config.getInt("config-version") >= config.defaults!!.getInt("config-version")) return
        logger.log(Level.WARNING, "Your HiddenArmorPlugin configuration file is outdated!")
        logger.log(Level.WARNING, "Please regenerate the 'config.yml' file when possible.")
    }
}
