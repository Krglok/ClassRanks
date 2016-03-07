package net.slipcor.classranks.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/*
 * Player manager class
 * - handles the cooldown , cooldown stored in yml
 * - handles Inventory of player and Items
 * 
 * v0.2.0 - mayor rewrite; no SQL; multiPermissions
 * 
 * History:
 * 
 *     v0.1.6 - cleanup
 *     v0.1.5.1 - cleanup
 *     v0.1.5.0 - more fixes, update to CB #1337
 * 
 * @author slipcor / Krglok
 */

public class PlayerManager 
{
	private final ClassRanks plugin;
	
	public PlayerManager(ClassRanks plugin) 
	{
		this.plugin = plugin;
	}


	public static Player searchPlayerName(String name) 
	{
		for (Player player : Bukkit.getServer().getOnlinePlayers())
		{
			if (player.getName().equalsIgnoreCase(name))
			{
				return player; // gotcha!
			}
		}
		System.out.println("[ClassRank]  player not online "+name);
		//		plugin.db.i("player not online: " + playerName);
		// not found online, hope that it was right anyways
		return null;
	}
	
	/*
	 * receive a string and search for online usernames containing it
	 */	
	public static String searchName(String playerName) 
	{
		Collection<? extends Player> players = Bukkit.getServer().getOnlinePlayers();
		for (Player p : players)
		{
			if (p.getName().toLowerCase().contains(playerName.toLowerCase()))
			{
				return p.getName(); // gotcha!
			}
		}
//		db.i("player not online: " + player);
		// not found online, hope that it was right anyways
		return "";
	}

	
	public boolean hasItems(Player player, ItemStack[] items) 
	{
		boolean isContain = false;
		if (items.length == 0)
		{
			return false;
		}
        for (int i=0;i<items.length;i++) 
        {
            if (items[i] != null) 
            {
            	isContain = false;
            	plugin.db.i("checkItem : "+items[i].getData().toString() + " : " + items[i].getAmount());
            	for (ItemStack item : player.getInventory().getContents())
            	{
            		if ((item != null) && (item.getType() == items[i].getType()))
            		{
//	                	plugin.db.i("Inventory: "+item.getType().name() + " : " + item.getAmount());
		            	if (item.getAmount() >= items[i].getAmount())
		            	{
		            		isContain = true;
		                	plugin.db.i("Found : "+item.getData().getItemType().name() + " : " + item.getAmount());
		            		//getData().getItemType().name();
		            	}
            		}
            	}
                if (isContain == false)
                {
            		plugin.msg(player,"Not found : "+items[i].getData().getItemType().name() + " : " + items[i].getAmount());
                	plugin.db.i("Not found : "+items[i].getData().getItemType().name() + " : " + items[i].getAmount());
                	return false;
                }
            }
            	
        }
        return true;  
	}
	
	public boolean takeItems(Player player, ItemStack[] items) 
	{

        for (int i=0;i<items.length;i++) {
            if (items[i] != null) 
            {
            	player.getInventory().removeItem(items[i]);
            }
        }
        return true;  
	}

	/**
	 * Upgrade to 1.7.xx  get the UUID for player reference
	 * 
	 * @param player
	 * @return the time to cooldown
	 */
	public int coolDownCheck(Player player) 
	{
		if ((player.isOp()) || (plugin.config.getCoolDown() == 0)) 
		{
			return 0; // if we do not want/need to calculate the cooldown, get out of here!
		}

		File fConfig = new File(plugin.getDataFolder(),"cooldowns.yml");
		YamlConfiguration configCool = new YamlConfiguration();
        
        if(fConfig.isFile())
        {
        	try {
        		configCool.load(fConfig);
			} 
        	catch (FileNotFoundException e) 
			{
				ClassRanks.log("File not found!", Level.SEVERE);
				e.printStackTrace();
			} 
        	catch (IOException e) 
			{
				ClassRanks.log("IO Exception!", Level.SEVERE);
				e.printStackTrace();
			} 
        	catch (InvalidConfigurationException e) 
        	{
				ClassRanks.log("Invalid Configuration!", Level.SEVERE);
				e.printStackTrace();
			}
        	ClassRanks.log("CoolDown file loaded!", Level.INFO);
        } else 
        {
        	Map<String, Object> cdx = new HashMap<String, Object>();
        	cdx.put("slipcor", 0);
        	configCool.addDefault("cooldown", cdx);
        }
        
        Map<String, Object> cds = (Map<String, Object>) configCool.getConfigurationSection("cooldown").getValues(true);
        int now = Math.round((System.currentTimeMillis() % (60*60*24*1000)) /1000);

//        if (cds.containsKey(player.getName())) 
        if (cds.containsKey(player.getUniqueId().toString())) 
        {
        	int cd = plugin.config.getCoolDown() - (now - (Integer) cds.get(player.getName()));
        	if ((cd <= plugin.config.getCoolDown()) && (cd > 0)) 
        	{
        		return cd; // we still have to wait, return how many seconds
        	}
        	cds.remove(player.getUniqueId().toString()); // delete the value
        }

    	cds.put(player.getUniqueId().toString(), now);
    	
		configCool.set("cooldown", cds);
		try 
		{
			configCool.save(fConfig);
		} 
		catch (IOException e) 
		{
			ClassRanks.log("IO Exception!", Level.SEVERE);
			e.printStackTrace();
		}
        
		return 0;
	}
	

}
