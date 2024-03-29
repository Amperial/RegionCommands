package net.pl3x.regionperms;

import java.io.IOException;
import java.util.logging.Level;

import net.pl3x.regionperms.flags.GivePermFlag;
import net.pl3x.regionperms.flags.RemovePermFlag;
import net.pl3x.regionperms.listeners.PlayerListener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

public class RegionPerms extends JavaPlugin {
	private PluginManager pm = Bukkit.getPluginManager();
	
	public void onEnable() {
		saveDefaultConfig();
		
		if (!pm.isPluginEnabled("WorldGuard")) {
			log("&4ERROR! WorldGuard is required for this plugin to function!");
			pm.disablePlugin(this);
			return;
		}
		
		pm.registerEvents(new PlayerListener(this), this);
		
		GivePermFlag.injectHax();
		RemovePermFlag.injectHax();
		
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			log("&4[ERROR] &rFailed to start Metrics: " + e.getMessage());
		}
		
		log(getName() + " v" + getDescription().getVersion() + " by BillyGalbreath enabled!");
	}
	
	public void onDisable() {
		log(getName() + " Disabled.");
	}
	
	public void log (Object obj) {
		if (getConfig().getBoolean("color-logs", true)) {
			getServer().getConsoleSender().sendMessage(colorize("&3[&d" +  getName() + "&3]&r " + obj));
		} else {
			Bukkit.getLogger().log(Level.INFO, "[" + getName() + "] " + ((String) obj).replaceAll("(?)\u00a7([a-f0-9k-or])", ""));
		}
	}
	
	public void debug(Object obj) {
		if (getConfig().getBoolean("debug-mode", false))
			log(obj);
	}
	
	public String colorize(String str) {
		return str.replaceAll("(?i)&([a-f0-9k-or])", "\u00a7$1");
	}
	
	public WorldGuardPlugin getWorldGuard() {
		Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
		if (plugin == null || !(plugin instanceof WorldGuardPlugin))
			return null;
		return (WorldGuardPlugin) plugin;
	}
}
