package GodNev3r.bwdivisions;

import GodNev3r.bwdivisions.economy.EconomyManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * PlaceholderAPI expansion per BW-Divisions
 * Placeholders disponibili:
 * - %bwdiv_prefix% - Prefisso della divisione
 * - %bwdiv_name% - Nome della divisione
 * - %bwdiv_balance% - Saldo del giocatore
 * - %bwdiv_balance_formatted% - Saldo formattato del giocatore
 * - %bwdiv_progress% - Progresso percentuale verso la prossima divisione
 * - %bwdiv_progress_formatted% - Progresso formattato (es. 75.5%)
 * - %bwdiv_levels_until_next% - Livelli rimanenti per la prossima divisione
 * - %bwdiv_next_division% - Nome della prossima divisione
 */
public class DivisionExpansion extends PlaceholderExpansion {
    private final Main plugin;
    private final DivisionManager divisionManager;
    private final EconomyManager economyManager;
    private final ProgressManager progressManager;

    public DivisionExpansion(Main plugin, DivisionManager divisionManager, EconomyManager economyManager, ProgressManager progressManager) {
        this.plugin = plugin;
        this.divisionManager = divisionManager;
        this.economyManager = economyManager;
        this.progressManager = progressManager;
    }

    @Override
    public String getIdentifier() {
        return "bwdiv";
    }

    @Override
    public String getAuthor() {
        return "GodNev3r";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null || divisionManager == null) {
            return "";
        }

        switch (identifier.toLowerCase()) {
            case "prefix":
                return divisionManager.getDivisionPrefix(player);
            case "name":
                return divisionManager.getDivisionName(player);
            case "balance":
                if (economyManager != null) {
                    return String.valueOf(economyManager.getBalance(player));
                }
                return "0";
            case "balance_formatted":
                if (economyManager != null) {
                    return economyManager.formatBalance(economyManager.getBalance(player));
                }
                return "0";
            case "progress":
                if (progressManager != null) {
                    return String.valueOf((int) progressManager.getProgressPercent(player));
                }
                return "0";
            case "progress_formatted":
                if (progressManager != null) {
                    double progress = progressManager.getProgressPercent(player);
                    return String.format("%.1f%%", progress);
                }
                return "0%";
            case "levels_until_next":
                if (progressManager != null) {
                    return String.valueOf(progressManager.getLevelsUntilNextDivision(player));
                }
                return "0";
            case "next_division":
                if (progressManager != null) {
                    return progressManager.getNextDivisionName(player);
                }
                return "Massimo";
            default:
                return null;
        }
    }

    @Override
    public String onRequest(OfflinePlayer player, String identifier) {
        if (player == null || divisionManager == null) {
            return "";
        }

        // Per l'economia, possiamo gestire anche giocatori offline
        if (identifier.equalsIgnoreCase("balance") || identifier.equalsIgnoreCase("balance_formatted")) {
            if (economyManager != null) {
                double balance = economyManager.getBalance(player.getUniqueId());
                if (identifier.equalsIgnoreCase("balance_formatted")) {
                    return economyManager.formatBalance(balance);
                }
                return String.valueOf(balance);
            }
            return "0";
        }

        // Per altre placeholders, serve un giocatore online
        if (!player.isOnline()) {
            return "";
        }

        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return "";
        }

        return onPlaceholderRequest(onlinePlayer, identifier);
    }
}
