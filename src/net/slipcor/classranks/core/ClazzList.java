package net.slipcor.classranks.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Clazz;
import net.slipcor.classranks.managers.FormatManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * This class hold the list of defined Clazzes
 * 
 * - List of Clazzes as Key / Value - key = ClazzName - value = Clazz
 * 
 * 
 * @version v0.7.1
 * 
 * @author krglok
 */

public class ClazzList
{
	private HashMap<String, Clazz> clazzes ;
	private ClassRanks plugin;

	// private static DebugManager db;

	public ClazzList(ClassRanks plugin)
	{
		this.plugin = plugin;
		clazzes = new HashMap<String, Clazz>();
		// db = new DebugManager(cr);
	}

	public int getIndex(Clazz cClazz)
	{
		int index = 0;
		for (Clazz clazz : clazzes.values())
		{
			if (clazz.clazzName().equalsIgnoreCase(cClazz.clazzName()))
			{
				return index;
			}
			index++;
		}
		return 0;
	}

	/**
	 * search for ClazzName with ignoreCase
	 * 
	 * @param sClassname
	 * @return true if found
	 */
	public boolean containClazz(String sClassname)
	{
		for (String key : clazzes.keySet())
		{
			if (key.equalsIgnoreCase(sClassname))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * create new Clazz and add to the List of clazzes check if clazzName are
	 * always available the key is uppercase , ignoreCase in the list
	 * 
	 * @param sClassName
	 */
	public Clazz add(String sClassName)
	{
		Clazz clazz;
		if (this.containClazz(sClassName) == false)
		{
			String key = sClassName.toUpperCase();
			clazz = new Clazz(sClassName); 
			clazzes.put(key, clazz);
		} else
		{
			clazz = getClassbyClassName(sClassName);
		}
		return clazz;
	}

	public String getFirstPermNameByClassName(String sClassName)
	{
		// standard version: get first rank
		if (this.containClazz(sClassName))
		{
			String key = sClassName.toUpperCase();
			Clazz clazz = this.clazzes.get(key);
			if (clazz.ranks().size() > 0)
			{
				return clazz.ranks().get(1).getPermName();
			}
		}
		return null;
	}

	// public static String getFirstPermNameByClassName(String cString, String
	// sPlayer)
	// {
	// // extended version: get rank
	// for (Clazz c : clazzes)
	// {
	// if (c.name.equals(cString))
	// {
	// return c.ranks
	// .get(ClazzList.loadClassProcess(
	// Bukkit.getPlayer(sPlayer), c)).getPermName();
	// }
	// }
	// return null;
	// }

	/**
	 * search for classname by Rank / sPermname
	 * 
	 * @param rank
	 * @return clazzName or null
	 */
	public String getClassNameByPermName(String permName)
	{
		for (Clazz clazz : clazzes.values())
		{
			for (Rank r : clazz.ranks().values())
			{
				if (r.getPermName().equalsIgnoreCase(permName))
				{
					return clazz.clazzName();
				}
			}
		}
		return null;
	}

	/**
	 * search permissionName in permissionGroup (via Vault)
	 * 
	 * @param permGroups
	 * @return permissionName or emptyString
	 */
	public String getLastPermNameByPermGroups(ArrayList<String> permGroups)
	{
		String sPermName = "";
		for (Clazz clazz : clazzes.values())
		{
			for (Rank rank : clazz.ranks().values())
			{
				plugin.db.i(clazz.clazzName() + " => " + rank.getPermName());
				if (permGroups.contains(rank.getPermName()))
				{
					sPermName = rank.getPermName();
				}
			}
		}
		return sPermName;
	}

	/**
	 * search for Rank by permissionName
	 * 
	 * @param sPermName
	 * @return Rank or null
	 */
	public Rank getRankByPermName(String sPermName)
	{
		for (Clazz clazz : clazzes.values())
		{
			for (Rank rank : clazz.ranks().values())
			{
				if (rank.getPermName().equals(sPermName))
				{
					return rank;
				}
			}
		}
		return null;
	}

	/**
	 * search for Rank in a List of PermissionName
	 * 
	 * @param sPermName
	 * @return Rank or null
	 */
	public Rank getRankByPermName(String[] sPermName)
	{
		for (Clazz clazz : clazzes.values())
		{
			for (Rank rank : clazz.ranks().values())
			{
				for (String permName : sPermName)
				{
					plugin.db.i("Rank: " + rank.getPermName() + " Group: "
							+ permName);
					if (rank.getPermName().equals(permName))
					{
						return rank;
					}
				}
			}
		}
		return null;
	}

	public Clazz getClazzByRank(Rank rank)
	{
		for (Clazz clazz : this.clazzes.values())
		{
			if (clazz.clazzName()
					.equalsIgnoreCase(rank.getSuperClass().clazzName()))
			{
				return clazz;
			}
		}
		return null;
	}

	public Rank getNextRank(Rank rank)
	{
		Clazz clazz = getClazzByRank(rank);
		if (clazz != null)
		{
			return clazz.ranks().nextRank(rank.getPermName());
		}
		return null;
	}

	public Rank getPrevRank(Rank rank)
	{
		Clazz clazz = getClazzByRank(rank);
		if (clazz != null)
		{
			return clazz.ranks().prevRank(rank.getPermName());
		}
		return null;
	}

	private Clazz getClassbyClassName(String sClassName)
	{
		if (this.containClazz(sClassName))
		{
			sClassName = sClassName.toUpperCase();
			return this.clazzes.get(sClassName);
		}
		return null;
	}

	public boolean rankExists(String sRank)
	{
		for (Clazz clazz : clazzes.values())
		{
			for (Rank r : clazz.ranks().values())
			{
				if (r.getPermName().equals(sRank))
				{
					return true;
				}
			}
		}
		return false;
	}

	public HashMap<String, Clazz> getClazzes()
	{
		return clazzes;
	}

	public boolean configClassRemove(String sClassName, Player pPlayer)
	{
		Clazz cClass = getClassbyClassName(sClassName);
		if (cClass != null)
		{
			clazzes.remove(cClass);
			plugin.msg(pPlayer, "Clazz removed: " + sClassName);
			plugin.config.writeClazzConfig();
			return true;
		}
		plugin.msg(pPlayer, "Clazz not found: " + sClassName);
		return true;
	}

	public boolean configRankRemove(String sClassName, String sPermName,
			Player pPlayer)
	{
		Clazz clazz = getClassbyClassName(sClassName);
		if (clazz != null)
		{
			Rank rank = getRankByPermName(sPermName);
			if (rank != null)
			{
				ChatColor cColor = rank.getColor();
				clazz.removeRank(rank.getPermName());
				plugin.msg(pPlayer, "Rank removed: "+sClassName+":" + cColor + sPermName);
				plugin.config.writeClazzConfig();
				return true;
			}
			plugin.msg(pPlayer, "Rank not found: " + sPermName);
			return true;
		}
		plugin.msg(pPlayer, "Clazz not found: " + sClassName);
		return true;
	}

	public boolean importClazz(String sClassName  	// clazz
						, String sPermName		// rank
						, String sDispName		// rank
						, String sColor			// rank
						, ItemStack[] rankItems	// rank
						, double rankCost		// rank
						, int rankExp			// rank
						, Player pPlayer
						)
	{

		Clazz clazz = getClassbyClassName(sClassName);

		if (clazz == null)
		{
			clazz = new Clazz(sClassName);
			clazz.addRank(sPermName, sDispName,	(FormatManager.formatColor(sColor)), rankItems, rankCost, rankExp);
			// add Clazz to ClazzList
			String key = clazz.clazzName().toUpperCase();
			clazzes.put(key, clazz);
			if (pPlayer == null)
			{
				ClassRanks.log("Clazz added:" + sClassName, Level.INFO);
			} else
			{
				plugin.msg(pPlayer, "Clazz added: " + sClassName+":"+sPermName);
			}
			return true;
		} else
		{
			if (clazz.ranks().hasRank(sPermName) == false)
			{
				clazz.addRank(sPermName, sDispName,	(FormatManager.formatColor(sColor)), rankItems, rankCost, rankExp);
			}
		}
		return true;
	}
	
	
	/**
	 * haengt  eine neue Rank an die Clazz an.
	 * Die neue Clazzrank wird  in der Config gespeichert.
	 * Existiert die Clazz nicht wird sie erzeugt !
	 *  
	 * @param sClassName
	 * @param sPermName
	 * @param sDispName
	 * @param sColor
	 * @param rankItems
	 * @param rankCost
	 * @param rankExp
	 * @param pPlayer
	 * @return  commandExecution status true
	 */
	public boolean AddClazzRank(String sClassName
							, String sPermName
							, String sDispName
							, String sColor
							, ItemStack[] rankItems
							, double rankCost
							, int rankExp
							, Player pPlayer)
	{
		Clazz cClass;
		if (plugin.clazzList().containClazz(sClassName))
		{
			cClass = getClassbyClassName(sClassName);
		} else
		{
			cClass = plugin.clazzList().add(sClassName);
		}
		if (cClass != null)
		{
			Rank rank = new Rank(sPermName, sDispName,(FormatManager.formatColor(sColor)), cClass, rankItems,rankCost, rankExp);
			cClass.ranks().addRank(rank);
			if (pPlayer == null)
			{
				ClassRanks.log("Rank added:" + sClassName+":"+sPermName, Level.INFO);
			} else
			{
				plugin.msg(pPlayer,"Rank added: " + sClassName+":"+ (FormatManager.formatColor(sColor))
								+ sPermName );
				// speichern der ClazzRank in config
			}
			plugin.config.writeClazzConfig();
			return true;
		}
		if (pPlayer == null)
			plugin.db.i("Clazz not found: " + sClassName);
		else
			plugin.msg(pPlayer, "Clazz not found: " + sClassName);
		return true;
	}

	/**
	 * 
	 * @param sClassName
	 * @param sClassNewName
	 * @param pPlayer
	 * @return
	 */
	public boolean ClassChange(String sClassName, String sClassNewName,
			Player pPlayer)
	{
		Clazz clazz = getClassbyClassName(sClassName);
		if (clazz != null)
		{
			clazz.setClazzName(sClassNewName);
			plugin.msg(pPlayer, ChatColor.GREEN+"Clazz changed: " + sClassName + " => "	+ sClassNewName);
			plugin.msg(pPlayer, ChatColor.RED+"Player(s) Clazz NOT changed");
			plugin.config.writeClazzConfig();
			return true;
		}
		plugin.msg(pPlayer,ChatColor.RED+ "Clazz not found: " + sClassName);
		return true;
	}

	/**
	 * Change rank data for rank sPermName
	 * Save data to config file
	 * 
	 * @param sClassName
	 * @param sPermName
	 * @param sDispName
	 * @param sColor
	 * @param pPlayer
	 * @return
	 */
	public boolean RankChange(String sClassName, String sPermName,
			String sDispName, String sColor, Player pPlayer)
	{
		Clazz cClass = getClassbyClassName(sClassName);
		if (cClass != null)
		{
			Rank rank = getRankByPermName(sPermName);
			if (rank != null)
			{
				rank.setDispName(sDispName);
				rank.setColor((FormatManager.formatColor(sColor)));
				plugin.msg(pPlayer,
						"Rank updated: " + (FormatManager.formatColor(sColor))
								+ sPermName);
				plugin.config.writeClazzConfig();
				return true;
			}
			plugin.msg(pPlayer, "Rank not found: " + sPermName);
		}
		plugin.msg(pPlayer, "Clazz not found: " + sClassName);
		return true;
	}

	public void saveClassProgress(Player pPlayer)
	{
		plugin.db.i("saving Clazz process");
		String s = plugin.getConfig()
				.getString("progress." + pPlayer.getName());
		plugin.db.i("progress of " + pPlayer.getName() + ": " + s);
		Rank rank = getRankByPermName(plugin.perms.getPermNameByPlayer(pPlayer
				.getWorld().getName(), pPlayer));
		if (rank == null)
		{
			plugin.db.i("rank is null!");
			return;
		}

		int rankID = rank.getSuperClass().ranks().getRankIndex(rank.getPermName());
		plugin.db.i("rank ID: " + rankID);
		int classID = this.getIndex(rank.getSuperClass());
		plugin.db.i("classID: " + classID);

		if (s != null && s.length() == clazzes.size())
		{

			char[] c = s.toCharArray();
			c[classID] = String.valueOf(rankID).charAt(0);
			plugin.db.i("new c[classID]: " + c[classID]);

			plugin.db.i("saving: " + c.toString());
			plugin.getConfig().set("progress." + pPlayer.getName(),
					String.valueOf(c.toString()));

			return;
		}

		plugin.db.i("no entry yet!");
		String result = "";
		for (int i = 0; i < clazzes.size(); i++)
		{
			if (i == classID)
			{
				result += String.valueOf(rankID);
			} else
			{
				result += "0";
			}
		}
		plugin.db.i("setting: " + result);
		plugin.getConfig().set("progress." + pPlayer.getName(),
				String.valueOf(result));
		plugin.saveConfig();
	}

	public int loadClassProcess(Player pPlayer, Clazz cClass)
	{
		try
		{
			String s = plugin.getConfig().getString(
					"progress." + pPlayer.getName());
			int classID = this.getIndex(cClass);

			int rankID = Integer.parseInt(String.valueOf(s.charAt(classID)));
			return rankID;
		} catch (Exception e)
		{
			return 0;
		}
	}

	public int getRankIndex(Rank rank, Clazz clazz)
	{
		// init with 0 to set default to first Rank in Clazz
		return clazz.ranks().getRankIndex(rank.getPermName());
	}

}
