package net.slipcor.classranks.core;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author Windu
 *
 *	define a player Class Rank List object
 *  will be used to track player classes
 */
public class PlayerClazzList  extends ArrayList<PlayerClazzRank>
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3071712660629867671L;
	
	private String playerName;
	
//	private ArrayList<PlayerClassRank> playerClassRanks;

	public PlayerClazzList(String playerName, ArrayList<PlayerClazzRank> playerClassRanks) 
	{
		super();
		this.playerName = playerName;
		this.addAll(playerClassRanks);
	}
	
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	
	public String getPlayerName() {
		return this.playerName;
	}
	
	public boolean equalsIgnoreCaseName(String playerName) {
		return playerName.equalsIgnoreCase(playerName);
	}
	
	public boolean addPlayerClassRank (String className, String rankName) {
		
		this.add(new PlayerClazzRank(className, rankName));
		
		return true;
	}
	
	public PlayerClazzRank getPlayerClassRank(int index) {
		
		return this.get(index); 
	}

	public String getPlayerClass(int index) {
		
		return this.get(index).className; 
	}

	public String getPlayerRank(int index) {
		
		return this.get(index).rankName; 
	}
	
	
	public boolean equalsIgnoreCaseClass(String className) {
		boolean isfound = false;
		for (Iterator<PlayerClazzRank> item =  this.iterator(); item.hasNext(); ) {
			 if ( item.next().className.equalsIgnoreCase(className)) {
				 isfound = true;
			 }
		}
		return isfound;
	}

	public boolean equalsIgnoreCaseRank(String rankName) 
	{
		boolean isfound = false;
		for (Iterator<PlayerClazzRank> item =  this.iterator(); item.hasNext(); ) 
		{
			 if ( item.next().rankName.equalsIgnoreCase(rankName)) 
			 {
				 isfound = true;
			 }
		}
		return isfound;
	}

	@SuppressWarnings("null")
	public int getClassIndex(String className) 
	{
		int found = -1;
		PlayerClazzRank[] items = null; 
		if (this.size() > 0) 
		{
			this.toArray(items);
			for (int i=0; i<items.length; i++) 
			{
				 if ( items[i].className.equalsIgnoreCase(className)) 
				 {
					 found = i;
				 }
			}
		}
		return found;
	}

	@SuppressWarnings("null")
	public int getRankIndex(String rankName) 
	{
		int found = -1;
		PlayerClazzRank[] items = null; 
		if (this.size() > 0) 
		{
			this.toArray(items);
			for (int i=0; i<items.length; i++) 
			{
				 if ( items[i].rankName.equalsIgnoreCase(rankName)) 
				 {
					 found = i;
				 }
			}
		}
		return found;
	}
	
}
