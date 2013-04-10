package net.slipcor.classranks.managers;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
//import org.bukkit.plugin.Plugin;
import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.core.Rank;

/*
 * this Class read the config and make it persistaned
 * each config node  will be stored here.
 * 
 */
public class ConfigManager {
	public ClassRanks plugin;

	public static String debugVersion = "0400";

	private Map<String, Object> classes;
	public Map<String, Object> getClasses() {
		return classes;
	}
//	private void setClasses(Map<String, Object> classes) {
//		this.classRanks = classes;
//	}

	private Map<String, Object> players;
	
	public Map<String, Object> getPlayers() {
		return players;
	}

	private Map<String,Object> playerClassList;
	
	private Map<String, String> playerClassRank;
	
	private Double[] moneyCost;
	public Double[] getMoneyCost() {
		return moneyCost;
	}
	public void setMoneyCost(Double[] moneyCost) {
		this.moneyCost = moneyCost;
	}
	private int[] expCost; //= new int[ClassManager.getClasses().size()]; // EXP
	public int[] getExpCost() {
		return expCost;
	}

	
	public ConfigManager(ClassRanks plugin) {
		super();
		this.plugin = plugin;
	}

	private Boolean debug;
	public Boolean isDebug() {
		return debug;
	}
	public void setDebug(Boolean debug) {
		this.debug = debug;
	}
	
	private Boolean checkprices;
	public Boolean isCheckprices() {
		return checkprices;
	}
	public void setCheckprices(Boolean checkprices) {
		this.checkprices = checkprices;
	}
	
	private Map<String, Object> prices;
	public Map<String, Object> getPrices() {
		return prices;
	}
	public void setPrices(Map<String, Object> prices) {
		this.prices = prices;
	}

	private Boolean checkexp;
	public Boolean isCheckexp() {
		return checkexp;
	}
	public void setCheckexp(Boolean checkexp) {
		this.checkexp = checkexp;
	}
	
	private Map<String, Object> expprices;
	public Map<String, Object> getExpprices() {
		return expprices;
	}
	public void setExpprices(Map<String, Object> expprices) {
		this.expprices = expprices;
	}
	
	private Boolean trackRanks;
	public Boolean isTrackRanks() {
		return trackRanks;
	}
	public void setTrackRanks(Boolean trackRanks) {
		this.trackRanks = trackRanks;
	}

	private Boolean rankPublic;
	public Boolean isRankpublic() {
		return rankPublic;
	}
	public void setRankpublic(Boolean rankpublic) {
		this.rankPublic = rankpublic;
	}
	
	private Boolean signcheck;
	public Boolean isSigncheck() {
		return signcheck;
	}
	public void setSigncheck(Boolean signcheck) {
		this.signcheck = signcheck;
	}
	
	private String[] signs;
	public String[] getSigns() {
		return signs;
	}
	public void setSigns(String[] signs) {
		this.signs = signs;
	}

	private int coolDown;
	public int getCoolDown() {
		return coolDown;
	}
	public void setCoolDown(int coolDown) {
		this.coolDown = coolDown;
	}
	
	private ItemStack[][] itemStacks;
	public ItemStack[][] getItemStacks() {
		return itemStacks;
	}
	public void setItemStacks(ItemStack[][] itemStacks) {
		this.itemStacks = itemStacks;
	}
	
	private Boolean checkitems;
	public Boolean isCheckitems() {
		return checkitems;
	}
	public void setCheckitems(Boolean checkitems) {
		this.checkitems = checkitems;
	}
	
	private Map<String, Object> items;
	public Map<String, Object> getItems() {
		return items;
	}
	public void setItems(Map<String, Object> items) {
		this.items = items;
	}
	
	private ItemStack[][] rankItems;
	public ItemStack[][] getRankItems() {
		return rankItems;
	}
	public void setRankItems(ItemStack[][] rankItems) {
		this.rankItems = rankItems;
	}

	private boolean defaultRankAllWorlds; // do rank themselves in all worlds?
	public Boolean isDefaultrankallworlds() {
		return defaultRankAllWorlds;
	}
	public void setDefaultrankallworlds(boolean defaultrankallworlds) {
		this.defaultRankAllWorlds = defaultrankallworlds;
	}
	
	private boolean onlyOneClass;
	public Boolean isOnlyoneclass() {
		return onlyOneClass;
	}
	public void setOnlyoneclass(boolean onlyoneclass) {
		this.onlyOneClass = onlyoneclass;
	}
	
	
	
