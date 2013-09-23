package net.slipcor.classranks.managers;

import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Set;
import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Class;
import net.slipcor.classranks.core.Rank;
import net.slipcor.classranks.managers.PlayerManager;
import net.slipcor.classranks.register.payment.Method.MethodAccount;

//import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * command manager class
 * 
 * @version v0.4.5
 * 
 * @author Krglok
 */

public class CommandManager {
	private final ClassRanks plugin;
	private final FormatManager fm;
	private final PlayerManager pm;
//	private final DebugManager db;

	public Double[] moneyCost = new Double[ClassManager.getClasses().size()]; // Costs
	public int[] expCost = new int[ClassManager.getClasses().size()]; // EXP
																		// Costs
//	public boolean rankpublic; // do we spam changed to the public?
//	public boolean onlyoneclass; // only maintain oneass
//	public ItemStack[][] rankItems;
//	public String[] signCheck = { null, null, null }; // SignCheck variable

	public CommandManager(ClassRanks plugin) 
	{
		this.plugin = plugin;
		this.pm = new PlayerManager(plugin);
		this.fm = new FormatManager();
//		this.db = new DebugManager(plugin);
		moneyCost = plugin.config.getMoneyCost();
	}

	/*
	 * This function provides the main ranking process - including error
	 * messages when stuff is not right. Parameter check must be done before. 
	 * /class [rankup] [playername] [classname] {world} | Rank user up
	 * - check if permissions are there 
	 * - get old rank 
	 * - calculate new rank 
	 * - eventually delete from old rank 
	 * - add to new rank
	 */
	public boolean rank(Player sender, Player player, String[] args) 
	{
		plugin.db.i("ranking " + player.getName() + " : " + FormatManager.formatStringArray(args));

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

		if ((!plugin.perms.hasPerms(player, "classranks.rankdown", player.getWorld().getName()))) 
		{
			// if we either are no OP or try to rank down, but we may not
			plugin.msg(player, "You don't have permission to rank down!");
			return true;
		}
		plugin.db.i("perm check successful");

		String world = getWorldArg(sender.getWorld().getName(), args); // store

		String playerName = player.getName(); // store the player we want to

		String className = args[2];
				
//		Boolean self = true; // we want to change ourselves ... maybe

		// does player have a rank?
		Rank rank = null; // actual Rank
		if (hasClass(className, playerName, world))
		{
			rank = hasRank( className, playerName, world);
			if (rank == null) 
			{
				plugin.msg(player, "Player " + fm.formatPlayer(playerName)
						+ " does not have a class !");
				return true;
			}
		} else
		{
			plugin.msg(sender, "Player " + fm.formatPlayer(playerName)
					+ " not in class "+ className);
			return true;
		}

		plugin.db.i("rank check successful");

		String cPermName = rank.getPermName(); // Permissions rank name
		String cDispName = rank.getDispName(); // Display rank name
		ChatColor c_Color = rank.getColor(); // Rank color
		Class oClass = rank.getSuperClass(); // Rank class

		int rID = ClassManager.getRankIndex(rank, oClass );	//  Index des Rank
//		int cRank = cClass.ranks.indexOf(rank); // Rank index
		int iMaxRank = oClass.ranks.size() - 1; // Classes max tree rank
		plugin.db.i("rank " + rID + " of " + iMaxRank);
		// placeholder: cost
		double rank_cost = 0;
		// placeholder: items
		ItemStack[] items = null;

//		// placeholders for new rank
//		int rankOffset = 0;

		if (args[0].equalsIgnoreCase("rankdown")) 
		{
			if (rID < 1) 
			{
				// We are at lowest rank
				plugin.msg(sender, "Player " + fm.formatPlayer(playerName) + " already at lowest rank!");
				plugin.msg(sender, "use /class [remove] [playername] [classname] {world} | Remove user from a class");
				return true;
			}

			rank = ClassManager.getPrevRank(rank, rID);
			plugin.perms.rankAdd(world, playerName, rank.getPermName());
			plugin.db.i("Rank Down to" + rank.getDispName());
			plugin.msg(sender, "Player " + fm.formatPlayer(playerName)
					+ " Rank Down to " + rank.getDispName());
			return true;
		}
		
		if (args[0].equalsIgnoreCase("rankup")) 
		{
			// we want to rank up!
			if (rID >= iMaxRank)
			{
				plugin.msg(sender, "Player " + fm.formatPlayer(playerName)
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
			
			Rank rNext =  ClassManager.getNextRank(rank, rID);  // New Rank
			if (rNext != null) 
			{
				if (rNext.getCost() != -1337D) 
				{
					rank_cost = rNext.getCost();
				}
			}

			if (ClassRanks.economy != null) 
			{
				
//				if (ClassRanks.economy.hasAccount(comP.getName())) {
//					plugin.log("Account not found: " + comP.getName(),
//							Level.SEVERE);
//					return true;
//				}
				if (!ClassRanks.economy.has(player.getName(), rank_cost)) 
				{
					// no money, no ranking!
					plugin.msg(sender,"You don't have enough money to rank up!");
					return true;
				}
			} else if (plugin.method != null) 
			{
				MethodAccount ma = plugin.method.getAccount(player.getName());
				if (ma == null) 
				{
					ClassRanks.log("Account not found: " + player.getName(),
							Level.SEVERE);
					return true;
				}
				if (!ma.hasEnough(rank_cost)) 
				{
					// no money, no ranking!
					plugin.msg(sender,"You don't have enough money to rank up!");
					return true;
				}
			}
			ItemStack[][] rankItems = plugin.config.getRankItems(); 
			items =  rankItems != null ?rankItems[rID + 1] : null;
			
			if (rNext.getItemstacks() != null) 
			{
				items = rNext.getItemstacks();
			}
		
			if (expCost != null && expCost.length > 0) 
			{
				int exp_cost = -1;
				
				try {
					exp_cost = expCost[rID + 1];
				} catch (Exception e) {
					ClassRanks.log("Exp cost not set: "+ rID + 1, Level.WARNING);
				}
				rNext = ClassManager.getNextRank(rank, 1);
				if (rNext != null && rNext.getExp() != -1) 
				{
					exp_cost = rNext.getExp();
				}
				
				if (exp_cost > 0) 
				{
					if (player.getTotalExperience() < exp_cost) {
						plugin.msg(sender,
								"You don't have enough experience ("+player.getTotalExperience()+")! You need "
										+ exp_cost);
						return true;
					}
					int newExp = player.getTotalExperience() - exp_cost;
					player.setExp(0);
					player.setLevel(0);
					player.giveExp(newExp);
					
					plugin.msg(sender, "You paid " + exp_cost
							+ " experience points!");
				}
			}

			if ((items != null)
					&& (!FormatManager.formatItemStacks(items).equals(""))) {
				if (!pm.ifHasTakeItems(player, items)) {
					plugin.msg(sender, "You don't have the required items!");
					plugin.msg(sender,
							"(" + FormatManager.formatItemStacks(items) + ")");
					return true;
				}
			}
			// up and only OR down
			plugin.db.i("removing old class...");
			plugin.perms.rankRemove(world, playerName, rank.getPermName());

			rank = ClassManager.getNextRank(rank, rID);

			cPermName = rNext.getPermName();
			cDispName = rNext.getDispName();
			c_Color = rNext.getColor();

			plugin.db.i("adding new rank...");
			plugin.perms.rankAdd(world, playerName, rNext.getPermName());

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
						"You are now a " + c_Color + cDispName + ChatColor.WHITE
								+ ChatColor.WHITE + " in " + fm.formatWorld(world)
								+ "!");
			} else 
			{
				plugin.getServer().broadcastMessage(
						"[" + ChatColor.AQUA + plugin.getConfig().getString("prefix") + ChatColor.WHITE
								+ "] Player " + fm.formatPlayer(playerName)
								+ " now is a " + c_Color + cDispName
								+ ChatColor.WHITE + " in " + fm.formatWorld(world)
								+ "!");
			}

		}
		return true;
	}

