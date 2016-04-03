package net.slipcor.classranks.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 * @author Windu
 * 
 * define a player Class Rank List object will be used to track player
 * clazzes. the playerClazzes are are a key/Value List
 * each player can have 1 entry for a clazz.
 * clazz is unique in the list
 */
public class PlayerClazz 
{
	private static final long serialVersionUID = -3071712660629867671L;

	private String uuid;
	private String playername;
	private HashMap <String,String> playerClazzRanks;

	// private ArrayList<PlayerClassRank> playerClassRanks;

	public PlayerClazz(String uuid, String playerName)
	{
		super();
		this.uuid =uuid;
		this.playername = playername;
		this.playerClazzRanks = new HashMap <String,String>();
	}
	
	/**
	 * @return the uuid
	 */
	public String getUuid()
	{
		return uuid;
	}

	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid)
	{
		this.uuid = uuid;
	}


	public void setPlayerName(String playerName, boolean isUUID)
	{
		if (isUUID)
		{
			this.setUuid(playerName);
		}
		this.playername = playerName;
	}

	public String getPlayerName()
	{
		return this.playername;
	}
	
	public HashMap <String,String> playerClazzRanks()
	{
		return playerClazzRanks;
	}
	
	public void setplayerClazzRanks(HashMap <String,String> list)
	{
		this.playerClazzRanks = list;
	}

	public boolean equalsIgnoreCaseName(String playerName)
	{
		return playerName.equalsIgnoreCase(playerName);
	}

	/**
	 * add ClazzRank to list , overwrite existing entry
	 * 
	 * @param clazzName
	 * @param rankName
	 * @return
	 */
	public boolean addPlayerClassRank(String clazzName, String rankName)
	{
		this.playerClazzRanks.put(clazzName, rankName);

		return true;
	}

	public String getPlayerRank(String className)
	{
		if (this.playerClazzRanks.containsKey(className))
		{
			return this.playerClazzRanks.get(className);
		} else
		{
			return "";
		}
	}

	public boolean hasClass(String className)
	{
		for (String name : this.playerClazzRanks().keySet())
		{
			if (name.equalsIgnoreCase(className))
			{
				return true;
			}
		}
		return false;
	}

	public boolean hasRank(String rankName)
	{
		for (String rank : this.playerClazzRanks.values())
		{
			if (rank.equalsIgnoreCase(rankName))
			{
				return true;
			}
		}
		return false;
	}


}
