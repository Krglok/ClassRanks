package net.slipcor.classranks.commands;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Clazz;
import net.slipcor.classranks.core.Rank;
import net.slipcor.classranks.managers.ClazzList;
import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.managers.FormatManager;
import net.slipcor.classranks.managers.PlayerManager;
import net.slipcor.classranks.register.payment.Method.MethodAccount;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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

public class PlayerCommands extends AbstractClassCommand
{


	
	// Costs
//	public boolean rankpublic; // do we spam changed to the public?
//	public boolean onlyoneclass; // only maintain oneass
//	public ItemStack[][] rankItems;
//	public String[] signCheck = { null, null, null }; // SignCheck variable

	public PlayerCommands(ClassRanks plugin) 
	{
		super(plugin);
	}

	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
    	if (!(sender instanceof Player)) 
    	{
    		plugin.msg(sender, "Console access is not implemented. ");
    		plugin.msg(sender, "Because a player instance is necessary");
    		return true;
    	}
		// admin class command, parse it!
		db.i("/classadmin detected! parsing...");
		return parseCommand((Player) sender, args);
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
			ClazzList.saveClassProgress(player);
		}
		// suche plass in permissions
		if (hasClass(className, player, world))
		{
			className = getClassName(className);
			// remove permission group
			removeRanks(className, player, world);
			// remove playerSection
			plugin.config.playerSectionRemove(player, className);
			
			// show public message 
			if (plugin.config.isRankpublic() ) 
			{
				// self remove successful!
				plugin.getServer().broadcastMessage(player+
							" are removed from class " + ChatColor.RED
							+ className + ChatColor.WHITE
							+ " in "
							+ FormatManager.formatWorld(world) + "!");
			} else
			{
				// self remove successful!
				plugin.msg(player,ChatColor.GREEN+"Class removed " + ChatColor.RED
							+ className + ChatColor.WHITE
							+ " in "
							+ FormatManager.formatWorld(world) + "!");
				if ((sender.getUniqueId() != player.getUniqueId()))
				{
					// self remove successful!
					plugin.msg(sender,playerName+" are removed from class " + ChatColor.RED
								+ className + ChatColor.WHITE
								+ " in "
								+ FormatManager.formatWorld(world) + "!");
				}
			}
		}
		return true;
	}
	

	
	/**
	 * printout Classas and/or Clazz Ranks
	 * 
	 * @param pPlayer
	 * @param args
	 */
	private void cmdList (Player pPlayer, String[] args) 
	{
	
		ArrayList<Clazz> classes = ClazzList.getClasses();
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
		Rank rank = ClazzList.getRankByPermName(plugin.perms.getPlayerGroups(pPlayer));

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
		boolean isSelf = plugin.perms.hasPerms(pPlayer, "classranks.self.rank", pPlayer.getWorld().getName()); 
		
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
		pPlayer.sendMessage(ChatColor.YELLOW+"SelfRank   :  "+ChatColor.GREEN+isSelf);
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
			plugin.msg(pPlayer, ChatColor.YELLOW+"-------------------------------------------");
			plugin.msg(pPlayer, "[ClassRanks] "+ChatColor.YELLOW+"Class "+ "User Groups of "+ChatColor.DARK_GREEN+playerName);
			for (int i = 0; i < list.length; i++) 
			{
				plugin.msg(pPlayer, ChatColor.YELLOW+": "+list[i]);
			}
			plugin.msg(pPlayer, ChatColor.YELLOW+"Count "+max+" ------------");
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
			plugin.msg(pPlayer,ChatColor.RED+"Not implementedt yet !");
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
					plugin.msg(pPlayer,ChatColor.RED+"Not enough arguments ( 2 )!");
					plugin.msg(pPlayer,ChatColor.GREEN+" /class [rankup] [playername] [classname] {world} | Rank user up");
					return true;
				}
			} else 
			{
				plugin.error(pPlayer, "You don't have the right permission !");
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
					plugin.msg(pPlayer,ChatColor.RED+"Not enough arguments ( 2 )!");
					plugin.msg(pPlayer,ChatColor.GREEN+" /class [rankdown] [playername] [classname] {world} | Rank user down");
					return true;
				}

			} else 
			{
				plugin.error(pPlayer, "You don't have the right permission !");
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
				plugin.error(pPlayer, "You don't have the right permission !");
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
				plugin.msg(pPlayer,ChatColor.RED+"Not enough arguments ( 2 )!");
				plugin.msg(pPlayer,ChatColor.GREEN+" /class [add] [playername] [classname] {world} | Add user to a class");
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
						plugin.error(pPlayer, "Not found player " + PlayerName);
						return true;
					}
				} else
				{
					plugin.error(pPlayer, "You don't have the right permission !");
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
				plugin.error(pPlayer, "You don't have the right permission !");
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
				plugin.error(pPlayer, "You don't have the right permission !");
				return true;
			}
		}
		
		// /class [classname]
		// /class [classname] | Choose the class [classname] for yourself in all worlds

		// command wird nur ausgefuehrt bei permissions
		if (isSelf == false) 
		{
			plugin.msg(pPlayer,ChatColor.RED+"permission classranks.self.rank not set to you , call your admin!");
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
				plugin.error(pPlayer,"The class name not exist!");
				plugin.msg(pPlayer,ChatColor.GREEN+"/class [classname] | Choose the class [classname] for yourself in all worlds");
				return true;
			}
		}
		
	}


}
