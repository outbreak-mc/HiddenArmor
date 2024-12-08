package me.kteq.hiddenarmor.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture

class HiddenArmorDB(
    val file: File
) {
    lateinit var db: Database
    fun init() {
        db = Database.connect("jdbc:sqlite:${file.absolutePath}", driver = "org.sqlite.JDBC")
        transaction(db) {
            SchemaUtils.createMissingTablesAndColumns(HiddenArmorTable)
        }
    }

    fun search(pattern: String): Map<UUID, String> {
        return transaction(db) {
            val out = mutableMapOf<UUID, String>()
            val q = HiddenArmorTable.select {
                HiddenArmorTable.name like pattern
            }
            for (row in q)
                out[UUID.fromString(row[HiddenArmorTable.uuid])] = row[HiddenArmorTable.name]

            return@transaction out
        }
    }

    fun isHidden(uuids: List<UUID>): Set<UUID> {
        val out = mutableSetOf<UUID>()
        val uuidsAsStrings = uuids.stream().map { it.toString() }.toList()
        transaction(db) {
            val q = HiddenArmorTable.select {
                HiddenArmorTable.uuid inList uuidsAsStrings
            }
            for (row in q) {
                if (uuidsAsStrings.contains(row[HiddenArmorTable.uuid]))
                    out.add(UUID.fromString(row[HiddenArmorTable.uuid]))
            }
        }
        return out
    }

    fun isHidden(uuid: UUID): Boolean {
        return transaction(db) {
            return@transaction !HiddenArmorTable.select {
                HiddenArmorTable.uuid eq uuid.toString()
            }.empty()
        }
    }

    fun insert(uuid: UUID, name: String) {
        transaction(db) {
            HiddenArmorTable.insert {
                it[HiddenArmorTable.uuid] = uuid.toString()
                it[HiddenArmorTable.name] = name
            }
        }
    }

    fun delete(uuid: UUID): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync {
            transaction(db) {
                HiddenArmorTable.deleteWhere { HiddenArmorTable.uuid eq uuid.toString() }
            }
        }
    }

    fun delete(name: String): CompletableFuture<Unit> {
        return CompletableFuture.supplyAsync {
            transaction(db) {
                HiddenArmorTable.deleteWhere { HiddenArmorTable.name eq name }
            }
        }
    }
}