# HiddenArmor

This is a reworked version of [Kteq1's HiddenArmor plugin](https://github.com/Kteq1/HiddenArmor) that allows players to
hide their armor so their skins can be visible to everyone.

Rework contains better configuration with MiniMessage format support, commands, permissions and data storage, supports
modern Minecraft versions, and it was partially rewritten in Kotlin.

[ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) & [CommandAPI](https://modrinth.com/plugin/commandapi)
plugins are required!

Tested on Paper 1.21.3

### Commands

- `/hiddenarmor togglearmor [show/hide/toggle] [player]` - hides or shows your own or other player's armor.
- `/hiddenarmor reload` - reloads plugin configuration.

### Permissions

- `hiddenarmor.toggle` - hide or show own armor
- `hiddenarmor.toggle.other` - hide or show other player's armor
- `hiddenarmor.bypasscooldown` - toggle armor without cooldown
- `hiddenarmor.reload` - reload plugin configuration