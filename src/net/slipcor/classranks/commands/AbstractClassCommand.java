package net.slipcor.classranks.commands;

import java.util.ArrayList;
import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Clazz;
import net.slipcor.classranks.core.Rank;
import net.slipcor.classranks.managers.ClazzList;
import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.managers.FormatManager;
import net.slipcor.classranks.register.payment.Method.MethodAccount;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * class command class
 * 
 * @version v0.2.1
 * 
 * @author slipcor
 */

public abstract class AbstractClassCommand implements CommandExecutor 
{
	protected final ClassRanks plugin;
//	protected final PlayerCommands cmdMgr;
	protected final DebugManager db;

	protected ArrayList<Double> moneyCost = new ArrayList<Double>(); // Costs
	protected ArrayList<Integer> expCost = new ArrayList<Integer>(); // EXP
	
	protected Double checkedCost;		// checked cost for rank up / add
	protected int  checkedExp;		// checked Exp Cost for rank up / add
	protected ItemStack[] checkedItems;  // checked list of required items for rank up / add
	
	
	/**
	 * create a class command instance
	 * @param cr the plugin instance to hand over
	 * @param cm the command manager instance to hand over
	 */
	public AbstractClassCommand(ClassRanks plugin) 
	{
		this.plugin = plugin;
//		cmdMgr = plugin.getCommandMgr();
		db = plugin.getDebugManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
		
    	if (!(sender instanceof Player)) {
    		plugin.msg(sender, "Console access is not implemented. If you want that, visit:");
    		plugin.msg(sender, "http://dev.bukkit.org/server-mods/classranks/");
    		return true;
    	}
		// standard class command, parse it!
		db.i("/class detected! parsing...");
		return false; //cmdMgr.parseCommand((Player) sender, args);
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
	protected boolean cmdAddRank(Player sender, Player player, String className, String world)
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
		Rank tempRank = plugin.clazzList().getRankByPermName( plugin.clazzList().getFirstPermNameByClassName(className));
		
		//  ClassRank does not exist
		if (tempRank == null) 
		{
			plugin.error(sender,"The class you entered does not exist!"+className);
			return true;
		}


		int rID = plugin.clazzList().getRankIndex(tempRank, tempRank.getSuperClass());	//  Index des Rank
		// Track Rank 
//		if (plugin.trackRanks) 
//		{
////			rID = ClazzList.loadClassProcess(player,
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
			plugin.pm.takeItems(player, checkedItems);
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
		plugin.config.playerSectionWrite(player, className, cPermName);
		
		plugin.db.i("Added Class + Rank  to config");
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
	 * Send command Info for /rank to player
	 * @param pPlayer
	 */
	protected void cmdRankDownInfo (Player pPlayer)
	{
		plugin.msg(pPlayer, "Plugin ClassRanks Ver: " + plugin.getDescription().getVersion());
		plugin.msg(pPlayer, ChatColor.YELLOW+"-----------------------------------------");
		plugin.msg(pPlayer, ChatColor.YELLOW+"usage: [] = required  {} = optional ");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/rankdown [classname] | rankdown for yourself");
		plugin.msg(pPlayer, ChatColor.YELLOW+"need permissions <classranks.self.rank>");
		
	}
	

	/**
	 * Prueft die Command Parameter auf World
	 * @param args
	 * @return
	 */
	protected boolean isWorld (String[] args)
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
	protected String getWorldArg(String world, String[] args)
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
	
	
	protected boolean hasAdminPerms(Player player)
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

	protected String getClassName(String className)
	{
		for (Clazz oClass : plugin.clazzList().getClazzes().values()) 
		{
		    //  check  Classname
		    if (className.equalsIgnoreCase(oClass.clazzName))
		    {
		    	return oClass.clazzName;
		    }
		}
		return "";
	}
	
	/**
	 * 
	 * @param className
	 * @param playerName
	 * @param world
	 * @return
	 */
	protected Rank hasRank(String className, Player player, String world)
	{
		String sPermName = "";
		Rank isRank = null;
		for (Clazz oClass : plugin.clazzList().getClazzes().values()) 
		{
		    //  check  Classname
		    if (className.equalsIgnoreCase(oClass.clazzName))
		    {
		    	plugin.db.i("Found : " + oClass.clazzName);
//		    	ArrayList<Rank> ranks = oClass.ranks;
		    	// check for each  Rank the permName in permissionsgroups
				for (Rank rank : oClass.ranks.values()) 
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
	

	protected void removeRanks(String className, Player player, String world)
	{
		String sPermName = "";
		for (Clazz oClass : plugin.clazzList().getClazzes().values()) 
		{
		    //  check  Classname
		    if (className.equalsIgnoreCase(oClass.clazzName))
		    {
		    	plugin.db.i("Found : " + oClass.clazzName);
				for (Rank rank : oClass.ranks.values()) 
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
	
	
	protected boolean hasPlayerGroup(String PermName, Player player)
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
	protected boolean hasClass(String className, Player player, String world)
	{
		String sPermName = "";
		boolean isClass = false;
    	plugin.db.i("SearchClass : " + className);
		for (Clazz oClass : plugin.clazzList().getClazzes().values()) 
		{
		    //  pruefe ob KLassenname identisch 
		    if (className.equalsIgnoreCase(oClass.clazzName))
		    {
		    	plugin.db.i("Found : " + oClass.clazzName);
//				ArrayList<Rank> ranks = oClass.getRanks();
				for (Rank rank : oClass.ranks.values()) 
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
	 * @param className
	 * @return
	 */
	protected boolean existClass( String className)
	{
		for (Clazz oClass : plugin.clazzList().getClazzes().values()) 
		{
		    //  pruefe ob KLassenname identisch 
		    if (className.equalsIgnoreCase(oClass.clazzName))
		    {
		    	return true;
		    }
		}
		return false;
	}
	
	
	
	/**
	 * Check the Rank cost with default value and individual value
	 * @param sender , command sneder 
	 * @param player , ranked player
	 * @param rank	, new rank
	 * @param rID   , index of rank in Clazz
	 * @return  true = cost available or not relevant / false = not enough money
	 */
	protected boolean checkCost(Player sender, Player player , Rank rank, int rID)
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
					plugin.error(player,"You don't have enough money! ("
							+ ClassRanks.economy.format(checkedCost)
							+"/ "+ClassRanks.economy.getBalance(player.getName())
							+ ")");
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
	
	
	protected boolean checkExp(Player sender, Player player , Rank rank, int rID)
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
				plugin.error(player,
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
	protected boolean checkitems(Player sender, Player player , Rank rank, int rID)
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
			if (plugin.pm.hasItems(player, items) == false) 
			{
				plugin.error(player,"You don't have enough items!");
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
	protected boolean rankUp(Player sender, Player player, String className, String world) 
	{
		plugin.db.i("ranking " + player.getName() + " : " +  className);

		plugin.db.i("CoolDown Check for " + player.getName());
		int cDown = plugin.pm.coolDownCheck(player);
		if (cDown > 0) 
		{
			plugin.error(sender,
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
				plugin.msg(player, "You " + " are not in this class !");
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

		int rID = plugin.clazzList().getRankIndex(rank, oClass );	//  Index des Rank
		int iMaxRank = oClass.ranks.size() - 1; // Classes max tree rank
		plugin.db.i("rank " + rID + " of " + iMaxRank);
		// placeholder: cost
//		double rank_cost = 0;
		
			// we want to rank up!
			if (rID >= iMaxRank)
			{
				plugin.msg(sender, "Player " + FormatManager.formatPlayer(playerName)
						+ " is in MaxRank of class "+ className);
				return true;
			}
			
			Rank tempRank =  plugin.clazzList().getNextRank(rank, rID);  // New Rank

			// Check for money to rank up
			// rankCosts stored in checkedCost
			if (checkCost(sender, player , tempRank, rID+1)== false)
			{
				// not enough money message send to player and to promoter
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
				plugin.error(sender,"Player don't have enough experience! Need "+ checkedExp);
				return true;
			}
			plugin.db.i("Exp check done");

			// check items for rank up
			if (!checkitems(sender, player , tempRank, rID+1))
			{
				// not enough items message send to player and to promoter
				plugin.error(sender,"Player don't have enough items!");
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
				plugin.pm.takeItems(player, checkedItems);
				plugin.msg(player, "You paid the required items!");
				plugin.msg(player, "Player paid the required items!");
			}

			plugin.db.i(">>>> remove old rank...");
			plugin.perms.rankRemove(player, rank.getPermName()); // do it!

			plugin.db.i(">>>> adding new rank...");
			plugin.perms.rankAdd(player, tempRank.getPermName());
			plugin.config.playerSectionWrite(player, className, tempRank.getPermName());

			
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
	 * rankdown a player in the given Clazz and Wold
	 * 
	 * @param sender
	 * @param player
	 * @param className
	 * @param world
	 * @return
	 */
	protected boolean rankDown(Player sender, Player player, String className, String world) 
	{
		plugin.db.i("ranking " + player.getName() + " : " +  className);

		plugin.db.i("CoolDown Check for " + player.getName());
		int cDown = plugin.pm.coolDownCheck(player);
		if (cDown > 0) 
		{
			plugin.error(sender,
					"You have to wait " + ChatColor.RED + String.valueOf(cDown)
							+ ChatColor.WHITE + " seconds!");
			return true; // cooldown timer still counting => OUT!
		}
		plugin.db.i("cooldown check positive");

		if ((!plugin.perms.hasPerms(sender, "classranks.rankdown", player.getWorld().getName()))) 
		{
			// if we either are no OP or try to rank down, but we may not
			plugin.error(sender, "You don't have permission to rank down!");
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
				plugin.error(sender, "Player " + FormatManager.formatPlayer(playerName)
						+ " does not have a class !");
				return true;
			}
		} else
		{
			plugin.error(sender, "Player " + FormatManager.formatPlayer(playerName)
					+ " not in class "+ className);
			return true;
		}

		plugin.db.i("rank check successful");

		Clazz oClass = rank.getSuperClass();   // actual Rank class

		int rID = plugin.clazzList().getRankIndex(rank, oClass );	//  Index des Rank
		int iMaxRank = oClass.ranks.size() - 1; // Classes max tree rank
		plugin.db.i("rank " + rID + " of " + iMaxRank);

			if (rID < 1) 
			{
				// We are at lowest rank
				plugin.error(sender, "Player " + FormatManager.formatPlayer(playerName) + " already at lowest rank!");
				return true;
			}
			if (plugin.config.isDefaultrankallworlds())
			{
				plugin.perms.rankRemoveGlobal(player, rank.getPermName()); // do it!
				
			} else
			{
				plugin.perms.rankRemove(player, rank.getPermName()); // do it!
			}

			Rank tempRank = plugin.clazzList().getPrevRank(rank, rID);
			plugin.perms.rankAdd(player, tempRank.getPermName());
			plugin.config.playerSectionWrite(player, className, tempRank.getPermName());
			plugin.db.i("Rank Down to" + tempRank.getDispName());
			plugin.msg(sender, "Player " + FormatManager.formatPlayer(playerName)
					+ " Rank Down to " + tempRank.getDispName());
			return true;
	}	
	
}
