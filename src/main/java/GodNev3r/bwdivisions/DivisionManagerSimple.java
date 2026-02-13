package GodNev3r.bwdivisions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Versione semplificata del DivisionManager che compila senza dipendenze esterne
 */
public class DivisionManagerSimple {
    private final JavaPlugin plugin;
    private boolean isProxyMode = false;
    private boolean isArenaMode = false;
    private DivisionTierManager tierManager;

    public DivisionManagerSimple(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Imposta il DivisionTierManager
     */
    public void setTierManager(DivisionTierManager tierManager) {
        this.tierManager = tierManager;
    }

    /**
     * Inizializza il manager e verifica quale API è disponibile
     */
    public void initialize() {
        // Verifica se è disponibile BedWarsProxy
        try {
            Class.forName("com.tomkeuper.bedwars.proxy.api.BedWarsAPI");
            if (Bukkit.getPluginManager().getPlugin("BedWarsProxy") != null) {
                isProxyMode = true;
                plugin.getLogger().info("Modalità Proxy rilevata!");
            }
        } catch (ClassNotFoundException e) {
            // BedWarsProxy non disponibile
        }

        // Verifica se è disponibile BedWars1058
        try {
            Class.forName("com.tomkeuper.bedwars.api.BedWars");
            if (Bukkit.getPluginManager().getPlugin("BedWars1058") != null) {
                isArenaMode = true;
                plugin.getLogger().info("Modalità Arena (BedWars1058) rilevata!");
            }
        } catch (ClassNotFoundException e) {
            // BedWars1058 non disponibile
        }

        if (!isProxyMode && !isArenaMode) {
            plugin.getLogger().warning("Nessuna API BedWars rilevata! Il plugin funzionerà in modalità base.");
        }
    }

    /**
     * Ottiene il livello del giocatore (versione semplificata)
     */
    public int getPlayerLevel(Player player) {
        if (isProxyMode) {
            // Per modalità Proxy, usa reflection per BedWarsProxy
            try {
                Class<?> bedWarsAPI = Class.forName("com.tomkeuper.bedwars.proxy.api.BedWarsAPI");
                Object api = bedWarsAPI.getMethod("getInstance").invoke(null);
                Object stats = api.getClass().getMethod("getStats").invoke(api);
                return (int) stats.getClass().getMethod("getLevel", java.util.UUID.class).invoke(stats, player.getUniqueId());
            } catch (Exception e) {
                plugin.getLogger().warning("Impossibile ottenere il livello da BedWarsProxy: " + e.getMessage());
            }
        } else if (isArenaMode) {
            // Per modalità Arena, usa reflection per BedWars1058
            try {
                Class<?> bedWars = Class.forName("com.tomkeuper.bedwars.api.BedWars");
                Object api = Bukkit.getServicesManager().getRegistration((Class<?>) bedWars).getProvider();
                Object stats = api.getClass().getMethod("getStats").invoke(api);
                return (int) stats.getClass().getMethod("getLevel", java.util.UUID.class).invoke(stats, player.getUniqueId());
            } catch (Exception e) {
                plugin.getLogger().warning("Impossibile ottenere il livello da BedWars1058: " + e.getMessage());
            }
        }
        
        // Fallback: livello basato su esperienza del giocatore
        return player.getLevel();
    }

    /**
     * Ottiene il prefisso della divisione del giocatore
     */
    public String getDivisionPrefix(Player player) {
        if (tierManager == null) {
            return "§7[§f1§7]"; // Fallback
        }
        
        int level = getPlayerLevel(player);
        DivisionTier tier = tierManager.getTierByLevel(level);
        
        // Formato: §7[§(ColoreRank)(Livello)§7]
        return "§7[" + tier.getColor() + level + "§7]";
    }

    /**
     * Ottiene il nome della divisione del giocatore
     */
    public String getDivisionName(Player player) {
        if (tierManager == null) {
            return "Supremo"; // Fallback
        }
        
        int level = getPlayerLevel(player);
        DivisionTier tier = tierManager.getTierByLevel(level);
        return tier.getName();
    }

    /**
     * Ottiene il DivisionTierManager
     */
    public DivisionTierManager getTierManager() {
        return tierManager;
    }

    /**
     * Ottiene il formato TAB per la lobby (Proxy)
     */
    public String getTabFormatForLobby(Player player) {
        String playerName = player.getName();
        String divisionName = getDivisionName(player);
        return playerName + " " + divisionName;
    }

    /**
     * Ottiene il formato TAB per l'arena (1058)
     */
    public String getTabFormatForArena(Player player) {
        String playerName = player.getName();
        String divisionName = getDivisionName(player);
        return playerName + " " + divisionName;
    }

    /**
     * Verifica se siamo in modalità Proxy (lobby)
     */
    public boolean isProxyMode() {
        return isProxyMode;
    }

    /**
     * Verifica se siamo in modalità Arena (1058)
     */
    public boolean isArenaMode() {
        return isArenaMode;
    }

    /**
     * Verifica se il manager è stato inizializzato correttamente
     */
    public boolean isInitialized() {
        return isProxyMode || isArenaMode;
    }

    /**
     * Imposta manualmente la divisione di un giocatore
     */
    public void setPlayerDivision(Player player, String divisionName) {
        if (tierManager == null) {
            return;
        }
        
        DivisionTier targetTier = null;
        for (DivisionTier tier : tierManager.getAllTiers()) {
            if (tier.getName().equalsIgnoreCase(divisionName)) {
                targetTier = tier;
                break;
            }
        }
        
        if (targetTier != null) {
            // Imposta il livello al livello minimo della divisione
            int targetLevel = targetTier.getMinLevel();
            
            if (isProxyMode) {
                // Per modalità Proxy, usa reflection
                try {
                    Class<?> bedWarsAPI = Class.forName("com.tomkeuper.bedwars.proxy.api.BedWarsAPI");
                    Object api = bedWarsAPI.getMethod("getInstance").invoke(null);
                    Object stats = api.getClass().getMethod("getStats").invoke(api);
                    stats.getClass().getMethod("setLevel", java.util.UUID.class, int.class).invoke(stats, player.getUniqueId(), targetLevel);
                } catch (Exception e) {
                    plugin.getLogger().warning("Impossibile impostare il livello per " + player.getName() + ": " + e.getMessage());
                }
            } else if (isArenaMode) {
                // Per modalità Arena, usa reflection
                try {
                    Class<?> bedWars = Class.forName("com.tomkeuper.bedwars.api.BedWars");
                    Object api = Bukkit.getServicesManager().getRegistration((Class<?>) bedWars).getProvider();
                    Object stats = api.getClass().getMethod("getStats").invoke(api);
                    stats.getClass().getMethod("setLevel", java.util.UUID.class, int.class).invoke(stats, player.getUniqueId(), targetLevel);
                } catch (Exception e) {
                    plugin.getLogger().warning("Impossibile impostare il livello per " + player.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Ottiene il formato TAB corretto in base al tipo di server
     */
    public String getTabFormat(Player player) {
        if (this.isProxyMode) {
            return getTabFormatForLobby(player);
        }
        return getTabFormatForArena(player);
    }
}
