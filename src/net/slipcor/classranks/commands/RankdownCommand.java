package net.slipcor.classranks.commands;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Rank;
import net.slipcor.classranks.managers.ClassManager;
import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.managers.FormatManager;
import net.slipcor.classranks.managers.PlayerManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * rankdown command class
 * 
 * @version v0.2.1
 * 
 * @author slipcor
 */

public class RankdownCommand extends AbstractClassCommand  
{
	
	/**
	 * create a rankdown command instance
	 * @param cr the plugin instance to hand over
	 * @param cm the command manager instance to hand over
	 */
	public RankdownCommand(ClassRanks plugin) 
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
		plugin.msg(pPlayer, ChatColor.YELLOW+"/rankdown [classname] | Choose the class for yourself");
		plugin.msg(pPlayer, ChatColor.YELLOW+"need permissions <classranks.self.rank>");
		
	}
	

	
	/**
	 * The main switch for the command usage. Check for known commands,
	 * permissions, arguments, and commit whatever is wanted.
	 * 
	 * <pre>
     /class [rankdown][playername] {world} | Rank user down
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
			plugin.msg(pPlayer,ChatColor.RED+"You don't have permission to choose your class!");
			return true;
		}

		//RANKDOWN 
		//RANKUP for multiClasses
		if (plugin.config.isOnlyoneclass() == false)
		{
			if (args.length == 0) 
			{
				plugin.msg(pPlayer,ChatColor.RED+"Not enough arguments ("+ String.valueOf(args.length) + ")!");
				plugin.msg(pPlayer,ChatColor.GREEN+" /rankup [classname] {world} | Rank user up");
				return true;
			}
			
			// rank up or down
			if (args.length > 1) 
			{
				world = args[1];
			}
			className = args[0];
			return rankDown(pPlayer, pPlayer, className, world);
		}
		
		

		if (isSelf == false) 
		{
			plugin.msg(pPlayer,ChatColor.RED+"You don't have permission to rankdown your class!");
			return true;
		} else
		{
			Rank rank = ClassManager.getRankByPermName(plugin.perms.getPlayerGroups(pPlayer));
			if (rank != null)
			{

				className = ClassManager.getClassNameByPermName(rank.getPermName());
				if (existClass(className))
				{
					rankDown(pPlayer, pPlayer, className, world);
					return true;
				} else
				{
					plugin.msg(pPlayer,ChatColor.RED+"The class name are not found!");
					plugin.msg(pPlayer,ChatColor.RED+"Are have any class or rank ?");
					plugin.msg(pPlayer,"/class info show your class and rank");
					return true;
				}
			} else
			{
				plugin.msg(pPlayer,ChatColor.RED+"The class name are not found!");
				plugin.msg(pPlayer,ChatColor.RED+"Are have any class or rank ?");
				plugin.msg(pPlayer,"/class info show your class and rank");
				return true;
			}
		}
		
	}
	
}
