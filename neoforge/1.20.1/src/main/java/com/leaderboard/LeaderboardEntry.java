package com.leaderboard;

public record LeaderboardEntry(String playerName, long value) implements Comparable<LeaderboardEntry> {
   public int compareTo(LeaderboardEntry other) {
      int byValue = Long.compare(other.value, this.value);
      return byValue != 0 ? byValue : this.playerName.compareToIgnoreCase(other.playerName);
   }
}
