package com.leaderboard;

import java.util.List;
import java.util.Locale;

public record StatisticPreset(String id, String displayName, String primaryStatPath, String description, List<StatisticSource> sources, List<String> aliases) {
    public StatisticPreset(String id, String displayName, String primaryStatPath, String description, List<StatisticSource> sources, List<String> aliases) {
        id = normalize(id);
        primaryStatPath = StatisticPathValidator.normalize(primaryStatPath);
        sources = List.copyOf(sources);
        aliases = List.copyOf(aliases);
        this.id = id;
        this.displayName = displayName;
        this.primaryStatPath = primaryStatPath;
        this.description = description;
        this.sources = sources;
        this.aliases = aliases;
    }

    public String statReference() {
        return "preset:" + this.id;
    }

    public boolean isAggregate() {
        return this.sources.size() > 1 || this.sources.stream().anyMatch(StatisticSource::isCategoryTotal);
    }

    public boolean matches(String rawQuery) {
        String query = normalize(rawQuery);
        if (query.isEmpty()) {
            return true;
        } else if (normalize(this.id).contains(query)
            || normalize(this.displayName).contains(query)
            || normalize(this.description).contains(query)
            || normalize(this.primaryStatPath).contains(query)
            || normalize(this.statReference()).contains(query)) {
            return true;
        } else {
            return this.sources.stream().map(StatisticSource::searchText).map(StatisticPreset::normalize).anyMatch(source -> source.contains(query))
                ? true
                : this.aliases.stream().map(StatisticPreset::normalize).anyMatch(alias -> alias.contains(query));
        }
    }

    public static String normalize(String rawValue) {
        return rawValue == null ? "" : rawValue.trim().toLowerCase(Locale.ROOT).replace(' ', '_').replace('-', '_');
    }
}
