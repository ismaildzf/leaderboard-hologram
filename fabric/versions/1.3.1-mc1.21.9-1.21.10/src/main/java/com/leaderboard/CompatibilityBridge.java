package com.leaderboard;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.Vec3d;

public final class CompatibilityBridge {
   private CompatibilityBridge() {
   }

   public static Vec3d getEntityPos(Entity entity) {
      return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
   }

   public static ServerWorld getEntityWorld(Entity entity) {
      if (entity.getEntityWorld() instanceof ServerWorld serverWorld) {
         return serverWorld;
      } else {
         throw new IllegalStateException("Could not resolve world for entity " + entity.getClass().getName());
      }
   }

   public static ServerWorld getServerPlayerWorld(ServerPlayerEntity player) {
      ServerWorld var2 = player.getEntityWorld();
      if (var2 instanceof ServerWorld) {
         return var2;
      } else {
         throw new IllegalStateException("Could not resolve world for player entity " + player.getClass().getName());
      }
   }

   public static boolean hasPermissionLevel(ServerCommandSource source, int requiredLevel) {
      return source.hasPermissionLevel(requiredLevel);
   }

   public static void applyTextDisplayStyle(
      TextDisplayEntity display,
      Text text,
      int lineWidth,
      int background,
      byte textOpacity,
      boolean shadow,
      boolean seeThrough,
      boolean defaultBackground,
      String alignment
   ) {
      ServerWorld world = getEntityWorld(display);
      NbtWriteView view = NbtWriteView.create(ErrorReporter.EMPTY, world.getRegistryManager());
      display.writeData(view);
      view.put("text", TextCodecs.CODEC, text);
      view.putString("billboard", BillboardMode.CENTER.asString());
      view.putInt("line_width", lineWidth);
      view.putInt("background", background);
      view.putByte("text_opacity", textOpacity);
      view.putBoolean("shadow", shadow);
      view.putBoolean("see_through", seeThrough);
      view.putBoolean("default_background", defaultBackground);
      view.putString("alignment", alignment == null ? "center" : alignment);
      display.readData(NbtReadView.create(ErrorReporter.EMPTY, world.getRegistryManager(), view.getNbt()));
   }

   public static void writeEntityData(Entity entity, NbtCompound nbt) {
      ServerWorld world = getEntityWorld(entity);
      NbtWriteView view = NbtWriteView.create(ErrorReporter.EMPTY, world.getRegistryManager());
      entity.writeData(view);
      nbt.copyFrom(view.getNbt());
   }

   public static void readEntityData(Entity entity, NbtCompound nbt) {
      ServerWorld world = getEntityWorld(entity);
      entity.readData(NbtReadView.create(ErrorReporter.EMPTY, world.getRegistryManager(), nbt));
   }
}
