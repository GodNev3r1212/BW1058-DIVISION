package GodNev3r.bwdivisions;

import java.util.UUID;

/**
 * Memorizza i dati della divisione di un giocatore per rilevare i cambiamenti
 */
public class PlayerDivisionData {
    private final UUID playerUUID;
    private String currentDivision;
    private int currentLevel;

    public PlayerDivisionData(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.currentDivision = null;
        this.currentLevel = 0;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getCurrentDivision() {
        return currentDivision;
    }

    public void setCurrentDivision(String currentDivision) {
        this.currentDivision = currentDivision;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    /**
     * Verifica se la divisione è cambiata
     * @param newDivision La nuova divisione
     * @return true se è cambiata
     */
    public boolean hasDivisionChanged(String newDivision) {
        return currentDivision != null && !currentDivision.equals(newDivision);
    }
}
