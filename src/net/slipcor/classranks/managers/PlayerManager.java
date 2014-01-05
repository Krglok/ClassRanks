package net.slipcor.classranks.managers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
 * @author slipcor
 */

public class PlayerManager {
	private final ClassRanks plugin;
//	private static DebugManager db;
//	public static int coolDown;
	
	public PlayerManager(ClassRanks plugin) {
		this.plugin = plugin;
//		db = new DebugManager(plugin);
	}

	public static Player searchPlayer(String playerName) {
		return Bukkit.getPlayer(playerName);
//		Player[] p = Bukkit.getServer().getOnlinePlayers();
//		for (int i=0;i<p.length;i++)
//			if (p[i].getName().toLowerCase().contains(playerName.toLowerCase()))
//				return p[i]; // gotcha!
//
////		plugin.db.i("player not online: " + playerName);
//		// not found online, hope that it was right anyways
//		return null;
	}
	
	/*
	 * receive a string and search for online usernames containing it
	 */	
	public static String search(String player) {
		Player[] p = Bukkit.getServer().getOnlinePlayers();
		for (int i=0;i<p.length;i++)
			if (p[i].getName().toLowerCase().contains(player.toLowerCase()))
				return p[i].getName(); // gotcha!

//		db.i("player not online: " + player);
		// not found online, hope that it was right anyways
		return player;
	}

	
	public boolean hasItems(Player player, ItemStack[] items) 
	{
//		boolean isContain = true;
        for (int i=0;i<items.length;i++) 
        {
            if (items[i] != null) 
            {
            	plugin.db.i("checkItem : "+items[i].getData().toString() + " : " + items[i].getAmount());
//            	isContain = player.getInventory().contains(items[i]);
            	
            	if (player.getInventory().contains(items[i]) == false)	//player.getInventory().getItem(items[i].getTypeId()).getAmount() < items[i].getAmount())
            	{
            		//getData().getItemType().name();
            		plugin.msg(player,"Not found : "+items[i].getData().getItemType().name() + " : " + items[i].getAmount());
                	plugin.db.i("Not found : "+items[i].getData().getItemType().name() + " : " + items[i].getAmount());
            		return false;
            	}
            }
        }
        return true;  
	}
	
	boolean takeItems(Player player, ItemStack[] items) 
	{

        for (int i=0;i<items.length;i++) {
            if (items[i] != null) 
            {
            	player.getInventory().removeItem(items[i]);
            }
        }
        return true;  
	}

	int coolDownCheck(Player comP) {
		if ((comP.isOp()) || (plugin.config.getCoolDown() == 0)) 
		{
			return 0; // if we do not want/need to calculate the cooldown, get out of here!
		}

//		db.i("calculating cooldown");
		
		File fConfig = new File(plugin.getDataFolder(),"cooldowns.yml");
		YamlConfiguration configCool = new YamlConfiguration();
        
        if(fConfig.isFile()){
        	try {
        		configCool.load(fConfig);
			} catch (FileNotFoundException e) {
				ClassRanks.log("File not found!", Level.SEVERE);
				e.printStackTrace();
			} catch (IOException e) {
				ClassRanks.log("IO Exception!", Level.SEVERE);
				e.printStackTrace();
			} catch (InvalidConfigurationException e) {
				ClassRanks.log("Invalid Configuration!", Level.SEVERE);
				e.printStackTrace();
			}
        	ClassRanks.log("CoolDown file loaded!", Level.INFO);
        } else {
        	Map<String, Object> cdx = new HashMap<String, Object>();
        	cdx.put("slipcor", 0);
        	configCool.addDefault("cooldown", cdx);
        }
        
        Map<String, Object> cds = (Map<String, Object>) configCool.getConfigurationSection("cooldown").getValues(true);
        int now = Math.round((System.currentTimeMillis() % (60*60*24*1000)) /1000);

        if (cds.containsKey(comP.getName())) {
//    		db.i("player cooldown found!");
        	// Subtract the seconds waited from the needed seconds
        	int cd = plugin.config.getCoolDown() - (now - (Integer) cds.get(comP.getName()));
        	if ((cd <= plugin.config.getCoolDown()) && (cd > 0)) {
//        		db.i("cooldown still is: "+cd);
        		return cd; // we still have to wait, return how many seconds
        	}
        	cds.remove(comP.getName()); // delete the value
//    		db.i("value deleted");
        }

    	cds.put(comP.getName(), now);
//		db.i("value set");
    	
		configCool.set("cooldown", cds);
		try {
			configCool.save(fConfig);
		} catch (IOException e) {
			ClassRanks.log("IO Exception!", Level.SEVERE);
			e.printStackTrace();
		}
        
		return 0;
	}
	

}
