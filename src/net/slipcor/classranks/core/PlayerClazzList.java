package net.slipcor.classranks.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 
 * @author Windu
 * 
 * define a player Class Rank List object will be used to track player
 * classes
 */
public class PlayerClazzList extends HashMap<String, String>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3071712660629867671L;

	private String uuid;
	private String playername;

	// private ArrayList<PlayerClassRank> playerClassRanks;

	public PlayerClazzList(String uuid, String playerName)
	{
		super();
		this.uuid =uuid;
		this.playername = playername;
		this.put("name", playerName);
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


	public void setPlayerName(String playerName)
	{
		this.setUuid(playerName);
		this.put("name", playerName);
	}

	public String getPlayerName()
	{
		if (this.containsKey("name"))
		{
			return "";
		}
		return this.get("name");
	}

	public boolean equalsIgnoreCaseName(String playerName)
	{
		return playerName.equalsIgnoreCase(playerName);
	}

	public boolean addPlayerClassRank(String className, String rankName)
	{
		this.put(className, rankName);

		return true;
	}

	public String getPlayerRank(String className)
	{

		return this.get(className);
	}

	public boolean equalsIgnoreCaseClass(String className)
	{
		for (String name : this.keySet())
		{
			if (name.equalsIgnoreCase(className))
			{
				return true;
			}
		}
		return false;
	}

	public boolean equalsIgnoreCaseRank(String rankName)
	{
		for (String rank : this.values())
		{
			if (rank.equalsIgnoreCase(rankName))
			{
				return true;
			}
		}
		return false;
	}


}
