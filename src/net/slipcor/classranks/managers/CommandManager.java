package net.slipcor.classranks.managers;

import java.util.ArrayList;
import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Class;
import net.slipcor.classranks.core.Rank;
import net.slipcor.classranks.managers.PlayerManager;
import net.slipcor.classranks.register.payment.Method.MethodAccount;

import org.bukkit.Bukkit;
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
	private final DebugManager db;

	public double[] moneyCost = new double[ClassManager.getClasses().size()]; // Costs
	public int[] expCost = new int[ClassManager.getClasses().size()]; // EXP
																		// Costs
//	public boolean rankpublic; // do we spam changed to the public?
//	public boolean onlyoneclass; // only maintain oneass
//	public ItemStack[][] rankItems;
//	public String[] signCheck = { null, null, null }; // SignCheck variable

	public CommandManager(ClassRanks plugin) {
		this.plugin = plugin;
		this.pm = new PlayerManager(plugin);
		this.fm = new FormatManager();
		this.db = new DebugManager(plugin);
	}

	/*
	 * This function provides the main ranking process - including error
	 * messages when stuff is not right. 
	 * - check if permissions are there 
	 * - get old rank 
	 * - calculate new rank 
	 * - eventually delete from old rank 
	 * - add to new rank
	 */
	public boolean rank(String[] args, Player comP) {
		db.i("ranking " + comP.getName() + " : " + FormatManager.formatStringArray(args));
		int cDown = pm.coolDownCheck(comP);
		if (cDown > 0) {
			plugin.msg(comP,
					"You have to wait " + ChatColor.RED + String.valueOf(cDown)
							+ ChatColor.WHITE + " seconds!");
			return true; // cooldown timer still counting => OUT!
		}
		db.i("cooldown check positive");

		if ((!plugin.perms.hasPerms(comP, "classranks.rankdown", comP
				.getWorld().getName()))
				&& args[0].equalsIgnoreCase("rankdown")) {
			// if we either are no OP or try to rank down, but we may not
			plugin.msg(comP, "You don't have permission to rank down!");
			return true;
		}

		db.i("perm check successful");

		String World = plugin.config.isDefaultrankallworlds() ? "all" : comP.getWorld().getName(); // store
																					// the
																					// player's
																					// world
		String changePlayer = comP.getName(); // store the player we want to
												// change
		Boolean self = true; // we want to change ourselves ... maybe

		if (args.length > 1) { // we have more than one argument. that means:
			changePlayer = PlayerManager.search(args[1]); // we want to change a
															// different player
			self = false; // we do not want to change ourselves
			if (args.length > 2) { // we even have more than two arguments. that
									// means:
				World = args[2]; // we want to specify a world where we change
			}
		}

		// does player have a rank?

		Rank rank = ClassManager.getRankByPermName(plugin.perms
				.getPermNameByPlayer(World, changePlayer));

		if (rank == null) {
			// No Rank
			if (self) {
				plugin.msg(comP, "You don't have a class!");
				return true;
			} else {
				plugin.msg(comP, "Player " + fm.formatPlayer(changePlayer)
						+ " does not have a class !");
				return true;
			}
		}

		db.i("rank check successful");

		String cPermName = rank.getPermName(); // Permissions rank name
		String cDispName = rank.getDispName(); // Display rank name
		ChatColor c_Color = rank.getColor(); // Rank color
		Class cClass = rank.getSuperClass(); // Rank class
		int cRank = cClass.ranks.indexOf(rank); // Rank index
		int cMaxRank = cClass.ranks.size() - 1; // Classes max tree rank
		db.i("rank " + cRank + " of " + cMaxRank);
		// placeholder: cost
		double rank_cost = 0;
		// placeholder: items
		ItemStack[] items = null;

		// placeholders for new rank
		int rankOffset = 0;

		if (args[0].equalsIgnoreCase("rankup")) {
			// we want to rank up!
			if (cRank >= cMaxRank) {
				// if the player is in the maximum rank
				if (self) {
					plugin.msg(comP, "You are already at highest rank!");
				} else {
					plugin.msg(comP, "Player " + fm.formatPlayer(changePlayer)
							+ " already at highest rank!");
				}
				return true;
			}
			if (self) {
				// if we rank ourselves, that might cost money
				// (otherwise, thats not the players problem ;) )
				try {
					rank_cost = moneyCost[cRank + 1];
				} catch (Exception e) {
					plugin.log("cost not set: "+(cRank + 1), Level.WARNING);
				}
				
				Rank rNext =  ClassManager.getNextRank(rank, 1);
				if (rNext != null) {
					if (rNext.getCost() != -1337D) {
						rank_cost = rNext.getCost();
					}
				}

				if (ClassRanks.economy != null) {
					
//					if (ClassRanks.economy.hasAccount(comP.getName())) {
//						plugin.log("Account not found: " + comP.getName(),
//								Level.SEVERE);
//						return true;
//					}
					if (!ClassRanks.economy.has(comP.getName(), rank_cost)) {
						// no money, no ranking!
						plugin.msg(comP,
								"You don't have enough money to rank up!");
						return true;
					}
				} else if (plugin.method != null) {
					MethodAccount ma = plugin.method.getAccount(comP.getName());
					if (ma == null) {
						plugin.log("Account not found: " + comP.getName(),
								Level.SEVERE);
						return true;
					}
					if (!ma.hasEnough(rank_cost)) {
						// no money, no ranking!
						plugin.msg(comP,
								"You don't have enough money to rank up!");
						return true;
					}
				}
				ItemStack[][] rankItems = plugin.config.getRankItems(); 
				items =  rankItems != null ?rankItems[cRank + 1] : null;
				
				if (rNext.getItemstacks() != null) {
					items = rNext.getItemstacks();
				}
			}
			rankOffset = 1;
		} else if (args[0].equalsIgnoreCase("rankdown")) {
			if (cRank < 1) {
				// We are at lowest rank
				if (self) {
					plugin.msg(comP, "You are already at lowest rank!");
				} else {
					plugin.msg(comP, "Player " + fm.formatPlayer(changePlayer)
							+ " already at lowest rank!");
				}
				return true;
			}
			rankOffset = -1;
		}

		if (rankOffset > 0) {
			
			if (expCost != null && expCost.length > 0) {
				int exp_cost = -1;
				
				try {
					exp_cost = expCost[cRank + 1];
				} catch (Exception e) {
					plugin.log("Exp cost not set: "+cRank + 1, Level.WARNING);
				}
				Rank rNext = ClassManager.getNextRank(rank, 1);
				if (rNext != null && rNext.getExp() != -1) {
					exp_cost = rNext.getExp();
				}
				
				if (exp_cost > 0) {
					if (comP.getTotalExperience() < exp_cost) {
						plugin.msg(comP,
								"You don't have enough experience ("+comP.getTotalExperience()+")! You need "
										+ exp_cost);
						return true;
					}
					int newExp = comP.getTotalExperience() - exp_cost;
					comP.setExp(0);
					comP.setLevel(0);
					comP.giveExp(newExp);
					
					plugin.msg(comP, "You paid " + exp_cost
							+ " experience points!");
				}
			}

			if ((items != null)
					&& (!FormatManager.formatItemStacks(items).equals(""))) {
				if (!pm.ifHasTakeItems(comP, items)) {
					plugin.msg(comP, "You don't have the required items!");
					plugin.msg(comP,
							"(" + FormatManager.formatItemStacks(items) + ")");
					return true;
				}
			}
		}

		if (plugin.config.isOnlyoneclass() || rankOffset < 0) {
			// up and only OR down
			db.i("removing old class...");
			plugin.perms.rankRemove(World, changePlayer, cPermName);
		}

		rank = ClassManager.getNextRank(rank, rankOffset);

		cPermName = rank.getPermName();
		cDispName = rank.getDispName();
		c_Color = rank.getColor();

		db.i("adding new rank...");
		plugin.perms.rankAdd(World, changePlayer, cPermName);

		if ((rank_cost > 0)) {
			// take the money, inform the player

			if (ClassRanks.economy != null) {
				ClassRanks.economy.withdrawPlayer(comP.getName(), rank_cost);
				comP.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.WHITE
						+ "Money" + ChatColor.DARK_GREEN + "] " + ChatColor.RED
						+ "Your account had " + ChatColor.WHITE
						+ ClassRanks.economy.format(rank_cost) + ChatColor.RED
						+ " debited.");
			} else if (plugin.method != null) {
				MethodAccount ma = plugin.method.getAccount(comP.getName());
				ma.subtract(rank_cost);
				comP.sendMessage(ChatColor.DARK_GREEN + "[" + ChatColor.WHITE
						+ "Money" + ChatColor.DARK_GREEN + "] " + ChatColor.RED
						+ "Your account had " + ChatColor.WHITE
						+ plugin.method.format(rank_cost) + ChatColor.RED
						+ " debited.");
			}
		}

		if (self && !  plugin.config.isRankpublic()) {
			// we rank ourselves and do NOT want that to be public
			plugin.msg(comP,
					"You are now a " + c_Color + cDispName + ChatColor.WHITE
							+ ChatColor.WHITE + " in " + fm.formatWorld(World)
							+ "!");
		} else {
			plugin.getServer().broadcastMessage(
					"[" + ChatColor.AQUA + plugin.getConfig().getString("prefix") + ChatColor.WHITE
							+ "] Player " + fm.formatPlayer(changePlayer)
							+ " now is a " + c_Color + cDispName
							+ ChatColor.WHITE + " in " + fm.formatWorld(World)
							+ "!");
		}
		return true;
	}

	/**
	 * printout Classas and/or Class Ranks
	 * 
	 * @param pPlayer
	 * @param args
	 */
	private void cmdList (Player pPlayer, String[] args) {
		ArrayList<Class> classes = ClassManager.getClasses();
		plugin.msg(pPlayer, ChatColor.YELLOW+"Class / Rank List");
		plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------");
		
		for (Class c : classes) {
			if (c.name.startsWith("%") && !pPlayer.isOp()) {
				continue;
			}
			if (args[1].equalsIgnoreCase(c.name)) {
				plugin.msg(pPlayer, "Class "+ ChatColor.GREEN + c.name);
				for (Rank r : c.ranks) {
					plugin.msg(pPlayer, "=> " + r.getColor() + r.getDispName());
				}
			}
		}
		
	}

	/**
	 * printout player Class, Rank and world
	 * 
	 * @param pPlayer
	 * @param args
	 */
	private void cmdGet (Player pPlayer, String[] args) {
		String world = pPlayer.getWorld().getName();
		if (args.length > 2) {
			world = args[2];
		} else {
			world = "all";
		}
		String player = pPlayer.getName(); 
		if (args.length > 1) {
			player = args[1];
		}

		plugin.msg(pPlayer, "Player " + fm.formatPlayer(player)+ " Get in " + fm.formatWorld(world) + "!");
		Rank rank = ClassManager.getRankByPermName(plugin.perms.getPermNameByPlayer(world, player));

		if (rank == null) {
			plugin.msg(pPlayer, "Player " + player
					+ " has no class in " + fm.formatWorld(world) + "!");
		} else {

			String cDispName = rank.getDispName(); // Display rank name
			ChatColor c_Color = rank.getColor(); // Rank color

			plugin.msg(pPlayer, "Player " + player
					+ " is " + c_Color + cDispName + ChatColor.WHITE
					+ " in " + fm.formatWorld(world) + "!");
		}
		
	}
	
	private void cmdConfig (Player pPlayer, String[] args) 
	{
		plugin.msg(pPlayer, ChatColor.YELLOW+"Class Config Data");
		plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------");
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
		
		for (int i = 0; i < MoneyCost.length; i++) 
		{
			plugin.msg(pPlayer, ChatColor.YELLOW+": "+MoneyCost[i].toString());
		}
	}
	
	private void cmdUserGroups (Player pPlayer, String[] args)
	{
		String player = pPlayer.getName();
		// is there a player parameter, get it
		if (args.length > 1)
		{
			player = args[1];
		}
		String[] list = plugin.perms.getPlayerGroups(player);
		if (list != null) 
		{
			int max = list.length;
			plugin.msg(pPlayer, ChatColor.YELLOW+"User Groups "+ChatColor.DARK_GREEN+player);
			plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------");
			for (int i = 0; i < list.length; i++) 
			{
				plugin.msg(pPlayer, ChatColor.YELLOW+": "+list[i]);
			}
			plugin.msg(pPlayer, ChatColor.YELLOW+"Count "+max+" ---------");
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
	
	private boolean hasClassRank(String classRank, String playerName, String world)
	{
		String sPermName = plugin.perms.getPermNameByPlayer(world, playerName);
		int i = 0;
		while (!sPermName.equals("")) 
		{
			db.i("check add rank " + sPermName);

			Rank tempRank = ClassManager.getRankByPermName(sPermName);

			String cDispName = tempRank.getDispName(); // Display rank
														// name
			ChatColor c_Color = tempRank.getColor(); // Rank color

			if (classRank == tempRank.getSuperClass().name) 
			{
				return true;
			} 
			if (++i > 10) {
				plugin.log("Infinite loop! More than 10 ranks!?",
						Level.SEVERE);
				break;
			}
			sPermName = plugin.perms.getPermNameByPlayer(world, playerName);
		}
		
		return false;
	}
	
//	private void cmdTemp (Player pPlayer, String[] args) {
//		
//	}
	
	private boolean cmdRemove(Player sender, Player player, String[] args, String world)
	{

		String classRank = args[2];

		if (plugin.trackRanks) 
		{
			ClassManager.saveClassProgress(Bukkit.getPlayer(player.getName()));
		}
		
		if (hasClassRank(classRank, player.getName(), world))
		{
			plugin.perms.rankRemove(world, args[1],classRank); // do it!
			if (plugin.config.isRankpublic() ) 
			{
				// self remove successful!
				plugin.getServer().broadcastMessage(player.getName()+
							" are removed from rank " + ChatColor.RED
							+ classRank + ChatColor.WHITE
							+ " in "
							+ fm.formatWorld(world) + "!");
			} else
			{
				// self remove successful!
				plugin.msg(player,"You are removed from rank " + ChatColor.RED
							+ classRank + ChatColor.WHITE
							+ " in "
							+ fm.formatWorld(world) + "!");
				if (!(sender == player))
				{
					// self remove successful!
					plugin.msg(sender,player.getName()+" are removed from rank " + ChatColor.RED
								+ classRank + ChatColor.WHITE
								+ " in "
								+ fm.formatWorld(world) + "!");
				}
			}
		}
		return true;
	}
	

	private boolean cmdAddRank(Player sender, Player player, String[] args, String world)
	{
		///  ADD // REMOVE
		String classRank = args[2];
		//  Check for Rank is always given
		if (hasClassRank(classRank, player.getName(), world))
		{
			plugin.msg(sender,
							"Player " + fm.formatPlayer(args[1])
							+ " already has the rank "
							+ ChatColor.RED + classRank + "!");
			return true;
		}
		
		// ADD
		Rank tempRank = ClassManager.getRankByPermName( ClassManager.getFirstPermNameByClassName(args[2]));
		
		//  ClassRank does not exist
		if (tempRank == null) 
		{
			plugin.msg(sender,"The class you entered does not exist!"+args[2]);
			return true;
		}

		String cPermName = tempRank.getPermName(); // Display rank name
		String cDispName = tempRank.getDispName(); // Display rank name
		ChatColor c_Color = tempRank.getColor(); // Rank color

		if (plugin.trackRanks) 
		{
			int rID = ClassManager.loadClassProcess(
					Bukkit.getPlayer(args[1]), tempRank.getSuperClass());

			tempRank = tempRank.getSuperClass().ranks.get(rID);

			cPermName = tempRank.getPermName(); // Display rank name
			cDispName = tempRank.getDispName(); // Display rank name
			c_Color = tempRank.getColor(); // Rank color

			String sRank = tempRank.getPermName();
			plugin.perms.rankAdd(world, args[1], sRank);
		} else {
			plugin.perms.rankAdd(world, args[1], cPermName);
//			plugin.perms.classAdd(world, args[1], cPermName);
		}

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
	
	private boolean isPlayerPara(String[] args)
	{
		if (args.length == 2) 
		{
			return true;
		} else
		{
			return false;
		}
	}
	
	/**
	 * Prueft die Paraanzahl , wenn 2 Para dann wird PlayerName ergaemzt
	 * ansonsten wird der Parameter genommen 
	 * @param pPlayer
	 * @param args
	 * @return
	 */
	private String getPlayerName(Player player, String[] args)
	{
		if (isPlayerPara(args)) 
		{
			return args[1];
		} else
		{
			// => /class add *classname*
			return  player.getName();
		}		
	}
	
	/*
	 * The main switch for the command usage. Check for known commands,
	 * permissions, arguments, and commit whatever is wanted.
	 */
	public boolean parseCommand(Player pPlayer, String[] args) 
	{
		Player player = pPlayer;
		db.i("parsing player " + pPlayer + ", command: " + FormatManager.formatStringArray(args));
		if (args.length > 1) 
		{
			// LIST
			if (args[0].equalsIgnoreCase("list")) {
				// Command list of Ranks in the Class  3.2
				cmdList(player,args);
				return true;
				
			} 
			// GET
			if (args[0].equalsIgnoreCase("get")) {
				// Command Get   3.2
				cmdGet(player, args);
				return true;
			}
			// GROUPS
			if (args[0].equalsIgnoreCase("groups")) {
				// Command list of Ranks in the Class  3.2
				plugin.msg(player,"groups "+args[1]);
				
				cmdUserGroups(player, args);
				return true;
			}

//			// more than /class <classname> or /class rankup
//			if (!plugin.perms.hasPerms(pPlayer, "classranks.admin.rank",
//					pPlayer.getWorld().getName())) {
//				plugin.msg(pPlayer,
//						"You don't have permission to change other user's ranks!");
//				return true;
//			}
			
			//RANKUP  SELF
			if ((args[0].equalsIgnoreCase("rankup")))
			{
				// rank up or down
				if (args.length > 1) 
				{
					return rank(args, player);
				} else 
				{
					plugin.msg(player,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
					return false;
				}
				
			}
			//RANKDOWN SELF
			if ((args[0].equalsIgnoreCase("rankdown"))) 
			{
				// rank up or down
				if (args.length > 1) {
					return rank(args, player);
				} else {
					plugin.msg(pPlayer,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
					return false;
				}
			}
			
			// preset of World name
			String world = player.getWorld().getName();
			String PlayerName = getPlayerName(pPlayer, args);
			
			if (args[0].equalsIgnoreCase("remove")) 
			{
				// es müssen mindestens  2 Argumente vorhanden sein
				// class add *classname*
				if (args.length < 2) 
				{
					// not enough arguments ;)
					plugin.msg(pPlayer,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
					return false;
				} else
				{
					// class add *classname* <playerName>
					if (args.length > 2) 
					{
						if (hasAdminPerms(pPlayer))
						{
							db.i("perm check successful");
							// get the class of the player we're talking about
							// change PlayerObject
							player = PlayerManager.searchPlayer(PlayerName);

						} else
						{
							return false;
						}
					}
				}
				world = getWorldArg(world, args);
				db.i("precheck successful");
				cmdRemove(pPlayer,player, args, world);
			}

			if ((args[0].equalsIgnoreCase("add")))
			{
				// es müssen mindestens  2 Argumente vorhanden sein
				// class add *classname*
				if (args.length < 2) 
				{
					// not enough arguments ;)
					plugin.msg(pPlayer,"Not enough arguments ("+ String.valueOf(args.length) + ")!");
					return false;
				} else
				{
					// class add *classname* <playerName>
					if (args.length > 2) 
					{
						if (hasAdminPerms(pPlayer))
						{
							db.i("perm check successful");
							// get the class of the player we're talking about
							// change PlayerObject
							player = PlayerManager.searchPlayer(PlayerName);
							// Add Rank
						} else
						{
							return false;
						}
					}
				}
				//world name 
				world = getWorldArg(world, args);
				db.i("precheck successful");
				// !!!! KEIE vollstaendige Bearbeitung des Befehls
				cmdAddRank(pPlayer,player,args, world,xxx);
			}
		}
		
		//---------------------------------------------------------------------------
		// Simple Commands
		//
		if (args.length > 0) 
		{
			// only ONE argument!
			if (args[0].equalsIgnoreCase("config")) 
			{
				// Command list of Ranks in the Class  3.2
				cmdConfig(pPlayer, args);
				return true;
			}

			if (args[0].equalsIgnoreCase("get")) 
			{
				// Command Get   3.2
				cmdGet(pPlayer, args);
				return true;
			}

			if (args[0].equalsIgnoreCase("groups")) {
				// Command list of Ranks in the Class  3.2
				cmdUserGroups(pPlayer, args);
				return true;
			}
			
			if (args[0].equalsIgnoreCase("reload")) {
				// Command reload 
				if (pPlayer.isOp()) {
					plugin.config.load_config();
					plugin.msg(pPlayer, "Config reloaded!");
				} else {
					plugin.msg(pPlayer, "You don't have permission to reload!");
				}
				return true;
			}

			if (args[0].equalsIgnoreCase("rankup")) {
				// Command rankup
				if (!plugin.perms.hasPerms(pPlayer, "classranks.self.rank",
						pPlayer.getWorld().getName())) {
					plugin.msg(pPlayer,
							"You don't have permission to rank yourself up!");
					return true;
				}
				return rank(args, pPlayer);
			} else if (args[0].equalsIgnoreCase("rankdown")) {
				// Command  rank down
				if (!plugin.perms.hasPerms(pPlayer, "classranks.self.rank",
						pPlayer.getWorld().getName())) {
					plugin.msg(pPlayer,
							"You don't have permission to rank yourself down!");
					return true;
				}
				return rank(args, pPlayer);
			} else if (args[0].equalsIgnoreCase("remove")) {
				// Command remove
				if (!plugin.perms.hasPerms(pPlayer,
						"classranks.self.addremove", pPlayer.getWorld()
								.getName())) {
					plugin.msg(pPlayer,
							"You don't have permission to add/remove your rank!");
					return true;
				}
				Rank rank = ClassManager.getRankByPermName(plugin.perms
						.getPermNameByPlayer(pPlayer.getWorld().getName(),
								pPlayer.getName()));

				if (rank == null) {
					plugin.msg(pPlayer, "You don't have a class!");
					return true;
				}

				String cDispName = rank.getDispName(); // Display rank name
				ChatColor c_Color = rank.getColor(); // Rank color
				if (plugin.trackRanks) {
					ClassManager.saveClassProgress(pPlayer);
				}
				plugin.perms.rankRemove(plugin.config.isDefaultrankallworlds() ? "all" : pPlayer
						.getWorld().getName(), pPlayer.getName(), rank
						.getPermName());

				plugin.msg(
						pPlayer,
						"You were removed from rank "
								+ c_Color
								+ cDispName
								+ ChatColor.WHITE
								+ " in "
								+ fm.formatWorld(plugin.config.isDefaultrankallworlds() ? "all"
										: pPlayer.getWorld().getName()) + "!");
				return true;
			} else if (args[0].equalsIgnoreCase("add")) 
			{
				// Command add 				
				plugin.msg(pPlayer, ChatColor.RED+"Not enough arguments!");
				plugin.msg(pPlayer, ChatColor.GRAY+"/class [add] {username} {classname} {world} | Add user to a class");
				return true;

			} 
			if (args[0].equalsIgnoreCase("list")) 
			{
				// Command  list Classes
				ArrayList<Class> classes = ClassManager.getClasses();
				plugin.msg(pPlayer, ChatColor.YELLOW+"Class List");
				plugin.msg(pPlayer, ChatColor.YELLOW+"--------------------");
				
				for (Class c : classes) {
					if (c.name.startsWith("%") && !pPlayer.isOp()) {
						continue;
					}
					plugin.msg(pPlayer, "Class "+ ChatColor.GREEN + c.name);
//					for (Rank r : c.ranks) {
//						plugin.msg(pPlayer, "=> " + r.getColor() + r.getDispName());
//					}
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("info")) 
			{
				
			}

			// /class [classname]
			if (!plugin.perms.hasPerms(pPlayer, "classranks.self.rank", pPlayer
					.getWorld().getName())) {
				plugin.msg(pPlayer,
						"You don't have permission to choose your class!");
				return true;
			}

			String sRankInfo = plugin.perms.getPermNameByPlayer(pPlayer
					.getWorld().getName(), pPlayer.getName());

			if (!sRankInfo.equals("")) {
				plugin.msg(pPlayer, "You already have the rank " + sRankInfo
						+ "!");
				return true;
			}
			
			String s = "";

			if (plugin.trackRanks) {
				s = ClassManager.getFirstPermNameByClassName(args[0],
						pPlayer.getName());
			} else {
				s = ClassManager.getFirstPermNameByClassName(args[0]);
			}

			if (s == null || s.equals("")) {
				plugin.msg(pPlayer,
						"The class you have entered does not exist!");
				return true;
			}
			Rank rank = ClassManager.getRankByPermName(s);

			if (rank == null) {
				plugin.msg(pPlayer, "Internal Error #1!");
				return true;
			}
			db.i("rank check successful");

			int rID = 0;
			if (plugin.trackRanks) {
				rID = ClassManager.loadClassProcess(pPlayer,
						rank.getSuperClass());
			}
			double rank_cost = 0D;
			
			try {
				rank_cost = moneyCost[rID];
			} catch (Exception e) {
				plugin.log("cost not set: "+rID, Level.WARNING);
			}

			if (rank.getCost() != -1337D) {
				rank_cost = rank.getCost();
			}
			// Check if the player has got the money
			if (ClassRanks.economy != null) {
				if (plugin.getConfig().getBoolean("checkprices")
						&& ClassRanks.economy.has(pPlayer.getName(), rank_cost)) {

					plugin.msg(pPlayer,
							"You don't have enough money to choose your class! ("
									+ ClassRanks.economy.format(rank_cost)
									+ ")");
					return true;
				}
			} else if (plugin.method != null) {
				MethodAccount ma = plugin.method.getAccount(pPlayer.getName());
				if (plugin.getConfig().getBoolean("checkprices")
						&& !ma.hasEnough(rank_cost)) {

					plugin.msg(pPlayer,
							"You don't have enough money to choose your class! ("
									+ plugin.method.format(rank_cost)
									+ ")");
					return true;
				}
			}
			db.i("money check successful");
			if (expCost != null && expCost.length > 0) {
				int exp_cost = -1;
				
				try {
					exp_cost = expCost[rID];
				} catch (Exception e) {
					plugin.log("Exp cost not set: "+rID, Level.WARNING);
				}
				
				if (rank.getExp() != -1) {
					exp_cost = rank.getExp();
				}
				
				if (exp_cost > 0) {
					if (pPlayer.getTotalExperience() < exp_cost) {
						plugin.msg(pPlayer,
								"You don't have enough experience! You need "
										+ exp_cost);
						return true;
					}
					pPlayer.setTotalExperience(pPlayer.getTotalExperience()
							- exp_cost);
					plugin.msg(pPlayer, "You paid " + exp_cost
							+ " experience points!");
				}
			}
			db.i("exp check successful");

			ItemStack[] items = null;
			ItemStack[][] rankItems = plugin.config.getRankItems(); 
			
			if (rankItems != null && (rankItems[rID] != null)
					&& (!FormatManager.formatItemStacks(rankItems[rID]).equals(
							""))) {
				items = rankItems[rID];
			}

			if (rank.getItemstacks() != null) {
				items = rank.getItemstacks();
			}
			
			if (items != null) {
				
				if (!pm.ifHasTakeItems(pPlayer, items)) {
					plugin.msg(pPlayer, "You don't have the required items!");
					plugin.msg(
							pPlayer,
							"("
									+ FormatManager
											.formatItemStacks(items)
									+ ")");
					return true;
				}
			}
			db.i("item check successful");

			String cPermName = rank.getPermName(); // Display rank name
			String cDispName = rank.getDispName(); // Display rank name
			ChatColor c_Color = rank.getColor(); // Rank color

			// success!
			
			if (plugin.getConfig().getBoolean("clearranks")) {
				plugin.perms.removeGroups(pPlayer);
			}

			plugin.perms.rankAdd(plugin.config.isDefaultrankallworlds() ? "all" : pPlayer
					.getWorld().getName(), pPlayer.getName(), cPermName);
			if (plugin.config.isRankpublic()) {
				plugin.getServer().broadcastMessage(
						"Player " + fm.formatPlayer(pPlayer.getName())
								+ " has chosen a class, now has the rank "
								+ c_Color + cDispName);
			} else {
				plugin.msg(pPlayer,
						"You have chosen your class! You now have the rank "
								+ c_Color + cDispName);
			}

			if (plugin.getConfig().getBoolean("checkprices")
					&& (rank_cost > 0)) {
				// if it costs anything at all

				if (ClassRanks.economy != null) {
					ClassRanks.economy.withdrawPlayer(pPlayer.getName(), rank_cost);
					pPlayer.sendMessage(ChatColor.DARK_GREEN + "["
							+ ChatColor.WHITE + "Money" + ChatColor.DARK_GREEN
							+ "] " + ChatColor.RED + "Your account had "
							+ ChatColor.WHITE
							+ ClassRanks.economy.format(rank_cost) + ChatColor.RED
							+ " debited.");
				} else if ((plugin.method != null)) {
					MethodAccount ma = plugin.method.getAccount(pPlayer.getName());
					ma.subtract(rank_cost);
					pPlayer.sendMessage(ChatColor.DARK_GREEN + "["
							+ ChatColor.WHITE + "Money" + ChatColor.DARK_GREEN
							+ "] " + ChatColor.RED + "Your account had "
							+ ChatColor.WHITE
							+ plugin.method.format(rank_cost) + ChatColor.RED
							+ " debited.");
				}
			}
			return true;
		}
		plugin.msg(pPlayer,ChatColor.YELLOW+"Not enough arguments !");
		return false;
	}

	public boolean parseAdminCommand(Player pPlayer, String[] args) 
	{
		db.i("parsing admin " + pPlayer.getName() + ", command: "
				+ FormatManager.formatStringArray(args));

		if (!plugin.perms.hasPerms(pPlayer, "classranks.admin.admin", pPlayer
				.getWorld().getName())) {
			plugin.msg(pPlayer,
					"You don't have permission to administrate ranks!");
			return true;
		}

		db.i("perm check successful");

		if (args.length < 3) {
			plugin.msg(pPlayer,
					"Not enough arguments (" + String.valueOf(args.length)
							+ ")!");
			return false;
		}
		db.i("enough arguments");

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
