# CelestialDash

Storm-powered movement and progression for Minecraft servers. During thunderstorms, players can obtain Celestial Tears, use them to perform fast aerial dashes, and craft a Celestial Amulet for emergency purification.

**Version:** 1.1.6
**License:** MIT
**Requirements:** Java 17 and a Spigot, Paper, or Purpur server running Minecraft 1.20 or newer.

## Features

- Storm-based Celestial Tear drops with per-player cooldowns, world blacklisting, and configurable ground or inventory delivery.
- Wind Dash: consume the Celestial Tear in your main hand to launch in the direction you are facing, with an optional world blacklist.
- Double Dash combo with configurable timing, second-dash multipliers, and temporary fall-damage immunity.
- Configurable regeneration, sounds, impact particles, and particle trails.
- Craftable Celestial Amulet that removes configurable harmful effects and extinguishes fire.
- Per-feature permissions for dashing, receiving Tears, and using the Amulet.
- Fully configurable player-facing messages, including command feedback.
- Optional PlaceholderAPI integration.
- Defensive configuration validation to keep invalid values within safe limits.

## Gameplay

### Celestial Tears

Once per second during a thunderstorm, each eligible online player is checked independently against the configured drop chance and that player's own cooldown. Hold a valid Celestial Tear in your main hand and right-click to dash. One Tear is consumed for every dash.

`drop-delivery: "GROUND"` drops the generated Tear near that eligible player. With `drop-delivery: "INVENTORY"`, the same player's generated Tear is added directly to their inventory instead; it is not chosen from the ground or from another player's inventory. If their inventory is full, the overflow is dropped at that player's location. This option affects storm-generated Tears only, not `/celestialdash give`.

If Double Dash is enabled, right-click again within the configured combo window to perform a second dash. The second dash grants the configured fall-damage immunity and uses the configured strength and lift multipliers. Administrators can blacklist worlds from dashing; `celestialdash.bypass-dash-blacklist` bypasses that restriction.

### Celestial Amulet

Craft the amulet with four Celestial Tears and one Netherite Ingot:

<img width="342" height="147" alt="imagen" src="https://github.com/user-attachments/assets/443ec236-e781-4380-9aca-80bf6a9e89b0" />

Hold the amulet in your main hand and right-click to remove configured effects. By default, it removes:

- Poison, Wither, Slowness, Weakness, Blindness, Hunger, Levitation, Darkness, Unluck, and Bad Omen
- Fire

Fire is always extinguished. The amulet only consumes a charge when it successfully purifies something. Its number of uses, cooldown, and potion-effect list are configurable. Every crafted amulet has a unique internal identity, so separate amulets do not stack.

## Configuration

All settings are in `plugins/CelestialDash/config.yml`. After editing the file, run `/celestialdash reload`.

Important settings include:

```yml
# Storm drops
drop-chance-per-second: 0.03
drop-cooldown-seconds: 60
drop-delivery: "GROUND" # GROUND or INVENTORY; overflow falls on the ground
drop-blacklist-worlds:
  - "world_nether"
  - "world_the_end"
give-max-amount: 2304

# Dash
dash-cooldown-seconds: 10
dash-blacklist-worlds: []
dash-strength: 1.8
dash-vertical-lift: 0.4
double-dash:
  strength-multiplier: 1.2
  lift-multiplier: 1.1

# Craftable amulet
celestial-amulet:
  enabled: true
  recipe-enabled: true
  uses: 3
  cooldown-seconds: 60
  purifiable-effects:
    - POISON
    - WITHER
```

`uses` applies to newly crafted amulets. Existing amulets keep their current remaining charges. Set `enabled` to `false` to stop amulet use, or `recipe-enabled` to `false` to remove its crafting recipe.

`drop-delivery` applies only to each eligible player's independently generated storm drop. Use `GROUND` to leave it at that player's location, or `INVENTORY` to add it directly to that player's inventory and safely drop any overflow.

Configuration values are safely limited by the plugin. For example, cooldowns allow 0 to 86,400 seconds, amulet uses allow 1 to 64, particle counts allow 0 to 500, and second-dash multipliers allow 0.0 to 10.0. Invalid amulet effect names are ignored with a warning; an empty `purifiable-effects` list disables potion-effect removal but still lets the amulet extinguish fire.

The `messages` section supports Minecraft `&` color codes and can be used to customize gameplay and command messages. Existing configurations are never auto-overwritten: on startup and reload, the plugin warns if the v1.1.6 resource-pack or CustomModelData keys are missing.

## Optional Resource Pack

The editable pack source is in [`resource-pack/CelestialDash-Resource-Pack`](resource-pack/CelestialDash-Resource-Pack). The release-ready archive is [`resource-pack/CelestialDash-Resource-Pack.zip`](resource-pack/CelestialDash-Resource-Pack.zip); do not extract it. It gives the Celestial Tear and Celestial Amulet their own 32x32 pixel-art textures without changing vanilla Ghast Tears or Nautilus Shells.

The plugin supports Minecraft 1.20 and newer, while the bundled resource pack is currently verified for clients from 1.20 through 1.21.3 and for 1.21.4+ through resource-pack format 88. Check the pack's own README before assuming compatibility with a future Minecraft format.

