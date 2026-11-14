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
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.StatType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticParser {
   private static final Logger LOGGER = LoggerFactory.getLogger("leaderboard");

   public static List<LeaderboardEntry> parseTopPlayers(Path serverDir, Path worldDir, String statPath, int limit) {
      return readSnapshot(serverDir, worldDir).topPlayers(statPath, limit);
   }

   public static List<LeaderboardEntry> parseTopPlayers(MinecraftServer server, Path serverDir, Path worldDir, String statPath, int limit) {
      return readSnapshot(server, serverDir, worldDir).topPlayers(statPath, limit);
   }

   public static StatisticSnapshot readSnapshot(Path serverDir, Path worldDir) {
      return readSnapshot(null, serverDir, worldDir);
   }

   public static StatisticSnapshot readSnapshot(MinecraftServer server, Path serverDir, Path worldDir) {
      Map<String, String> uuidToName = loadUserCache(serverDir);
      Path statsDir = worldDir.resolve("stats");
      if (!Files.isDirectory(statsDir)) {
         LOGGER.warn("[Leaderboard] Stats directory not found: {}", statsDir);
         return createSnapshot(server, uuidToName, new HashMap<>());
      } else {
         Map<String, StatisticSnapshot.PlayerStats> players = new HashMap<>();

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
                           players.put(normalizeUuid(uuid), new StatisticSnapshot.PlayerStats(playerName, stats));
                           return;
                        }
                     }
                  } catch (Exception var11x) {
                     LOGGER.warn("[Leaderboard] Failed to parse stats file: {}", file, var11x);
                  }
               }
            });
         } catch (IOException var11) {
            LOGGER.error("[Leaderboard] Failed to list stats directory", var11);
         }

         return createSnapshot(server, uuidToName, players);
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

   private static StatisticSnapshot createSnapshot(
      MinecraftServer server, Map<String, String> uuidToName, Map<String, StatisticSnapshot.PlayerStats> playersByUuid
   ) {
      if (server != null) {
         for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String uuid = normalizeUuid(player.getUUID().toString());
            String playerName = player.getScoreboardName();
            if (playerName == null || playerName.isBlank()) {
               playerName = uuidToName.getOrDefault(uuid, uuid);
            }

            playersByUuid.put(uuid, new StatisticSnapshot.PlayerStats(playerName, buildLiveStatsRoot(player)));
         }
      }

      return new StatisticSnapshot(new ArrayList<>(playersByUuid.values()));
   }

   private static JsonObject buildLiveStatsRoot(ServerPlayer player) {
      JsonObject root = new JsonObject();
      ServerStatsCounter counter = player.getStats();
      BuiltInRegistries.STAT_TYPE.stream().forEach(statType -> appendLiveCategory(root, counter, (StatType<?>)statType));
      return root;
   }

   private static <T> void appendLiveCategory(JsonObject root, ServerStatsCounter counter, StatType<T> statType) {
      ResourceLocation categoryId = BuiltInRegistries.STAT_TYPE.getKey(statType);
      if (categoryId != null) {
         Registry<T> registry = statType.getRegistry();
         JsonObject categoryObject = new JsonObject();
         registry.stream().forEach(value -> {
            int statValue = counter.getValue(statType, value);
            if (statValue > 0) {
               if (registry.getKey(value) instanceof ResourceLocation identifier) {
                  categoryObject.addProperty(identifier.toString(), statValue);
               }
            }
         });
         if (!categoryObject.entrySet().isEmpty()) {
            root.add(categoryId.toString(), categoryObject);
         }
      }
   }

   private static String normalizeUuid(String uuid) {
      return uuid == null ? "" : uuid.replace("-", "").trim();
   }
}
