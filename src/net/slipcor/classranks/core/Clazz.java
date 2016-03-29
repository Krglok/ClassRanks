package net.slipcor.classranks.core;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * realize a objekt with multiple ranks. Each rank is a name of a
 * permissionGroup 
 * - ClassName 
 * - [0] = Rankname 0 
 * - [1] = Rankname 1 
 * - [2] = Rankname 2 
 * - [3] = Rankname 3 
 * and so on. The rank is given by position in the list.
 * You can go rankUp or rankDown in the list.
 * A player can have one rank in each Clazz.
 * 
 * a Clazzname be unique
 * a Rankname must be unique in all Clazzes 
 * 
 * @version v 0.7.1  krglok 
 *  * 
 * @version v0.3.0
 * @author slipcor
 */

public class Clazz
{
	public HashMap<Integer,Rank> ranks = new HashMap<Integer,Rank>();
	public String clazzName;

	/**
	 * create a class instance
	 * 
	 * @param sClassName
	 *            the class name
	 */
	public Clazz(String sClassName)
	{
		this.clazzName = sClassName;
	}

	public boolean containRank(String sPermName)
	{
		for (Rank rank : ranks.values())
		{
			if (rank.sPermissionName.equalsIgnoreCase(sPermName) == true)
			{
				return true;
			}
		}
		return false;
	}
	
	/**
	 * add a rank to class
	 * 
	 * @param sPermName   the rank permissions name
	 * @param sDispName   the rank display name
	 * @param cColor      the rank color
	 * @param isItems     the array of rank items cost
	 * @param dCost       the rank cost (money)
	 * @param iExp        the rank exp cost
	 */
	public void add(String sPermName, String sDispName, ChatColor cColor,
			ItemStack[] isItems, double dCost, int iExp)
	{
		if (containRank(sPermName) == false)
		{
			int nextIndex = this.ranks.size()+1;
			this.ranks.put(nextIndex,new Rank(sPermName, sDispName, cColor, this, isItems,
				dCost, iExp));
		}
	}

	/**
	 * remove a string from class
	 * be carefull, this reindex the ranks !!!
	 * 
	 * @param sPermName  the rank name to remove
	 */
	public void remove(String sPermName)
	{
		if (containRank(sPermName))
		{
			HashMap<Integer,Rank> newRanks = new HashMap<Integer,Rank>();
			for (Rank rank : ranks.values())
			{
				if (rank.sPermissionName.equalsIgnoreCase(sPermName) == false)
				{
					int nextIndex = this.ranks.size()+1;
					newRanks.put(nextIndex, rank);
				}
			}
			ranks = newRanks;
		}
	}
	
	/**
	 * search for next rank in ranklist
	 * 
	 * @param sPermName
	 * @return sPermissionName or null
	 */
	public String nextRankPerm(String sPermName)
	{
		int lastIndex = 0;
		for (Integer key : this.ranks.keySet())
		{
			
			if (key < lastIndex)
			{
				Rank rank = this.ranks.get(key+1);
				return rank.sPermissionName;
			}
		}
		return null;
	}

	
	public Rank nextRank(String sPermName)
	{
		int lastIndex = 0;
		for (Integer key : this.ranks.keySet())
		{
			
			if (key < lastIndex)
			{
				Rank rank = this.ranks.get(key+1);
				return rank;
			}
		}
		return null;
	}
	
	/**
	 * search for prev rank in ranklist
	 * 
	 * @param sPermName
	 * @return sPermissionName or null
	 */
	public String prevRankName(String sPermName)
	{
		for (Integer key : this.ranks.keySet())
		{
			Rank rank = this.ranks.get(key);
			if (rank.sPermissionName.equalsIgnoreCase(sPermName))
			{
				if (key > 1)
				{
					rank = this.ranks.get(key-1);
					return rank.sPermissionName;
				}
				return null;
			}
		}
		return null;
	}
	
	
	public Rank prevRank(String sPermName)
	{
		for (Integer key : this.ranks.keySet())
		{
			Rank rank = this.ranks.get(key);
			if (rank.sPermissionName.equalsIgnoreCase(sPermName))
			{
				if (key > 1)
				{
					rank = this.ranks.get(key-1);
					return rank;
				}
				return null;
			}
		}
		return null;
	}

	
	public int getIndexOf(Rank rRank)
	{
		String sPermName = rRank.getPermName();
		for (Integer key : this.ranks.keySet())
		{
			Rank rank = this.ranks.get(key);
			if (rank.sPermissionName.equalsIgnoreCase(sPermName))
			{
				return key;
			}
		}
		return 0;
	}
}
