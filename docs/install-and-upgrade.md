# Installing CelestialDash 1.1.8

Start with a working Minecraft Java server. The [compatibility table](../README.md#tested-server-compatibility) lists the Paper builds and Java runtimes used in testing. Get `CelestialDash-1.1.8.jar` from the [1.1.8 release](https://github.com/Miniico/CelestialDash/releases/tag/v1.1.8). The JAR goes in the server's `plugins/` directory. Players can join with a vanilla client. PlaceholderAPI is optional and supplies placeholders for other plugins.

See the [installation and migration test results](install-validation-1.1.8.md) for the two Paper builds.

## Clean install

1. Stop the server with `stop` in its console and wait for it to exit. Place `CelestialDash-1.1.8.jar` in `plugins/`.
2. Start the server normally. Wait for `Done`. Run `version CelestialDash` and `cdash help` in the console. Check that the version is `1.1.8` and that the help lists CelestialDash commands. In game, use `/cdash help`.
3. The first start creates `plugins/CelestialDash/config.yml`. The plugin works with vanilla item textures by default. To use the official textures, follow the pack instructions below.

## Upgrade from 1.1.7

**Paper 1.20 build 17:** if the old JAR reports `Unsupported API version 1.2`, stop the server, back up any existing data and continue with the JAR replacement below. The public 1.1.8 JAR fixes this version declaration.

1. Stop the 1.1.7 server with `stop` and wait for it to exit. Back up the server before replacing the JAR. Include the old plugin JAR, `plugins/CelestialDash/`, and all world folders with their player data. Player inventories, item charges and the Chronicle delivery marker are stored with player data. Keep the backup outside the live server directory.
2. Move the old CelestialDash JAR out of `plugins/`. Put `CelestialDash-1.1.8.jar` there as the only CelestialDash JAR. Keep the existing configuration, worlds and player data in place.
3. Start the server normally, wait for `Done`, then run `version CelestialDash` and `cdash help`. Open the existing `plugins/CelestialDash/config.yml` and check a setting you customized. The plugin keeps this file. To customize a new setting, copy the relevant key from the default `config.yml` inside the 1.1.8 JAR.
4. Rejoin with a player who already had Tears, an Amulet or the Chronicle. Check their inventory and the Amulet's remaining charges. A player who already received the automatic Chronicle should still have the same number of books. Keep your existing pack URL and hash if you use your own artwork. To switch to the official pack, follow the next section.

For a standard 1.1.7 Amulet showing `4/5`, 1.1.8 reads the four remaining charges from the item and the capacity of five from its lore. After one successful purification it should show `3/5`. Check a second Amulet separately to confirm it kept its own charges.

To roll back, stop the server and restore the 1.1.7 JAR, configuration and world/player data from the same backup. This returns player progress to the time of that backup.

## Enable the official resource pack

On a clean install, set `resource-pack.enabled` to `true` in `plugins/CelestialDash/config.yml`. On an upgrade, also set the following URL and SHA-1 together under the **existing** `resource-pack` section if switching to the official 1.1.8 pack. Preserve the other keys in that section.

```yaml
resource-pack:
  enabled: true
  url: "https://github.com/Miniico/CelestialDash/releases/download/v1.1.8/CelestialDash-Resource-Pack-1.1.8.zip"
  sha1: "71614664213a919ec735258bea6c9940f5ffe245"
  required: false
```

Save the file and run `cdash reload` in the console. Join and accept the pack, or run `cdash pack send <player>` for a player already online. With the default model IDs, new Celestial Tears and Amulets display the blue tear and the gold-rimmed amulet. The server sends the download URL to the client. The ZIP stays out of `plugins/`.
