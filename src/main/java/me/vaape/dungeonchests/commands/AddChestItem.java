package me.vaape.dungeonchests.commands;
import me.vaape.dungeonchests.DungeonChests;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AddChestItem implements CommandExecutor {

    public DungeonChests plugin;
    public FileConfiguration config;

    public AddChestItem(DungeonChests passedPlugin) {
        this.plugin = passedPlugin;
        this.config = passedPlugin.getConfig();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        //addchestitem [item reference name]

        if(!(sender instanceof Player player)){
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used in game.");
            return true;
        }

        if (!player.hasPermission("dungeonchests.addchestitem")) {player.sendMessage(ChatColor.RED + "You do not have permission to do this."); return false; }

        ItemStack hand = player.getInventory().getItemInMainHand();
        String name = buildName(args);
        config.set("items." + name, hand);
        plugin.saveConfig();

        player.sendMessage(ChatColor.GREEN + "Added " + hand.getI18NDisplayName() + " as " + name + ".");
        return true;
    }

    public String buildName(String[] args) {
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            nameBuilder.append(args[i] + " ");
        }
        return StringUtils.chop(nameBuilder.toString());
    }
}
