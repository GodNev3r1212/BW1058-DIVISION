package GodNev3r.bwdivisions;

import GodNev3r.bwdivisions.config.ConfigManager;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gestisce il caricamento e la gestione delle divisioni dal config
 */
public class DivisionTierManager {
    private final ConfigManager configManager;
    private List<DivisionTier> tiers;

    public DivisionTierManager(ConfigManager configManager) {
        this.configManager = configManager;
        this.tiers = new ArrayList<>();
        loadTiersFromConfig();
    }

    /**
     * Carica le divisioni dal config
     */
    public void loadTiersFromConfig() {
        tiers.clear();
        
        // Prova prima "tiers" (formato nuovo), poi "divisions.tiers" (formato vecchio)
        ConfigurationSection divisionsSection = configManager.getConfig().getConfigurationSection("tiers");
        if (divisionsSection == null) {
            divisionsSection = configManager.getConfig().getConfigurationSection("divisions.tiers");
        }
        
        if (divisionsSection == null) {
            // Carica le divisioni di default
            loadDefaultTiers();
            return;
        }

        for (String key : divisionsSection.getKeys(false)) {
            ConfigurationSection tierSection = divisionsSection.getConfigurationSection(key);
            if (tierSection == null) {
                continue;
            }

            String name = tierSection.getString("name", key);
            String color = tierSection.getString("color", "§f");
            int minLevel = tierSection.getInt("min-level", 1);
            int maxLevel = tierSection.getInt("max-level", Integer.MAX_VALUE);

            // Valida i valori
            if (name == null || name.isEmpty()) {
                continue;
            }
            if (minLevel < 0) {
                minLevel = 0;
            }
            if (maxLevel < minLevel) {
                maxLevel = Integer.MAX_VALUE;
            }

            tiers.add(new DivisionTier(name, color, minLevel, maxLevel));
        }

        // Ordina per minLevel
        tiers.sort((t1, t2) -> Integer.compare(t1.getMinLevel(), t2.getMinLevel()));

        // Se non ci sono divisioni, carica quelle di default
        if (tiers.isEmpty()) {
            loadDefaultTiers();
        }
    }

    /**
     * Carica le divisioni di default
     */
    private void loadDefaultTiers() {
        tiers.clear();
        tiers.add(new DivisionTier("Recluta", "§8", 1, 14));
        tiers.add(new DivisionTier("Soldato", "§f", 15, 29));
        tiers.add(new DivisionTier("Veterano", "§a", 30, 49));
        tiers.add(new DivisionTier("Elite", "§3", 50, 74));
        tiers.add(new DivisionTier("Maestro", "§d", 75, 99));
        tiers.add(new DivisionTier("Supremo", "§e", 100, Integer.MAX_VALUE));
    }

    /**
     * Ottiene il tier corrispondente al livello specificato
     * @param level Il livello del giocatore
     * @return Il DivisionTier corrispondente, o l'ultimo tier se non trovato
     */
    public DivisionTier getTierByLevel(int level) {
        for (DivisionTier tier : tiers) {
            if (tier.containsLevel(level)) {
                return tier;
            }
        }
        // Fallback: ritorna l'ultimo tier (di solito il più alto)
        if (!tiers.isEmpty()) {
            return tiers.get(tiers.size() - 1);
        }
        // Se non ci sono tier, crea uno di default
        return new DivisionTier("Supremo", "§e", 100, Integer.MAX_VALUE);
    }

    /**
     * Ottiene tutte le divisioni
     * @return Lista di tutte le divisioni
     */
    public List<DivisionTier> getAllTiers() {
        return Collections.unmodifiableList(tiers);
    }

    /**
     * Ricarica le divisioni dal config
     */
    public void reload() {
        configManager.reloadConfig();
        loadTiersFromConfig();
    }
}
