package net.slipcor.classranks.commands;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Clazz;
import net.slipcor.classranks.core.Rank;
import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.managers.FormatManager;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * classadmin command class
 * 
 * @version v0.2.1
 * 
 * @author slipcor
 */

public class ClassAdminCommand implements CommandExecutor
{
	private final ClassRanks plugin;
	private DebugManager db;

	/**
	 * create a class admin command instance
	 * 
	 * @param cr
	 *            the plugin instance to hand over
	 * @param cm
	 *            the command manager instance to hand over
	 */
	public ClassAdminCommand(ClassRanks plugin)
	{
		this.plugin = plugin;
		this.db = plugin.getDebugManager();
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args)
	{
		if (!(sender instanceof Player))
		{
			plugin.msg(sender, "Console access is not implemented. ");
			plugin.msg(sender, "Because a player instance is necessary");
			return true;
		}
		// admin class command, parse it!
		db.i("/classadmin detected! parsing...");
		return parseAdminCommand((Player) sender, args);
	}

	public void cmdNone(Player pPlayer)
	{
		plugin.msg(pPlayer, "Plugin ClassRanks Ver: "
				+ plugin.getDescription().getVersion());
		plugin.msg(pPlayer, ChatColor.YELLOW
				+ "-----------------------------------------");
		plugin.msg(pPlayer, ChatColor.YELLOW
				+ "usage: [] = required  {} = optional ");
		plugin.msg(pPlayer, ChatColor.YELLOW
				+ "description: Administrate the Classes and Ranks");
		plugin.msg(pPlayer, ChatColor.YELLOW
				+ "usage: only for Ops and Admin (classrank.admin)");
		plugin.msg(pPlayer, ChatColor.YELLOW + " ");
		plugin.msg(
				pPlayer,
				ChatColor.YELLOW
						+ "/classadmin add class [classname] [permname] [displayname] [color] [item:0] [cost] [exp]");
		plugin.msg(
				pPlayer,
				ChatColor.YELLOW
						+ "/classadmin add rank  [classname] [permname] [displayname] [color] [item:0] [cost] [exp]");
		plugin.msg(pPlayer, ChatColor.YELLOW
				+ "/classadmin change class [classname] [newname]");
		plugin.msg(
				pPlayer,
				ChatColor.YELLOW
						+ "/classadmin change rank  [classname] [rankname] [displayname] [color]");
		plugin.msg(pPlayer, ChatColor.YELLOW
				+ "/classadmin remove class [classname]" + ChatColor.RED
				+ "Be careful ");
		plugin.msg(pPlayer, ChatColor.YELLOW
				+ "/classadmin remove rank [classname] [rankname]"
				+ ChatColor.RED + "Be careful ");
		plugin.msg(pPlayer, ChatColor.YELLOW + " ");
		plugin.msg(pPlayer, ChatColor.YELLOW
				+ "/classadmin list {classname} {rankname}");
		plugin.msg(pPlayer, ChatColor.YELLOW + "/classadmin config ");
		plugin.msg(pPlayer, ChatColor.YELLOW + "/classadmin debug [TRUE/FALSE]");

	}

	private void cmdList(Player pPlayer, String[] args)
	{

		plugin.msg(pPlayer, "[ClassRanks] " + ChatColor.YELLOW + "Class List");
		plugin.msg(pPlayer, ChatColor.YELLOW + "--------------------");

		for (Clazz clazz : plugin.clazzList().getClazzes().values())
		{
			if (clazz.clazzName().startsWith("%") && !pPlayer.isOp())
			{
				continue;
			}
			plugin.msg(pPlayer, "Class " + ChatColor.GREEN + clazz.clazzName());
		}

	}

