package net.slipcor.classranks.managers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
//import org.bukkit.plugin.Plugin;
import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Clazz;
import net.slipcor.classranks.core.PlayerClazz;
import net.slipcor.classranks.core.Rank;

/*
 * this Class read the config and make it persistaned
 * each config node  will be stored here.
 * the config data stored in parameters, so not always read from file
 * the ClassRank tree are stored in the ClazzList
 * 
 * @version v0.4.6 
 * 
 * @author krglok
 * 
 */
public class ConfigManager
{
	public ClassRanks plugin;

	public static String debugVersion = "0400";

	// private void setClasses(Map<String, Object> classes) {
	// this.classRanks = classes;
	// }
	private Boolean autoUpdate = new Boolean(false);

	public Boolean isAutoUpdate()
	{
		return autoUpdate;
	}

	private Boolean updateCheck = new Boolean(false);

	public Boolean isUpdateCheck()
	{
		return updateCheck;
	}

	private boolean useUUID = new Boolean(false);

	public Boolean isUUD()
	{
		return useUUID;
	}

	public void setUUID(Boolean value)
	{
		useUUID = value;
	}

	private Boolean clearranks = new Boolean(false);

	public Boolean isClearranks()
	{
		return clearranks;
	}

	public void setClearranks(Boolean clearranks)
	{
		this.clearranks = clearranks;
	}

	private HashMap<String, PlayerClazz> playersClazzList;

	public HashMap<String, PlayerClazz> getPlayers()
	{
		return playersClazzList;
	}

	// private Map<String,Object> playerClassList; // should store the
	// playerClasses

	// private Map<String, String> playerClassRank; // should store the
	// playerRanks

	private Double[] moneyCost;

	public Double[] getMoneyCost()
	{
		return moneyCost;
	}

	public Double getMoneyCost(int index)
	{
		if (index < moneyCost.length)
		{
			return moneyCost[index];
		} else
		{
			return moneyCost[moneyCost.length - 1];
		}
	}

	public void setMoneyCost(Double[] moneyCost)
	{
		this.moneyCost = moneyCost;
	}

	private int[] expCost; // = new int[ClazzList.getClasses().size()]; // EXP

	public int[] getExpCost()
	{
		return expCost;
	}

	public int getExpCost(int index)
	{
		if (index < expCost.length)
		{
			return expCost[index];
		} else
		{
			return expCost[expCost.length - 1];
		}
	}

	public ConfigManager(ClassRanks plugin)
	{
		super();
		this.plugin = plugin;
		moneyCost = new Double[1];
		expCost = new int[1];
		signs = new String[3];
		playersClazzList = new HashMap<String, PlayerClazz>();
	}

	private Boolean debug = new Boolean(false);

	public Boolean isDebug()
	{
		return debug;
	}

	public void setDebug(Boolean debug)
	{
		this.debug = debug;
	}

	private Boolean checkprices = new Boolean(false);

	public Boolean isCheckprices()
	{
		return checkprices;
	}

	public void setCheckprices(Boolean checkprices)
	{
		this.checkprices = checkprices;
	}

	private Map<String, Object> prices;

	public Map<String, Object> getPrices()
	{
		return prices;
	}

	public void setPrices(Map<String, Object> prices)
	{
		this.prices = prices;
	}

	private Boolean checkexp = new Boolean(false);

	public Boolean isCheckexp()
	{
		return checkexp;
	}

	public void setCheckexp(Boolean checkexp)
	{
		this.checkexp = checkexp;
	}

	private Map<String, Object> expprices;

	public Map<String, Object> getExpprices()
	{
		return expprices;
	}

	public void setExpprices(Map<String, Object> expprices)
	{
		this.expprices = expprices;
	}

	private Boolean trackRanks = new Boolean(false);

	public Boolean isTrackRanks()
	{
		return trackRanks;
	}

	public void setTrackRanks(Boolean trackRanks)
	{
		this.trackRanks = trackRanks;
	}

	private Boolean rankPublic;

	public Boolean isRankpublic()
	{
		return rankPublic;
	}

