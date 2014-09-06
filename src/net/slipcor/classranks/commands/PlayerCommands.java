package net.slipcor.classranks.commands;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Clazz;
import net.slipcor.classranks.core.Rank;
import net.slipcor.classranks.managers.ClassManager;
import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.managers.FormatManager;
import net.slipcor.classranks.managers.PlayerManager;
import net.slipcor.classranks.register.payment.Method.MethodAccount;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * <pre>
 * command manager , is the command interpreter for the user commands
 * 
 * 
 * @version v0.4.5
 * 
 * @author Krglok
 * </pre>
 */

public class PlayerCommands {
	private final ClassRanks plugin;
	private PlayerManager pm;
	private  DebugManager db;

	public double[] moneyCost = new double[ClassManager.getClasses().size()]; // Costs
	public int[] expCost = new int[ClassManager.getClasses().size()]; // EXP

	Double checkedCost;		// checked cost for rank up / add
	int  checkedExp;		// checked Exp Cost for rank up / add
	ItemStack[] checkedItems;  // checked list of required items for rank up / add
	
	// Costs
//	public boolean rankpublic; // do we spam changed to the public?
//	public boolean onlyoneclass; // only maintain oneass
//	public ItemStack[][] rankItems;
//	public String[] signCheck = { null, null, null }; // SignCheck variable

	public PlayerCommands(ClassRanks plugin) {
		this.plugin = plugin;
		this.pm = plugin.getPlayerManager();
		this.db = plugin.getDebugManager();
	}

