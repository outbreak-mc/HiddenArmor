package me.kteq.hiddenarmor.listener

import me.kteq.hiddenarmor.HiddenArmorPlugin
import me.kteq.hiddenarmor.UpdateReason
import me.kteq.hiddenarmor.util.EventUtil
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.PlayerInventory

class InventoryShiftClickListener(
    private val plugin: HiddenArmorPlugin
) : Listener {
    init {
        EventUtil.register(this, plugin)
    }

    @EventHandler
    fun onShiftClickArmor(event: InventoryClickEvent) {
        if (!plugin.hiddenArmorManager.shouldCurrentlyHideArmor((event.whoClicked as Player))) return
        if (event.clickedInventory !is PlayerInventory) return
        if (!event.isShiftClick) return
        val player = event.whoClicked as Player
        val inv = player.inventory
        val armor = event.currentItem ?: return

        if (armor.type.toString().endsWith("_HELMET") && inv.helmet == null || (armor.type.toString()
                .endsWith("_CHESTPLATE") || armor.type == Material.ELYTRA) && inv.chestplate == null || armor.type.toString()
                .endsWith("_LEGGINGS") && inv.leggings == null || armor.type.toString()
                .endsWith("_BOOTS") && inv.boots == null
        ) {
            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                plugin.packetHandler.updatePlayer(player, UpdateReason.INVENTORY_SHIFT_CLICK)
            }, 1L)
        }
    }
}
