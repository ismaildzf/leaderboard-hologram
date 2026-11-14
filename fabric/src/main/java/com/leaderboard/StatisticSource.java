package com.leaderboard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record StatisticSource(String category, String statKey, long multiplier) {
    public StatisticSource(String category, String statKey, long multiplier) {
        category = StatisticPathValidator.normalize(category);
        statKey = statKey != null && !statKey.isBlank() ? StatisticPathValidator.normalize(statKey) : null;
        multiplier = Math.max(1L, multiplier);
        this.category = category;
        this.statKey = statKey;
        this.multiplier = multiplier;
    }

    public static StatisticSource path(String rawStatPath) {
        String normalizedPath = StatisticPathValidator.normalize(rawStatPath);
        String[] parts = normalizedPath.split("/", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Statistic path must use category/stat_key: " + rawStatPath);
        } else {
            return new StatisticSource(parts[0], parts[1], 1L);
        }
    }

    public static StatisticSource path(String rawStatPath, long multiplier) {
        String normalizedPath = StatisticPathValidator.normalize(rawStatPath);
        String[] parts = normalizedPath.split("/", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Statistic path must use category/stat_key: " + rawStatPath);
        } else {
            return new StatisticSource(parts[0], parts[1], multiplier);
        }
    }

    public static StatisticSource categoryTotal(String rawCategory) {
        return new StatisticSource(rawCategory, null, 1L);
    }

    public boolean isCategoryTotal() {
        return this.statKey == null || this.statKey.isBlank();
    }

    public String searchText() {
        return this.isCategoryTotal() ? this.category : this.category + "/" + this.statKey;
    }

    public long readValue(JsonObject statsRoot) {
        if (statsRoot == null) {
            return 0L;
        } else {
            JsonObject categoryObject = statsRoot.getAsJsonObject(this.category);
            if (categoryObject == null) {
                return 0L;
            } else if (!this.isCategoryTotal()) {
                return multiply(readPrimitiveLong(categoryObject.get(this.statKey)), this.multiplier);
            } else {
                long total = 0L;

                for (String key : categoryObject.keySet()) {
                    total = saturatingAdd(total, multiply(readPrimitiveLong(categoryObject.get(key)), this.multiplier));
                }

                return total;
            }
        }
    }

    private static long readPrimitiveLong(JsonElement valueElement) {
        if (valueElement != null && valueElement.isJsonPrimitive()) {
            try {
                return valueElement.getAsLong();
            } catch (RuntimeException var2) {
                return 0L;
            }
        } else {
            return 0L;
        }
    }

    private static long multiply(long value, long multiplier) {
        if (value > 0L && multiplier > 1L) {
            try {
                return Math.multiplyExact(value, multiplier);
            } catch (ArithmeticException var5) {
                return Long.MAX_VALUE;
            }
        } else {
            return value;
        }
    }

    private static long saturatingAdd(long left, long right) {
        if (left <= 0L) {
            return Math.max(0L, right);
        } else if (right <= 0L) {
            return left;
        } else {
            try {
                return Math.addExact(left, right);
            } catch (ArithmeticException var5) {
                return Long.MAX_VALUE;
            }
        }
    }
}
