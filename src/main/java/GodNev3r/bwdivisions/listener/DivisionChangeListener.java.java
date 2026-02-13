package GodNev3r.bwdivisions.listener;

import GodNev3r.bwdivisions.*;
import GodNev3r.bwdivisions.rewards.RewardManager;
import GodNev3r.bwdivisions.stats.StatisticsManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listener per rilevare i cambiamenti di divisione e inviare messaggi
 */
public class DivisionChangeListener implements Listener {
    private final Main plugin;
    private final DivisionManager divisionManager;
    private final ConfigManager configManager;
    private final RewardManager rewardManager;
    private final StatisticsManager statisticsManager;
    private final Map<UUID, PlayerDivisionData> playerDataMap;

    public DivisionChangeListener(Main plugin, DivisionManager divisionManager, ConfigManager configManager,
                                  RewardManager rewardManager, StatisticsManager statisticsManager) {
        this.plugin = plugin;
        this.divisionManager = divisionManager;
        this.configManager = configManager;
        this.rewardManager = rewardManager;
        this.statisticsManager = statisticsManager;
        this.playerDataMap = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Inizializza i dati del giocatore dopo un breve delay
        new BukkitRunnable() {
            @Override
            public void run() {
                initializePlayerData(player);
            }
        }.runTaskLater(plugin, 20L); // 1 secondo di delay
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Rimuove i dati del giocatore quando esce
        removePlayerData(event.getPlayer().getUniqueId());
    }

    /**
     * Inizializza i dati della divisione per un giocatore
     */
    private void initializePlayerData(Player player) {
        if (divisionManager == null || !divisionManager.isInitialized()) {
            return;
        }

        UUID uuid = player.getUniqueId();
        String currentDivision = divisionManager.getDivisionName(player);
        int currentLevel = divisionManager.getPlayerLevel(player);

        PlayerDivisionData data = new PlayerDivisionData(uuid);
        data.setCurrentDivision(currentDivision);
        data.setCurrentLevel(currentLevel);
        playerDataMap.put(uuid, data);
    }

    /**
     * Controlla se la divisione di un giocatore è cambiata
     */
    public void checkDivisionChange(Player player) {
        if (divisionManager == null || !divisionManager.isInitialized() || player == null) {
            return;
        }

        UUID uuid = player.getUniqueId();
        PlayerDivisionData data = playerDataMap.get(uuid);
        
        if (data == null) {
            initializePlayerData(player);
            return;
        }

        String newDivision = divisionManager.getDivisionName(player);
        int newLevel = divisionManager.getPlayerLevel(player);

        // Se la divisione è cambiata
        if (data.hasDivisionChanged(newDivision)) {
            String oldDivision = data.getCurrentDivision();
            
            // Determina se è salito o sceso
            DivisionTierManager tierManager = divisionManager.getTierManager();
            if (tierManager != null) {
                DivisionTier oldTier = null;
                DivisionTier newTier = null;
                
                for (DivisionTier tier : tierManager.getAllTiers()) {
                    if (tier.getName().equals(oldDivision)) {
                        oldTier = tier;
                    }
                    if (tier.getName().equals(newDivision)) {
                        newTier = tier;
                    }
                }
                
                if (oldTier != null && newTier != null) {
                    if (newTier.getMinLevel() > oldTier.getMinLevel()) {
                        // Salito di divisione
                        sendDivisionUpMessage(player, oldDivision, newDivision);
                        // Assegna ricompense
                        if (rewardManager != null) {
                            rewardManager.giveRewards(player, newTier);
                        }
                        // Aggiorna statistiche
                        if (statisticsManager != null) {
                            statisticsManager.updateBestDivision(player, newDivision);
                        }
                    } else {
                        // Sceso di divisione
                        sendDivisionDownMessage(player, oldDivision, newDivision);
                    }
                }
            }
        }

        // Aggiorna i dati
        data.setCurrentDivision(newDivision);
        data.setCurrentLevel(newLevel);
    }

    /**
     * Invia il messaggio quando un giocatore sale di divisione
     */
    private void sendDivisionUpMessage(Player player, String oldDivision, String newDivision) {
        FileConfiguration config = configManager.getConfig();
        String message = config.getString("messages.division-up", 
            "§a§l[BW-Divisions] §7Sei salito di divisione a §f%new_division%");
        
        message = message.replace("%player%", player.getName())
                        .replace("%old_division%", oldDivision)
                        .replace("%new_division%", newDivision);
        
        player.sendMessage(message);
        
        // Messaggio broadcast opzionale
        if (config.getBoolean("messages.broadcast-division-up", false)) {
            String broadcast = config.getString("messages.division-up-broadcast",
                "§a§l[BW-Divisions] §7%player% è salito di divisione a §f%new_division%");
            broadcast = broadcast.replace("%player%", player.getName())
                               .replace("%old_division%", oldDivision)
                               .replace("%new_division%", newDivision);
            Bukkit.broadcastMessage(broadcast);
        }
    }

    /**
     * Invia il messaggio quando un giocatore scende di divisione
     */
    private void sendDivisionDownMessage(Player player, String oldDivision, String newDivision) {
        FileConfiguration config = configManager.getConfig();
        String message = config.getString("messages.division-down",
            "§c§l[BW-Divisions] §7Sei sceso di divisione da §f%old_division% §7a §f%new_division%");
        
        message = message.replace("%player%", player.getName())
                        .replace("%old_division%", oldDivision)
                        .replace("%new_division%", newDivision);
        
        player.sendMessage(message);
        
        // Messaggio broadcast opzionale
        if (config.getBoolean("messages.broadcast-division-down", false)) {
            String broadcast = config.getString("messages.division-down-broadcast",
                "§c§l[BW-Divisions] §7%player% è sceso di divisione da §f%old_division% §7a §f%new_division%");
            broadcast = broadcast.replace("%player%", player.getName())
                               .replace("%old_division%", oldDivision)
                               .replace("%new_division%", newDivision);
            Bukkit.broadcastMessage(broadcast);
        }
    }

    /**
     * Controlla tutti i giocatori online per cambiamenti di divisione
     */
    public void checkAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            checkDivisionChange(player);
        }
    }

    /**
     * Rimuove i dati di un giocatore quando esce
     */
    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }
}
