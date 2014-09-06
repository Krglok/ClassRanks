package net.slipcor.classranks.managers;

import net.slipcor.classranks.core.*;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author Windu
 *
 */
public class PlayerClassManager {
	
	private  ArrayList<PlayerClazzList> playerClassList;

	public ArrayList<PlayerClazzList> getPlayerClassList() {
		return playerClassList;
	}

//	public void setPlayerClassList(ArrayList<PlayerClassList> playerClassList) {
//		this.playerClassList = playerClassList;
//	}

	public PlayerClassManager() {
		super();
		this.playerClassList = new ArrayList<PlayerClazzList>();
	}

	/*
	 * add player to list
	 * prevent double entrys
	 */
	public void addPlayer(String playerName) {
		
		//  player not found make player entry
		if (!checkPlayerName(playerName)) {
			playerClassList.add(new PlayerClazzList(playerName, null));
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
		for (Iterator<PlayerClazzList> item =  playerClassList.iterator(); item.hasNext(); ) {
			 if ( item.next().equalsIgnoreCaseName(playerName)) {
				 isfound = true;
			 }
		}
		return isfound;
		
	}
	
	
	
}
