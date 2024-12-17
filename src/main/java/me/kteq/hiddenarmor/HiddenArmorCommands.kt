package me.kteq.hiddenarmor

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.SuggestionInfo
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.BooleanArgument
import dev.jorel.commandapi.arguments.MultiLiteralArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.executors.CommandExecutor
import dev.jorel.commandapi.executors.PlayerCommandExecutor
import me.kteq.hiddenarmor.db.HiddenArmorDB
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.CompletableFuture

class HiddenArmorCommands(
    val plugin: HiddenArmorPlugin
) {
    private val manager: HiddenArmorManager get() = plugin.hiddenArmorManager
    private val cooldowns = mutableMapOf<UUID, Long>()
    private val cooldownMillis = 3000L

    companion object {
        const val HIDE = "hide"
        const val SHOW = "show"
        const val TOGGLE = "toggle"
    }

    private fun setStatus(sender: Player, targetName: String, statusStr: String, silent: Boolean) {
        val target = Bukkit.getOfflinePlayer(targetName)

        val shouldHide = when (statusStr) {
            HIDE -> true
            SHOW -> false
            else -> !manager.isPlayerArmorHidden(target)
        }

        if (!target.hasPlayedBefore()) {
            Locale.UNKNOWN_PLAYER.send(sender, "player" to targetName)
            return
        }

        if (!sender.hasPermission(Permission.BYPASS_COOLDOWN)) {
            cooldowns.entries.removeIf { System.currentTimeMillis() - it.value > cooldownMillis }
            if (cooldowns[sender.uniqueId] != null) {
                Locale.COMMAND_DELAY.send(sender)
                return
            }
            cooldowns[sender.uniqueId] = System.currentTimeMillis()
        }

        manager.setPlayerArmorHiddenStatus(target, shouldHide).thenAccept {
            if (it == HiddenArmorManager.OperationStatus.UNKNOWN_PLAYER) {
                Locale.UNKNOWN_PLAYER.send(sender, "player" to targetName)
                return@thenAccept
            } else if (it == HiddenArmorManager.OperationStatus.NOTHING_CHANGED) {
                Locale.NOTHING_CHANGED.send(sender, "player" to targetName)
                return@thenAccept
            }

            val statusComponent: Component by lazy {
                return@lazy (if (manager.isPlayerArmorHidden(target))
                    Locale.VISIBILITY__HIDDEN
                else
                    Locale.VISIBILITY__SHOWN).comp()
            }

            val onlineTarget = if (target.name != null) Bukkit.getPlayer(target.name!!) else null
            if (target.uniqueId != sender.uniqueId) {
                Locale.VISIBILITY__FOR_OTHER.send(
                    sender,
                    "visibility" to statusComponent,
                    "player" to targetName
                )

                if (onlineTarget != null && !onlineTarget.hasPermission(Permission.TOGGLE)) {
                    Locale.ARMOR_WILL_REAPPEAR_PERMISSION_WARNING.send(sender, "player" to onlineTarget.name)
                }
            }

            if (!silent && onlineTarget != null) {
                Locale.VISIBILITY__FOR_SELF.sendActionBar(onlineTarget, "visibility" to statusComponent)
            }
        }
    }

    private val db: HiddenArmorDB get() = plugin.hiddenArmorManager.db

    init {
        val targetArg = StringArgument("target").replaceSuggestions(
            ArgumentSuggestions.stringsAsync { info: SuggestionInfo<CommandSender> ->
                CompletableFuture.supplyAsync {
                    val out = mutableListOf<String>()
                    when (info.previousArgs[info.previousArgs.count() - 1]) {
                        SHOW -> {
                            for (row in db.search("%${info.currentArg.replace("_", "\\_")}%"))
                                out.add(row.value)
                        }

                        HIDE -> {
                            val uuids = Bukkit.getOnlinePlayers().stream().map { it.uniqueId }.toList()
                            val uuidsWithHiddenArmorFromOnlinePlayers = db.isHidden(uuids)
                            for (player in Bukkit.getOnlinePlayers()) {
                                if (player.uniqueId !in uuidsWithHiddenArmorFromOnlinePlayers) {
                                    out.add(player.name)
                                }
                            }
                        }

                        TOGGLE -> {
                            out.addAll(Bukkit.getOnlinePlayers().stream().map { it.name }.toList())
                            for (row in db.search("%${info.currentArg.replace("_", "\\_")}%"))
                                out.add(row.value)
                        }
                    }
                    return@supplyAsync out.toTypedArray()
                }
            }
        )

        val toggleArmorCommand = CommandAPICommand("togglearmor")
            .withPermission(Permission.TOGGLE)
            .withOptionalArguments(
                MultiLiteralArgument("status", SHOW, HIDE, TOGGLE),
                targetArg.withPermission(Permission.TOGGLE_OTHER),
                BooleanArgument("silent").withPermission(Permission.TOGGLE_OTHER)
            )
            .executesPlayer(PlayerCommandExecutor { sender, args ->
                setStatus(
                    sender,
                    args.getOptional("target").orElse(sender.name) as String,
                    args.getOptional("status").orElse(TOGGLE) as String,
                    args.getOptional("silent").orElse(false) as Boolean
                )
            })

        CommandAPICommand("hiddenarmor")
            .withSubcommands(
                CommandAPICommand("reload")
                    .withPermission(Permission.RELOAD)
                    .executes(CommandExecutor { sender, _ ->
                        plugin.reload()
                        Locale.RELOADED.send(sender)
                    }),
                toggleArmorCommand
            )
            .register()

        toggleArmorCommand.register()
    }
}