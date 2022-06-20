package me.vaape.dungeonchests;

import me.vaape.dungeonchests.commands.*;
import me.vaape.dungeonchests.listeners.InteractListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class DungeonChests extends JavaPlugin implements Listener {
    public DungeonChests plugin;
    public FileConfiguration config = this.getConfig();
    public ReloadConfig reloadConfig;
    public ReplaceChests replaceChests;
    public RefillChests refillChests;
    public AddChestItem addChestItem;
    public AddChestLocation addChestLocation;
    public RemoveChestLocation removeChestLocation;
    public InteractListener interactListener;

    @Override
    public void onEnable() {
        plugin = this;
        loadConfiguration();

        interactListener = new InteractListener(this);
        replaceChests = new ReplaceChests(this);
        refillChests = new RefillChests(this);
        reloadConfig = new ReloadConfig(this);
        addChestItem = new AddChestItem(this);
        addChestLocation = new AddChestLocation(this);
        removeChestLocation = new RemoveChestLocation(this);

        this.getCommand("replacechests").setExecutor(replaceChests);
        this.getCommand("reloaddungeonchests").setExecutor(reloadConfig);
        this.getCommand("addchestitem").setExecutor(addChestItem);
        this.getCommand("addchestlocation").setExecutor(addChestLocation);
        this.getCommand("removechestlocation").setExecutor(removeChestLocation);

        this.getServer().getPluginManager().registerEvents(interactListener, this);

        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "DungeonChests has been enabled!");

    }

    public AddChestItem getAddChestItem() {return addChestItem;}
    public AddChestLocation getAddChestLocation() {return addChestLocation;}
    public RemoveChestLocation getRemoveChestLocation() {return removeChestLocation;}
    public ReplaceChests getReplaceChests() {return replaceChests;}
    public RefillChests getRefillChests() {return refillChests;}
    public InteractListener getInteractListener() {return interactListener;}


    public void loadConfiguration() {
        config = this.getConfig();
        saveConfig();
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    public DungeonChests getPlugin() {
        return plugin;
    }

    public void reloadAllConfigs() {
        reloadConfig();
        config = plugin.getConfig();
        getAddChestItem().config = this.config;
        getAddChestLocation().config = this.config;
        getRemoveChestLocation().config = this.config;
        getInteractListener().config = this.config;
        getReplaceChests().config = this.config;
        getRefillChests().config = this.config;
    }

}
