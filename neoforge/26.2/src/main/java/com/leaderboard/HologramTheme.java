package com.leaderboard;

import net.minecraft.ChatFormatting;

public record HologramTheme(
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
}
