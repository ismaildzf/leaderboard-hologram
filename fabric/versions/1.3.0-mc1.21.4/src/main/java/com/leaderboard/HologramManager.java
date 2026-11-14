package com.leaderboard;

import com.google.gson.JsonObject;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HologramManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("leaderboard");
   private static final int DEFAULT_SPAWN_LINE_WIDTH = 200;
   private static final int TRANSPARENT_BACKGROUND = 0;
   private final List<HologramManager.TrackedHologram> tracked = new ArrayList<>();

   public TextDisplayEntity spawnHologram(ServerWorld world, Vec3d pos, String id, String name, String statPath, int topCount, String themeId) {
      this.purgeInvalid();
      if (this.findById(id) != null) {
         LOGGER.warn("[Leaderboard] Refusing to spawn duplicate hologram id: {}", id);
         return null;
      } else {
         TextDisplayEntity display = this.createDisplay(world, pos);
         if (!world.spawnEntity(display)) {
            LOGGER.warn("[Leaderboard] Failed to spawn hologram at ({}, {}, {}) for stat {}", new Object[]{pos.x, pos.y, pos.z, statPath});
            return null;
         } else {
            String worldId = world.getRegistryKey().getValue().toString();
            this.tracked.add(new HologramManager.TrackedHologram(id, name, display, statPath, worldId, LeaderboardConfig.clampTopCount(topCount), themeId));
            LOGGER.info("[Leaderboard] Spawned hologram {} ({}) at ({}, {}, {}) for stat: {}", new Object[]{id, name, pos.x, pos.y, pos.z, statPath});
            return display;
         }
      }
   }

   public void updateHologramText(TextDisplayEntity entity, String hologramName, List<LeaderboardEntry> entries, String statPath, ServerWorld world) {
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

   public boolean hasNearby(ServerWorld world, Vec3d pos, double maxDistance) {
      this.purgeInvalid();
      String worldId = world.getRegistryKey().getValue().toString();

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

   public boolean moveTo(String id, ServerWorld targetWorld, Vec3d pos) {
      for (int i = 0; i < this.tracked.size(); i++) {
         HologramManager.TrackedHologram trackedHologram = this.tracked.get(i);
         if (trackedHologram.id.equals(id)) {
            if (CompatibilityBridge.getEntityWorld(trackedHologram.entity) == targetWorld) {
               trackedHologram.entity.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0F, 0.0F);
               this.discardExtraEntities(trackedHologram);
               trackedHologram.worldId = targetWorld.getRegistryKey().getValue().toString();
               return true;
            }

            this.discardAllEntities(trackedHologram);
            TextDisplayEntity replacement = this.createDisplay(targetWorld, pos);
            if (!targetWorld.spawnEntity(replacement)) {
               return false;
            }

            trackedHologram.entity = replacement;
            trackedHologram.worldId = targetWorld.getRegistryKey().getValue().toString();
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

   public HologramManager.TrackedHologram removeNearest(ServerWorld world, Vec3d pos, double maxDistance) {
      this.purgeInvalid();
      String worldId = world.getRegistryKey().getValue().toString();
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

   private TextDisplayEntity createDisplay(ServerWorld world, Vec3d pos) {
      TextDisplayEntity display = new TextDisplayEntity(EntityType.TEXT_DISPLAY, world);
      display.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0.0F, 0.0F);
      display.setInvulnerable(true);
      display.setNoGravity(true);
      NbtCompound nbt = new NbtCompound();
      CompatibilityBridge.writeEntityData(display, nbt);
      nbt.putString("text", this.serializeText(Text.literal("Loading leaderboard..."), world));
      nbt.putString("billboard", BillboardMode.CENTER.asString());
      nbt.putInt("line_width", 200);
      nbt.putInt("background", 0);
      nbt.putByte("text_opacity", (byte)-1);
      nbt.putBoolean("shadow", true);
      nbt.putBoolean("see_through", false);
      nbt.putBoolean("default_background", false);
      nbt.putString("alignment", "center");
      CompatibilityBridge.readEntityData(display, nbt);
      return display;
   }

   private HologramManager.TrackedHologram findByEntity(TextDisplayEntity entity) {
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
            Text.literal(title).styled(s -> s.withColor(theme.titleColor()).withBold(true)),
            "center",
            theme.headerLineWidth(),
            0.0,
            0.0,
            theme.headerBackground()
         )
      );
      lines.add(
         new HologramManager.LineSpec(
            Text.literal(metaLine).styled(s -> s.withColor(theme.metaColor())),
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
               Text.literal("Waiting for player data").styled(s -> s.withColor(Formatting.DARK_GRAY).withItalic(true)),
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

   private MutableText formatEntryLine(HologramTheme theme, int rank, LeaderboardEntry entry, String statPath) {
      MutableText line = Text.empty();
      line.append(this.getRankPrefix(rank));
      line.append(Text.literal(" "));
      line.append(
         Text.literal(this.shortenPlayerName(entry.playerName()))
            .styled(s -> s.withColor(rank <= 3 ? theme.topNameColor() : theme.defaultNameColor()).withBold(rank == 1))
      );
      line.append(Text.literal(" - ").styled(s -> s.withColor(theme.metaColor())));
      line.append(
         Text.literal(StatisticDisplayFormatter.formatValue(statPath, entry.value()))
            .styled(s -> s.withColor(this.getValueColor(theme, rank)).withBold(rank <= 2))
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

   private void syncDisplays(HologramManager.TrackedHologram trackedHologram, ServerWorld world, Vec3d basePos, List<HologramManager.LineSpec> lines) {
      if (trackedHologram.entity != null && !trackedHologram.entity.isRemoved()) {
         while (trackedHologram.extraEntities.size() < lines.size() - 1) {
            TextDisplayEntity extra = this.createDisplay(world, basePos);
            if (!world.spawnEntity(extra)) {
               LOGGER.warn("[Leaderboard] Failed to spawn additional leaderboard display for {}", trackedHologram.id);
               break;
            }

            trackedHologram.extraEntities.add(extra);
         }

         while (trackedHologram.extraEntities.size() > lines.size() - 1) {
            int lastIndex = trackedHologram.extraEntities.size() - 1;
            TextDisplayEntity removed = trackedHologram.extraEntities.remove(lastIndex);
            if (removed != null) {
               removed.discard();
            }
         }

         int renderableLines = Math.min(lines.size(), trackedHologram.extraEntities.size() + 1);

         for (int i = 0; i < renderableLines; i++) {
            TextDisplayEntity display = i == 0 ? trackedHologram.entity : trackedHologram.extraEntities.get(i - 1);
            this.applyLineSpec(display, world, basePos, lines.get(i));
         }
      }
   }

   private void applyLineSpec(TextDisplayEntity display, ServerWorld world, Vec3d basePos, HologramManager.LineSpec lineSpec) {
      display.refreshPositionAndAngles(basePos.x + lineSpec.xOffset, basePos.y + lineSpec.yOffset, basePos.z, 0.0F, 0.0F);
      NbtCompound nbt = new NbtCompound();
      CompatibilityBridge.writeEntityData(display, nbt);
      nbt.putString("text", this.serializeText(lineSpec.text, world));
      nbt.putString("billboard", BillboardMode.CENTER.asString());
      nbt.putInt("line_width", lineSpec.lineWidth);
      nbt.putInt("background", lineSpec.background);
      nbt.putByte("text_opacity", (byte)-1);
      nbt.putBoolean("shadow", true);
      nbt.putBoolean("see_through", false);
      nbt.putBoolean("default_background", false);
      nbt.putString("alignment", lineSpec.alignment);
      CompatibilityBridge.readEntityData(display, nbt);
   }

   private void discardExtraEntities(HologramManager.TrackedHologram trackedHologram) {
      for (TextDisplayEntity extraEntity : trackedHologram.extraEntities) {
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

   private String serializeText(Text text, ServerWorld world) {
      Object registries = world.getRegistryManager();
      String serialized = this.invokeStaticTextSerializer("net.minecraft.text.Text$Serialization", "toJsonString", text, registries);
      if (serialized != null) {
         return serialized;
      } else {
         serialized = this.invokeStaticTextSerializer("net.minecraft.text.Text$Serializer", "toJson", text);
         if (serialized != null) {
            return serialized;
         } else {
            JsonObject fallback = new JsonObject();
            fallback.addProperty("text", text.getString());
            return fallback.toString();
         }
      }
   }

   private String invokeStaticTextSerializer(String className, String methodName, Object... args) {
      try {
         Class<?> serializerClass = Class.forName(className);

         for (Method method : serializerClass.getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())
               && method.getName().equals(methodName)
               && method.getParameterCount() == args.length
               && this.parametersMatch(method.getParameterTypes(), args)
               && method.invoke(null, args) instanceof String json) {
               return json;
            }
         }
      } catch (ReflectiveOperationException var11) {
         LOGGER.debug("[Leaderboard] Text serializer {}.{} unavailable: {}", new Object[]{className, methodName, var11.getMessage()});
      }

      return null;
   }

   private boolean parametersMatch(Class<?>[] parameterTypes, Object[] args) {
      for (int i = 0; i < parameterTypes.length; i++) {
         Object argument = args[i];
         if (argument != null && !parameterTypes[i].isInstance(argument)) {
            return false;
         }
      }

      return true;
   }

   private MutableText getRankPrefix(int rank) {
      return switch (rank) {
         case 1 -> Text.literal("#1").styled(s -> s.withColor(Formatting.GOLD).withBold(true));
         case 2 -> Text.literal("#2").styled(s -> s.withColor(Formatting.GRAY).withBold(true));
         case 3 -> Text.literal("#3").styled(s -> s.withColor(Formatting.DARK_AQUA).withBold(true));
         default -> Text.literal("#" + rank).styled(s -> s.withColor(Formatting.DARK_GRAY));
      };
   }

   private Formatting getValueColor(HologramTheme theme, int rank) {
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

   private record LineSpec(Text text, String alignment, int lineWidth, double xOffset, double yOffset, int background) {
   }

   public static class TrackedHologram {
      public final String id;
      public String name;
      public TextDisplayEntity entity;
      public final List<TextDisplayEntity> extraEntities;
      public final String statPath;
      public String worldId;
      public int topCount;
      public String themeId;

      public TrackedHologram(String id, String name, TextDisplayEntity entity, String statPath, String worldId, int topCount, String themeId) {
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
