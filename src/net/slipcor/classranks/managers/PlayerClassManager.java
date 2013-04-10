package net.slipcor.classranks.managers;

import net.slipcor.classranks.core.*;

import java.util.ArrayList;
import java.util.Iterator;

public class PlayerClassManager {
	
	private  ArrayList<PlayerClassList> playerClassList;

	public ArrayList<PlayerClassList> getPlayerClassList() {
		return playerClassList;
	}

//	public void setPlayerClassList(ArrayList<PlayerClassList> playerClassList) {
//		this.playerClassList = playerClassList;
//	}

	public PlayerClassManager() {
		super();
		this.playerClassList = new ArrayList<PlayerClassList>();
	}

	/*
	 * add player to list
	 * prevent double entrys
	 */
	public void addPlayer(String playerName) {
		
		//  player not found make player entry
		if (!checkPlayerName(playerName)) {
			playerClassList.add(new PlayerClassList(playerName, null));
		}
		
	}
	
	/*
	 * checks for playername in list
	 */
	public boolean checkPlayerName(String playerName)  {
		
		if (playerClassList.isEmpty()) {
			return false;
		}
		boolean isfound = false;
		for (Iterator<PlayerClassList> item =  playerClassList.iterator(); item.hasNext(); ) {
			 if ( item.next().equalsIgnoreCaseName(playerName)) {
				 isfound = true;
			 }
		}
		return isfound;
		
	}
	
	
	
}
