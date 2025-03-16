# NisoPlugin

NisoPlugin is a PaperMC plugin for Minecraft 1.21.4 that provides various features to enhance your server experience.

This plugin includes functionalities such as warps and homes stored in a PostgreSQL Database, customizable server MOTD,
join/leave messages, and chat
formatting.

## Table of Contents

- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
    - [PostgreSQL Database](#postgresql-database)
    - [Customizable Messages](#customizable-messages)
- [Commands](#commands)
    - [Warps](#warps)
    - [Homes](#homes)
- [Permissions](#permissions)
    - [Warps](#warps-1)
    - [Homes](#homes-1)
- [Support](#support)
- [License](#license)

## Features

- **Warps**: Set and manage warp points for easy teleportation.
- **Homes**: Allow players to set and teleport to their homes.
- **Customizable Server MOTD**: Set a custom Message of the Day (MOTD) using MiniMessage formatting.
- **Customizable Join/Leave Messages**: Customize join and leave messages with MiniMessage and PlaceholderAPI support.
- **Customizable Chat Format**: Customize the chat format using MiniMessage.

## Requirements

- **[PaperMC](https://papermc.io/) 1.21.4**: Ensure your server is running PaperMC version 1.21.4.
- **[PostgreSQL](https://www.postgresql.org/) Database**: A PostgreSQL database to store warps and homes.
- **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)**: For placeholders in join/leave messages
  and chat format.

## Installation

1. Download the latest release of NisoPlugin.
2. Place the `NisoPlugin.jar` file in your server's `plugins` directory.
3. Configure the `config.yml` file to set up your PostgreSQL database connection and customize messages.
4. Restart your server to load the plugin.

## Configuration

### PostgreSQL Database

Configure your PostgreSQL database connection in the `config.yml` file:

```yaml
database:
  host: localhost
  port: 5432
  database: minecraft
  user: username
  password: super_secret
  pool-size: 10
```

### Customizable Messages

Customize the server MOTD, join/leave messages, and chat format in the `config.yml` file
using [MiniMessage](https://docs.papermc.io/misc/tools/minimessage-web-editor) formatting
and [PlaceholderAPI](https://wiki.placeholderapi.com/) placeholders.

Example:

```yaml
# This prefix is used in all messages sent by the plugin.
message-prefix: <dark_gray>[<gradient:#FF5CCC:#743296>Niso<dark_gray>] <reset>

# Example message: [+] Nikki
welcome-message: <dark_gray>[<green>+<dark_gray>] <gray>%player_name%
leave-message: <dark_gray>[<red>-<dark_gray>] <gray>%player_name%

# Example message: Nikki » Hello, world!
chat-format: <gray>%player_name% <dark_gray>» <reset>%message%

server-motd: |-
  <b><red>A Minecraft Server</red></b>
  <gray>Powered by Niso</gray>
```

![message-prefix.png](assets/message-prefix.png)

![join-message.png](assets/join-message.png)

![chat-format.png](assets/chat-format.png)

![motd.png](assets/motd.png)

![autocomplete.png](assets/autocomplete.png)

## Commands

#### Warps

- **/warp set (Name)**: Set a warp point at your current location.
- **/warp delete (Name)**: Remove a warp point.
- **/warp tp (Name)**: Teleport to a warp point.
- **/warp list**: List all warp points.

#### Homes

- **/home set (Name)**: Set your home location.
- **/home delete (Name)**: Remove your home location.
- **/home tp (Name)**: Teleport to your home location.
- **/home list**: List all home locations.

## Permissions

#### Warps

- **niso.warp.use**: Allows players to use warp commands.
- **niso.warp.set**: Allows players to set warp points.
- **niso.warp.delete**: Allows players to remove warp points.
- **niso.warp.teleport**: Allows players to teleport to warp points.
- **niso.warp.list**: Allows players to list warp points.

#### Homes

- **niso.home.use**: Allows players to use home commands.
- **niso.home.set**: Allows players to set home locations.
- **niso.home.delete**: Allows players to remove home locations.
- **niso.home.teleport**: Allows players to teleport to home locations.
- **niso.home.list**: Allows players to list home locations.

## Support

For Support, please open an [issue](https://github.com/CuteNikki/NisoPlugin/issues) on
the [GitHub repository](https://github.com/CuteNikki/NisoPlugin/).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
