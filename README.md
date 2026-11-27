# CelestialDash

Storm-powered movement and progression for Minecraft servers. During thunderstorms, players can obtain Celestial Tears,
use them to perform fast aerial dashes, and craft a Celestial Amulet for emergency purification.

**Version:** 1.1.8

**License:** MIT

**Requirements:** Java 17 and a Spigot, Paper, or Purpur server running Minecraft 1.20 or newer.

## Roadmap

See [the development roadmap](ROADMAP.md) for planned releases and [the 1.1.8 release notes](releases/1.1.8.md) for the changes in this build.

The [1.1.8 validation report](docs/validation-1.1.8.md) records the real-server crafting, WorldGuard and mechanics checks on Paper 1.20 and 26.3.

## Features

- Storm-based Celestial Tear drops with per-player cooldowns, world blacklisting, and configurable ground or inventory
  delivery.
- A localized, one-time in-game Chronicle for every player after upgrading to 1.1.7, with player recovery and admin
  reissue commands for replacement copies.
- Wind Dash: consume the Celestial Tear in your main hand for a horizontal impulse based on your look direction and fixed vertical lift, with an optional
  world blacklist.
- Double Dash combo with configurable timing, second-dash multipliers, and temporary fall-damage immunity.
- Configurable regeneration, sounds, impact particles, and particle trails.
- Craftable Celestial Amulet that removes configurable harmful effects and extinguishes fire.
- Per-feature permissions for dashing, receiving Tears, and using the Amulet.
- Fully configurable player-facing messages, including command feedback.
- Optional PlaceholderAPI integration with a short-lived, bounded Tear-count cache for scoreboards and tab lists.
- Defensive configuration validation to keep invalid values within safe limits.

## Gameplay

### Celestial Tears

Once per second during a thunderstorm, each eligible online player is checked independently against the configured drop
chance and that player's own cooldown. Ordinary rain without thunder does not generate Celestial Tears.
Hold a valid Celestial Tear in your main hand and right-click to dash. One Tear
is consumed for every dash.

The dash always sets vertical velocity to `dash-vertical-lift`, regardless of whether you look up or down.
The horizontal impulse follows your look direction: looking level gives the full `dash-strength`, tilting up or down
reduces horizontal speed, and looking straight up or down gives effectively no horizontal movement. Looking down
does not produce a downward dash. The second dash uses your current look direction and applies the configured
strength and lift multipliers to those same rules. This preserves the existing movement behavior.

`drop-delivery: "GROUND"` drops the generated Tear near that eligible player. With `drop-delivery: "INVENTORY"`, the
same player's generated Tear is added directly to their inventory instead; it is not chosen from the ground or from
another player's inventory. If their inventory is full, the overflow is dropped at that player's location. This option
affects storm-generated Tears only, not `/celestialdash give`.

If Double Dash is enabled, right-click again within the configured combo window to perform a second dash. The second
dash grants the configured fall-damage immunity and uses the configured strength and lift multipliers. Administrators
can blacklist worlds from dashing; `celestialdash.bypass-dash-blacklist` bypasses that restriction.

The second dash is allowed through the end of the combo window, even while the normal cooldown is active.
After it, further clicks are blocked until the cooldown measured from the second dash expires; blocked clicks
do not consume Tears. Its fall protection covers only the next fall-damage event within the configured duration,
not other damage types. The first dash does not grant this protection.

Disconnecting, dying, changing worlds, or successfully teleporting to a different position cancels the combo,
fall protection, and active particle trail, while retaining the normal dash cooldown. Cancelled teleports and
rotation-only teleports preserve the combo and protection. Cooldowns remain in memory and reset on plugin/server restart.

### The Falling Sky

The first time a player joins a server running 1.1.7, CelestialDash gives them a non-editable written book, *The Falling
Sky*. It tells a short science-fiction story about the Celestial Tears and quietly describes how four Tears surrounding
Netherite form the Amulet and why it can purify harmful effects and fire. Its text is conservatively wrapped across as
many pages as each translation needs, so no language is cut off in Minecraft's book interface. The book follows the
player's game language when it is English, Spanish, Portuguese, Italian, French, or Russian; all other client languages
receive English. This delivery is tracked in that player's persistent data, so it also reaches existing players once
after the update and is never repeated on later joins. If their inventory is full, the book is safely dropped at their
location.

