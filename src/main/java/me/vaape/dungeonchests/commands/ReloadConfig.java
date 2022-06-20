package me.vaape.dungeonchests.commands;
import me.vaape.dungeonchests.DungeonChests;
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

public class ReloadConfig implements CommandExecutor {

    public DungeonChests plugin;
    public FileConfiguration config;

    public ReloadConfig(DungeonChests passedPlugin) {
        this.plugin = passedPlugin;
        this.config = passedPlugin.getConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!sender.hasPermission("dungeonchests.reload")) {sender.sendMessage(ChatColor.RED + "You do not have permission to do this."); return false; }

        plugin.reloadAllConfigs();

        sender.sendMessage(ChatColor.GREEN + "DungeonChests has been reloaded.");

        return true;
    }
}
