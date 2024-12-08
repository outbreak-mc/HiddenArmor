package me.kteq.hiddenarmor.handler

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot
import com.comphenix.protocol.wrappers.Pair
import me.kteq.hiddenarmor.HiddenArmorPlugin
import me.kteq.hiddenarmor.UpdateReason
import me.kteq.hiddenarmor.util.ProtocolUtil
import org.bukkit.entity.Player

class ArmorPacketHandler(
    private val plugin: HiddenArmorPlugin,
    private var protocolManager: ProtocolManager
) {
    fun updatePlayer(player: Player, reason: UpdateReason) {
        updateSelf(player)
        updateOthers(player)
//        plugin.logger.info("updatePlayer: ${reason}")
    }

    private fun updateSelf(player: Player) {
        val inv = player.inventory
        for (i in 5..8) {
            val packetSelf = protocolManager.createPacket(PacketType.Play.Server.SET_SLOT)
            packetSelf.integers.write(0, 0)
            if (!plugin.isOld) packetSelf.integers.write(2, i) else packetSelf.integers.write(1, i)
            val armor = ProtocolUtil.getArmor(ProtocolUtil.ArmorType.getType(i), inv)
            packetSelf.itemModifier.write(0, armor)
            protocolManager.sendServerPacket(player, packetSelf)
        }
    }

    private fun updateOthers(player: Player) {
        val inv = player.inventory
        val packetOthers = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT)
        packetOthers.integers.write(0, player.entityId)
        val pairList = packetOthers.slotStackPairLists.read(0)
        pairList.add(Pair(ItemSlot.HEAD, ProtocolUtil.getArmor(ProtocolUtil.ArmorType.HELMET, inv)))
        pairList.add(Pair(ItemSlot.CHEST, ProtocolUtil.getArmor(ProtocolUtil.ArmorType.CHEST, inv)))
        pairList.add(Pair(ItemSlot.LEGS, ProtocolUtil.getArmor(ProtocolUtil.ArmorType.LEGGS, inv)))
        pairList.add(Pair(ItemSlot.FEET, ProtocolUtil.getArmor(ProtocolUtil.ArmorType.BOOTS, inv)))
        pairList.add(Pair(ItemSlot.MAINHAND, player.inventory.itemInMainHand.clone()))
        pairList.add(Pair(ItemSlot.OFFHAND, player.inventory.itemInOffHand.clone()))
        packetOthers.slotStackPairLists.write(0, pairList)
        ProtocolUtil.broadcastPlayerPacket(protocolManager, packetOthers, player)
    }
}
