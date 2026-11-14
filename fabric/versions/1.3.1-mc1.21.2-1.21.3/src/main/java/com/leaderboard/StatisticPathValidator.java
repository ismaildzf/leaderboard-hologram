package com.leaderboard;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public final class StatisticPathValidator {
   private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("^[a-z0-9_.-]+:[a-z0-9_./-]+$");
   private static final Set<String> KNOWN_CATEGORIES = Set.of(
      "minecraft:broken",
      "minecraft:crafted",
      "minecraft:custom",
      "minecraft:dropped",
      "minecraft:killed",
      "minecraft:killed_by",
      "minecraft:mined",
      "minecraft:picked_up",
      "minecraft:used"
   );
   private static final List<String> COMMON_STAT_PATHS = List.of(
      "minecraft:custom/minecraft:deaths",
      "minecraft:custom/minecraft:jump",
      "minecraft:custom/minecraft:mob_kills",
      "minecraft:custom/minecraft:play_time",
      "minecraft:custom/minecraft:walk_one_cm",
      "minecraft:killed/minecraft:zombie",
      "minecraft:picked_up/minecraft:diamond",
      "minecraft:used/minecraft:elytra"
   );

   private StatisticPathValidator() {
   }

   public static StatisticPathValidator.ValidationResult validate(String rawPath) {
      String statPath = normalize(rawPath);
      if (statPath.isEmpty()) {
         return new StatisticPathValidator.ValidationResult(false, statPath, "Statistic path cannot be empty.", true);
      } else if (statPath.startsWith("preset:")) {
         String presetId = statPath.substring("preset:".length());
         if (presetId.isBlank()) {
            return new StatisticPathValidator.ValidationResult(false, statPath, "Preset reference cannot be empty.", true);
         } else {
            StatisticPreset preset = StatisticPresetRegistry.find(presetId);
            return preset == null
               ? new StatisticPathValidator.ValidationResult(false, statPath, "Unknown preset '" + presetId + "'. Use /leaderboard presets.", true)
               : new StatisticPathValidator.ValidationResult(true, preset.statReference(), "", true);
         }
      } else if (statPath.contains(" ")) {
         return new StatisticPathValidator.ValidationResult(false, statPath, "Statistic path cannot contain spaces.", true);
      } else {
         String[] parts = statPath.split("/", -1);
         if (parts.length != 2) {
            return new StatisticPathValidator.ValidationResult(
               false, statPath, "Statistic path must use category/stat_key, for example minecraft:custom/minecraft:mob_kills.", true
            );
         } else if (!IDENTIFIER_PATTERN.matcher(parts[0]).matches()) {
            return new StatisticPathValidator.ValidationResult(false, statPath, "Invalid statistic category: " + parts[0], true);
         } else if (!IDENTIFIER_PATTERN.matcher(parts[1]).matches()) {
            return new StatisticPathValidator.ValidationResult(false, statPath, "Invalid statistic key: " + parts[1], true);
         } else {
            boolean knownCategory = KNOWN_CATEGORIES.contains(parts[0]);
            return new StatisticPathValidator.ValidationResult(true, statPath, "", knownCategory);
         }
      }
   }

   public static String normalize(String rawPath) {
      if (rawPath == null) {
         return "";
      } else {
         String normalized = rawPath.trim().toLowerCase(Locale.ROOT);
         return normalized.startsWith("preset:") ? "preset:" + StatisticPreset.normalize(normalized.substring("preset:".length())) : normalized;
      }
   }

   public static List<String> commonStatPaths() {
      return COMMON_STAT_PATHS;
   }

   public static List<String> allStatPaths() {
      List<String> paths = new ArrayList<>();

      for (String category : commonCategories()) {
         for (String statKey : allStatKeys(category)) {
            paths.add(category + "/" + statKey);
         }
      }

      return paths;
   }

   public static List<String> commonCategories() {
      return List.copyOf(new LinkedHashSet<>(KNOWN_CATEGORIES));
   }

   public static List<String> commonStatKeys(String category) {
      String normalizedCategory = normalize(category);
      List<String> keys = new ArrayList<>();

      for (String statPath : COMMON_STAT_PATHS) {
         String[] parts = statPath.split("/", 2);
         if (parts.length == 2 && parts[0].equals(normalizedCategory)) {
            keys.add(parts[1]);
         }
      }

      return keys;
   }

   public static List<String> allStatKeys(String category) {
      String normalizedCategory = normalize(category);

      try {
         return switch (normalizedCategory) {
            case "minecraft:mined" -> sortedMinecraftIds(Registries.BLOCK.getIds());
            case "minecraft:crafted", "minecraft:used", "minecraft:broken", "minecraft:picked_up", "minecraft:dropped" -> sortedMinecraftIds(
               Registries.ITEM.getIds()
            );
            case "minecraft:killed", "minecraft:killed_by" -> sortedMinecraftIds(Registries.ENTITY_TYPE.getIds());
            case "minecraft:custom" -> sortedMinecraftIds(Registries.CUSTOM_STAT.getIds());
            default -> commonStatKeys(category);
         };
      } catch (LinkageError | RuntimeException var4) {
         return commonStatKeys(category);
      }
   }

   private static List<String> sortedMinecraftIds(Set<Identifier> ids) {
      return ids.stream().filter(id -> "minecraft".equals(id.getNamespace())).<String>map(Identifier::toString).sorted().toList();
   }

   public record ValidationResult(boolean valid, String normalizedPath, String error, boolean knownCategory) {
   }
}
