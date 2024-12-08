package me.kteq.hiddenarmor.listener

import me.kteq.hiddenarmor.HiddenArmorPlugin
import me.kteq.hiddenarmor.Permission
import me.kteq.hiddenarmor.util.EventUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class JoinQuitListener(private val plugin: HiddenArmorPlugin) : Listener {
    init {
        EventUtil.register(this, plugin)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (!plugin.hiddenArmorManager.isPlayerArmorHidden(event.player)) return
        plugin.temporaryIgnoreService.free(event.player.uniqueId)
        plugin.hiddenArmorManager.removePlayerForCache(event.player.uniqueId)
    }

    @EventHandler
    fun onAsyncJoin(event: AsyncPlayerPreLoginEvent) {
        plugin.hiddenArmorManager.loadPlayerToCache(event.uniqueId)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (!plugin.hiddenArmorManager.isPlayerArmorHidden(event.player)) return
        if (event.player.hasPermission(Permission.TOGGLE)) return
        plugin.hiddenArmorManager.setPlayerArmorHiddenStatus(event.player, false)
        plugin.logger.info("${event.player.name} no longer has permission to hide their armor.")
    }
}
