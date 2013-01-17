package net.slipcor.classranks.handlers;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import net.milkbowl.vault.permission.Permission;
import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.managers.ClassManager;
import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.managers.PlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault permissions handler class
 * 
 * @version v0.4.3
 * 
 * @author slipcor
 */

public class HandleVaultPerms extends CRHandler {
	private final ClassRanks plugin;
	private final DebugManager db;
	public static Permission permission = null;

	public HandleVaultPerms(ClassRanks cr) {
		plugin = cr;
		db = new DebugManager(cr);
	}

	@Override
	public boolean isInGroup(String world, String permName, String player) {
		return permission.playerInGroup(world, player, permName);
	}

	/*
	 * Function that tries to setup the permissions system, returns result
	 */
	@Override
	public boolean setupPermissions() {
		// try to load permissions, return result
		RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) {
        	try {
                permission = permissionProvider.getProvider();
                getPermNameByPlayer(Bukkit.getWorlds().get(0).getName(), "slipcor");
        	} catch (Exception e) {
        		permission = null;
        	}
        }
        return (permission != null);
	}

	@Override
	public boolean hasPerms(Player comP, String string, String world) {
		return permission.has(world, comP.getName(), string);
	}

	/*
	 * Add a user to a given class in the given world
	 */
	@Override
	public void classAdd(String world, String player, String cString) {
		
		player = PlayerManager.search(player); // auto-complete playername

		if (world.equalsIgnoreCase("all")) {
			classAddGlobal(player, cString);
			return;
		}
		try {
			permission.playerAddGroup(world, player, cString);
			db.i("added group " + cString + " to player " + player
					+ " in world " + world);
		} catch (Exception e) {
			plugin.log("PermName " + cString + " or user " + player
					+ " not found in world " + world, Level.WARNING);
		}
	}

	/*
	 * Add a user to a given rank in the given world
	 */
	@Override
	public void rankAdd(String world, String player, String rank) {
		player = PlayerManager.search(player); // auto-complete playername
		if (world.equalsIgnoreCase("all")) {
			rankAddGlobal(player, rank);
			return;
		}
		String cString = ClassManager.getClassNameByPermName(rank);
		plugin.getConfig().set("players." + player + "." + cString, rank);
		plugin.getConfig().set("worlds." + world + "." + player, rank);
		plugin.saveConfig();
		db.i("Vault PermName " + rank + ": user " + player + " set " + world);

		try {
			if (permission.playerAddGroup(world, player, rank)) {
				db.i("Vault added rank " + rank + " to player " + player	+ " in world " + world);
			} else {
				db.i("ERROR Vault added rank " + rank + " to player " + player	+ " in world " + world);
			}
		} catch (Exception e) {
			plugin.log("Vault Exception added rank " + rank + " or user " + player + " not found in world " + world, Level.WARNING);
		}
	}

	/*
	 * Remove a user from the class he has in the given world
	 */
	@Override
	public void rankRemove(String world, String player, String cString) {
		player = PlayerManager.search(player); // auto-complete playername

		if (world.equalsIgnoreCase("all")) {
			rankRemoveGlobal(player, cString);
			return;
		}
		plugin.getConfig().set("players." + player, null);
		plugin.getConfig().set("worlds." + world + "." + player, null);
		plugin.saveConfig();

		try {
			permission.playerRemoveGroup(world, player, cString);
			db.i("removed rank " + cString + " from player " + player
					+ " in world " + world);
		} catch (Exception e) {
			plugin.log("PermName " + cString + " or user " + player
					+ " not found in world " + world, Level.WARNING);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getPermNameByPlayer(String world, String player) {
//		ArrayList<String> permGroups = new ArrayList<String>();
		Map<String, Object> groups = null;
		
        String cPerm = "";
        
		if ((world == "") || (world == "all") ) {
			try {
				groups = plugin.getConfig().getConfigurationSection("players." + player).getValues(true);
			} catch (Exception e) {
				db.i("Exception GetPermByPlayer : GetConfigurationSection");
			}
			
			if (groups == null) {
				return "";
			}
			// Set last Value as result
			for (Object group : groups.values()) {
				cPerm = (String) group;
//				permGroups.add((String) group);
			}
			db.i("player has groups: " + groups.toString());
		} else {
			try {
				groups = (Map<String, Object>) plugin.getConfig().getConfigurationSection("worlds." + world);
			} catch (Exception e) {
				db.i("Exception GetPermByPlayer : GetConfigurationSection");
			}
			
			if (groups == null) {
				return "";
			}
			if (groups.containsKey(player)) {
				cPerm = groups.get(player).toString();
				db.i("worlds contains key "+player +" value"+ cPerm);
			} else {
			    db.i("worlds contains NOT key "+player );
			}
			
		}
//		return ClassManager.getLastPermNameByPermGroups(permGroups);
		return cPerm;
	}

	@Override
	public void classAddGlobal(String player, String cString) {
		player = PlayerManager.search(player); // auto-complete playername
		try {
			permission.playerAddGroup(Bukkit.getPlayer(player), cString);
			db.i("added rank " + cString + " to player " + player);
		} catch (Exception e) {
			plugin.log("PermName " + cString + " or user " + player
					+ " not found", Level.WARNING);
		}
	}

	@Override
	public void rankAddGlobal(String player, String rank) {
		player = PlayerManager.search(player); // auto-complete playername
		String cString = ClassManager.getClassNameByPermName(rank);
		plugin.getConfig().set("players." + player + "." + cString, rank);
		db.i("Vault added rank " + rank + " to player " + player + ", no world support");
		plugin.saveConfig();
		try {
			permission.playerAddGroup(Bukkit.getPlayer(player), rank);
			plugin.log("Vault PermName " + rank + ": user " + player , Level.INFO);
			db.i("added rank " + rank + " to player " + player);
		} catch (Exception e) {
			plugin.log("PermName " + rank + " or user " + player
					+ " not found", Level.WARNING);
		}
	}

	@Override
	public void rankRemoveGlobal(String player, String cString) {

		player = PlayerManager.search(player); // auto-complete playername
		plugin.getConfig().set("players." + player, null);
		plugin.saveConfig();
	
		try {
			permission.playerRemoveGroup(Bukkit.getPlayer(player), cString);
			db.i("removed rank " + cString + " from player " + player);
		} catch (Exception e) {
			plugin.log("PermName " + cString + " or user " + player
					+ " not found ", Level.WARNING);
		}
	}

	@Override
	public String getPermNameByPlayerGlobal(String player) {
		player = PlayerManager.search(player); // auto-complete playername
		ArrayList<String> permGroups = new ArrayList<String>();
		
		String[] list = permission.getPlayerGroups(Bukkit.getPlayer(player));
		for (String sRank : list) {
			db.i("checking rank "+sRank);
			if (ClassManager.rankExists(sRank)) {
				permGroups.add(sRank);
			}
		}
		db.i("player has groups: " + permGroups.toString());
		return ClassManager.getLastPermNameByPermGroups(permGroups);
	}

	@Override
	public void removeGroups(Player player) {
		String[] list = permission.getPlayerGroups(player);
		if (list != null) {
			for (String sRank : list) {
				permission.playerRemoveGroup(player, sRank);
				plugin.getConfig().set("players." + player, null);
			}
			plugin.saveConfig();
		}
	}
}
