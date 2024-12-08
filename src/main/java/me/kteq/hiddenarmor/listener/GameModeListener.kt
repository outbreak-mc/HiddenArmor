package me.kteq.hiddenarmor.listener

import me.kteq.hiddenarmor.HiddenArmorPlugin
import me.kteq.hiddenarmor.UpdateReason
import me.kteq.hiddenarmor.util.EventUtil
import org.bukkit.GameMode
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerGameModeChangeEvent

class GameModeListener(var plugin: HiddenArmorPlugin) : Listener {
    init {
        EventUtil.register(this, plugin)
    }

    @EventHandler
    fun onGameModeChange(event: PlayerGameModeChangeEvent) {
        if (!plugin.hiddenArmorManager.isPlayerArmorHidden(event.player)) return
        if (event.newGameMode == GameMode.CREATIVE || event.player.gameMode == GameMode.CREATIVE) {
            plugin.temporaryIgnoreService.free(event.player.uniqueId)
            // На данный момент режим игры ещё не изменился на креатив, так что
            // isArmorHidden будет возвращать true. Нам же нужно прямо сейчас скрыть броню,
            // при этом не изменяя настройки игрока. Сделаем это через temporaryDisableHiding().
            // Уже через 1 тик режим игры сменится и isArmorHidden будет возвращать false для креатива.
            plugin.temporaryIgnoreService.temporaryDisableHiding(event.player, 1L, UpdateReason.GAMEMODE)
        }
    }
}
