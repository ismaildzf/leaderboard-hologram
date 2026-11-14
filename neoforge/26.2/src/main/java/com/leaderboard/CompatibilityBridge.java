package com.leaderboard;

import java.io.File;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.server.permissions.Permission.HasCommandLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.Display.TextDisplay.Align;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

public final class CompatibilityBridge {
   private CompatibilityBridge() {
   }

   public static Vec3 getEntityPos(Entity entity) {
      return new Vec3(entity.getX(), entity.getY(), entity.getZ());
   }

   public static ServerLevel getEntityWorld(Entity entity) {
      if (entity.level() instanceof ServerLevel serverLevel) {
         return serverLevel;
      } else {
         throw new IllegalStateException("Could not resolve world for entity " + entity.getClass().getName());
      }
   }

   public static ServerLevel getServerPlayerWorld(ServerPlayer player) {
      return getEntityWorld(player);
   }

   public static boolean hasPermissionLevel(CommandSourceStack source, int requiredLevel) {
      return source.getEntity() == null ? true : source.permissions().hasPermission(new HasCommandLevel(PermissionLevel.byId(requiredLevel)));
   }

   public static ServerLevel getSourceWorld(CommandSourceStack source) {
      ServerLevel var2 = source.getLevel();
      if (var2 instanceof ServerLevel) {
         return var2;
      } else {
         throw new IllegalStateException("Could not resolve world for command source.");
      }
   }

   public static ServerPlayer getSourcePlayer(CommandSourceStack source) {
      ServerPlayer var2 = source.getPlayer();
      return var2 instanceof ServerPlayer ? var2 : null;
   }

   public static String getWorldId(ServerLevel world) {
      return world.dimension().identifier().toString();
   }

   public static Iterable<ServerLevel> getWorlds(MinecraftServer server) {
      return server.getAllLevels();
   }

   public static Path resolveServerRunDirectory(MinecraftServer server) {
      Object runDirectory = server.getServerDirectory();
      if (runDirectory instanceof Path path) {
         return path;
      } else if (runDirectory instanceof File file) {
         return file.toPath();
      } else {
         throw new IllegalStateException("Unsupported server run directory type: " + runDirectory);
      }
   }

   public static Path getWorldSavePath(MinecraftServer server) {
      Path var2 = server.getWorldPath(LevelResource.ROOT);
      if (var2 instanceof Path) {
         return var2;
      } else {
         throw new IllegalStateException("Could not resolve world save path for server.");
      }
   }

   public static void saveAllPlayerData(MinecraftServer server) {
      server.getPlayerList().saveAll();
   }

   public static boolean addFreshEntity(ServerLevel world, Entity entity) {
      return world.addFreshEntity(entity);
   }

   public static void moveEntity(Entity entity, Vec3 pos) {
      entity.setPos(pos);
   }

   public static void applyTextDisplayStyle(
      TextDisplay display,
      Component text,
      int lineWidth,
      int background,
      byte textOpacity,
      boolean shadow,
      boolean seeThrough,
      boolean defaultBackground,
      String alignment
   ) {
      TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, getEntityWorld(display).registryAccess());
      display.saveWithoutId(output);
      output.store("text", ComponentSerialization.CODEC, text);
      output.store("billboard", BillboardConstraints.CODEC, BillboardConstraints.CENTER);
      output.putInt("line_width", lineWidth);
      output.putInt("background", background);
      output.putByte("text_opacity", textOpacity);
      output.putBoolean("shadow", shadow);
      output.putBoolean("see_through", seeThrough);
      output.putBoolean("default_background", defaultBackground);
      output.store("alignment", Align.CODEC, resolveAlignment(alignment));
      readEntityData(display, output.buildResult());
   }

   public static void writeEntityData(Entity entity, CompoundTag nbt) {
      TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, getEntityWorld(entity).registryAccess());
      entity.saveWithoutId(output);
      nbt.merge(output.buildResult());
   }

   public static void readEntityData(Entity entity, CompoundTag nbt) {
      entity.load(TagValueInput.create(ProblemReporter.DISCARDING, getEntityWorld(entity).registryAccess(), nbt));
   }

   private static Align resolveAlignment(String alignment) {
      String normalized = alignment == null ? "CENTER" : alignment.trim().toUpperCase();
      if (normalized.isEmpty()) {
         normalized = "CENTER";
      }
      return switch (normalized) {
         case "LEFT" -> Align.LEFT;
         case "RIGHT" -> Align.RIGHT;
         default -> Align.CENTER;
      };
   }
}
