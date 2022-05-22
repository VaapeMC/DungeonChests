package me.vaape.refer;

import me.vaape.rewards.Rewards;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Refer extends JavaPlugin implements Listener {
    public static Refer plugin;
    private static Permission perms = null;

    @Override
    public void onEnable() {
        plugin = this; saveDefaultConfig(); getLogger().info(ChatColor.GREEN + "Refer has been enabled!");
        getServer().getPluginManager().registerEvents(this, this); setupPermissions();
    }

    @Override
    public void onDisable() {
        plugin = null;
    }

    //refer
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (label.equalsIgnoreCase("refer")) {
                if (args.length > 0) {
                    UUID referredUUID = Bukkit.getOfflinePlayer(args[0]).getUniqueId();
                    UUID playerUUID = player.getUniqueId();

                    List<String> referredUUIDs = getConfig().getStringList("referals." + playerUUID + ".players");
                    //Get list of UUIDs already referred

                    if (referredUUIDs.contains(referredUUID.toString())) {
                        player.sendMessage(ChatColor.RED + "You have already referred this player."); return false;
                    }


                    //Remove UUID from referees in the config
                    ConfigurationSection section = getConfig().getConfigurationSection("referals");
                    if (section != null) {
                        Set<String> referees = section.getKeys(false); //List of people who have referred someone

                        for (String refereeUUID : referees) {

                            List<String> configReferredUUIDs = getConfig().getStringList("referals." + refereeUUID +
                                                                                                 ".players"); //List
                            // of referred people for each referee
                            List<String> configReferredUUIDsCopy = new ArrayList<String>(configReferredUUIDs);

                            for (String configReferredUUID : configReferredUUIDsCopy) {

                                if (configReferredUUID.equals(referredUUID.toString())) { //If newly referred player
                                    // is in the list of referred UUIDs

                                    configReferredUUIDs.remove(configReferredUUID);
                                    getConfig().set("referals." + refereeUUID + ".players", configReferredUUIDs);
                                    saveConfig();

                                }
                            }
                        }
                    }

                    referredUUIDs.add(referredUUID.toString()); //Add the new UUID
                    getConfig().set("referals." + playerUUID + ".players", referredUUIDs); //Set the new list in the
                    // config
                    saveConfig();
                    player.sendMessage(ChatColor.BLUE + "You have successfully referred " + args[0] + ". If they " +
                                               "join" + " you will both receive Iron rank.");
                } else {
                    player.sendMessage(ChatColor.RED + "Wrong usage, try /refer [username]");
                }
            }
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player.");
        } return true;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer(); if (player.hasPlayedBefore()) {
            return;
        } ConfigurationSection section = getConfig().getConfigurationSection("referals"); if (section == null) {
            return;
        } Set<String> referees = section.getKeys(false); //List of people who
        // have referred someone

        boolean referRewardGiven = false; //Is set to true once the first referee found in the config is rewarded (so
        // it doesn't reward more than 1 referee)

        for (String refereeUUID : referees) {

            List<String> referredUUIDs = getConfig().getStringList("referals." + refereeUUID + ".players"); //List of
            // referred people for each referee
            List<String> referredUUIDsCopy = new ArrayList<String>(referredUUIDs);

            for (String referredUUID : referredUUIDsCopy) {
                if (referredUUID.equals(Bukkit.getOfflinePlayer(player.getName()).getUniqueId().toString())) { //If
                    // new player's UUID is in the list of referred UUIDs

                    referredUUIDs.remove(referredUUID);
                    getConfig().set("referals." + refereeUUID + ".players", referredUUIDs); saveConfig();

                    if (referRewardGiven) {
                        continue;
                    }

                    int successfulReferrals = getConfig().getInt("referals." + refereeUUID + ".successful referrals");
                    getConfig().set("referals." + refereeUUID + ".successful referrals", successfulReferrals + 1);
                    saveConfig();

                    if (successfulReferrals == 0) { //0 successful referrals before this
                        new BukkitRunnable() {

                            @Override
                            public void run() {
                                if (isPlayerInGroup(UUID.fromString(refereeUUID), "default")) {
                                    //Give referee Iron
                                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                                                       "lp " + "user " + Bukkit.getOfflinePlayer(UUID.fromString(refereeUUID)).getName() + " parent set iron");
                                }
                            }
                        }.runTask(plugin);
                    } if (successfulReferrals < 5) {
                        Rewards.plugin.giveReward("degg", Bukkit.getOfflinePlayer(UUID.fromString(refereeUUID)), true);
                    }

                    //Make give referred player Iron
                    Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(),
                                                       "lp user " + Bukkit.getOfflinePlayer(referredUUID).getName() + " parent set iron");
                    Bukkit.getServer().broadcastMessage(ChatColor.of("#18C2FF") + "" + ChatColor.BOLD + "[Referals] " + ChatColor.BLUE + ChatColor.ITALIC + Bukkit.getOfflinePlayer(UUID.fromString(referredUUID)).getName() + ChatColor.BLUE + " was referred by " + ChatColor.BLUE + ChatColor.ITALIC + Bukkit.getOfflinePlayer(UUID.fromString(refereeUUID)).getName() + ChatColor.BLUE + " using /refer and both players have received " + ChatColor.GRAY + "Iron " + ChatColor.BLUE + "rank.");

                }
            }
        }
    }

    private void setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        assert rsp != null; perms = rsp.getProvider();
    }

    public static Permission getPermissions() {
        return perms;
    }

    public boolean isPlayerInGroup(UUID uuid, String group) {
        return getPermissions().playerHas(null, Bukkit.getOfflinePlayer(uuid), "group." + group);
    }
}
