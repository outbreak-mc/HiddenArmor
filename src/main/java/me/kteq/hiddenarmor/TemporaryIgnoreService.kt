package me.kteq.hiddenarmor

import me.kteq.hiddenarmor.handler.ArmorPacketHandler
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitTask
import java.util.*

class TemporaryIgnoreService(
    private val plugin: HiddenArmorPlugin,
    private val packetHandler: ArmorPacketHandler,
) {
    private data class Delay(
        val endTime: Long = 0,
        var task: BukkitTask
    )

    private val playerDelays: MutableMap<UUID, Delay> = mutableMapOf()

    fun temporaryDisableHiding(player: Player, delayMillis: Long, reason: UpdateReason) {
        val endTime = System.currentTimeMillis() + delayMillis
        val existing = playerDelays[player.uniqueId]
        if (existing != null) {
            if (existing.endTime >= endTime) return
            if (!existing.task.isCancelled)
                existing.task.cancel()
        }
        playerDelays[player.uniqueId] = Delay(
            endTime,
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                playerDelays.remove(player.uniqueId)
                packetHandler.updatePlayer(player, reason)
            }, delayMillis / 50L)
        )
        if (existing == null)
            packetHandler.updatePlayer(player, reason)
    }

    fun free(uuid: UUID) {
        val existing = playerDelays[uuid]
        if (existing != null) {
            if (!existing.task.isCancelled)
                existing.task.cancel()
            playerDelays.remove(uuid)
        }
    }

    fun isHidingTemporaryDisabled(uuid: UUID): Boolean {
        val existing = playerDelays[uuid] ?: return false
        return existing.endTime > System.currentTimeMillis()
    }
}
