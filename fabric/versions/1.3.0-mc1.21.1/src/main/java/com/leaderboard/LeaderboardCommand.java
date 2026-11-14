package com.leaderboard;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;

public class LeaderboardCommand {
   private static final SuggestionProvider<ServerCommandSource> CATEGORY_SUGGESTIONS = (context, builder) -> CommandSource.suggestMatching(
      StatisticPathValidator.commonCategories(), builder
   );
   private static final SuggestionProvider<ServerCommandSource> STAT_KEY_SUGGESTIONS = (context, builder) -> {
      String category = getArgumentOrEmpty(context, "category");
      return CommandSource.suggestMatching(StatisticPathValidator.allStatKeys(category), builder);
   };
   private static final SuggestionProvider<ServerCommandSource> PRESET_SUGGESTIONS = (context, builder) -> CommandSource.suggestMatching(
      StatisticPresetRegistry.presetIds(), builder
   );
   private static final SuggestionProvider<ServerCommandSource> THEME_SUGGESTIONS = (context, builder) -> CommandSource.suggestMatching(
      HologramThemeRegistry.ids(), builder
   );
   private static final SuggestionProvider<ServerCommandSource> QUOTED_STAT_PATH_SUGGESTIONS = (context, builder) -> CommandSource.suggestMatching(
      StatisticPresetRegistry.suggestedStatPaths(), builder
   );

