package com.leaderboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticParser {
   private static final Logger LOGGER = LoggerFactory.getLogger("leaderboard");

   public static List<LeaderboardEntry> parseTopPlayers(Path serverDir, Path worldDir, String statPath, int limit) {
      return readSnapshot(serverDir, worldDir).topPlayers(statPath, limit);
   }

   public static StatisticSnapshot readSnapshot(Path serverDir, Path worldDir) {
      Map<String, String> uuidToName = loadUserCache(serverDir);
      Path statsDir = worldDir.resolve("stats");
      if (!Files.isDirectory(statsDir)) {
         LOGGER.warn("[Leaderboard] Stats directory not found: {}", statsDir);
         return new StatisticSnapshot(List.of());
      } else {
         List<StatisticSnapshot.PlayerStats> players = new ArrayList<>();

         try (Stream<Path> files = Files.list(statsDir)) {
            files.filter(path -> path.toString().endsWith(".json")).forEach(file -> {
               String fileName = file.getFileName().toString();
               if (fileName.length() > 5) {
                  String uuid = fileName.substring(0, fileName.length() - 5);
                  String playerName = uuidToName.getOrDefault(uuid, uuid);

                  try {
                     try (Reader reader = Files.newBufferedReader(file)) {
                        JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                        JsonObject stats = root.getAsJsonObject("stats");
                        if (stats != null) {
                           players.add(new StatisticSnapshot.PlayerStats(playerName, stats));
                           return;
                        }
                     }
                  } catch (Exception var11) {
                     LOGGER.warn("[Leaderboard] Failed to parse stats file: {}", file, var11);
                  }
               }
            });
         } catch (IOException var10) {
            LOGGER.error("[Leaderboard] Failed to list stats directory", var10);
         }

         return new StatisticSnapshot(players);
      }
   }

   private static Map<String, String> loadUserCache(Path serverDir) {
      Path cacheFile = serverDir.resolve("usercache.json");
      Map<String, String> map = new HashMap<>();
      if (!Files.exists(cacheFile)) {
         LOGGER.warn("[Leaderboard] usercache.json not found at {}", cacheFile);
         return map;
      } else {
         try {
            label91: {
               Object var14;
               try (Reader reader = Files.newBufferedReader(cacheFile)) {
                  JsonElement root = JsonParser.parseReader(reader);
                  if (root.isJsonArray()) {
                     for (JsonElement element : root.getAsJsonArray()) {
                        if (element.isJsonObject()) {
                           JsonObject obj = element.getAsJsonObject();
                           String uuid = obj.has("uuid") ? obj.get("uuid").getAsString() : null;
                           String name = obj.has("name") ? obj.get("name").getAsString() : null;
                           if (uuid != null && !uuid.isBlank() && name != null && !name.isBlank()) {
                              map.put(uuid, name);
                              map.put(uuid.replace("-", ""), name);
                           }
                        }
                     }
                     break label91;
                  }

                  LOGGER.warn("[Leaderboard] usercache.json has an unexpected format.");
                  var14 = map;
               }

               return (Map<String, String>)var14;
            }
         } catch (Exception var13) {
            LOGGER.error("[Leaderboard] Failed to parse usercache.json", var13);
         }

         return map;
      }
   }
}
