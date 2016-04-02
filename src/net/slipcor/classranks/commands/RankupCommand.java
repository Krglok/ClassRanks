package net.slipcor.classranks.commands;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.ClazzList;
import net.slipcor.classranks.core.Rank;
import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.managers.FormatManager;
import net.slipcor.classranks.managers.PlayerManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * rankup command class
 * 
 * @version v0.2.1
 * 
 * @author slipcor
 */

public class RankupCommand extends AbstractClassCommand 
{
	
	/**
	 * create a rankup command instance
	 * @param cr the plugin instance to hand over
	 * @param cm the command manager instance to hand over
	 */
	public RankupCommand(ClassRanks plugin) 
	{
		super(plugin);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		
    	if (!(sender instanceof Player)) {
    		plugin.msg(sender, "Console access is not implemented. If you want that, visit:");
    		plugin.msg(sender, "http://dev.bukkit.org/server-mods/classranks/");
    		return true;
    	}

		// if we use the shortcut /rankup or /rankdown, shift the array
		String[] tStr = new String[args.length+1];
		System.arraycopy(args, 0, tStr, 1, args.length);
		tStr[0] = cmd.getName();
		db.i("shortcut detected, parsed '" + FormatManager.formatStringArray(args) + "' to '" + tStr.toString() + "'");
		return parseCommand((Player) sender, tStr);
	}

	/**
	 * Send command Info for /rank to player
	 * @param pPlayer
	 */
	private void cmdRankInfo (Player pPlayer)
	{
		plugin.msg(pPlayer, ChatColor.YELLOW+"-----------------------------------------");
		plugin.msg(pPlayer, "Plugin ClassRanks Ver: " + plugin.getDescription().getVersion());
		plugin.msg(pPlayer, ChatColor.YELLOW+"usage: [] = required  {} = optional ");
		plugin.msg(pPlayer, ChatColor.YELLOW+"/rankup [classname] | Choose the class for yourself");
		plugin.msg(pPlayer, ChatColor.YELLOW+"need permissions <classranks.self.rank>");
		
	}
	
//	public boolean parseSelfRankUp(Player pPlayer, String[] args) 
//	{
//		String className = "";
//		if (plugin.config.isOnlyoneclass() == false)
//		{
//			if (args.length == 0)
//			{
//				//zeigt command description aus Plugi.yml
//				cmdRankUpInfo (pPlayer);
//				return true;  
//			} 
//			className = args[0];
//		    // rankUp  [classname] | Add yourself to a class
//		} else
//		{
//			
//		}
//		String world = "";
//		
//		//world name auswerten und setzen
//		world = getWorldArg(world, args);
//		plugin.db.i("precheck successful");
//		// Add Rank
//		
//		rankUp(pPlayer, pPlayer, className, world);
//		return true;
//	}
	
	
	/**
	 * <pre>
	 * The main switch for the command usage. Check for known commands,
	 * permissions, arguments, and commit whatever is wanted.
	 * 
	 * <pre>

     /class [rankup]  [playername] {world} | Rank user up

	 * @param pPlayer , the sender of the command
	 * @param args , arguments of the Command /class
	 * @return
     * </pre>
	 */
	public boolean parseCommand(Player pPlayer, String[] args) 
	{
//		Player player = pPlayer;
		plugin.db.i("parsing player " + pPlayer + ", command: " + FormatManager.formatStringArray(args));

		boolean isSelf = plugin.perms.hasPerms(pPlayer, "classranks.self.rank", pPlayer.getWorld().getName()); 
		String world = "all";
		String className = ""; 
				
		if (pPlayer.isOp())
		{
			isSelf = true;
		}

		if (args.length == 0)
		{
			//zeigt command description aus Plugi.yml
			cmdRankInfo (pPlayer);
			return true;  
		}
		
		if (isSelf == false) 
		{
			plugin.msg(pPlayer,ChatColor.RED+"permission classranks.self.rank not set to you , call your admin!");
			return true;
		}

		
		//RANKUP for multiClasses
		if (plugin.config.isOnlyoneclass() == false)
		{
			// rank up or down
			if (args.length == 0) 
			{
				plugin.msg(pPlayer,ChatColor.RED+"Not enough arguments ("+ String.valueOf(args.length) + ")!");
				plugin.msg(pPlayer,ChatColor.GREEN+" /rankup [classname] {world} | Rank user up");
				return true;
			}
			if (args.length > 1) 
			{
				world = args[1];
			}  
			className = args[0];
			return rankUp(pPlayer, pPlayer, className, world);
		} else
		{
			if (isSelf == false) 
			{
				plugin.msg(pPlayer,ChatColor.RED+"You don't have permission to rankup your class!");
				return true;
			} else
			{
				Rank rank = plugin.clazzList().getRankByPermName(plugin.perms.getPlayerGroups(pPlayer));
				if (rank != null)
				{
					className = plugin.clazzList().getClassNameByPermName(rank.getPermName());
					if (existClass(className))
					{
						rankUp(pPlayer, pPlayer, className, world);
						return true;
					} else
					{
						plugin.msg(pPlayer,ChatColor.RED+"The class name are not found!");
						plugin.msg(pPlayer,ChatColor.RED+"Are you have any class or rank ?");
						plugin.msg(pPlayer,"/class get, show your class and rank");
						return true;
					}
				} else
				{
					plugin.msg(pPlayer,ChatColor.RED+"The class name are not found!");
					plugin.msg(pPlayer,ChatColor.RED+"Are you have any class or rank ?");
					plugin.msg(pPlayer,"/class get, show your class and rank");
					return true;
				}
			}
		}
	}
	
}