	public void setRankpublic(Boolean rankpublic)
	{
		this.rankPublic = rankpublic;
	}

	private Boolean signcheck = new Boolean(false);

	public Boolean isSigncheck()
	{
		return signcheck;
	}

	public void setSigncheck(Boolean signcheck)
	{
		this.signcheck = signcheck;
	}

	private String[] signs; // = new String[];

	public String[] getSigns()
	{
		return signs;
	}

	public void setSigns(String[] signs)
	{
		this.signs = signs;
	}

	private int coolDown;

	public int getCoolDown()
	{
		return coolDown;
	}

	public void setCoolDown(int coolDown)
	{
		this.coolDown = coolDown;
	}

	private ItemStack[][] itemStacks;

	public ItemStack[][] getItemStacks()
	{
		return itemStacks;
	}

	public void setItemStacks(ItemStack[][] itemStacks)
	{
		this.itemStacks = itemStacks;
	}

	private Boolean checkitems = new Boolean(false);

	public Boolean isCheckitems()
	{
		return checkitems;
	}

	public void setCheckitems(Boolean checkitems)
	{
		this.checkitems = checkitems;
	}

	private Map<String, Object> items;

	public Map<String, Object> getItems()
	{
		return items;
	}

	public void setItems(Map<String, Object> items)
	{
		this.items = items;
	}

	private ItemStack[][] rankItems;

	public ItemStack[][] getRankItems()
	{
		return rankItems;
	}

	public void setRankItems(ItemStack[][] rankItems)
	{
		this.rankItems = rankItems;
	}

	private boolean defaultRankAllWorlds; // do rank themselves in all worlds?

	public Boolean isDefaultrankallworlds()
	{
		return defaultRankAllWorlds;
	}

	public void setDefaultrankallworlds(boolean defaultrankallworlds)
	{
		this.defaultRankAllWorlds = defaultrankallworlds;
	}

	private boolean permissionpluginhasworldsupport = true;
	
	public boolean permissionpluginhasworldsupport()
	{
		return permissionpluginhasworldsupport;
	}
	
	public void setPermissionpluginhasworldsupport(boolean value)
	{
		permissionpluginhasworldsupport = value;
	}
	
	private boolean onlyOneClass = new Boolean(false);

	public Boolean isOnlyoneclass()
	{
		return onlyOneClass;
	}

	public void setOnlyoneclass(boolean onlyoneclass)
	{
		this.onlyOneClass = onlyoneclass;
	}

