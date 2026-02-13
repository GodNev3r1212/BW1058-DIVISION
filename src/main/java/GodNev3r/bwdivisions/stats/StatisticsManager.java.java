package GodNev3r.bwdivisions.stats;

import GodNev3r.bwdivisions.Main;
import GodNev3r.bwdivisions.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestisce le statistiche dei giocatori
 */
public class StatisticsManager {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private final Map<UUID, PlayerStatistics> statisticsMap;
    private File statsFile;
    private FileConfiguration statsConfig;

    public StatisticsManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.statisticsMap = new HashMap<>();
    }

    /**
     * Inizializza il manager delle statistiche
     */
    public void initialize() {
        if (!configManager.getConfig().getBoolean("statistics.enabled", true)) {
            plugin.getLogger().info("Statistiche disabilitate nel config!");
            return;
        }

        loadStatsFile();
        loadStatistics();
    }

    /**
     * Carica il file delle statistiche
     */
    private void loadStatsFile() {
        File addonFolder = getAddonFolder();
        if (addonFolder == null) {
            statsFile = new File(plugin.getDataFolder(), "statistics.yml");
        } else {
            statsFile = new File(addonFolder, "statistics.yml");
        }

        if (!statsFile.exists()) {
            statsFile.getParentFile().mkdirs();
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossibile creare statistics.yml: " + e.getMessage());
            }
        }
        statsConfig = YamlConfiguration.loadConfiguration(statsFile);
    }

    /**
     * Ottiene la cartella addon
     */
    private File getAddonFolder() {
        File pluginsFolder = plugin.getDataFolder().getParentFile();
        
        if (Bukkit.getPluginManager().getPlugin("BedWarsProxy") != null) {
            File proxyFolder = new File(pluginsFolder, "BedWarsProxy");
            if (proxyFolder.exists()) {
                File addonFolder = new File(proxyFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        if (Bukkit.getPluginManager().getPlugin("BedWars1058") != null) {
            File arenaFolder = new File(pluginsFolder, "BedWars1058");
            if (arenaFolder.exists()) {
                File addonFolder = new File(arenaFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        return null;
    }

    /**
     * Carica le statistiche dal file
     */
    private void loadStatistics() {
        if (statsConfig.getConfigurationSection("players") == null) {
            return;
        }

        for (String uuidString : statsConfig.getConfigurationSection("players").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                PlayerStatistics stats = new PlayerStatistics(uuid);
                
                String path = "players." + uuidString + ".";
                stats.setWins(statsConfig.getInt(path + "wins", 0));
                stats.setLosses(statsConfig.getInt(path + "losses", 0));
                stats.setWinstreak(statsConfig.getInt(path + "winstreak", 0));
                stats.setBestWinstreak(statsConfig.getInt(path + "best-winstreak", 0));
                stats.setBestDivision(statsConfig.getString(path + "best-division"));
                stats.setTotalPlaytime(statsConfig.getLong(path + "playtime", 0));
                stats.setLastLogin(statsConfig.getLong(path + "last-login", System.currentTimeMillis()));
                
                statisticsMap.put(uuid, stats);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("UUID non valido in statistics.yml: " + uuidString);
            }
        }
    }

    /**
     * Salva le statistiche nel file
     */
    public void saveStatistics() {
        for (Map.Entry<UUID, PlayerStatistics> entry : statisticsMap.entrySet()) {
            String path = "players." + entry.getKey().toString() + ".";
            PlayerStatistics stats = entry.getValue();
            
            statsConfig.set(path + "wins", stats.getWins());
            statsConfig.set(path + "losses", stats.getLosses());
            statsConfig.set(path + "winstreak", stats.getWinstreak());
            statsConfig.set(path + "best-winstreak", stats.getBestWinstreak());
            statsConfig.set(path + "best-division", stats.getBestDivision());
            statsConfig.set(path + "playtime", stats.getTotalPlaytime());
            statsConfig.set(path + "last-login", stats.getLastLogin());
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossibile salvare statistics.yml: " + e.getMessage());
        }
    }

    /**
     * Ottiene le statistiche di un giocatore
     */
    public PlayerStatistics getStatistics(Player player) {
        return getStatistics(player.getUniqueId());
    }

    /**
     * Ottiene le statistiche di un giocatore tramite UUID
     */
    public PlayerStatistics getStatistics(UUID uuid) {
        return statisticsMap.computeIfAbsent(uuid, PlayerStatistics::new);
    }

    /**
     * Registra una vittoria
     */
    public void recordWin(Player player) {
        if (!configManager.getConfig().getBoolean("statistics.track-wins", true)) {
            return;
        }
        getStatistics(player).addWin();
    }

    /**
     * Registra una sconfitta
     */
    public void recordLoss(Player player) {
        if (!configManager.getConfig().getBoolean("statistics.track-losses", true)) {
            return;
        }
        getStatistics(player).addLoss();
    }

    /**
     * Aggiorna la migliore divisione raggiunta
     */
    public void updateBestDivision(Player player, String divisionName) {
        if (!configManager.getConfig().getBoolean("statistics.track-best-division", true)) {
            return;
        }
        PlayerStatistics stats = getStatistics(player);
        if (stats.getBestDivision() == null || isDivisionHigher(divisionName, stats.getBestDivision())) {
            stats.setBestDivision(divisionName);
        }
    }

    /**
     * Verifica se una divisione è superiore a un'altra
     */
    private boolean isDivisionHigher(String div1, String div2) {
        // Logica semplice: confronta per nome (può essere migliorata)
        String[] order = {"Recluta", "Soldato", "Veterano", "Elite", "Maestro", "Supremo"};
        int index1 = -1, index2 = -1;
        for (int i = 0; i < order.length; i++) {
            if (order[i].equals(div1)) index1 = i;
            if (order[i].equals(div2)) index2 = i;
        }
        return index1 > index2;
    }

    /**
     * Aggiorna il tempo di gioco
     */
    public void updatePlaytime(Player player) {
        if (!configManager.getConfig().getBoolean("statistics.track-playtime", true)) {
            return;
        }
        getStatistics(player).updateLastLogin();
    }

    /**
     * Salva quando il plugin viene disabilitato
     */
    public void onDisable() {
        saveStatistics();
    }
}
