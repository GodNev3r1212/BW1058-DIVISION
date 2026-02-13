package GodNev3r.bwdivisions.stats;

import java.util.UUID;

/**
 * Memorizza le statistiche di un giocatore
 */
public class PlayerStatistics {
    private final UUID playerUUID;
    private int wins;
    private int losses;
    private int winstreak;
    private int bestWinstreak;
    private String bestDivision;
    private long totalPlaytime; // in millisecondi
    private long lastLogin;

    public PlayerStatistics(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.wins = 0;
        this.losses = 0;
        this.winstreak = 0;
        this.bestWinstreak = 0;
        this.bestDivision = null;
        this.totalPlaytime = 0;
        this.lastLogin = System.currentTimeMillis();
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public void addWin() {
        this.wins++;
        this.winstreak++;
        if (this.winstreak > this.bestWinstreak) {
            this.bestWinstreak = this.winstreak;
        }
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public void addLoss() {
        this.losses++;
        this.winstreak = 0; // Reset winstreak
    }

    public int getWinstreak() {
        return winstreak;
    }

    public void setWinstreak(int winstreak) {
        this.winstreak = winstreak;
    }

    public int getBestWinstreak() {
        return bestWinstreak;
    }

    public void setBestWinstreak(int bestWinstreak) {
        this.bestWinstreak = bestWinstreak;
    }

    public String getBestDivision() {
        return bestDivision;
    }

    public void setBestDivision(String bestDivision) {
        this.bestDivision = bestDivision;
    }

    public long getTotalPlaytime() {
        return totalPlaytime;
    }

    public void setTotalPlaytime(long totalPlaytime) {
        this.totalPlaytime = totalPlaytime;
    }

    public void addPlaytime(long milliseconds) {
        this.totalPlaytime += milliseconds;
    }

    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

    public void updateLastLogin() {
        long currentTime = System.currentTimeMillis();
        if (lastLogin > 0) {
            addPlaytime(currentTime - lastLogin);
        }
        this.lastLogin = currentTime;
    }

    public double getWinRate() {
        int totalGames = wins + losses;
        if (totalGames == 0) {
            return 0.0;
        }
        return (double) wins / totalGames * 100.0;
    }

    public String getFormattedPlaytime() {
        long seconds = totalPlaytime / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h " + (minutes % 60) + "m";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }
}
