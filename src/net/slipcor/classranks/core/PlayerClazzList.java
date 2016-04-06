package net.slipcor.classranks.core;

import java.util.HashMap;

import org.bukkit.entity.Player;

/**
 * 
 * @author Windu
 * @create 03.04.2016
 * <pre>
 * description:
 * Hold the list of players and Clazzes of the player.
 * key = uuid
 * value = playerClazz
 *
 * </pre>
 */
public class PlayerClazzList extends HashMap<String, PlayerClazz>
{
	private static final long serialVersionUID = 1L;

	
	public PlayerClazzList()
	{
		
	}
	
	/**
	 * add PlayerClazz tio end of List
	 * if playerUUID not in list
	 * 
	 * @param pClazz
	 */
	public void addPlayerClazz(PlayerClazz pClazz)
	{
		if (this.containsKey(pClazz.getUuid())==false)
		{
			this.put(pClazz.getUuid(), pClazz);
		}
	}
	
	/**
	 * add PlayerClazz to list. overwrite existing data
	 * 
	 * @param pClazz
	 */
	public void importPlayerClazz(PlayerClazz pClazz)
	{
		this.put(pClazz.getUuid(), pClazz);
	}
	
	public PlayerClazz getPlayerClazz(String uuid)
	{
		if (this.containsKey(uuid))
		{
			return this.get(uuid);
		}
		
		return null;
	}

	public PlayerClazz getPlayerClazzByName(String playername)
	{
		for (PlayerClazz pClazz : this.values())
		{
			if (pClazz.getPlayerName().equalsIgnoreCase(playername))
			{
				return pClazz;
			}
		}
		return null;
	}

	public void removeClass(Player player, String className)
	{
		PlayerClazz pClazz = this.get(player.getUniqueId().toString());
		for (String clazzName : pClazz.playerClazzRanks().keySet() )
		{
			if (clazzName.equalsIgnoreCase(className))
			{
				pClazz.playerClazzRanks().remove(clazzName);
			}
		}
				
	}
}
