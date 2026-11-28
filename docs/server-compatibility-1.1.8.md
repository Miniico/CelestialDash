# CelestialDash 1.1.8 server compatibility

The tests used the [public 1.1.8 JAR](https://github.com/Miniico/CelestialDash/releases/tag/v1.1.8), SHA-256 `163f54c348b7c1239c73b19041155875624ce670d036555e3146ec53f34ae422`, and PlaceholderAPI 2.12.3. The rows use the Minecraft versions listed for this release on [Modrinth](https://modrinth.com/plugin/celestialdash/versions). Each server ran on loopback with a disposable world.

## What a gameplay pass covers

- The server enabled CelestialDash and registered its PlaceholderAPI expansion. A new player received one Chronicle and kept exactly one after reconnecting.
- The player used a Celestial Tear twice. PlaceholderAPI reported 8, 7 and 6 Tears. The second-dash flag became available after the first impulse and cleared after the second. The cooldown returned to zero.
- The player sent a Shift-click on the output slot of a server workbench. Eight netherite ingots and 32 Celestial Tears yielded eight Amulets with distinct IDs. The ingredient slots were empty after the transaction.
- The client downloaded the 13,215-byte official resource pack from the URL sent by the plugin and checked its SHA-1, `71614664213a919ec735258bea6c9940f5ffe245`.

The protocol clients used for the matrix do not draw textures. Visual checks in matching vanilla clients are recorded for [Paper 1.20](evidence/1.1.8-inventory-1.20.png) and [Paper 26.3](evidence/1.1.8-inventory-26.3.png). The [protection tests](protection-validation-2026-11-24.md) and [movement and fall tests](mechanics-validation-2026-11-25.md) identify the builds on which those additional scenarios were tested.

## Results by server build

**Pass** covers the gameplay checks above. **Visual** adds a vanilla-client texture check. **Startup** covers plugin loading, its commands and registration with PlaceholderAPI. **No build** means the distributor's public catalog has no server for that exact game version. **Pending** means this matrix has no completed run yet.

| Minecraft | Spigot | Paper | Purpur |
| --- | --- | --- | --- |
| 1.20 | [3798](https://hub.spigotmc.org/versions/3798.json) · Pass | [17](https://fill.papermc.io/v3/projects/paper/versions/1.20/builds/17) · Visual | [1990](https://api.purpurmc.org/v2/purpur/1.20/1990) · Pass |
| 1.20.1 | [3871](https://hub.spigotmc.org/versions/3871.json) · Pass | [196](https://fill.papermc.io/v3/projects/paper/versions/1.20.1/builds/196) · Pass | [2062](https://api.purpurmc.org/v2/purpur/1.20.1/2062) · Pass |
| 1.20.2 | Pending | [318](https://fill.papermc.io/v3/projects/paper/versions/1.20.2/builds/318) · Pass | [2095](https://api.purpurmc.org/v2/purpur/1.20.2/2095) · Pass |
| 1.20.3 | Pending | No build | No build |
| 1.20.4 | Pending | [499](https://fill.papermc.io/v3/projects/paper/versions/1.20.4/builds/499) · Pass | [2176](https://api.purpurmc.org/v2/purpur/1.20.4/2176) · Pass |
| 1.20.5 | Pending | [22](https://fill.papermc.io/v3/projects/paper/versions/1.20.5/builds/22) · Pass | No build |
| 1.20.6 | Pending | [151](https://fill.papermc.io/v3/projects/paper/versions/1.20.6/builds/151) · Pass | [2233](https://api.purpurmc.org/v2/purpur/1.20.6/2233) · Pass |
| 1.21 | Pending | [130](https://fill.papermc.io/v3/projects/paper/versions/1.21/builds/130) · Pass | [2284](https://api.purpurmc.org/v2/purpur/1.21/2284) · Pass |
| 1.21.1 | Pending | [133](https://fill.papermc.io/v3/projects/paper/versions/1.21.1/builds/133) · Pass | [2329](https://api.purpurmc.org/v2/purpur/1.21.1/2329) · Pass |
| 1.21.2 | Pending | No build | No build |
| 1.21.3 | Pending | [83](https://fill.papermc.io/v3/projects/paper/versions/1.21.3/builds/83) · Pass | [2358](https://api.purpurmc.org/v2/purpur/1.21.3/2358) · Pass |
| 1.21.4 | Pending | [232](https://fill.papermc.io/v3/projects/paper/versions/1.21.4/builds/232) · Pass | [2416](https://api.purpurmc.org/v2/purpur/1.21.4/2416) · Pass |
| 1.21.5 | Pending | [114](https://fill.papermc.io/v3/projects/paper/versions/1.21.5/builds/114) · Pass | [2450](https://api.purpurmc.org/v2/purpur/1.21.5/2450) · Pass |
| 1.21.6 | Pending | [48](https://fill.papermc.io/v3/projects/paper/versions/1.21.6/builds/48) · Pass | [2465](https://api.purpurmc.org/v2/purpur/1.21.6/2465) · Pass |
| 1.21.7 | Pending | [32](https://fill.papermc.io/v3/projects/paper/versions/1.21.7/builds/32) · Pass | [2477](https://api.purpurmc.org/v2/purpur/1.21.7/2477) · Pass |
| 1.21.8 | Pending | [60](https://fill.papermc.io/v3/projects/paper/versions/1.21.8/builds/60) · Pass | [2497](https://api.purpurmc.org/v2/purpur/1.21.8/2497) · Pass |
| 1.21.9 | Pending | [59](https://fill.papermc.io/v3/projects/paper/versions/1.21.9/builds/59) · Pass | [2505](https://api.purpurmc.org/v2/purpur/1.21.9/2505) · Pass |
| 1.21.10 | Pending | [130](https://fill.papermc.io/v3/projects/paper/versions/1.21.10/builds/130) · Pass | [2535](https://api.purpurmc.org/v2/purpur/1.21.10/2535) · Pass |
| 1.21.11 | Pending | [132](https://fill.papermc.io/v3/projects/paper/versions/1.21.11/builds/132) · Pass | [2568](https://api.purpurmc.org/v2/purpur/1.21.11/2568) · Startup |
| 26.1 | Pending | No build | No build |
| 26.1.1 | Pending | [29](https://fill.papermc.io/v3/projects/paper/versions/26.1.1/builds/29) · Pass | No build |
| 26.1.2 | Pending | [74](https://fill.papermc.io/v3/projects/paper/versions/26.1.2/builds/74) · Pass | [2592](https://api.purpurmc.org/v2/purpur/26.1.2/2592) · Startup |
| 26.2 | Pending | [135](https://fill.papermc.io/v3/projects/paper/versions/26.2/builds/135) · Pass | Pending |
| 26.3 | Pending | [194](https://fill.papermc.io/v3/projects/paper/versions/26.3/builds/194) · Visual | [2661](https://api.purpurmc.org/v2/purpur/26.3/2661) · Pass |

The linked build numbers identify the server binaries used. Java 17 was used through Minecraft 1.20.4, Java 21 for gameplay from 1.20.5 through 1.21.11, and Java 25 for 26.x. Additional startup runs on Paper 1.20.6 and 1.21.11 used Java 25.

The Paper 1.20 and 26.3 rows combine the published-JAR download and dash tests with the [previous crafting results](crafting-validation-2026-11-24.md). The Paper 1.20 session produced three Amulets with ordinary clicks and five with Shift. The other gameplay runs checked eight in a single Shift-click.

Some Spigot version names now point to a later patch. Historical BuildTools revisions were used where necessary. The matrix uses the server's own Minecraft version. When two client releases share a network protocol, a successful protocol test counts once for the server build that ran.

Paper marked the following downloads as alpha: 1.20.5 #22, 1.21.5 #114, 1.21.9 #59, 26.1.1 #29.
