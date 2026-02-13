package GodNev3r.bwdivisions.command;

import GodNev3r.bwdivisions.DivisionManagerSimple;
import GodNev3r.bwdivisions.DivisionTierManager;
import GodNev3r.bwdivisions.DivisionTier;
import GodNev3r.bwdivisions.gui.TopDivisionsGUI;
import GodNev3r.bwdivisions.config.ConfigManager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Comando per gestire le divisioni (versione semplificata)
 */
public class DivisioneCommandSimple implements CommandExecutor, TabCompleter {
    private final GodNev3r.bwdivisions.Main plugin;
    private final ConfigManager configManager;
    private final DivisionManagerSimple divisionManager;
    private final TopDivisionsGUI topDivisionsGUI;

    public DivisioneCommandSimple(GodNev3r.bwdivisions.Main plugin, ConfigManager configManager, DivisionManagerSimple divisionManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.divisionManager = divisionManager;
        this.topDivisionsGUI = new TopDivisionsGUI(plugin, configManager, divisionManager);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cQuesto comando può essere eseguito solo da un giocatore!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "give":
                if (!player.hasPermission(configManager.getConfig().getString("commands.permissions.admin", "bwdiv.admin"))) {
                    player.sendMessage(configManager.getConfig().getString("messages.no-permission",
                        "§cNon hai il permesso per eseguire questo comando!"));
                    return true;
                }
                if (args.length < 3) {
                    player.sendMessage("§cUso corretto: /divisione give <giocatore> <divisione>");
                    return true;
                }
                giveDivision(player, args[1], args[2]);
                break;

            case "top":
                if (!player.hasPermission(configManager.getConfig().getString("commands.permissions.top", "bwdiv.top"))) {
                    player.sendMessage(configManager.getConfig().getString("messages.no-permission",
                        "§cNon hai il permesso per eseguire questo comando!"));
                    return true;
                }
                openTopGUI(player);
                break;

            case "help":
            default:
                showHelp(player);
                break;
        }

        return true;
    }

    private void giveDivision(Player sender, String targetName, String divisionName) {
        Player target = plugin.getServer().getPlayer(targetName);
        if (target == null) {
            sender.sendMessage("§cGiocatore non trovato: " + targetName);
            return;
        }

        DivisionTierManager tierManager = divisionManager.getTierManager();
        if (tierManager == null) {
            sender.sendMessage("§cIl sistema delle divisioni non è disponibile!");
            return;
        }

        boolean divisionExists = false;
        for (DivisionTier tier : tierManager.getAllTiers()) {
            if (tier.getName().equalsIgnoreCase(divisionName)) {
                divisionExists = true;
                break;
            }
        }

        if (!divisionExists) {
            sender.sendMessage("§cDivisione non trovata: " + divisionName);
            return;
        }

        divisionManager.setPlayerDivision(target, divisionName);
        sender.sendMessage("§aHai impostato la divisione §e" + divisionName + " §aper il giocatore §e" + target.getName());
        target.sendMessage("§aLa tua divisione è stata impostata a §e" + divisionName);
    }

    private void openTopGUI(Player player) {
        topDivisionsGUI.openGUI(player);
    }

    private void showHelp(Player player) {
        player.sendMessage("§8§m----------§r §6§lComandi Divisioni §8§m----------");
        player.sendMessage("§e/divisione give <giocatore> <divisione> §7- Imposta una divisione (admin)");
        player.sendMessage("§e/divisione top §7- Mostra le top divisioni");
        player.sendMessage("§e/divisione help §7- Mostra questo aiuto");
        player.sendMessage("§8§m----------------------------------------");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("give");
            completions.add("top");
            completions.add("help");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            // Completa nomi dei giocatori online
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("give")) {
            // Completa nomi delle divisioni
            DivisionTierManager tierManager = divisionManager.getTierManager();
            if (tierManager != null) {
                for (DivisionTier tier : tierManager.getAllTiers()) {
                    completions.add(tier.getName());
                }
            }
        }

        // Filtra i risultati che iniziano con l'input corrente
        String current = args[args.length - 1].toLowerCase();
        List<String> filtered = new ArrayList<>();
        for (String completion : completions) {
            if (completion.toLowerCase().startsWith(current)) {
                filtered.add(completion);
            }
        }

        return filtered;
    }
}