	public boolean rankDown(Player sender, Player player, String className, String world) 
	{
		plugin.db.i("ranking " + player.getName() + " : " +  className);

		plugin.db.i("CoolDown Check for " + player.getName());
		int cDown = pm.coolDownCheck(player);
		if (cDown > 0) 
		{
			plugin.msg(sender,
					"You have to wait " + ChatColor.RED + String.valueOf(cDown)
							+ ChatColor.WHITE + " seconds!");
			return true; // cooldown timer still counting => OUT!
		}
		plugin.db.i("cooldown check positive");

		if ((!plugin.perms.hasPerms(sender, "classranks.rankdown", player.getWorld().getName()))) 
		{
			// if we either are no OP or try to rank down, but we may not
			plugin.msg(sender, "You don't have permission to rank down!");
			return true;
		}
		plugin.db.i("perm check successful");


		String playerName = player.getName(); // store the player we want to

				
//		Boolean self = true; // we want to change ourselves ... maybe

		// does player have a rank?
		Rank rank = null; // actual Rank
		if (hasClass(className, player, world))
		{
			rank = hasRank( className, player, world);
			if (rank == null) 
			{
				plugin.msg(sender, "Player " + FormatManager.formatPlayer(playerName)
						+ " does not have a class !");
				return true;
			}
		} else
		{
			plugin.msg(sender, "Player " + FormatManager.formatPlayer(playerName)
					+ " not in class "+ className);
			return true;
		}

		plugin.db.i("rank check successful");

		Clazz oClass = rank.getSuperClass();   // actual Rank class

		int rID = ClassManager.getRankIndex(rank, oClass );	//  Index des Rank
		int iMaxRank = oClass.ranks.size() - 1; // Classes max tree rank
		plugin.db.i("rank " + rID + " of " + iMaxRank);

			if (rID < 1) 
			{
				// We are at lowest rank
				plugin.msg(sender, "Player " + FormatManager.formatPlayer(playerName) + " already at lowest rank!");
				return true;
			}
			if (plugin.config.isDefaultrankallworlds())
			{
				plugin.perms.rankRemoveGlobal(player, rank.getPermName()); // do it!
				
			} else
			{
				plugin.perms.rankRemove(player, rank.getPermName()); // do it!
			}

			Rank tempRank = ClassManager.getPrevRank(rank, rID);
			plugin.perms.rankAdd(player, tempRank.getPermName());
			plugin.db.i("Rank Down to" + tempRank.getDispName());
			plugin.msg(sender, "Player " + FormatManager.formatPlayer(playerName)
					+ " Rank Down to " + tempRank.getDispName());
			return true;
	}	
	/**
	 * This function provides the main ranking process - including error
	 * messages when stuff is not right. Parameter check must be done before.
	 * processes rank up / down 
	 * <pre>
	 * - check cooldown for player
	 * - get old rank 
	 * - calculate new rank
	 * - check money, exp, items for new rank 
	 * - eventually delete from old rank 
	 * - add to new rank
	 * </pre>
	 */
	public boolean rankUp(Player sender, Player player, String className, String world) 
	{
		plugin.db.i("ranking " + player.getName() + " : " +  className);

		plugin.db.i("CoolDown Check for " + player.getName());
		int cDown = pm.coolDownCheck(player);
		if (cDown > 0) 
		{
			plugin.msg(sender,
					"You have to wait " + ChatColor.RED + String.valueOf(cDown)
							+ ChatColor.WHITE + " seconds!");
			return true; // cooldown timer still counting => OUT!
		}
		plugin.db.i("cooldown check positive");

		if ((!plugin.perms.hasPerms(sender, "classranks.rankup", player.getWorld().getName()))) 
		{
			// if we either are no OP or try to rank down, but we may not
			plugin.msg(sender, "You don't have permission to rank up!");
			return true;
		}
		plugin.db.i("perm check successful");


		String playerName = player.getName(); // store the player we want to

				
//		Boolean self = true; // we want to change ourselves ... maybe

		// does player have a rank?
		Rank rank = null; // actual Rank
		if (hasClass(className, player, world))
		{
			rank = hasRank( className, player, world);
			if (rank == null) 
			{
				plugin.msg(player, "Player " + FormatManager.formatPlayer(playerName)
						+ " does not have a class !");
				return true;
			}
		} else
		{
			plugin.msg(sender, "Player " + FormatManager.formatPlayer(playerName)
					+ " not in class "+ className);
			return true;
		}

		plugin.db.i("rank check successful");

		String cDispName = rank.getDispName(); // actual Display rank name
		ChatColor c_Color = rank.getColor();   // actual Rank color
		Clazz oClass = rank.getSuperClass();   // actual Rank class

		int rID = ClassManager.getRankIndex(rank, oClass );	//  Index des Rank
		int iMaxRank = oClass.ranks.size() - 1; // Classes max tree rank
		plugin.db.i("rank " + rID + " of " + iMaxRank);
		// placeholder: cost
		double rank_cost = 0;
		
			// we want to rank up!
			if (rID >= iMaxRank)
			{
				plugin.msg(sender, "Player " + FormatManager.formatPlayer(playerName)
						+ " is in MaxRank of class "+ className);
				return true;
			}
			// if we rank ourselves, that might cost money
			// (otherwise, thats not the players problem ;) )
			try 
			{
				rank_cost = moneyCost[rID + 1];
			} catch (Exception e) 
			{
				ClassRanks.log("cost not set: "+(rID + 1), Level.WARNING);
				rank_cost = 0;
			}
			
			Rank tempRank =  ClassManager.getNextRank(rank, rID);  // New Rank
//			if (rNext != null) 
//			{
//				if (rNext.getCost() != -1337D) 
//				{
//					rank_cost = rNext.getCost();
//				}
//			}

			// Check for money to rank up
			// rankCosts stored in checkedCost
			if (checkCost(sender, player , tempRank, rID+1)== false)
			{
				// not enough money message send to player and to promoter
				plugin.msg(player,"You don't have enough money to choose your class! ("
				+ ClassRanks.economy.format(checkedCost)
				+"/ "+ClassRanks.economy.getBalance(player.getName())
				+ ")");
				plugin.msg(sender,"The player don't have enough money ! ("
				+ ClassRanks.economy.format(checkedCost)
				+"/ "+ClassRanks.economy.getBalance(player.getName())
				+ ")");
				return true;
				
			} 
			plugin.db.i("money check successful");

			// check for experience to rank up
			// expcost stored in checkedExp
			if (checkExp(sender, player, tempRank, rID+1) == false)
			{
				// not enough exp message send to player and to promoter
				plugin.msg(player,"You don't have enough experience! You need "+ checkedExp);
				plugin.msg(sender,"Player don't have enough experience! You need "+ checkedExp);
				return true;
			}
			plugin.db.i("Exp check done");

			// check items for rank up
			if (!checkitems(sender, player , tempRank, rID+1))
			{
				// not enough items message send to player and to promoter
				plugin.msg(player,"You don't have enough items!");
				plugin.msg(sender,"Player don't have enough items!");
				return true;
			}
			
			plugin.db.i("item check done");

//			cPermName = tempRank.getPermName(); // Display new rank name
			cDispName = tempRank.getDispName(); // Display new rank name
			c_Color = tempRank.getColor(); // new Rank color

			// success!

			// Do Cost and Addrank 
			if (checkedCost > 0)
			{
				ClassRanks.economy.withdrawPlayer(player.getName(), checkedCost);
				plugin.msg(player, "You paid " + checkedCost + " money !");
				plugin.msg(sender, "Player paid " + checkedCost + " money !");
			}
			
			if (checkedExp > 0)
			{
				player.setTotalExperience(player.getTotalExperience() - checkedExp);
				plugin.msg(player, "You paid " + checkedExp + " experience points!");
				plugin.msg(player, "Player paid " + checkedExp + " experience points!");
			}

			if (plugin.config.isCheckitems())
			{
				pm.takeItems(player, checkedItems);
				plugin.msg(player, "You paid the required items!");
				plugin.msg(player, "Player paid the required items!");
			}

			plugin.perms.rankRemove(player, rank.getPermName()); // do it!

			plugin.db.i(">>>> adding new rank...");
			plugin.perms.rankAdd(player, tempRank.getPermName());

			if ((rank_cost > 0)) 
			{
				// take the money, inform the player
				String text = 
				ChatColor.DARK_GREEN + "[" + ChatColor.WHITE
						+ "Money" + ChatColor.DARK_GREEN + "] " + ChatColor.RED
						+ "Your account had " + ChatColor.WHITE
						+ ClassRanks.economy.format(rank_cost) + ChatColor.RED
						+ " debited.";

				if (ClassRanks.economy != null) 
				{
					ClassRanks.economy.withdrawPlayer(player.getName(), rank_cost);
					sender.sendMessage(text);
				} else if (plugin.method != null) 
				{
					MethodAccount ma = plugin.method.getAccount(player.getName());
					ma.subtract(rank_cost);
					sender.sendMessage(text);
				} else
				{
					sender.sendMessage("NO Economy found!");
				}
			}

			if (plugin.config.isRankpublic()) 
			{
				// we rank ourselves and do NOT want that to be public
				plugin.msg(sender,
						" Player " + FormatManager.formatPlayer(playerName)
						+ " now a " + c_Color + cDispName + ChatColor.WHITE
						+ ChatColor.WHITE + " in " + FormatManager.formatWorld(world)
						+ "!");
				plugin.msg(player,
						"You are now a " + c_Color + cDispName + ChatColor.WHITE
								+ ChatColor.WHITE + " in " + FormatManager.formatWorld(world)
								+ "!");
			} else 
			{
				plugin.getServer().broadcastMessage(
						"[" + ChatColor.AQUA + plugin.getConfig().getString("prefix") + ChatColor.WHITE
								+ "] Player " + FormatManager.formatPlayer(playerName)
								+ " now is a " + c_Color + cDispName
								+ ChatColor.WHITE + " in " + FormatManager.formatWorld(world)
								+ "!");
			}

		return true;
	}

