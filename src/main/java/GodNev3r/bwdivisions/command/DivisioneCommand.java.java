package GodNev3r.bwdivisions.command;

import GodNev3r.bwdivisions.DivisionManager;
import GodNev3r.bwdivisions.DivisionTier;
import GodNev3r.bwdivisions.DivisionTierManager;
import GodNev3r.bwdivisions.Main;
import GodNev3r.bwdivisions.config.ConfigManager;
import GodNev3r.bwdivisions.gui.TopDivisionsGUI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gestisce i comandi /divisione e /d
 */
public class DivisioneCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final ConfigManager configManager;
    private final DivisionManager divisionManager;
    private final TopDivisionsGUI topDivisionsGUI;

    public DivisioneCommand(Main plugin, ConfigManager configManager, DivisionManager divisionManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.divisionManager = divisionManager;
        this.topDivisionsGUI = new TopDivisionsGUI(plugin, divisionManager, configManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                if (!sender.hasPermission(configManager.getConfig().getString("commands.permissions.give", "bwdiv.admin"))) {
                    sender.sendMessage(configManager.getConfig().getString("messages.no-permission",
                        "§cNon hai il permesso per eseguire questo comando!"));
                    return true;
                }
                handleGiveCommand(sender, args);
                break;

            case "top":
            case "leaderboard":
                if (!sender.hasPermission(configManager.getConfig().getString("commands.permissions.top", "bwdiv.top"))) {
                    sender.sendMessage(configManager.getConfig().getString("messages.no-permission",
                        "§cNon hai il permesso per eseguire questo comando!"));
                    return true;
                }
                
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cQuesto comando può essere eseguito solo da un giocatore!");
                    return true;
                }
                
                topDivisionsGUI.openTopGUI((Player) sender);
                break;

            case "help":
            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    /**
     * Gestisce il comando give per assegnare divisioni
     */
    private void handleGiveCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("§cUso corretto: /divisione give <giocatore> <divisione>");
            return;
        }

        String playerName = args[1];
        String divisionName = args[2];

        Player target = Bukkit.getPlayer(playerName);
        if (target == null) {
            sender.sendMessage("§cGiocatore non trovato: " + playerName);
            return;
        }

        DivisionTierManager tierManager = divisionManager.getTierManager();
        if (tierManager == null) {
            sender.sendMessage("§cIl sistema delle divisioni non è disponibile!");
            return;
        }

        // Cerca la divisione richiesta
        DivisionTier targetTier = null;
        for (DivisionTier tier : tierManager.getAllTiers()) {
            if (tier.getName().equalsIgnoreCase(divisionName)) {
                targetTier = tier;
                break;
            }
        }

        if (targetTier == null) {
            sender.sendMessage("§cDivisione non trovata: " + divisionName);
            sender.sendMessage("§7Divisioni disponibili:");
            for (DivisionTier tier : tierManager.getAllTiers()) {
                sender.sendMessage("§7- " + tier.getColor() + tier.getName());
            }
            return;
        }

        // Imposta la divisione al giocatore
        divisionManager.setPlayerDivision(target, targetTier.getName());
        
        sender.sendMessage("§aDivisione " + targetTier.getColor() + targetTier.getName() + 
                          " §aassegnata a " + target.getName() + "!");
        
        target.sendMessage("§aHai ricevuto la divisione " + targetTier.getColor() + targetTier.getName() + " §a!");
    }

    /**
     * Mostra l'help del comando
     */
    private void showHelp(CommandSender sender) {
        String header = configManager.getConfig().getString("messages.divisione-help-header",
            "§8§m----------§r §6§lComandi Divisione §8§m----------");
        sender.sendMessage(header);
        
        sender.sendMessage("§7/divisione give <giocatore> <divisione> §8- §fAssegna una divisione (admin)");
        sender.sendMessage("§7/divisione top §8- §fApri la GUI delle top divisioni");
        sender.sendMessage("§7/divisione help §8- §fMostra questo aiuto");
        
        if (sender.hasPermission("bwdiv.admin")) {
            sender.sendMessage("");
            sender.sendMessage("§eComandi admin:");
            for (DivisionTier tier : divisionManager.getTierManager().getAllTiers()) {
                sender.sendMessage("§7- " + tier.getColor() + tier.getName());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            if (sender.hasPermission("bwdiv.admin")) {
                completions.addAll(Arrays.asList("give", "top", "help"));
            } else {
                completions.addAll(Arrays.asList("top", "help"));
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Tab complete per i giocatori online
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Tab complete per le divisioni
            DivisionTierManager tierManager = divisionManager.getTierManager();
            if (tierManager != null) {
                for (DivisionTier tier : tierManager.getAllTiers()) {
                    completions.add(tier.getName());
                }
            }
        }
        
        return completions;
    }
}
