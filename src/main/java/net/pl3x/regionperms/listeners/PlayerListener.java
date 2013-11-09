package net.pl3x.regionperms.listeners;

import java.util.HashMap;

import net.pl3x.regionperms.RegionPerms;
import net.pl3x.regionperms.flags.GivePermFlag;
import net.pl3x.regionperms.flags.RemovePermFlag;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class PlayerListener implements Listener {
	private RegionPerms plugin;
	private HashMap<String,String> removePermCache = new HashMap<String,String>();
	
	public PlayerListener(RegionPerms plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerMove(PlayerMoveEvent event) {
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
				event.getFrom().getBlockY() == event.getTo().getBlockY() &&
				event.getFrom().getBlockZ() == event.getTo().getBlockZ())
			return;
		Player player = event.getPlayer();
		ApplicableRegionSet set = getApplicableRegions(event.getTo());
		String givePerm = GivePermFlag.givePermFlag(set);
		String removePerm = RemovePermFlag.removePermFlag(set);
		if (removePerm != null) {
			if (!removePermCache.containsKey(player.getName())) {
				plugin.debug("Cache Put: " + player.getName() + " " + removePerm);
				removePermCache.put(player.getName(), removePerm);
			} else {
				if (!removePermCache.get(player.getName()).equals(removePerm)) {
					plugin.debug("Cache Put: " + player.getName() + " " + removePerm);
					removePermCache.put(player.getName(), removePerm);
				}
			}
		} else {
			removePerm(player, event.getFrom());
		}
		if (givePerm == null || player.hasPermission(givePerm))
			return;
		String command = plugin.getConfig().getString("give-perm-command");
		if (command == null || command.equals("")) {
			plugin.debug("Give permission command not found in config!");
			return;
		}
		String regionName = "(unknown)";
		for (ProtectedRegion region : set) {
			if (GivePermFlag.givePermFlag(region) == null)
				continue;
			regionName = region.getId();
			break;
		}
		
		command = parseVars(command, player, givePerm, regionName);
		command = command.replaceAll("(?i)\\{node\\}", givePerm);
		
		plugin.debug("Executing command: &e/" + command);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		
		if (plugin.getConfig().getBoolean("notify-player", true)) {
			String message = plugin.getConfig().getString("notify-give-message", "&dYour permissions have been updated.");
			message = parseVars(message, player, givePerm, regionName);
			player.sendMessage(plugin.colorize(message));
		}
	}
	
	private void removePerm(Player player, Location from) {
		if (!removePermCache.containsKey(player.getName()))
			return;
		String removePerm = removePermCache.get(player.getName());
		if (!player.hasPermission(removePerm))
			return;
		plugin.debug("Cache Remove: " + player.getName() + " " + removePerm);
		String command = plugin.getConfig().getString("remove-perm-command");
		if (command == null || command.equals("")) {
			plugin.debug("Remove permission command not found in config!");
			return;
		}
		ApplicableRegionSet set = getApplicableRegions(from);
		String regionName = "(unknown)";
		for (ProtectedRegion region : set) {
			if (RemovePermFlag.removePermFlag(region) == null)
				continue;
			regionName = region.getId();
			break;
		}
		command = parseVars(command, player, removePerm, regionName);
		command = command.replaceAll("(?i)\\{node\\}", removePerm);
		
		plugin.debug("Executing command: &e/" + command);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		
		if (plugin.getConfig().getBoolean("notify-player", true)) {
			String message = plugin.getConfig().getString("notify-remove-message", "&dYour permissions have been updated.");
			message = parseVars(message, player, removePerm, regionName);
			player.sendMessage(plugin.colorize(message));
		}
		
		removePermCache.remove(player.getName());
	}
	
	private String parseVars(String string, Player player, String perm, String region) {
		string = string.replaceAll("(?i)\\{user\\}", player.getName());
		string = string.replaceAll("(?i)\\{dispname\\}", player.getDisplayName());
		string = string.replaceAll("(?i)\\{node\\}", perm);
		string = string.replaceAll("(?i)\\{world\\}", player.getWorld().getName());
		string = string.replaceAll("(?i)\\{region\\}", region);
		return string;
	}
	
	private ApplicableRegionSet getApplicableRegions(Location location) {
		return plugin.getWorldGuard().getGlobalRegionManager().get(location.getWorld()).getApplicableRegions(BukkitUtil.toVector(location));
	}
}