	/**
	 * Send command Info to player
	 * @param pPlayer
	 */
	private void cmdCommandInfo (Player pPlayer)
	{
		plugin.msg(pPlayer, ChatColor.GREEN+"ClassRanks Ver: " + plugin.getDescription().getVersion());
		plugin.msg(pPlayer, ChatColor.YELLOW+"----------------------------------------");
		plugin.msg(pPlayer, ChatColor.YELLOW+"usage: [] = required  {} = optional ");
		plugin.msg(pPlayer, ChatColor.RED+"/class [classname] | the command is deleted !");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class LIST {classname} | list of class/ranks");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class CONGFIG | list the configuration");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class GROUPS {playername} | list the groups of user");
		plugin.msg(pPlayer, ChatColor.GOLD+"need permissions <classranks.admin>");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class ADD [playername] [classname] {world} | Add user to a class");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class REMOVE [playername] [classname] {world} | Remove user from a class");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class RANKUP [playername] {world} | Rank user up");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class RANKDOWN  [playername] {world} | Rank user down");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class GET [playername] {world} | Show rank of user");
		
	}

	/**
	 * Send command Info for /rank to player
	 * @param pPlayer
	 */
	private void cmdRankInfo (Player pPlayer)
	{
		plugin.msg(pPlayer, "Plugin ClassRanks Ver: " + plugin.getDescription().getVersion());
		plugin.msg(pPlayer, ChatColor.YELLOW+"-----------------------------------------");
		plugin.msg(pPlayer, ChatColor.YELLOW+"usage: [] = required  {} = optional ");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/rank [classname] | Choose the class for yourself");
		plugin.msg(pPlayer, ChatColor.YELLOW+"need permissions <classranks.self.rank>");
		
	}

	/**
	 * Send command Info for /rankup to player
	 * @param pPlayer
	 */
	private void cmdRankUpInfo (Player pPlayer)
	{
		plugin.msg(pPlayer, "Plugin ClassRanks Ver: " + plugin.getDescription().getVersion());
		plugin.msg(pPlayer, ChatColor.YELLOW+"-----------------------------------------");
		plugin.msg(pPlayer, ChatColor.YELLOW+"usage: [] = required  {} = optional ");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/rankup [classname] | rankup for yourself");
		plugin.msg(pPlayer, ChatColor.YELLOW+"need permissions <classranks.self.rank>");
		
	}

	/**
	 * Send command Info for /rank to player
	 * @param pPlayer
	 */
	private void cmdRankDownInfo (Player pPlayer)
	{
		plugin.msg(pPlayer, "Plugin ClassRanks Ver: " + plugin.getDescription().getVersion());
		plugin.msg(pPlayer, ChatColor.YELLOW+"-----------------------------------------");
		plugin.msg(pPlayer, ChatColor.YELLOW+"usage: [] = required  {} = optional ");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/rankdown [classname] | rankdown for yourself");
		plugin.msg(pPlayer, ChatColor.YELLOW+"need permissions <classranks.self.rank>");
		
	}
	
	/**
	 * printout Classas and/or Clazz Ranks
	 * 
	 * @param pPlayer
	 * @param args
	 */
	private void cmdList (Player pPlayer, String[] args) 
	{
	
		ArrayList<Clazz> classes = ClassManager.getClasses();
		if (args.length > 1) 
		{
			for (Clazz c : classes) 
			{
				if (c.name.equalsIgnoreCase(args[1]) ) 
				{
					plugin.msg(pPlayer, "[ClassRanks] "+ChatColor.YELLOW+"Clazz "+ ChatColor.GREEN + c.name);
					plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------");
					for (Rank r : c.ranks) 
					{
						plugin.msg(pPlayer, "=> " + r.getColor() + r.getDispName());
					}
				}
			}
		} else
		{
			
			plugin.msg(pPlayer, "[ClassRanks] "+ChatColor.YELLOW+"Clazz List");
			plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------");
			
			for (Clazz c : classes) 
			{
				if (c.name.startsWith("%") && !pPlayer.isOp()) 
				{
					continue;
				}
				plugin.msg(pPlayer, "Clazz "+ ChatColor.GREEN + c.name);
			}
		}
		
	}

	/**
	 * printout player Clazz, Rank and world
	 * 
	 * @param pPlayer
	 * @param args
	 */
	private void cmdGet (Player pPlayer, String[] args) 
	{
		String world = pPlayer.getWorld().getName();
		if (args.length > 2) 
		{
			world = args[2];
		} else {
			world = "all";
		}
		String playerName = pPlayer.getName(); 
		if (args.length > 1) 
		{
			playerName = args[1];
		}

		plugin.msg(pPlayer, "Player " + FormatManager.formatPlayer(playerName)+ " Get in " + FormatManager.formatWorld(world) + "!");
		Rank rank = ClassManager.getRankByPermName(plugin.perms.getPlayerGroups(pPlayer));

		if (rank == null) 
		{
			plugin.msg(pPlayer, "Player " + playerName
					+ " has no class in " + FormatManager.formatWorld(world) + "!");
		} else 
		{

			String cDispName = rank.getDispName(); // Display rank name
			ChatColor c_Color = rank.getColor(); // Rank color

			plugin.msg(pPlayer, "" + playerName
					+ " is in " + c_Color + rank.getSuperClass().name + ChatColor.WHITE
					+ " as " + c_Color + cDispName + ChatColor.WHITE
					+ " in " + FormatManager.formatWorld(world) + "!");
		}
		
	}
	
	private void cmdConfig (Player pPlayer, String[] args) 
	{
		pPlayer.sendMessage(ChatColor.YELLOW+"[ClassRanks] "+ChatColor.GREEN+"Configuration ");
		pPlayer.sendMessage(ChatColor.YELLOW+"----------"+"Ver: " + ChatColor.GREEN+plugin.getDescription().getVersion());
		pPlayer.sendMessage(ChatColor.YELLOW+"OneClass  : "+ChatColor.GREEN+plugin.config.isOnlyoneclass());
		pPlayer.sendMessage(ChatColor.YELLOW+"CheckExp  :  "+ChatColor.GREEN+plugin.config.isCheckexp());
		pPlayer.sendMessage(ChatColor.YELLOW+"CheckItem :  "+ChatColor.GREEN+plugin.config.isCheckitems());
		pPlayer.sendMessage(ChatColor.YELLOW+"CheckMoney:  "+ChatColor.GREEN+plugin.config.isCheckprices());
		pPlayer.sendMessage(ChatColor.YELLOW+"CoolDown: "+ChatColor.GREEN+plugin.config.getCoolDown());
		pPlayer.sendMessage(ChatColor.YELLOW+"ClearRanks:  "+ChatColor.GREEN+plugin.config.isClearranks());
		pPlayer.sendMessage(ChatColor.YELLOW+"DefaultRankWorld: "+ChatColor.GREEN+plugin.config.isDefaultrankallworlds());
		pPlayer.sendMessage(ChatColor.YELLOW+"RankPublic:  "+ChatColor.GREEN+plugin.config.isRankpublic());
		plugin.msg(pPlayer, ChatColor.YELLOW+" ");
	}
	
	private void cmdUserGroups (Player pPlayer, String[] args)
	{
		String playerName = pPlayer.getName();
		// is there a player parameter, get it
		if (args.length > 1)
		{
			playerName = args[1];
		} else
		{
			playerName = pPlayer.getName();
		}
		String[] list = plugin.perms.getPlayerGroups(pPlayer);
		if (list != null) 
		{
			int max = list.length;
			plugin.msg(pPlayer, "[ClassRanks] "+ChatColor.YELLOW+"Clazz "+ "User Groups of "+ChatColor.DARK_GREEN+playerName);
			plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------------------");
			for (int i = 0; i < list.length; i++) 
			{
				plugin.msg(pPlayer, ChatColor.YELLOW+": "+list[i]);
			}
			plugin.msg(pPlayer, ChatColor.YELLOW+"Count "+max+" ------------");
		}
		
	}

	private boolean hasAdminPerms(Player player)
	{
		if (player.isOp())
		{
			return true;
		}
		if (plugin.perms.hasPerms(player,"classranks.admin.addremove", player.getWorld().getName())) 
		{
			return true;
		}
		// only if we are NOT op or may NOT add/remove classes
		return false;
	}

	/**
	 * 
	 * @param className
	 * @param playerName
	 * @param world
	 * @return
	 */
	private Rank hasRank(String className, Player player, String world)
	{
		ArrayList<Clazz> classes = ClassManager.getClasses();
		String sPermName = "";
		Rank isRank = null;
		for (Clazz oClass : classes) 
		{
		    //  check  Classname
		    if (className.equalsIgnoreCase(oClass.name))
		    {
		    	plugin.db.i("Found : " + oClass.name);
//		    	ArrayList<Rank> ranks = oClass.ranks;
		    	// check for each  Rank the permName in permissionsgroups
				for (Rank rank : oClass.ranks) 
				{
				    sPermName = rank.getPermName();
			    	plugin.db.i("Search Perm : " + sPermName);
				    // pruefe ob Permname als group vorhanden ist
				    // wenn gefunden ,  setze reult rank 
				    if (hasPlayerGroup(sPermName, player))
				    {
				    	isRank = rank;
				    	return rank;
				    }
				}

		    }
		}
		return isRank;
	}
	

	private void removeRanks(String className, Player player, String world)
	{
		ArrayList<Clazz> classes = ClassManager.getClasses();
		String sPermName = "";
		for (Clazz oClass : classes) 
		{
		    //  check  Classname
		    if (className.equalsIgnoreCase(oClass.name))
		    {
		    	plugin.db.i("Found : " + oClass.name);
				for (Rank rank : oClass.ranks) 
				{
				    sPermName = rank.getPermName();
			    	plugin.db.i("Search Perm : " + sPermName);
				    // pruefe ob Permname als group vorhanden ist
				    // wenn gefunden ,  remove groups 
				    if (hasPlayerGroup(sPermName, player))
				    {
				    	if (plugin.config.isDefaultrankallworlds())
				    	{
				    		plugin.perms.rankRemoveGlobal(player, sPermName);
				    	} else
				    	{
				    		plugin.perms.rankRemove(player, sPermName);
				    	}
				    }
				}

		    }
		}
	}
	
	
	private boolean hasPlayerGroup(String PermName, Player player)
	{
		plugin.db.i("Check group : " + PermName);
		String[] list = plugin.perms.getPlayerGroups(player);
		boolean isPerm = false;
		for (int i = 0; i < list.length; i++) 
		{
			plugin.db.i("Check " + i +  " : " + list[i]);
			if(list[i].equalsIgnoreCase(PermName))
			{
				isPerm = true;
			}
		}
		return isPerm;
	}
	
	/**
	 * Sucht das Clazz Array nach dem Classnamen
	 * und sucht die Group zu den Raengen
	 * TRUE , wenn Group gesetzt
	 * 
	 * @param className
	 * @param playerName
	 * @param world
	 * @return
	 */
	private boolean hasClass(String className, Player player, String world)
	{
		ArrayList<Clazz> classes = ClassManager.getClasses();
		String sPermName = "";
		boolean isClass = false;
    	plugin.db.i("SearchClass : " + className);
		for (Clazz oClass : classes) 
		{
		    //  pruefe ob KLassenname identisch 
		    if (className.equalsIgnoreCase(oClass.name))
		    {
		    	plugin.db.i("Found : " + oClass.name);
//				ArrayList<Rank> ranks = oClass.getRanks();
				for (Rank rank : oClass.ranks) 
				{
				    sPermName = rank.getPermName();
			    	plugin.db.i("Search Perm : " + sPermName);
				    // pruefe ob Permname als group vorhanden ist
				    // wenn gefunden ,  setze result
				    if (hasPlayerGroup(sPermName, player))
				    {
				    	plugin.db.i("HasPerm : " + sPermName);
				    	isClass = true;
				    	return true;
				    }
				}

		    }
		}
		return isClass;
	}

	
	/**
	 * Durchsucht die ClassList nach dem ClassNamen
	 * @param ClassRank
	 * @return
	 */
	private boolean existClass( String className)
	{
		ArrayList<Clazz> classes = ClassManager.getClasses();
		for (Clazz oClass : classes) 
		{
		    //  pruefe ob KLassenname identisch 
		    if (className.equalsIgnoreCase(oClass.name))
		    {
		    	return true;
		    }
		}
		return false;
	}
	
	