	/**
	 * printout Class List
	 * 
	 * @param pPlayer
	 * @param args
	 */
	private void cmdListClass(Player pPlayer, String[] args)
	{

		if (args.length > 1)
		{
			for (Clazz clazz : plugin.clazzList().getClazzes().values())
			{
				if (clazz.clazzName().equalsIgnoreCase(args[1]))
				{
					plugin.msg(pPlayer, "[ClassRanks] " + ChatColor.YELLOW
							+ "Class " + ChatColor.GREEN + clazz.clazzName());
					plugin.msg(pPlayer, ChatColor.YELLOW
							+ "--------------------");
					for (Rank r : clazz.ranks().values())
					{
						plugin.msg(pPlayer,
						+ clazz.ranks().getRankIndex(r.getPermName())+":" 
						+ r.getPermName() + " : " + r.getColor()
										+ r.getDispName());
					}
				}
			}
		}

	}

	private void cmdListClassRank(Player pPlayer, String[] args)
	{

		if (args.length > 1)
		{
			for (Clazz clazz : plugin.clazzList().getClazzes().values())
			{
				if (clazz.clazzName().equalsIgnoreCase(args[1]))
				{
					plugin.msg(pPlayer, "[ClassRanks] " + ChatColor.YELLOW
							+ "Class " + ChatColor.GREEN + clazz.clazzName());
					plugin.msg(pPlayer, ChatColor.YELLOW
							+ "--------------------");
					for (Rank r : clazz.ranks().values())
					{
						if (r.getPermName().equalsIgnoreCase(args[2]))
						{
							plugin.msg(pPlayer,
									"Rank: " + r.getColor() + r.getDispName());
							plugin.msg(pPlayer,
									"Perm: " + r.getColor() + r.getPermName());
							plugin.msg(pPlayer,
									"Cost: " + r.getColor() + r.getCost());
							plugin.msg(pPlayer,
									"Exp  : " + r.getColor() + r.getExp());
							plugin.msg(pPlayer, "Items : ");
							if (r.getItems() != null)
							{
								for (String sItem : r.getItems())
								{
									plugin.msg(pPlayer, "=> " + r.getColor()
											+ sItem);
								}
								for (ItemStack item : r.getItemstacks())
								{
									if (item != null)
									{
										plugin.msg(
												pPlayer,
												"=> " + r.getColor()
														+ item.getType() + ":"
														+ item.getAmount());
									}
								}
							}
							plugin.msg(pPlayer, " ");
						}
					}
				}
			}
		}

	}

