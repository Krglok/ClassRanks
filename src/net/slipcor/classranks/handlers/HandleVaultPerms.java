package net.slipcor.classranks.handlers;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import net.milkbowl.vault.permission.Permission;
import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.managers.ClassManager;
//import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.managers.PlayerManager;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 * Vault permissions handler class
 * This will be the default Handler for permissions
 * @version v0.4.3
 * 
 * @author slipcor, Krglok
 */

public class HandleVaultPerms extends CRHandler {
	private final ClassRanks plugin;
//	private final DebugManager db;
	public static Permission permission = null;

	public HandleVaultPerms(ClassRanks cr) {
		plugin = cr;
//		db = new DebugManager(cr);
	}

	@Override
	public boolean isInGroup(String world, String permName, String player) {
		return permission.playerInGroup(world, player, permName);
	}

	/*
	 * Function that tries to setup the permissions system, returns result
	 */
	@Override
	public boolean setupPermissions() 
	{
		// try to load permissions, return result
		RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        if (permissionProvider != null) 
        {
        	try 
        	{
                permission = permissionProvider.getProvider();
                getPermNameByPlayer(Bukkit.getWorlds().get(0).getName(), "slipcor");
        	} catch (Exception e) 
        	{
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
	 * diese funktion ist wahrscheinlich unnötig !!
	 */
	@Override
	public void classAdd(String world, String player, String cString) 
	{
		
		player = PlayerManager.search(player); // auto-complete playername

		if (world.equalsIgnoreCase("all")) 
		{
			classAddGlobal(player, cString);
			return;
		}
		try 
		{
			permission.playerAddGroup(world, player, cString);
			plugin.db.i("added group " + cString + " to player " + player+ " in world " + world);
		} catch (Exception e) 
		{
			ClassRanks.log("Exception Add Class " + cString + " or user " + player	+ " not found in world " + world, Level.WARNING);
		}
	}

	/*
	 * Add a user to a given rank in the given world
	 * if world = all then rank added without world
	 * stores player Rank also in the config
	 */
	@Override
	public void rankAdd(String world, String player, String rank) 
	{
		player = PlayerManager.search(player); // auto-complete playername
		if (world.equalsIgnoreCase("all")) 
		{
			rankAddGlobal(player, rank);
			return;
		}

		try 
		{
			if (permission.playerAddGroup(world, player, rank))//(permission.playerAddGroup(world, player, rank)) 
			{
				plugin.db.i("Vault added rank " + rank + " to player " + player	+ " in world " + world);
				String cString = ClassManager.getClassNameByPermName(rank);
				plugin.getConfig().set("players." + player + "." + cString, rank);
				plugin.getConfig().set("worlds." + world + "." + player, rank);
				plugin.saveConfig();
				plugin.db.i("Vault PermName " + rank + ": user " + player + " set " + world);

			} else {
				plugin.db.i("ERROR Vault added rank " + rank + " to player " + player	+ " in world " + world);
			}
		} catch (Exception e) {
			ClassRanks.log("Exception added rank " + rank + " or user " + player + " not found in world " + world, Level.WARNING);
		}
	}

	/*
	 * Remove a user from the Rank he has in the given world
	 * if world = all then rank added without world
	 * Remove player Rank also in the config
	 */
	@Override
	public void rankRemove(String world, String player, String cString) {
		player = PlayerManager.search(player); // auto-complete playername

		if (world.equalsIgnoreCase("all")) 
		{
			rankRemoveGlobal(player, cString);
			return;
		}

		try {
			if (permission.playerRemoveGroup(world, player, cString))
			{
				plugin.db.i("removed rank " + cString + " from player " + player+ " in world " + world);
				plugin.getConfig().set("players." + player, null);
				plugin.getConfig().set("worlds." + world + "." + player, null);
				plugin.saveConfig();
			} else
			{
				
			}
		} catch (Exception e) {
			ClassRanks.log("Exception Remove Rank " + cString + " or user " + player+ " not found in world " + world, Level.WARNING);
		}
	}

	/* (non-Javadoc)
	 * @see net.slipcor.classranks.handlers.CRHandler#getPermNameByPlayer(java.lang.String, java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public String getPermNameByPlayer(String world, String player) {
		Map<String, Object> groups = null;
        String cPerm = "";
        
		if ((world == "") || (world == "all") ) {
			try 
			{
				groups = plugin.getConfig().getConfigurationSection("players." + player).getValues(true);
			} catch (Exception e) {
				plugin.db.i("Exception GetPermByPlayer : GetConfigurationSection");
			}
			
			if (groups == null) {
				return "";
			}
			// Set last Value as result
			for (Object group : groups.values()) {
				cPerm = (String) group;
//				permGroups.add((String) group);
			}
			plugin.db.i("player has groups: " + groups.toString());
		} else {
			try {
				groups = (Map<String, Object>) plugin.getConfig().getConfigurationSection("worlds." + world);
			} catch (Exception e) {
				plugin.db.i("Exception GetPermByPlayer : GetConfigurationSection");
			}
			
			if (groups == null) {
				return "";
			}
			if (groups.containsKey(player)) {
				cPerm = groups.get(player).toString();
				plugin.db.i("worlds contains key "+player +" value"+ cPerm);
			} else {
				plugin.db.i("worlds contains NOT key "+player );
			}
			
		}
//		return ClassManager.getLastPermNameByPermGroups(permGroups);
		return cPerm;
	}

	@Override
	public void classAddGlobal(String player, String cString) 
	{
		player = PlayerManager.search(player); // auto-complete playername
		String world = null;
		try 
		{
			if (permission.playerAddGroup(world,player, cString)==false)
			{
				ClassRanks.log("ERROR ADD PermName " + cString + " or user " + player + " ", Level.SEVERE);
				
			} else
			{
				ClassRanks.log("Vault Global PermName " + cString + ": user " + player , Level.INFO);
				plugin.db.i("Global added rank " + cString + " to player " + player);
			}
		} catch (Exception e) {
			ClassRanks.log("Exception Add Class Global " + cString + " or user " + player, Level.SEVERE);
		}
	}

	@Override
	public void rankAddGlobal(String player, String rank) 
	{
		ClassRanks.log("ADD PermName " + rank + " or user " + player + " ", Level.INFO);
		player = PlayerManager.search(player); // auto-complete playername
		String cString = ClassManager.getClassNameByPermName(rank);
		String world = null;
		try 
		{
			if (permission.playerAddGroup(world,player, rank)==false)
			{
				ClassRanks.log("ERROR ADD PermName " + rank + " or user " + player + " ", Level.SEVERE);
				
			} else
			{
				ClassRanks.log("Vault Global PermName " + rank + ": user " + player , Level.INFO);
				plugin.db.i("Global added rank " + rank + " to player " + player);
	
				plugin.getConfig().set("players." + player + "." + cString, rank);
				plugin.db.i("Vault added rank " + rank + " to player " + player + ", no world support");
				plugin.saveConfig();
			}
		} catch (Exception e) 
		{
			ClassRanks.log("Exception ADD GLOBAL RANK " + rank + " or user " + player, Level.SEVERE);
			ClassRanks.log(e.getMessage(), Level.SEVERE);
			ClassRanks.log(e.getClass().getName(), Level.SEVERE);
			ClassRanks.log(e.getCause().getMessage(), Level.SEVERE);
		}
	}

	@Override
	public void rankRemoveGlobal(String player, String cString) {

		player = PlayerManager.search(player); // auto-complete playername
		plugin.getConfig().set("players." + player, null);
		plugin.saveConfig();
	    String world = null;
		try 
		{
			permission.playerRemoveGroup(world,player, cString);
			plugin.db.i("removed rank " + cString + " from player " + player);
		} catch (Exception e) {
			ClassRanks.log("Exception Remove Group Global" + cString + " or user " + player+ " not found ", Level.SEVERE);
		}
	}

	@Override
	public String getPermNameByPlayerGlobal(String player) 
	{
		player = PlayerManager.search(player); // auto-complete playername
		ArrayList<String> permGroups = new ArrayList<String>();
		
		String[] list = permission.getPlayerGroups(Bukkit.getPlayer(player));
		for (String sRank : list) 
		{
			plugin.db.i("checking rank "+sRank);
			if (ClassManager.rankExists(sRank)) 
			{
				permGroups.add(sRank);
			}
		}
		plugin.db.i("player has groups: " + permGroups.toString());
		return ClassManager.getLastPermNameByPermGroups(permGroups);
	}

	/**
	 * This function remove ALL groups from the player 
	 * 
	 */
	@Override
	public void removeGroups(Player player) 
	{
		String[] list = permission.getPlayerGroups(player);
		if (list != null) {
			for (String sRank : list) {
				permission.playerRemoveGroup(player, sRank);
				plugin.getConfig().set("players." + player, null);
			}
			plugin.saveConfig();
		}
	}

	@Override
	public  String[] getPlayerGroups(String player)
	{
		String world = null;
		return  permission.getPlayerGroups(world,player);
	}
}
