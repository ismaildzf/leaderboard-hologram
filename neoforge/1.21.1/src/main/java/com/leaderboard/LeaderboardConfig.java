package com.leaderboard;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderboardConfig {
   public static final int DEFAULT_TOP_COUNT = 10;
   public static final int MAX_TOP_COUNT = 25;
   private static final Logger LOGGER = LoggerFactory.getLogger("leaderboard");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private final Path configFile;
   private final List<LeaderboardConfig.HologramDefinition> holograms = new ArrayList<>();

   public LeaderboardConfig(Path configDir) {
      this.configFile = configDir.resolve("leaderboard_holograms.json");
   }

   public void load() {
      this.holograms.clear();
      if (!Files.exists(this.configFile)) {
         LOGGER.info("[Leaderboard] No config file found at {}, creating an empty config.", this.configFile.toAbsolutePath());
         this.save();
      } else {
         boolean changed = false;

         try {
            label86: {
               try (Reader reader = Files.newBufferedReader(this.configFile)) {
                  JsonArray array = this.extractHologramArray(JsonParser.parseReader(reader));
                  if (array != null) {
                     Set<String> ids = new HashSet<>();
                     Set<String> names = new HashSet<>();
                     Set<String> duplicateKeys = new HashSet<>();

                     for (JsonElement elem : array) {
                        LeaderboardConfig.HologramDefinition def = (LeaderboardConfig.HologramDefinition)GSON.fromJson(
                           elem, LeaderboardConfig.HologramDefinition.class
                        );
                        if (def == null) {
                           changed = true;
                        } else {
                           changed |= def.normalize();
                           changed |= this.ensureUniqueName(def, names);
                           if (!def.isValid()) {
                              LOGGER.warn("[Leaderboard] Skipping invalid hologram definition: {}", elem);
                              changed = true;
                           } else if (!ids.add(def.id)) {
                              LOGGER.warn("[Leaderboard] Skipping duplicate hologram id: {}", def.id);
                              changed = true;
                           } else if (!duplicateKeys.add(def.duplicateKey())) {
                              LOGGER.warn(
                                 "[Leaderboard] Skipping duplicate hologram entry at ({}, {}, {}) for stat {}", new Object[]{def.x, def.y, def.z, def.statPath}
                              );
                              changed = true;
                           } else {
                              this.holograms.add(def);
                           }
                        }
                     }

                     LOGGER.info("[Leaderboard] Loaded {} hologram(s) from config {}.", this.holograms.size(), this.configFile.toAbsolutePath());
                     break label86;
                  }

                  LOGGER.error("[Leaderboard] Config file has an unsupported format: {}", this.configFile);
               }

               return;
            }
         } catch (Exception var12) {
            LOGGER.error("[Leaderboard] Failed to load config file {}", this.configFile.toAbsolutePath(), var12);
         }

         if (changed) {
            this.save();
         }
      }
   }

   public void save() {
      try {
         Files.createDirectories(this.configFile.getParent());
         Path tempFile = this.configFile.resolveSibling(this.configFile.getFileName() + ".tmp");

         try (Writer writer = Files.newBufferedWriter(tempFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            GSON.toJson(this.holograms, writer);
         }

         try {
            Files.move(tempFile, this.configFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
         } catch (AtomicMoveNotSupportedException var6) {
            Files.move(tempFile, this.configFile, StandardCopyOption.REPLACE_EXISTING);
         }
      } catch (IOException var8) {
         LOGGER.error("[Leaderboard] Failed to save config file {}", this.configFile.toAbsolutePath(), var8);
      }
   }

   public boolean addHologram(LeaderboardConfig.HologramDefinition def) {
      if (def == null) {
         return false;
      } else {
         def.normalize();
         if (!HologramNameHelper.validate(def.name).valid()) {
            return false;
         } else if (!def.isValid()) {
            LOGGER.warn("[Leaderboard] Refusing to save invalid hologram definition: {}", def.statPath);
            return false;
         } else {
            boolean duplicate = this.holograms
               .stream()
               .anyMatch(existing -> existing.id.equals(def.id) || existing.name.equals(def.name) || existing.duplicateKey().equals(def.duplicateKey()));
            if (duplicate) {
               return false;
            } else {
               this.holograms.add(def);
               this.save();
               return true;
            }
         }
      }
   }

   public boolean removeHologram(String id) {
      boolean removed = this.holograms.removeIf(def -> def.id.equals(id));
      if (removed) {
         this.save();
      }

      return removed;
   }

   public LeaderboardConfig.HologramDefinition findByName(String rawName) {
      String normalizedName = HologramNameHelper.normalize(rawName);

      for (LeaderboardConfig.HologramDefinition hologram : this.holograms) {
         if (hologram.name.equals(normalizedName)) {
            return hologram;
         }
      }

      return null;
   }

   public boolean renameHologram(String id, String rawNewName) {
      HologramNameHelper.ValidationResult validation = HologramNameHelper.validate(rawNewName);
      if (!validation.valid()) {
         return false;
      } else {
         for (LeaderboardConfig.HologramDefinition hologram : this.holograms) {
            if (!hologram.id.equals(id) && hologram.name.equals(validation.normalizedName())) {
               return false;
            }
         }

         LeaderboardConfig.HologramDefinition hologramx = this.findById(id);
         if (hologramx == null) {
            return false;
         } else {
            hologramx.name = validation.normalizedName();
            this.save();
            return true;
         }
      }
   }

   public boolean updatePosition(String id, String worldId, double x, double y, double z) {
      LeaderboardConfig.HologramDefinition hologram = this.findById(id);
      if (hologram == null) {
         return false;
      } else {
         hologram.worldId = worldId;
         hologram.x = x;
         hologram.y = y;
         hologram.z = z;
         this.save();
         return true;
      }
   }

   public boolean updateTopCount(String id, int topCount) {
      LeaderboardConfig.HologramDefinition hologram = this.findById(id);
      if (hologram == null) {
         return false;
      } else {
         hologram.topCount = clampTopCount(topCount);
         this.save();
         return true;
      }
   }

   public boolean updateStatPath(String id, String rawStatPath) {
      LeaderboardConfig.HologramDefinition hologram = this.findById(id);
      if (hologram == null) {
         return false;
      } else {
         StatisticPathValidator.ValidationResult validation = StatisticPathValidator.validate(rawStatPath);
         if (!validation.valid()) {
            return false;
         } else {
            hologram.statPath = validation.normalizedPath();
            this.save();
            return true;
         }
      }
   }

   public boolean updateTheme(String id, String rawTheme) {
      LeaderboardConfig.HologramDefinition hologram = this.findById(id);
      if (hologram == null) {
         return false;
      } else {
         String normalizedTheme = HologramThemeRegistry.normalize(rawTheme);
         if (!HologramThemeRegistry.isKnownTheme(normalizedTheme)) {
            return false;
         } else {
            hologram.theme = normalizedTheme;
            this.save();
            return true;
         }
      }
   }

   public List<LeaderboardConfig.HologramDefinition> getHolograms() {
      return Collections.unmodifiableList(this.holograms);
   }

   public static int clampTopCount(int topCount) {
      return Math.max(1, Math.min(25, topCount));
   }

   private JsonArray extractHologramArray(JsonElement root) {
      if (root == null) {
         return null;
      } else if (root.isJsonArray()) {
         return root.getAsJsonArray();
      } else {
         if (root.isJsonObject()) {
            JsonObject object = root.getAsJsonObject();
            JsonElement hologramsElement = object.get("holograms");
            if (hologramsElement != null && hologramsElement.isJsonArray()) {
               return hologramsElement.getAsJsonArray();
            }
         }

         return null;
      }
   }

   private boolean ensureUniqueName(LeaderboardConfig.HologramDefinition def, Set<String> usedNames) {
      String baseName = HologramNameHelper.normalize(def.name);
      if (baseName.isEmpty()) {
         baseName = HologramNameHelper.suggestFromStatPath(def.statPath);
      }

      String uniqueName = baseName;
      int suffix = 2;

      while (usedNames.contains(uniqueName)) {
         uniqueName = baseName + "_" + suffix++;
      }

      boolean changed = !uniqueName.equals(def.name);
      def.name = uniqueName;
      usedNames.add(uniqueName);
      return changed;
   }

   private LeaderboardConfig.HologramDefinition findById(String id) {
      for (LeaderboardConfig.HologramDefinition hologram : this.holograms) {
         if (hologram.id.equals(id)) {
            return hologram;
         }
      }

      return null;
   }

   public static class HologramDefinition {
      public String id;
      public String name;
      public String worldId;
      public double x;
      public double y;
      public double z;
      public String statPath;
      public int topCount = 10;
      public String theme = "clean";

      public HologramDefinition() {
      }

      public HologramDefinition(String name, String worldId, double x, double y, double z, String statPath, int topCount) {
         this(UUID.randomUUID().toString(), name, worldId, x, y, z, statPath, topCount, "clean");
      }

      public HologramDefinition(String name, String worldId, double x, double y, double z, String statPath, int topCount, String theme) {
         this(UUID.randomUUID().toString(), name, worldId, x, y, z, statPath, topCount, theme);
      }

      public HologramDefinition(String id, String name, String worldId, double x, double y, double z, String statPath, int topCount) {
         this(id, name, worldId, x, y, z, statPath, topCount, "clean");
      }

      public HologramDefinition(String id, String name, String worldId, double x, double y, double z, String statPath, int topCount, String theme) {
         this.id = id;
         this.name = name;
         this.worldId = worldId;
         this.x = x;
         this.y = y;
         this.z = z;
         this.statPath = statPath;
         this.topCount = topCount;
         this.theme = theme;
      }

      boolean normalize() {
         boolean changed = false;
         if (this.id != null && !this.id.isBlank()) {
            String normalizedId = this.id.trim();
            if (!normalizedId.equals(this.id)) {
               this.id = normalizedId;
               changed = true;
            }
         } else {
            this.id = UUID.randomUUID().toString();
            changed = true;
         }

         String normalizedWorldId = this.worldId == null ? "" : this.worldId.trim();
         if (!normalizedWorldId.equals(this.worldId)) {
            this.worldId = normalizedWorldId;
            changed = true;
         }

         String normalizedName = HologramNameHelper.normalize(this.name);
         if (!normalizedName.equals(this.name)) {
            this.name = normalizedName;
            changed = true;
         }

         StatisticPathValidator.ValidationResult statValidation = StatisticPathValidator.validate(this.statPath);
         String normalizedStatPath = statValidation.valid() ? statValidation.normalizedPath() : StatisticPathValidator.normalize(this.statPath);
         if (!normalizedStatPath.equals(this.statPath)) {
            this.statPath = normalizedStatPath;
            changed = true;
         }

         String normalizedTheme = HologramThemeRegistry.normalize(this.theme);
         if (!HologramThemeRegistry.isKnownTheme(normalizedTheme)) {
            normalizedTheme = "clean";
         }

         if (!normalizedTheme.equals(this.theme)) {
            this.theme = normalizedTheme;
            changed = true;
         }

         int clampedTopCount = LeaderboardConfig.clampTopCount(this.topCount);
         if (clampedTopCount != this.topCount) {
            this.topCount = clampedTopCount;
            changed = true;
         }

         return changed;
      }

      boolean isValid() {
         if (this.worldId == null || this.worldId.isBlank()) {
            return false;
         } else {
            return !HologramNameHelper.validate(this.name).valid() ? false : StatisticPathValidator.validate(this.statPath).valid();
         }
      }

      String duplicateKey() {
         return String.format(Locale.ROOT, "%s|%.2f|%.2f|%.2f|%s|%d", this.worldId, this.x, this.y, this.z, this.statPath, this.topCount);
      }
   }
}
