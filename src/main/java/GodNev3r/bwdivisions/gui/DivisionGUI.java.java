package GodNev3r.bwdivisions.gui;

import GodNev3r.bwdivisions.DivisionManager;
import GodNev3r.bwdivisions.DivisionTier;
import GodNev3r.bwdivisions.DivisionTierManager;
import GodNev3r.bwdivisions.Main;
import GodNev3r.bwdivisions.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Gestisce la GUI delle divisioni
 */
public class DivisionGUI {
    private final Main plugin;
    private final DivisionManager divisionManager;
    private final ConfigManager configManager;

    public DivisionGUI(Main plugin, DivisionManager divisionManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.divisionManager = divisionManager;
        this.configManager = configManager;
    }

    /**
     * Apre la GUI delle divisioni per un giocatore
     */
    public void openGUI(Player player) {
        if (!configManager.getConfig().getBoolean("divisions.gui-enabled", true)) {
            player.sendMessage(configManager.getConfig().getString("messages.gui-opened",
                "§cLa GUI delle divisioni è disabilitata!"));
            return;
        }

        int size = configManager.getConfig().getInt("divisions.gui-size", 54);
        String title = configManager.getConfig().getString("divisions.gui-title", "§8§lDivisioni");
        
        Inventory gui = Bukkit.createInventory(null, size, title);
        
        DivisionTierManager tierManager = divisionManager.getTierManager();
        if (tierManager == null) {
            return;
        }

        ConfigurationSection guiItems = configManager.getConfig()
            .getConfigurationSection("gui.items");
        
        if (guiItems == null) {
            return;
        }

        // Aggiungi gli item per ogni divisione
        for (DivisionTier tier : tierManager.getAllTiers()) {
            String tierKey = getTierKey(tier);
            if (tierKey == null) {
                continue;
            }

            ConfigurationSection itemSection = guiItems.getConfigurationSection(tierKey);
            if (itemSection == null) {
                continue;
            }

            ItemStack item = createDivisionItem(tier, itemSection, player);
            int slot = itemSection.getInt("slot", -1);
            
            if (slot >= 0 && slot < size) {
                gui.setItem(slot, item);
            }
        }

        player.openInventory(gui);
        
        String message = configManager.getConfig().getString("messages.gui-opened",
            "§aGUI delle divisioni aperta!");
        player.sendMessage(message);
    }

    /**
     * Crea un item per una divisione
     */
    private ItemStack createDivisionItem(DivisionTier tier, ConfigurationSection itemSection, Player player) {
        String materialName = itemSection.getString("material", "PAPER");
        Material material = Material.matchMaterial(materialName);
        if (material == null) {
            material = Material.PAPER;
        }

        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta == null) {
            return item;
        }

        String name = itemSection.getString("name", tier.getName());
        name = name.replace("%division%", tier.getName())
                  .replace("%color%", tier.getColor())
                  .replace("%min_level%", String.valueOf(tier.getMinLevel()))
                  .replace("%max_level%", String.valueOf(tier.getMaxLevel()));
        meta.setDisplayName(name);

        List<String> lore = itemSection.getStringList("lore");
        List<String> formattedLore = new ArrayList<>();
        
        for (String line : lore) {
            line = line.replace("%division%", tier.getName())
                      .replace("%color%", tier.getColor())
                      .replace("%min_level%", String.valueOf(tier.getMinLevel()))
                      .replace("%max_level%", String.valueOf(tier.getMaxLevel()));
            
            // Aggiungi informazioni sul progresso se il giocatore è in questa divisione
            String currentDivision = divisionManager.getDivisionName(player);
            if (currentDivision.equals(tier.getName())) {
                line = line.replace("%current%", "§a✓ Attuale");
            }
            
            formattedLore.add(line);
        }
        
        meta.setLore(formattedLore);
        item.setItemMeta(meta);
        
        return item;
    }

    /**
     * Ottiene la chiave del tier dal config
     */
    private String getTierKey(DivisionTier tier) {
        ConfigurationSection tiersSection = configManager.getConfig()
            .getConfigurationSection("tiers");
        
        if (tiersSection == null) {
            return null;
        }

        for (String key : tiersSection.getKeys(false)) {
            ConfigurationSection tierSection = tiersSection.getConfigurationSection(key);
            if (tierSection != null && tierSection.getString("name", "").equals(tier.getName())) {
                return key;
            }
        }
        return null;
    }
}
