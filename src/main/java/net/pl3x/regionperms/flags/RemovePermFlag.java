package net.pl3x.regionperms.flags;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.GlobalRegionManager;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class RemovePermFlag extends StringFlag {
	public static RemovePermFlag flag = new RemovePermFlag();
	
	public RemovePermFlag() {
		super("remove-perm-node");
	}
	
	public static String removePermFlag(ApplicableRegionSet set) {
		return set.getFlag(flag);
	}
	
	public static String removePermFlag(ProtectedRegion region) {
		return region.getFlag(flag);
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
			GlobalRegionManager globalRegionManager = (GlobalRegionManager) grm.get(Bukkit.getPluginManager().getPlugin("WorldGuard"));
			globalRegionManager.preload();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
