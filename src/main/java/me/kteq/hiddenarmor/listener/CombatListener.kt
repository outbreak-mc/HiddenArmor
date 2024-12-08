package me.kteq.hiddenarmor.listener

import me.kteq.hiddenarmor.HiddenArmorPlugin
import me.kteq.hiddenarmor.UpdateReason
import me.kteq.hiddenarmor.util.EventUtil
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent

class CombatListener(
    private val plugin: HiddenArmorPlugin
) : Listener {
    init {
        EventUtil.register(this, plugin)
    }

    private fun show(player: Player) {
        if (!plugin.hiddenArmorManager.isPlayerArmorHidden(player)) return
        plugin.temporaryIgnoreService.temporaryDisableHiding(
            player,
            plugin.config.getLong("temporary-ignore.hide-after-millis", 5000L),
            UpdateReason.COMBAT
        )
    }

    @EventHandler
    fun onTakingDamage(event: EntityDamageByEntityEvent) {
        if (event.entity is Player) show(event.entity as Player)
        if (event.damager is Player) show(event.damager as Player)
    }
}
