package com.leaderboard;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LeaderboardMod.MOD_ID)
public class LeaderboardMod {
    public static final String MOD_ID = "leaderboard";
    private static final Logger LOGGER = LoggerFactory.getLogger("leaderboard");
    private static final int REFRESH_INTERVAL_TICKS = 6000;
    private final HologramManager hologramManager = new HologramManager();
    private LeaderboardConfig config;
    private MinecraftServer server;
    private int tickCounter;

    public LeaderboardMod() {
        LOGGER.info("[Leaderboard] Initializing leaderboard hologram mod (NeoForge 1.20.1)...");
        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStarted);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStopping);
        MinecraftForge.EVENT_BUS.addListener(this::onServerTickPost);
    }

    private void onRegisterCommands(RegisterCommandsEvent event) {
        LOGGER.info("[Leaderboard] Registering /leaderboard commands. Access requires permission level 2 or console.");
        Supplier<LeaderboardConfig> configSupplier = () -> this.config;
        LeaderboardCommand.register(
            event.getDispatcher(),
            this.hologramManager,
            configSupplier,
            this::forceRefreshAllHolograms,
            this::reloadAllHolograms
        );
    }

    private void onServerStarted(ServerStartedEvent event) {
        this.server = event.getServer();
        Path serverDir = CompatibilityBridge.resolveServerRunDirectory(this.server);
        this.config = new LeaderboardConfig(serverDir.resolve("config"));
        this.config.load();
        this.respawnSavedHolograms();
        this.refreshAllHolograms();
        LOGGER.info("[Leaderboard] Mod fully loaded. Refresh interval: {} ticks ({} minutes).", REFRESH_INTERVAL_TICKS, 5);
    }

    private void onServerStopping(ServerStoppingEvent event) {
        this.hologramManager.removeAll();
        LOGGER.info("[Leaderboard] Server stopping, cleaned up holograms.");
    }

    private void onServerTickPost(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        this.tickCounter++;
        if (this.tickCounter >= REFRESH_INTERVAL_TICKS) {
            this.tickCounter = 0;
            this.refreshAllHolograms();
        }
    }

    private void respawnSavedHolograms() {
        if (this.server != null && this.config != null) {
            int respawned = 0;
            for (LeaderboardConfig.HologramDefinition def : this.config.getHolograms()) {
                ServerLevel world = this.findWorld(def.worldId);
                if (world == null) {
                    LOGGER.warn("[Leaderboard] Could not find world '{}' for saved hologram {}.", def.worldId, def.id);
                } else {
                    Vec3 pos = new Vec3(def.x, def.y, def.z);
                    if (this.hologramManager.spawnHologram(world, pos, def.id, def.name, def.statPath, def.topCount, def.theme) != null) {
                        respawned++;
                    }
                }
            }
            LOGGER.info("[Leaderboard] Respawned {} of {} saved hologram(s).", respawned, this.config.getHolograms().size());
        }
    }

    private void refreshAllHolograms() {
        if (this.server != null) {
            List<HologramManager.TrackedHologram> holograms = this.hologramManager.getTracked();
            if (!holograms.isEmpty()) {
                this.server.getPlayerList().saveAll();
                Path serverDir = CompatibilityBridge.resolveServerRunDirectory(this.server);
                Path worldDir = CompatibilityBridge.getWorldSavePath(this.server);
                StatisticSnapshot snapshot = StatisticParser.readSnapshot(serverDir, worldDir);

                for (HologramManager.TrackedHologram trackedHologram : holograms) {
                    ServerLevel world = this.findWorld(trackedHologram.worldId);
                    if (world != null) {
                        List<LeaderboardEntry> entries = snapshot.topPlayers(trackedHologram.statPath, trackedHologram.topCount);
                        this.hologramManager.updateHologramText(trackedHologram.entity, trackedHologram.name, entries, trackedHologram.statPath, world);
                    }
                }

                LOGGER.debug("[Leaderboard] Refreshed all holograms.");
            }
        }
    }

    private void forceRefreshAllHolograms() {
        this.tickCounter = 0;
        this.refreshAllHolograms();
    }

    private void reloadAllHolograms() {
        this.tickCounter = 0;
        this.hologramManager.removeAll();
        if (this.config != null) {
            this.config.load();
        }
        this.respawnSavedHolograms();
        this.refreshAllHolograms();
    }

    private ServerLevel findWorld(String worldId) {
        if (this.server == null) {
            return null;
        }
        for (ServerLevel world : this.server.getAllLevels()) {
            if (CompatibilityBridge.getWorldId(world).equals(worldId)) {
                return world;
            }
        }
        return null;
    }
}
