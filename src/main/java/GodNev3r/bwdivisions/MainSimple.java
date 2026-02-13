package GodNev3r.bwdivisions;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * Versione semplificata del plugin che compilia senza dipendenze esterne
 */
public class MainSimple extends JavaPlugin {
    
    @Override
    public void onEnable() {
        getLogger().info("BW-Divisions (Simple) abilitato!");
        
        // Registra il comando divisione
        getCommand("divisione").setExecutor(new DivisioneCommandSimple());
        getLogger().info("Comando /divisione registrato!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("BW-Divisions (Simple) disabilitato!");
    }
    
    /**
     * Comando semplificato per le divisioni
     */
    private class DivisioneCommandSimple implements CommandExecutor {
        
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
                    if (!player.hasPermission("bwdiv.admin")) {
                        player.sendMessage("§cNon hai il permesso per eseguire questo comando!");
                        return true;
                    }
                    if (args.length < 3) {
                        player.sendMessage("§cUso corretto: /divisione give <giocatore> <divisione>");
                        return true;
                    }
                    giveDivision(player, args[1], args[2]);
                    break;

                case "top":
                    if (!player.hasPermission("bwdiv.top")) {
                        player.sendMessage("§cNon hai il permesso per eseguire questo comando!");
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
            Player target = Bukkit.getPlayer(targetName);
            if (target == null) {
                sender.sendMessage("§cGiocatore non trovato: " + targetName);
                return;
            }

            // Simula l'impostazione della divisione (in realtà non fa nulla senza BedWars API)
            sender.sendMessage("§aHai impostato la divisione §e" + divisionName + " §aper il giocatore §e" + target.getName());
            target.sendMessage("§aLa tua divisione è stata impostata a §e" + divisionName);
            getLogger().info("Divisione " + divisionName + " data a " + target.getName() + " da " + sender.getName());
        }

        private void openTopGUI(Player player) {
            // Crea una GUI semplice con divisioni fittizie
            Inventory gui = Bukkit.createInventory(null, 27, "§8§lTop Divisioni");
            
            // Divisioni fittizie per demo
            String[] divisions = {"Diamante", "Oro", "Argento", "Bronzo", "Ferro"};
            String[] colors = {"§b", "§6", "§7", "§c", "§f"};
            
            for (int i = 0; i < Math.min(divisions.length, 5); i++) {
                ItemStack item = createPlayerHead(player, divisions[i], colors[i], i + 1);
                gui.setItem(i + 10, item);
            }
            
            // Bordo decorativo
            ItemStack border = new ItemStack(org.bukkit.Material.STAINED_GLASS_PANE, 1, (short) 15);
            for (int i = 0; i < 27; i++) {
                if (gui.getItem(i) == null) {
                    gui.setItem(i, border);
                }
            }
            
            player.openInventory(gui);
            player.sendMessage("§aGUI delle top divisioni aperta!");
        }

        private ItemStack createPlayerHead(Player player, String divisionName, String color, int rank) {
            ItemStack head = new ItemStack(org.bukkit.Material.SKULL_ITEM);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            
            if (meta != null) {
                // Per 1.8.9 usa solo setOwner()
                meta.setOwner(player.getName());
                
                String displayName = color + "Top " + rank + " - " + divisionName;
                meta.setDisplayName(displayName);
                
                List<String> lore = new ArrayList<>();
                lore.add("§7Divisione: " + color + divisionName);
                lore.add("§7Posizione: #" + rank);
                lore.add("§7Giocatore: " + player.getName());
                lore.add("");
                lore.add("§eClicca per info!");
                
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            
            return head;
        }

        private void showHelp(Player player) {
            player.sendMessage("§8§m----------§r §6§lComandi Divisioni §8§m----------");
            player.sendMessage("§e/divisione give <giocatore> <divisione> §7- Imposta una divisione (admin)");
            player.sendMessage("§e/divisione top §7- Mostra le top divisioni");
            player.sendMessage("§e/divisione help §7- Mostra questo aiuto");
            player.sendMessage("§8§m----------------------------------------");
            player.sendMessage("§7Note: Questo è una versione demo senza BedWars API");
        }
    }
}
