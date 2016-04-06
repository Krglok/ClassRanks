package net.slipcor.classranks.handlers;

import org.bukkit.entity.Player;

/**
 * Permissions handler class
 * 
 * @version v0.4.3
 * 
 * @author slipcor
 */

public abstract class CRHandler 
{
	/**
	 * check if user in group
	 * use world = null for search in all worlds
	 * 
	 * @param world
	 * @param permName
	 * @param player
	 * @return
	 */
	public abstract boolean isInGroup(String world, String permName, Player player);
	
	/**
	 * initialize permission provider
	 * 
	 * @return
	 */
	public abstract boolean setupPermissions();
	
	/**
	 * check permission
	 * use world = null for check in all worlds
	 *  
	 * @param player
	 * @param string
	 * @param world
	 * @return
	 */
	public abstract boolean hasPerms(Player player, String string, String world);
	
	/**
	 * Add rank/group to player permission
	 * use classAddGlobal if congig allWorlds = true
	 * 
	 * @param world
	 * @param player
	 * @param cString
	 */
	public abstract void classAdd(String world, Player player, String cString);
	
//	/**
//	 * add rank/group to player permission in all worlds
//	 * use world = null
//	 * 
//	 * @param player
//	 * @param cString
//	 */
//	public abstract void classAddGlobal(Player player, String cString);
	
	/**
	 * Add rank/group to player permission
	 * use rankAddGlobal if congig allWorlds = true
	 * 
	 * @param world
	 * @param player
	 * @param rank
	 */
	public abstract void rankAdd( Player player, String rank, String world);
	
//	/**
//	 * add rank/group to player permission in all worlds
//	 * use world = null
//	 * 
//	 * @param player
//	 * @param rank
//	 */
//	public abstract void rankAddGlobal(Player player, String rank);
	
	/**
	 * remove rank/group from player
	 * use rankRemoveGlobal if congig allWorlds = true
	 * 
	 * @param world
	 * @param player
	 * @param cString
	 */
	public abstract void rankRemove(Player player, String cString, String world);
	
//	/**
//	 * remove rank/group from player permission in all worlds
//	 * use world = null
//	 * 
//	 * @param player
//	 * @param cString
//	 */
//	public abstract void rankRemoveGlobal(Player player, String cString);
	
	public abstract String getPermNameByPlayer(String world, Player player);
	
//	public abstract String getPermNameByPlayerGlobal(Player player);
	
	public abstract void removeGroups(Player player, String world);
	
	public abstract String[] getPlayerGroups(Player player, String world);
}
