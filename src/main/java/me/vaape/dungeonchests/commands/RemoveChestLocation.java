package me.vaape.dungeonchests.commands;
import me.vaape.dungeonchests.DungeonChests;
import me.vaape.dungeonchests.listeners.InteractListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class RemoveChestLocation implements CommandExecutor {

    public DungeonChests plugin;
    public FileConfiguration config;
    public InteractListener interactListener;

    public RemoveChestLocation(DungeonChests passedPlugin) {
        this.plugin = passedPlugin;
        this.config = passedPlugin.getConfig();
        this.interactListener = passedPlugin.getInteractListener();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //removechestlocation

        if(!(sender instanceof Player player)){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED +"This command can only be used in game.");
            return true;
        }

        if (!player.hasPermission("dungeonchests.removechestlocation")) {player.sendMessage(ChatColor.RED + "You do not have permission to do this."); return false; }

        interactListener.mode = "remove";
        interactListener.playerEditingLocation = player;

        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Right click a chest to remove chest from the config.");

        return true;
    }

    public void removeChestLocation(List<Integer> coords, Player playerRemoving) {
        String configPath = getChestConfigPath(coords);
        if (configPath == null) {
            playerRemoving.sendMessage(ChatColor.RED + "No chest exists at these coords");
            return;
        }
        config.set(configPath, null);
        plugin.saveConfig();
        playerRemoving.sendMessage(ChatColor.GREEN + "Removed chest with config path: " + configPath);

    }

    //Returns the path in the config to the chest with given coords - returns null if no chest exists
    public String getChestConfigPath(List<Integer> coords) {
        Set<String> dungeonNames = config.getConfigurationSection("dungeons").getKeys(false);
        for (String dungeonName : dungeonNames) {
            Set<String> levels = config.getConfigurationSection("dungeons." + dungeonName).getKeys(false);
            for (String level : levels) {
                Set<String> chests = config.getConfigurationSection("dungeons." + dungeonName + "." + level + ".chests").getKeys(false);
                for (String chest : chests) {
                    int x = config.getInt("dungeons." + dungeonName + "." + level + ".chests." + chest + ".x");
                    int y = config.getInt("dungeons." + dungeonName + "." + level + ".chests." + chest + ".y");
                    int z = config.getInt("dungeons." + dungeonName + "." + level + ".chests." + chest + ".z");
                    if (coords.get(0) == x && coords.get(1) == y && coords.get(2) == z) {
                        return "dungeons." + dungeonName + "." + level + ".chests." + chest;
                    }
                }
            }
        }
        return null;
    }
}
