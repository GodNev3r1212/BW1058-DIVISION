package GodNev3r.bwdivisions.economy;

import GodNev3r.bwdivisions.DivisionManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Gestisce l'economia del plugin (monete/coins per le divisioni)
 */
public class EconomyManager {
    private final JavaPlugin plugin;
    private final DivisionManager divisionManager;
    private final Map<UUID, Double> playerBalances;
    private File dataFile;
    private FileConfiguration dataConfig;

    public EconomyManager(JavaPlugin plugin, DivisionManager divisionManager) {
        this.plugin = plugin;
        this.divisionManager = divisionManager;
        this.playerBalances = new HashMap<>();
    }

    /**
     * Inizializza il file di dati (chiamato dopo che DivisionManager è inizializzato)
     */
    public void initialize() {
        loadDataFile();
        loadBalances();
    }

    /**
     * Carica il file di configurazione per i dati dell'economia
     * Salva nella cartella corretta in base al tipo di server
     */
    private void loadDataFile() {
        File addonFolder = getAddonFolder();
        if (addonFolder == null) {
            // Fallback alla cartella del plugin se non riesce a determinare la cartella corretta
            dataFile = new File(plugin.getDataFolder(), "economy.yml");
            plugin.getLogger().warning("Impossibile determinare la cartella addon, uso la cartella del plugin come fallback");
        } else {
            dataFile = new File(addonFolder, "economy.yml");
            plugin.getLogger().info("Cartella addon rilevata: " + addonFolder.getAbsolutePath());
        }
        
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossibile creare il file economy.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    /**
     * Ottiene la cartella addon corretta in base al tipo di server
     * @return La cartella addon o null se non può essere determinata
     */
    private File getAddonFolder() {
        File pluginsFolder = plugin.getDataFolder().getParentFile();
        
        // Verifica se siamo in modalità Proxy
        if (divisionManager != null && divisionManager.isProxyMode()) {
            File proxyFolder = new File(pluginsFolder, "BedWarsProxy");
            if (proxyFolder.exists() && proxyFolder.isDirectory()) {
                File addonFolder = new File(proxyFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        // Verifica se siamo in modalità Arena (1058)
        if (divisionManager != null && divisionManager.isArenaMode()) {
            File arenaFolder = new File(pluginsFolder, "BedWars1058");
            if (arenaFolder.exists() && arenaFolder.isDirectory()) {
                File addonFolder = new File(arenaFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        // Se non è ancora inizializzato, prova a verificare direttamente
        if (Bukkit.getPluginManager().getPlugin("BedWarsProxy") != null) {
            File proxyFolder = new File(pluginsFolder, "BedWarsProxy");
            if (proxyFolder.exists() && proxyFolder.isDirectory()) {
                File addonFolder = new File(proxyFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        if (Bukkit.getPluginManager().getPlugin("BedWars1058") != null) {
            File arenaFolder = new File(pluginsFolder, "BedWars1058");
            if (arenaFolder.exists() && arenaFolder.isDirectory()) {
                File addonFolder = new File(arenaFolder, "addon");
                addonFolder.mkdirs();
                return addonFolder;
            }
        }
        
        return null;
    }

    /**
     * Carica i saldi dei giocatori dal file
     */
    private void loadBalances() {
        if (dataConfig.getConfigurationSection("balances") != null) {
            for (String uuidString : dataConfig.getConfigurationSection("balances").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    double balance = dataConfig.getDouble("balances." + uuidString, 0.0);
                    playerBalances.put(uuid, balance);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("UUID non valido nel file economy.yml: " + uuidString);
                }
            }
        }
    }

    /**
     * Salva i saldi dei giocatori nel file
     */
    public void saveBalances() {
        for (Map.Entry<UUID, Double> entry : playerBalances.entrySet()) {
            dataConfig.set("balances." + entry.getKey().toString(), entry.getValue());
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossibile salvare il file economy.yml: " + e.getMessage());
        }
    }

    /**
     * Ottiene il saldo di un giocatore
     * @param player Il giocatore
     * @return Il saldo del giocatore
     */
    public double getBalance(Player player) {
        return getBalance(player.getUniqueId());
    }

    /**
     * Ottiene il saldo di un giocatore tramite UUID
     * @param uuid L'UUID del giocatore
     * @return Il saldo del giocatore
     */
    public double getBalance(UUID uuid) {
        return playerBalances.getOrDefault(uuid, 0.0);
    }

    /**
     * Imposta il saldo di un giocatore
     * @param player Il giocatore
     * @param amount L'importo da impostare
     */
    public void setBalance(Player player, double amount) {
        setBalance(player.getUniqueId(), amount);
    }

    /**
     * Imposta il saldo di un giocatore tramite UUID
     * @param uuid L'UUID del giocatore
     * @param amount L'importo da impostare
     */
    public void setBalance(UUID uuid, double amount) {
        if (amount < 0) {
            amount = 0;
        }
        playerBalances.put(uuid, amount);
        saveBalances();
    }

    /**
     * Aggiunge monete al saldo di un giocatore
     * @param player Il giocatore
     * @param amount L'importo da aggiungere
     */
    public void addBalance(Player player, double amount) {
        addBalance(player.getUniqueId(), amount);
    }

    /**
     * Aggiunge monete al saldo di un giocatore tramite UUID
     * @param uuid L'UUID del giocatore
     * @param amount L'importo da aggiungere
     */
    public void addBalance(UUID uuid, double amount) {
        double currentBalance = getBalance(uuid);
        setBalance(uuid, currentBalance + amount);
    }

    /**
     * Rimuove monete dal saldo di un giocatore
     * @param player Il giocatore
     * @param amount L'importo da rimuovere
     * @return true se l'operazione è riuscita, false se il saldo è insufficiente
     */
    public boolean removeBalance(Player player, double amount) {
        return removeBalance(player.getUniqueId(), amount);
    }

    /**
     * Rimuove monete dal saldo di un giocatore tramite UUID
     * @param uuid L'UUID del giocatore
     * @param amount L'importo da rimuovere
     * @return true se l'operazione è riuscita, false se il saldo è insufficiente
     */
    public boolean removeBalance(UUID uuid, double amount) {
        double currentBalance = getBalance(uuid);
        if (currentBalance < amount) {
            return false;
        }
        setBalance(uuid, currentBalance - amount);
        return true;
    }

    /**
     * Verifica se un giocatore ha abbastanza monete
     * @param player Il giocatore
     * @param amount L'importo richiesto
     * @return true se il giocatore ha abbastanza monete
     */
    public boolean hasEnough(Player player, double amount) {
        return hasEnough(player.getUniqueId(), amount);
    }

    /**
     * Verifica se un giocatore ha abbastanza monete tramite UUID
     * @param uuid L'UUID del giocatore
     * @param amount L'importo richiesto
     * @return true se il giocatore ha abbastanza monete
     */
    public boolean hasEnough(UUID uuid, double amount) {
        return getBalance(uuid) >= amount;
    }

    /**
     * Formatta il saldo in una stringa leggibile
     * @param amount L'importo da formattare
     * @return La stringa formattata
     */
    public String formatBalance(double amount) {
        if (amount == (long) amount) {
            return String.format("%.0f", amount);
        }
        return String.format("%.2f", amount);
    }

    /**
     * Salva tutti i dati quando il plugin viene disabilitato
     */
    public void onDisable() {
        saveBalances();
    }
}
