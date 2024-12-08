package me.kteq.hiddenarmor.listener

import me.kteq.hiddenarmor.HiddenArmorPlugin
import me.kteq.hiddenarmor.UpdateReason
import me.kteq.hiddenarmor.util.EventUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityToggleGlideEvent

class EntityToggleGlideListener(
    private val plugin: HiddenArmorPlugin
) : Listener {
    init {
        EventUtil.register(this, plugin)
    }

    @EventHandler
    fun onPlayerToggleGlide(e: EntityToggleGlideEvent) {
        if (e.entity !is Player) return
        val player = e.entity as Player
        if (!plugin.hiddenArmorManager.shouldCurrentlyHideArmor(player))
            return
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            plugin.packetHandler.updatePlayer(player, UpdateReason.GLIDE)
        }, 1L)
    }
}
