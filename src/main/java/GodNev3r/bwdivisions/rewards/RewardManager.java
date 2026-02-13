package GodNev3r.bwdivisions.rewards;

import GodNev3r.bwdivisions.DivisionTier;
import GodNev3r.bwdivisions.Main;
import GodNev3r.bwdivisions.config.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Gestisce le ricompense per le divisioni
 */
public class RewardManager {
    private final Main plugin;
    private final ConfigManager configManager;
    private final List<UUID> rewardedPlayers; // Giocatori che hanno già ricevuto ricompense

    public RewardManager(Main plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.rewardedPlayers = new ArrayList<>();
    }

    /**
     * Verifica se le ricompense sono abilitate
     */
    public boolean isEnabled() {
        return configManager.getConfig().getBoolean("divisions.rewards-enabled", true);
    }

    /**
     * Assegna le ricompense per una divisione
     */
    public void giveRewards(Player player, DivisionTier tier) {
        if (!isEnabled()) {
            return;
        }

        String tierKey = getTierKey(tier);
        if (tierKey == null) {
            return;
        }

        ConfigurationSection tierSection = configManager.getConfig()
            .getConfigurationSection("tiers." + tierKey);
        
        if (tierSection == null) {
            return;
        }

        List<String> rewards = tierSection.getStringList("rewards");
        if (rewards.isEmpty()) {
            return;
        }

        // Esegui i comandi delle ricompense
        for (String reward : rewards) {
            String command = reward.replace("%player%", player.getName());
            
            // Esegui come console per avere tutti i permessi
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        }

        // Invia messaggio di ricompensa
        String message = configManager.getConfig().getString("messages.reward-received",
            "§a§l[BW-Divisions] §7Hai ricevuto una ricompensa per aver raggiunto §f%division%");
        message = message.replace("%division%", tier.getName());
        player.sendMessage(message);
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

    /**
     * Verifica se un giocatore ha già ricevuto le ricompense per una divisione
     */
    public boolean hasReceivedReward(UUID uuid, DivisionTier tier) {
        // Per ora, diamo sempre le ricompense quando si raggiunge una nuova divisione
        // Può essere migliorato salvando nel file quali ricompense sono state date
        return false;
    }
}
