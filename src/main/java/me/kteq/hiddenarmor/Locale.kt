package me.kteq.hiddenarmor

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.ParserDirective
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import org.apache.commons.text.StringSubstitutor
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration

enum class Locale {
    VISIBILITY__FOR_SELF,
    VISIBILITY__FOR_OTHER,
    VISIBILITY__HIDDEN,
    VISIBILITY__SHOWN,
    RELOADED,
    UNKNOWN_PLAYER,
    NOTHING_CHANGED,
    COMMAND_DELAY,
    ARMOR_WILL_REAPPEAR_PERMISSION_WARNING
    ;

    private val key: String = toString().replace("__", ".").replace("_", "-").lowercase()

    fun comp(vararg replacing: Pair<String, Any>): Component {
        return process(config.getString(key, key)!!, *replacing)
    }

    fun send(audience: Audience) {
        audience.sendMessage(comp())
    }

    fun send(audience: Audience, vararg replacing: Pair<String, Any>) {
        audience.sendMessage(comp(*replacing))
    }

    fun sendActionBar(audience: Audience, vararg replacing: Pair<String, Any>) {
        audience.sendActionBar(comp(*replacing))
    }

    companion object {
        val defaultColorResolvers = mutableListOf<TagResolver.Single>()
        val defaultFormatResolvers = mutableListOf(
            TagResolver.resolver("r", ParserDirective.RESET)
        )

        private fun constructDefaultSerializer(customColorTags: Map<String, String>): MiniMessage {
            defaultColorResolvers.clear()

            customColorTags.forEach { key, value ->
                val color = TextColor.fromCSSHexString(value)
                if (color != null) {
                    defaultColorResolvers.add(TagResolver.resolver(key, Tag.styling(color)))
                }
            }

            return MiniMessage.builder()
                .tags(
                    TagResolver.builder()
                        .resolvers(StandardTags.defaults())
                        .resolvers(Locale.defaultFormatResolvers)
                        .resolvers(Locale.defaultColorResolvers)
                        .build()
                ).build()
        }

        var mm: MiniMessage = constructDefaultSerializer(HashMap())
            private set

        private lateinit var config: FileConfiguration
        var papi = false

        val placeholders = mutableMapOf<String, String>()

        fun load(config: YamlConfiguration, papi: Boolean, customColorTags: Map<String, String>) {
            Companion.config = config
            Companion.papi = papi
            mm = constructDefaultSerializer(customColorTags)

            val placeholdersSection = config.getConfigurationSection("placeholders")
            if (placeholdersSection != null) {
                for (p in placeholdersSection.getKeys(false)) {
                    placeholders[p] = placeholdersSection.getString(p) ?: "null"
                }
            }
        }

        private fun replaceAll(str: String, map: Map<String, Any>): String {
            val substitutor = StringSubstitutor(map, "%", "%", '\\')
            return substitutor.replace(str)
        }

        /**
         * Парсит строку формата MiniMessage в компонент.
         *
         * @param text строка для перевода в компонент
         * @param replacing плейсхолдеры для замены, где ключ - имя плейсхолдера
         *  без %. Значение - Component либо любой другой объект. Компоненты будут
         *  вставлены, используя TextReplacementConfig (медленно),
         *  объекты любого другого типа - просто переведены в строку и
         *  заменены (оптимизированно)
         * */
        fun process(text: String, vararg replacing: Pair<String, Any>): Component {
            val mapComps = mutableMapOf<String, Component>()
            val mapStrings = HashMap<String, Any>(placeholders)

            for (pair in replacing) {
                if (pair.second is Component)
                    mapComps[pair.first] = pair.second as Component
                else
                    mapStrings[pair.first] = pair.second
            }

            var comp = mm.deserialize(replaceAll(text, mapStrings))

            for (entry in mapComps.iterator()) {
                comp = comp.replaceText(
                    TextReplacementConfig.builder()
                        .matchLiteral("%${entry.key}%")
                        .replacement(entry.value)
                        .build()
                )
            }
            return comp
        }
    }
}

