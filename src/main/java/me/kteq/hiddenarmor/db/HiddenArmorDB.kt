package me.kteq.hiddenarmor.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*

class HiddenArmorDB(
    file: File,
) {
    private val db: Database = Database.connect("jdbc:sqlite:${file.absolutePath}", driver = "org.sqlite.JDBC")

    fun init() {
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(HiddenArmorTable)
        }
    }

    fun search(pattern: String): Map<UUID, String> {
        return transaction(db) {
            val out = mutableMapOf<UUID, String>()
            val q = HiddenArmorTable.selectAll().where { HiddenArmorTable.name like pattern }
            for (row in q)
                out[row[HiddenArmorTable.id].value] = row[HiddenArmorTable.name]

            return@transaction out
        }
    }

    fun isHidden(uuids: List<UUID>): Set<UUID> {
        return transaction(db) {
            HiddenArmorTable.selectAll().where { HiddenArmorTable.id inList uuids }
        }.map { it[HiddenArmorTable.id].value }.toSet()
    }

    fun isHidden(uuid: UUID): Boolean {
        return transaction(db) {
            return@transaction !HiddenArmorTable.selectAll().where { HiddenArmorTable.id eq uuid }.empty()
        }
    }

    fun insert(uuid: UUID, name: String) {
        transaction(db) {
            HiddenArmorTable.insert {
                it[HiddenArmorTable.id] = uuid
                it[HiddenArmorTable.name] = name
            }
        }
    }

    fun delete(uuid: UUID) {
        transaction(db) {
            HiddenArmorTable.deleteWhere { HiddenArmorTable.id eq uuid }
        }
    }

    fun delete(name: String) {
        transaction(db) {
            HiddenArmorTable.deleteWhere { HiddenArmorTable.name eq name }
        }
    }
}