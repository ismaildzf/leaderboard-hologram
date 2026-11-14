# Leaderboard Hologram

**Your players' statistics, floating in the world — not buried in a menu.**

Every survival server already tracks play time, deaths, mob kills, blocks broken and villager trades. The problem is that nobody ever sees those numbers. Leaderboard Hologram puts them in front of your players as clean, auto-updating boards built from Minecraft display entities: at spawn, over the mining area, inside the lobby, next to the shop.

The mod is **fully server-side**. A vanilla client sees the boards with nothing installed.

![Leaderboard Hologram](https://media.forgecdn.net/attachments/2191/172/01_hero-jpg.jpg)

---

## Why servers use it

* **Zero client setup** — one jar on the server, nothing for players to download.
* **Readable by default** — ranks, player names and values are laid out as centered rows with per-row backgrounds, not a wall of raw text.
* **Friendly presets** — 44 ready-made statistics with clean names and short aliases, so you never have to type `minecraft:mined/minecraft:deepslate_diamond_ore` by hand.
* **Aggregate boards** — some presets combine many vanilla statistics into one meaningful number (all ores mined, all items crafted, all logs chopped, and more).
* **Persistent boards** — every board is stored by name in `config/leaderboard_holograms.json` and respawns automatically after a restart.
* **Live data** — boards refresh from real player statistics every 5 minutes, and `/leaderboard refresh` updates every board immediately.
* **Four visual themes** — `clean`, `gold`, `dark` and `compact`, switchable per board.
* **Human-readable numbers** — play time as `3d 7h 12m`, distances in metres and kilometres, large counts with thousands separators.

---

## Requirements

* Minecraft Java Edition with **Fabric Loader 0.16 or newer**
* **Fabric API**
* **Java 21 or newer**
* Download the jar that matches your Minecraft version (see the Files tab)

You only install this on the **server**. Clients do not need it.

---

## Quick start

Stand where you want the board and run:

```mcfunction
/leaderboard create quick play_time
```

That creates a board at your position, named `play_time`, showing the top 10 players by total play time.

A named board with a custom row count and theme:

```mcfunction
/leaderboard create preset miners diamonds_collected 10
/leaderboard settheme miners gold
```

A board built from a raw statistic path:

```mcfunction
/leaderboard create path totems minecraft:used/minecraft:totem_of_undying 5
```

---

## Built-in presets

Run `/leaderboard presets` in game for the full list, or `/leaderboard presets <query>` to search it. Short aliases are accepted too, for example `diamonds`, `wood` or `kills`.

**Time and survival**
`play_time`, `time_since_death`, `time_since_rest`, `deaths`, `games_left`

**Combat**
`mob_kills`, `player_kills`, `damage_dealt`, `damage_taken`, `zombie_kills`, `skeleton_kills`, `creeper_kills`, `spider_kills`, `enderman_kills`, `dragon_kills`, `wither_kills`

**Movement**
`distance_walked`, `distance_sprinted`, `distance_swum`, `distance_flown`, `jumps`, `elytra_uses`

**Gathering**
`diamonds_collected`, `iron_ingots_collected`, `gold_ingots_collected`, `emeralds_collected`, `redstone_collected`, `coal_collected`, `lapis_collected`, `copper_ingots_collected`, `netherite_scrap_collected`

**Mining**
`blocks_broken_total`, `ores_mined_total`, `diamond_ore_mined`, `iron_ore_mined`, `gold_ore_mined`, `obsidian_mined`, `ancient_debris_mined`, `logs_chopped`

**Crafting and usage**
`items_crafted_total`, `items_used_total`, `items_picked_up_total`

**Economy and fishing**
`villager_trades`, `fish_caught`

---

## Themes

| Theme | Look |
| --- | --- |
| `clean` | Minimal centered rows with soft highlights (default) |
| `gold` | Warmer, premium styling inspired by network leaderboards |
| `dark` | Sharper dark styling with cleaner centered rows |
| `compact` | Tighter rows for smaller areas |

```mcfunction
/leaderboard settheme spawn_board compact
```

![Four themes](https://media.forgecdn.net/attachments/2191/184/03_themes-jpg.jpg)

---

## Commands

All commands require permission level 2 (operator) or the server console.

| Command | What it does |
| --- | --- |
| `/leaderboard create quick <preset>` | Create a board at your position, named after the preset |
| `/leaderboard create preset <name> <preset> [rows]` | Create a named board from a preset |
| `/leaderboard create path <name> <statistic_path> [rows]` | Create a named board from a raw statistic path |
| `/leaderboard remove <name>` | Delete a board by name |
| `/leaderboard remove nearest` | Delete the closest board (within 10 blocks) |
| `/leaderboard rename <name> <new_name>` | Rename a board |
| `/leaderboard move <name>` | Move a board to your current position |
| `/leaderboard setpos <name> <pos>` | Move a board to absolute coordinates |
| `/leaderboard settop <name> <rows>` | Change how many players are shown (max 25) |
| `/leaderboard settheme <name> <theme>` | Change a board's theme |
| `/leaderboard setstat <name> preset <preset>` | Point a board at another preset |
| `/leaderboard setstat <name> path <statistic_path>` | Point a board at a raw statistic path |
| `/leaderboard info <name>` | Show a board's statistic, position, row count and theme |
| `/leaderboard list` | List every active board on the server |
| `/leaderboard presets [query]` | List or search available presets |
| `/leaderboard themes` | List available themes |
| `/leaderboard search <query>` | Search presets by name |
| `/leaderboard refresh` | Refresh every board immediately |
| `/leaderboard reload` | Reload boards from the config file |

Boards keep a 2-block distance from each other, so you can build a neat wall of leaderboards without overlapping text.

---

## Configuration and storage

Boards are saved in `config/leaderboard_holograms.json` inside your server directory. The file stores each board's name, statistic, world, position, row count and theme — edit it only while the server is stopped, then use `/leaderboard reload` after starting.

Automatic refresh runs every 5 minutes (6000 ticks). Use `/leaderboard refresh` after a big event if you want the boards up to date instantly.

---

## Custom statistics

Anything the vanilla statistics screen tracks can be shown. Use `/leaderboard setstat <name> path <statistic_path>` or create a board with a raw path.

Common paths look like:

* `minecraft:custom/minecraft:play_time`
* `minecraft:mined/minecraft:deepslate_diamond_ore`
* `minecraft:killed/minecraft:warden`
* `minecraft:used/minecraft:ender_pearl`

Statistics from other mods usually work the same way — check the exact key your mod registers. The mod warns you when a path is outside the common vanilla categories.

---

## FAQ

**Do players need the mod?**
No. It is server-side only.

**Does it work on a vanilla server or Paper?**
No. It needs Fabric Loader, Fabric API and the matching jar for your Minecraft version.

**Which Minecraft versions are supported?**
1.20.1 through 1.21.11, plus 26.1 / 26.1.1 builds. Each jar targets a specific version range — check the file name or the Files tab.

**How many players can a board show?**
Up to 25, 10 by default.

**Will it lag my server?**
Boards are display entities that update every 5 minutes, not every tick. A handful of boards is negligible.

**Why does a board show nothing?**
Statistics are per player and per world. A fresh world has no data yet, and boards only read the world they were placed in.

**Can I have several boards for the same statistic?**
Yes. Boards are addressed by name, and each keeps its own position, row count and theme.

---

## Support

Found a bug or need a preset that does not exist yet? Contact me on Discord: **v_eno**

Licensed under the **MIT License**.
