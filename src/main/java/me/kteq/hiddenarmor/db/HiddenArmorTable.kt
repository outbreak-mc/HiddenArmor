package me.kteq.hiddenarmor.db

import org.jetbrains.exposed.dao.id.UUIDTable

object HiddenArmorTable : UUIDTable() {
    val name = varchar("name", 64).uniqueIndex()
}