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
public class PlayerClassList {
	
	private String playerName;
	
	private ArrayList<PlayerClassRank> playerClassRanks;

	public PlayerClassList(String playerName,
			ArrayList<PlayerClassRank> playerClassRanks) {
		super();
		this.playerName = playerName;
		this.playerClassRanks = playerClassRanks;
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
		
		if (playerClassRanks == null) {
			playerClassRanks = new ArrayList<PlayerClassRank>();
		}
		playerClassRanks.add(new PlayerClassRank(className, rankName));
		
		return true;
	}
	
	public PlayerClassRank getPlayerClassRank(int index) {
		
		return playerClassRanks.get(index); 
	}

	public String getPlayerClass(int index) {
		
		return playerClassRanks.get(index).className; 
	}

	public String getPlayerRank(int index) {
		
		return playerClassRanks.get(index).rankName; 
	}
	
	
	public boolean equalsIgnoreCaseClass(String className) {
		boolean isfound = false;
		for (Iterator<PlayerClassRank> item =  playerClassRanks.iterator(); item.hasNext(); ) {
			 if ( item.next().className.equalsIgnoreCase(className)) {
				 isfound = true;
			 }
		}
		return isfound;
	}

	public boolean equalsIgnoreCaseRank(String rankName) {
		boolean isfound = false;
		for (Iterator<PlayerClassRank> item =  playerClassRanks.iterator(); item.hasNext(); ) {
			 if ( item.next().rankName.equalsIgnoreCase(rankName)) {
				 isfound = true;
			 }
		}
		return isfound;
	}

	@SuppressWarnings("null")
	public int getClassIndex(String className) {
		int found = -1;
		PlayerClassRank[] items = null; 
		if (playerClassRanks.size() > 0) {
			playerClassRanks.toArray(items);
			for (int i=0; i<items.length; i++) {
				 if ( items[i].className.equalsIgnoreCase(className)) {
					 found = i;
				 }
			}
		}
		return found;
	}

	@SuppressWarnings("null")
	public int getRankIndex(String rankName) {
		int found = -1;
		PlayerClassRank[] items = null; 
		if (playerClassRanks.size() > 0) {
			playerClassRanks.toArray(items);
			for (int i=0; i<items.length; i++) {
				 if ( items[i].rankName.equalsIgnoreCase(rankName)) {
					 found = i;
				 }
			}
		}
		return found;
	}
	
}