### Use the Bundled Pack with Google Drive

The default `config.yml` already contains the bundled pack's direct Google Drive URL, SHA-1, and model data values. Resource-pack delivery remains disabled by default.

To use the bundled pack on a new installation, open `plugins/CelestialDash/config.yml` and change only this option:

```yml
resource-pack:
  enabled: true
```

Then run `/celestialdash reload`. New players receive the request when they join; an administrator can resend it to an online player with `/celestialdash pack send <player>`. New Tears issued after the reload and newly crafted or reissued Amulets receive the custom appearance. Tears created with a previous `tear-custom-model-data` value must be reissued, because the configured model data is part of their strict item validation.

If you are upgrading from an older `config.yml`, add or update these bundled-pack values before enabling it:

```yml
tear-custom-model-data: 22001

resource-pack:
  enabled: true
  url: "https://drive.google.com/uc?export=download&id=1FXO5hS3Rg7FAUsn6Oq6ZdENKq0hBYS9C"
  sha1: "21ddb9da21e3f74b41514d662700a25c2b10a750"
  required: false
  prompt: "&bThis server uses the CelestialDash resource pack."

celestial-amulet:
  custom-model-data: 22002
```

`required: false` lets players decline the pack. After confirming that the direct URL works for your players, set it to `true` to disconnect players who decline it. The server console logs a warning when a player declines the CelestialDash request or its download fails; the plugin does not add a separate manual kick.

Before enabling the pack, open the direct URL in a private browser window. It must download the ZIP without requiring a Google sign-in or showing a Drive page. The configured SHA-1 belongs to the bundled ZIP and lets Minecraft reuse its cached copy instead of downloading it again when the file has not changed.

Minecraft applies server-sent packs automatically; players do not need to place them in their normal `resourcepacks` folder. In Minecraft 1.20.3 and newer, they are cached in the game directory's `downloads` folder. In GDLauncher, this is normally `instance/downloads` inside the selected instance folder.

### Host Your Own Version

You may host a modified pack at any public, direct-download HTTPS URL. Replace both `url` and `sha1` with values for the exact ZIP you host, then run `/celestialdash reload` before new players join. Keep `required: false` while testing a new URL or pack revision.

## Commands

| Command | Permission | Description |
| --- | --- | --- |
| `/celestialdash give <player> <amount>` | `celestialdash.admin` | Gives valid Celestial Tears to an online player. Any overflow is dropped at the player's location. |
| `/celestialdash pack send <player>` | `celestialdash.admin` | Resends the enabled, valid resource-pack request to an online player. |
| `/celestialdash reload` | `celestialdash.admin` | Reloads the configuration, messages, item settings, amulet recipe, and resource-pack settings for future joins. |

Aliases: `/cdash` and `/celestial`.

Admin command tab completion is available for subcommands, online players, and common amounts.

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `celestialdash.use` | Everyone | Allows using Celestial Tears to dash. |
| `celestialdash.bypass-dash-blacklist` | OP | Allows dashing in worlds listed in `dash-blacklist-worlds`. |
| `celestialdash.receive` | Everyone | Allows receiving storm-generated Celestial Tears. |
| `celestialdash.amulet` | Everyone | Allows using the Celestial Amulet. |
| `celestialdash.admin` | OP | Allows `/celestialdash give`, `/celestialdash pack send`, and `/celestialdash reload`. |
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

## What's New in 1.1.6

- Added an optional bundled resource pack with custom 32x32 pixel-art models for Celestial Tears and Celestial Amulets. It uses the reserved CustomModelData values `22001` and `22002` while leaving vanilla Ghast Tears and Nautilus Shells unchanged.
- Added `celestial-amulet.custom-model-data` so newly crafted or reissued Amulets can use the bundled model; the bundled value is preconfigured while resource-pack delivery remains disabled by default.
- Added optional server-managed resource-pack delivery with configurable URL, SHA-1, required status, prompt text, and a documented Google Drive setup.
- Added `/celestialdash pack send <player>` to resend an enabled resource pack to an online player, plus console logging for pack declines and download failures.
- Added explicit warnings for pre-v1.1.6 configuration files that lack the resource-pack or CustomModelData keys, without overwriting existing configuration.
- Added configurable Celestial Amulet potion effects; fire purification remains enabled independently.
- Added configurable storm-drop delivery to the ground or the independently selected eligible player's inventory, with safe ground overflow handling.
- Added a dash world blacklist with an OP bypass permission, plus configurable second-dash strength and lift multipliers.
- Fixed dashing to consume the Celestial Tear in the player's main hand rather than a different inventory stack.
- Added `give-max-amount` to cap `/celestialdash give` from 1 to 2,304, with an invalid-amount message that shows the configured maximum.
- Cleans temporary storm-drop, amulet, double-dash, fall-immunity, and particle-trail state when players disconnect, while intentionally retaining the normal dash cooldown across reconnects.
- Ensures only one particle-trail task runs for each player, replacing an existing trail and cancelling it on player disconnect or plugin disable.
- Hardened numeric configuration validation for non-negative settings.
- Added the Maven Wrapper, MockBukkit gameplay integration tests, and GitHub Actions verification with Java 17.

## License

CelestialDash is released under the [MIT License](LICENSE).
