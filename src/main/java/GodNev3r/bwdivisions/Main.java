package GodNev3r.bwdivisions;

import org.bukkit.plugin.java.JavaPlugin;
import GodNev3r.bwdivisions.config.ConfigManager;
import GodNev3r.bwdivisions.stats.StatisticsManager;
import GodNev3r.bwdivisions.economy.EconomyManager;
import GodNev3r.bwdivisions.listener.DivisionChangeListener;

public class Main extends JavaPlugin {

    private static Main instance;
    private ConfigManager configManager;
    private StatisticsManager statisticsManager;
    private EconomyManager economyManager;

    @Override
    public void onEnable() {
        instance = this;
        
        // Inizializza il file di configurazione
        saveDefaultConfig();
        
        // Inizializza i manager
        configManager = new ConfigManager(this);
        statisticsManager = new StatisticsManager(this);
        economyManager = new EconomyManager(this);
        
        // Registra gli event listener
        getServer().getPluginManager().registerEvents(new DivisionChangeListener(this), this);
        
        getLogger().info("==============================");
        getLogger().info("BW-Divisions v" + getDescription().getVersion());
        getLogger().info("Plugin abilitato con successo!");
        getLogger().info("==============================");
    }

    @Override
    public void onDisable() {
        getLogger().info("BW-Divisions disabilitato!");
    }

    public static Main getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StatisticsManager getStatisticsManager() {
        return statisticsManager;
    }

    public EconomyManager getEconomyManager() {
        return economyManager;
    }
}