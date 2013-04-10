package net.slipcor.classranks.core;

/**
 * 
 * @author Windu
 *
 *	define a Player Class Rank, will be used to track player clases
 */
public class PlayerClassRank {
	public String className;
	
	public String rankName;

	public PlayerClassRank(String className, String rankName) {
		super();
		this.className = className;
		this.rankName = rankName;
	}
	
}
