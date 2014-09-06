package net.slipcor.classranks.managers;

import java.util.ArrayList;
import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Clazz;
import net.slipcor.classranks.core.Clazz;
import net.slipcor.classranks.core.Rank;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Clazz manager Clazz 
 * - Array of Classes
 * 
 * used as static to workaround of initialize problems ! 
 * 
 * @version v0.4.4.2
 * 
 * @author slipcor / krglok
 */

public class ClassManager 
{
	private static ArrayList<Clazz> clazzes = new ArrayList<Clazz>();
	private static ClassRanks plugin;
//	private static DebugManager db;
	
	public ClassManager(ClassRanks cr) {
		ClassManager.plugin = cr;
//		db = new DebugManager(cr);
	}
	
	public void add(String sClassName) {
		clazzes.add(new Clazz(sClassName));
	}

	public static String getFirstPermNameByClassName(String cString) 
	{
		// standard version: get first rank
		for (Clazz c : clazzes) {
			for (Rank r : c.ranks) {
				if (c.name.equals(cString))
					return r.getPermName();
			}
		}
		return null;
	}

	public static String getFirstPermNameByClassName(String cString, String sPlayer) {
		// extended version: get rank
		for (Clazz c : clazzes) {
			if (c.name.equals(cString)) {
				return c.ranks.get(ClassManager.loadClassProcess(Bukkit.getPlayer(sPlayer), c)).getPermName();
			}
		}
		return null;
	}

	public static String getClassNameByPermName(String rank) {
		for (Clazz c : clazzes) {
			for (Rank r : c.ranks) {
				if (r.getPermName().equals(rank))
					return c.name;
			}
		}
		return null;
	}

	public static String getLastPermNameByPermGroups(ArrayList<String> permGroups) {
		String sPermName = "";
		for (Clazz c : clazzes) {
			for (Rank r : c.ranks) {
				plugin.db.i(c.name + " => " + r.getPermName());
				if (permGroups.contains(r.getPermName()))
					sPermName = r.getPermName();
			}
		}
		return sPermName;
	}

	public static Rank getRankByPermName(String sPermName) 
	{
		for (Clazz c : clazzes) 
		{
			for (Rank r : c.ranks) 
			{
			
				if (r.getPermName().equals(sPermName))
					return r;
			}
		}
		return null;
	}

	public static Rank getRankByPermName(String[] sPermName) 
	{
		for (Clazz c : clazzes) 
		{
			for (Rank r : c.ranks) 
			{
				for (String permName : sPermName)
				{
					plugin.db.i("Rank: "+r.getPermName()+" Group: "+permName);
					if (r.getPermName().equals(permName))
					return r;
				}
			}
		}
		return null;
	}

	public static Rank getNextRank(Rank rank, int rankIndex) 
	{
		if (rankIndex+1 < rank.getSuperClass().ranks.size())
		{
		   return rank.getSuperClass().ranks.get(rankIndex+1);
		} else
		{
			return null;
		}
	}

	public static Rank getPrevRank(Rank rank, int rankIndex) 
	{
		if (rankIndex > 0)
		{
			return rank.getSuperClass().ranks.get(rankIndex-1);
		} else
		{
			return null;
		}
	}
	
	private static Clazz getClassbyClassName(String sClassName) {
		for (Clazz c : clazzes) {
			if (c.name.equals(sClassName))
				return c;
		}
		return null;
	}
	
	public static boolean rankExists(String sRank) {
		for (Clazz c : clazzes) {
			for (Rank r : c.ranks) {
				if (r.getPermName().equals(sRank))
					return true;
			}
		}
		return false;
	}
	
	public static ArrayList<Clazz> getClasses() {
		return clazzes;
	}

	public static boolean configClassRemove(String sClassName, Player pPlayer) {
		Clazz cClass = getClassbyClassName(sClassName);
		if (cClass != null) {
			clazzes.remove(cClass);
			plugin.msg(pPlayer, "Clazz removed: " + sClassName);
			plugin.config.save_config();
			return true;
		}
		plugin.msg(pPlayer, "Clazz not found: " + sClassName);
		return true;
	}

	public static boolean configRankRemove(String sClassName, String sPermName, Player pPlayer) {
		Clazz cClass = getClassbyClassName(sClassName);
		if (cClass != null) {
			Rank rank = getRankByPermName(sPermName);
			if (rank != null) {
				ChatColor cColor = rank.getColor();
				cClass.ranks.remove(rank);
				plugin.msg(pPlayer, "Rank removed: " + cColor + sPermName);
				plugin.config.save_config();
				return true;
			}
			plugin.msg(pPlayer, "Rank not found: " + sPermName);
			return true;
		}
		plugin.msg(pPlayer, "Clazz not found: " + sClassName);
		return true;
	}


