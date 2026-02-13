package GodNev3r.bwdivisions.command;

import GodNev3r.bwdivisions.Main;
import GodNev3r.bwdivisions.config.ConfigManager;
import GodNev3r.bwdivisions.gui.DivisionGUI;
import GodNev3r.bwdivisions.stats.StatisticsManager;
import GodNev3r.bwdivisions.stats.PlayerStatistics;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gestisce i comandi del plugin
 */
public class DivisionCommand implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final ConfigManager configManager;
    private final DivisionGUI gui;
    private final StatisticsManager statisticsManager;

    public DivisionCommand(Main plugin, ConfigManager configManager, DivisionGUI gui,
                          StatisticsManager statisticsManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.gui = gui;
        this.statisticsManager = statisticsManager;
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
            case "stats":
            case "statistiche":
                if (!player.hasPermission(configManager.getConfig().getString("commands.permissions.stats", "bwdiv.stats"))) {
                    player.sendMessage(configManager.getConfig().getString("messages.no-permission",
                        "§cNon hai il permesso per eseguire questo comando!"));
                    return true;
                }
                showStats(player);
                break;

            case "gui":
            case "menu":
                if (!player.hasPermission(configManager.getConfig().getString("commands.permissions.gui", "bwdiv.gui"))) {
                    player.sendMessage(configManager.getConfig().getString("messages.no-permission",
                        "§cNon hai il permesso per eseguire questo comando!"));
                    return true;
                }
                gui.openGUI(player);
                break;

            case "leaderboard":
            case "top":
            case "lb":
                if (!player.hasPermission(configManager.getConfig().getString("commands.permissions.leaderboard", "bwdiv.leaderboard"))) {
                    player.sendMessage(configManager.getConfig().getString("messages.no-permission",
                        "§cNon hai il permesso per eseguire questo comando!"));
                    return true;
                }
                showLeaderboard(player);
                break;

            case "reload":
                if (!player.hasPermission(configManager.getConfig().getString("commands.permissions.reload", "bwdiv.reload"))) {
                    player.sendMessage(configManager.getConfig().getString("messages.no-permission",
                        "§cNon hai il permesso per eseguire questo comando!"));
                    return true;
                }
                plugin.reload();
                player.sendMessage("§aConfigurazione ricaricata!");
                break;

            case "help":
            default:
                showHelp(player);
                break;
        }

        return true;
    }

    /**
     * Mostra l'help
     */
    private void showHelp(Player player) {
        String header = configManager.getConfig().getString("messages.help-header",
            "§8§m----------§r §6§lBW-Divisions Help §8§m----------");
        player.sendMessage(header);
        
        List<String> helpCommands = configManager.getConfig().getStringList("messages.help-commands");
        if (helpCommands.isEmpty()) {
            helpCommands = Arrays.asList(
                "§7/bwdiv stats §8- §fVisualizza le tue statistiche",
                "§7/bwdiv gui §8- §fApri la GUI delle divisioni",
                "§7/bwdiv leaderboard §8- §fVisualizza la classifica",
                "§7/bwdiv reload §8- §fRicarica la configurazione (admin)"
            );
        }
        
        for (String line : helpCommands) {
            player.sendMessage(line);
        }
    }

    /**
     * Mostra le statistiche del giocatore
     */
    private void showStats(Player player) {
        if (statisticsManager == null) {
            player.sendMessage("§cLe statistiche non sono disponibili!");
            return;
        }

        String header = configManager.getConfig().getString("messages.stats-header",
            "§8§m----------§r §6§lStatistiche Divisioni §8§m----------");
        player.sendMessage(header);

        PlayerStatistics stats = statisticsManager.getStatistics(player);
        String currentDivision = plugin.getDivisionManager().getDivisionName(player);
        int currentLevel = plugin.getDivisionManager().getPlayerLevel(player);

        player.sendMessage("§7Divisione attuale: §f" + currentDivision);
        player.sendMessage("§7Livello attuale: §f" + currentLevel);
        player.sendMessage("");
        player.sendMessage("§7Vittorie: §f" + stats.getWins());
        player.sendMessage("§7Sconfitte: §f" + stats.getLosses());
        player.sendMessage("§7Win Rate: §f" + String.format("%.1f%%", stats.getWinRate()));
        player.sendMessage("§7Winstreak attuale: §f" + stats.getWinstreak());
        player.sendMessage("§7Miglior winstreak: §f" + stats.getBestWinstreak());
        
        if (stats.getBestDivision() != null) {
            player.sendMessage("§7Migliore divisione raggiunta: §f" + stats.getBestDivision());
        }
        
        if (configManager.getConfig().getBoolean("statistics.track-playtime", true)) {
            player.sendMessage("§7Tempo totale giocato: §f" + stats.getFormattedPlaytime());
        }
    }

    /**
     * Mostra il leaderboard
     */
    private void showLeaderboard(Player player) {
        // Implementazione semplificata - può essere migliorata
        player.sendMessage("§8§m----------§r §6§lTop Giocatori §8§m----------");
        player.sendMessage("§7Il leaderboard sarà implementato nella prossima versione!");
        // TODO: Implementare il leaderboard completo
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            completions.addAll(Arrays.asList("stats", "gui", "leaderboard", "help", "reload"));
        }
        
        return completions;
    }
}
