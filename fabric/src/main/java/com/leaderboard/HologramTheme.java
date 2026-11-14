package com.leaderboard;

import net.minecraft.util.Formatting;

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
    Formatting titleColor,
    Formatting metaColor,
    Formatting topNameColor,
    Formatting defaultNameColor,
    Formatting firstValueColor,
    Formatting secondValueColor,
    Formatting thirdValueColor,
    Formatting defaultValueColor
) {
}