	private void cmdCommandInfo (Player pPlayer)
	{
		plugin.msg(pPlayer, "Plugin ClassRanks Ver: " + plugin.getDescription().getVersion());
		plugin.msg(pPlayer, ChatColor.YELLOW+"-----------------------------------------");
		plugin.msg(pPlayer, ChatColor.YELLOW+"usage: [] = required  {} = optional ");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class [classname] | Choose the class [classname] for yourself");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class [list] {classname} | list of class/ranks");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class [config] | list the configuration");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class [groups] {playername} | list the groups of user");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class [add] [playername] [classname] {world} | Add user to a class");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class [remove] [playername] [classname] {world} | Remove user from a class");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class [rankup] [playername] {world} | Rank user up");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class [rankdown]  [playername] {world} | Rank user down");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/class [get] [playername] {world} | Show rank of user");
		
	}
	
	/**
	 * printout Classas and/or Class Ranks
	 * 
	 * @param pPlayer
	 * @param args
	 */
	private void cmdList (Player pPlayer, String[] args) 
	{
	
		ArrayList<Class> classes = ClassManager.getClasses();
		if (args.length > 1) 
		{
			for (Class c : classes) 
			{
				if (c.name.equalsIgnoreCase(args[1]) ) 
				{
					plugin.msg(pPlayer, "[ClassRanks] "+ChatColor.YELLOW+"Class "+ ChatColor.GREEN + c.name);
					plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------");
					for (Rank r : c.ranks) 
					{
						plugin.msg(pPlayer, "=> " + r.getColor() + r.getDispName());
					}
				}
			}
		} else
		{
			
			plugin.msg(pPlayer, "[ClassRanks] "+ChatColor.YELLOW+"Class List");
			plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------");
			
			for (Class c : classes) 
			{
				if (c.name.startsWith("%") && !pPlayer.isOp()) 
				{
					continue;
				}
				plugin.msg(pPlayer, "Class "+ ChatColor.GREEN + c.name);
			}
		}
		
	}

	/**
	 * printout player Class, Rank and world
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
		String player = pPlayer.getName(); 
		if (args.length > 1) 
		{
			player = args[1];
		}

		plugin.msg(pPlayer, "Player " + fm.formatPlayer(player)+ " Get in " + fm.formatWorld(world) + "!");
		Rank rank = ClassManager.getRankByPermName(plugin.perms.getPermNameByPlayer(world, player));

		if (rank == null) 
		{
			plugin.msg(pPlayer, "Player " + player
					+ " has no class in " + fm.formatWorld(world) + "!");
		} else 
		{

			String cDispName = rank.getDispName(); // Display rank name
			ChatColor c_Color = rank.getColor(); // Rank color

			plugin.msg(pPlayer, "Player " + player
					+ " is " + c_Color + cDispName + ChatColor.WHITE
					+ " in " + fm.formatWorld(world) + "!");
		}
		
	}
	
	private void cmdConfig (Player pPlayer, String[] args) 
	{
		plugin.msg(pPlayer, "[ClassRanks] "+ChatColor.YELLOW+"Class Config Data");
		plugin.msg(pPlayer, ChatColor.YELLOW+"----------------------------");
		plugin.msg(pPlayer, ChatColor.RED+"Debug  : "+plugin.config.isDebug().toString());
		plugin.msg(pPlayer, ChatColor.YELLOW+"Prices : "+plugin.config.isCheckprices().toString());
		plugin.msg(pPlayer, ChatColor.YELLOW+"Items  : "+plugin.config.isCheckitems().toString());
		plugin.msg(pPlayer, ChatColor.YELLOW+"Exp     : "+plugin.config.isCheckexp().toString());
		plugin.msg(pPlayer, ChatColor.YELLOW+"CoolDown: "+plugin.config.getCoolDown());
		plugin.msg(pPlayer, ChatColor.GREEN+"OneClass: "+plugin.config.isOnlyoneclass().toString());
		plugin.msg(pPlayer, ChatColor.GREEN+"AllWorlds: "+plugin.config.isDefaultrankallworlds().toString());
		plugin.msg(pPlayer, ChatColor.GREEN+"SignCheck: "+plugin.config.isSigncheck().toString());
		plugin.msg(pPlayer, ChatColor.GREEN+"TrackRank: "+plugin.config.isTrackRanks().toString());
		plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------");
		
		Double[] MoneyCost = plugin.config.getMoneyCost();
		plugin.msg(pPlayer, ChatColor.YELLOW+"Default Cost");
		
		for (int i = 0; i < MoneyCost.length; i++) 
		{
			plugin.msg(pPlayer, ChatColor.YELLOW + String.valueOf(i) + " : "+MoneyCost[i].toString());
		}
		int[] exp = plugin.config.getExpCost();
		plugin.msg(pPlayer, ChatColor.YELLOW+"Default Experience " + exp.length);
		
		for (int i = 0; i < exp.length; i++) 
		{
			plugin.msg(pPlayer, ChatColor.YELLOW + String.valueOf(i) + " : "+exp[i]);
		}
	}
	
	private void cmdUserGroups (Player pPlayer, String[] args)
	{
		String player = pPlayer.getName();
		// is there a player parameter, get it
		if (args.length > 1)
		{
			player = args[1];
		} else
		{
			player = pPlayer.getName();
		}
		String[] list = plugin.perms.getPlayerGroups(player);
		if (list != null) 
		{
			int max = list.length;
			plugin.msg(pPlayer, "[ClassRanks] "+ChatColor.YELLOW+"Class "+ "User Groups of "+ChatColor.DARK_GREEN+player);
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
		plugin.msg(player,"You don't have permission to add/remove other player's classes!");
		return false;
	}

	/**
	 * 
	 * @param className
	 * @param playerName
	 * @param world
	 * @return
	 */
	private Rank hasRank(String className, String playerName, String world)
	{
		ArrayList<Class> classes = ClassManager.getClasses();
		String sPermName = "";
		boolean isClass = false;
		for (Class oClass : classes) 
		{
		    //  pruefe ob KLassenname identisch 
		    if (className.equalsIgnoreCase(oClass.name))
		    {
				ArrayList<Rank> ranks = oClass.getRanks();
				for (Rank rank : ranks) 
				{
				    sPermName = rank.getPermName();
				    // pruefe ob Permname als group vorhonden ist
				    isClass = plugin.perms.isInGroup(world, sPermName, playerName);
				    // wenn gefunden ,  beende es fruehzeitig und gib rank zurueck
				    if (isClass)
				    {
				    	return rank;
				    }
				}

		    }
		}
		return null;
	}
	
	/**
	 * Sucht das Class Array nach dem Classnamen
	 * und sucht die Group zu den Raengen
	 * TRUE , wenn Group gesetzt
	 * 
	 * @param className
	 * @param playerName
	 * @param world
	 * @return
	 */
	private boolean hasClass(String className, String playerName, String world)
	{
		ArrayList<Class> classes = ClassManager.getClasses();
		String sPermName = "";
		boolean isClass = false;
		for (Class oClass : classes) 
		{
		    //  pruefe ob KLassenname identisch 
		    if (className.equalsIgnoreCase(oClass.name))
		    {
				ArrayList<Rank> ranks = oClass.getRanks();
				for (Rank rank : ranks) 
				{
				    sPermName = rank.getPermName();
				    // pruefe ob Permname als group vorhonden ist
				    isClass = plugin.perms.isInGroup(world, sPermName, playerName);
				    // wenn gefunden ,  beende es fruehzeitig
				    if (isClass)
				    {
				    	return isClass;
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
		ArrayList<Class> classes = ClassManager.getClasses();
		for (Class oClass : classes) 
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
		Player player = null;
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

		if (plugin.trackRanks) 
		{
			ClassManager.saveClassProgress(player);
		}
		
		if (hasClass(className, playerName, world))
		{
			plugin.perms.rankRemove(world, playerName, className); // do it!
			if (plugin.config.isRankpublic() ) 
			{
				// self remove successful!
				plugin.getServer().broadcastMessage(player+
							" are removed from rank " + ChatColor.RED
							+ className + ChatColor.WHITE
							+ " in "
							+ fm.formatWorld(world) + "!");
			} else
			{
				// self remove successful!
				plugin.msg(player,"You are removed from rank " + ChatColor.RED
							+ className + ChatColor.WHITE
							+ " in "
							+ fm.formatWorld(world) + "!");
				if (!(sender == player))
				{
					// self remove successful!
					plugin.msg(sender,playerName+" are removed from rank " + ChatColor.RED
								+ className + ChatColor.WHITE
								+ " in "
								+ fm.formatWorld(world) + "!");
				}
			}
		}
		return true;
	}
	
	
		
	
	/**
	 * Add Class d first Rank to player
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
		if (hasClass(className, player.getName(), world))
		{
			plugin.msg(sender,
							"Player " + fm.formatPlayer(player.getName())
							+ " already has the Class "
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
			plugin.msg(sender,"The class you entered does not exist!"+className);
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

		plugin.db.i("Get Rank Cost");
		double rank_cost = 0; //0D;
		
		// get default cost from Config table
		rank_cost =  plugin.config.getMoneyCost(rID);
		// get individual rank cost
		if (tempRank.getCost() != -1337D) 
		{
			rank_cost = tempRank.getCost();
		}
		plugin.db.i("Get Rank Cost : " + rank_cost);
		// Check if the player has got the money
		// check loaded economy 
		plugin.db.i("Check economy");
		if (ClassRanks.economy != null) 
		{
			plugin.db.i("Economy found : " + ClassRanks.economy.getName() );
			plugin.db.i("isCheckCost : " + plugin.config.isCheckprices());
			if (plugin.config.isCheckprices()) 
			{
				if (ClassRanks.economy.getBalance(player.getName()) < rank_cost)
				{
					plugin.msg(player,"You don't have enough ECO money to choose your class! ("
							+ ClassRanks.economy.format(rank_cost)
							+"/ "+ClassRanks.economy.getBalance(player.getName())
							+ ")");
					return true;
				}
			}
		} else if (plugin.method != null) 
		{
			plugin.db.i("No economy found, use Method : " + plugin.method.getName());
			plugin.db.i("isCheckCost" + plugin.config.isCheckprices());
			if (plugin.config.isCheckprices())
			{
				MethodAccount ma = plugin.method.getAccount(player.getName());
				if (ma.hasEnough(rank_cost)) 
				{
					plugin.msg(player,"You don't have enough money to choose your class! ("
								+ plugin.method.format(rank_cost)
								+ ")");
					return true;
				}
			}
		}
		plugin.db.i("money check successful");
		
		//
		int exp_cost = -1;
		// get default exp cost from config
		exp_cost = plugin.config.getExpCost(rID);
		// get indivial expCost from rank
		if (tempRank.getExp() > 0)
		{
			exp_cost = tempRank.getExp();
		}
		plugin.db.i("Get ExpCost : " + exp_cost);
		
		plugin.db.i("isCheckExp : " + plugin.config.isCheckexp());
		if (plugin.config.isCheckexp()) 
		{
			if (player.getTotalExperience() < exp_cost) {
				plugin.msg(player,
						"You don't have enough experience! You need "
								+ exp_cost);
				return true;
			}
			player.setTotalExperience(player.getTotalExperience()
					- exp_cost);
			plugin.msg(player, "You paid " + exp_cost
					+ " experience points!");
		}
		plugin.db.i("exp check successful");

		ItemStack[] items = null;
		ItemStack[][] rankItems = plugin.config.getRankItems(); 
		
		if (rankItems != null && (rankItems[rID] != null)
				&& (!FormatManager.formatItemStacks(rankItems[rID]).equals(
						""))) 
		{
			items = rankItems[rID];
		}

		if (tempRank.getItemstacks() != null) 
		{
			items = tempRank.getItemstacks();
		}
		if (items != null)
		{
			plugin.db.i("Get Item Cost : " + items.toString());
			plugin.db.i("isCheckItems" + plugin.config.isCheckitems());
			if (plugin.config.isCheckitems()) 
			{
				
				if (!pm.ifHasTakeItems(player, items)) 
				{
					plugin.msg(player, "You don't have the required items!");
					plugin.msg(player,"("+ FormatManager.formatItemStacks(items)+ ")");
					return true;
				}
			}
			plugin.db.i("item check successful");
		} else
		{
			plugin.db.i("No Items found !");
			plugin.db.i("item check NOT successful");
		}

		String cPermName = tempRank.getPermName(); // Display rank name
		String cDispName = tempRank.getDispName(); // Display rank name
		ChatColor c_Color = tempRank.getColor(); // Rank color

		// success!
		
		plugin.db.i("isClearRanks : " + plugin.config.isClearranks());
		if (plugin.config.isClearranks()) 
		{
			plugin.perms.removeGroups(player);
			plugin.db.i("Remove Groups from Permission");
		}


		plugin.perms.rankAdd(plugin.config.isDefaultrankallworlds() ? "all" : player.getWorld().getName()
				, player.getName(), cPermName);
		plugin.db.i("Added Group to Permission");
		
		if (plugin.config.isRankpublic() ) 
		{
			plugin.getServer().broadcastMessage(
					"[" + ChatColor.AQUA + plugin.getConfig().getString("prefix") + ChatColor.WHITE
							+ "] " + fm.formatPlayer(player.getName())
							+ " now is a " + c_Color + cDispName);
			return true;
		} else 
		{
			plugin.msg(player, "You now are a " + c_Color + cDispName);
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
	


	private boolean cmdreload (Player player, String[] args)
	{
		
		return false;
	}
	
	private boolean cmdInfo (Player player, String[] args)
	{
		
		return false;
	}
	
	
	private boolean cmdAdminRemove (Player player, String[] args)
	{
		
		return false;
	}

	private boolean cmdAdminAdd (Player player, String[] args)
	{
		
		return false;
	}
	
	private boolean cmdAdminChange (Player player, String[] args)
	{
		
		return false;
	}
	
	/**
	 * The main switch for the command usage. Check for known commands,
	 * permissions, arguments, and commit whatever is wanted.
	 * 
     /class [classname] | Choose the class [classname] for yourself
     /class [config]  | list the configuration
     /class [list] {classname} | list of class/ranks
     /class [groups]  {playername} | list the groups of user
      
     /class [add]     [playername] [classname] {world} | Add user to a class
     /class [remove]  [playername] [classname] {world} | Remove user from a class

     /class [rankup]  [playername] {world} | Rank user up
     /class [rankdown][playername] {world} | Rank user down
     /class [get]     [playername] {world} | Show rank of user

	 * @param pPlayer , the sender of the command
	 * @param args , arguments of the Command /class
	 * @return
	 */
	public boolean parseCommand(Player pPlayer, String[] args) 
	{
//		Player player = pPlayer;
		plugin.db.i("parsing player " + pPlayer + ", command: " + FormatManager.formatStringArray(args));

		boolean isSelf = plugin.perms.hasPerms(pPlayer, "classranks.self.rank", pPlayer.getWorld().getName()); 

		if (args.length == 0)
		{
			//zeigt command description aus Plugi.yml
			cmdCommandInfo (pPlayer);
			return true;  
		} 

		// LIST
		if (args[0].equalsIgnoreCase("list")) {
			// Command list of Ranks in the Class  3.2
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
			// Command list of Ranks in the Class  3.2
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
					String PlayerName = args[1];
					Player player = PlayerManager.searchPlayer(PlayerName);
					if ((player == null))
					{
						plugin.msg(pPlayer, "Not found player " + PlayerName);
						return true;
					}
					
					return rank(pPlayer, player, args);
				} else 
				{
					plugin.msg(pPlayer,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
					plugin.msg(pPlayer," /class [rankup]  [playername] {world} | Rank user up");
					return true;
				}
			} else 
			{
				plugin.msg(pPlayer, "You don't have permission to reload!");
			}
		}

		//RANKDOWN 
		if ((args[0].equalsIgnoreCase("rankdown"))) 
		{
			if (hasAdminPerms(pPlayer))
			{
				// rank up or down
				if (args.length > 1) {
					String PlayerName = args[1];
					Player player = PlayerManager.searchPlayer(PlayerName);
					if ((player == null))
					{
						plugin.msg(pPlayer, "Not found player " + PlayerName);
						return true;
					}
					return rank(pPlayer, player, args);
				} else {
					plugin.msg(pPlayer,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
					plugin.msg(pPlayer," /class [rankdown][playername] {world} | Rank user down");
					return true;
				}

			} else 
			{
				plugin.msg(pPlayer, "You don't have permission to reload!");
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
				plugin.msg(pPlayer, "You don't have permission to reload!");
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
					player = PlayerManager.searchPlayer(PlayerName);
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
					plugin.msg(pPlayer, "You don't have permission to reload!");
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
				// Command list of Ranks in the Class  3.2
				cmdConfig(pPlayer, args);
				return true;
			} else 
			{
				plugin.msg(pPlayer, "You don't have permission to reload!");
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
				plugin.msg(pPlayer, "You don't have permission to reload!");
				return true;
			}
		}
		
		// /class [classname]
		// /class [classname] | Choose the class [classname] for yourself in all worlds

		// command wird nur ausgefuehrt bei permissions
		if (!isSelf) 
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

	
	public boolean parseAdminCommand(Player pPlayer, String[] args) 
	{
		plugin.db.i("parsing admin " + pPlayer.getName() + ", command: "
				+ FormatManager.formatStringArray(args));

		if (!plugin.perms.hasPerms(pPlayer, "classranks.admin.admin", pPlayer
				.getWorld().getName())) {
			plugin.msg(pPlayer,
					"You don't have permission to administrate ranks!");
			return true;
		}

		plugin.db.i("perm check successful");

		if (args.length < 3) {
			plugin.msg(pPlayer,
					"Not enough arguments (" + String.valueOf(args.length)
							+ ")!");
			return false;
		}
		plugin.db.i("enough arguments");

		if (args[0].equalsIgnoreCase("remove")) {
			/*
			 * /classadmin remove class *classname* /classadmin remove rank
			 * *classname* *rankname*
			 */
			if (args[1].equalsIgnoreCase("class")) {
				if (args.length != 3) {
					plugin.msg(
							pPlayer,
							"Wrong number of arguments ("
									+ String.valueOf(args.length) + ")!");
					return false;
				}
				return ClassManager.configClassRemove(args[2], pPlayer);
			} else if (args[1].equalsIgnoreCase("rank")) {
				if (args.length != 4) {
					plugin.msg(
							pPlayer,
							"Wrong number of arguments ("
									+ String.valueOf(args.length) + ")!");
					return false;
				}
				return ClassManager.configRankRemove(args[2], args[3], pPlayer);
			}
			// second argument unknown
			return false;
		}
		if (args[0].equalsIgnoreCase("add")) {

			/* Add Class and first Rank
			 * /classadmin add class *classname* *rankname* *displayname*
			 * *color* 
			 * 
			 * Add Rank to Class
			 * /classadmin add rank *classname* *rankname* *displayname*
			 * *color*
			 */
			if (args.length != 9) 
			{
				plugin.msg(
						pPlayer,
						"Wrong number of arguments ("
								+ String.valueOf(args.length) + ") should be 9 !");
				return false;
			}

			if (args[1].equalsIgnoreCase("class")) 
			{// , ItemStack[] isItems, double dCost, int iExp

				return ClassManager.configClassAdd(args[2], args[3], args[4],
						args[5],
						FormatManager.getItemStacksFromCommaString(args[6]),
						Double.parseDouble(args[7]), Integer.parseInt(args[8]),
						null); //pPlayer);
			}
			if (args[1].equalsIgnoreCase("rank")) 
			{
			
				return ClassManager.configRankAdd(args[2], args[3], args[4],
						args[5],
						FormatManager.getItemStacksFromCommaString(args[6]),
						Double.parseDouble(args[7]), Integer.parseInt(args[8]),
						null); //pPlayer);
			}
			// second argument unknown
			return false;
		} else if (args[0].equalsIgnoreCase("change")) {

			/*
			 * /classadmin change rank *classname* *rankname* *displayname*
			 * *color* /classadmin change class *classname* *newclassname*
			 */

			if (args[1].equalsIgnoreCase("class")) {
				if (args.length != 4) {
					plugin.msg(
							pPlayer,
							"Wrong number of arguments ("
									+ String.valueOf(args.length) + ")!");
					return false;
				}
				return ClassManager
						.configClassChange(args[2], args[3], pPlayer);
			} else if (args[1].equalsIgnoreCase("rank")) {
				if (args.length != 6) {
					plugin.msg(
							pPlayer,
							"Wrong number of arguments ("
									+ String.valueOf(args.length) + ")!");
					return false;
				}
				return ClassManager.configRankChange(args[2], args[3], args[4],
						args[5], pPlayer);
			}
			// second argument unknown
			return false;
		}
		return false;
	}

	public FormatManager getFormatManager() {
		return fm;
	}
	
	
}
