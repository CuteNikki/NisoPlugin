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
  - [Default Configuration](#default-configuration)
- [Images](#images)
- [Commands](#commands)
  - [Warps](#warps)
  - [Homes](#homes)
- [Permissions](#permissions)
  - [Warps](#warps-1)
  - [Homes](#homes-1)
- [Permission Group Examples](#permission-group-examples)
- [Support](#support)
- [License](#license)

## Features

- **Warps**: Set and manage warp points for easy teleportation.
- **Homes**: Allow players to set and teleport to their homes.
- **Fly**: Toggle flight mode for yourself or other players.
- **Vanish**: Toggle vanish mode for yourself or other players.
- **Repair**: Repair the item in your hand.
- **Trash**: Open a trash bin to delete items.
- **Enderchest**: Open an ender chest.
- **Invsee**: View the inventory of other players.
- **Feed**: Takes care of hunger for a while.
- **Heal**: Heal yourself.
- **God Mode**: Toggle god mode for yourself.
- **Autocomplete**: Autocompletion for things like warps, homes and more.
- **Customizable Server MOTD**: Set a custom Message of the Day (MOTD) using MiniMessage formatting.
- **Customizable Join/Leave Messages**: Customize join and leave messages with MiniMessage and PlaceholderAPI support.
- **Customizable Chat Format**: Customize the chat format using MiniMessage.
- **Customizable Tablist**: Set a custom tablist header and footer using MiniMessage and PlaceholderAPI.
- **Automatic Updates**: Automatically downloads the latest release of the plugin.
- **Config Health Check**: Checks the configuration file for errors and missing values.

## Requirements

- **[PaperMC](https://papermc.io/)**: version 1.21!
- **[PostgreSQL Database](https://www.postgresql.org/)**: to store warps and homes.
- **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)**: for placeholders in various places.

## Installation

1. Download the latest release of NisoPlugin.
2. Place the `NisoPlugin.jar` file in your server's `plugins` directory.
3. Configure the `plugins/NisoPlugin/config.yml` file to set up your PostgreSQL database connection and customize messages.
4. Restart your server to load the plugin.

## Configuration

### PostgreSQL Database

Configure your PostgreSQL database connection in the `config.yml` file like in [this configuration](#default-configuration) shown below.

### Customizable Messages

Customize the server MOTD, tablist, join/leave messages, and chat format in the `config.yml` file
using [MiniMessage](https://docs.papermc.io/misc/tools/minimessage-web-editor) formatting
and [PlaceholderAPI](https://wiki.placeholderapi.com/) placeholders.

### Default Configuration:

```yaml
# Automatically download the latest release
auto-update: true
# Notify on join if a new version is available
update-notifications: true

# Debug logs
debug: false

# This prefix is used in most messages sent by the plugin
message-prefix: <dark_gray>[<gradient:#FF5CCC:#743296>Niso<dark_gray>] <reset>

# We use PlaceholderAPI placeholders and MiniMessage formatting in this configuration file.
# Recommended placeholders to download:
# /papi ecloud download Server
# /papi ecloud download Player
# /papi ecloud download LuckPerms

# Tablist configuration
tablist:
  enabled: false
  # this is the interval in ticks to update the tablist (20 ticks = 1 second)
  # keep this value between 20 and 1000 ticks to prevent issues
  update-interval: 100
  header: |-
    <dark_gray>  <strikethrough> ]                                                                  [ <reset>  <dark_gray>
    <gray>
    <gradient:#FF5CCC:#743296>mc.niso.moe
    <gray>
  footer: |-
    <gray>
    <gray>TPS: <green>%server_tps_15% <dark_gray>| <gray>Your Ping: <green>%player_ping%ms
    <gray>Players: <green>%server_online%<dark_gray>/<green>%server_max_players% <dark_gray>| <gray>Uptime: <green>%server_uptime%
    <gray>
    <dark_gray>  <strikethrough> ]                                                                  [ <reset>  <dark_gray>

# Welcome Message Configuration
welcome-message:
  enabled: false
  # Message sent only to the joining player
  personal-message:
    enabled: true
    message: <gray>Welcome to the server, <green>%player_name%<gray>!
  # Message broadcast to all players
  broadcast-message:
    enabled: true
    message: <dark_gray>[<green>+<dark_gray>] <gray>%player_name%

# Leave Message Configuration
leave-message:
  enabled: false
  # Message sent to all players
  broadcast-message:
    enabled: true
    message: <dark_gray>[<red>-<dark_gray>] <gray>%player_name%

# Chat format Configuration
chat-format:
  enabled: false
  # Example message: Nikki » Hello, world!
  # format: <gray>%player_name% <dark_gray>» <reset>%message%
  # Example message with LuckPerms Prefix: Admin | Nikki » Hello, world!
  format: "%luckperms_prefix% %player_name% <dark_gray>» <reset>%message%"

# Server MOTD Configuration
server-motd:
  enabled: false
  first-line: <gray>                     <rainbow><b>mc.niso.moe</b></rainbow>
  second-line: <gray>                         by Nikki</gray>

# Database configuration for PostgreSQL
#
# Commands to create the database and user:
# CREATE DATABASE minecraft;
# CREATE USER username WITH ENCRYPTED PASSWORD 'super_secret';
# GRANT ALL PRIVILEGES ON DATABASE minecraft to username;
#
database:
  host: localhost
  port: 5432
  database: minecraft
  username: username
  password: super_secret
  pool-size: 10
```

## Images

Message Prefix:

![message-prefix.png](assets/message-prefix.png)

Join/Leave Messages:

![join-message.png](assets/join-message.png)

Chat Format:

![chat-format.png](assets/chat-format.png)

Server MOTD:

![motd.png](assets/motd.png)

Tablist:

![tablist.png](assets/tablist.png)

Autocomplete:

![autocomplete.png](assets/autocomplete.png)

## Commands

#### Fly

- **/fly**: Toggle flight mode on or off.
- **/fly Player**: Toggle flight mode on or off for a target player.

#### Vanish

- **/vanish**: Toggle vanish mode on or off.
- **/vanish Player**: Toggle vanish mode on or off for a target player.

#### Repair

- **/repair**: Repair the item in your hand.
- **/repair Player**: Repair a target player's item.

#### Trash

- **/trash**: Open a trash bin to delete items.

#### Enderchest

- **/enderchest**: Open your ender chest.
- **/enderchest Player**: Open a target player's ender chest.

#### Invsee

- **/invsee Player**: View the inventory of a target player.

#### Feed

- **/feed**: Feed yourself.
- **/feed Player**: Feed a target player.

#### Heal

- **/heal**: Heal yourself.
- **/heal Player**: Heal a target player.

#### God

- **/god**: Toggle god mode on or off.
- **/god Player**: Toggle god mode on or off for a target player.

#### Warps

- **/warp set Name**: Set a warp point at your current location.
- **/warp delete Name**: Remove a warp point.
- **/warp tp Name**: Teleport to a warp point.
- **/warp list**: List all warp points.

#### Homes

- **/home set Name**: Set your home location.
- **/home delete Name**: Remove your home location.
- **/home tp Name**: Teleport to your home location.
- **/home list**: List all home locations.

## Permissions

#### Fly

- **niso.fly.use**: Allows players to use the fly command.
- **niso.fly.others**: Allows the player to toggle flight on a target player.
- **niso.fly.bypass**: Other players cannot toggle flight for the player.

#### Vanish

- **niso.vanish.use**: Allows players to use the vanish command.
- **niso.vanish.others**: Allows the player to toggle vanish on a target player.
- **niso.vanish.bypass**: Other players cannot toggle vanish for the player.
- **niso.vanish.see**: Allows the player to see vanished players.

#### Repair

- **niso.repair.use**: Allows players to use the repair command.
- **niso.repair.others**: Allows the player to repair a target player's item.

#### Trash

- **niso.trash.use**: Allows players to use the trash command.

#### Enderchest

- **niso.enderchest.use**: Allows players to use the enderchest command.
- **niso.enderchest.others**: Allows the player to open other player's ender chest.
- **niso.enderchest.bypass**: Other players cannot open the player's ender chest.

#### Invsee

- **niso.invsee.use**: Allows players to use the invsee command.
- **niso.invsee.bypass**: Other players cannot open the player's inventory.

#### Feed

- **niso.feed.use**: Allows players to use the feed command.
- **niso.feed.others**: Allows the player to feed a target player.

#### Heal

- **niso.heal.use**: Allows players to use the heal command.
- **niso.heal.others**: Allows the player to heal a target player.

#### God

- **niso.god.use**: Allows players to use the god command.
- **niso.god.others**: Allows the player to toggle god mode on a target player.
- **niso.god.bypass**: Other players cannot toggle god mode for the player.

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
- **niso.home.limit-(number)**: Allows players to set a specific number (up to 50) of home locations (`niso.home.limit-3` for a max of 3 homes).

## Permission Group Examples:

#### Admin (Inherits from Moderator):

- niso.heal.others
- niso.feed.others
- niso.repair.others
- niso.fly.bypass
- niso.fly.others
- niso.god.bypass
- niso.god.others
- niso.enderchest.bypass
- niso.enderchest.others
- niso.invsee.use
- niso.invsee.bypass
- niso.vanish.bypass
- niso.home.limit-50

#### Moderator (Inherits from Player):

- niso.feed.use
- niso.heal.use
- niso.fly.use
- niso.god.use
- niso.repair.use
- niso.warp.set
- niso.warp.delete
- niso.vanish.use
- niso.vanish.see
- niso.home.limit-10

#### Player:

- niso.trash.use
- niso.warp.use
- niso.warp.list
- niso.warp.teleport
- niso.enderchest.use
- niso.home.use
- niso.home.list
- niso.home.set
- niso.home.teleport
- niso.home.delete
- niso.home.limit-3

## Support

For Support, please open an [issue](https://github.com/CuteNikki/NisoPlugin/issues) on
the [GitHub repository](https://github.com/CuteNikki/NisoPlugin/).

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