   public static void register(
      CommandDispatcher<ServerCommandSource> dispatcher,
      HologramManager hologramManager,
      Supplier<LeaderboardConfig> configSupplier,
      Runnable refreshCallback,
      Runnable reloadCallback
   ) {
      SuggestionProvider<ServerCommandSource> hologramNameSuggestions = createHologramNameSuggestions(configSupplier);
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                                                            "leaderboard"
                                                         )
                                                         .requires(source -> source.hasPermissionLevel(2)))
                                                      .then(
                                                         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal(
                                                                        "create"
                                                                     )
                                                                     .then(
                                                                        CommandManager.literal("quick")
                                                                           .then(
                                                                              ((RequiredArgumentBuilder)CommandManager.argument(
                                                                                       "preset", StringArgumentType.word()
                                                                                    )
                                                                                    .suggests(PRESET_SUGGESTIONS)
                                                                                    .executes(
                                                                                       ctx -> executeCreateQuickPreset(
                                                                                          ctx, hologramManager, configSupplier, refreshCallback
                                                                                       )
                                                                                    ))
                                                                                 .then(
                                                                                    CommandManager.argument("top_count", IntegerArgumentType.integer(1, 25))
                                                                                       .executes(
                                                                                          ctx -> executeCreateQuickPreset(
                                                                                             ctx, hologramManager, configSupplier, refreshCallback
                                                                                          )
                                                                                       )
                                                                                 )
                                                                           )
                                                                     ))
                                                                  .then(
                                                                     CommandManager.literal("preset")
                                                                        .then(
                                                                           CommandManager.argument("name", StringArgumentType.word())
                                                                              .then(
                                                                                 ((RequiredArgumentBuilder)CommandManager.argument(
                                                                                          "preset", StringArgumentType.word()
                                                                                       )
                                                                                       .suggests(PRESET_SUGGESTIONS)
                                                                                       .executes(
                                                                                          ctx -> executeCreateFromPreset(
                                                                                             ctx, hologramManager, configSupplier, refreshCallback
                                                                                          )
                                                                                       ))
                                                                                    .then(
                                                                                       CommandManager.argument("top_count", IntegerArgumentType.integer(1, 25))
                                                                                          .executes(
                                                                                             ctx -> executeCreateFromPreset(
                                                                                                ctx, hologramManager, configSupplier, refreshCallback
                                                                                             )
                                                                                          )
                                                                                    )
                                                                              )
                                                                        )
                                                                  ))
                                                               .then(
                                                                  CommandManager.literal("path")
                                                                     .then(
                                                                        CommandManager.argument("name", StringArgumentType.word())
                                                                           .then(
                                                                              ((RequiredArgumentBuilder)CommandManager.argument(
                                                                                       "statistic_path", StringArgumentType.string()
                                                                                    )
                                                                                    .suggests(QUOTED_STAT_PATH_SUGGESTIONS)
                                                                                    .executes(
                                                                                       ctx -> executeCreateFromPath(
                                                                                          ctx, hologramManager, configSupplier, refreshCallback
                                                                                       )
                                                                                    ))
                                                                                 .then(
                                                                                    CommandManager.argument("top_count", IntegerArgumentType.integer(1, 25))
                                                                                       .executes(
                                                                                          ctx -> executeCreateFromPath(
                                                                                             ctx, hologramManager, configSupplier, refreshCallback
                                                                                          )
                                                                                       )
                                                                                 )
                                                                           )
                                                                     )
                                                               ))
                                                            .then(
                                                               CommandManager.argument("name", StringArgumentType.word())
                                                                  .then(
                                                                     CommandManager.argument("category", IdentifierArgumentType.identifier())
                                                                        .suggests(CATEGORY_SUGGESTIONS)
                                                                        .then(
                                                                           ((RequiredArgumentBuilder)CommandManager.argument(
                                                                                    "stat_key", IdentifierArgumentType.identifier()
                                                                                 )
                                                                                 .suggests(STAT_KEY_SUGGESTIONS)
                                                                                 .executes(
                                                                                    ctx -> executeCreate(ctx, hologramManager, configSupplier, refreshCallback)
                                                                                 ))
                                                                              .then(
                                                                                 CommandManager.argument("top_count", IntegerArgumentType.integer(1, 25))
                                                                                    .executes(
                                                                                       ctx -> executeCreate(
                                                                                          ctx, hologramManager, configSupplier, refreshCallback
                                                                                       )
                                                                                    )
                                                                              )
                                                                        )
                                                                  )
                                                            )
                                                      ))
                                                   .then(
                                                      ((LiteralArgumentBuilder)CommandManager.literal("remove")
                                                            .then(
                                                               CommandManager.literal("nearest")
                                                                  .executes(ctx -> executeRemoveNearest(ctx, hologramManager, configSupplier))
                                                            ))
                                                         .then(
                                                            CommandManager.argument("name", StringArgumentType.word())
                                                               .suggests(hologramNameSuggestions)
                                                               .executes(ctx -> executeRemoveByName(ctx, hologramManager, configSupplier))
                                                         )
                                                   ))
                                                .then(
                                                   CommandManager.literal("rename")
                                                      .then(
                                                         CommandManager.argument("name", StringArgumentType.word())
                                                            .suggests(hologramNameSuggestions)
                                                            .then(
                                                               CommandManager.argument("new_name", StringArgumentType.word())
                                                                  .executes(ctx -> executeRename(ctx, hologramManager, configSupplier, refreshCallback))
                                                            )
                                                      )
                                                ))
                                             .then(
                                                CommandManager.literal("move")
                                                   .then(
                                                      CommandManager.argument("name", StringArgumentType.word())
                                                         .suggests(hologramNameSuggestions)
                                                         .executes(ctx -> executeMoveToPlayer(ctx, hologramManager, configSupplier, refreshCallback))
                                                   )
                                             ))
                                          .then(
                                             CommandManager.literal("setpos")
                                                .then(
                                                   CommandManager.argument("name", StringArgumentType.word())
                                                      .suggests(hologramNameSuggestions)
                                                      .then(
                                                         CommandManager.argument("pos", Vec3ArgumentType.vec3())
                                                            .executes(ctx -> executeSetPos(ctx, hologramManager, configSupplier, refreshCallback))
                                                      )
                                                )
                                          ))
                                       .then(
                                          CommandManager.literal("settop")
                                             .then(
                                                CommandManager.argument("name", StringArgumentType.word())
                                                   .suggests(hologramNameSuggestions)
                                                   .then(
                                                      CommandManager.argument("top_count", IntegerArgumentType.integer(1, 25))
                                                         .executes(ctx -> executeSetTop(ctx, hologramManager, configSupplier, refreshCallback))
                                                   )
                                             )
                                       ))
                                    .then(
                                       CommandManager.literal("settheme")
                                          .then(
                                             CommandManager.argument("name", StringArgumentType.word())
                                                .suggests(hologramNameSuggestions)
                                                .then(
                                                   CommandManager.argument("theme", StringArgumentType.word())
                                                      .suggests(THEME_SUGGESTIONS)
                                                      .executes(ctx -> executeSetTheme(ctx, hologramManager, configSupplier, refreshCallback))
                                                )
                                          )
                                    ))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setstat")
                                             .then(
                                                CommandManager.literal("preset")
                                                   .then(
                                                      CommandManager.argument("name", StringArgumentType.word())
                                                         .suggests(hologramNameSuggestions)
                                                         .then(
                                                            CommandManager.argument("preset", StringArgumentType.word())
                                                               .suggests(PRESET_SUGGESTIONS)
                                                               .executes(ctx -> executeSetStatFromPreset(ctx, configSupplier, reloadCallback))
                                                         )
                                                   )
                                             ))
                                          .then(
                                             CommandManager.literal("path")
                                                .then(
                                                   CommandManager.argument("name", StringArgumentType.word())
                                                      .suggests(hologramNameSuggestions)
                                                      .then(
                                                         CommandManager.argument("statistic_path", StringArgumentType.string())
                                                            .suggests(QUOTED_STAT_PATH_SUGGESTIONS)
                                                            .executes(ctx -> executeSetStatFromPath(ctx, configSupplier, reloadCallback))
                                                      )
                                                )
                                          ))
                                       .then(
                                          CommandManager.argument("name", StringArgumentType.word())
                                             .suggests(hologramNameSuggestions)
                                             .then(
                                                CommandManager.argument("category", IdentifierArgumentType.identifier())
                                                   .suggests(CATEGORY_SUGGESTIONS)
                                                   .then(
                                                      CommandManager.argument("stat_key", IdentifierArgumentType.identifier())
                                                         .suggests(STAT_KEY_SUGGESTIONS)
                                                         .executes(ctx -> executeSetStat(ctx, configSupplier, reloadCallback))
                                                   )
                                             )
                                       )
                                 ))
                              .then(
                                 CommandManager.literal("info")
                                    .then(
                                       CommandManager.argument("name", StringArgumentType.word())
                                          .suggests(hologramNameSuggestions)
                                          .executes(ctx -> executeInfo(ctx, configSupplier))
                                    )
                              ))
                           .then(CommandManager.literal("list").executes(ctx -> executeList(ctx, hologramManager))))
                        .then(
                           ((LiteralArgumentBuilder)CommandManager.literal("presets").executes(ctx -> executeListPresets(ctx, "")))
                              .then(
                                 CommandManager.argument("query", StringArgumentType.greedyString())
                                    .executes(ctx -> executeListPresets(ctx, StringArgumentType.getString(ctx, "query")))
                              )
                        ))
                     .then(CommandManager.literal("themes").executes(LeaderboardCommand::executeListThemes)))
                  .then(
                     CommandManager.literal("search")
                        .then(
                           CommandManager.argument("query", StringArgumentType.greedyString())
                              .executes(ctx -> executeListPresets(ctx, StringArgumentType.getString(ctx, "query")))
                        )
                  ))
               .then(CommandManager.literal("refresh").executes(ctx -> executeRefresh(ctx, refreshCallback))))
            .then(CommandManager.literal("reload").executes(ctx -> executeReload(ctx, reloadCallback)))
      );
   }

   private static int executeCreate(
      CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      String category = IdentifierArgumentType.getIdentifier(ctx, "category").toString();
      String statKey = IdentifierArgumentType.getIdentifier(ctx, "stat_key").toString();
      return executeCreateInternal(ctx, hologramManager, configSupplier, refreshCallback, StringArgumentType.getString(ctx, "name"), category + "/" + statKey);
   }

   private static int executeCreateFromPath(
      CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      return executeCreateInternal(
         ctx, hologramManager, configSupplier, refreshCallback, StringArgumentType.getString(ctx, "name"), StringArgumentType.getString(ctx, "statistic_path")
      );
   }

   private static int executeCreateFromPreset(
      CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      StatisticPreset preset = resolvePreset(ctx);
      if (preset == null) {
         return 0;
      } else {
         int result = executeCreateInternal(
            ctx, hologramManager, configSupplier, refreshCallback, StringArgumentType.getString(ctx, "name"), preset.statReference()
         );
         if (result == 1) {
            sendPresetFeedback((ServerCommandSource)ctx.getSource(), preset);
         }

         return result;
      }
   }

   private static int executeCreateQuickPreset(
      CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      StatisticPreset preset = resolvePreset(ctx);
      if (preset == null) {
         return 0;
      } else {
         LeaderboardConfig config = configSupplier.get();
         if (config == null) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Leaderboard config is not ready yet."));
            return 0;
         } else {
            int result = executeCreateInternal(
               ctx, hologramManager, configSupplier, refreshCallback, createUniqueHologramName(config, preset.id()), preset.statReference()
            );
            if (result == 1) {
               sendPresetFeedback((ServerCommandSource)ctx.getSource(), preset);
            }

            return result;
         }
      }
   }

   private static int executeCreateInternal(
      CommandContext<ServerCommandSource> ctx,
      HologramManager hologramManager,
      Supplier<LeaderboardConfig> configSupplier,
      Runnable refreshCallback,
      String rawName,
      String rawStatPath
   ) {
      ServerCommandSource source = (ServerCommandSource)ctx.getSource();
      LeaderboardConfig config = configSupplier.get();
      if (config == null) {
         source.sendError(Text.literal("Leaderboard config is not ready yet."));
         return 0;
      } else {
         HologramNameHelper.ValidationResult nameValidation = HologramNameHelper.validate(rawName);
         if (!nameValidation.valid()) {
            source.sendError(Text.literal(nameValidation.error()));
            return 0;
         } else if (config.findByName(nameValidation.normalizedName()) != null) {
            source.sendError(Text.literal("A hologram with that name already exists."));
            return 0;
         } else {
            StatisticPathValidator.ValidationResult validation = StatisticPathValidator.validate(rawStatPath);
            if (!validation.valid()) {
               source.sendError(Text.literal(validation.error()));
               return 0;
            } else {
               int topCount = getTopCount(ctx);
               ServerPlayerEntity player = source.getPlayer();
               if (player == null) {
                  source.sendError(Text.literal("This command must be run by a player."));
                  return 0;
               } else {
                  ServerWorld world = player.getServerWorld();
                  Vec3d pos = player.getPos().add(0.0, 2.5, 0.0);
                  if (hologramManager.hasNearby(world, pos, 1.5)) {
                     source.sendError(Text.literal("Another leaderboard hologram is too close. Move at least 2 blocks away."));
                     return 0;
                  } else {
                     LeaderboardConfig.HologramDefinition def = new LeaderboardConfig.HologramDefinition(
                        nameValidation.normalizedName(),
                        world.getRegistryKey().getValue().toString(),
                        pos.x,
                        pos.y,
                        pos.z,
                        validation.normalizedPath(),
                        topCount,
                        "clean"
                     );
                     if (hologramManager.spawnHologram(world, pos, def.id, def.name, def.statPath, def.topCount, def.theme) == null) {
                        source.sendError(Text.literal("Failed to spawn leaderboard hologram in this world."));
                        return 0;
                     } else if (!config.addHologram(def)) {
                        hologramManager.removeById(def.id);
                        source.sendError(Text.literal("Failed to save leaderboard hologram because it conflicts with an existing entry."));
                        return 0;
                     } else {
                        refreshCallback.run();
                        source.sendFeedback(
                           () -> Text.literal("Created hologram ")
                              .styled(s -> s.withColor(Formatting.GREEN))
                              .append(Text.literal(def.name).styled(s -> s.withColor(Formatting.YELLOW).withBold(true)))
                              .append(Text.literal(" for ").styled(s -> s.withColor(Formatting.GREEN)))
                              .append(Text.literal(describeStatReference(def.statPath)).styled(s -> s.withColor(Formatting.GOLD)))
                              .append(Text.literal(" (top " + def.topCount + ", theme " + def.theme + ")").styled(s -> s.withColor(Formatting.AQUA))),
                           true
                        );
                        if (!validation.knownCategory()) {
                           source.sendFeedback(
                              () -> Text.literal("Note: this statistic category is not a common vanilla category. It may still work for modded stats.")
                                 .styled(s -> s.withColor(Formatting.YELLOW)),
                              false
                           );
                        }

                        return 1;
                     }
                  }
               }
            }
         }
      }
   }

   private static int executeRemoveByName(CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         hologramManager.removeById(resolved.definition.id);
         resolved.config.removeHologram(resolved.definition.id);
         ((ServerCommandSource)ctx.getSource())
            .sendFeedback(
               () -> Text.literal("Removed hologram ")
                  .styled(s -> s.withColor(Formatting.GREEN))
                  .append(Text.literal(resolved.definition.name).styled(s -> s.withColor(Formatting.YELLOW).withBold(true))),
               true
            );
         return 1;
      }
   }

   private static int executeRemoveNearest(CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier) {
      ServerCommandSource source = (ServerCommandSource)ctx.getSource();
      LeaderboardConfig config = configSupplier.get();
      if (config == null) {
         source.sendError(Text.literal("Leaderboard config is not ready yet."));
         return 0;
      } else {
         ServerPlayerEntity player = source.getPlayer();
         if (player == null) {
            source.sendError(Text.literal("This command must be run by a player."));
            return 0;
         } else {
            HologramManager.TrackedHologram removed = hologramManager.removeNearest(player.getServerWorld(), player.getPos(), 10.0);
            if (removed == null) {
               source.sendError(Text.literal("No leaderboard hologram found within 10 blocks."));
               return 0;
            } else {
               config.removeHologram(removed.id);
               source.sendFeedback(
                  () -> Text.literal("Removed nearest hologram ")
                     .styled(s -> s.withColor(Formatting.GREEN))
                     .append(Text.literal(removed.name).styled(s -> s.withColor(Formatting.YELLOW).withBold(true))),
                  true
               );
               return 1;
            }
         }
      }
   }

   private static int executeRename(
      CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         HologramNameHelper.ValidationResult validation = HologramNameHelper.validate(StringArgumentType.getString(ctx, "new_name"));
         if (!validation.valid()) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal(validation.error()));
            return 0;
         } else {
            LeaderboardConfig.HologramDefinition existing = resolved.config.findByName(validation.normalizedName());
            if (existing != null && !existing.id.equals(resolved.definition.id)) {
               ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Another hologram already uses that name."));
               return 0;
            } else if (!resolved.config.renameHologram(resolved.definition.id, validation.normalizedName())) {
               ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Failed to rename hologram."));
               return 0;
            } else {
               hologramManager.rename(resolved.definition.id, validation.normalizedName());
               refreshCallback.run();
               ((ServerCommandSource)ctx.getSource())
                  .sendFeedback(
                     () -> Text.literal("Renamed hologram to ")
                        .styled(s -> s.withColor(Formatting.GREEN))
                        .append(Text.literal(validation.normalizedName()).styled(s -> s.withColor(Formatting.YELLOW).withBold(true))),
                     true
                  );
               return 1;
            }
         }
      }
   }

   private static int executeMoveToPlayer(
      CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         ServerPlayerEntity player = ((ServerCommandSource)ctx.getSource()).getPlayer();
         if (player == null) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("This command must be run by a player."));
            return 0;
         } else {
            ServerWorld world = player.getServerWorld();
            Vec3d pos = player.getPos().add(0.0, 2.5, 0.0);
            return moveHologram(ctx, hologramManager, refreshCallback, resolved, world, pos);
         }
      }
   }

   private static int executeSetPos(
      CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         ServerWorld world = ((ServerCommandSource)ctx.getSource()).getWorld();
         Vec3d pos = Vec3ArgumentType.getVec3(ctx, "pos");
         return moveHologram(ctx, hologramManager, refreshCallback, resolved, world, pos);
      }
   }

   private static int moveHologram(
      CommandContext<ServerCommandSource> ctx,
      HologramManager hologramManager,
      Runnable refreshCallback,
      LeaderboardCommand.ResolvedHologram resolved,
      ServerWorld world,
      Vec3d pos
   ) {
      if (!resolved.config.updatePosition(resolved.definition.id, world.getRegistryKey().getValue().toString(), pos.x, pos.y, pos.z)) {
         ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Failed to save hologram position."));
         return 0;
      } else if (!hologramManager.moveTo(resolved.definition.id, world, pos)) {
         ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Failed to move hologram entity."));
         return 0;
      } else {
         refreshCallback.run();
         ((ServerCommandSource)ctx.getSource())
            .sendFeedback(
               () -> Text.literal("Moved hologram ")
                  .styled(s -> s.withColor(Formatting.GREEN))
                  .append(Text.literal(resolved.definition.name).styled(s -> s.withColor(Formatting.YELLOW).withBold(true)))
                  .append(Text.literal(String.format(" to (%.1f, %.1f, %.1f)", pos.x, pos.y, pos.z)).styled(s -> s.withColor(Formatting.AQUA))),
               true
            );
         return 1;
      }
   }

   private static int executeSetTop(
      CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         int topCount = IntegerArgumentType.getInteger(ctx, "top_count");
         if (!resolved.config.updateTopCount(resolved.definition.id, topCount)) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Failed to update top count."));
            return 0;
         } else {
            hologramManager.updateTopCount(resolved.definition.id, topCount);
            refreshCallback.run();
            ((ServerCommandSource)ctx.getSource())
               .sendFeedback(
                  () -> Text.literal("Updated top count for ")
                     .styled(s -> s.withColor(Formatting.GREEN))
                     .append(Text.literal(resolved.definition.name).styled(s -> s.withColor(Formatting.YELLOW).withBold(true)))
                     .append(Text.literal(" to " + topCount).styled(s -> s.withColor(Formatting.AQUA))),
                  true
               );
            return 1;
         }
      }
   }

   private static int executeSetTheme(
      CommandContext<ServerCommandSource> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         String rawTheme = StringArgumentType.getString(ctx, "theme");
         String normalizedTheme = HologramThemeRegistry.normalize(rawTheme);
         if (!HologramThemeRegistry.isKnownTheme(normalizedTheme)) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Unknown theme '" + rawTheme + "'. Use /leaderboard themes"));
            return 0;
         } else if (!resolved.config.updateTheme(resolved.definition.id, normalizedTheme)) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Failed to update hologram theme."));
            return 0;
         } else if (!hologramManager.updateTheme(resolved.definition.id, normalizedTheme)) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Failed to apply hologram theme to the active entity."));
            return 0;
         } else {
            refreshCallback.run();
            HologramTheme theme = HologramThemeRegistry.get(normalizedTheme);
            ((ServerCommandSource)ctx.getSource())
               .sendFeedback(
                  () -> Text.literal("Updated theme for ")
                     .styled(s -> s.withColor(Formatting.GREEN))
                     .append(Text.literal(resolved.definition.name).styled(s -> s.withColor(Formatting.YELLOW).withBold(true)))
                     .append(Text.literal(" to ").styled(s -> s.withColor(Formatting.GREEN)))
                     .append(Text.literal(theme.id()).styled(s -> s.withColor(Formatting.AQUA).withBold(true)))
                     .append(Text.literal(" (" + theme.displayName() + ")").styled(s -> s.withColor(Formatting.GRAY))),
                  true
               );
            return 1;
         }
      }
   }

   private static int executeSetStat(CommandContext<ServerCommandSource> ctx, Supplier<LeaderboardConfig> configSupplier, Runnable reloadCallback) {
      String category = IdentifierArgumentType.getIdentifier(ctx, "category").toString();
      String statKey = IdentifierArgumentType.getIdentifier(ctx, "stat_key").toString();
      return executeSetStatInternal(ctx, configSupplier, reloadCallback, category + "/" + statKey);
   }

   private static int executeSetStatFromPreset(CommandContext<ServerCommandSource> ctx, Supplier<LeaderboardConfig> configSupplier, Runnable reloadCallback) {
      StatisticPreset preset = resolvePreset(ctx);
      if (preset == null) {
         return 0;
      } else {
         int result = executeSetStatInternal(ctx, configSupplier, reloadCallback, preset.statReference());
         if (result == 1) {
            sendPresetFeedback((ServerCommandSource)ctx.getSource(), preset);
         }

         return result;
      }
   }

   private static int executeSetStatFromPath(CommandContext<ServerCommandSource> ctx, Supplier<LeaderboardConfig> configSupplier, Runnable reloadCallback) {
      return executeSetStatInternal(ctx, configSupplier, reloadCallback, StringArgumentType.getString(ctx, "statistic_path"));
   }

   private static int executeSetStatInternal(
      CommandContext<ServerCommandSource> ctx, Supplier<LeaderboardConfig> configSupplier, Runnable reloadCallback, String rawStatPath
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         StatisticPathValidator.ValidationResult validation = StatisticPathValidator.validate(rawStatPath);
         if (!validation.valid()) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal(validation.error()));
            return 0;
         } else if (!resolved.config.updateStatPath(resolved.definition.id, validation.normalizedPath())) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Failed to update hologram statistic."));
            return 0;
         } else {
            reloadCallback.run();
            ((ServerCommandSource)ctx.getSource())
               .sendFeedback(
                  () -> Text.literal("Updated statistic for ")
                     .styled(s -> s.withColor(Formatting.GREEN))
                     .append(Text.literal(resolved.definition.name).styled(s -> s.withColor(Formatting.YELLOW).withBold(true)))
                     .append(Text.literal(" to ").styled(s -> s.withColor(Formatting.GREEN)))
                     .append(Text.literal(describeStatReference(validation.normalizedPath())).styled(s -> s.withColor(Formatting.GOLD))),
                  true
               );
            return 1;
         }
      }
   }

   private static int executeInfo(CommandContext<ServerCommandSource> ctx, Supplier<LeaderboardConfig> configSupplier) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         LeaderboardConfig.HologramDefinition def = resolved.definition;
         StatisticPreset preset = StatisticPresetRegistry.findByStatPath(def.statPath);
         ((ServerCommandSource)ctx.getSource())
            .sendFeedback(
               () -> Text.literal("Hologram ")
                  .styled(s -> s.withColor(Formatting.GOLD).withBold(true))
                  .append(Text.literal(def.name).styled(s -> s.withColor(Formatting.YELLOW).withBold(true))),
               false
            );
         if (preset != null) {
            ((ServerCommandSource)ctx.getSource())
               .sendFeedback(
                  () -> Text.literal("  preset: " + preset.id() + " (" + preset.displayName() + ")").styled(s -> s.withColor(Formatting.AQUA)), false
               );
         }

         HologramTheme theme = HologramThemeRegistry.get(def.theme);
         ((ServerCommandSource)ctx.getSource())
            .sendFeedback(() -> Text.literal("  theme: " + theme.id() + " (" + theme.displayName() + ")").styled(s -> s.withColor(Formatting.WHITE)), false);
         ((ServerCommandSource)ctx.getSource())
            .sendFeedback(() -> Text.literal("  stat: " + describeStatReference(def.statPath)).styled(s -> s.withColor(Formatting.WHITE)), false);
         ((ServerCommandSource)ctx.getSource())
            .sendFeedback(
               () -> Text.literal(String.format("  pos: %.1f %.1f %.1f [%s]", def.x, def.y, def.z, def.worldId)).styled(s -> s.withColor(Formatting.WHITE)),
               false
            );
         ((ServerCommandSource)ctx.getSource()).sendFeedback(() -> Text.literal("  top: " + def.topCount).styled(s -> s.withColor(Formatting.WHITE)), false);
         return 1;
      }
   }

   private static int executeList(CommandContext<ServerCommandSource> ctx, HologramManager hologramManager) {
      ServerCommandSource source = (ServerCommandSource)ctx.getSource();
      List<HologramManager.TrackedHologram> holograms = hologramManager.getTracked();
      if (holograms.isEmpty()) {
         source.sendFeedback(() -> Text.literal("No active leaderboard holograms.").styled(s -> s.withColor(Formatting.YELLOW)), false);
         return 0;
      } else {
         source.sendFeedback(() -> Text.literal("=== Active Leaderboards ===").styled(s -> s.withColor(Formatting.GOLD).withBold(true)), false);

         for (HologramManager.TrackedHologram trackedHologram : holograms) {
            StatisticPreset preset = StatisticPresetRegistry.findByStatPath(trackedHologram.statPath);
            String statLabel = preset == null ? trackedHologram.statPath : preset.id() + " -> " + describeStatReference(trackedHologram.statPath);
            HologramTheme theme = HologramThemeRegistry.get(trackedHologram.themeId);
            source.sendFeedback(
               () -> Text.literal(
                     String.format(
                        "  %s -> %s at (%.1f, %.1f, %.1f) [%s] top=%d theme=%s",
                        trackedHologram.name,
                        statLabel,
                        trackedHologram.entity.getX(),
                        trackedHologram.entity.getY(),
                        trackedHologram.entity.getZ(),
                        trackedHologram.worldId,
                        trackedHologram.topCount,
                        theme.id()
                     )
                  )
                  .styled(s -> s.withColor(Formatting.WHITE)),
               false
            );
         }

         return holograms.size();
      }
   }

   private static int executeListPresets(CommandContext<ServerCommandSource> ctx, String query) {
      List<StatisticPreset> presets = StatisticPresetRegistry.search(query);
      if (presets.isEmpty()) {
         ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("No presets matched '" + query + "'."));
         return 0;
      } else {
         ((ServerCommandSource)ctx.getSource())
            .sendFeedback(() -> Text.literal("=== Available Presets ===").styled(s -> s.withColor(Formatting.GOLD).withBold(true)), false);

         for (StatisticPreset preset : presets) {
            ((ServerCommandSource)ctx.getSource())
               .sendFeedback(
                  () -> Text.literal("  " + preset.id())
                     .styled(s -> s.withColor(Formatting.YELLOW).withBold(true))
                     .append(Text.literal(" -> " + preset.displayName()).styled(s -> s.withColor(Formatting.WHITE)))
                     .append(Text.literal(" | " + preset.description()).styled(s -> s.withColor(Formatting.GRAY)))
                     .append(
                        preset.isAggregate()
                           ? Text.literal(" | aggregate").styled(s -> s.withColor(Formatting.DARK_GRAY))
                           : Text.literal("").styled(s -> s.withColor(Formatting.GRAY))
                     ),
                  false
               );
         }

         ((ServerCommandSource)ctx.getSource())
            .sendFeedback(
               () -> Text.literal("Use /leaderboard create quick <preset> or /leaderboard create preset <name> <preset>")
                  .styled(s -> s.withColor(Formatting.DARK_GRAY)),
               false
            );
         return presets.size();
      }
   }

   private static int executeListThemes(CommandContext<ServerCommandSource> ctx) {
      ((ServerCommandSource)ctx.getSource())
         .sendFeedback(() -> Text.literal("=== Available Themes ===").styled(s -> s.withColor(Formatting.GOLD).withBold(true)), false);

      for (HologramTheme theme : HologramThemeRegistry.all()) {
         ((ServerCommandSource)ctx.getSource())
            .sendFeedback(
               () -> Text.literal("  " + theme.id())
                  .styled(s -> s.withColor(Formatting.YELLOW).withBold(true))
                  .append(Text.literal(" -> " + theme.displayName()).styled(s -> s.withColor(Formatting.WHITE)))
                  .append(Text.literal(" | " + theme.description()).styled(s -> s.withColor(Formatting.GRAY))),
               false
            );
      }

      ((ServerCommandSource)ctx.getSource())
         .sendFeedback(() -> Text.literal("Use /leaderboard settheme <name> <theme>").styled(s -> s.withColor(Formatting.DARK_GRAY)), false);
      return HologramThemeRegistry.all().size();
   }

   private static int executeRefresh(CommandContext<ServerCommandSource> ctx, Runnable refreshCallback) {
      refreshCallback.run();
      ((ServerCommandSource)ctx.getSource())
         .sendFeedback(() -> Text.literal("Leaderboard data refreshed from live player stats.").styled(s -> s.withColor(Formatting.GREEN)), false);
      return 1;
   }

   private static int executeReload(CommandContext<ServerCommandSource> ctx, Runnable reloadCallback) {
      reloadCallback.run();
      ((ServerCommandSource)ctx.getSource())
         .sendFeedback(() -> Text.literal("Leaderboard holograms reloaded from config.").styled(s -> s.withColor(Formatting.GREEN)), false);
      return 1;
   }

   private static int getTopCount(CommandContext<ServerCommandSource> ctx) {
      boolean hasTopCountArgument = ctx.getNodes().stream().anyMatch(node -> "top_count".equals(node.getNode().getName()));
      return hasTopCountArgument ? IntegerArgumentType.getInteger(ctx, "top_count") : 10;
   }

   private static SuggestionProvider<ServerCommandSource> createHologramNameSuggestions(Supplier<LeaderboardConfig> configSupplier) {
      return (context, builder) -> {
         LeaderboardConfig config = configSupplier.get();
         return config == null ? builder.buildFuture() : CommandSource.suggestMatching(config.getHolograms().stream().map(def -> def.name), builder);
      };
   }

   private static StatisticPreset resolvePreset(CommandContext<ServerCommandSource> ctx) {
      String rawPreset = StringArgumentType.getString(ctx, "preset");
      StatisticPreset preset = StatisticPresetRegistry.find(rawPreset);
      if (preset == null) {
         ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Unknown preset '" + rawPreset + "'. Use /leaderboard presets"));
      }

      return preset;
   }

   private static String createUniqueHologramName(LeaderboardConfig config, String baseName) {
      String normalizedBase = HologramNameHelper.normalize(baseName);
      if (config.findByName(normalizedBase) == null) {
         return normalizedBase;
      } else {
         int suffix = 2;

         while (config.findByName(normalizedBase + "_" + suffix) != null) {
            suffix++;
         }

         return normalizedBase + "_" + suffix;
      }
   }

   private static void sendPresetFeedback(ServerCommandSource source, StatisticPreset preset) {
      source.sendFeedback(
         () -> Text.literal("Preset: ")
            .styled(s -> s.withColor(Formatting.GREEN))
            .append(Text.literal(preset.id()).styled(s -> s.withColor(Formatting.YELLOW).withBold(true)))
            .append(Text.literal(" -> ").styled(s -> s.withColor(Formatting.GREEN)))
            .append(Text.literal(preset.displayName()).styled(s -> s.withColor(Formatting.AQUA)))
            .append(Text.literal(" (" + preset.statReference() + ")").styled(s -> s.withColor(Formatting.GRAY)))
            .append(
               preset.isAggregate()
                  ? Text.literal(" [aggregate]").styled(s -> s.withColor(Formatting.DARK_GRAY))
                  : Text.literal("").styled(s -> s.withColor(Formatting.GRAY))
            ),
         false
      );
   }

   private static String describeStatReference(String statReference) {
      StatisticPreset preset = StatisticPresetRegistry.findByStatPath(statReference);
      if (preset == null) {
         return statReference;
      } else {
         String normalized = StatisticPathValidator.normalize(statReference);
         return normalized.equals(preset.statReference())
            ? preset.displayName() + " (" + preset.statReference() + ")"
            : preset.displayName() + " (" + normalized + ")";
      }
   }

   private static LeaderboardCommand.ResolvedHologram resolveNamedHologram(CommandContext<ServerCommandSource> ctx, Supplier<LeaderboardConfig> configSupplier) {
      LeaderboardConfig config = configSupplier.get();
      if (config == null) {
         ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("Leaderboard config is not ready yet."));
         return null;
      } else {
         String rawName = StringArgumentType.getString(ctx, "name");
         HologramNameHelper.ValidationResult validation = HologramNameHelper.validate(rawName);
         if (!validation.valid()) {
            ((ServerCommandSource)ctx.getSource()).sendError(Text.literal(validation.error()));
            return null;
         } else {
            LeaderboardConfig.HologramDefinition definition = config.findByName(validation.normalizedName());
            if (definition == null) {
               ((ServerCommandSource)ctx.getSource()).sendError(Text.literal("No hologram named '" + validation.normalizedName() + "' exists."));
               return null;
            } else {
               return new LeaderboardCommand.ResolvedHologram(config, definition);
            }
         }
      }
   }

   private static String getArgumentOrEmpty(CommandContext<ServerCommandSource> context, String argument) {
      try {
         return IdentifierArgumentType.getIdentifier(context, argument).toString();
      } catch (IllegalArgumentException var3) {
         return "";
      }
   }

   private record ResolvedHologram(LeaderboardConfig config, LeaderboardConfig.HologramDefinition definition) {
   }
}
