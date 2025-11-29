# 🌟 CelestialDash

A storm-based dash system powered by Celestial Tears for Minecraft 1.20+

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1+-green.svg)](https://www.minecraft.net/)

---

## ✨ Features

- 🌩️ **Storm Drops** - Celestial Tears fall during thunderstorms
- ⚡ **Dash Mechanics** - Dash in any direction with customizable strength
- 🎨 **Particle Effects** - Beautiful trail and impact particles
- 🔊 **Custom Sounds** - Configurable sound effects
- 💎 **Resource Pack Support** - Custom model data integration
- 🛡️ **Permission System** - Full LuckPerms/Vault compatibility
- 🌈 **Hex Colors** - Modern color support in messages (1.16+)
- 🔒 **NBT Security** - PersistentDataContainer for item validation

---

## 📦 Installation

1. Download the latest release
2. Place `CelestialDash-1.1.2.jar` in your `/plugins/` folder
3. Restart your server
4. Configure in `/plugins/CelestialDash/config.yml`
5. Reload with `/celestialdash reload`

---

## 🎮 Commands

| Command | Permission | Description |
|---------|------------|-------------|
| `/celestialdash give <player> <amount>` | `celestialdash.admin` | Give Celestial Tears to a player |
| `/celestialdash reload` | `celestialdash.admin` | Reload plugin configuration |
| `/cdash` | - | Alias for main command |
| `/celestial` | - | Alias for main command |

---

## 🔑 Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `celestialdash.*` | op | All permissions |
| `celestialdash.use` | **true** | Use Celestial Tears to dash |
| `celestialdash.receive` | **true** | Receive tears from storm drops |
| `celestialdash.admin` | op | Access admin commands |

---

## ⚙️ Configuration

<details>
<summary>Click to expand config.yml example</summary>
```yaml
# Drop system
drop-chance-per-second: 0.03
drop-cooldown-seconds: 60

# Dash mechanics
dash-cooldown-seconds: 10
dash-strength: 1.8
dash-vertical-lift: 0.4

# Particle effects
dash-particle-enabled: true
dash-particle-type: "CLOUD"
trail-enabled: true

# And much more...
```

</details>

See full config: [config.yml](src/main/resources/config.yml)

---

## 🛠️ Building from Source
```bash
# Clone the repository
git clone https://github.com/Miinico/CelestialDash.git

# Navigate to directory
cd CelestialDash

# Build with Maven
mvn clean package

# Output: target/CelestialDash-1.2.0.jar
```

---

## 📊 Technical Details

- **Version:** 1.2.0
- **Minecraft Version:** 1.20.1+
- **API:** Spigot/Paper
- **Java Version:** 17+
- **Dependencies:** None (standalone)

---

## 🤝 Support

For issues, suggestions, or contributions, please use the [Issues](../../issues) tab.

---

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👤 Author

**Miinico**

---

<div align="center">
Made with ❤️ for the Minecraft community
</div>
