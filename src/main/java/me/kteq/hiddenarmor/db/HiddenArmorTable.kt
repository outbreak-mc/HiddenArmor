package me.kteq.hiddenarmor.db

import org.jetbrains.exposed.dao.id.IntIdTable

object HiddenArmorTable : IntIdTable() {
    val uuid = varchar("uuid", 36).uniqueIndex()
    val name = varchar("name", 64).uniqueIndex()
}