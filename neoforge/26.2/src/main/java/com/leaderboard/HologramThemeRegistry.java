package com.leaderboard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.ChatFormatting;

public final class HologramThemeRegistry {
   public static final String DEFAULT_THEME_ID = "clean";
   private static final List<HologramTheme> THEMES = List.of(
      theme(
         "clean",
         "Clean",
         "Minimal centered rows with soft highlights",
         188,
         176,
         194,
         0,
         0.24,
         0.4,
         0.58,
         0.23,
         0.0,
         0.0,
         874062384,
         0,
         0,
         404493321,
         303438368,
         303175966,
         0,
         ChatFormatting.GOLD,
         ChatFormatting.GRAY,
         ChatFormatting.WHITE,
         ChatFormatting.WHITE,
         ChatFormatting.GOLD,
         ChatFormatting.WHITE,
         ChatFormatting.AQUA,
         ChatFormatting.GREEN
      ),
      theme(
         "gold",
         "Gold",
         "Warmer premium styling inspired by network leaderboards",
         190,
         178,
         196,
         0,
         0.24,
         0.4,
         0.58,
         0.23,
         0.0,
         0.0,
         1011756560,
         0,
         0,
         509294866,
         373172753,
         372581905,
         0,
         ChatFormatting.GOLD,
         ChatFormatting.YELLOW,
         ChatFormatting.WHITE,
         ChatFormatting.YELLOW,
         ChatFormatting.GOLD,
         ChatFormatting.YELLOW,
         ChatFormatting.WHITE,
         ChatFormatting.GOLD
      ),
      theme(
         "dark",
         "Dark",
         "Sharper dark styling with cleaner centered rows",
         190,
         178,
         196,
         0,
         0.24,
         0.4,
         0.58,
         0.23,
         0.0,
         0.0,
         906694678,
         0,
         0,
         405677070,
         336731690,
         336863274,
         0,
         ChatFormatting.WHITE,
         ChatFormatting.DARK_AQUA,
         ChatFormatting.WHITE,
         ChatFormatting.GRAY,
         ChatFormatting.GOLD,
         ChatFormatting.AQUA,
         ChatFormatting.LIGHT_PURPLE,
         ChatFormatting.GREEN
      ),
      theme(
         "compact",
         "Compact",
         "Tighter centered rows for smaller areas",
         166,
         158,
         170,
         0,
         0.22,
         0.36,
         0.52,
         0.19,
         0.0,
         0.0,
         806756138,
         0,
         0,
         371463946,
         269752864,
         269621531,
         0,
         ChatFormatting.WHITE,
         ChatFormatting.GRAY,
         ChatFormatting.WHITE,
         ChatFormatting.GRAY,
         ChatFormatting.GOLD,
         ChatFormatting.WHITE,
         ChatFormatting.AQUA,
         ChatFormatting.GREEN
      )
   );
   private static final Map<String, HologramTheme> LOOKUP = buildLookup();

   private HologramThemeRegistry() {
   }

   public static List<HologramTheme> all() {
      return THEMES;
   }

   public static List<String> ids() {
      return THEMES.stream().map(HologramTheme::id).toList();
   }

   public static HologramTheme get(String rawThemeId) {
      return LOOKUP.getOrDefault(normalize(rawThemeId), LOOKUP.get("clean"));
   }

   public static boolean isKnownTheme(String rawThemeId) {
      return LOOKUP.containsKey(normalize(rawThemeId));
   }

   public static String normalize(String rawThemeId) {
      if (rawThemeId == null) {
         return "clean";
      } else {
         String normalized = rawThemeId.trim().toLowerCase(Locale.ROOT);
         return normalized.isEmpty() ? "clean" : normalized;
      }
   }

   private static HologramTheme theme(
      String id,
      String displayName,
      String description,
      int headerLineWidth,
      int metaLineWidth,
      int rowNameLineWidth,
      int rowValueLineWidth,
      double metaOffsetY,
      double dividerOffsetY,
      double entryStartOffsetY,
      double entrySpacingY,
      double rowNameXOffset,
      double rowValueXOffset,
      int headerBackground,
      int metaBackground,
      int dividerBackground,
      int topOneBackground,
      int topTwoBackground,
      int topThreeBackground,
      int defaultRowBackground,
      ChatFormatting titleColor,
      ChatFormatting metaColor,
      ChatFormatting topNameColor,
      ChatFormatting defaultNameColor,
      ChatFormatting firstValueColor,
      ChatFormatting secondValueColor,
      ChatFormatting thirdValueColor,
      ChatFormatting defaultValueColor
   ) {
      return new HologramTheme(
         id,
         displayName,
         description,
         headerLineWidth,
         metaLineWidth,
         rowNameLineWidth,
         rowValueLineWidth,
         metaOffsetY,
         dividerOffsetY,
         entryStartOffsetY,
         entrySpacingY,
         rowNameXOffset,
         rowValueXOffset,
         headerBackground,
         metaBackground,
         dividerBackground,
         topOneBackground,
         topTwoBackground,
         topThreeBackground,
         defaultRowBackground,
         titleColor,
         metaColor,
         topNameColor,
         defaultNameColor,
         firstValueColor,
         secondValueColor,
         thirdValueColor,
         defaultValueColor
      );
   }

   private static Map<String, HologramTheme> buildLookup() {
      Map<String, HologramTheme> lookup = new LinkedHashMap<>();

      for (HologramTheme theme : THEMES) {
         lookup.put(theme.id(), theme);
      }

      return Map.copyOf(lookup);
   }
}
