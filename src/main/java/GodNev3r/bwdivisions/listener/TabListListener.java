package GodNev3r.bwdivisions.listener;

import GodNev3r.bwdivisions.DivisionManager;
import GodNev3r.bwdivisions.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener per aggiornare il TAB list con i prefissi delle divisioni
 */
public class TabListListener implements Listener {
    private final Main plugin;
    private final DivisionManager divisionManager;

    public TabListListener(Main plugin, DivisionManager divisionManager) {
        this.plugin = plugin;
        this.divisionManager = divisionManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Aggiorna il TAB dopo un breve delay per permettere il caricamento completo
        new BukkitRunnable() {
            @Override
            public void run() {
                updateTabList(player);
            }
        }.runTaskLater(plugin, 20L); // 1 secondo di delay

        // Aggiorna anche per tutti gli altri giocatori online
        updateTabListForAll();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Aggiorna il TAB per tutti i giocatori rimanenti
        updateTabListForAll();
    }

    /**
     * Aggiorna il TAB list per un singolo giocatore
     * Formato lobby: NomeGiocatore NomeDivisione
     * Formato arena: [Colore] NomeGiocatore NomeDivisione
     */
    public void updateTabList(Player player) {
        if (divisionManager == null || !divisionManager.isInitialized() || player == null) {
            return;
        }

        try {
            // Ottiene il formato corretto in base al tipo di server
            String tabName = divisionManager.getTabFormat(player);
            
            player.setPlayerListName(tabName);
            
        } catch (Exception e) {
            plugin.getLogger().warning("Errore nell'aggiornamento del TAB per " + player.getName() + ": " + e.getMessage());
        }
    }

    /**
     * Aggiorna il TAB list per tutti i giocatori online
     */
    public void updateTabListForAll() {
        if (divisionManager == null || !divisionManager.isInitialized()) {
            return;
        }

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            updateTabList(onlinePlayer);
        }
    }

    /**
     * Aggiorna periodicamente il TAB list (ogni 30 secondi)
     */
    public void startPeriodicUpdate() {
        new BukkitRunnable() {
            @Override
            public void run() {
                updateTabListForAll();
            }
        }.runTaskTimer(plugin, 600L, 600L); // Ogni 30 secondi (600 tick)
    }
}
