package com.leaderboard;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LeaderboardMod implements ModInitializer {
   public static final String MOD_ID = "leaderboard";
   private static final Logger LOGGER = LoggerFactory.getLogger("leaderboard");
   private static final int REFRESH_INTERVAL_TICKS = 6000;
   private final HologramManager hologramManager = new HologramManager();
   private LeaderboardConfig config;
   private MinecraftServer server;
   private int tickCounter = 0;
   private boolean startupBootstrapPending = false;
   private int startupBootstrapDelayTicks = 0;

   public void onInitialize() {
      LOGGER.info("[Leaderboard] Initializing leaderboard hologram mod...");
      Supplier<LeaderboardConfig> configSupplier = () -> this.config;
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(dispatcher, registryAccess, environment) -> {
         LOGGER.info("[Leaderboard] Registering /leaderboard commands. Access requires permission level 2 or console.");
         LeaderboardCommand.register(dispatcher, this.hologramManager, configSupplier, this::forceRefreshAllHolograms, this::reloadAllHolograms);
      });
      ServerLifecycleEvents.SERVER_STARTED.register((ServerStarted)server -> {
         this.server = server;
         Path serverDir = CompatibilityBridge.resolveServerRunDirectory(server);
         Path worldDir = CompatibilityBridge.getWorldSavePath(server);
         this.config = new LeaderboardConfig(this.resolveConfigDirectory(server, serverDir, worldDir));
         this.config.load();
         this.startupBootstrapPending = true;
         this.startupBootstrapDelayTicks = 20;
         LOGGER.info("[Leaderboard] Mod fully loaded. Refresh interval: {} ticks ({} minutes).", 6000, 5);
      });
      ServerLifecycleEvents.SERVER_STOPPING.register((ServerStopping)server -> {
         this.startupBootstrapPending = false;
         this.startupBootstrapDelayTicks = 0;
         this.hologramManager.removeAll();
         LOGGER.info("[Leaderboard] Server stopping, cleaned up holograms.");
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)server -> {
         if (this.startupBootstrapPending) {
            if (this.startupBootstrapDelayTicks > 0) {
               this.startupBootstrapDelayTicks--;
               return;
            }

            this.startupBootstrapPending = false;
            this.respawnSavedHolograms();
            this.refreshAllHolograms();
         }

         this.tickCounter++;
         if (this.tickCounter >= 6000) {
            this.tickCounter = 0;
            this.refreshAllHolograms();
         }
      });
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
            Path serverDir = CompatibilityBridge.resolveServerRunDirectory(this.server);
            Path worldDir = CompatibilityBridge.getWorldSavePath(this.server);
            StatisticSnapshot snapshot = StatisticParser.readSnapshot(this.server, serverDir, worldDir);

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
      } else {
         for (ServerLevel world : CompatibilityBridge.getWorlds(this.server)) {
            if (CompatibilityBridge.getWorldId(world).equals(worldId)) {
               return world;
            }
         }

         return null;
      }
   }

   private Path resolveConfigDirectory(MinecraftServer server, Path serverDir, Path worldDir) {
      return server.isSingleplayer() ? worldDir.resolve("data") : serverDir.resolve("config");
   }
}
