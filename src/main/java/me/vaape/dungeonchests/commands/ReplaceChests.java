package me.vaape.dungeonchests.commands;
import me.vaape.dungeonchests.DungeonChests;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ReplaceChests implements CommandExecutor {

    public DungeonChests plugin;
    public FileConfiguration config;

    public ReplaceChests(DungeonChests passedPlugin) {
        this.plugin = passedPlugin;
        this.config = passedPlugin.getConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //refillchests [dungeon] [level]

        if (!sender.hasPermission("dungeonchests.addchestitem")) {sender.sendMessage(ChatColor.RED + "You do not have permission to do this."); return false; }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Invalid usage, try /refillchests [dungeon] [level]");
            return false;
        }
        String dungeon = args[0];
        String level = args[1];

        if (config.getConfigurationSection("dungeons." + dungeon + ".level " + level + ".chests") == null) {
            sender.sendMessage(ChatColor.RED + "No chests exist for " + ChatColor.UNDERLINE + dungeon + ", level " + level);
            return false;
        }

        //Load all chests into 2 HashMaps
        HashMap<Location, String> permanentChestLocations = new HashMap<>(); //Chests that will always spawn
        HashMap<Location, String> randomChestLocations = new HashMap<>(); //Chests with a random chance of spawning

        Set<String> chests = config.getConfigurationSection("dungeons." + dungeon + ".level " + level + ".chests").getKeys(false);
        for (String chest : chests) {
            String path = "dungeons." + dungeon + ".level " + level + ".chests." + chest;
            int x = config.getInt(path + ".x");
            int y = config.getInt(path + ".y");
            int z = config.getInt(path + ".z");
            String facing = config.getString(path + ".facing");
            Location chestLocation = new Location(Bukkit.getWorld("world"), x, y, z);
            chestLocation.getBlock().setType(Material.AIR);
            if (config.getBoolean(path + ".always spawn")) {
                permanentChestLocations.put(chestLocation, facing);
            }
            else {
                randomChestLocations.put(chestLocation, facing);
            }
        }

        int numberOfChestsToSpawn = config.getInt("dungeons." + dungeon + ".level " + level + ".number of chests to spawn");
        int numberOfPermanentChests = permanentChestLocations.size();
        int numberOfRandomChestsToSpawn = numberOfChestsToSpawn - numberOfPermanentChests;
        Set<Location> pickedRandomLocations = getMultipleRandomChestLocations(randomChestLocations.keySet(), numberOfRandomChestsToSpawn);

        List<Chest> spawnedChests = new ArrayList<>();

        for (Location pickedLocation : pickedRandomLocations) {
            pickedLocation.getBlock().setType(Material.CHEST);
            spawnedChests.add(createChest(pickedLocation, BlockFace.valueOf(randomChestLocations.get(pickedLocation))));
        }
        for (Location permanentChestLocation : permanentChestLocations.keySet()) {
            permanentChestLocation.getBlock().setType(Material.CHEST);
            spawnedChests.add(createChest(permanentChestLocation, BlockFace.valueOf(permanentChestLocations.get(permanentChestLocation))));
        }

        plugin.getRefillChests().refillChests(dungeon, Integer.parseInt(level), spawnedChests);
        return true;
    }

    public Chest createChest(Location location, BlockFace direction) {
        location.getBlock().setType(Material.CHEST);
        Chest chest = (Chest) location.getBlock().getState();
        Directional chestData = (Directional) chest.getBlockData();
        chestData.setFacing(direction);
        chest.setBlockData(chestData);
        return chest;
    }

    public Set<Location> getMultipleRandomChestLocations(Set<Location> allRandomChestLocations, int n) {
        Set<Location> locations = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            Location locationToAdd = pickRandomLocation(allRandomChestLocations);
            if (locations.contains(locationToAdd)) continue;
            locations.add(locationToAdd);
            if (locations.size() >= n) break;
        }
        return locations;
    }

    public Location pickRandomLocation(Set<Location> locations) {
        int size = locations.size();
        int item = new Random().nextInt(size);
        int i = 0;
        for(Location pickedLocation : locations)
        {
            if (i == item)
                return pickedLocation;
            i++;
        }
        return null;
    }
}
