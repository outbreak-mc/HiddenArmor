package me.kteq.hiddenarmor.listener

import me.kteq.hiddenarmor.HiddenArmorPlugin
import me.kteq.hiddenarmor.UpdateReason
import me.kteq.hiddenarmor.util.EventUtil
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.potion.PotionEffectType

class PotionEffectListener(
    private val plugin: HiddenArmorPlugin
) : Listener {
    init {
        EventUtil.register(this, plugin)
    }

    @EventHandler
    fun onPlayerInvisibleEffect(event: EntityPotionEffectEvent) {
        if (event.entity !is Player) return
        if (event.newEffect?.type != PotionEffectType.INVISIBILITY
            && event.oldEffect?.type != PotionEffectType.INVISIBILITY
        ) return
        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            plugin.packetHandler.updatePlayer(event.entity as Player, UpdateReason.POTION_EFFECT)
        }, 2L)
    }
}
