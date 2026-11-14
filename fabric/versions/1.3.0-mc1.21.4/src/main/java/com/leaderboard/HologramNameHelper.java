package com.leaderboard;

import java.util.Locale;
import java.util.regex.Pattern;

public final class HologramNameHelper {
   private static final Pattern VALID_NAME = Pattern.compile("^[a-z0-9_-]{1,32}$");

   private HologramNameHelper() {
   }

   public static String normalize(String rawName) {
      return rawName == null ? "" : rawName.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replaceAll("[^a-z0-9_-]", "_").replaceAll("_+", "_");
   }

   public static HologramNameHelper.ValidationResult validate(String rawName) {
      String normalized = normalize(rawName);
      if (normalized.isEmpty()) {
         return new HologramNameHelper.ValidationResult(false, normalized, "Hologram name cannot be empty.");
      } else {
         return !VALID_NAME.matcher(normalized).matches()
            ? new HologramNameHelper.ValidationResult(false, normalized, "Hologram name must use only lowercase letters, numbers, '_' or '-'.")
            : new HologramNameHelper.ValidationResult(true, normalized, "");
      }
   }

   public static String prettify(String normalizedName) {
      String value = normalize(normalizedName);
      if (value.isEmpty()) {
         return "Leaderboard";
      } else {
         String[] words = value.replace('-', '_').split("_+");
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

         return builder.isEmpty() ? "Leaderboard" : builder.toString();
      }
   }

   public static String suggestFromStatPath(String statPath) {
      String normalizedPath = StatisticPathValidator.normalize(statPath);
      if (normalizedPath.isEmpty()) {
         return "leaderboard";
      } else {
         String[] parts = normalizedPath.split("/", 2);
         String statKey = parts.length == 2 ? parts[1] : normalizedPath;
         if (statKey.contains(":")) {
            statKey = statKey.substring(statKey.indexOf(58) + 1);
         }

         String suggested = normalize(statKey.replace('/', '_'));
         return suggested.isEmpty() ? "leaderboard" : suggested;
      }
   }

   public record ValidationResult(boolean valid, String normalizedName, String error) {
   }
}
