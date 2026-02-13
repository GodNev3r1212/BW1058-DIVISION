package GodNev3r.bwdivisions.gui;

import GodNev3r.bwdivisions.DivisionManager;
import GodNev3r.bwdivisions.DivisionTier;
import GodNev3r.bwdivisions.DivisionTierManager;
import GodNev3r.bwdivisions.Main;
import GodNev3r.bwdivisions.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Gestisce la GUI delle top divisioni con le teste dei giocatori
 */
public class TopDivisionsGUI {
    private final Main plugin;
    private final DivisionManager divisionManager;
    private final ConfigManager configManager;

    public TopDivisionsGUI(Main plugin, DivisionManager divisionManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.divisionManager = divisionManager;
        this.configManager = configManager;
    }

    /**
     * Apre la GUI delle top divisioni
     */
    public void openTopGUI(Player player) {
        int size = 54; // 6 righe
        String title = "§8§lTop Divisioni";
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        DivisionTierManager tierManager = divisionManager.getTierManager();
        if (tierManager == null) {
            player.sendMessage("§cIl sistema delle divisioni non è disponibile!");
            return;
        }

        // Crea una mappa per raggruppare i giocatori per divisione
        Map<String, List<Player>> playersByDivision = new HashMap<>();
        
        // Raggruppa i giocatori online per divisione
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String divisionName = divisionManager.getDivisionName(onlinePlayer);
            playersByDivision.computeIfAbsent(divisionName, k -> new ArrayList<>()).add(onlinePlayer);
        }

        // Ordina le divisioni per livello (dalla più alta alla più bassa)
        List<DivisionTier> sortedTiers = new ArrayList<>(tierManager.getAllTiers());
        sortedTiers.sort((a, b) -> Integer.compare(b.getMinLevel(), a.getMinLevel()));

        int slot = 0;
        // Mostra le prime 3 divisioni con i loro giocatori
        for (int i = 0; i < Math.min(3, sortedTiers.size()); i++) {
            DivisionTier tier = sortedTiers.get(i);
            List<Player> playersInDivision = playersByDivision.get(tier.getName());
            
            if (playersInDivision == null || playersInDivision.isEmpty()) {
                // Se non ci sono giocatori in questa divisione, mostra un item informativo
                ItemStack emptyItem = createEmptyDivisionItem(tier, i + 1);
                if (slot < size) {
                    gui.setItem(slot, emptyItem);
                }
                slot++;
                continue;
            }

            // Mostra i giocatori di questa divisione (max 5 per divisione)
            int playersToShow = Math.min(5, playersInDivision.size());
            for (int j = 0; j < playersToShow; j++) {
                Player divisionPlayer = playersInDivision.get(j);
                ItemStack playerHead = createPlayerHead(divisionPlayer, tier, i + 1, j + 1);
                
                if (slot < size) {
                    gui.setItem(slot, playerHead);
                }
                slot++;
            }
        }

        // Aggiungi item decorativi
        addDecorativeItems(gui);

        player.openInventory(gui);
        player.sendMessage("§aGUI delle top divisioni aperta!");
    }

    /**
     * Ottiene un materiale compatibile con diverse versioni di Bukkit
     */
    private Material getMaterial(String newName, String oldName) {
        try {
            return Material.valueOf(newName);
        } catch (IllegalArgumentException e) {
            try {
                return Material.valueOf(oldName);
            } catch (IllegalArgumentException e2) {
                return Material.STONE; // Fallback
            }
        }
    }

    /**
     * Crea una testa di giocatore per la GUI
     */
    private ItemStack createPlayerHead(Player player, DivisionTier tier, int divisionRank, int playerRank) {
        ItemStack head = new ItemStack(getMaterial("PLAYER_HEAD", "SKULL_ITEM"));
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            try {
                // Per versioni più vecchie di Bukkit (1.8.9)
                meta.setOwner(player.getName());
            } catch (NoSuchMethodError e) {
                // Per versioni più nuove di Bukkit
                meta.setOwningPlayer(player);
            }
            
            String displayName = tier.getColor() + "Top " + divisionRank + " - " + player.getName();
            meta.setDisplayName(displayName);
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Divisione: " + tier.getColor() + tier.getName());
            lore.add("§7Livello: " + divisionManager.getPlayerLevel(player));
            lore.add("§7Posizione: #" + playerRank);
            lore.add("");
            lore.add("§eClicca per vedere i dettagli!");
            
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        
        return head;
    }

    /**
     * Crea un item per divisioni vuote
     */
    private ItemStack createEmptyDivisionItem(DivisionTier tier, int rank) {
        ItemStack item = new ItemStack(getMaterial("GRAY_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"));
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(tier.getColor() + "Top " + rank + " - " + tier.getName());
            
            List<String> lore = new ArrayList<>();
            lore.add("§7Nessun giocatore in questa divisione");
            lore.add("§7al momento.");
            lore.add("");
            lore.add("§cSii il primo a raggiungerla!");
            
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        
        return item;
    }

    /**
     * Aggiunge item decorativi alla GUI
     */
    private void addDecorativeItems(Inventory gui) {
        // Bordo di vetri colorati
        ItemStack border = new ItemStack(getMaterial("BLACK_STAINED_GLASS_PANE", "STAINED_GLASS_PANE"));
        ItemMeta borderMeta = border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName("§f");
            border.setItemMeta(borderMeta);
        }

        // Bordo superiore e inferiore
        for (int i = 0; i < 9; i++) {
            gui.setItem(i, border.clone());
            gui.setItem(45 + i, border.clone());
        }

        // Bordi laterali
        for (int i = 9; i < 45; i += 9) {
            gui.setItem(i, border.clone());
            gui.setItem(i + 8, border.clone());
        }

        // Item informativo centrale
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName("§6§lInformazioni");
            List<String> lore = new ArrayList<>();
            lore.add("§7Questa GUI mostra i migliori");
            lore.add("§7giocatori per ogni divisione.");
            lore.add("");
            lore.add("§eLe divisioni sono ordinate");
            lore.add("§edalla più alta alla più bassa.");
            lore.add("");
            lore.add("§7Aggiornamento in tempo reale!");
            infoMeta.setLore(lore);
            info.setItemMeta(infoMeta);
        }
        gui.setItem(49, info);
    }
}
