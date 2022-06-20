package me.vaape.dungeonchests.listeners;

import me.vaape.dungeonchests.DungeonChests;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import java.util.ArrayList;
import java.util.List;


public class InteractListener implements Listener {

    public DungeonChests plugin;
    public FileConfiguration config;

    public InteractListener(DungeonChests passedPlugin) {
        this.plugin = passedPlugin;
        this.config = passedPlugin.getConfig();
    }

    public String dungeon = null;
    public Integer level = null;
    public Player playerEditingLocation = null;
    public String mode = null; //"add" or "remove"
    public boolean alwaysSpawn;

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (playerEditingLocation == null) return;
        if (event.getHand() == EquipmentSlot.OFF_HAND) return;

        Player player = event.getPlayer();
        if (player != playerEditingLocation) return;

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block.getType() != Material.CHEST) {
            player.sendMessage(ChatColor.RED + "You must select a chest block.");
            return;
        }
        Chest chest = (Chest) block.getBlockData();

        List<Integer> chestCoords = new ArrayList<>();
        chestCoords.add(block.getX());
        chestCoords.add(block.getY());
        chestCoords.add(block.getZ());
        BlockFace facing = chest.getFacing();
        player.sendMessage(ChatColor.YELLOW + "Selected Coords are: X:" + chestCoords.get(0) + " Y:" + chestCoords.get(1) + " Z:" + chestCoords.get(2));

        if (mode.equals("add")){

            player.sendMessage(ChatColor.YELLOW + "Adding chest to " + dungeon + "...");
            plugin.getAddChestLocation().addChestLocation(dungeon, level, chestCoords, facing, alwaysSpawn);
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "New " + dungeon + " chest added at X:" + chestCoords.get(0) + " Y:" + chestCoords.get(1) + " Z:" + chestCoords.get(2));
            resetVariables();

        }
        else if (mode.equals("remove")){

            player.sendMessage(ChatColor.YELLOW + "Attempting to remove chest...");
            plugin.getRemoveChestLocation().removeChestLocation(chestCoords, player);
            resetVariables();
        }
    }

    public void resetVariables() {
        dungeon = null;
        level = null;
        playerEditingLocation = null;
        mode = null;
        alwaysSpawn = false;
    }
}
