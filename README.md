# 🌟 CelestialDash
A fast, storm-powered dash ability fueled by **Celestial Tears**.  
Lightweight, polished and fully configurable — perfect for survival servers, RPG worlds and movement-based gameplay.

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)  
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)  
[![Minecraft](https://img.shields.io/badge/Spigot/Paper-1.20%2B-green.svg)](https://papermc.io/)

---

## ✨ Features

### 🌩 Storm-Forged Celestial Tears
- Tears drop randomly around players **only during thunderstorms**
- Fully configurable drop chance & cooldown  
- Custom model data support  
- Each tear is **automatically consumed** when dashing  
- New storm-drop message (`messages.tear-drop`)  

---

## ⚡ Wind Dash Ability
- Activated by **right-clicking with a Celestial Tear in the main hand**
- **Double Dash System**:
  - Perform a second dash within a configurable window  
  - Second dash is stronger  
  - Grants temporary **fall-damage immunity**
- Adjustable dash strength, vertical lift, and cooldown  
- Clean action-bar cooldown warnings  
- 100% conflict-free with vanilla actions (eating, blocking, bow charging…)  

---

## 🌬 Visual & Audio Effects

### 🌀 Wind Trail
- Dynamic trail that follows the player during the dash  
- Fully configurable:
  - Particle type  
  - Amount  
  - Duration  
  - Interval  
  - Speed  

### 💨 Dash Impact Burst
- Particle explosion at dash activation  
- Configurable offsets & amount  

### 🔊 Dash Sound
- Custom sound triggered on dash  
- Adjustable volume & pitch  

---

## ❤️ Regeneration Boost
After each dash:
- Short **Regeneration** effect  
- Duration & amplifier configurable in `config.yml`  

---

## 📦 Installation
1. Download the latest release  
2. Place the `.jar` into `/plugins/`  
3. Start the server  
4. Configure `config.yml`  
5. Reload with: /celestialdash reload


---

## 🎮 Commands

| Command | Permission | Description |
|--------|-------------|-------------|
| `/celestialdash give <player> <amount>` | `celestialdash.admin` | Give Celestial Tears |
| `/celestialdash reload` | `celestialdash.admin` | Reloads plugin configuration |

Players do **not** need any permission to use the dash.

---

## 🔧 Configuration
Everything can be modified:
- Drop mechanics  
- Dash physics  
- Double dash behavior  
- Particles  
- Trails  
- Sounds  
- Regeneration  
- Custom model data  
- Messages  

See full config: [`config.yml`](src/main/resources/config.yml)

---

## 🛠️ Build From Source

git clone https://github.com/Miinico/CelestialDash.git

cd CelestialDash
mvn clean package

Output: `target/CelestialDash-x.x.x.jar`

---

## 📊 Technical Details
- **Minecraft:** 1.20+  
- **API:** Spigot / Paper / Purpur  
- **Java:** 17+  
- **Dependencies:** None  
- **Performance:** Zero TPS impact  

---

## 🤝 Support
Found a bug or want a feature?  
➡️ Open an Issue on GitHub.

---

## 📝 License
Distributed under the MIT License.  
See [`LICENSE`](LICENSE).

---

## 👤 Author
**Miinico**

