package net.slipcor.classranks.core;

/**
 * 
 * @author Windu
 *
 *	define a Player Rank in a Clazz , will be used to track player clazzes
 *  - className = name of the ClassRank
 *  - rankName  = name of the rank or permissionGroup
 * The player  can go rankUp or rankDown in the Clazz
 * A player can have only one rank in each Clazz, given by this key/value
 *  
 */
public class PlayerClazzRank 
{
	public String className;
	
	public String rankName;

	public PlayerClazzRank(String className, String rankName) 
	{
		super();
		this.className = className;
		this.rankName = rankName;
	}
	
}
