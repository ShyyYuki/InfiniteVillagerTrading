# Infinite Villager Trading

<p align="center">
  A lightweight Minecraft plugin for unlimited villager trades.
</p>

<p align="center">
  <strong>Spigot · Paper · Purpur</strong>
</p>

---

## Overview

**Infinite Villager Trading** is an open-source Minecraft plugin that removes normal villager trade usage limits and provides a configurable option to reset trade price modifiers after a player attacks a villager.

The plugin is designed to use the Bukkit/Spigot API wherever possible, keeping compatibility with Spigot, Paper, and Purpur in mind.

## Features

- **Unlimited Trades** — Villagers can restock their trades without normal usage limits.
- **Configurable Price Reset** — Toggle the price-reset feature in `config.yml`.
- **Easy Configuration** — Simple YAML configuration with documented options.
- **Configuration Reload** — Reload settings without restarting the server.
- **Permission Support** — Restrict configuration reloads to authorized users.
- **Open Source** — Free to inspect, modify, and contribute to, subject to the project's license.

## Compatibility

The plugin targets Bukkit-based Minecraft servers:

- Spigot
- Paper
- Purpur

Minecraft version compatibility depends on the API version used to build the plugin and the Java version required by the server.

**Note:** The price-reset feature resets trade demand and special-price adjustments. The shared Bukkit API does not provide a universal method to clear villager reputation, so reputation-based price changes may still occur. Full compatibility should be verified against each supported server version.

## Installation

1. Download the plugin JAR from the project's GitHub Releases page.
2. Stop your Minecraft server.
3. Place the JAR file into your server's `plugins` directory.
4. Start the server.
5. Open `plugins/InfiniteVillagerTrading/config.yml`.
6. Adjust the settings to your preferences.

You can also build the plugin from source using Maven or Gradle, depending on the project configuration.

## Configuration

The configuration file is generated automatically when the plugin starts for the first time.

```yaml
# ==========================================================
#             Infinite Villager Trading Configuration
# ==========================================================

# Enable or disable infinite villager trading.
# true  = Villager trades have unlimited uses.
# false = Vanilla trade usage limits are preserved.
enabled: true

# Prevent villager trade prices from increasing after a player
# attacks a villager.
#
# true  = Enable the price-reset workaround.
# false = Keep vanilla price behavior.
#
# Note: The shared Bukkit API cannot clear villager reputation.
# Reputation-based discounts and penalties may still affect prices.
prevent-price-increase-on-hit: true
```

### Configuration options

| Option | Default | Description |
|---|---|---|
| `enabled` | `true` | Enables or disables unlimited trading. |
| `prevent-price-increase-on-hit` | `true` | Enables or disables the trade price-reset workaround after a player attacks a villager. |

After changing the configuration, use `/infinitevillagertrading reload` to reload it.

## Commands

| Command | Description |
|---|---|
| `/infinitevillagertrading reload` | Reloads the plugin configuration. |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `ivt.reload` | Allows the player to reload the configuration. | Not specified by the command implementation |

The permission's effective default can be configured in `plugin.yml`.

## Building from Source

### Maven

```bash
mvn clean package
```

The compiled JAR is normally generated in the `target/` directory.

### Gradle

```bash
./gradlew build
```

On Windows:

```bat
gradlew.bat build
```

The compiled JAR is normally generated in `build/libs/`.

Use the build system configured in the repository.

## Project Structure

```text
InfiniteVillagerTrading/
├── src/
│   └── main/
│       ├── java/
│       │   └── de/
│       │       └── shyyyuki/
│       │           └── infiniteVillagerTrading/
│       │               ├── InfiniteVillagerTrading.java
│       │               └── commands/
│       │                   └── ReloadCommand.java
│       └── resources/
│           ├── config.yml
│           └── plugin.yml
├── README.md
└── pom.xml
```

## Contributing

Contributions are welcome!

1. Fork the repository.
2. Create a feature branch.
3. Make your changes and test them on a supported server.
4. Submit a pull request describing your changes.

Please include reproducible steps when reporting bugs.

## Bug Reports

If you encounter a problem, open a GitHub Issue and include:

- Minecraft version
- Server software and version
- Java version
- Relevant server logs
- Steps to reproduce the issue

## License

This project is open source. Add a `LICENSE` file to the repository to specify the terms under which others may use, modify, and redistribute the code.

---

<p align="center">
  Made for Minecraft server owners who want simpler villager trading.
</p>