	private boolean cmdConfig(Player pPlayer, String[] args)
	{
		pPlayer.sendMessage(ChatColor.YELLOW + "[ClassRanks] "
				+ ChatColor.GREEN + "Configuration ");
		pPlayer.sendMessage(ChatColor.YELLOW + "----------" + "Ver: "
				+ ChatColor.GREEN + plugin.getDescription().getVersion());
		pPlayer.sendMessage(ChatColor.YELLOW + "Debug  :  " + ChatColor.RED
				+ plugin.config.isDebug());
		pPlayer.sendMessage(ChatColor.YELLOW + "Update :  " + ChatColor.GREEN
				+ plugin.config.isUpdateCheck());
		pPlayer.sendMessage(ChatColor.WHITE + "AutoUpdate : " + ChatColor.GREEN
				+ plugin.config.isAutoUpdate());
		pPlayer.sendMessage(ChatColor.YELLOW + "OneClass  : " + ChatColor.GREEN
				+ plugin.config.isOnlyoneclass());
		pPlayer.sendMessage(ChatColor.YELLOW + "CheckExp  :  "
				+ ChatColor.GREEN + plugin.config.isCheckexp());
		pPlayer.sendMessage(ChatColor.YELLOW + "CheckItem :  "
				+ ChatColor.GREEN + plugin.config.isCheckitems());
		pPlayer.sendMessage(ChatColor.YELLOW + "CheckMoney:  "
				+ ChatColor.GREEN + plugin.config.isCheckprices());
		pPlayer.sendMessage(ChatColor.YELLOW + "CoolDown: " + ChatColor.GREEN
				+ plugin.config.getCoolDown());
		pPlayer.sendMessage(ChatColor.YELLOW + "ClearRanks:  "
				+ ChatColor.GREEN + plugin.config.isClearranks());
		pPlayer.sendMessage(ChatColor.YELLOW + "DefaultRankWorld: "
				+ ChatColor.GREEN + plugin.config.isDefaultrankallworlds());
		pPlayer.sendMessage(ChatColor.YELLOW + "RankPublic:  "
				+ ChatColor.GREEN + plugin.config.isRankpublic());
		pPlayer.sendMessage(ChatColor.YELLOW + "TrackRanks:  "
				+ ChatColor.GREEN + plugin.config.isTrackRanks());
		pPlayer.sendMessage(ChatColor.GREEN + "Default Cost  :  ");
		for (int i = 0; i < plugin.config.getMoneyCost().length; i++)
		{
			pPlayer.sendMessage(ChatColor.YELLOW + "Rank " + (i + 1) + ": "
					+ ChatColor.GREEN
					+ String.valueOf(plugin.config.getMoneyCost()[i]));

		}
		pPlayer.sendMessage(ChatColor.GREEN + "Default Exp  :  ");
		for (int i = 0; i < plugin.config.getExpCost().length; i++)
		{
			pPlayer.sendMessage(ChatColor.YELLOW + "Rank " + (i + 1) + ": "
					+ ChatColor.GREEN
					+ String.valueOf(plugin.config.getExpCost()[i]));

		}
		pPlayer.sendMessage(ChatColor.GREEN + "Default Item  :  ");
		for (int i = 0; i < plugin.config.getItemStacks().length; i++)
		{
			pPlayer.sendMessage(ChatColor.GREEN + "Rank: " + (i + 1));
			for (int j = 0; j < plugin.config.getItemStacks()[i].length; j++)
			{
				pPlayer.sendMessage(ChatColor.YELLOW + "   => "
						+ ChatColor.GREEN
						+ plugin.config.getItemStacks()[i][j].getType() + " : "
						+ plugin.config.getItemStacks()[i][j].getAmount());
			}

		}

		return true;
	}

	private boolean cmdDebug(Player player, String[] args)
	{
		if (args[1].equalsIgnoreCase("TRUE"))
		{
			plugin.config.setDebug(true);
		}
		if (args[1].equalsIgnoreCase("FALSE"))
		{
			plugin.config.setDebug(false);
		}
		plugin.config.saveConfig();
		player.sendMessage(ChatColor.YELLOW + "[ClassRanks] " + ChatColor.GREEN
				+ "DEBUG Mode ");
		player.sendMessage(ChatColor.YELLOW + "----------" + "Ver: "
				+ ChatColor.GREEN + plugin.getDescription().getVersion());
		player.sendMessage(ChatColor.YELLOW + "Debug  :  " + ChatColor.RED
				+ plugin.config.isDebug());
		return true;
	}

