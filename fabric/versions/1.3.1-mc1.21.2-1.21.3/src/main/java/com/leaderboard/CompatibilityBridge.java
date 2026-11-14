package com.leaderboard;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity.BillboardMode;
import net.minecraft.entity.decoration.DisplayEntity.TextDisplayEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serialization;
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
      return player.getServerWorld();
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
      NbtCompound nbt = new NbtCompound();
      display.writeNbt(nbt);
      nbt.putString("text", Serialization.toJsonString(text, getEntityWorld(display).getRegistryManager()));
      nbt.putString("billboard", BillboardMode.CENTER.asString());
      nbt.putInt("line_width", lineWidth);
      nbt.putInt("background", background);
      nbt.putByte("text_opacity", textOpacity);
      nbt.putBoolean("shadow", shadow);
      nbt.putBoolean("see_through", seeThrough);
      nbt.putBoolean("default_background", defaultBackground);
      nbt.putString("alignment", alignment == null ? "center" : alignment);
      display.readNbt(nbt);
   }

   public static void writeEntityData(Entity entity, NbtCompound nbt) {
      entity.writeNbt(nbt);
   }

   public static void readEntityData(Entity entity, NbtCompound nbt) {
      entity.readNbt(nbt);
   }
}