	public static boolean configClassAdd(String sClassName, String sPermName, String sDispName, String sColor, ItemStack[] isItems, double dCost, int iExp, Player pPlayer) {
		Clazz cClass = getClassbyClassName(sClassName);
		if (cClass == null) 
		{
			Clazz c = new Clazz(sClassName);
			c.add(sPermName, sDispName, (FormatManager.formatColor(sColor)), isItems, dCost, iExp);
			// add Clazz
			clazzes.add(c);
			if (pPlayer == null)
			{
			    ClassRanks.log("Clazz added:" + sClassName, Level.INFO); 
				
				//db.i("Clazz added: " + sClassName); 
			} else
			{
				plugin.msg(pPlayer, "Clazz added: " + sClassName);
			}
//			plugin.config.save_config();
			return true;
		}
		if (pPlayer == null)
			plugin.db.i("Clazz already exists: " + sClassName);
		else
			plugin.msg(pPlayer, "Clazz already exists for " + pPlayer);
		return true;
	}

	public static boolean configRankAdd(String sClassName, String sPermName, String sDispName, String sColor, ItemStack[] isItems, double dCost, int iExp, Player pPlayer) {
		Clazz cClass = getClassbyClassName(sClassName);
		if (cClass != null) 
		{
			cClass.ranks.add(new Rank(sPermName, sDispName, (FormatManager.formatColor(sColor)), cClass, isItems, dCost, iExp));
			if (pPlayer == null)
			{
//				plugin.db.i("Rank added: " + (FormatManager.formatColor(sColor)) + sPermName);
//				plugin.config.save_config();
			} else
			{
				plugin.msg(pPlayer, "Rank added: " + (FormatManager.formatColor(sColor)) + sPermName +"/"+pPlayer);
			    plugin.config.save_config();
			}
			return true;
		}
		if (pPlayer == null)
			plugin.db.i("Clazz not found: " + sClassName);
		else
			plugin.msg(pPlayer, "Clazz not found: " + sClassName);
		return true;
	}

	public static boolean configClassChange(String sClassName, String sClassNewName, Player pPlayer) {
		Clazz cClass = getClassbyClassName(sClassName);
		if (cClass != null) {
			cClass.name = sClassNewName;
			plugin.msg(pPlayer, "Clazz changed: " + sClassName + " => " + sClassNewName);
			plugin.config.save_config();
			return true;
		}
		plugin.msg(pPlayer, "Clazz not found: " + sClassName);
		return true;
	}

	public static boolean configRankChange(String sClassName, String sPermName, String sDispName, String sColor, Player pPlayer) {
		Clazz cClass = getClassbyClassName(sClassName);
		if (cClass != null) {
			Rank rank = getRankByPermName(sPermName);
			if (rank != null) {
				rank.setDispName(sDispName);
				rank.setColor((FormatManager.formatColor(sColor)));
				plugin.msg(pPlayer, "Rank updated: " + (FormatManager.formatColor(sColor)) + sPermName);
				plugin.config.save_config();
				return true;
			}
			plugin.msg(pPlayer, "Rank not found: " + sPermName);
		}
		plugin.msg(pPlayer, "Clazz not found: " + sClassName);
		return true;
	}
	
	public static void saveClassProgress(Player pPlayer) 
	{
		plugin.db.i("saving Clazz process");
		String s = plugin.getConfig().getString("progress."+pPlayer.getName());
		plugin.db.i("progress of "+pPlayer.getName()+": "+s);
		Rank rank = ClassManager.getRankByPermName(plugin.perms.getPermNameByPlayer(pPlayer.getWorld().getName(), pPlayer));
		if (rank == null) {
			plugin.db.i("rank is null!");
			return;
		}

		int rankID = rank.getSuperClass().ranks.indexOf(rank);
		plugin.db.i("rank ID: "+rankID);
		int classID = clazzes.indexOf(rank.getSuperClass());
		plugin.db.i("classID: "+classID);
		
		if (s != null && s.length() == clazzes.size()) {
			
			char[] c = s.toCharArray();
			c[classID] = String.valueOf(rankID).charAt(0);
			plugin.db.i("new c[classID]: "+c[classID]);

			plugin.db.i("saving: "+c.toString());
			plugin.getConfig().set("progress."+pPlayer.getName(), String.valueOf(c.toString()));
			
			return;
		}

		plugin.db.i("no entry yet!");
		String result = "";
		for (int i = 0; i<clazzes.size();i++) {
			if (i == classID) {
				result += String.valueOf(rankID);
			} else {
				result += "0";
			}
		}
		plugin.db.i("setting: "+result);
		plugin.getConfig().set("progress."+pPlayer.getName(), String.valueOf(result));
		plugin.saveConfig();
	}
	
	public static int loadClassProcess(Player pPlayer, Clazz cClass) {
		try {
			String s = plugin.getConfig().getString("progress."+pPlayer.getName());
			int classID = clazzes.indexOf(cClass);
			
			int rankID = Integer.parseInt(String.valueOf(s.charAt(classID)));
			return rankID;
		} catch (Exception e) {
			return 0;
		}
	}

	public static int getRankIndex(Rank  rank, Clazz oClass) 
	{
		// init with 0 to set default to first Rank in Clazz
		int rankID = 0;
		try 
		{
			ArrayList<Rank> ranks = oClass.ranks;
			for (Rank oRank : ranks) 
			{
			    if (rank.getPermName().equals(oRank.getPermName()))
			    {
			    	return rankID;
			    } else
			    {
			    	rankID++;
			    }
			}
			return rankID;
		} catch (Exception e) {
			return 0;
		}
	}
	
	
}
