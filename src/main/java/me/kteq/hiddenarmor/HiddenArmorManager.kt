package me.kteq.hiddenarmor

import me.kteq.hiddenarmor.db.HiddenArmorDB
import org.bukkit.GameMode
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Predicate

class HiddenArmorManager(
    private val plugin: HiddenArmorPlugin
) {
    private var enabledPlayersUUID: MutableSet<UUID> = HashSet()
    private val forceDisablePredicates: MutableSet<Predicate<Player>> = HashSet()
    private val forceEnablePredicates: MutableSet<Predicate<Player>> = HashSet()

    init {
        registerDefaultPredicates()
    }

    val db: HiddenArmorDB get() = plugin.db

    enum class OperationStatus {
        SUCCESS,
        UNKNOWN_PLAYER,
        NOTHING_CHANGED
    }

    /**
     * Производит запрос в базу данных, проверяя, включено ли скрытие брони
     * для игрока с переданным uuid, и на основе этого обновляет кэш плагина.
     *
     * Запрос выполняется в текущем потоке, так что следует выполнять этот
     * метод в отдельном потоке.
     * */
    fun loadPlayerToCache(uuid: UUID) {
        if (plugin.hiddenArmorManager.db.isHidden(uuid)) {
            enabledPlayersUUID.add(uuid)
        } else {
            enabledPlayersUUID.remove(uuid)
        }
    }

    /**
     * Убирает игрока из кэша плагина. Следует вызывать
     * этот метод при выходе игрока с сервера.
     * */
    fun removePlayerForCache(uuid: UUID) {
        enabledPlayersUUID.remove(uuid)
    }

    /**
     * Включает или выключает отображение брони для игрока.
     *
     * @param player целевой игрок
     * @param status true - скрывать броню, false - показывать броню
     * */
    fun setPlayerArmorHiddenStatus(player: OfflinePlayer, status: Boolean): CompletableFuture<OperationStatus> {
        val notAlready = db.isHidden(player.uniqueId) != status
        if (notAlready) {
            if (status) {
                enabledPlayersUUID.add(player.uniqueId)
            } else {
                enabledPlayersUUID.remove(player.uniqueId)
            }
            if (player is Player)
                plugin.packetHandler.updatePlayer(player, UpdateReason.MANUAL)
        }
        return CompletableFuture.supplyAsync {
            if (notAlready) {
                val name = player.name

                if (status) {
                    if (name == null)
                        return@supplyAsync OperationStatus.UNKNOWN_PLAYER
                    db.insert(player.uniqueId, name)
                } else {
                    db.delete(player.uniqueId)
                }
            } else {
                return@supplyAsync OperationStatus.NOTHING_CHANGED
            }
            return@supplyAsync OperationStatus.SUCCESS
        }
    }

    /**
     * Возвращает true, если у игрока включено скрытие брони.
     *
     * Иногда броня может отображаться вне зависимости от этого статуса,
     * например в креативе. Чтобы узнать более точный текущий статус отображения,
     * следует использовать shouldCurrentlyHideArmor()
     * */
    fun isPlayerArmorHidden(player: OfflinePlayer): Boolean {
        return enabledPlayersUUID.contains(player.uniqueId)
    }

    /**
     * Возвращает true, если броня на данный момент скрыта, но, в
     * отличие от isPlayerArmorHidden, учитывает принудительные
     * факторы для скрытия или отображения брони (так, например, броня всегда
     * показывается в креативе).
     * */
    fun shouldCurrentlyHideArmor(player: Player): Boolean {
        if (player.gameMode == GameMode.CREATIVE) return false
        var hidden = isPlayerArmorHidden(player)
        if (hidden) for (predicate in forceDisablePredicates) {
            if (predicate.test(player)) {
                hidden = false
                break
            }
        }
        for (predicate in forceEnablePredicates) {
            if (predicate.test(player)) {
                hidden = true
                break
            }
        }
        return hidden
    }

    private fun registerDefaultPredicates() {
        val config = plugin.config
        val hideWhenInvisible = config.getBoolean("invisibility-potion.always-hide-gear")
        forceDisablePredicates.add(Predicate { player: Player -> player.isInvisible && !hideWhenInvisible })
        forceDisablePredicates.add(Predicate { player: Player ->
            plugin.temporaryIgnoreService.isHidingTemporaryDisabled(
                player.uniqueId
            )
        })
        forceEnablePredicates.add(Predicate { player: Player -> player.isInvisible && hideWhenInvisible })
    }
}