Automatic delivery is enabled by default. The recipient is notified in their supported game language and hears a quiet
page-turn sound; both can be disabled independently. To turn off automatic delivery, add this root-level setting to
`config.yml` and run `/celestialdash reload`:

```yml
chronicle:
  enabled: false
```

Disabled delivery does not mark players. If it is enabled later, every player still awaiting the Chronicle receives it
once. An administrator can still give an online player a replacement with `/celestialdash chronicle give <player>`; this
uses that player's current game language and does not reset the one-time delivery marker.

Players can recover a lost copy themselves with `/celestialdash chronicle`. This never resets the one-time marker and
uses their current game language. The default recovery cooldown is five minutes and can be changed independently of
automatic delivery. Each Chronicle carries an internal CelestialDash marker, allowing the plugin to distinguish it from
ordinary written books without changing how the book behaves in Minecraft.

On Minecraft 1.20.5 and newer, the book uses the native enchantment glint override for its enchanted appearance without
carrying any actual enchantment. Earlier 1.20 servers receive the same non-editable book without a fake enchantment or
visual glint.

### Celestial Amulet

Craft the amulet with four Celestial Tears and one Netherite Ingot:

<img width="342" height="147" alt="Celestial Amulet crafting recipe" src="https://github.com/user-attachments/assets/443ec236-e781-4380-9aca-80bf6a9e89b0" />

Hold the amulet in your main hand and right-click to remove configured effects. By default, it removes:

- Poison, Wither, Slowness, Weakness, Blindness, Hunger, Levitation, Darkness, Unluck, and Bad Omen
- Fire

Fire is always extinguished. The amulet only consumes a charge when it successfully purifies something. Its number of
uses, cooldown, and potion-effect list are configurable. Every crafted amulet has a unique internal identity, so
separate amulets do not stack.

Hover over an amulet to see its remaining uses and its original capacity. After each successful
purification, the action bar above the hotbar shows the updated count (for example, `2/3`).
Spending the last charge removes that amulet and displays a depleted notice with `0/3`.
Other amulets keep their own charges.

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
dash-success-chat-enabled: false
dash-blacklist-worlds: [ ]
dash-strength: 1.8
dash-vertical-lift: 0.4
double-dash:
  strength-multiplier: 1.2
  lift-multiplier: 1.1
dash-action-bar:
  enabled: true
  only-while-holding-tear: true
  update-interval-ticks: 5
  ready: "&bCelestial Dash &7» &aReady"
  cooldown: "&bCelestial Dash &7» &e%seconds%s"
  second-dash: "&bSecond dash &7» &f%seconds%s"

# Automatic first-join Chronicle
chronicle:
  enabled: true
  notification-enabled: true
  delivery-sound-enabled: true
  self-reissue-cooldown-seconds: 300

# Maximum players whose %celestialdash_tears% value is cached at once (1 to 10000)
placeholder-tear-cache-max-entries: 256

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

Hold a Celestial Tear in your main hand to see when the dash is ready. After the first dash, the bar shows
the second-dash window in tenths of a second. Once that window closes or you use the second dash, it shows
the remaining cooldown in whole seconds. Both countdowns round up. Dash and amulet warnings take priority for two seconds.

Set `dash-action-bar.only-while-holding-tear` to `false` to show the bar while a tear is anywhere in your inventory.
Set `dash-action-bar.enabled` to `false` to turn the status bar off. The refresh interval accepts 1–20 ticks.
Each message accepts `&` color codes and an empty string hides that state. Older configs use the defaults above.

By default, the action bar handles dash feedback and successful dashes stay out of chat.
Set `dash-success-chat-enabled: true` to also announce both impulses in chat. Turning the action bar off
automatically restores these announcements. Customize their text with `messages.dash-used` and `messages.second-dash`.

