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
	private RankList rankList;
	private String clazzName;

	/**
	 * create a class instance
	 * 
	 * @param sClassName
	 *            the class name
	 */
	public Clazz(String sClassName)
	{
		this.clazzName = sClassName;
		rankList = new RankList();
	}

	
	public RankList ranks()
	{
		return rankList;
	}

	public String clazzName()
	{
		return clazzName;
	}
	
	public void setClazzName(String sClassNewName)
	{
		clazzName = sClassNewName;
	}
	
	public boolean containRank(String sPermName)
	{
		return this.rankList.hasRank(sPermName);
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
	public void addRank(String sPermName, String sDispName, ChatColor cColor,
			ItemStack[] isItems, double dCost, int iExp)
	{
		Rank rank = new Rank(sPermName, sDispName, cColor, this, isItems,dCost, iExp);
		this.rankList.addRank(rank);
	}

	/**
	 * remove a string from class
	 * be carefull, this reindex the ranks !!!
	 * 
	 * @param sPermName  the rank name to remove
	 */
	public void removeRank(String sPermName)
	{
		if (containRank(sPermName))
		{
			RankList newRanks = new RankList();
			for (int index : this.rankList.keySet())
			{
				Rank rank = this.rankList.get(index);
				if (rank.getPermName().equalsIgnoreCase(sPermName) == false)
				{
					newRanks.addRank(rank);
				}
			}
			rankList = newRanks;
		}
	}
	

	
	
}
