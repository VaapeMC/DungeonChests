package me.vaape.refer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import me.vaape.rewards.Rewards;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.permission.Permission;

public class Refer extends JavaPlugin implements Listener{
	
	public static Refer plugin;
	private FileConfiguration config = this.getConfig();
	private static Permission perms = null;
	
	public void onEnable() {
		loadConfiguration();
		plugin = this;
		getLogger().info(ChatColor.GREEN + "Refer has been enabled!");
		getServer().getPluginManager().registerEvents(this, this);
		
		setupPermissions();
	}
	
	public void onDisable(){
		plugin = null;
	}
	
	public static Refer getInstance() {
		return plugin;
	}
	
	public void loadConfiguration() {
		final FileConfiguration config = this.getConfig();
		
		config.options().copyDefaults(true);
		saveConfig();
	}
	
	//refer
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (sender instanceof Player) {
			Player player = (Player) sender;
			
			if (label.equalsIgnoreCase("refer")) {
				if (args.length > 0) {
					UUID referredUUID = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
					UUID playerUUID = player.getUniqueId();
					
					List<String> referredUUIDs = config.getStringList("referals." + playerUUID.toString() + ".players"); //Get list of UUIDs already referred
					
					if (referredUUIDs.contains(referredUUID.toString())) {
						player.sendMessage(ChatColor.RED + "You have already referred this player.");
						return false;
					}
					
					
					//Remove UUID from referees in the config
					Set<String> referees = config.getConfigurationSection("referals").getKeys(false); //List of people who have referred someone
					
					for (String refereeUUID : referees) {
						
						List<String> configReferredUUIDs = config.getStringList("referals." + refereeUUID + ".players"); //List of referred people for each referee
						List<String> configReferredUUIDsCopy = new ArrayList<String>(configReferredUUIDs);
						
						for (String configReferredUUID : configReferredUUIDsCopy) {
							
							if (configReferredUUID.equals(referredUUID.toString())) { //If newly referred player is in the list of referred UUIDs
								
								configReferredUUIDs.remove(configReferredUUID);
								config.set("referals." + refereeUUID + ".players", configReferredUUIDs);
								saveConfig();
								
							}
						}
					}
					
					
					referredUUIDs.add(referredUUID.toString()); //Add the new UUID
					config.set("referals." + playerUUID.toString() + ".players", referredUUIDs); //Set the new list in the config
					saveConfig();
					player.sendMessage(ChatColor.BLUE + "You have successfully referred " + args[0] + ". If they join you will both receive Iron rank.");
				}
				else {
					player.sendMessage(ChatColor.RED + "Wrong usage, try /refer [username]");
				}
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "You must be a player.");
		}
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onJoin (PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (player.hasPlayedBefore()) {
			return;
		}
		Set<String> referees = config.getConfigurationSection("referals").getKeys(false); //List of people who have referred someone
		
		boolean referRewardGiven = false; //Is set to true once the first referee found in the config is rewarded (so it doesn't reward more than 1 referee)
		
		for (String refereeUUID : referees) {
			
			List<String> referredUUIDs = config.getStringList("referals." + refereeUUID + ".players"); //List of referred people for each referee
			List<String> referredUUIDsCopy = new ArrayList<String>(referredUUIDs);
			
			for (String referredUUID : referredUUIDsCopy) {
				if (referredUUID.equals(Bukkit.getOfflinePlayer(player.getName()).getUniqueId().toString())) { //If new player's UUID is in the list of referred UUIDs
					
					referredUUIDs.remove(referredUUID);
					config.set("referals." + refereeUUID + ".players", referredUUIDs);
					saveConfig();
					
					if (referRewardGiven) {
						continue;
					}
					
					int successfulReferrals = config.getInt("referals." + refereeUUID + ".successful referrals");
					config.set("referals." + refereeUUID + ".successful referrals", successfulReferrals + 1);
					saveConfig();
					
					if (successfulReferrals == 0) { //0 successful referrals before this
						new BukkitRunnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								if (isPlayerInGroup(UUID.fromString(refereeUUID), "default")) {
									//Give referee Iron
									Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + Bukkit.getOfflinePlayer(UUID.fromString(refereeUUID)).getName() + " parent set iron");
								}
							}
						}.runTaskAsynchronously(plugin);
					}
					if (successfulReferrals < 3) {
						Rewards.plugin.giveReward("degg", Bukkit.getOfflinePlayer(refereeUUID), true);
					}
					
					//Make give referred player Iron
					Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "lp user " + Bukkit.getOfflinePlayer(referredUUID).getName() + " parent set iron");
					Bukkit.getServer().broadcastMessage(ChatColor.of("#18C2FF") + "" + ChatColor.BOLD + "[Referals] " + ChatColor.BLUE + ChatColor.ITALIC + Bukkit.getOfflinePlayer(UUID.fromString(referredUUID)).getName() + ChatColor.BLUE + " was referred by " + ChatColor.BLUE + ChatColor.ITALIC + Bukkit.getOfflinePlayer(UUID.fromString(refereeUUID)).getName() + ChatColor.BLUE + " using /refer and both players have received " + ChatColor.GRAY + "Iron " + ChatColor.BLUE + "rank.");

				}
			}
		}
	}
	
	private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
	
	public static Permission getPermissions() {
        return perms;
    }
	
	public boolean isPlayerInGroup(UUID uuid, String group) {
		return getPermissions().playerHas(null, Bukkit.getOfflinePlayer(uuid), "group." + group);
	}
}
