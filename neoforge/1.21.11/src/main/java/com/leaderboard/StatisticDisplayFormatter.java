package com.leaderboard;

import java.util.Locale;

public final class StatisticDisplayFormatter {
   private StatisticDisplayFormatter() {
   }

   public static String formatTitle(String hologramName) {
      return HologramNameHelper.prettify(hologramName);
   }

   public static String formatSubtitle(String hologramName, String statPath) {
      String title = formatTitle(hologramName);
      String statName = formatStatName(statPath);
      return title.equalsIgnoreCase(statName) ? "" : statName;
   }

   public static String formatValue(String statPath, long value) {
      StatisticPreset preset = StatisticPresetRegistry.findByStatPath(statPath);
      String statKey = extractStatKey(preset == null ? statPath : preset.primaryStatPath());
      if (isTickStat(statKey)) {
         return formatTicks(value);
      } else {
         return statKey.endsWith("_one_cm") ? formatCentimeters(value) : String.format(Locale.ROOT, "%,d", value);
      }
   }

   public static String formatStatName(String statPath) {
      StatisticPreset preset = StatisticPresetRegistry.findByStatPath(statPath);
      if (preset != null) {
         return preset.displayName();
      } else {
         String key = extractStatKey(statPath);
         String[] words = key.replace('-', '_').split("_+");
         StringBuilder builder = new StringBuilder();

         for (String word : words) {
            if (!word.isEmpty()) {
               if (builder.length() > 0) {
                  builder.append(' ');
               }

               builder.append(Character.toUpperCase(word.charAt(0)));
               if (word.length() > 1) {
                  builder.append(word.substring(1));
               }
            }
         }

         return builder.isEmpty() ? "Statistic" : builder.toString();
      }
   }

   private static boolean isTickStat(String statKey) {
      return statKey.equals("play_time")
         || statKey.equals("total_world_time")
         || statKey.equals("sneak_time")
         || statKey.startsWith("time_since_")
         || statKey.endsWith("_time");
   }

   private static String formatTicks(long ticks) {
      if (ticks <= 0L) {
         return "0s";
      } else {
         long totalSeconds = Math.max(1L, ticks / 20L);
         long days = totalSeconds / 86400L;
         long hours = totalSeconds % 86400L / 3600L;
         long minutes = totalSeconds % 3600L / 60L;
         long seconds = totalSeconds % 60L;
         if (days > 0L) {
            return minutes > 0L ? String.format(Locale.ROOT, "%dd %dh %dm", days, hours, minutes) : String.format(Locale.ROOT, "%dd %dh", days, hours);
         } else if (hours > 0L) {
            return minutes > 0L ? String.format(Locale.ROOT, "%dh %dm", hours, minutes) : String.format(Locale.ROOT, "%dh", hours);
         } else if (minutes > 0L) {
            return seconds > 0L ? String.format(Locale.ROOT, "%dm %ds", minutes, seconds) : String.format(Locale.ROOT, "%dm", minutes);
         } else {
            return String.format(Locale.ROOT, "%ds", seconds);
         }
      }
   }

   private static String formatCentimeters(long centimeters) {
      if (centimeters < 100L) {
         return centimeters + " cm";
      } else {
         double meters = centimeters / 100.0;
         if (meters < 1000.0) {
            return meters >= 100.0 ? String.format(Locale.ROOT, "%.0f m", meters) : String.format(Locale.ROOT, "%.1f m", meters);
         } else {
            double kilometers = meters / 1000.0;
            if (kilometers >= 100.0) {
               return String.format(Locale.ROOT, "%.0f km", kilometers);
            } else {
               return kilometers >= 10.0 ? String.format(Locale.ROOT, "%.1f km", kilometers) : String.format(Locale.ROOT, "%.2f km", kilometers);
            }
         }
      }
   }

   private static String extractStatKey(String statPath) {
      if (StatisticPathValidator.normalize(statPath).startsWith("preset:")) {
         StatisticPreset preset = StatisticPresetRegistry.findByStatPath(statPath);
         if (preset != null) {
            statPath = preset.primaryStatPath();
         }
      }

      String[] parts = StatisticPathValidator.normalize(statPath).split("/", 2);
      String key = parts.length == 2 ? parts[1] : parts[0];
      if (key.contains(":")) {
         key = key.substring(key.indexOf(58) + 1);
      }

      return key;
   }
}
