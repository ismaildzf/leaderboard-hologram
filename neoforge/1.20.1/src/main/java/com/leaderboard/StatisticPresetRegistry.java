package com.leaderboard;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class StatisticPresetRegistry {
   private static final List<StatisticPreset> PRESETS = List.of(
      single("play_time", "Play Time", "minecraft:custom/minecraft:play_time", "Total time spent playing", "time", "online_time"),
      single("deaths", "Deaths", "minecraft:custom/minecraft:deaths", "Most player deaths", "death_count"),
      single("jumps", "Jumps", "minecraft:custom/minecraft:jump", "Number of jumps", "jump_count"),
      single("mob_kills", "Mob Kills", "minecraft:custom/minecraft:mob_kills", "Total hostile and passive mob kills", "kills"),
      single("player_kills", "Player Kills", "minecraft:custom/minecraft:player_kills", "PvP kills", "pvp_kills"),
      single("damage_dealt", "Damage Dealt", "minecraft:custom/minecraft:damage_dealt", "Total damage dealt", "dmg_dealt"),
      single("damage_taken", "Damage Taken", "minecraft:custom/minecraft:damage_taken", "Total damage taken", "dmg_taken"),
      single("distance_walked", "Distance Walked", "minecraft:custom/minecraft:walk_one_cm", "Distance walked on foot", "walked", "walk"),
      single("distance_sprinted", "Distance Sprinted", "minecraft:custom/minecraft:sprint_one_cm", "Distance sprinted", "sprinted", "sprint"),
      single("distance_swum", "Distance Swum", "minecraft:custom/minecraft:swim_one_cm", "Distance swum", "swam", "swim"),
      single("distance_flown", "Distance Flown", "minecraft:custom/minecraft:aviate_one_cm", "Distance flown with elytra", "elytra", "flight"),
      single("time_since_death", "Time Since Death", "minecraft:custom/minecraft:time_since_death", "Longest survival since last death", "survival_time"),
      single("time_since_rest", "Time Since Rest", "minecraft:custom/minecraft:time_since_rest", "Longest time without sleeping", "no_sleep"),
      single("fish_caught", "Fish Caught", "minecraft:custom/minecraft:fish_caught", "Most fish caught", "fishing"),
      single("villager_trades", "Villager Trades", "minecraft:custom/minecraft:traded_with_villager", "Number of villager trades", "trades", "traded"),
      single("games_left", "Games Left", "minecraft:custom/minecraft:leave_game", "How many times a player disconnected", "leave", "disconnects"),
      single("zombie_kills", "Zombie Kills", "minecraft:killed/minecraft:zombie", "Most zombies killed", "zombies"),
      single("skeleton_kills", "Skeleton Kills", "minecraft:killed/minecraft:skeleton", "Most skeletons killed", "skeletons"),
      single("creeper_kills", "Creeper Kills", "minecraft:killed/minecraft:creeper", "Most creepers killed", "creepers"),
      single("spider_kills", "Spider Kills", "minecraft:killed/minecraft:spider", "Most spiders killed", "spiders"),
      single("enderman_kills", "Enderman Kills", "minecraft:killed/minecraft:enderman", "Most endermen killed", "endermen"),
      single("dragon_kills", "Dragon Kills", "minecraft:killed/minecraft:ender_dragon", "Ender Dragon kills", "ender_dragon"),
      single("wither_kills", "Wither Kills", "minecraft:killed/minecraft:wither", "Wither kills", "boss_kills"),
      single("elytra_uses", "Elytra Uses", "minecraft:used/minecraft:elytra", "Number of times elytra was used", "used_elytra"),
      single("obsidian_mined", "Obsidian Mined", "minecraft:mined/minecraft:obsidian", "Obsidian blocks mined", "obsidian"),
      aggregate(
         "blocks_broken_total",
         "Blocks Broken",
         "minecraft:mined/minecraft:stone",
         "Total of all mined block statistics",
         List.of(StatisticSource.categoryTotal("minecraft:mined")),
         "blocks",
         "broken_blocks",
         "mined_total"
      ),
      aggregate(
         "items_crafted_total",
         "Items Crafted",
         "minecraft:crafted/minecraft:crafting_table",
         "Total of all crafted item statistics",
         List.of(StatisticSource.categoryTotal("minecraft:crafted")),
         "crafted_total",
         "crafts"
      ),
      aggregate(
         "items_used_total",
         "Items Used",
         "minecraft:used/minecraft:stone_pickaxe",
         "Total of all item use statistics",
         List.of(StatisticSource.categoryTotal("minecraft:used")),
         "used_total",
         "uses"
      ),
      aggregate(
         "items_picked_up_total",
         "Items Picked Up",
         "minecraft:picked_up/minecraft:cobblestone",
         "Total of all picked-up item statistics",
         List.of(StatisticSource.categoryTotal("minecraft:picked_up")),
         "picked_up_total",
         "loot_collected"
      ),
      single(
         "diamonds_collected",
         "Diamonds Collected",
         "minecraft:picked_up/minecraft:diamond",
         "Diamond items picked up",
         "diamonds",
         "diamond",
         "diamonds_mined"
      ),
      single("iron_ingots_collected", "Iron Ingots Collected", "minecraft:picked_up/minecraft:iron_ingot", "Iron ingots picked up", "iron", "iron_ingots"),
      single("gold_ingots_collected", "Gold Ingots Collected", "minecraft:picked_up/minecraft:gold_ingot", "Gold ingots picked up", "gold", "gold_ingots"),
      single("emeralds_collected", "Emeralds Collected", "minecraft:picked_up/minecraft:emerald", "Emerald items picked up", "emeralds", "emerald"),
      single("redstone_collected", "Redstone Collected", "minecraft:picked_up/minecraft:redstone", "Redstone dust picked up", "redstone"),
      single("coal_collected", "Coal Collected", "minecraft:picked_up/minecraft:coal", "Coal items picked up", "coal"),
      single("lapis_collected", "Lapis Collected", "minecraft:picked_up/minecraft:lapis_lazuli", "Lapis lazuli picked up", "lapis"),
      single(
         "copper_ingots_collected",
         "Copper Ingots Collected",
         "minecraft:picked_up/minecraft:copper_ingot",
         "Copper ingots picked up",
         "copper",
         "copper_ingots"
      ),
      single(
         "netherite_scrap_collected",
         "Netherite Scrap Collected",
         "minecraft:picked_up/minecraft:netherite_scrap",
         "Netherite scrap picked up",
         "scrap",
         "netherite"
      ),
      aggregate(
         "diamond_ore_mined",
         "Diamond Ore Mined",
         "minecraft:mined/minecraft:diamond_ore",
         "Diamond ore blocks mined, including deepslate variants",
         List.of(StatisticSource.path("minecraft:mined/minecraft:diamond_ore"), StatisticSource.path("minecraft:mined/minecraft:deepslate_diamond_ore")),
         "diamond_ore",
         "ore_diamond"
      ),
      aggregate(
         "iron_ore_mined",
         "Iron Ore Mined",
         "minecraft:mined/minecraft:iron_ore",
         "Iron ore blocks mined, including deepslate variants",
         List.of(StatisticSource.path("minecraft:mined/minecraft:iron_ore"), StatisticSource.path("minecraft:mined/minecraft:deepslate_iron_ore")),
         "iron_ore",
         "ore_iron"
      ),
      aggregate(
         "gold_ore_mined",
         "Gold Ore Mined",
         "minecraft:mined/minecraft:gold_ore",
         "Gold ore blocks mined across overworld and nether variants",
         List.of(
            StatisticSource.path("minecraft:mined/minecraft:gold_ore"),
            StatisticSource.path("minecraft:mined/minecraft:deepslate_gold_ore"),
            StatisticSource.path("minecraft:mined/minecraft:nether_gold_ore")
         ),
         "gold_ore",
         "ore_gold"
      ),
      single("ancient_debris_mined", "Ancient Debris Mined", "minecraft:mined/minecraft:ancient_debris", "Ancient debris mined", "ancient_debris"),
      aggregate(
         "ores_mined_total",
         "Ores Mined",
         "minecraft:mined/minecraft:diamond_ore",
         "Combined ore mining total across the main vanilla ores",
         List.of(
            StatisticSource.path("minecraft:mined/minecraft:coal_ore"),
            StatisticSource.path("minecraft:mined/minecraft:deepslate_coal_ore"),
            StatisticSource.path("minecraft:mined/minecraft:iron_ore"),
            StatisticSource.path("minecraft:mined/minecraft:deepslate_iron_ore"),
            StatisticSource.path("minecraft:mined/minecraft:copper_ore"),
            StatisticSource.path("minecraft:mined/minecraft:deepslate_copper_ore"),
            StatisticSource.path("minecraft:mined/minecraft:gold_ore"),
            StatisticSource.path("minecraft:mined/minecraft:deepslate_gold_ore"),
            StatisticSource.path("minecraft:mined/minecraft:nether_gold_ore"),
            StatisticSource.path("minecraft:mined/minecraft:redstone_ore"),
            StatisticSource.path("minecraft:mined/minecraft:deepslate_redstone_ore"),
            StatisticSource.path("minecraft:mined/minecraft:lapis_ore"),
            StatisticSource.path("minecraft:mined/minecraft:deepslate_lapis_ore"),
            StatisticSource.path("minecraft:mined/minecraft:emerald_ore"),
            StatisticSource.path("minecraft:mined/minecraft:deepslate_emerald_ore"),
            StatisticSource.path("minecraft:mined/minecraft:diamond_ore"),
            StatisticSource.path("minecraft:mined/minecraft:deepslate_diamond_ore"),
            StatisticSource.path("minecraft:mined/minecraft:nether_quartz_ore"),
            StatisticSource.path("minecraft:mined/minecraft:ancient_debris")
         ),
         "ores",
         "ore_total"
      ),
      aggregate(
         "logs_chopped",
         "Logs Chopped",
         "minecraft:mined/minecraft:oak_log",
         "Combined total of overworld logs and nether stems mined",
         List.of(
            StatisticSource.path("minecraft:mined/minecraft:oak_log"),
            StatisticSource.path("minecraft:mined/minecraft:spruce_log"),
            StatisticSource.path("minecraft:mined/minecraft:birch_log"),
            StatisticSource.path("minecraft:mined/minecraft:jungle_log"),
            StatisticSource.path("minecraft:mined/minecraft:acacia_log"),
            StatisticSource.path("minecraft:mined/minecraft:dark_oak_log"),
            StatisticSource.path("minecraft:mined/minecraft:mangrove_log"),
            StatisticSource.path("minecraft:mined/minecraft:cherry_log"),
            StatisticSource.path("minecraft:mined/minecraft:crimson_stem"),
            StatisticSource.path("minecraft:mined/minecraft:warped_stem")
         ),
         "logs",
         "wood",
         "trees"
      )
   );
   private static final Map<String, StatisticPreset> LOOKUP = buildLookup();

   private StatisticPresetRegistry() {
   }

   public static List<StatisticPreset> all() {
      return PRESETS;
   }

   public static List<String> presetIds() {
      return PRESETS.stream().map(StatisticPreset::id).toList();
   }

   public static StatisticPreset find(String rawPresetId) {
      return LOOKUP.get(StatisticPreset.normalize(rawPresetId));
   }

   public static StatisticPreset findByStatPath(String rawStatPath) {
      String normalizedPath = StatisticPathValidator.normalize(rawStatPath);
      if (normalizedPath.startsWith("preset:")) {
         return find(normalizedPath.substring("preset:".length()));
      } else {
         for (StatisticPreset preset : PRESETS) {
            if (preset.primaryStatPath().equals(normalizedPath)) {
               return preset;
            }
         }

         return null;
      }
   }

   public static List<StatisticPreset> search(String rawQuery) {
      String query = StatisticPreset.normalize(rawQuery);
      return query.isEmpty() ? PRESETS : PRESETS.stream().filter(preset -> preset.matches(query)).toList();
   }

   public static List<String> suggestedStatPaths() {
      Set<String> paths = new LinkedHashSet<>();

      for (StatisticPreset preset : PRESETS) {
         paths.add(preset.primaryStatPath());
      }

      paths.addAll(StatisticPathValidator.commonStatPaths());
      return List.copyOf(paths);
   }

   private static StatisticPreset single(String id, String displayName, String statPath, String description, String... aliases) {
      return new StatisticPreset(id, displayName, statPath, description, List.of(StatisticSource.path(statPath)), List.of(aliases));
   }

   private static StatisticPreset aggregate(
      String id, String displayName, String primaryStatPath, String description, List<StatisticSource> sources, String... aliases
   ) {
      return new StatisticPreset(id, displayName, primaryStatPath, description, sources, List.of(aliases));
   }

   private static Map<String, StatisticPreset> buildLookup() {
      Map<String, StatisticPreset> lookup = new LinkedHashMap<>();

      for (StatisticPreset preset : PRESETS) {
         lookup.put(StatisticPreset.normalize(preset.id()), preset);

         for (String alias : preset.aliases()) {
            lookup.putIfAbsent(StatisticPreset.normalize(alias), preset);
         }
      }

      return Map.copyOf(lookup);
   }
}
