package com.leaderboard;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class StatisticSnapshot {
   private final List<StatisticSnapshot.PlayerStats> players;
   private final Map<String, List<LeaderboardEntry>> topCache = new HashMap<>();

   public StatisticSnapshot(List<StatisticSnapshot.PlayerStats> players) {
      this.players = List.copyOf(players);
   }

   public List<LeaderboardEntry> topPlayers(String rawStatPath, int limit) {
      StatisticPathValidator.ValidationResult validation = StatisticPathValidator.validate(rawStatPath);
      if (!validation.valid()) {
         return Collections.emptyList();
      } else {
         int normalizedLimit = Math.max(1, limit);
         List<LeaderboardEntry> allEntries = this.topCache.computeIfAbsent(validation.normalizedPath(), this::computeSortedEntries);
         return allEntries.size() <= normalizedLimit ? allEntries : List.copyOf(allEntries.subList(0, normalizedLimit));
      }
   }

   private List<LeaderboardEntry> computeSortedEntries(String normalizedStatPath) {
      List<LeaderboardEntry> entries = new ArrayList<>();
      StatisticPreset preset = StatisticPresetRegistry.findByStatPath(normalizedStatPath);

      for (StatisticSnapshot.PlayerStats playerStats : this.players) {
         long value = preset != null && preset.statReference().equals(normalizedStatPath)
            ? this.readPresetValue(playerStats.stats(), preset)
            : this.readRawValue(playerStats.stats(), normalizedStatPath);
         if (value > 0L) {
            entries.add(new LeaderboardEntry(playerStats.playerName(), value));
         }
      }

      entries.sort(null);
      return List.copyOf(entries);
   }

   private long readPresetValue(JsonObject statsRoot, StatisticPreset preset) {
      long total = 0L;

      for (StatisticSource source : preset.sources()) {
         total = this.saturatingAdd(total, source.readValue(statsRoot));
      }

      return total;
   }

   private long readRawValue(JsonObject statsRoot, String normalizedStatPath) {
      String[] parts = normalizedStatPath.split("/", 2);
      return parts.length != 2 ? 0L : new StatisticSource(parts[0], parts[1], 1L).readValue(statsRoot);
   }

   private long saturatingAdd(long left, long right) {
      if (left <= 0L) {
         return Math.max(0L, right);
      } else if (right <= 0L) {
         return left;
      } else {
         try {
            return Math.addExact(left, right);
         } catch (ArithmeticException var6) {
            return Long.MAX_VALUE;
         }
      }
   }

   public record PlayerStats(String playerName, JsonObject stats) {
   }
}
