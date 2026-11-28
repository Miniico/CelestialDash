# CelestialDash 1.1.8 installation and migration results

The [installation guide](install-and-upgrade.md) was tested on isolated Paper servers on 28 November 2026.

| Server | Clean install of public 1.1.8 | Upgrade from 1.1.7 |
| --- | --- | --- |
| Paper 1.20 build 17, Java 17 | Passed | Saved items and settings passed using a metadata correction to the old JAR |
| Paper 26.3 build 194, Java 25 | Passed | Passed using both published JARs |

On clean installs, each server started without optional plugins, reported version `1.1.8`, responded to `cdash help` and generated the configuration shipped in the JAR. The resource pack started disabled. Enabling it and running `cdash reload` loaded the official URL and SHA-1. Both servers stopped cleanly.

For each upgrade, a test player received eight Tears, one Chronicle and two Amulets with a configured capacity of five charges. The player used the first Amulet once. With the server stopped, the test backed up the plugin, configuration, worlds and player data and verified the backup file hashes before replacing the JAR.

After reconnecting to the same world, the player retained eight Tears and exactly one Chronicle. Both Amulets kept their distinct IDs and their respective charges of `4/5` and `5/5`. A successful purification with the first reduced it to `3/5`. The second stayed at `5/5`. The existing configuration stayed byte-for-byte identical, including a custom dash strength, Amulet capacity and pack URL, hash and prompt. Pack delivery was disabled during these migration tests.

Paper 1.20 build 17 rejected the [public 1.1.7 JAR](https://modrinth.com/plugin/celestialdash/version/q0KgkJca) with `Unsupported API version 1.2`. To produce 1.1.7 player data on that server, the test quoted `1.20` in a copy of its `plugin.yml`. All Java classes were identical to the published ones. Paper 26.3 loaded the original JAR. Both upgrades used the same [public 1.1.8 JAR](https://github.com/Miniico/CelestialDash/releases/tag/v1.1.8), with SHA-256 `163f54c348b7c1239c73b19041155875624ce670d036555e3146ec53f34ae422`.