	public void load_config() {

		if (plugin.getConfig() == null
				|| !plugin.getConfig().getString("cversion").equals(debugVersion)) 
		{
			plugin.log("creating default config.yml", Level.INFO);
			plugin.getConfig().set("cversion", debugVersion);
			plugin.getConfig().options().copyDefaults(true);
			plugin.saveConfig();
		}
//		DebugManager.active = plugin.getConfig().getBoolean("debug", false);
		debug = plugin.getConfig().getBoolean("debug", false);
		
		// check for section is available
		checkprices = plugin.getConfig().getBoolean("checkprices", false);
		boolean isSection = false;
		if (plugin.getConfig().getConfigurationSection("prices") != null)
		{
			isSection = true;
		}
		
		if ( isCheckprices()  && isSection) {
			plugin.db.i("prices are already set, reading...");
			// set prices
			prices = (Map<String, Object>) plugin.getConfig().getConfigurationSection("prices").getValues(true);

			if (prices.size() > 0)
			{
			  moneyCost = new Double[prices.size()];
			} else
			{
				moneyCost = new Double[1];
			}
			
			int i = 0;
			for (String Key : prices.keySet()) {
				String sVal = (String) prices.get(Key);
				try {
					moneyCost[i] = Double.parseDouble(sVal);
					plugin.db.i("#" + i + " => "
							+ String.valueOf(Double.parseDouble(sVal)));
				} catch (Exception e) {
					moneyCost[i] = 0.0;
					plugin.log("Unrecognized cost key '" + String.valueOf(Key) + "': "
							+ sVal, Level.INFO);
				}
				i++;
			}
		}

		// check for section is available
		
		checkexp = plugin.getConfig().getBoolean("checkexp", false);
		isSection = (plugin.getConfig().getConfigurationSection("exp") != null);
		if ((isCheckexp()) && (isSection)) {
			// set exp prices
			plugin.db.i("exp costs are set, reading...");
			
			expprices = (Map<String, Object>) plugin.getConfig().getConfigurationSection("exp").getValues(true);
//			cmdMgr.expCost = new int[expprices.size()];
			
			int i = 0;
			for (String Key : expprices.keySet()) {
				String sVal = (String) expprices.get(Key);
				try {
					plugin.cmdMgr.expCost[i] = Integer.parseInt(sVal);
					plugin.db.i("#" + i + " => "
							+ String.valueOf(Integer.parseInt(sVal)));
				} catch (Exception e) {
					
					expCost = new int[i+1];
					expCost[i] = 0;
					plugin.log("Unrecognized exp cost key '" + String.valueOf(Key)
							+ "': " + sVal, Level.INFO);
				}
				i++;
			}
		}

		// set subcolors
		plugin.cmdMgr.getFormatManager().setColors("world",
				plugin.getConfig().getString("playercolor"),
				plugin.getConfig().getString("worldcolor"));

		// set other variables
		rankPublic = plugin.getConfig().getBoolean("rankpublic", false);
		
		defaultRankAllWorlds = plugin.getConfig().getBoolean("defaultrankallworlds", false);
		
		onlyOneClass = plugin.getConfig().getBoolean("onlyoneclass", true);
		
		trackRanks = plugin.getConfig().getBoolean("trackRanks", false);

		// Sign 
		signcheck = plugin.getConfig().getBoolean("signcheck", false);

		if (signcheck) {
			plugin.db.i("sign check activated!");
			signs[0] = plugin.getConfig().getString("signchoose",
					"[choose]");
			signs[1] = plugin.getConfig().getString("signrankup",
					"[rankup]");
			signs[2] = plugin.getConfig().getString("signrankdown",
					"[rankdown]");
		}

		///PlayerManager.coolDown = plugin.getConfig().getInt("cooldown", 0);
		coolDown = plugin.getConfig().getInt("cooldown", 0);
		
		itemStacks = null;
		checkitems = plugin.getConfig().getBoolean("checkitems");
		
		// check section is available
		isSection = plugin.getConfig().getConfigurationSection("items") != null;
		// item read and check
		if (isCheckitems() && isSection) {
			plugin.db.i("items exist, parsing...");

			items = (Map<String, Object>) plugin.getConfig()
					.getConfigurationSection("items").getValues(false);
			// items error check
			if (items == null) {
				plugin.db.i("items invalid, setting to null");
				itemStacks = new ItemStack[ClassManager.getClasses().size()][1];
			} else {
				// for each items => ItemStack[][1,2,3]
				int iI = 0;
				itemStacks = new ItemStack[items.size()][];
				for (String isKey : items.keySet()) {
					List<?> values = plugin.getConfig().getList("items." + isKey);
					itemStacks[iI] = new ItemStack[values.size()];
					plugin.db.i("creating itemstack:");
					for (int iJ = 0; iJ < values.size(); iJ++) {
						String[] vValue = (String.valueOf(values.get(iJ)))
								.split(":");

						int vAmount = vValue.length > 1 ? Integer
								.parseInt(vValue[1]) : 1;
						try {
							itemStacks[iI][iJ] = new ItemStack(
									Material.valueOf(vValue[0]), vAmount);

						} catch (Exception e) {
							try {
								itemStacks[iI][iJ] = new ItemStack(
										Integer.valueOf(vValue[0]), vAmount);
							} catch (Exception e2) {

								plugin.log("Unrecognized reagent: " + vValue[0],
										Level.WARNING);
								continue;
							}
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
		classes = plugin.getConfig().getConfigurationSection("classes").getValues(false);
		for (String sClassName : classes.keySet()) {
			Map<String, Object> ranks = ((ConfigurationSection) classes
					.get(sClassName)).getValues(false);
			boolean newClass = true;
			for (String sRankName : ranks.keySet()) {

				String rankName = null;
				String rankColor = "&f";
				double rankCost = -1337D;
				ItemStack[] rankItems = null;
				int rankExp = -1;

				if (plugin.getConfig().get(
						"classes." + sClassName + "." + sRankName + ".name") != null) {
					rankName = plugin.getConfig()
							.getString(
									"classes." + sClassName + "." + sRankName
											+ ".name");
				}
				if (plugin.getConfig().get(
						"classes." + sClassName + "." + sRankName + ".color") != null) {
					rankColor = plugin.getConfig().getString(
							"classes." + sClassName + "." + sRankName
									+ ".color");
				}
				if (plugin.getConfig().get(
						"classes." + sClassName + "." + sRankName + ".price") != null) {
					rankCost = Double.valueOf(plugin.getConfig().getString(
							"classes." + sClassName + "." + sRankName
									+ ".price"));
				}
				if (plugin.getConfig().get(
						"classes." + sClassName + "." + sRankName + ".items") != null) {
					rankItems = FormatManager
							.getItemStacksFromStringList(plugin.getConfig()
									.getStringList(
											"classes." + sClassName + "."
													+ sRankName + ".items"));
				}
				if (plugin.getConfig().get(
						"classes." + sClassName + "." + sRankName + ".exp") != null) {
					rankExp = Integer.parseInt(plugin.getConfig().getString(
							"classes." + sClassName + "." + sRankName + ".exp"));
				}

				if (newClass) {
					// create class
					ClassManager.configClassAdd(sClassName, sRankName,
							rankName, rankColor, rankItems, rankCost, rankExp,
							null);

					newClass = false;
				} else {
					ClassManager.configRankAdd(sClassName, sRankName, rankName,
							rankColor, rankItems, rankCost, rankExp, null);
				}
			}
		}

//		getConfig().getString("progress."+pPlayer.getName());
		
		plugin.saveConfig();
	}
	
	/**
	 * 
	 */
	public void readPlayerSection() {
		boolean isSection = (plugin.getConfig().getConfigurationSection("players") != null);
		if (!isSection) {
			players = null;
		} else {
			players = plugin.getConfig().getConfigurationSection("players").getValues(false);
			
			for (String sClassName : players.keySet()) {
				
				
			}
		}
		
	}

	/**
	 * save the classrank map to the config
	 */
	public void save_config() {
		plugin.db.i("saving config...");
		for (net.slipcor.classranks.core.Class cClass : ClassManager
				.getClasses()) {
			plugin.db.i(" - " + cClass.name);
			for (Rank rRank : cClass.ranks) {

				rRank.debugPrint();

				plugin.getConfig().set(
						"classes." + cClass.name + "." + rRank.getPermName()
								+ ".name", String.valueOf(rRank.getDispName()));
				plugin.getConfig().set(
						"classes." + cClass.name + "." + rRank.getPermName()
								+ ".color",
						String.valueOf("&"
								+ Integer.toHexString(rRank.getColor()
										.ordinal())));
				plugin.getConfig().set(
						"classes." + cClass.name + "." + rRank.getPermName()
								+ ".price", String.valueOf(rRank.getCost()));

				if (rRank.getExp() > -1)
					plugin.getConfig().set(
							"classes." + cClass.name + "."
									+ rRank.getPermName() + ".exp",
							String.valueOf(rRank.getExp()));
				if (rRank.getItems() != null)
					plugin.getConfig().set(
							"classes." + cClass.name + "."
									+ rRank.getPermName() + ".items",
							rRank.getItems());
				if (rRank.getCost() != -1337D)
					plugin.getConfig().set(
							"classes." + cClass.name + "."
									+ rRank.getPermName() + ".price",
							String.valueOf(rRank.getCost()));
			}
		}
		plugin.saveConfig();
	}
	

}