`uses` sets the capacity for newly crafted amulets. Older amulets recover their capacity from the
original count in their lore. If that count is missing or invalid, the plugin uses the larger of the
current setting and the charges still on the item. Customize the two action-bar notices with
`messages.amulet-used-action-bar` and `messages.amulet-depleted-action-bar`. Both accept
`%uses%` and `%max-uses%`. Set `enabled` to `false`
to stop amulet use, or `recipe-enabled` to `false` to remove its crafting recipe. Set `chronicle.enabled` to `false` to
stop automatic first-join Chronicle delivery; it does not disable player recovery or the explicit admin reissue command.
`chronicle.notification-enabled` and `chronicle.delivery-sound-enabled` control the delivery announcement, while
`chronicle.self-reissue-cooldown-seconds` limits `/celestialdash chronicle` from 0 to 86,400 seconds.

`drop-delivery` applies only to each eligible player's independently generated storm drop. Use `GROUND` to leave it at
that player's location, or `INVENTORY` to add it directly to that player's inventory and safely drop any overflow.

Configuration values are safely limited by the plugin. For example, cooldowns allow 0 to 86,400 seconds, amulet uses
allow 1 to 64, particle counts allow 0 to 500, and second-dash multipliers allow 0.0 to 10.0. A single trail is also
capped at 5,000 particles: if `trail-particle-count × ceil(trail-duration-ticks / trail-interval-ticks)` exceeds that
budget, the plugin lowers the count and logs a warning. The default trail produces 200 particles, so its appearance is
unchanged. Dash and trail particle types must not require additional particle data; unsupported types such as `REDSTONE`
or `BLOCK_CRACK` log a warning and safely fall back to `CLOUD`. Invalid amulet effect names are ignored with a warning;
an empty `purifiable-effects` list disables potion-effect removal but still lets the amulet extinguish fire.

The `messages` section supports Minecraft `&` color codes and can be used to customize gameplay and command messages.
Existing configurations are never auto-overwritten: on startup and reload, the plugin warns if the v1.1.6 resource-pack
or CustomModelData keys are missing.

## Building a release

Set the project version in `pom.xml`, build the ZIP from its source with `python3 scripts/package_resource_pack.py target/pack.zip`, and copy the printed SHA-1 to `src/main/resources/config.yml` when the pack changes. Add the player-facing release notes in `releases/<version>.md`. Adding that new file to `main` requests publication for the matching project version. Alternatively, push a `v<version>` tag for the release commit. Use the manual **Release / dry run** action to test the selected ref and download its artifacts before publication.

GitHub Actions runs the Maven tests, checks the version embedded in the JAR and the pack URL and checksum, stages both files in a draft, and verifies the uploaded files before publishing.

## Optional Resource Pack

