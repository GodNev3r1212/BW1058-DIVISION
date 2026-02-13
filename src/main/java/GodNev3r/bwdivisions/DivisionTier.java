package GodNev3r.bwdivisions;

/**
 * Classe che rappresenta un grado di progressione nel sistema Divisions
 * Ora configurabile tramite config.yml
 */
public class DivisionTier {
    private final String name;
    private final String color;
    private final int minLevel;
    private final int maxLevel;

    public DivisionTier(String name, String color, int minLevel, int maxLevel) {
        this.name = name;
        this.color = color;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    public String getName() {
        return name;
    }

    public String getColor() {
        return color;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Verifica se un livello è compreso in questo tier
     * @param level Il livello da verificare
     * @return true se il livello è compreso in questo tier
     */
    public boolean containsLevel(int level) {
        return level >= minLevel && level <= maxLevel;
    }
}
