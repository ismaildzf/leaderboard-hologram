package com.leaderboard;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HologramManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("leaderboard");
   private static final int DEFAULT_SPAWN_LINE_WIDTH = 200;
   private static final int TRANSPARENT_BACKGROUND = 0;
   private final List<HologramManager.TrackedHologram> tracked = new ArrayList<>();

   public TextDisplay spawnHologram(ServerLevel world, Vec3 pos, String id, String name, String statPath, int topCount, String themeId) {
      this.purgeInvalid();
      if (this.findById(id) != null) {
         LOGGER.warn("[Leaderboard] Refusing to spawn duplicate hologram id: {}", id);
         return null;
      } else {
         TextDisplay display = this.createDisplay(world, pos);
         if (!CompatibilityBridge.addFreshEntity(world, display)) {
            LOGGER.warn("[Leaderboard] Failed to spawn hologram at ({}, {}, {}) for stat {}", new Object[]{pos.x, pos.y, pos.z, statPath});
            return null;
         } else {
            String worldId = CompatibilityBridge.getWorldId(world);
            this.tracked.add(new HologramManager.TrackedHologram(id, name, display, statPath, worldId, LeaderboardConfig.clampTopCount(topCount), themeId));
            LOGGER.info("[Leaderboard] Spawned hologram {} ({}) at ({}, {}, {}) for stat: {}", new Object[]{id, name, pos.x, pos.y, pos.z, statPath});
            return display;
         }
      }
   }

   public void updateHologramText(TextDisplay entity, String hologramName, List<LeaderboardEntry> entries, String statPath, ServerLevel world) {
      if (entity != null && !entity.isRemoved()) {
         HologramManager.TrackedHologram trackedHologram = this.findByEntity(entity);
         if (trackedHologram != null) {
            HologramTheme theme = HologramThemeRegistry.get(trackedHologram.themeId);
            List<HologramManager.LineSpec> lines = this.buildLineSpecs(hologramName, entries, statPath, trackedHologram.topCount, theme);
            this.syncDisplays(trackedHologram, world, CompatibilityBridge.getEntityPos(trackedHologram.entity), lines);
         }
      }
   }

   public int purgeInvalid() {
      int removed = 0;
      Iterator<HologramManager.TrackedHologram> iterator = this.tracked.iterator();

      while (iterator.hasNext()) {
         HologramManager.TrackedHologram trackedHologram = iterator.next();
         if (trackedHologram.entity != null && !trackedHologram.entity.isRemoved()) {
            trackedHologram.extraEntities.removeIf(extra -> extra == null || extra.isRemoved());
         } else {
            this.discardExtraEntities(trackedHologram);
            iterator.remove();
            removed++;
         }
      }

      return removed;
   }

   public boolean hasNearby(ServerLevel world, Vec3 pos, double maxDistance) {
      this.purgeInvalid();
      String worldId = CompatibilityBridge.getWorldId(world);

      for (HologramManager.TrackedHologram trackedHologram : this.tracked) {
         if (trackedHologram.worldId.equals(worldId) && CompatibilityBridge.getEntityPos(trackedHologram.entity).distanceTo(pos) <= maxDistance) {
            return true;
         }
      }

      return false;
   }

   public boolean removeById(String id) {
      Iterator<HologramManager.TrackedHologram> iterator = this.tracked.iterator();

      while (iterator.hasNext()) {
         HologramManager.TrackedHologram trackedHologram = iterator.next();
         if (trackedHologram.id.equals(id)) {
            this.discardAllEntities(trackedHologram);
            iterator.remove();
            return true;
         }
      }

      return false;
   }

   public HologramManager.TrackedHologram findByName(String rawName) {
      String normalizedName = HologramNameHelper.normalize(rawName);

      for (HologramManager.TrackedHologram trackedHologram : this.tracked) {
         if (trackedHologram.name.equals(normalizedName)) {
            return trackedHologram;
         }
      }

      return null;
   }

   public boolean rename(String id, String rawNewName) {
      String normalizedName = HologramNameHelper.normalize(rawNewName);

      for (HologramManager.TrackedHologram trackedHologram : this.tracked) {
         if (trackedHologram.id.equals(id)) {
            trackedHologram.name = normalizedName;
            return true;
         }
      }

      return false;
   }

   public boolean moveTo(String id, ServerLevel targetWorld, Vec3 pos) {
      for (int i = 0; i < this.tracked.size(); i++) {
         HologramManager.TrackedHologram trackedHologram = this.tracked.get(i);
         if (trackedHologram.id.equals(id)) {
            if (CompatibilityBridge.getEntityWorld(trackedHologram.entity) == targetWorld) {
               CompatibilityBridge.moveEntity(trackedHologram.entity, pos);
               this.discardExtraEntities(trackedHologram);
               trackedHologram.worldId = CompatibilityBridge.getWorldId(targetWorld);
               return true;
            }

            this.discardAllEntities(trackedHologram);
            TextDisplay replacement = this.createDisplay(targetWorld, pos);
            if (!CompatibilityBridge.addFreshEntity(targetWorld, replacement)) {
               return false;
            }

            trackedHologram.entity = replacement;
            trackedHologram.worldId = CompatibilityBridge.getWorldId(targetWorld);
            return true;
         }
      }

      return false;
   }

   public boolean updateTopCount(String id, int topCount) {
      int normalizedTopCount = LeaderboardConfig.clampTopCount(topCount);

      for (HologramManager.TrackedHologram trackedHologram : this.tracked) {
         if (trackedHologram.id.equals(id)) {
            trackedHologram.topCount = normalizedTopCount;
            return true;
         }
      }

      return false;
   }

   public boolean updateTheme(String id, String rawThemeId) {
      String normalizedThemeId = HologramThemeRegistry.normalize(rawThemeId);
      if (!HologramThemeRegistry.isKnownTheme(normalizedThemeId)) {
         return false;
      } else {
         for (HologramManager.TrackedHologram trackedHologram : this.tracked) {
            if (trackedHologram.id.equals(id)) {
               trackedHologram.themeId = normalizedThemeId;
               return true;
            }
         }

         return false;
      }
   }

   public List<HologramManager.TrackedHologram> getTracked() {
      this.purgeInvalid();
      return List.copyOf(this.tracked);
   }

   public HologramManager.TrackedHologram removeNearest(ServerLevel world, Vec3 pos, double maxDistance) {
      this.purgeInvalid();
      String worldId = CompatibilityBridge.getWorldId(world);
      HologramManager.TrackedHologram closest = null;
      double closestDist = maxDistance;

      for (HologramManager.TrackedHologram trackedHologram : this.tracked) {
         if (trackedHologram.worldId.equals(worldId)) {
            double dist = CompatibilityBridge.getEntityPos(trackedHologram.entity).distanceTo(pos);
            if (dist < closestDist) {
               closestDist = dist;
               closest = trackedHologram;
            }
         }
      }

      if (closest != null) {
         this.discardAllEntities(closest);
         this.tracked.remove(closest);
         LOGGER.info(
            "[Leaderboard] Removed hologram {} at ({}, {}, {})", new Object[]{closest.id, closest.entity.getX(), closest.entity.getY(), closest.entity.getZ()}
         );
      }

      return closest;
   }

   public void removeAll() {
      for (HologramManager.TrackedHologram trackedHologram : this.tracked) {
         this.discardAllEntities(trackedHologram);
      }

      this.tracked.clear();
      LOGGER.info("[Leaderboard] Removed all holograms.");
   }

   private HologramManager.TrackedHologram findById(String id) {
      for (HologramManager.TrackedHologram trackedHologram : this.tracked) {
         if (trackedHologram.id.equals(id)) {
            return trackedHologram;
         }
      }

      return null;
   }

   private TextDisplay createDisplay(ServerLevel world, Vec3 pos) {
      EntityType<?> displayType = BuiltInRegistries.ENTITY_TYPE.getValue(net.minecraft.resources.Identifier.withDefaultNamespace("text_display"));
      if (displayType == null) {
         throw new IllegalStateException("Missing minecraft:text_display entity type");
      }
      TextDisplay display = new TextDisplay(displayType, world);
      CompatibilityBridge.moveEntity(display, pos);
      display.setInvulnerable(true);
      display.setNoGravity(true);
      CompatibilityBridge.applyTextDisplayStyle(display, Component.literal("Loading leaderboard..."), 200, 0, (byte)-1, true, false, false, "center");
      return display;
   }

   private HologramManager.TrackedHologram findByEntity(TextDisplay entity) {
      for (HologramManager.TrackedHologram trackedHologram : this.tracked) {
         if (trackedHologram.entity == entity || trackedHologram.extraEntities.contains(entity)) {
            return trackedHologram;
         }
      }

      return null;
   }

   private List<HologramManager.LineSpec> buildLineSpecs(
      String hologramName, List<LeaderboardEntry> entries, String statPath, int topCount, HologramTheme theme
   ) {
      List<HologramManager.LineSpec> lines = new ArrayList<>();
      String title = StatisticDisplayFormatter.formatTitle(hologramName);
      String subtitle = StatisticDisplayFormatter.formatSubtitle(hologramName, statPath);
      String metaLine = subtitle.isEmpty() ? "Top " + topCount + " players" : subtitle + " | Top " + topCount;
      lines.add(
         new HologramManager.LineSpec(
            Component.literal(title).withStyle(s -> s.withColor(theme.titleColor()).withBold(true)),
            "center",
            theme.headerLineWidth(),
            0.0,
            0.0,
            theme.headerBackground()
         )
      );
      lines.add(
         new HologramManager.LineSpec(
            Component.literal(metaLine).withStyle(s -> s.withColor(theme.metaColor())),
            "center",
            theme.metaLineWidth(),
            0.0,
            -theme.metaOffsetY(),
            theme.metaBackground()
         )
      );
      if (entries.isEmpty()) {
         lines.add(
            new HologramManager.LineSpec(
               Component.literal("Waiting for player data").withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY).withItalic(true)),
               "center",
               theme.headerLineWidth(),
               0.0,
               -theme.entryStartOffsetY(),
               theme.defaultRowBackground()
            )
         );
         return lines;
      } else {
         double yOffset = -theme.entryStartOffsetY();

         for (int i = 0; i < entries.size(); i++) {
            int rank = i + 1;
            lines.add(
               new HologramManager.LineSpec(
                  this.formatEntryLine(theme, rank, entries.get(i), statPath),
                  "center",
                  theme.rowNameLineWidth(),
                  0.0,
                  yOffset,
                  this.getRowBackground(theme, rank)
               )
            );
            yOffset -= theme.entrySpacingY();
         }

         return lines;
      }
   }

   private MutableComponent formatEntryLine(HologramTheme theme, int rank, LeaderboardEntry entry, String statPath) {
      MutableComponent line = Component.empty();
      line.append(this.getRankPrefix(rank));
      line.append(Component.literal(" "));
      line.append(
         Component.literal(this.shortenPlayerName(entry.playerName()))
            .withStyle(s -> s.withColor(rank <= 3 ? theme.topNameColor() : theme.defaultNameColor()).withBold(rank == 1))
      );
      line.append(Component.literal(" - ").withStyle(s -> s.withColor(theme.metaColor())));
      line.append(
         Component.literal(StatisticDisplayFormatter.formatValue(statPath, entry.value()))
            .withStyle(s -> s.withColor(this.getValueColor(theme, rank)).withBold(rank <= 2))
      );
      return line;
   }

   private String shortenPlayerName(String playerName) {
      if (playerName != null && !playerName.isBlank()) {
         return playerName.length() <= 16 ? playerName : playerName.substring(0, 13) + "...";
      } else {
         return "Unknown";
      }
   }

   private void syncDisplays(HologramManager.TrackedHologram trackedHologram, ServerLevel world, Vec3 basePos, List<HologramManager.LineSpec> lines) {
      if (trackedHologram.entity != null && !trackedHologram.entity.isRemoved()) {
         while (trackedHologram.extraEntities.size() < lines.size() - 1) {
            TextDisplay extra = this.createDisplay(world, basePos);
            if (!CompatibilityBridge.addFreshEntity(world, extra)) {
               LOGGER.warn("[Leaderboard] Failed to spawn additional leaderboard display for {}", trackedHologram.id);
               break;
            }

            trackedHologram.extraEntities.add(extra);
         }

         while (trackedHologram.extraEntities.size() > lines.size() - 1) {
            int lastIndex = trackedHologram.extraEntities.size() - 1;
            TextDisplay removed = trackedHologram.extraEntities.remove(lastIndex);
            if (removed != null) {
               removed.discard();
            }
         }

         int renderableLines = Math.min(lines.size(), trackedHologram.extraEntities.size() + 1);

         for (int i = 0; i < renderableLines; i++) {
            TextDisplay display = i == 0 ? trackedHologram.entity : trackedHologram.extraEntities.get(i - 1);
            this.applyLineSpec(display, world, basePos, lines.get(i));
         }
      }
   }

   private void applyLineSpec(TextDisplay display, ServerLevel world, Vec3 basePos, HologramManager.LineSpec lineSpec) {
      CompatibilityBridge.moveEntity(display, new Vec3(basePos.x + lineSpec.xOffset, basePos.y + lineSpec.yOffset, basePos.z));
      CompatibilityBridge.applyTextDisplayStyle(
         display, lineSpec.text, lineSpec.lineWidth, lineSpec.background, (byte)-1, true, false, false, lineSpec.alignment
      );
   }

   private void discardExtraEntities(HologramManager.TrackedHologram trackedHologram) {
      for (TextDisplay extraEntity : trackedHologram.extraEntities) {
         if (extraEntity != null) {
            extraEntity.discard();
         }
      }

      trackedHologram.extraEntities.clear();
   }

   private void discardAllEntities(HologramManager.TrackedHologram trackedHologram) {
      if (trackedHologram.entity != null) {
         trackedHologram.entity.discard();
      }

      this.discardExtraEntities(trackedHologram);
   }

   private MutableComponent getRankPrefix(int rank) {
      return switch (rank) {
         case 1 -> Component.literal("#1").withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true));
         case 2 -> Component.literal("#2").withStyle(s -> s.withColor(ChatFormatting.GRAY).withBold(true));
         case 3 -> Component.literal("#3").withStyle(s -> s.withColor(ChatFormatting.DARK_AQUA).withBold(true));
         default -> Component.literal("#" + rank).withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY));
      };
   }

   private ChatFormatting getValueColor(HologramTheme theme, int rank) {
      return switch (rank) {
         case 1 -> theme.firstValueColor();
         case 2 -> theme.secondValueColor();
         case 3 -> theme.thirdValueColor();
         default -> theme.defaultValueColor();
      };
   }

   private int getRowBackground(HologramTheme theme, int rank) {
      return switch (rank) {
         case 1 -> theme.topOneBackground();
         case 2 -> theme.topTwoBackground();
         case 3 -> theme.topThreeBackground();
         default -> theme.defaultRowBackground();
      };
   }

   private record LineSpec(Component text, String alignment, int lineWidth, double xOffset, double yOffset, int background) {
   }

   public static class TrackedHologram {
      public final String id;
      public String name;
      public TextDisplay entity;
      public final List<TextDisplay> extraEntities;
      public final String statPath;
      public String worldId;
      public int topCount;
      public String themeId;

      public TrackedHologram(String id, String name, TextDisplay entity, String statPath, String worldId, int topCount, String themeId) {
         this.id = id;
         this.name = name;
         this.entity = entity;
         this.extraEntities = new ArrayList<>();
         this.statPath = statPath;
         this.worldId = worldId;
         this.topCount = topCount;
         this.themeId = HologramThemeRegistry.normalize(themeId);
      }
   }
}
