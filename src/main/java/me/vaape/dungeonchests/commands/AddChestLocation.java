package me.vaape.dungeonchests.commands;
import me.vaape.dungeonchests.DungeonChests;
import me.vaape.dungeonchests.listeners.InteractListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AddChestLocation implements CommandExecutor {

    public DungeonChests plugin;
    public FileConfiguration config;
    public InteractListener interactListener;

    public AddChestLocation(DungeonChests passedPlugin) {
        this.plugin = passedPlugin;
        this.config = passedPlugin.getConfig();
        this.interactListener = passedPlugin.getInteractListener();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //addchestlocation [dungeon] [level]

        if(!(sender instanceof Player player)){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used in game.");
            return false;
        }
        if (!player.hasPermission("dungeonchests.addchest")) {player.sendMessage(ChatColor.RED + "You do not have permission to do this."); return false; }

        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Incorrect usage, try /addchestlocation [dungeon] [level] [alwaysSpawn]");
            return false;
        }

        String dungeon = args[0];
        try {
            Integer.parseInt(args[1]);
        }
        catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid likelihood (integer), try /addchestlocation [dungeon] [level] [alwaysSpawn]");
            return false;
        }
        int level = Integer.parseInt(args[1]);

        boolean alwaysSpawn = false;
        if (args[2].equalsIgnoreCase("true")) {
            alwaysSpawn = true;
        }
        else if (args[2].equalsIgnoreCase("false")) {
        }
        else {
            player.sendMessage(ChatColor.RED + "Invalid alwaysSpawn value (boolean), try /addchestlocation [dungeon] [level] [permanent (true/false)]");
            return false;
        }


        interactListener.dungeon = dungeon;
        interactListener.level = level;
        interactListener.mode = "add";
        interactListener.playerEditingLocation = player;
        interactListener.alwaysSpawn = alwaysSpawn;

        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.ITALIC + "Right click a chest to add this position to the list of " + dungeon + " chests...");

        return true;
    }

    public void addChestLocation(String dungeon, Integer level, List<Integer> coords, BlockFace facing, boolean alwaysSpawn) {
        UUID chestUUID = UUID.randomUUID();
        if (config.getConfigurationSection("dungeons." + dungeon + ".level " + level + ".chests") == null) {
            config.set("dungeons." + dungeon + ".level " + level + ".chests." + chestUUID + ".x", coords.get(0));
            config.set("dungeons." + dungeon + ".level " + level + ".chests." + chestUUID + ".y", coords.get(1));
            config.set("dungeons." + dungeon + ".level " + level + ".chests." + chestUUID + ".z", coords.get(2));
            config.set("dungeons." + dungeon + ".level " + level + ".chests." + chestUUID + ".facing", facing.toString());
            config.set("dungeons." + dungeon + ".level " + level + ".chests." + chestUUID + ".always spawn", alwaysSpawn);
            plugin.saveConfig();
        }
        else {
            config.set("dungeons." +dungeon + ".level " + level + ".chests." + chestUUID + ".x", coords.get(0));
            config.set("dungeons." +dungeon + ".level " + level + ".chests." + chestUUID + ".y", coords.get(1));
            config.set("dungeons." +dungeon + ".level " + level + ".chests." + chestUUID + ".z", coords.get(2));
            config.set("dungeons." +dungeon + ".level " + level + ".chests." + chestUUID + ".facing", facing.toString());
            config.set("dungeons." +dungeon + ".level " + level + ".chests." + chestUUID + ".always spawn", alwaysSpawn);
            plugin.saveConfig();
        }
    }
}
