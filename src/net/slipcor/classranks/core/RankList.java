package net.slipcor.classranks.core;

import java.util.HashMap;

/**
 * 
 * @author Windu
 * @create 02.04.2016
 * <pre>
 * description:
 * Hold the list of ranks for a clazz
 * becarefull with add and remove the ranks are indexed !
 * so add append to the list
 * and remove destroy the index. use for remove the methode in the Clazz
 * this methode make a reindex on remove 
 *
 * </pre>
 */

public class RankList extends HashMap<Integer, Rank>
{
	private static final long serialVersionUID = 1L;
	
	public RankList()
	{
		
	}
	
	public int addRank(Rank rank)
	{
		if (this.hasRank(rank.getPermName())==false)
		{
			int key =this.size()+1;
			this.put(key, rank);
			return key;
		}
		return -1;
	}
	
	public boolean hasRank(String permName)
	{
		for (Rank rank : values())
		{
			if (rank.getPermName().equalsIgnoreCase(permName))
			{
				return true;
			}
		}
		return false;
	}
	
	public Rank getRankByIndex(int index)
	{
		if (index < this.size())
		{
			return this.get(index);
		}
		return null;
	}
	
	public Rank getRankByName(String permName)
	{
		for (Rank rank : values())
		{
			if (rank.getPermName().equalsIgnoreCase(permName))
			{
				return rank;
			}
		}
		return null;
	}
	
	public Rank getRankByClazz(String clazzName)
	{
		for (Rank rank : values())
		{
			if (rank.getSuperClass().clazzName().equalsIgnoreCase(clazzName))
			{
				return rank;
			}
		}
		return null;
	}
	
	public int getRankIndex(String permName)
	{
		for (int index : keySet())
		{
			Rank rank = get(index);
			if (rank.getPermName().equalsIgnoreCase(permName))
			{
				return index;
			}
		}
		return -1;
	}
	
	public int getMaxIndex()
	{
		return size();
	}

	public Rank nextRank(String sPermName)
	{
		int index = getRankIndex(sPermName);
		if (index+1 <= this.size())
		{
			Rank rank = this.get(index+1);
			return rank;
		}
		return null;
	}

//	/**
//	 * search for next rank in ranklist
//	 * 
//	 * @param sPermName
//	 * @return sPermissionName or null
//	 */
//	public String nextRankName(String sPermName)
//	{
//		for (Integer key : this.keySet())
//		{
//			
//			if (key+1 < this.size())
//			{
//				Rank rank = this.get(key+1);
//				return rank.getPermName();
//			}
//		}
//		return null;
//	}

	
//	/**
//	 * search for prev rank in ranklist
//	 * 
//	 * @param sPermName
//	 * @return sPermissionName or null
//	 */
//	public String prevRankName(String sPermName)
//	{
//		for (Integer key : this.keySet())
//		{
//			Rank rank = this.get(key);
//			if (rank.getPermName().equalsIgnoreCase(sPermName))
//			{
//				if (key > 1)
//				{
//					rank = this.get(key-1);
//					return rank.getPermName();
//				}
//				return null;
//			}
//		}
//		return null;
//	}
	
	
	public Rank prevRank(String sPermName)
	{
		int index = getRankIndex(sPermName);
		if (index > 1)
		{
			Rank rank = this.get(index-1);
			return rank;
		}
		return null;
	}
	
}