//	private void cmdTemp (Player pPlayer, String[] args) {
//		
//	}
	/**
	 * Remove Player from Rank
	 * Args[0] = Subcommand
	 * Args[1] = Playername
	 * Args[2] = Classname
	 * Args[3] = Worldname default = all  
	 * 
	 * @param sender
	 * @param player
	 * @param args
	 * @param world
	 * @return
	 */
	private boolean cmdRemove(Player sender,  String[] args)
	{
		String world = "";
		String className = "";
		String playerName  = "";
		// es müssen mindestens  3 Argumente vorhanden sein
		if (args.length < 3) 
		{
			// not enough arguments ;)
			plugin.msg(sender,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
			plugin.msg(sender,"/class [remove] [playername] [classname] {world} | Remove user from a class");

			return false;
		} else
		{
			playerName = args[1];
			className  = args[2];
			// Args[4] = world
			if (args.length > 3)
			{
				world =  args[3];
			}
		}
		Player player = plugin.getServer().getPlayer(playerName);

		if (plugin.trackRanks) 
		{
			ClassManager.saveClassProgress(player);
		}
		
		if (hasClass(className, player, world))
		{
			
//			Rank rank = hasRank( className, player, world);
//			if (rank == null) 
//			{
//				plugin.msg(sender, "Player " + FormatManager.formatPlayer(playerName)
//						+ " does not have class : " + className);
//				return true;
//			}
//			plugin.perms.rankRemove(world, player, rank.getPermName()); // do it!
			removeRanks(className, player, world);
			System.out.println("debug :  remove ready");
			
			if (plugin.config.isRankpublic() ) 
			{
				// self remove successful!
				plugin.getServer().broadcastMessage(player+
							" are removed from rank " + ChatColor.RED
							+ className + ChatColor.WHITE
							+ " in "
							+ FormatManager.formatWorld(world) + "!");
			} else
			{
				// self remove successful!
				plugin.msg(player,"You are removed from rank " + ChatColor.RED
							+ className + ChatColor.WHITE
							+ " in "
							+ FormatManager.formatWorld(world) + "!");
				if (!(sender == player))
				{
					// self remove successful!
					plugin.msg(sender,playerName+" are removed from rank " + ChatColor.RED
								+ className + ChatColor.WHITE
								+ " in "
								+ FormatManager.formatWorld(world) + "!");
				}
			}
		}
		return true;
	}
	
	/**
	 * Check the Rank cost with default value and individual value
	 * @param sender , command sneder 
	 * @param player , ranked player
	 * @param rank	, new rank
	 * @param rID   , index of rank in Clazz
	 * @return  true = cost available or not relevant / false = not enough money
	 */
	private boolean checkCost(Player sender, Player player , Rank rank, int rID)
	{
		plugin.db.i("Get Rank Cost");
		double rank_cost = 0; 	// tempvalue
	
		// get default cost from Config table
		rank_cost =  plugin.config.getMoneyCost(rID);
		
		// get individual rank cost
		if (rank.getCost() != -1337D) 
		{
			rank_cost = rank.getCost();
		}
		plugin.db.i("Get Rank Cost : " + rank_cost);
		
		// Check if the player has got the money
		// check loaded economy 
		plugin.db.i("Check economy");
		if (ClassRanks.economy != null) 
		{
			plugin.db.i("Economy found : " + ClassRanks.economy.getName() );
			plugin.db.i("isCheckCost : " + plugin.config.isCheckprices());
			// should Cost be checked 
			if (plugin.config.isCheckprices()) 
			{
				if (ClassRanks.economy.getBalance(player.getName()) < rank_cost)
				{
					plugin.db.i("money check NOT successful");
					checkedCost = rank_cost;
					return false;
				} else
				{
					plugin.db.i("money check NOT successful");
					checkedCost = rank_cost;
					return true;
				}
			} else
			{
				plugin.db.i("money check successful");
				checkedCost = rank_cost;
				return true;
			}
		} else if (plugin.method != null) 
		{
			plugin.db.i("No economy found, use Method : " + plugin.method.getName());
			plugin.db.i("isCheckCost" + plugin.config.isCheckprices());
			// should cost be checked 
			if (plugin.config.isCheckprices())
			{
				MethodAccount ma = plugin.method.getAccount(player.getName());
				if (!ma.hasEnough(rank_cost)) 
				{
					checkedCost = rank_cost;
					return false;
				} else
				{
					plugin.db.i("money check NOT successful");
					checkedCost = rank_cost;
					return false;
				}
			} else
			{
				plugin.db.i("money check successful");
				checkedCost = rank_cost;
				return true;
			}
		} else
		{
			plugin.db.i("money check done, NO economy");
			checkedCost = 0.0;
			return true;
		}
	}
	
	
	private boolean checkExp(Player sender, Player player , Rank rank, int rID)
	{
		int exp_cost = -1;
		// get default exp cost from config
		exp_cost = plugin.config.getExpCost(rID);
		
		// get individual expCost from rank
		if (rank.getExp() > 0)
		{
			exp_cost = rank.getExp();
		}
		plugin.db.i("Get ExpCost : " + exp_cost);
		
		plugin.db.i("isCheckExp : " + plugin.config.isCheckexp());
		if (plugin.config.isCheckexp()) 
		{
			if (player.getTotalExperience() < exp_cost) {
				plugin.msg(player,
						"You don't have enough experience! You need "
								+ exp_cost);
				checkedExp = exp_cost;
				return false;
			} else
			{
				plugin.db.i("exp check successful");
				checkedExp = exp_cost;
				return true;
			}
		} else
		{
			plugin.db.i("exp check not relevant");
			checkedExp = 0;
			return true;
		}
	}
		
	/**
	 * Check the default and rank items against player inventory
	 *  
	 * @param sender   , promoter
	 * @param player   , ranked player
	 * @param rank     , new Rank
	 * @param rID      , Index actual Rank 
	 * @return  true = items available, not relevant, false = items not available
	 */
	private boolean checkitems(Player sender, Player player , Rank rank, int rID)
	{

		ItemStack[] items = null;
		ItemStack[][] rankItems = plugin.config.getRankItems(); // per rank a list of items 
		
		items = rankItems[rID];

		if (rank.getItemstacks() != null) 
		{
			items = rank.getItemstacks();
		}
		
		if (plugin.config.isCheckitems())
		{
			plugin.db.i("Get Item Cost : " + items.length);
			plugin.db.i("isCheckItems : " + plugin.config.isCheckitems());
			plugin.db.i("item check start");
			plugin.db.i("Player "+player.getName());
			if (pm.hasItems(player, items) == false) 
			{
				return false;
			} else
			{
				plugin.db.i("Items found ");
				checkedItems = items;
				return true;
				
			}
		} else
		{
			plugin.db.i("No Items found !");
			plugin.db.i("item check NOT relevant");
			checkedItems = null;
			return true;
		}
	
	}
	
	/**
	 * Add Clazz d first Rank to player
     * /class [add]     [playername] [classname] {world} | Add user to a class
	 * 
	 * @param sender	of the Command
	 * @param player	to promot ein class
	 * @param className 
	 * @param world
	 * @return
	 */
	private boolean cmdAddRank(Player sender, Player player, String className, String world)
	{
		///  ADD 
		//  Check for Rank is always given
		plugin.db.i("Check hasClass");
		if (hasClass(className, player, world))
		{
			plugin.error(sender,
							"Player " + FormatManager.formatPlayer(player.getName())
							+ " already has the Clazz "
							+ ChatColor.RED + className + "!");
			plugin.msg(sender,"Use  /class [rankup]  [playername] {world} | Rank user up");
			return true;
		}
		
		// ADD
		plugin.db.i("Get TempRank");
		Rank tempRank = ClassManager.getRankByPermName( ClassManager.getFirstPermNameByClassName(className));
		
		//  ClassRank does not exist
		if (tempRank == null) 
		{
			plugin.error(sender,"The class you entered does not exist!"+className);
			return true;
		}


		int rID = ClassManager.getRankIndex(tempRank, tempRank.getSuperClass());	//  Index des Rank
		// Track Rank 
//		if (plugin.trackRanks) 
//		{
////			rID = ClassManager.loadClassProcess(player,
////					tempRank.getSuperClass());
//		}
		plugin.db.i("Get Rank Index : " + rID);

		// Check for money to rank up
		// rankCosts stored in checkedCost
		if (checkCost(sender, player , tempRank, rID) == false)
		{
			// not enough money message send to player and to promoter
			plugin.error(player,"You don't have enough money to choose your class! ("
			+ ClassRanks.economy.format(checkedCost)
			+"/ "+ClassRanks.economy.getBalance(player.getName())
			+ ")");
			plugin.error(sender,"The player don't have enough money ! ("
			+ ClassRanks.economy.format(checkedCost)
			+"/ "+ClassRanks.economy.getBalance(player.getName())
			+ ")");
			return true;
			
		} 
		plugin.db.i("money check successful");

		// check for experience to rank up
		// expcost stored in checkedExp
		if (checkExp(sender, player, tempRank, rID) == false)
		{
			// not enough exp message send to player and to promoter
			plugin.error(player,"You don't have enough experience! You need "+ checkedExp);
			plugin.error(sender,"Player don't have enough experience! You need "+ checkedExp);
			return true;
		}
		plugin.db.i("Exp check done");

		// check items for rank up
		if (checkitems(sender, player , tempRank, rID) == false)
		{
			// not enough items message send to player and to promoter
			plugin.error(player,"You don't have enough items!");
			plugin.error(sender,"Player don't have enough items!");
			return true;
		}
		
		plugin.db.i("item check done");

		String cPermName = tempRank.getPermName(); // Display rank name
		String cDispName = tempRank.getDispName(); // Display rank name
		ChatColor c_Color = tempRank.getColor(); // Rank color

		// success!

		// Do Cost and Addrank 
		if (checkedCost > 0)
		{
			ClassRanks.economy.withdrawPlayer(player.getName(), checkedCost);
			plugin.msg(player, "You paid " + checkedCost + " money !");
			plugin.msg(sender, "Player paid " + checkedCost + " money !");
		}
		
		if (checkedExp > 0)
		{
			player.setTotalExperience(player.getTotalExperience() - checkedExp);
			plugin.msg(player, "You paid " + checkedExp + " experience points!");
			plugin.msg(player, "Player paid " + checkedExp + " experience points!");
		}

		if (plugin.config.isCheckitems())
		{
			pm.takeItems(player, checkedItems);
			plugin.msg(player, "You paid the required items!");
			plugin.msg(player, "Player paid the required items!");
		}
		
		// remove oldRank
		plugin.db.i("isClearRanks : " + plugin.config.isClearranks());
		if (plugin.config.isClearranks()) 
		{
			plugin.perms.removeGroups(player);
			plugin.error(sender, "The old perms are removed !");
			plugin.db.i("Remove Groups from Permission");
		}

//		world = plugin.config.isDefaultrankallworlds() ? "all" : player.getWorld().getName();
		if (plugin.config.isDefaultrankallworlds())
		{
			plugin.perms.rankAddGlobal(player, cPermName);
		} else
		{
			plugin.perms.rankAdd(player, cPermName);
		}
		plugin.db.i("Added Group to Permission");
		plugin.msg(sender,"Added Perms to Group "+cPermName);
		
		if (plugin.config.isRankpublic() ) 
		{
			plugin.getServer().broadcastMessage(
					"[" + ChatColor.AQUA + plugin.getConfig().getString("prefix") + ChatColor.WHITE
							+ "] " + FormatManager.formatPlayer(player.getName())
							+ " now is a " + c_Color + cDispName);
			return true;
		} else 
		{
			plugin.msg(player, "You now are in Class " + c_Color + cDispName);
			return true;
		}
//END ADD / REMOVE			
	}

	/**
	 * Prueft die Command Parameter auf World
	 * @param args
	 * @return
	 */
	private boolean isWorld (String[] args)
	{
		if (args.length > 3) 
		{
			return true;
		} else
		{
			return false;
		}
	}
	
	/**
	 * Setzt World Parameter oder default wert "all"
	 * @param world
	 * @param args
	 * @return
	 */
	private String getWorldArg(String world, String[] args)
	{
		
		if (isWorld(args)) 
		{
			// => /class add *player* *classname* world
			return args[3];
		} else
		{
			if (plugin.config.isDefaultrankallworlds())
			{
				return "all";
			} else
			{
				return world;
			}
			
		}
	}
	


	
	/**
	 * The main switch for the command usage. Check for known commands,
	 * permissions, arguments, and commit whatever is wanted.
	 * 
	 * <pre>
     /class [classname] | Choose the class [classname] for yourself
     /class [config]  | list the configuration
     /class [list] {classname} | list of class/ranks
     /class [groups]  {playername} | list the groups of user
      
     /class [add]     [playername] [classname] {world} | Add user to a class
     /class [remove]  [playername] [classname] {world} | Remove user from a class

     /class [rankup]  [playername] {world} | Rank user up
     /class [rankdown][playername] {world} | Rank user down
     /class [get]     [playername] {world} | Show rank of user
     </pre>
	 * @param pPlayer , the sender of the command
	 * @param args , arguments of the Command /class
	 * @return
	 */
	public boolean parseCommand(Player pPlayer, String[] args) 
	{
//		Player player = pPlayer;
		plugin.db.i("parsing player " + pPlayer + ", command: " + FormatManager.formatStringArray(args));

		boolean isSelf = plugin.perms.hasPerms(pPlayer, "classranks.self.rank", pPlayer.getWorld().getName()); 
		
		if (pPlayer.isOp())
		{
			isSelf = true;
		}

		if (args.length == 0)
		{
			//zeigt command description aus Plugi.yml
			cmdCommandInfo (pPlayer);
			return true;  
		} 

		// LIST
		if (args[0].equalsIgnoreCase("list")) {
			// Command list of Ranks in the Clazz  3.2
			cmdList(pPlayer,args);
			return true;
		} 
		
		if (args[0].equalsIgnoreCase("info")) 
		{
			plugin.msg(pPlayer,"Not implementedt yet !");
		}
		// GET
		if (args[0].equalsIgnoreCase("get")) {
			// Command Get   3.2
			cmdGet(pPlayer, args);
			return true;
		}
		// GROUPS
		if (args[0].equalsIgnoreCase("groups")) {
			// Command list of Ranks in the Clazz  3.2
			cmdUserGroups(pPlayer, args);
			return true;
		}
		
		//RANKUP 
		if ((args[0].equalsIgnoreCase("rankup")))
		{
			if (hasAdminPerms(pPlayer))
			{
				// rank up or down
				if (args.length > 2) 
				{
					String playerName = args[1];
					Player player = PlayerManager.searchPlayerName(playerName);
					if ((player == null))
					{
						plugin.msg(pPlayer, "Not found player " + playerName);
						return true;
					}
					String world = getWorldArg(pPlayer.getWorld().getName(), args); // store
					String className = args[2];
					
					return rankUp(pPlayer, player, className, world);
				} else 
				{
					plugin.msg(pPlayer,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
					plugin.msg(pPlayer," /class [rankup] [playername] [classname] {world} | Rank user up");
					return true;
				}
			} else 
			{
				plugin.msg(pPlayer, "You don't have the right permission !");
			}
		}

		//RANKDOWN 
		if ((args[0].equalsIgnoreCase("rankdown"))) 
		{
			if (hasAdminPerms(pPlayer))
			{
				// rank up or down
				if (args.length > 2) 
				{
					String playerName = args[1];
					Player player = PlayerManager.searchPlayerName(playerName);
					if ((player == null))
					{
						plugin.msg(pPlayer, "Not found player " + playerName);
						return true;
					}
					String world = getWorldArg(pPlayer.getWorld().getName(), args); // store
					String className = args[2];
					
					return rankDown(pPlayer, player, className, world);
				} else {
					plugin.msg(pPlayer,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
					plugin.msg(pPlayer," /class [rankdown] [playername] [classname] {world} | Rank user down");
					return true;
				}

			} else 
			{
				plugin.msg(pPlayer, "You don't have the right permission !");
			}

		}
		
		// preset of World name
//		String world = player.getWorld().getName();
//		String PlayerName = getPlayerName(pPlayer, args);
		
		if (args[0].equalsIgnoreCase("remove")) 
		{
			if (hasAdminPerms(pPlayer))
			{
				cmdRemove(pPlayer, args);
			} else 
			{
				plugin.msg(pPlayer, "You don't have the right permission !");
				return true;
			}

		}

		if ((args[0].equalsIgnoreCase("add")))
		{
			String className = "";
			// es müssen mindestens  3 Argumente vorhanden sein
		    // class [add] [playername] [classname] {world} | Add user to a class
			if (args.length < 3) 
			{
				// not enough arguments ;)
				plugin.msg(pPlayer,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
				plugin.msg(pPlayer," /class [add] [playername] [classname] {world} | Add user to a class");
				return true;
			} else
			{
				Player player = null;
				String PlayerName = args[1];
				String world = "";
				if (hasAdminPerms(pPlayer))
				{
					plugin.db.i("perm check successful");
					// get the class of the player we're talking about
					// change PlayerObject
					player = PlayerManager.searchPlayerName(PlayerName);
					if ((player != null))
					{
						world  = player.getWorld().getName();
					} else
					{
						plugin.msg(pPlayer, "Not found player " + PlayerName);
						return true;
					}
				} else
				{
					plugin.msg(pPlayer, "You don't have the right permission !");
					return true;
				}
				className = args[2];

				//world name auswerten und setzen
				 world = getWorldArg(world, args);
				 plugin.db.i("precheck successful");
				// Add Rank
				cmdAddRank(pPlayer,player,className, world);
				return true;
			}
		}

		//---------------------------------------------------------------------------
		// Simple Commands
		//
		if (args[0].equalsIgnoreCase("config")) 
		{
			if(pPlayer.isOp())
			{
				// Command list of Ranks in the Clazz  3.2
				cmdConfig(pPlayer, args);
				return true;
			} else 
			{
				plugin.msg(pPlayer, "You don't have the right permission !");
				return true;
			}
		}

		if (args[0].equalsIgnoreCase("get")) 
		{
			// Command Get   3.2
			cmdGet(pPlayer, args);
			return true;
		}

		
		if (args[0].equalsIgnoreCase("reload")) 
		{
			// Command reload 
			if (pPlayer.isOp()) {
				plugin.config.load_config();
				plugin.msg(pPlayer, "Config reloaded!");
			} else 
			{
				plugin.msg(pPlayer, "You don't have the right permission !");
				return true;
			}
		}
		
		// /class [classname]
		// /class [classname] | Choose the class [classname] for yourself in all worlds

		// command wird nur ausgefuehrt bei permissions
		if (isSelf == false) 
		{
			plugin.msg(pPlayer,"You don't have permission to choose your class!");
			return true;
		} else
		{
			String className = args[0];
			if (existClass(className))
			{
				String world = "all";
				cmdAddRank(pPlayer, pPlayer, className, world);
				return true;
			} else
			{
				plugin.msg(pPlayer,"The class name not exist!");
				plugin.msg(pPlayer,"/class [classname] | Choose the class [classname] for yourself in all worlds");
				return true;
			}
		}
		
	}

	public boolean parseSelfRank(Player pPlayer, String[] args) 
	{
		if (args.length == 0)
		{
			//zeigt command description aus Plugi.yml
			cmdRankInfo (pPlayer);
			return true;  
		} 
		if (plugin.perms.hasPerms(pPlayer, "classranks.self.rank", pPlayer.getWorld().getName()))
		{
			plugin.msg(pPlayer, "You don't have the right permission !");
			return true;
		}
		String className = args[0];
	    // rank  [classname] | Add yourself to a class
		String world = "";
		
		//world name auswerten und setzen
		world = getWorldArg(world, args);
		plugin.db.i("precheck successful");
		// Add Rank
		cmdAddRank(pPlayer,pPlayer,className, world);
		return true;
	
	}

	public boolean parseSelfRankUp(Player pPlayer, String[] args) 
	{
		if (args.length == 0)
		{
			//zeigt command description aus Plugi.yml
			cmdRankUpInfo (pPlayer);
			return true;  
		} 
		String className = args[0];
	    // rank  [classname] | Add yourself to a class
		String world = "";
		
		//world name auswerten und setzen
		world = getWorldArg(world, args);
		plugin.db.i("precheck successful");
		// Add Rank
		
		rankUp(pPlayer, pPlayer, className, world);
		return true;
	}

	public boolean parseSelfRankDown(Player pPlayer, String[] args) 
	{
		if (args.length == 0)
		{
			//zeigt command description aus Plugi.yml
			cmdRankDownInfo (pPlayer);
			return true;  
		} 
		String className = args[0];
	    // rank  [classname] | Add yourself to a class
		String world = "";
		
		//world name auswerten und setzen
		world = getWorldArg(world, args);
		plugin.db.i("precheck successful");
		// Add Rank
		
		rankDown(pPlayer, pPlayer, className, world);
	
		return true;
	}
	


}
