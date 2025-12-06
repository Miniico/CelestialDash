# CelestialDash

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)  
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)  
[![Minecraft](https://img.shields.io/badge/Spigot/Paper-1.20%2B-green.svg)](https://papermc.io/)

CelestialDash is a lightweight Spigot/Paper/Purpur plugin that adds a storm-powered **Wind Dash** ability to Minecraft servers.

During thunderstorms, rare **Celestial Tears** drop around players.  
Holding a tear in your main hand lets you perform a configurable dash — and even a **second, stronger dash** within a short time window — with particles, sound, regeneration and temporary fall-damage immunity.

Perfect for survival, RPG servers, movement-based gameplay and cosmetic abilities.

---

## ✨ Features

- **Storm-Forged Celestial Tears**
  - Tears drop randomly around players during thunderstorms.
  - Per-player drop cooldown and drop chance are fully configurable.
  - Custom name, lore and optional CustomModelData support.
  - A configurable message is shown when a tear drops.

- **Wind Dash**
  - Activate by right-clicking while holding a Celestial Tear in your **main hand**.
  - Dash direction follows the player's look vector.
  - Each dash consumes exactly **one** tear from the inventory.
  - Per-player dash cooldown to prevent spam.
  - Configurable dash strength and vertical lift.
  - Optional regeneration effect applied after dashing.

- **Double Dash System**
  - A second dash can be triggered within a configurable time window.
  - The second dash has increased power.
  - Grants temporary fall-damage immunity after the second dash.
  - Separate message for the second dash.

- **Visual & Audio Effects**
  - Configurable impact particles on dash activation.
  - Optional wind trail particle effect during the dash.
  - Configurable dash sound (type, volume, pitch).
  - All particle and sound settings are configurable.

- **Messages & Customization**
  - All messages are configurable in `config.yml`:
    - `messages.cooldown`
    - `messages.no-tears`
    - `messages.dash-used`
    - `messages.second-dash`
    - `messages.tear-drop`
  - Tear CustomModelData can be set from `config.yml`.

- **Lightweight & Optimized**
  - Designed to be very lightweight and efficient.
  - Suitable for both small and large servers.

---

## ✅ Compatibility

- Spigot, Paper, Purpur
- Tested on Minecraft versions **1.17 – 1.21**
- No client mods or resource packs required (CustomModelData is optional).

---

## 🔧 Commands

- `/celestialdash give <player> <amount>`  
  Gives Celestial Tears to a player.

- `/celestialdash reload`  
  Reloads the plugin configuration and messages.

---

## 🔑 Permissions

- `celestialdash.admin`  
  Required for `/celestialdash give` and `/celestialdash reload`.

Players do **not** need any permission to use the dash as long as they can obtain Celestial Tears.

---

## ⚙ Configuration

A sample `config.yml` is generated on first run. It includes:

- Storm drop settings (chance, cooldown).
- Dash settings (cooldown, strength, vertical lift).
- Double dash settings (time window, fall-damage immunity).
- Particle and sound settings.
- Regeneration settings.
- Message entries:
  - `messages.cooldown`
  - `messages.no-tears`
  - `messages.dash-used`
  - `messages.second-dash`
  - `messages.tear-drop`

---

## 📦 Installation

1. Download the latest release `.jar`.
2. Place it in your server’s `plugins` folder.
3. Start or restart the server.
4. Edit `config.yml` if needed.
5. Use `/celestialdash reload` to apply changes without restarting.

---

## 🧑‍💻 Open Source

CelestialDash is open source and licensed under the **MIT License**.

- Modrinth: (link to Modrinth page)
- Spigot: (link to Spigot resource)
- Source code: (link to this repository)

Contributions, feedback and pull requests are welcome.