The editable pack source is in [`resource-pack/CelestialDash-Resource-Pack`](resource-pack/CelestialDash-Resource-Pack).
Download the ZIP attached to your [plugin release](https://github.com/Miniico/CelestialDash/releases). It gives the Celestial Tear and Celestial Amulet their own 32×32 textures and preserves the vanilla Ghast Tear and Nautilus Shell.

The pack declares resource formats 15 through 97.1, with legacy models and an overlay for modern clients. See the [pack notes](resource-pack/CelestialDash-Resource-Pack/README.md) for the client checks and their scope.

### Enable the release pack

The default `config.yml` contains the direct download URL for this release, its SHA-1 and the model data values.
Resource-pack delivery remains disabled by default.

To use the bundled pack on a new installation, open `plugins/CelestialDash/config.yml` and change only this option:

```yml
resource-pack:
  enabled: true
```

Then run `/celestialdash reload`. New players receive the request when they join; an administrator can resend it to an
online player with `/celestialdash pack send <player>`. New Tears issued after the reload and newly crafted or reissued
Amulets use the configured appearance. Existing Tears remain valid when `tear-custom-model-data` changes, and their
appearance is not changed retroactively. In particular, setting `tear-custom-model-data: 0` prevents the custom model
from being added to new Tears; it does not remove the model stored on existing Tears.

If you are upgrading from an older `config.yml`, add or update these bundled-pack values before enabling it:

```yml
tear-custom-model-data: 22001

resource-pack:
  enabled: true
  url: "https://github.com/Miniico/CelestialDash/releases/download/v1.1.8/CelestialDash-Resource-Pack-1.1.8.zip"
  sha1: "71614664213a919ec735258bea6c9940f5ffe245"
  required: false
  prompt: "&bThis server uses the CelestialDash resource pack."

celestial-amulet:
  custom-model-data: 22002
```

`required: false` lets players decline the pack. After confirming that the direct URL works for your players, set it to
`true` to disconnect players who decline it. The server console logs a warning when a player declines the CelestialDash
request or its download fails; the plugin does not add a separate manual kick.

Before enabling the pack, open the direct URL in a private browser window. It must download the ZIP without requiring a
sign-in. The configured SHA-1 belongs to the release ZIP and lets Minecraft reuse its
cached copy instead of downloading it again when the file has not changed.

Minecraft applies server-sent packs automatically; players do not need to place them in their normal `resourcepacks`
folder. In Minecraft 1.20.3 and newer, they are cached in the game directory's `downloads` folder. In GDLauncher, this
is normally `instance/downloads` inside the selected instance folder.

### Host Your Own Version

You may host a modified pack at any public, direct-download HTTPS URL. Replace both `url` and `sha1` with values for the
exact ZIP you host, then run `/celestialdash reload` before new players join. Keep `required: false` while testing a new
URL or pack revision.

## Commands

Run `/celestialdash` or `/celestialdash help` to see the commands available to you.
Players with the Chronicle permission see how to recover their book. Administrators also see
examples for giving Tears, replacing a player's book and sending the resource pack. From the
console, use an online player's name for `give`, `chronicle give` and `pack send`.

| Command                                  | Permission                | Description                                                                                                                                                            |
|------------------------------------------|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `/celestialdash chronicle`               | `celestialdash.chronicle` | Recovers a localized copy of *The Falling Sky* for yourself without resetting its one-time delivery marker. Subject to the configured recovery cooldown.               |
| `/celestialdash give <player> <amount>`  | `celestialdash.admin`     | Gives valid Celestial Tears to an online player. Any overflow is dropped at the player's location.                                                                     |
| `/celestialdash chronicle give <player>` | `celestialdash.admin`     | Gives an online player a localized replacement copy of *The Falling Sky*. It does not reset their one-time delivery marker; any overflow is dropped at their location. |
| `/celestialdash pack send <player>`      | `celestialdash.admin`     | Resends the enabled, valid resource-pack request to an online player.                                                                                                  |
| `/celestialdash reload`                  | `celestialdash.admin`     | Reloads the configuration, messages, item settings, amulet recipe, and resource-pack settings for future joins.                                                        |

Aliases: `/cdash` and `/celestial`.

Tab completion follows the sender's permissions and the server's configured maximum give amount.

## Permissions

| Permission                            | Default  | Description                                                                                                             |
|---------------------------------------|----------|-------------------------------------------------------------------------------------------------------------------------|
| `celestialdash.use`                   | Everyone | Allows using Celestial Tears to dash.                                                                                   |
| `celestialdash.bypass-dash-blacklist` | OP       | Allows dashing in worlds listed in `dash-blacklist-worlds`.                                                             |
| `celestialdash.receive`               | Everyone | Allows receiving storm-generated Celestial Tears.                                                                       |
| `celestialdash.amulet`                | Everyone | Allows using the Celestial Amulet.                                                                                      |
| `celestialdash.chronicle`             | Everyone | Allows recovering *The Falling Sky* with `/celestialdash chronicle`.                                                    |
| `celestialdash.admin`                 | OP       | Allows `/celestialdash give`, `/celestialdash chronicle give`, `/celestialdash pack send`, and `/celestialdash reload`. |
| `celestialdash.*`                     | OP       | Grants every CelestialDash permission.                                                                                  |

## PlaceholderAPI

PlaceholderAPI is optional. When it is installed, CelestialDash registers these placeholders:

| Placeholder                    | Result                                                                  |
|--------------------------------|-------------------------------------------------------------------------|
| `%celestialdash_tears%`        | Number of valid Celestial Tears in the player's inventory.              |
| `%celestialdash_cooldown%`     | Remaining dash cooldown in seconds.                                     |
| `%celestialdash_double_ready%` | `true` while the player can perform the second dash; otherwise `false`. |

`%celestialdash_tears%` caches each player's count for 250 milliseconds. This prevents repeated inventory scans when a scoreboard or tab list asks for the same value several times per second. After gaining, moving, or consuming Tears, the displayed count can be up to a quarter of a second behind; gameplay, crafting, and Tear validation are unaffected. `placeholder-tear-cache-max-entries` controls how many player UUIDs the cache holds at once (256 by default; 1 to 10,000) and takes effect after `/celestialdash reload`. Raise it for very large servers that display this placeholder frequently; expired entries are removed automatically.

## Item Security and Migration

Celestial Tears and Celestial Amulets use internal Persistent Data Container markers. Renaming a normal Ghast Tear or
Nautilus Shell does not turn it into a plugin item.

Tears created before 1.1.5 do not have the new internal marker and are not recognized by this version. Replace them with
new storm drops or issue new Tears with `/celestialdash give`.

## What's New in 1.1.7

- Respect interaction denials from protection and other plugins, leaving canceled clicks untouched.
- Do not activate or consume a Celestial Tear or Celestial Amulet when right-clicking a vanilla interactable block, such
  as a crafting table or chest.
- Cancel the click only after a dash or amulet purification succeeds, preserving normal block interactions when use
  fails.
- Consume a Celestial Tear only after its inventory stack has been validated, so a dash cannot succeed without paying
  its cost.
- Keep storm-drop and Amulet cooldowns through reconnects until they expire, preventing relogging from bypassing them.
- Give every player a one-time, non-editable sci-fi chronicle on their first join after the update; it subtly records
  the Amulet recipe and uses a visual-only enchanted-book glint when the server supports it.
- Localize the Chronicle for English, Spanish, Portuguese, Italian, French, and Russian game clients, with English as
  the fallback for every other locale.
- Add `chronicle.enabled` to disable automatic first-join Chronicle delivery without marking players; manual admin
  reissues remain available.
- Notify a recipient in their supported game language when the Chronicle arrives, with independently configurable
  notification and page-turn sound settings.
- Let players recover a localized Chronicle with `/celestialdash chronicle`, subject to a configurable cooldown and
  without resetting their one-time delivery marker.
- Mark each Chronicle internally so it can be reliably distinguished from ordinary written books.
- Let administrators reissue a localized Chronicle with `/celestialdash chronicle give <player>` without resetting its
  one-time delivery marker.
- Treat a Celestial Tear's PDC marker as its identity and CustomModelData as visual metadata, so existing Tears remain
  usable and craftable after a model-data change.
- Apply `tear-custom-model-data` only when creating new Tears. Setting it to `0` leaves existing Tears and their current
  icon unchanged.
- Allow PDC-marked Tears with older visual models in the Amulet recipe while continuing to reject vanilla Ghast Tears.
- Reject dash and trail particle types that require extra data, falling back to `CLOUD` so an invalid configuration
  cannot interrupt a dash after its Tear is consumed.
- Limit every particle trail to 5,000 total particles, automatically lowering `trail-particle-count` and logging a
  warning when a configuration would exceed that budget.
- Pin patched transitive versions of Guava, Commons Lang, Plexus Utils, and Commons Codec for the development and test
  classpaths; they remain server-provided or test-only and are not bundled in the plugin JAR.
- Expand automated coverage for interaction compatibility, reconnect-safe cooldowns, Chronicle delivery and recovery,
  localization and pagination, item-model migration, Amulet crafting, particle validation, and PlaceholderAPI caching.
- Cache `%celestialdash_tears%` briefly for PlaceholderAPI scoreboards and tab lists, avoiding repeated inventory scans
  while keeping the displayed count at most 250 milliseconds behind.
- Add `placeholder-tear-cache-max-entries` so large servers can configure the maximum number of cached Tear counts.

## License

CelestialDash is released under the [MIT License](LICENSE).