	/**
	 * Load Config file , stores data in parmeters
	 */
	public void load_config()
	{

		if (plugin.getConfig() == null
				|| !plugin.getConfig().getString("cversion")
						.equals(debugVersion))
		{
			ClassRanks.log("creating default config.yml", Level.INFO);
			plugin.getConfig().set("cversion", debugVersion);
			plugin.getConfig().options().copyDefaults(true);
			plugin.saveConfig();
		}
		// DebugManager.active = plugin.getConfig().getBoolean("debug", false);
		debug = plugin.getConfig().getBoolean("debug", false);
		plugin.db.active = debug;
		// AutoUpdate Config
		autoUpdate = plugin.getConfig().getBoolean("autoupdate", false);
		// UpdateCheck Config
		updateCheck = plugin.getConfig().getBoolean("updatecheck", false);

		// check for section is available
		checkprices = plugin.getConfig().getBoolean("checkprices", false);

		// useUUID
		useUUID = plugin.getConfig().getBoolean("useUUID", false);
		// permissionpluginhasworldsupport
		permissionpluginhasworldsupport = plugin.getConfig().getBoolean("permissionpluginhasworldsupport",true);
		boolean isSection = false;
		if (plugin.getConfig().getConfigurationSection("prices") != null)
		{
			isSection = true;
		}

		// if ( isCheckprices() && isSection)
		if (isSection)
		{
			plugin.db.i("Config prices reading...");
			// set prices
			prices = (Map<String, Object>) plugin.getConfig()
					.getConfigurationSection("prices").getValues(true);

			if (prices.size() > 0)
			{
				moneyCost = new Double[prices.size()];
			} else
			{
				moneyCost = new Double[1];
			}

			int i = 0;
			for (String Key : prices.keySet())
			{
				String sVal = (String) prices.get(Key);
				try
				{
					moneyCost[i] = Double.parseDouble(sVal);
					plugin.db.i("#" + i + " => "
							+ String.valueOf(Double.parseDouble(sVal)));
				} catch (Exception e)
				{
					moneyCost[i] = 0.0;
					ClassRanks.log(
							"Unrecognized cost key '" + String.valueOf(Key)
									+ "': " + sVal, Level.INFO);
				}
				i++;
			}
		}

		// check for section is available

		checkexp = plugin.getConfig().getBoolean("checkexp", false);
		isSection = (plugin.getConfig().getConfigurationSection("exp") != null);
		// if ((isCheckexp()) && (isSection))

		if (isSection)
		{
			// set exp prices
			plugin.db.i("CONFG exp costs reading...");

			expprices = (Map<String, Object>) plugin.getConfig()
					.getConfigurationSection("exp").getValues(true);
			// cmdMgr.expCost = new int[expprices.size()];

			int i = 0;
			// array erweitern, wenn zu klein
			if (expCost.length < expprices.size())
			{
				expCost = new int[expprices.size()];
			}
			// config in array uebertragen
			for (String Key : expprices.keySet())
			{
				String sVal = (String) expprices.get(Key);
				ClassRanks.log("ExpPrice : " + sVal, Level.INFO);
				try
				{
					expCost[i] = Integer.parseInt(sVal);
					plugin.db.i("#" + i + " => "
							+ String.valueOf(Integer.parseInt(sVal)));
				} catch (Exception e)
				{

					expCost = new int[i + 1];
					expCost[i] = 0;
					ClassRanks.log(
							"Unrecognized exp cost key '" + String.valueOf(Key)
									+ "': " + sVal, Level.INFO);
				}
				i++;
			}
		}

		// set subcolors
		FormatManager.setColors(plugin.getConfig().getString("playercolor"),
				plugin.getConfig().getString("worldcolor"));

		// set other variables
		rankPublic = plugin.getConfig().getBoolean("rankpublic", false);

		defaultRankAllWorlds = plugin.getConfig().getBoolean(
				"defaultrankallworlds", false);

		onlyOneClass = plugin.getConfig().getBoolean("onlyoneclass", true);

		trackRanks = plugin.getConfig().getBoolean("trackRanks", false);

		// Sign
		signcheck = plugin.getConfig().getBoolean("signcheck", false);

		plugin.db.i("sign check Read!");
		signs[0] = plugin.getConfig().getString("signchoose", "[choose]");
		signs[1] = plugin.getConfig().getString("signrankup", "[rankup]");
		signs[2] = plugin.getConfig().getString("signrankdown", "[rankdown]");

		setClearranks(plugin.getConfig().getBoolean("clearranks", false));

		// /PlayerManager.coolDown = plugin.getConfig().getInt("cooldown", 0);
		coolDown = plugin.getConfig().getInt("cooldown", 0);

		itemStacks = null;
		checkitems = plugin.getConfig().getBoolean("checkitems");

		// check section is available
		plugin.db.i("read items section");
		isSection = plugin.getConfig().getConfigurationSection("items") != null;
		// item read and check
		if (isSection)
		{
			plugin.db.i("items exist, parsing...");

			items = (Map<String, Object>) plugin.getConfig()
					.getConfigurationSection("items").getValues(false);
			// items error check
			if (items == null)
			{
				plugin.db.i("items invalid, setting to null");
				itemStacks = new ItemStack[plugin.clazzList().getClazzes()
						.size()][1];
			} else
			{
				// for each items => ItemStack[][1,2,3]
				int iI = 0;
				itemStacks = new ItemStack[items.size()][];
				for (String isKey : items.keySet())
				{
					plugin.db.i("Item Key : " + isKey.toString());
					List<?> values = plugin.getConfig().getList(
							"items." + isKey);
					itemStacks[iI] = new ItemStack[values.size()];
					plugin.db.i("creating itemstack:");
					for (int iJ = 0; iJ < values.size(); iJ++)
					{
						String[] vValue = (String.valueOf(values.get(iJ)))
								.split(":");

						int vAmount = vValue.length > 1 ? Integer
								.parseInt(vValue[1]) : 1;
						try
						{
							itemStacks[iI][iJ] = new ItemStack(
									Material.valueOf(vValue[0]), vAmount);

						} catch (Exception e)
						{

							ClassRanks.log(
									"Unrecognized reagent: " + vValue[0],
									Level.WARNING);
							continue;
						}
					}
					plugin.db.i(iI + " - "
							+ FormatManager.formatItemStacks(itemStacks[iI]));
					iI++;
				}
			}
		}
		setRankItems(itemStacks);

		// read basis class and ranks initialize the object
		Map<String, Object> classes = plugin.getConfig().getConfigurationSection("classes").getValues(false);
		for (String sClassName : classes.keySet())
		{
			Map<String, Object> ranks = ((ConfigurationSection) classes.get(sClassName)).getValues(false);
			for (String sRankName : ranks.keySet())
			{
				// Rank definition
				// this.sPermissionName = sPermName;
				// this.sDisplayName = sDispName;
				// this.cColor = cC;
				// this.crcSuper = crc;
				// this.items = isItems;
				// this.cost = dCost;
				// this.exp = iExp;

				String rankName = null;
				String rankColor = "&f";
				double rankCost = -1337D;
				ItemStack[] rankItems = null;
				int rankExp = -1;
				String nodeName = "classes." + sClassName + "." + sRankName + ".name";
				if (plugin.getConfig().get(nodeName) != null)
				{
					rankName = plugin.getConfig().getString(nodeName);
				}
				nodeName ="classes." + sClassName + "." + sRankName + ".color"; 
				if (plugin.getConfig().get(nodeName) != null)
				{
					rankColor = plugin.getConfig().getString(nodeName);
				}
				nodeName = "classes." + sClassName + "." + sRankName + ".price";
				if (plugin.getConfig().get(nodeName) != null)
				{
					rankCost = Double.valueOf(plugin.getConfig().getString(nodeName));
				}
				nodeName = "classes." + sClassName + "." + sRankName + ".items";
				if (plugin.getConfig().get(nodeName) != null)
				{
					rankItems = FormatManager.getItemStacksFromStringList(plugin.getConfig().getStringList(nodeName));
				}
				nodeName = "classes." + sClassName + "." + sRankName + ".exp";
				if (plugin.getConfig().get(nodeName) != null)
				{
					rankExp = Integer.parseInt(plugin.getConfig().getString(nodeName));
				}

				plugin.clazzList().importClazz(sClassName, sRankName,rankName, rankColor, rankItems, rankCost, rankExp,null);
			}
		}

		// getConfig().getString("progress."+pPlayer.getName());
		playerSectionRead();

		plugin.saveConfig();
	}

