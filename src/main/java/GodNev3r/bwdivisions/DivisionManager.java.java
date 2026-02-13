package GodNev3r.bwdivisions;

import com.tomkeuper.bedwars.api.BedWars;
import com.tomkeuper.bedwars.proxy.api.BedWarsAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Gestisce la compatibilità con BedWars1058 e BedWarsProxy
 * e calcola i prefissi delle divisioni per i giocatori
 */
public class DivisionManager {
    private final JavaPlugin plugin;
    private boolean isProxyMode = false;
    private boolean isArenaMode = false;
    private BedWars bedWarsAPI;
    private DivisionTierManager tierManager;

    public DivisionManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Imposta il DivisionTierManager
     * @param tierManager Il manager dei tier
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
                bedWarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
                isArenaMode = true;
                plugin.getLogger().info("Modalità Arena (1058) rilevata!");
            }
        } catch (ClassNotFoundException | NullPointerException e) {
            // BedWars1058 non disponibile
        }

        if (!isProxyMode && !isArenaMode) {
            plugin.getLogger().warning("Nessuna API BedWars rilevata! Il plugin potrebbe non funzionare correttamente.");
        }
    }

    /**
     * Ottiene il livello del giocatore dall'API appropriata
     * @param player Il giocatore
     * @return Il livello del giocatore, o 1 se non disponibile
     */
    public int getPlayerLevel(Player player) {
        if (isProxyMode) {
            try {
                com.tomkeuper.bedwars.proxy.api.BedWarsAPI api = 
                    com.tomkeuper.bedwars.proxy.api.BedWarsAPI.getInstance();
                if (api != null) {
                    return api.getLevelsUtil().getPlayerLevel(player.getUniqueId());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Errore nel recupero del livello da Proxy API: " + e.getMessage());
            }
        }

        if (isArenaMode && bedWarsAPI != null) {
            try {
                return bedWarsAPI.getLevelsUtil().getPlayerLevel(player.getUniqueId());
            } catch (Exception e) {
                plugin.getLogger().warning("Errore nel recupero del livello da Arena API: " + e.getMessage());
            }
        }

        // Fallback: nuovo giocatore inizia a livello 1
        return 1;
    }

    /**
     * Calcola il prefisso della divisione per il giocatore (per PlaceholderAPI)
     * Formato: §7[§(ColoreRank)(Livello)§7]
     * @param player Il giocatore
     * @return Il prefisso formattato
     */
    public String getDivisionPrefix(Player player) {
        if (tierManager == null) {
            return "§7[§f1§7]"; // Fallback
        }
        
        int level = getPlayerLevel(player);
        DivisionTier tier = tierManager.getTierByLevel(level);
        
        // Formato: §7[§(ColoreRank)(Livello)§7]
        // Esempio: §7[§81§7] per Recluta livello 1
        return "§7[" + tier.getColor() + level + "§7]";
    }

    /**
     * Ottiene il nome della divisione del giocatore
     * @param player Il giocatore
     * @return Il nome della divisione
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
     * @return Il manager dei tier
     */
    public DivisionTierManager getTierManager() {
        return tierManager;
    }

    /**
     * Ottiene il formato TAB per la lobby (Proxy)
     * Formato: NomeGiocatore NomeDivisione
     * @param player Il giocatore
     * @return Il formato TAB per la lobby
     */
    public String getTabFormatForLobby(Player player) {
        String playerName = player.getName();
        String divisionName = getDivisionName(player);
        return playerName + " " + divisionName;
    }

    /**
     * Ottiene il formato TAB per l'arena (1058)
     * Formato: NomeGiocatore NomeDivisione (solo la divisione)
     * @param player Il giocatore
     * @return Il formato TAB per l'arena
     */
    public String getTabFormatForArena(Player player) {
        String playerName = player.getName();
        String divisionName = getDivisionName(player);
        
        // Formato: NomeGiocatore NomeDivisione (solo la divisione, senza colore)
        return playerName + " " + divisionName;
    }

    /**
     * Verifica se siamo in modalità Proxy (lobby)
     * @return true se siamo in modalità Proxy
     */
    public boolean isProxyMode() {
        return isProxyMode;
    }

    /**
     * Verifica se siamo in modalità Arena (1058)
     * @return true se siamo in modalità Arena
     */
    public boolean isArenaMode() {
        return isArenaMode;
    }

    /**
     * Verifica se il manager è stato inizializzato correttamente
     * @return true se almeno una API è disponibile
     */
    public boolean isInitialized() {
        return isProxyMode || isArenaMode;
    }

    /**
     * Imposta manualmente la divisione di un giocatore
     * @param player Il giocatore
     * @param divisionName Il nome della divisione
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
                // Per modalità Proxy, usa l'API di BedWarsProxy
                try {
                    BedWarsAPI.getInstance().getStats().setLevel(player.getUniqueId(), targetLevel);
                } catch (Exception e) {
                    plugin.getLogger().warning("Impossibile impostare il livello per " + player.getName() + ": " + e.getMessage());
                }
            } else if (isArenaMode) {
                // Per modalità Arena, usa l'API di BedWars1058
                try {
                    bedWarsAPI.getStats().setLevel(player.getUniqueId(), targetLevel);
                } catch (Exception e) {
                    plugin.getLogger().warning("Impossibile impostare il livello per " + player.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Ottiene il formato TAB corretto in base al tipo di server
     * @param player Il giocatore
     * @return Il formato TAB appropriato
     */
    public String getTabFormat(Player player) {
        // Se siamo in modalità Proxy (lobby), usa il formato lobby
        if (this.isProxyMode) {
            return getTabFormatForLobby(player);
        }
        // Altrimenti usa il formato arena (1058)
        return getTabFormatForArena(player);
    }
}
