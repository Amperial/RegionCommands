package net.pl3x.regionperms.listeners;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.pl3x.regionperms.RegionPerms;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StringFlag;

public class PlayerListener implements Listener {
	private RegionPerms plugin;
	private WorldGuardPlugin worldGuard;
	
	public PlayerListener(RegionPerms plugin, WorldGuardPlugin wg) {
		this.plugin = plugin;
		this.worldGuard = wg;
		GivePermFlag.injectHax();
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEnterRegion(PlayerMoveEvent event) {
		if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
				event.getFrom().getBlockY() == event.getTo().getBlockY() &&
				event.getFrom().getBlockZ() == event.getTo().getBlockZ())
			return;
		Player player = event.getPlayer();
		ApplicableRegionSet set = getApplicableRegions(event.getTo());
		String perm = GivePermFlag.givePermFlag(set);
		if (perm == null)
			return;
		if (player.hasPermission(perm))
			return;
		String command = plugin.getConfig().getString("give-perm-command");
		if (command == null || command.equals("")) {
			plugin.debug("Permission command not found in config!");
			return;
		}
		command = command.replaceAll("(?i)\\{user\\}", player.getName());
		command = command.replaceAll("(?i)\\{node\\}", perm);
		
		plugin.debug("Executing command: &e/" + command);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
		
		if (plugin.getConfig().getBoolean("notify-player", true))
			player.sendMessage(plugin.colorize(plugin.getConfig().getString("notify-message", "&dYou can now use the warp to this area.")));
	}
	
	private ApplicableRegionSet getApplicableRegions(Location location) {
		return worldGuard.getGlobalRegionManager().get(location.getWorld()).getApplicableRegions(BukkitUtil.toVector(location));
	}
	
	public static class GivePermFlag extends StringFlag {
		public static GivePermFlag flag = new GivePermFlag();
		
		public GivePermFlag() {
			super("give-perm-node");
		}
		
		public static String givePermFlag(ApplicableRegionSet set) {
			return set.getFlag(flag);
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private static List elements() {
			List elements = new ArrayList(Arrays.asList(DefaultFlag.getFlags()));
			elements.add(flag);
			return elements;
		}
		
		@SuppressWarnings("rawtypes")
		public static void injectHax() {
			try {
				Field field = DefaultFlag.class.getDeclaredField("flagsList");
				Field modifiersField = Field.class.getDeclaredField("modifiers");
				modifiersField.setAccessible(true);
				modifiersField.setInt(field, field.getModifiers() & 0xFFFFFFEF);
				field.setAccessible(true);
				List elements = elements();
				Flag[] list = new Flag[elements.size()];
				for (int i = 0; i < elements.size(); i++) {
					list[i] = ((Flag) elements.get(i));
				}
				field.set(null, list);
				Field grm = WorldGuardPlugin.class.getDeclaredField("globalRegionManager");
				grm.setAccessible(true);
				GlobalRegionManager globalRegionManager = (GlobalRegionManager) grm.get(RegionPerms.getBukkitServer().getPluginManager().getPlugin("WorldGuard"));
				globalRegionManager.preload();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
