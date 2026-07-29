# CelestialDash

Storm-powered movement and progression for Minecraft servers. During thunderstorms, players can obtain Celestial Tears, use them to perform fast aerial dashes, and craft a Celestial Amulet for emergency purification.

**Version:** 1.1.5
**License:** MIT
**Requirements:** Java 17 and a Spigot, Paper, or Purpur server running Minecraft 1.20 or newer.

## Features

- Storm-based Celestial Tear drops with per-player cooldowns and world blacklisting.
- Wind Dash: consume a Celestial Tear to launch in the direction you are facing.
- Double Dash combo with configurable timing and temporary fall-damage immunity.
- Configurable regeneration, sounds, impact particles, and particle trails.
- Craftable Celestial Amulet that removes harmful effects and extinguishes fire.
- Per-feature permissions for dashing, receiving Tears, and using the Amulet.
- Fully configurable player-facing messages, including command feedback.
- Optional PlaceholderAPI integration.
- Defensive configuration validation to keep invalid values within safe limits.

## Gameplay

### Celestial Tears

Celestial Tears can drop near eligible players during thunderstorms. Hold a valid Celestial Tear in your main hand and right-click to dash. One Tear is consumed for every dash.

If Double Dash is enabled, right-click again within the configured combo window to perform a second dash. The second dash grants the configured fall-damage immunity.

### Celestial Amulet

Craft the amulet with four Celestial Tears and one Netherite Ingot:

```text
Empty          | Celestial Tear | Empty
Celestial Tear | Netherite Ingot | Celestial Tear
Empty          | Celestial Tear | Empty
```

Hold the amulet in your main hand and right-click to remove these effects:

- Poison, Wither, Slowness, Weakness, Blindness, Hunger, Levitation, Darkness, Unluck, and Bad Omen
- Fire

The amulet only consumes a charge when it successfully purifies something. Its number of uses and cooldown are configurable. Every crafted amulet has a unique internal identity, so separate amulets do not stack.

## Configuration

All settings are in `plugins/CelestialDash/config.yml`. After editing the file, run `/celestialdash reload`.

Important settings include:

```yml
# Storm drops
drop-chance-per-second: 0.03
drop-cooldown-seconds: 60
drop-blacklist-worlds:
  - "world_nether"
  - "world_the_end"

# Dash
dash-cooldown-seconds: 10
dash-strength: 1.8
dash-vertical-lift: 0.4

# Craftable amulet
celestial-amulet:
  enabled: true
  recipe-enabled: true
  uses: 3
  cooldown-seconds: 60
```

`uses` applies to newly crafted amulets. Existing amulets keep their current remaining charges. Set `enabled` to `false` to stop amulet use, or `recipe-enabled` to `false` to remove its crafting recipe.

Configuration values are safely limited by the plugin. For example, cooldowns allow 0 to 86,400 seconds, amulet uses allow 1 to 64, and particle counts allow 0 to 500.

The `messages` section supports Minecraft `&` color codes and can be used to customize gameplay and command messages.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/celestialdash give <player> <amount>` | `celestialdash.admin` | Gives valid Celestial Tears to an online player. Any overflow is dropped at the player's location. |
| `/celestialdash reload` | `celestialdash.admin` | Reloads the configuration, messages, item settings, and amulet recipe. |

Aliases: `/cdash` and `/celestial`.

Admin command tab completion is available for subcommands, online players, and common amounts.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `celestialdash.use` | Everyone | Allows using Celestial Tears to dash. |
| `celestialdash.receive` | Everyone | Allows receiving storm-generated Celestial Tears. |
| `celestialdash.amulet` | Everyone | Allows using the Celestial Amulet. |
| `celestialdash.admin` | OP | Allows `/celestialdash give` and `/celestialdash reload`. |
| `celestialdash.*` | OP | Grants every CelestialDash permission. |

## PlaceholderAPI

PlaceholderAPI is optional. When it is installed, CelestialDash registers these placeholders:

| Placeholder | Result |
| --- | --- |
| `%celestialdash_tears%` | Number of valid Celestial Tears in the player's inventory. |
| `%celestialdash_cooldown%` | Remaining dash cooldown in seconds. |
| `%celestialdash_double_ready%` | `true` while the player can perform the second dash; otherwise `false`. |

## Item Security and Migration

Celestial Tears and Celestial Amulets use internal Persistent Data Container markers. Renaming a normal Ghast Tear or Nautilus Shell does not turn it into a plugin item.

Tears created before 1.1.5 do not have the new internal marker and are not recognized by this version. Replace them with new storm drops or issue new Tears with `/celestialdash give`.

## What's New in 1.1.5

- Added the craftable, configurable Celestial Amulet.
- Added a Netherite Ingot amulet recipe and configurable uses and cooldown.
- Added `celestialdash.amulet`, `celestialdash.use`, and `celestialdash.receive` permission checks.
- Added internal item markers to prevent renamed vanilla items from being used as Celestial Tears or Amulets.
- Added configurable command, permission, and amulet messages.
- Added command tab completion and safe `/give` inventory-overflow handling.
- Added configuration bounds validation and warnings for invalid values.
- Fixed cooldown feedback to round up correctly while any cooldown time remains.
- Prevented duplicate off-hand interaction handling and cleaned expired player state automatically.
- Added unit tests for configuration clamping and cooldown rounding.

## Building

Build with Maven:

```bash
mvn clean package
```

The plugin JAR is created in `target/CelestialDash-1.1.5.jar`.

## License

CelestialDash is released under the [MIT License](LICENSE).
