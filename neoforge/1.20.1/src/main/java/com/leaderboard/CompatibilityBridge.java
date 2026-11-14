package com.leaderboard;

import java.io.File;
import java.nio.file.Path;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Display.BillboardConstraints;
import net.minecraft.world.entity.Display.TextDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.LevelResource;
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
        }
        throw new IllegalStateException("Could not resolve world for entity " + entity.getClass().getName());
    }

    public static ServerLevel getServerPlayerWorld(ServerPlayer player) {
        return getEntityWorld(player);
    }

    public static boolean hasPermissionLevel(CommandSourceStack source, int requiredLevel) {
        return source.hasPermission(requiredLevel);
    }

    public static ServerLevel getSourceWorld(CommandSourceStack source) {
        ServerLevel level = source.getLevel();
        if (level != null) {
            return level;
        }
        throw new IllegalStateException("Could not resolve world for command source.");
    }

    public static ServerPlayer getSourcePlayer(CommandSourceStack source) {
        return source.getPlayer();
    }

    public static String getWorldId(ServerLevel world) {
        return world.dimension().location().toString();
    }

    public static Iterable<ServerLevel> getWorlds(MinecraftServer server) {
        return server.getAllLevels();
    }

    public static Path resolveServerRunDirectory(MinecraftServer server) {
        Object runDirectory = server.getServerDirectory();
        if (runDirectory instanceof Path path) {
            return path.toAbsolutePath().normalize();
        } else if (runDirectory instanceof File file) {
            return file.toPath().toAbsolutePath().normalize();
        }
        throw new IllegalStateException("Unsupported server run directory type: " + runDirectory);
    }

    public static Path getWorldSavePath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT);
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
        CompoundTag tag = new CompoundTag();
        display.saveWithoutId(tag);
        tag.putString("text", Component.Serializer.toJson(text));
        tag.putString("billboard", BillboardConstraints.CENTER.getSerializedName());
        tag.putInt("line_width", lineWidth);
        tag.putInt("background", background);
        tag.putByte("text_opacity", textOpacity);
        tag.putBoolean("shadow", shadow);
        tag.putBoolean("see_through", seeThrough);
        tag.putBoolean("default_background", defaultBackground);
        tag.putString("alignment", normalizeAlignment(alignment));
        display.load(tag);
    }

    public static void writeEntityData(Entity entity, CompoundTag nbt) {
        entity.saveWithoutId(nbt);
    }

    public static void readEntityData(Entity entity, CompoundTag nbt) {
        entity.load(nbt);
    }

    private static String normalizeAlignment(String alignment) {
        String normalized = alignment == null ? "center" : alignment.trim().toLowerCase();
        return switch (normalized) {
            case "left", "right", "center" -> normalized;
            default -> "center";
        };
    }
}
