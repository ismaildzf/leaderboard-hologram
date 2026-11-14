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
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class LeaderboardCommand {
   private static final SuggestionProvider<CommandSourceStack> CATEGORY_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
      StatisticPathValidator.commonCategories(), builder
   );
   private static final SuggestionProvider<CommandSourceStack> STAT_KEY_SUGGESTIONS = (context, builder) -> {
      String category = getArgumentOrEmpty(context, "category");
      return SharedSuggestionProvider.suggest(StatisticPathValidator.allStatKeys(category), builder);
   };
   private static final SuggestionProvider<CommandSourceStack> PRESET_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
      StatisticPresetRegistry.presetIds(), builder
   );
   private static final SuggestionProvider<CommandSourceStack> THEME_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
      HologramThemeRegistry.ids(), builder
   );
   private static final SuggestionProvider<CommandSourceStack> QUOTED_STAT_PATH_SUGGESTIONS = (context, builder) -> SharedSuggestionProvider.suggest(
      StatisticPresetRegistry.suggestedStatPaths(), builder
   );

   public static void register(
      CommandDispatcher<CommandSourceStack> dispatcher,
      HologramManager hologramManager,
      Supplier<LeaderboardConfig> configSupplier,
      Runnable refreshCallback,
      Runnable reloadCallback
   ) {
      SuggestionProvider<CommandSourceStack> hologramNameSuggestions = createHologramNameSuggestions(configSupplier);
      dispatcher.register(
         (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal(
                                                            "leaderboard"
                                                         )
                                                         .requires(source -> CompatibilityBridge.hasPermissionLevel(source, 2)))
                                                      .then(
                                                         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("create")
                                                                     .then(
                                                                        Commands.literal("quick")
                                                                           .then(
                                                                              ((RequiredArgumentBuilder)Commands.argument("preset", StringArgumentType.word())
                                                                                    .suggests(PRESET_SUGGESTIONS)
                                                                                    .executes(
                                                                                       ctx -> executeCreateQuickPreset(
                                                                                          ctx, hologramManager, configSupplier, refreshCallback
                                                                                       )
                                                                                    ))
                                                                                 .then(
                                                                                    Commands.argument("top_count", IntegerArgumentType.integer(1, 25))
                                                                                       .executes(
                                                                                          ctx -> executeCreateQuickPreset(
                                                                                             ctx, hologramManager, configSupplier, refreshCallback
                                                                                          )
                                                                                       )
                                                                                 )
                                                                           )
                                                                     ))
                                                                  .then(
                                                                     Commands.literal("preset")
                                                                        .then(
                                                                           Commands.argument("name", StringArgumentType.word())
                                                                              .then(
                                                                                 ((RequiredArgumentBuilder)Commands.argument(
                                                                                          "preset", StringArgumentType.word()
                                                                                       )
                                                                                       .suggests(PRESET_SUGGESTIONS)
                                                                                       .executes(
                                                                                          ctx -> executeCreateFromPreset(
                                                                                             ctx, hologramManager, configSupplier, refreshCallback
                                                                                          )
                                                                                       ))
                                                                                    .then(
                                                                                       Commands.argument("top_count", IntegerArgumentType.integer(1, 25))
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
                                                                  Commands.literal("path")
                                                                     .then(
                                                                        Commands.argument("name", StringArgumentType.word())
                                                                           .then(
                                                                              ((RequiredArgumentBuilder)Commands.argument(
                                                                                       "statistic_path", StringArgumentType.string()
                                                                                    )
                                                                                    .suggests(QUOTED_STAT_PATH_SUGGESTIONS)
                                                                                    .executes(
                                                                                       ctx -> executeCreateFromPath(
                                                                                          ctx, hologramManager, configSupplier, refreshCallback
                                                                                       )
                                                                                    ))
                                                                                 .then(
                                                                                    Commands.argument("top_count", IntegerArgumentType.integer(1, 25))
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
                                                               Commands.argument("name", StringArgumentType.word())
                                                                  .then(
                                                                     Commands.argument("category", IdentifierArgument.id())
                                                                        .suggests(CATEGORY_SUGGESTIONS)
                                                                        .then(
                                                                           ((RequiredArgumentBuilder)Commands.argument("stat_key", IdentifierArgument.id())
                                                                                 .suggests(STAT_KEY_SUGGESTIONS)
                                                                                 .executes(
                                                                                    ctx -> executeCreate(ctx, hologramManager, configSupplier, refreshCallback)
                                                                                 ))
                                                                              .then(
                                                                                 Commands.argument("top_count", IntegerArgumentType.integer(1, 25))
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
                                                      ((LiteralArgumentBuilder)Commands.literal("remove")
                                                            .then(
                                                               Commands.literal("nearest")
                                                                  .executes(ctx -> executeRemoveNearest(ctx, hologramManager, configSupplier))
                                                            ))
                                                         .then(
                                                            Commands.argument("name", StringArgumentType.word())
                                                               .suggests(hologramNameSuggestions)
                                                               .executes(ctx -> executeRemoveByName(ctx, hologramManager, configSupplier))
                                                         )
                                                   ))
                                                .then(
                                                   Commands.literal("rename")
                                                      .then(
                                                         Commands.argument("name", StringArgumentType.word())
                                                            .suggests(hologramNameSuggestions)
                                                            .then(
                                                               Commands.argument("new_name", StringArgumentType.word())
                                                                  .executes(ctx -> executeRename(ctx, hologramManager, configSupplier, refreshCallback))
                                                            )
                                                      )
                                                ))
                                             .then(
                                                Commands.literal("move")
                                                   .then(
                                                      Commands.argument("name", StringArgumentType.word())
                                                         .suggests(hologramNameSuggestions)
                                                         .executes(ctx -> executeMoveToPlayer(ctx, hologramManager, configSupplier, refreshCallback))
                                                   )
                                             ))
                                          .then(
                                             Commands.literal("setpos")
                                                .then(
                                                   Commands.argument("name", StringArgumentType.word())
                                                      .suggests(hologramNameSuggestions)
                                                      .then(
                                                         Commands.argument("pos", Vec3Argument.vec3())
                                                            .executes(ctx -> executeSetPos(ctx, hologramManager, configSupplier, refreshCallback))
                                                      )
                                                )
                                          ))
                                       .then(
                                          Commands.literal("settop")
                                             .then(
                                                Commands.argument("name", StringArgumentType.word())
                                                   .suggests(hologramNameSuggestions)
                                                   .then(
                                                      Commands.argument("top_count", IntegerArgumentType.integer(1, 25))
                                                         .executes(ctx -> executeSetTop(ctx, hologramManager, configSupplier, refreshCallback))
                                                   )
                                             )
                                       ))
                                    .then(
                                       Commands.literal("settheme")
                                          .then(
                                             Commands.argument("name", StringArgumentType.word())
                                                .suggests(hologramNameSuggestions)
                                                .then(
                                                   Commands.argument("theme", StringArgumentType.word())
                                                      .suggests(THEME_SUGGESTIONS)
                                                      .executes(ctx -> executeSetTheme(ctx, hologramManager, configSupplier, refreshCallback))
                                                )
                                          )
                                    ))
                                 .then(
                                    ((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("setstat")
                                             .then(
                                                Commands.literal("preset")
                                                   .then(
                                                      Commands.argument("name", StringArgumentType.word())
                                                         .suggests(hologramNameSuggestions)
                                                         .then(
                                                            Commands.argument("preset", StringArgumentType.word())
                                                               .suggests(PRESET_SUGGESTIONS)
                                                               .executes(ctx -> executeSetStatFromPreset(ctx, configSupplier, reloadCallback))
                                                         )
                                                   )
                                             ))
                                          .then(
                                             Commands.literal("path")
                                                .then(
                                                   Commands.argument("name", StringArgumentType.word())
                                                      .suggests(hologramNameSuggestions)
                                                      .then(
                                                         Commands.argument("statistic_path", StringArgumentType.string())
                                                            .suggests(QUOTED_STAT_PATH_SUGGESTIONS)
                                                            .executes(ctx -> executeSetStatFromPath(ctx, configSupplier, reloadCallback))
                                                      )
                                                )
                                          ))
                                       .then(
                                          Commands.argument("name", StringArgumentType.word())
                                             .suggests(hologramNameSuggestions)
                                             .then(
                                                Commands.argument("category", IdentifierArgument.id())
                                                   .suggests(CATEGORY_SUGGESTIONS)
                                                   .then(
                                                      Commands.argument("stat_key", IdentifierArgument.id())
                                                         .suggests(STAT_KEY_SUGGESTIONS)
                                                         .executes(ctx -> executeSetStat(ctx, configSupplier, reloadCallback))
                                                   )
                                             )
                                       )
                                 ))
                              .then(
                                 Commands.literal("info")
                                    .then(
                                       Commands.argument("name", StringArgumentType.word())
                                          .suggests(hologramNameSuggestions)
                                          .executes(ctx -> executeInfo(ctx, configSupplier))
                                    )
                              ))
                           .then(Commands.literal("list").executes(ctx -> executeList(ctx, hologramManager))))
                        .then(
                           ((LiteralArgumentBuilder)Commands.literal("presets").executes(ctx -> executeListPresets(ctx, "")))
                              .then(
                                 Commands.argument("query", StringArgumentType.greedyString())
                                    .executes(ctx -> executeListPresets(ctx, StringArgumentType.getString(ctx, "query")))
                              )
                        ))
                     .then(Commands.literal("themes").executes(LeaderboardCommand::executeListThemes)))
                  .then(
                     Commands.literal("search")
                        .then(
                           Commands.argument("query", StringArgumentType.greedyString())
                              .executes(ctx -> executeListPresets(ctx, StringArgumentType.getString(ctx, "query")))
                        )
                  ))
               .then(Commands.literal("refresh").executes(ctx -> executeRefresh(ctx, refreshCallback))))
            .then(Commands.literal("reload").executes(ctx -> executeReload(ctx, reloadCallback)))
      );
   }

   private static int executeCreate(
      CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      String category = IdentifierArgument.getId(ctx, "category").toString();
      String statKey = IdentifierArgument.getId(ctx, "stat_key").toString();
      return executeCreateInternal(ctx, hologramManager, configSupplier, refreshCallback, StringArgumentType.getString(ctx, "name"), category + "/" + statKey);
   }

   private static int executeCreateFromPath(
      CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      return executeCreateInternal(
         ctx, hologramManager, configSupplier, refreshCallback, StringArgumentType.getString(ctx, "name"), StringArgumentType.getString(ctx, "statistic_path")
      );
   }

   private static int executeCreateFromPreset(
      CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      StatisticPreset preset = resolvePreset(ctx);
      if (preset == null) {
         return 0;
      } else {
         int result = executeCreateInternal(
            ctx, hologramManager, configSupplier, refreshCallback, StringArgumentType.getString(ctx, "name"), preset.statReference()
         );
         if (result == 1) {
            sendPresetFeedback((CommandSourceStack)ctx.getSource(), preset);
         }

         return result;
      }
   }

   private static int executeCreateQuickPreset(
      CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      StatisticPreset preset = resolvePreset(ctx);
      if (preset == null) {
         return 0;
      } else {
         LeaderboardConfig config = configSupplier.get();
         if (config == null) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Leaderboard config is not ready yet."));
            return 0;
         } else {
            int result = executeCreateInternal(
               ctx, hologramManager, configSupplier, refreshCallback, createUniqueHologramName(config, preset.id()), preset.statReference()
            );
            if (result == 1) {
               sendPresetFeedback((CommandSourceStack)ctx.getSource(), preset);
            }

            return result;
         }
      }
   }

   private static int executeCreateInternal(
      CommandContext<CommandSourceStack> ctx,
      HologramManager hologramManager,
      Supplier<LeaderboardConfig> configSupplier,
      Runnable refreshCallback,
      String rawName,
      String rawStatPath
   ) {
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      LeaderboardConfig config = configSupplier.get();
      if (config == null) {
         source.sendFailure(Component.literal("Leaderboard config is not ready yet."));
         return 0;
      } else {
         HologramNameHelper.ValidationResult nameValidation = HologramNameHelper.validate(rawName);
         if (!nameValidation.valid()) {
            source.sendFailure(Component.literal(nameValidation.error()));
            return 0;
         } else if (config.findByName(nameValidation.normalizedName()) != null) {
            source.sendFailure(Component.literal("A hologram with that name already exists."));
            return 0;
         } else {
            StatisticPathValidator.ValidationResult validation = StatisticPathValidator.validate(rawStatPath);
            if (!validation.valid()) {
               source.sendFailure(Component.literal(validation.error()));
               return 0;
            } else {
               int topCount = getTopCount(ctx);
               ServerPlayer player = CompatibilityBridge.getSourcePlayer(source);
               if (player == null) {
                  source.sendFailure(Component.literal("This command must be run by a player."));
                  return 0;
               } else {
                  ServerLevel world = CompatibilityBridge.getServerPlayerWorld(player);
                  Vec3 pos = CompatibilityBridge.getEntityPos(player).add(0.0, 2.5, 0.0);
                  if (hologramManager.hasNearby(world, pos, 1.5)) {
                     source.sendFailure(Component.literal("Another leaderboard hologram is too close. Move at least 2 blocks away."));
                     return 0;
                  } else {
                     LeaderboardConfig.HologramDefinition def = new LeaderboardConfig.HologramDefinition(
                        nameValidation.normalizedName(),
                        CompatibilityBridge.getWorldId(world),
                        pos.x,
                        pos.y,
                        pos.z,
                        validation.normalizedPath(),
                        topCount,
                        "clean"
                     );
                     if (hologramManager.spawnHologram(world, pos, def.id, def.name, def.statPath, def.topCount, def.theme) == null) {
                        source.sendFailure(Component.literal("Failed to spawn leaderboard hologram in this world."));
                        return 0;
                     } else if (!config.addHologram(def)) {
                        hologramManager.removeById(def.id);
                        source.sendFailure(Component.literal("Failed to save leaderboard hologram because it conflicts with an existing entry."));
                        return 0;
                     } else {
                        refreshCallback.run();
                        source.sendSuccess(
                           () -> Component.literal("Created hologram ")
                              .withStyle(s -> s.withColor(ChatFormatting.GREEN))
                              .append(Component.literal(def.name).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true)))
                              .append(Component.literal(" for ").withStyle(s -> s.withColor(ChatFormatting.GREEN)))
                              .append(Component.literal(describeStatReference(def.statPath)).withStyle(s -> s.withColor(ChatFormatting.GOLD)))
                              .append(
                                 Component.literal(" (top " + def.topCount + ", theme " + def.theme + ")").withStyle(s -> s.withColor(ChatFormatting.AQUA))
                              ),
                           true
                        );
                        if (!validation.knownCategory()) {
                           source.sendSuccess(
                              () -> Component.literal("Note: this statistic category is not a common vanilla category. It may still work for modded stats.")
                                 .withStyle(s -> s.withColor(ChatFormatting.YELLOW)),
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

   private static int executeRemoveByName(CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         hologramManager.removeById(resolved.definition.id);
         resolved.config.removeHologram(resolved.definition.id);
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.literal("Removed hologram ")
                  .withStyle(s -> s.withColor(ChatFormatting.GREEN))
                  .append(Component.literal(resolved.definition.name).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true))),
               true
            );
         return 1;
      }
   }

   private static int executeRemoveNearest(CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier) {
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      LeaderboardConfig config = configSupplier.get();
      if (config == null) {
         source.sendFailure(Component.literal("Leaderboard config is not ready yet."));
         return 0;
      } else {
         ServerPlayer player = CompatibilityBridge.getSourcePlayer(source);
         if (player == null) {
            source.sendFailure(Component.literal("This command must be run by a player."));
            return 0;
         } else {
            HologramManager.TrackedHologram removed = hologramManager.removeNearest(
               CompatibilityBridge.getServerPlayerWorld(player), CompatibilityBridge.getEntityPos(player), 10.0
            );
            if (removed == null) {
               source.sendFailure(Component.literal("No leaderboard hologram found within 10 blocks."));
               return 0;
            } else {
               config.removeHologram(removed.id);
               source.sendSuccess(
                  () -> Component.literal("Removed nearest hologram ")
                     .withStyle(s -> s.withColor(ChatFormatting.GREEN))
                     .append(Component.literal(removed.name).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true))),
                  true
               );
               return 1;
            }
         }
      }
   }

   private static int executeRename(
      CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         HologramNameHelper.ValidationResult validation = HologramNameHelper.validate(StringArgumentType.getString(ctx, "new_name"));
         if (!validation.valid()) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal(validation.error()));
            return 0;
         } else {
            LeaderboardConfig.HologramDefinition existing = resolved.config.findByName(validation.normalizedName());
            if (existing != null && !existing.id.equals(resolved.definition.id)) {
               ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Another hologram already uses that name."));
               return 0;
            } else if (!resolved.config.renameHologram(resolved.definition.id, validation.normalizedName())) {
               ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Failed to rename hologram."));
               return 0;
            } else {
               hologramManager.rename(resolved.definition.id, validation.normalizedName());
               refreshCallback.run();
               ((CommandSourceStack)ctx.getSource())
                  .sendSuccess(
                     () -> Component.literal("Renamed hologram to ")
                        .withStyle(s -> s.withColor(ChatFormatting.GREEN))
                        .append(Component.literal(validation.normalizedName()).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true))),
                     true
                  );
               return 1;
            }
         }
      }
   }

   private static int executeMoveToPlayer(
      CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         ServerPlayer player = CompatibilityBridge.getSourcePlayer((CommandSourceStack)ctx.getSource());
         if (player == null) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("This command must be run by a player."));
            return 0;
         } else {
            ServerLevel world = CompatibilityBridge.getServerPlayerWorld(player);
            Vec3 pos = CompatibilityBridge.getEntityPos(player).add(0.0, 2.5, 0.0);
            return moveHologram(ctx, hologramManager, refreshCallback, resolved, world, pos);
         }
      }
   }

   private static int executeSetPos(
      CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         ServerLevel world = CompatibilityBridge.getSourceWorld((CommandSourceStack)ctx.getSource());
         Vec3 pos = Vec3Argument.getVec3(ctx, "pos");
         return moveHologram(ctx, hologramManager, refreshCallback, resolved, world, pos);
      }
   }

   private static int moveHologram(
      CommandContext<CommandSourceStack> ctx,
      HologramManager hologramManager,
      Runnable refreshCallback,
      LeaderboardCommand.ResolvedHologram resolved,
      ServerLevel world,
      Vec3 pos
   ) {
      if (!resolved.config.updatePosition(resolved.definition.id, CompatibilityBridge.getWorldId(world), pos.x, pos.y, pos.z)) {
         ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Failed to save hologram position."));
         return 0;
      } else if (!hologramManager.moveTo(resolved.definition.id, world, pos)) {
         ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Failed to move hologram entity."));
         return 0;
      } else {
         refreshCallback.run();
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.literal("Moved hologram ")
                  .withStyle(s -> s.withColor(ChatFormatting.GREEN))
                  .append(Component.literal(resolved.definition.name).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true)))
                  .append(Component.literal(String.format(" to (%.1f, %.1f, %.1f)", pos.x, pos.y, pos.z)).withStyle(s -> s.withColor(ChatFormatting.AQUA))),
               true
            );
         return 1;
      }
   }

   private static int executeSetTop(
      CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         int topCount = IntegerArgumentType.getInteger(ctx, "top_count");
         if (!resolved.config.updateTopCount(resolved.definition.id, topCount)) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Failed to update top count."));
            return 0;
         } else {
            hologramManager.updateTopCount(resolved.definition.id, topCount);
            refreshCallback.run();
            ((CommandSourceStack)ctx.getSource())
               .sendSuccess(
                  () -> Component.literal("Updated top count for ")
                     .withStyle(s -> s.withColor(ChatFormatting.GREEN))
                     .append(Component.literal(resolved.definition.name).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true)))
                     .append(Component.literal(" to " + topCount).withStyle(s -> s.withColor(ChatFormatting.AQUA))),
                  true
               );
            return 1;
         }
      }
   }

   private static int executeSetTheme(
      CommandContext<CommandSourceStack> ctx, HologramManager hologramManager, Supplier<LeaderboardConfig> configSupplier, Runnable refreshCallback
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         String rawTheme = StringArgumentType.getString(ctx, "theme");
         String normalizedTheme = HologramThemeRegistry.normalize(rawTheme);
         if (!HologramThemeRegistry.isKnownTheme(normalizedTheme)) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Unknown theme '" + rawTheme + "'. Use /leaderboard themes"));
            return 0;
         } else if (!resolved.config.updateTheme(resolved.definition.id, normalizedTheme)) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Failed to update hologram theme."));
            return 0;
         } else if (!hologramManager.updateTheme(resolved.definition.id, normalizedTheme)) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Failed to apply hologram theme to the active entity."));
            return 0;
         } else {
            refreshCallback.run();
            HologramTheme theme = HologramThemeRegistry.get(normalizedTheme);
            ((CommandSourceStack)ctx.getSource())
               .sendSuccess(
                  () -> Component.literal("Updated theme for ")
                     .withStyle(s -> s.withColor(ChatFormatting.GREEN))
                     .append(Component.literal(resolved.definition.name).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true)))
                     .append(Component.literal(" to ").withStyle(s -> s.withColor(ChatFormatting.GREEN)))
                     .append(Component.literal(theme.id()).withStyle(s -> s.withColor(ChatFormatting.AQUA).withBold(true)))
                     .append(Component.literal(" (" + theme.displayName() + ")").withStyle(s -> s.withColor(ChatFormatting.GRAY))),
                  true
               );
            return 1;
         }
      }
   }

   private static int executeSetStat(CommandContext<CommandSourceStack> ctx, Supplier<LeaderboardConfig> configSupplier, Runnable reloadCallback) {
      String category = IdentifierArgument.getId(ctx, "category").toString();
      String statKey = IdentifierArgument.getId(ctx, "stat_key").toString();
      return executeSetStatInternal(ctx, configSupplier, reloadCallback, category + "/" + statKey);
   }

   private static int executeSetStatFromPreset(CommandContext<CommandSourceStack> ctx, Supplier<LeaderboardConfig> configSupplier, Runnable reloadCallback) {
      StatisticPreset preset = resolvePreset(ctx);
      if (preset == null) {
         return 0;
      } else {
         int result = executeSetStatInternal(ctx, configSupplier, reloadCallback, preset.statReference());
         if (result == 1) {
            sendPresetFeedback((CommandSourceStack)ctx.getSource(), preset);
         }

         return result;
      }
   }

   private static int executeSetStatFromPath(CommandContext<CommandSourceStack> ctx, Supplier<LeaderboardConfig> configSupplier, Runnable reloadCallback) {
      return executeSetStatInternal(ctx, configSupplier, reloadCallback, StringArgumentType.getString(ctx, "statistic_path"));
   }

   private static int executeSetStatInternal(
      CommandContext<CommandSourceStack> ctx, Supplier<LeaderboardConfig> configSupplier, Runnable reloadCallback, String rawStatPath
   ) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         StatisticPathValidator.ValidationResult validation = StatisticPathValidator.validate(rawStatPath);
         if (!validation.valid()) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal(validation.error()));
            return 0;
         } else if (!resolved.config.updateStatPath(resolved.definition.id, validation.normalizedPath())) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Failed to update hologram statistic."));
            return 0;
         } else {
            reloadCallback.run();
            ((CommandSourceStack)ctx.getSource())
               .sendSuccess(
                  () -> Component.literal("Updated statistic for ")
                     .withStyle(s -> s.withColor(ChatFormatting.GREEN))
                     .append(Component.literal(resolved.definition.name).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true)))
                     .append(Component.literal(" to ").withStyle(s -> s.withColor(ChatFormatting.GREEN)))
                     .append(Component.literal(describeStatReference(validation.normalizedPath())).withStyle(s -> s.withColor(ChatFormatting.GOLD))),
                  true
               );
            return 1;
         }
      }
   }

   private static int executeInfo(CommandContext<CommandSourceStack> ctx, Supplier<LeaderboardConfig> configSupplier) {
      LeaderboardCommand.ResolvedHologram resolved = resolveNamedHologram(ctx, configSupplier);
      if (resolved == null) {
         return 0;
      } else {
         LeaderboardConfig.HologramDefinition def = resolved.definition;
         StatisticPreset preset = StatisticPresetRegistry.findByStatPath(def.statPath);
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.literal("Hologram ")
                  .withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true))
                  .append(Component.literal(def.name).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true))),
               false
            );
         if (preset != null) {
            ((CommandSourceStack)ctx.getSource())
               .sendSuccess(
                  () -> Component.literal("  preset: " + preset.id() + " (" + preset.displayName() + ")").withStyle(s -> s.withColor(ChatFormatting.AQUA)),
                  false
               );
         }

         HologramTheme theme = HologramThemeRegistry.get(def.theme);
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.literal("  theme: " + theme.id() + " (" + theme.displayName() + ")").withStyle(s -> s.withColor(ChatFormatting.WHITE)), false
            );
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(() -> Component.literal("  stat: " + describeStatReference(def.statPath)).withStyle(s -> s.withColor(ChatFormatting.WHITE)), false);
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.literal(String.format("  pos: %.1f %.1f %.1f [%s]", def.x, def.y, def.z, def.worldId))
                  .withStyle(s -> s.withColor(ChatFormatting.WHITE)),
               false
            );
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(() -> Component.literal("  top: " + def.topCount).withStyle(s -> s.withColor(ChatFormatting.WHITE)), false);
         return 1;
      }
   }

   private static int executeList(CommandContext<CommandSourceStack> ctx, HologramManager hologramManager) {
      CommandSourceStack source = (CommandSourceStack)ctx.getSource();
      List<HologramManager.TrackedHologram> holograms = hologramManager.getTracked();
      if (holograms.isEmpty()) {
         source.sendSuccess(() -> Component.literal("No active leaderboard holograms.").withStyle(s -> s.withColor(ChatFormatting.YELLOW)), false);
         return 0;
      } else {
         source.sendSuccess(() -> Component.literal("=== Active Leaderboards ===").withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true)), false);

         for (HologramManager.TrackedHologram trackedHologram : holograms) {
            StatisticPreset preset = StatisticPresetRegistry.findByStatPath(trackedHologram.statPath);
            String statLabel = preset == null ? trackedHologram.statPath : preset.id() + " -> " + describeStatReference(trackedHologram.statPath);
            HologramTheme theme = HologramThemeRegistry.get(trackedHologram.themeId);
            source.sendSuccess(
               () -> Component.literal(
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
                  .withStyle(s -> s.withColor(ChatFormatting.WHITE)),
               false
            );
         }

         return holograms.size();
      }
   }

   private static int executeListPresets(CommandContext<CommandSourceStack> ctx, String query) {
      List<StatisticPreset> presets = StatisticPresetRegistry.search(query);
      if (presets.isEmpty()) {
         ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("No presets matched '" + query + "'."));
         return 0;
      } else {
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(() -> Component.literal("=== Available Presets ===").withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true)), false);

         for (StatisticPreset preset : presets) {
            ((CommandSourceStack)ctx.getSource())
               .sendSuccess(
                  () -> Component.literal("  " + preset.id())
                     .withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true))
                     .append(Component.literal(" -> " + preset.displayName()).withStyle(s -> s.withColor(ChatFormatting.WHITE)))
                     .append(Component.literal(" | " + preset.description()).withStyle(s -> s.withColor(ChatFormatting.GRAY)))
                     .append(
                        preset.isAggregate()
                           ? Component.literal(" | aggregate").withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY))
                           : Component.literal("").withStyle(s -> s.withColor(ChatFormatting.GRAY))
                     ),
                  false
               );
         }

         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.literal("Use /leaderboard create quick <preset> or /leaderboard create preset <name> <preset>")
                  .withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY)),
               false
            );
         return presets.size();
      }
   }

   private static int executeListThemes(CommandContext<CommandSourceStack> ctx) {
      ((CommandSourceStack)ctx.getSource())
         .sendSuccess(() -> Component.literal("=== Available Themes ===").withStyle(s -> s.withColor(ChatFormatting.GOLD).withBold(true)), false);

      for (HologramTheme theme : HologramThemeRegistry.all()) {
         ((CommandSourceStack)ctx.getSource())
            .sendSuccess(
               () -> Component.literal("  " + theme.id())
                  .withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true))
                  .append(Component.literal(" -> " + theme.displayName()).withStyle(s -> s.withColor(ChatFormatting.WHITE)))
                  .append(Component.literal(" | " + theme.description()).withStyle(s -> s.withColor(ChatFormatting.GRAY))),
               false
            );
      }

      ((CommandSourceStack)ctx.getSource())
         .sendSuccess(() -> Component.literal("Use /leaderboard settheme <name> <theme>").withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY)), false);
      return HologramThemeRegistry.all().size();
   }

   private static int executeRefresh(CommandContext<CommandSourceStack> ctx, Runnable refreshCallback) {
      refreshCallback.run();
      ((CommandSourceStack)ctx.getSource())
         .sendSuccess(
            () -> Component.literal("Leaderboard data refreshed from the latest saved player stats.").withStyle(s -> s.withColor(ChatFormatting.GREEN)), false
         );
      return 1;
   }

   private static int executeReload(CommandContext<CommandSourceStack> ctx, Runnable reloadCallback) {
      reloadCallback.run();
      ((CommandSourceStack)ctx.getSource())
         .sendSuccess(() -> Component.literal("Leaderboard holograms reloaded from config.").withStyle(s -> s.withColor(ChatFormatting.GREEN)), false);
      return 1;
   }

   private static int getTopCount(CommandContext<CommandSourceStack> ctx) {
      boolean hasTopCountArgument = ctx.getNodes().stream().anyMatch(node -> "top_count".equals(node.getNode().getName()));
      return hasTopCountArgument ? IntegerArgumentType.getInteger(ctx, "top_count") : 10;
   }

   private static SuggestionProvider<CommandSourceStack> createHologramNameSuggestions(Supplier<LeaderboardConfig> configSupplier) {
      return (context, builder) -> {
         LeaderboardConfig config = configSupplier.get();
         return config == null ? builder.buildFuture() : SharedSuggestionProvider.suggest(config.getHolograms().stream().map(def -> def.name), builder);
      };
   }

   private static StatisticPreset resolvePreset(CommandContext<CommandSourceStack> ctx) {
      String rawPreset = StringArgumentType.getString(ctx, "preset");
      StatisticPreset preset = StatisticPresetRegistry.find(rawPreset);
      if (preset == null) {
         ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Unknown preset '" + rawPreset + "'. Use /leaderboard presets"));
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

   private static void sendPresetFeedback(CommandSourceStack source, StatisticPreset preset) {
      source.sendSuccess(
         () -> Component.literal("Preset: ")
            .withStyle(s -> s.withColor(ChatFormatting.GREEN))
            .append(Component.literal(preset.id()).withStyle(s -> s.withColor(ChatFormatting.YELLOW).withBold(true)))
            .append(Component.literal(" -> ").withStyle(s -> s.withColor(ChatFormatting.GREEN)))
            .append(Component.literal(preset.displayName()).withStyle(s -> s.withColor(ChatFormatting.AQUA)))
            .append(Component.literal(" (" + preset.statReference() + ")").withStyle(s -> s.withColor(ChatFormatting.GRAY)))
            .append(
               preset.isAggregate()
                  ? Component.literal(" [aggregate]").withStyle(s -> s.withColor(ChatFormatting.DARK_GRAY))
                  : Component.literal("").withStyle(s -> s.withColor(ChatFormatting.GRAY))
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

   private static LeaderboardCommand.ResolvedHologram resolveNamedHologram(CommandContext<CommandSourceStack> ctx, Supplier<LeaderboardConfig> configSupplier) {
      LeaderboardConfig config = configSupplier.get();
      if (config == null) {
         ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("Leaderboard config is not ready yet."));
         return null;
      } else {
         String rawName = StringArgumentType.getString(ctx, "name");
         HologramNameHelper.ValidationResult validation = HologramNameHelper.validate(rawName);
         if (!validation.valid()) {
            ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal(validation.error()));
            return null;
         } else {
            LeaderboardConfig.HologramDefinition definition = config.findByName(validation.normalizedName());
            if (definition == null) {
               ((CommandSourceStack)ctx.getSource()).sendFailure(Component.literal("No hologram named '" + validation.normalizedName() + "' exists."));
               return null;
            } else {
               return new LeaderboardCommand.ResolvedHologram(config, definition);
            }
         }
      }
   }

   private static String getArgumentOrEmpty(CommandContext<CommandSourceStack> context, String argument) {
      try {
         return IdentifierArgument.getId(context, argument).toString();
      } catch (IllegalArgumentException var3) {
         return "";
      }
   }

   private record ResolvedHologram(LeaderboardConfig config, LeaderboardConfig.HologramDefinition definition) {
   }
}