	/**
	 * 
	 */
	public void playerSectionRead()
	{
		boolean isSection = (plugin.getConfig().isConfigurationSection("players"));

		if (isSection)
		{
			Map<String, Object> playerList = plugin.getConfig().getConfigurationSection("players").getValues(false);

			if (playerList != null)
			{
//				System.out.println("PlayerClassRank List");
				for (String playerName : playerList.keySet())
				{
					PlayerClazz playerClazz = new PlayerClazz(playerName, "");

					Map<String, Object> playerClassRank = plugin.getConfig().getConfigurationSection("players." + playerName).getValues(false);
					for (String clazzName : playerClassRank.keySet())
					{
						String rankName = plugin.getConfig().getString("players." + playerName + "." + clazzName, "");
						playerClazz.addPlayerClassRank(clazzName, rankName);

					}
					playersClazzList.put(playerName, playerClazz);
				}
				// Test output
				for (PlayerClazz playerClazz : playersClazzList.values())
				{
					for (String clazzName : playerClazz.playerClazzRanks().keySet())
					{
						String rankName = playerClazz.playerClazzRanks().get(clazzName);
//						System.out.println(playerClazz.getUuid() + ":" + clazzName+ ":" + rankName);
					}
				}
			}

		}

	}
	
	/**
	 * write config data to configsection
	 * save config to file
	 */
	public void saveConfig()
	{
		plugin.getConfig().set("debug", isDebug());
		plugin.getConfig().set("checkprices", isCheckprices());
		plugin.getConfig().set("checkexp", isCheckexp());
		plugin.getConfig().set("checkitems", isCheckitems());
		plugin.getConfig().set("clearranks", isClearranks());
		plugin.getConfig().set("onlyoneclass", isOnlyoneclass());
		plugin.getConfig().set("rankpublic", isRankpublic());
		plugin.getConfig().set("signcheck", isSigncheck());
		plugin.getConfig().set("cooldown", coolDown);
		plugin.saveConfig();
	}

//	private void playerSectionWrite()
//	{
//		plugin.getConfig().set("players", playersClazzList);
//		plugin.saveConfig();
//	}