	public boolean parseAdminCommand(Player pPlayer, String[] args)
	{
		db.i("parsing admin " + pPlayer.getName() + ", command: "
				+ FormatManager.formatStringArray(args));

		if (!plugin.perms.hasPerms(pPlayer, "classranks.admin.admin", pPlayer
				.getWorld().getName()))
		{
			plugin.msg(pPlayer,
					"You don't have permission to administrate ranks!");
			return true;
		}

		db.i("perm check successful");

		if (args.length == 0)
		{
			cmdNone(pPlayer);
			return true;
		}

		if (args[0].equalsIgnoreCase("debug"))
		{
			cmdDebug(pPlayer, args);
			return true;
		}
		if (args[0].equalsIgnoreCase("config"))
		{
			cmdConfig(pPlayer, args);
			return true;
		}
		if (args[0].equalsIgnoreCase("list"))
		{
			if (args.length == 1)
			{
				cmdList(pPlayer, args);
				return true;
			}
			if (args.length == 2)
			{
				cmdListClass(pPlayer, args);
				return true;
			}
			if (args.length == 3)
			{
				cmdListClassRank(pPlayer, args);
				return true;
			}
			return false;

		}

		if (args.length < 3)
		{
			plugin.msg(pPlayer,
					"Not enough arguments (" + String.valueOf(args.length)
							+ ")!");
			return false;
		}
		db.i("enough arguments");

		if (args[0].equalsIgnoreCase("remove"))
		{
			/*
			 * /classadmin remove class *classname* /classadmin remove rank
			 * *classname* *rankname*
			 */
			if (args[1].equalsIgnoreCase("class"))
			{
				if (args.length != 3)
				{
					plugin.msg(
							pPlayer,
							"Wrong number of arguments ("
									+ String.valueOf(args.length) + ")!");
					return false;
				}
				return plugin.clazzList().configClassRemove(args[2], pPlayer);
			} else if (args[1].equalsIgnoreCase("rank"))
			{
				if (args.length != 4)
				{
					plugin.msg(
							pPlayer,
							"Wrong number of arguments ("
									+ String.valueOf(args.length) + ")!");
					return false;
				}
				return plugin.clazzList().configRankRemove(args[2], args[3],
						pPlayer);
			}
			// second argument unknown
			return false;
		}
		if (args[0].equalsIgnoreCase("add"))
		{

			/*
			 * Add Class and first Rank /classadmin add class *classname*
			 * *rankname* *displayname* *color*
			 * 
			 * Add Rank to Class /classadmin add rank *classname* *rankname*
			 * *displayname* *color*
			 */

			if (args.length < 6)
			{
				plugin.msg(
						pPlayer,
						"Wrong number of arguments ("
								+ String.valueOf(args.length)
								+ ") should be minimum 6 !");
				cmdNone(pPlayer);
				return true;
			}
			ItemStack[] items;
			if (args.length >= 7)
			{
				items = FormatManager.getItemStacksFromCommaString(args[6]);
			} else
			{
				items = null;
			}
			double cost;
			if (args.length >= 8)
			{
				cost = Double.parseDouble(args[7]);
			} else
			{
				cost = 0.0;
			}
			int exp;
			if (args.length >= 9)
			{
				exp = Integer.parseInt(args[8]);
			} else
			{
				exp = 0;
			}
			if (args[1].equalsIgnoreCase("class"))
			{// , ItemStack[] isItems, double dCost, int iExp
				// classadmin add class [classname] [permname] [displayname]
				// [color] [item|0] [cost] [exp]
				// args[0] = add
				// args[1] = class
				// args[2] = clazzName
				// args[3] = rankName
				// args[4] = displayName
				// args[5] = Color
				// args[6] =
				return plugin.clazzList().AddClazzRank(args[2], args[3],
						args[4], args[5], items, cost, exp, null); // pPlayer);
			}
			if (args[1].equalsIgnoreCase("rank"))
			{
				// classadmin add rank [classname] [permname] [displayname]
				// [color] [item|0] [cost] [exp]
				return plugin.clazzList().AddClazzRank(args[2], args[3],
						args[4], args[5], items, cost, exp, null); // pPlayer);
			}
			// second argument unknown
			return false;
		} else if (args[0].equalsIgnoreCase("change"))
		{

			/*
			 * /classadmin change rank *classname* *rankname* *displayname*
			 * *color* /classadmin change class *classname* *newclassname*
			 */

			if (args[1].equalsIgnoreCase("class"))
			{
				if (args.length != 4)
				{
					plugin.msg(
							pPlayer,
							"Wrong number of arguments ("
									+ String.valueOf(args.length) + ")!");
					return false;
				}
				return plugin.clazzList()
						.ClassChange(args[2], args[3], pPlayer);
			} else if (args[1].equalsIgnoreCase("rank"))
			{
				if (args.length != 6)
				{
					plugin.msg(pPlayer,"Wrong number of arguments ("+ String.valueOf(args.length) + ")!");
					return false;
				}
				return plugin.clazzList().RankChange(args[2], args[3],
						args[4], args[5], pPlayer);
			}
			// second argument unknown
			return false;
		}
		return false;
	}

}
