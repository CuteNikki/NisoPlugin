# NisoPlugin

NisoPlugin is a PaperMC plugin for Minecraft 26.2 that provides various features to enhance your server experience.

This plugin includes functionalities such as warps and homes stored in a PostgreSQL Database, customizable server MOTD,
join/leave messages, and chat formatting.

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
    - [Teleport Asks](#teleport-asks)
- [Permissions](#permissions)
    - [Warps](#warps-1)
    - [Homes](#homes-1)
    - [Teleport Asks](#teleport-asks-1)
- [Permission Group Examples](#permission-group-examples)
    - [Admin](#admin-inherits-from-moderator)
    - [Moderator](#moderator-inherits-from-player)
    - [Player](#player)
- [Support](#support)
- [License](#license)

## Features

- **Warps**: Set and manage warp points for easy teleportation.
- **Homes**: Allow players to set and teleport to their homes.
- **Teleport Asks**: Players can ask to teleport to other players or request other players to teleport to them.
- **Fly**: Toggle flight mode for yourself or other players.
- **Vanish**: Toggle vanish mode for yourself or other players.
- **Repair**: Repair the item in your hand.
- **Trash**: Open a trash bin to delete items.
- **Enderchest**: Open an ender chest.
- **Workbench**: Open a virtual crafting table anytime.
- **Invsee**: View and manage the inventory of other players (including armor and offhand slots).
- **Feed**: Takes care of hunger for a while.
- **Heal**: Heal yourself.
- **God Mode**: Toggle god mode for yourself.
- **Autocomplete**: Autocompletion for things like warps, homes and more.
- **Customizable Server MOTD**: Set a custom Message of the Day (MOTD) using MiniMessage formatting.
- **Customizable Join/Leave Messages**: Customize join and leave messages with MiniMessage and PlaceholderAPI support.
- **Customizable Chat Format**: Customize the chat format using MiniMessage.
- **Customizable Tablist**: Set a custom tablist header and footer using MiniMessage and PlaceholderAPI.
- **Automatic Updates**: Automatically downloads the latest release of the plugin.
- **Config Health Check**: Adds settings that are missing after an update, replaces invalid values with their
  defaults, and keeps the comments explaining each setting up to date. A config with a syntax error is left as it is.

## Requirements

- **[PaperMC](https://papermc.io/)**: version 26.2!
- **[PostgreSQL Database](https://www.postgresql.org/)**: to store warps and homes.
- **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/)**: for placeholders in various places.
- **[LuckPerms](https://luckperms.net/)**: for prefixes and permissions.

## Installation

1. Download the latest release of NisoPlugin.
2. Place the `NisoPlugin.jar` file in your server's `plugins` directory.
3. Configure the `plugins/NisoPlugin/config.yml` file to set up your PostgreSQL database connection and customize
   messages.
4. Restart your server to load the plugin.

## Configuration

### PostgreSQL Database

Configure your PostgreSQL database connection in the `config.yml` file like
in [this configuration](#default-configuration) shown below.

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
  # The most connections the plugin opens at once, and how many it keeps open when it's idle
  pool-size: 3
  minimum-idle: 1

# Resource Pack Configuration
#
# To enable the resource pack, set 'enabled' to true and put a resource pack in the same folder as this config.
resource-pack:
  enabled: false
  server-ip: localhost
  server-port: 8080
  file-name: resource_pack.zip
  prompt-message: <red>Resource Pack Download</red>
  force-download: false
```

## Images

Message Prefix:

Join/Leave Messages:

Chat Format:

Server MOTD:

Tablist:

Autocomplete:

## Commands

#### Fly

* **/fly**: Toggle flight mode on or off.
* **/fly Player**: Toggle flight mode on or off for a target player.

#### Vanish

* **/vanish**: Toggle vanish mode on or off.
* **/vanish Player**: Toggle vanish mode on or off for a target player.

#### Repair

* **/repair**: Repair the item in your hand.
* **/repair Player**: Repair a target player's item.

#### Trash

* **/trash**: Open a trash bin to delete items.

#### Enderchest

* **/enderchest**: Open your ender chest.
* **/enderchest Player**: Open a target player's ender chest.

#### Workbench

* **/workbench**: Open a virtual crafting table menu.

#### Invsee

* **/invsee Player**: View and interact with the inventory of a target player.

#### Feed

* **/feed**: Feed yourself.
* **/feed Player**: Feed a target player.

#### Heal

* **/heal**: Heal yourself.
* **/heal Player**: Heal a target player.

#### God

* **/god**: Toggle god mode on or off.
* **/god Player**: Toggle god mode on or off for a target player.

#### Warps

* **/warp set Name**: Set a warp point at your current location.
* **/warp delete Name**: Remove a warp point.
* **/warp tp Name**: Teleport to a warp point.
* **/warp list**: List all warp points.

#### Homes

* **/home set Name**: Set your home location.
* **/home delete Name**: Remove your home location.
* **/home tp Name**: Teleport to your home location.
* **/home list**: List all home locations.

#### Teleport Asks

* **/tpask Player**: Ask a target player for teleportation to them.
* **/tpaskhere Player**: Ask a target player to teleport to you.
* **/tpaccept [Player]**: Accept a pending teleportation request (player argument optional if only 1 request is
  pending).
* **/tpdeny [Player]**: Deny a pending teleportation request (player argument optional if only 1 request is pending).
* **/tplist**: List all pending teleportation requests.

## Permissions

#### Fly

* **niso.fly.use**: Allows players to use the fly command.
* **niso.fly.others**: Allows the player to toggle flight on a target player.
* **niso.fly.bypass**: Other players cannot toggle flight for the player.

#### Vanish

* **niso.vanish.use**: Allows players to use the vanish command.
* **niso.vanish.others**: Allows the player to toggle vanish on a target player.
* **niso.vanish.bypass**: Other players cannot toggle vanish for the player.
* **niso.vanish.see**: Allows the player to see vanished players.

#### Repair

* **niso.repair.use**: Allows players to use the repair command.
* **niso.repair.others**: Allows the player to repair a target player's item.

#### Trash

* **niso.trash.use**: Allows players to use the trash command.

#### Enderchest

* **niso.enderchest.use**: Allows players to use the enderchest command.
* **niso.enderchest.others**: Allows the player to open other player's ender chest.
* **niso.enderchest.bypass**: Other players cannot open the player's ender chest.

#### Workbench

* **niso.workbench.use**: Allows players to open a virtual crafting table with `/workbench`.

#### Invsee

* **niso.invsee.use**: Allows players to use the invsee command.
* **niso.invsee.bypass**: Other players cannot open the player's inventory.

#### Feed

* **niso.feed.use**: Allows players to use the feed command.
* **niso.feed.others**: Allows the player to feed a target player.

#### Heal

* **niso.heal.use**: Allows players to use the heal command.
* **niso.heal.others**: Allows the player to heal a target player.

#### God

* **niso.god.use**: Allows players to use the god command.
* **niso.god.others**: Allows the player to toggle god mode on a target player.
* **niso.god.bypass**: Other players cannot toggle god mode for the player.

#### Warps

* **niso.warp.use**: Allows players to use warp commands.
* **niso.warp.set**: Allows players to set warp points.
* **niso.warp.delete**: Allows players to remove warp points.
* **niso.warp.teleport**: Allows players to teleport to warp points.
* **niso.warp.list**: Allows players to list warp points.

#### Homes

* **niso.home.use**: Allows players to use home commands.
* **niso.home.set**: Allows players to set home locations.
* **niso.home.delete**: Allows players to remove home locations.
* **niso.home.teleport**: Allows players to teleport to home locations.
* **niso.home.list**: Allows players to list home locations.
* **niso.home.limit-(number)**: Allows players to set a specific number (up to 50) of home
  locations (`niso.home.limit-3` for a max of 3 homes).

#### Teleport Asks

* **niso.tpask.use**: Allows players to ask for teleportation to other players.
* **niso.tpaskhere.use**: Allows players to ask other players to teleport to them.
* **niso.tpaccept.use**: Allows players to accept teleportation requests.
* **niso.tpdeny.use**: Allows players to deny teleportation requests.
* **niso.tplist.use**: Allows players to list teleportation requests.

## Permission Group Examples:

#### Admin (Inherits from Moderator):

* niso.heal.others
* niso.feed.others
* niso.repair.others
* niso.fly.bypass
* niso.fly.others
* niso.god.bypass
* niso.god.others
* niso.enderchest.bypass
* niso.enderchest.others
* niso.invsee.use
* niso.invsee.bypass
* niso.vanish.bypass
* niso.home.limit-50

#### Moderator (Inherits from Player):

* niso.feed.use
* niso.heal.use
* niso.fly.use
* niso.god.use
* niso.repair.use
* niso.warp.set
* niso.warp.delete
* niso.vanish.use
* niso.vanish.see
* niso.home.limit-10

#### Player:

* niso.trash.use
* niso.warp.use
* niso.warp.list
* niso.warp.teleport
* niso.enderchest.use
* niso.workbench.use
* niso.home.use
* niso.home.list
* niso.home.set
* niso.home.teleport
* niso.home.delete
* niso.home.limit-3
* niso.tpask.use
* niso.tpaskhere.use
* niso.tpaccept.use
* niso.tpdeny.use
* niso.tplist.use

## Support

For Support, please open an [issue](https://github.com/CuteNikki/NisoPlugin/issues) on
the [GitHub repository](https://github.com/CuteNikki/NisoPlugin/).

## License

This project is licensed under the MIT License - see the [LICENSE](https://www.google.com/search?q=LICENSE) file for
details.

```
