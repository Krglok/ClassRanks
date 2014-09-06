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
import org.bukkit.OfflinePlayer;
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

	public HandleVaultPerms(ClassRanks plugin) {
		this.plugin = plugin;
//		db = new DebugManager(cr);
	}

//	public boolean isInGroup(String world, String permName, String player) 
//	{
//		return permission.playerInGroup(world, player, permName);
//	}
//
	@Override
	public boolean isInGroup(String world, String permName, Player player) 
	{
		if (plugin.config.isDefaultrankallworlds())
		{
//			return permission.playerInGroup(world, player, permName);
			world = null;
			return permission.playerInGroup(world,player.getName(), permName);
		} else
		{
			return permission.playerInGroup(player, permName);
		}
	}
	
	/*
	 * Function that tries to setup the permissions system, returns result
	 */
	@Override
	public boolean setupPermissions() 
	{
		// try to load permissions, return result
//		RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
        	try 
        	{
                permission = plugin.permission;
                permission.getGroups();	// test for permission is instanced
//                getPermNameByPlayer(Bukkit.getWorlds().get(0).getName(), "slipcor");
        	} catch (Exception e) 
        	{
        		return false;
        	}
        return true;
	}

	@Override
	public boolean hasPerms(Player player, String perm, String world) 
	{
		if (world == null) world = "";
		if (perm == null) return false; 
		if (permission == null)
		{
			System.out.println("no VAULT not instance !");
			return false;
		}
		if (plugin.config.isDefaultrankallworlds())
		{
			world = null;
			return permission.playerHas(world, player.getName(), perm);
		} else
		{
//			return permission.playerHas(world, player, perm);
			return permission.playerHas(player, perm);
		}
	}

	/*
	 * Add a user to a given class in the given world
	 * diese funktion ist wahrscheinlich unnötig !!
	 */
	@Override
	public void classAdd(String world, Player player, String cString) 
	{
		
//		player = PlayerManager.searchName(player); // auto-complete playername

		if (plugin.config.isDefaultrankallworlds())
		{
			classAddGlobal(player, cString);
			return;
		}
		try 
		{
			
//			permission.playerAddGroup(world, player, cString);
			permission.playerAddGroup(player, cString);
			plugin.db.i("added group " + cString + " to player " + player+ " in world " + world);
		} catch (Exception e) 
		{
			ClassRanks.log("Exception Add Class " + cString + " or user " + player	+ " not found in world " + world, Level.WARNING);
		}
	}
	
	
	@Override
	/**
	 * Add Class/rank in all worlds
	 * world set to null
	 * 
	 */
	public void classAddGlobal(Player player, String cString) 
	{
//		player = PlayerManager.searchName(player); // auto-complete playername
		String world = null;
		try 
		{
			if (permission.playerAddGroup(player, cString) == false)
			{
				ClassRanks.log("ERROR ADD PermName " + cString + " or user " + player + " ", Level.SEVERE);
				
			} else
			{
				ClassRanks.log("Vault Global PermName " + cString + ": user " + player , Level.INFO);
				plugin.db.i("Added Class/Rank " + cString + " to player " + player);
			}
		} catch (Exception e) {
			ClassRanks.log("Exception Add Class Global " + cString + " or user " + player, Level.SEVERE);
		}
	}
	
	/*
	 * Add a user to a given rank in the given world
	 * if world = all then rank added without world
	 * stores player Rank also in the config
	 */
	@Override
	public void rankAdd(Player player, String rank) 
	{
		if (plugin.config.isDefaultrankallworlds())
		{
			System.out.println("CR rankAddGlobal");
			rankAddGlobal(player, rank);
		} else
		{
			try 
			{
				if (permission.playerAddGroup(player, rank)) 
				{
					plugin.db.i("Vault added rank " + rank + " to player " + player	+ " in world " + player.getWorld().getName());
					String cString = ClassManager.getClassNameByPermName(rank);
					plugin.getConfig().set("players." + player + "." + cString, rank);
					plugin.getConfig().set("worlds." + player.getWorld().getName() + "." + player, rank);
					plugin.saveConfig();
					plugin.db.i("Vault PermName " + rank + ": user " + player + " set " + player.getWorld().getName());
	
				} else {
					plugin.db.i("ERROR Vault added rank " + rank + " to player " + player	+ " in world " + player.getWorld().getName());
				}
			} catch (Exception e) {
				ClassRanks.log("Exception added rank " + rank + " or user " + player + " not found in world " + player.getWorld().getName(), Level.WARNING);
			}
		}
	}

	/**
	 * add rank/group in all worlds
	 * world set to null
	 */
	@Override
	public void rankAddGlobal(Player player, String rank) 
	{
		ClassRanks.log("ADD PermName " + rank + " or user " + player + " ", Level.INFO);
//		player = PlayerManager.searchName(player); // auto-complete playername
		String cString = ClassManager.getClassNameByPermName(rank);
		String world = null;
		try 
		{
			if (permission.playerAddGroup(world, player.getName(), rank)==false)
//			if (permission.playerAddGroup(player, rank)==false)
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


	/* (non-Javadoc)
	 * @see net.slipcor.classranks.handlers.CRHandler#getPermNameByPlayer(java.lang.String, java.lang.String)
	 */
	@Override
	public String getPermNameByPlayer(String world, Player player) 
	{
		String[] groups = null;
        String cPerm = "";
        
		if (plugin.config.isDefaultrankallworlds())
		{
			try 
			{
//				groups = plugin.getConfig().getConfigurationSection("players." + player).getValues(true);
//				groups = permission.getPlayerGroups(player);
				groups = permission.getPlayerGroups(world,player.getName());
			} catch (Exception e) {
				plugin.db.i("Exception GetPermByPlayer : GetPlayerGroups");
				return "";
			}
			
			if (groups.length == 0) 
			{
				return "";
			}
			// Set last Value as result
			return groups[groups.length];
		} else 
		{
			try {
//				groups = (Map<String, Object>) plugin.getConfig().getConfigurationSection("worlds." + world);
				groups = permission.getPlayerGroups(world,player.getName());
			} catch (Exception e) {
				plugin.db.i("Exception GetPermByPlayer : GetConfigurationSection");
				return "";
			}
			
			if (groups.length == 0) 
			{
				return "";
			}
			return groups[groups.length];
			
		}
	}

	@Override
	public String getPermNameByPlayerGlobal(Player player) 
	{
//		player = PlayerManager.searchName(player); // auto-complete playername
		ArrayList<String> permGroups = new ArrayList<String>();
		String world = null;
		String[] list = permission.getPlayerGroups(world,player.getName());
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
	 * Remove a user from the Rank in world that is current
	 * if world = all then rank added without world
	 * Remove player Rank also in the config
	 */
	@Override
	public void rankRemove(Player player, String rank) 
	{
//		player = PlayerManager.searchName(player); // auto-complete playername
		if (plugin.config.isDefaultrankallworlds())
		{
			System.out.println("CR rankRemoveGlobal");
			rankRemoveGlobal(player, rank);
		} else
		{
			try 
			{
				if (permission.playerRemoveGroup(player, rank))
				{
					plugin.db.i("removed rank " + rank + " from player " + player+ " in world ");
				} else
				{
					plugin.db.i("NOT removed rank " + rank + " from " + player+ " in world ");
				}
			} catch (Exception e) {
				ClassRanks.log("Exception Remove Rank " + rank + " or user " + player+ " not found in world " , Level.WARNING);
			}
		}
	}

	@Override
	public void rankRemoveGlobal(Player player, String rank) 
	{
	    String world = null;
		try 
		{
			permission.playerRemoveGroup(world,player.getName(), rank);
			plugin.db.i("removed rank " + rank + " from player " + player);
		} catch (Exception e) {
			ClassRanks.log("Exception Remove Group Global" + rank + " or user " + player+ " not found ", Level.SEVERE);
		}
	}


	/**
	 * This function remove ALL groups from the player 
	 * 
	 */
	@Override
	public void removeGroups(Player player) 
	{
		String[] list = permission.getPlayerGroups(player);
		if (list != null) 
		{
			for (String sRank : list) 
			{
				permission.playerRemoveGroup(player, sRank);
				plugin.getConfig().set("players." + player, null);
			}
			plugin.saveConfig();
		}
	}




	@Override
	public String[] getPlayerGroups(Player player)
	{
		String world = null;
		return  permission.getPlayerGroups(world,player.getName());
//		return permission.getPlayerGroups(player);
	}
}
