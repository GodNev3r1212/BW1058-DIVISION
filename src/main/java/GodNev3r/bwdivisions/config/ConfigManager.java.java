package GodNev3r.bwdivisions.config;

import GodNev3r.bwdivisions.DivisionManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Gestisce il file config.yml nella cartella addon
 */
public class ConfigManager {
    private final JavaPlugin plugin;
    private final DivisionManager divisionManager;
    private File configFile;
    private FileConfiguration config;

    public ConfigManager(JavaPlugin plugin, DivisionManager divisionManager) {
        this.plugin = plugin;
        this.divisionManager = divisionManager;
    }

    /**
     * Inizializza il file di configurazione
     */
    public void initialize() {
        loadConfigFile();
        loadDefaultConfig();
    }

    /**
     * Carica il file di configurazione dalla cartella addon corretta
     */
    private void loadConfigFile() {
        File addonFolder = getAddonFolder();
        if (addonFolder == null) {
            // Fallback alla cartella del plugin se non riesce a determinare la cartella corretta
            configFile = new File(plugin.getDataFolder(), "config.yml");
            plugin.getLogger().warning("Impossibile determinare la cartella addon, uso la cartella del plugin come fallback");
        } else {
            configFile = new File(addonFolder, "config.yml");
            plugin.getLogger().info("Config.yml verrà salvato in: " + configFile.getAbsolutePath());
        }
        
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveDefaultConfig();
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    /**
     * Ottiene la cartella addon corretta in base al tipo di server
     * @return La cartella addon o null se non può essere determinata
     */
    private File getAddonFolder() {
        File pluginsFolder = plugin.getDataFolder().getParentFile();
        
        // Verifica se siamo in modalità Proxy
        if (divisionManager != null && divisionManager.isProxyMode()) {
            File proxyFolder = new File(pluginsFolder, "BedWarsProxy");
            if (proxyFolder.exists() && proxyFolder.isDirectory()) {
                File addonFolder = new File(proxyFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        // Verifica se siamo in modalità Arena (1058)
        if (divisionManager != null && divisionManager.isArenaMode()) {
            File arenaFolder = new File(pluginsFolder, "BedWars1058");
            if (arenaFolder.exists() && arenaFolder.isDirectory()) {
                File addonFolder = new File(arenaFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        // Se non è ancora inizializzato, prova a verificare direttamente
        if (Bukkit.getPluginManager().getPlugin("BedWarsProxy") != null) {
            File proxyFolder = new File(pluginsFolder, "BedWarsProxy");
            if (proxyFolder.exists() && proxyFolder.isDirectory()) {
                File addonFolder = new File(proxyFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        if (Bukkit.getPluginManager().getPlugin("BedWars1058") != null) {
            File arenaFolder = new File(pluginsFolder, "BedWars1058");
            if (arenaFolder.exists() && arenaFolder.isDirectory()) {
                File addonFolder = new File(arenaFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        return null;
    }

    /**
     * Carica la configurazione di default se non esiste
     */
    private void loadDefaultConfig() {
        // Aggiungi valori di default se non esistono
        if (!config.contains("economy")) {
            config.set("economy.enabled", true);
            config.set("economy.starting-balance", 0.0);
        }
        
        if (!config.contains("divisions")) {
            config.set("divisions.update-interval", 30);
            config.set("divisions.check-interval", 10);
            config.set("divisions.rewards-enabled", true);
            config.set("divisions.statistics-enabled", true);
            config.set("divisions.leaderboard-enabled", true);
            config.set("divisions.leaderboard-size", 10);
            config.set("divisions.gui-enabled", true);
            config.set("divisions.gui-title", "§8§lDivisioni");
            config.set("divisions.gui-size", 54);
        }
        
        // Carica i messaggi di default se non esistono
        if (!config.contains("messages")) {
            config.set("messages.division-up", "§a§l[BW-Divisions] §7Sei salito di divisione a §f%new_division%");
            config.set("messages.division-down", "§c§l[BW-Divisions] §7Sei sceso di divisione da §f%old_division% §7a §f%new_division%");
            config.set("messages.broadcast-division-up", false);
            config.set("messages.broadcast-division-down", false);
            config.set("messages.division-up-broadcast", "§a§l[BW-Divisions] §7%player% è salito di divisione a §f%new_division%");
            config.set("messages.division-down-broadcast", "§c§l[BW-Divisions] §7%player% è sceso di divisione da §f%old_division% §7a §f%new_division%");
            config.set("messages.reward-received", "§a§l[BW-Divisions] §7Hai ricevuto una ricompensa per aver raggiunto §f%division%");
            config.set("messages.stats-header", "§8§m----------§r §6§lStatistiche Divisioni §8§m----------");
            config.set("messages.gui-opened", "§aGUI delle divisioni aperta!");
            config.set("messages.no-permission", "§cNon hai il permesso per eseguire questo comando!");
            config.set("messages.invalid-command", "§cComando non valido! Usa /bwdiv help");
            config.set("messages.help-header", "§8§m----------§r §6§lBW-Divisions Help §8§m----------");
            config.set("messages.help-commands", java.util.Arrays.asList(
                "§7/bwdiv stats §8- §fVisualizza le tue statistiche",
                "§7/bwdiv gui §8- §fApri la GUI delle divisioni",
                "§7/bwdiv leaderboard §8- §fVisualizza la classifica",
                "§7/bwdiv reload §8- §fRicarica la configurazione (admin)"
            ));
        }
        
        // Carica le statistiche di default se non esistono
        if (!config.contains("statistics")) {
            config.set("statistics.enabled", true);
            config.set("statistics.track-wins", true);
            config.set("statistics.track-losses", true);
            config.set("statistics.track-winstreak", true);
            config.set("statistics.track-best-division", true);
            config.set("statistics.track-playtime", true);
        }
        
        // Carica i comandi di default se non esistono
        if (!config.contains("commands")) {
            config.set("commands.main-command", "bwdiv");
            config.set("commands.aliases", java.util.Arrays.asList("bwdivisions", "divisions"));
            config.set("commands.permissions.stats", "bwdiv.stats");
            config.set("commands.permissions.gui", "bwdiv.gui");
            config.set("commands.permissions.leaderboard", "bwdiv.leaderboard");
            config.set("commands.permissions.reload", "bwdiv.reload");
            config.set("commands.permissions.admin", "bwdiv.admin");
        }
        
        // Carica il leaderboard di default se non esiste
        if (!config.contains("leaderboard")) {
            config.set("leaderboard.enabled", true);
            config.set("leaderboard.type", "level");
            config.set("leaderboard.size", 10);
            config.set("leaderboard.format.header", "§8§m----------§r §6§lTop %size% Giocatori §8§m----------");
            config.set("leaderboard.format.entry", "§7%position%. §f%player% §7- §f%value%");
            config.set("leaderboard.format.footer", "§8§m--------------------------------");
        }
        
        // Carica le placeholders di default se non esistono
        if (!config.contains("placeholders")) {
            config.set("placeholders.prefix-format", "§7[§%color%%level%§7]");
            config.set("placeholders.name-format", "%color%%name%");
            config.set("placeholders.progress-format", "%progress%%");
            config.set("placeholders.levels-format", "%levels% livelli");
        }
        
        // Carica le impostazioni avanzate di default se non esistono
        if (!config.contains("advanced")) {
            config.set("advanced.debug", false);
            config.set("advanced.auto-save-interval", 300);
            config.set("advanced.use-cache", true);
            config.set("advanced.cache-time", 30);
        }
        
        // Carica le divisioni di default se non esistono (controlla entrambi i formati)
        if (!config.contains("tiers") && !config.contains("divisions.tiers")) {
            loadDefaultTiers();
        }
        
        saveConfig();
    }

    /**
     * Carica le divisioni di default nel config (formato nuovo: tiers)
     */
    private void loadDefaultTiers() {
        // Usa il formato nuovo "tiers" invece di "divisions.tiers"
        config.set("tiers.recluta.name", "Recluta");
        config.set("tiers.recluta.color", "§8");
        config.set("tiers.recluta.min-level", 1);
        config.set("tiers.recluta.max-level", 14);
        config.set("tiers.recluta.rewards", java.util.Arrays.asList(
            "give %player% minecraft:gold_ingot 5",
            "eco give %player% 100"
        ));
        config.set("tiers.recluta.custom-message", "");
        
        config.set("tiers.soldato.name", "Soldato");
        config.set("tiers.soldato.color", "§f");
        config.set("tiers.soldato.min-level", 15);
        config.set("tiers.soldato.max-level", 29);
        config.set("tiers.soldato.rewards", java.util.Arrays.asList(
            "give %player% minecraft:gold_ingot 10",
            "eco give %player% 250"
        ));
        config.set("tiers.soldato.custom-message", "");
        
        config.set("tiers.veterano.name", "Veterano");
        config.set("tiers.veterano.color", "§a");
        config.set("tiers.veterano.min-level", 30);
        config.set("tiers.veterano.max-level", 49);
        config.set("tiers.veterano.rewards", java.util.Arrays.asList(
            "give %player% minecraft:diamond 5",
            "eco give %player% 500"
        ));
        config.set("tiers.veterano.custom-message", "");
        
        config.set("tiers.elite.name", "Elite");
        config.set("tiers.elite.color", "§3");
        config.set("tiers.elite.min-level", 50);
        config.set("tiers.elite.max-level", 74);
        config.set("tiers.elite.rewards", java.util.Arrays.asList(
            "give %player% minecraft:diamond 10",
            "eco give %player% 1000"
        ));
        config.set("tiers.elite.custom-message", "");
        
        config.set("tiers.maestro.name", "Maestro");
        config.set("tiers.maestro.color", "§d");
        config.set("tiers.maestro.min-level", 75);
        config.set("tiers.maestro.max-level", 99);
        config.set("tiers.maestro.rewards", java.util.Arrays.asList(
            "give %player% minecraft:emerald 5",
            "eco give %player% 2500"
        ));
        config.set("tiers.maestro.custom-message", "");
        
        config.set("tiers.supremo.name", "Supremo");
        config.set("tiers.supremo.color", "§e");
        config.set("tiers.supremo.min-level", 100);
        config.set("tiers.supremo.max-level", Integer.MAX_VALUE);
        config.set("tiers.supremo.rewards", java.util.Arrays.asList(
            "give %player% minecraft:emerald 10",
            "eco give %player% 5000"
        ));
        config.set("tiers.supremo.custom-message", "");
    }

    /**
     * Salva la configurazione di default dal JAR
     */
    private void saveDefaultConfig() {
        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            try {
                Files.copy(defaultConfigStream, configFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                plugin.getLogger().severe("Impossibile copiare il config.yml di default: " + e.getMessage());
            }
        } else {
            // Crea un config.yml vuoto con valori di default
            try {
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);
                loadDefaultConfig();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossibile creare il config.yml: " + e.getMessage());
            }
        }
    }

    /**
     * Salva la configurazione
     */
    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossibile salvare il config.yml: " + e.getMessage());
        }
    }

    /**
     * Ricarica la configurazione dal file
     */
    public void reloadConfig() {
        config = YamlConfiguration.loadConfiguration(configFile);
        loadDefaultConfig();
    }

    /**
     * Ottiene la configurazione
     * @return La configurazione
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    /**
     * Ottiene il file di configurazione
     * @return Il file di configurazione
     */
    public File getConfigFile() {
        return configFile;
    }
}
