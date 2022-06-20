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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.util.EnumUtils;

import java.util.*;

public class RefillChests {

    public DungeonChests plugin;
    public FileConfiguration config;

    public RefillChests(DungeonChests passedPlugin) {
        this.plugin = passedPlugin;
        this.config = passedPlugin.getConfig();
    }

    public void refillChests (String dungeon, Integer level, List<Chest> chests) {
        int numberOfChests = chests.size();

        for (Chest chest : chests) {
            Inventory chestInv = chest.getBlockInventory();
            for (int i = 0; i < chestInv.getSize(); i++) { //Loop through each item slot in inventory
                ItemStack generatedItem = pickItem(dungeon, level, numberOfChests);
                chestInv.setItem(i, generatedItem);
            }
        }

    }

    public ItemStack pickItem(String dungeon, Integer level, Integer numberOfChests) {
        int totalSlots = numberOfChests * 27;
        Set<String> items = config.getConfigurationSection("dungeons." + dungeon + ".level " + level + ".items").getKeys(false);

        double total = 1;
        //Count up from 0 with increment = each individual probability, when random < counter choose that item
        double random = Math.random() * total; //Random number between 0-total

        double counter = 0;
        for (String item : items) {
            double numberOfItemsToPut = config.getDouble("dungeons." + dungeon + ".level " + level + ".items." + item);
            double chance = numberOfItemsToPut / ((double) totalSlots);
            counter = counter + chance;

            if (random <= counter) {
                return config.getItemStack("items." + item);
            }
        }
        return null;
    }
}
