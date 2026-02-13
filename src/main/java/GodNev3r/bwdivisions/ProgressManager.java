package GodNev3r.bwdivisions;

import org.bukkit.entity.Player;

/**
 * Gestisce il calcolo del progresso verso la prossima divisione
 */
public class ProgressManager {
    private final DivisionManager divisionManager;
    private final DivisionTierManager tierManager;

    public ProgressManager(DivisionManager divisionManager, DivisionTierManager tierManager) {
        this.divisionManager = divisionManager;
        this.tierManager = tierManager;
    }

    /**
     * Calcola il progresso percentuale verso la prossima divisione
     * @param player Il giocatore
     * @return Percentuale di progresso (0-100)
     */
    public double getProgressPercent(Player player) {
        if (tierManager == null) {
            return 0.0;
        }

        int currentLevel = divisionManager.getPlayerLevel(player);
        DivisionTier currentTier = tierManager.getTierByLevel(currentLevel);
        
        // Se è già al massimo livello della divisione, controlla se c'è una divisione successiva
        if (currentLevel >= currentTier.getMaxLevel()) {
            // Cerca la prossima divisione
            DivisionTier nextTier = getNextTier(currentTier);
            if (nextTier == null) {
                return 100.0; // È già alla divisione massima
            }
            
            // Calcola il progresso verso la prossima divisione
            int levelRange = nextTier.getMaxLevel() - nextTier.getMinLevel() + 1;
            int levelsNeeded = nextTier.getMinLevel() - currentLevel;
            int totalLevels = levelRange;
            
            if (totalLevels <= 0) {
                return 100.0;
            }
            
            return Math.min(100.0, Math.max(0.0, (1.0 - (double) levelsNeeded / totalLevels) * 100.0));
        }
        
        // Calcola il progresso all'interno della divisione corrente
        int levelRange = currentTier.getMaxLevel() - currentTier.getMinLevel() + 1;
        int currentProgress = currentLevel - currentTier.getMinLevel() + 1;
        
        if (levelRange <= 0) {
            return 100.0;
        }
        
        return Math.min(100.0, Math.max(0.0, ((double) currentProgress / levelRange) * 100.0));
    }

    /**
     * Ottiene i livelli rimanenti per raggiungere la prossima divisione
     * @param player Il giocatore
     * @return Livelli rimanenti, o 0 se è già alla divisione massima
     */
    public int getLevelsUntilNextDivision(Player player) {
        if (tierManager == null) {
            return 0;
        }

        int currentLevel = divisionManager.getPlayerLevel(player);
        DivisionTier currentTier = tierManager.getTierByLevel(currentLevel);
        
        // Se è già al massimo livello della divisione, controlla se c'è una divisione successiva
        if (currentLevel >= currentTier.getMaxLevel()) {
            DivisionTier nextTier = getNextTier(currentTier);
            if (nextTier == null) {
                return 0; // È già alla divisione massima
            }
            return Math.max(0, nextTier.getMinLevel() - currentLevel);
        }
        
        // Calcola i livelli rimanenti nella divisione corrente
        return Math.max(0, currentTier.getMaxLevel() - currentLevel + 1);
    }

    /**
     * Ottiene la prossima divisione
     * @param currentTier La divisione corrente
     * @return La prossima divisione o null se non esiste
     */
    private DivisionTier getNextTier(DivisionTier currentTier) {
        if (tierManager == null) {
            return null;
        }

        boolean foundCurrent = false;
        for (DivisionTier tier : tierManager.getAllTiers()) {
            if (foundCurrent) {
                return tier;
            }
            if (tier.getMinLevel() == currentTier.getMinLevel() && 
                tier.getMaxLevel() == currentTier.getMaxLevel()) {
                foundCurrent = true;
            }
        }
        return null;
    }

    /**
     * Ottiene il nome della prossima divisione
     * @param player Il giocatore
     * @return Il nome della prossima divisione o "Massimo" se non esiste
     */
    public String getNextDivisionName(Player player) {
        if (tierManager == null) {
            return "Massimo";
        }

        int currentLevel = divisionManager.getPlayerLevel(player);
        DivisionTier currentTier = tierManager.getTierByLevel(currentLevel);
        DivisionTier nextTier = getNextTier(currentTier);
        
        if (nextTier == null) {
            return "Massimo";
        }
        
        return nextTier.getName();
    }
}