	/**
	 * set PlayerSection to ConfigSection
	 *  
	 * @param player
	 * @param className
	 * @param rank
	 */
	public void playerSectionWrite(PlayerClazz playerClazz)
	{
		for (String clazzName : playerClazz.playerClazzRanks().keySet())
		{
			String rank = playerClazz.playerClazzRanks().get(clazzName);
			plugin.getConfig().set("players." + playerClazz.getUuid() + "." + clazzName,rank);
			plugin.db.i("Write to config rank " + rank + " to player "+ playerClazz.getUuid() + ", no world support");
		}
		plugin.saveConfig();
	}
	/**
	 * Remove ClazzRank from player
	 * set PlayerSection to ConfigSection
	 * save config to File
	 * 
	 * @param player
	 * @param className
	 */
	public void playerSectionRemove(Player player, String className)
	{
		if (className != null)
		{
			plugin.db.i("Remove config " + player.getUniqueId().toString()+ " : " + className);
			PlayerClazz playerClazz = playersClazzList.get(player.getUniqueId().toString());
			if (playerClazz != null)
			{
				if (playerClazz.playerClazzRanks().containsKey(className))
				{
					playerClazz.playerClazzRanks().remove(className);
					plugin.db.i("Save player section after remove ");
					playerSectionWrite(playerClazz);
					plugin.saveConfig();
				}
			}
		}
	}

	/**
	 * write ClazzRankList to Config
	 * save config to File
	 */
	public void writeClazzConfig()
	{
		plugin.db.i("saving config...");
		for (Clazz cClass : plugin.clazzList().getClazzes().values())
		{
			plugin.db.i(" - " + cClass.clazzName());
			for (Rank rRank : cClass.ranks().values())
			{

				plugin.getConfig().set(
						"classes." + cClass.clazzName() + "."
								+ rRank.getPermName() + ".name",
						String.valueOf(rRank.getDispName()));
				plugin.getConfig().set(
						"classes." + cClass.clazzName() + "."
								+ rRank.getPermName() + ".color",
						String.valueOf("&"
								+ Integer.toHexString(rRank.getColor()
										.ordinal())));
				plugin.getConfig().set(
						"classes." + cClass.clazzName() + "."
								+ rRank.getPermName() + ".price",
						String.valueOf(rRank.getCost()));

				if (rRank.getExp() > -1)
					plugin.getConfig().set(
							"classes." + cClass.clazzName() + "."
									+ rRank.getPermName() + ".exp",
							String.valueOf(rRank.getExp()));
				if (rRank.getItems() != null)
					plugin.getConfig().set(
							"classes." + cClass.clazzName() + "."
									+ rRank.getPermName() + ".items",
							rRank.getItems());
				if (rRank.getCost() != -1337D)
					plugin.getConfig().set(
							"classes." + cClass.clazzName() + "."
									+ rRank.getPermName() + ".price",
							String.valueOf(rRank.getCost()));
			}
		}
		plugin.saveConfig();
	}

}
