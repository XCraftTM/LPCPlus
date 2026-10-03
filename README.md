# LPCPlus

A lightweight, LuckPerms-powered chat formatting plugin for Paper servers, with hex color, legacy
color code, and MiniMessage support.

## Features

- ✅ **LuckPerms integration** — format chat per-group using each player's primary group, and pull in
  prefixes, suffixes, and custom meta values directly from LuckPerms.
- ✅ **Per-group chat formats** — define a different chat format for every LuckPerms group, with a
  sensible fallback (`chat-format`) for groups that don't have one.
- ✅ **Tab list formatting** — optionally display LuckPerms prefixes, suffixes, and colors in player
  names in the tab list.
- ✅ **Legacy `&` and `§` color codes** — classic Bukkit color codes, gated behind a permission.
- ✅ **Hex colors** — `&#RRGGBB` hex color codes, gated behind a permission.
- ✅ **MiniMessage support** — let trusted players use full [MiniMessage](https://docs.advntr.dev/minimessage/format.html)
  formatting in their messages (gradients, rainbows, hover text, click events, and more), with a
  safe fallback to legacy/hex/plain text if the message isn't valid MiniMessage.
- ✅ **PlaceholderAPI support** — any installed PlaceholderAPI placeholders can be used inside your
  chat formats.
- ✅ **Built on the modern Paper chat API** — no deprecated Bukkit chat APIs; formatting is done
  using Adventure `Component`s under the hood for full compatibility with modern clients.
- ✅ **Folia compatible** — no scheduler or main-thread-only calls, so it works out of the box on
  [Folia](https://papermc.io/software/folia) as well as Paper/Spigot.
- ✅ **Config reload command** — reload your configuration without restarting the server.

## Requirements

- [Paper](https://papermc.io/) (or a Paper fork, including [Folia](https://papermc.io/software/folia)) **1.21+**
- [LuckPerms](https://luckperms.net/) (required, used for groups/prefixes/suffixes/meta)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (optional, soft-depend)

## Installation

1. Download the latest `LPCPlus-x.x.x.jar` (or build it yourself, see below) and drop it into your
   server's `plugins` folder.
2. Make sure LuckPerms (and, optionally, PlaceholderAPI) is also installed.
3. Start/restart your server. A default `config.yml` will be generated in `plugins/LPCPlus/`.
4. Edit `config.yml` to your liking and run `/lpcplus reload`.

## Commands

| Command           | Description                          | Permission      |
|--------------------|--------------------------------------|-----------------|
| `/lpcplus reload`  | Reloads the plugin configuration.    | `lpcplus.reload` |

## Permissions

| Permission             | Description                                                                 | Default |
|-------------------------|------------------------------------------------------------------------------|---------|
| `lpcplus.reload`        | Allows reloading the LPCPlus configuration.                                  | `op`    |
| `lpcplus.colorcodes`    | Allows using `&` or `§` legacy color codes in chat messages.                  | `false` |
| `lpcplus.rgbcodes`      | Allows using `&#RRGGBB` hex color codes in chat messages.                    | `false` |
| `lpcplus.minimessage`   | Allows using full MiniMessage formatting in chat messages.                  | `false` |

> When granted, `lpcplus.minimessage` takes priority over `lpcplus.colorcodes`/`lpcplus.rgbcodes`.
> If a player's message isn't valid MiniMessage, LPCPlus automatically falls back to the
> legacy/hex/plain-text behavior instead of failing.

## Configuration

Formats and placeholders are fully configurable in `config.yml`:

```yaml
# Default chat format for players without a group-specific format
chat-format: "{prefix}{username-color}{name}&r: {message-color}{message}"

# Group-specific formats
group-formats:
  default: "{prefix}&f{name}&r: {message}"
  vip: "&#FFD700{prefix}&#FFD700{name}&r: &#FFFAAA{message}"
  mod: "&#00FFAA{prefix}&#00FFAA{name}&r: &#00FFDD{message}"
  admin: "&#FF5555{prefix}&#FF5555{name}&r: &#FFAAAA{message}"

# Optional tab list formatting
tablist:
  # Set to true to enable this feature (default: false).
  enabled: true
  format: "{prefix}{username-color}{name}{suffix}"
```

### Available placeholders

| Placeholder          | Description                                    |
|-----------------------|-------------------------------------------------|
| `{message}`          | The player's chat message.                       |
| `{name}`             | The player's username.                           |
| `{displayname}`      | The player's display name/nickname.              |
| `{world}`            | The world the player is currently in.            |
| `{prefix}`           | The player's highest-priority LuckPerms prefix.  |
| `{suffix}`           | The player's highest-priority LuckPerms suffix.  |
| `{prefixes}`         | All of the player's LuckPerms prefixes, stacked. |
| `{suffixes}`         | All of the player's LuckPerms suffixes, stacked. |
| `{username-color}`   | The `username-color` LuckPerms meta value.       |
| `{message-color}`    | The `message-color` LuckPerms meta value.        |

Any [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI/wiki/Placeholders) placeholder
(e.g. `%player_health%`, `%vault_eco_balance%`) can also be used in any format, as long as
PlaceholderAPI and the relevant expansion are installed.

### Colors

- Legacy codes: `&a`, `§a`, `&c`, `§c`, etc. — requires `lpcplus.colorcodes` for player messages
  (formats themselves always support these).
- Hex codes: `&#RRGGBB` (e.g. `&#FF0000`) — requires `lpcplus.rgbcodes` for player messages.
- MiniMessage: `<red>`, `<gradient:red:blue>`, `<rainbow>`, `<hover:show_text:'hi'>`, etc. — requires
  `lpcplus.minimessage` for player messages. See the
  [MiniMessage format docs](https://docs.advntr.dev/minimessage/format.html) for the full tag list.

## Building from source

LPCPlus is built with Maven and targets Java 21:

```bash
mvn clean package
```

The compiled jar will be located at `target/LPCPlus-<version>.jar`.

## License

See [UNLICENSE](UNLICENSE) for details.
