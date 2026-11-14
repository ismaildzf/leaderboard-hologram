package com.leaderboard;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public final class CompatibilityBridge {
    private CompatibilityBridge() {
    }

    public static Vec3d getEntityPos(Entity entity) {
        return new Vec3d(entity.getX(), entity.getY(), entity.getZ());
    }

    public static ServerWorld getEntityWorld(Entity entity) {
        if (entity.getWorld() instanceof ServerWorld world) {
            return world;
        }
        throw new IllegalStateException("Could not resolve world for entity " + entity.getClass().getName());
    }

    public static ServerWorld getServerPlayerWorld(ServerPlayerEntity player) {
        return player.getServerWorld();
    }

    public static boolean hasPermissionLevel(ServerCommandSource source, int requiredLevel) {
        return source.hasPermissionLevel(requiredLevel);
    }

    public static void writeEntityData(Entity entity, NbtCompound nbt) {
        entity.writeNbt(nbt);
    }

    public static void readEntityData(Entity entity, NbtCompound nbt) {
        entity.readNbt(nbt);
    }
}
