package net.slipcor.classranks;

import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
//import org.bukkit.Material;
import org.bukkit.command.CommandSender;
//import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
//import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.Vault;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.slipcor.classranks.commands.ClassAdminCommand;
import net.slipcor.classranks.commands.AbstractClassCommand;
import net.slipcor.classranks.commands.PlayerCommands;
import net.slipcor.classranks.commands.RankdownCommand;
import net.slipcor.classranks.commands.RankupCommand;
import net.slipcor.classranks.core.ClazzList;
import net.slipcor.classranks.handlers.*;
import net.slipcor.classranks.listeners.*;
import net.slipcor.classranks.managers.ConfigManager;
import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.managers.FormatManager;
import net.slipcor.classranks.managers.PlayerManager;
import net.slipcor.classranks.register.payment.Method;

/**
 * main plugin class
 * 
 * @version v0.6.1 
 * 
 * @author slipcor/krglok
 */

public class ClassRanks extends JavaPlugin 
{
	// dont chance the order of these moduls !
	public ConfigManager config = new ConfigManager(this);
	public final PlayerManager pm = new PlayerManager(this);
	public final DebugManager db = new DebugManager(this);
	public final PlayerCommands cmdMgr = new PlayerCommands(this);
	private final CRServerListener serverListener = new CRServerListener(this,cmdMgr);
//	private final FormatManager fm = new FormatManager();
	private ClazzList clazzList = new ClazzList(this); 
	
	public boolean trackRanks = false;

	public Method method = null; // eConomy access
    public static Economy economy = null;
    public static Permission permission = null;
	public HandleVaultPerms perms; // Permissions access
	
//	private Map<String, Object> classes;

	@Override
	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(serverListener, this);

		@SuppressWarnings("unused")
		ClazzList cm = new ClazzList(this);

		// register commands
//		getCommand("classranks").setExecutor(new PlayerCommands(this));
		getCommand("class").setExecutor(new PlayerCommands(this));
		getCommand("classadmin").setExecutor(new ClassAdminCommand(this));
		getCommand("rankup").setExecutor(new RankupCommand(this));
		getCommand("rankdown").setExecutor(new RankdownCommand(this));

		// load the config file
		config.load_config(); 

		// load Vault
        Plugin vaultPlugin = pm.getPlugin("Vault");
        if (vaultPlugin != null) {
            log("[ClassRanks] found Vault Economy !",Level.INFO);
            RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
        } else {
            log("[ClassRanks] didnt find Vault.",Level.WARNING);
            log("[ClassRanks] please install the plugin Vault .",Level.INFO);
            log("[ClassRanks] will NOT be Enabled",Level.SEVERE);
            this.setEnabled(false);
            return;
        }
		if ( vaultPlugin != null) 
		{
			db.i("Vault found!");
//			if (getConfig().getBoolean("vaultpermissions")) 
				this.perms = new HandleVaultPerms(this);
			    log("ClassRank  Vault permissions plugin found", Level.INFO); // success!
				RegisteredServiceProvider<Permission> permissionProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			    permission = permissionProvider.getProvider();
			    if (this.perms.setupPermissions() == false)
			    {
				    log("ClassRank  Vault permissions NOT initialized", Level.SEVERE); // success!
			    }
//			if (getConfig().getBoolean("vaulteconomy")) 
//			{
//				setupEconomy();
//			}
		}
		
		// backup permissions

		if(config.isUpdateCheck())
		{
		    log(" UpdateCheck", Level.INFO); // success!
			Update.updateCheck(this);
		}
		log("Version " + this.getDescription().getVersion() + " init ready", Level.INFO);
	}

	@Override
	public void onDisable() {
		log("disabled", Level.INFO);
	}

	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) 
	{
    	if (!(sender instanceof Player)) 
    	{
    		msg(sender, "Console access is not implemented. ");
    		msg(sender, "Because a player instance is necessary");
    		return true;
    	}
		// admin class command, parse it!
		db.i("/classrank detected! parsing...");
		msg(sender, ChatColor.GREEN+"ClassRanks Ver: " + getDescription().getVersion());
		msg(sender, ChatColor.GOLD+"----------------------------------------");
		msg(sender, ChatColor.GOLD+"/class ,player command for choose the class");
		msg(sender, ChatColor.GOLD+"/rankup ,player rankup his class");
		msg(sender, ChatColor.GOLD+"/rankdown ,player rankdown his class");
		msg(sender, ChatColor.GOLD+"/classadmin , admin or op command for configuration");
		msg(sender, ChatColor.GREEN+"for help type command without parameter");
		return true;
	}
	
	/**
	 * (re)load the config
	 */

	/**
	 * log a prefixed message to the logfile
	 * 
	 * @param message
	 *            the message to log
	 * @param level
	 *            the logging level
	 */
	public static void log(String message, Level level) {
		Bukkit.getLogger().log(level, "[ClassRanks] " + message);
	}

//	public FormatManager getFormatManager() 
//	{
//		return fm;
//	}

	public DebugManager getDebugManager()
	{
		return db;
	}

	public PlayerCommands getCommandMgr()
	{
		return cmdMgr;
	}

	/**
	 * @return the pm
	 */
	public PlayerManager getPlayerManager()
	{
		return pm;
	}

	public  ClazzList clazzList()
	{
		return clazzList;
	}
	

	/**
	 * send a message to a player
	 * 
	 * @param pPlayer
	 *            the player to message
	 * @param string
	 *            the string to send
	 */
	public void msg(Player pPlayer, String string) 
	{
//		pPlayer.sendMessage("[" + ChatColor.AQUA + getConfig().getString("prefix")
//				+ ChatColor.WHITE + "] " + string);
		pPlayer.sendMessage(string);
		db.i("to " + pPlayer.getName() + ": " + string);
	}
	
	public void error(Player pPlayer, String string) 
	{
		pPlayer.sendMessage(ChatColor.RED+ string);
		db.i("to " + pPlayer.getName() + ": " + string);
	}

	/**
	 * send a message to a commandsender
	 * 
	 * @param sender  the commandsender to message
	 * @param string  the string to send
	 */
	public void msg(CommandSender sender, String string) 
	{
		sender.sendMessage("[" + ChatColor.AQUA + getConfig().getString("prefix")
				+ ChatColor.WHITE + "] " + string);
	}

	public void error(CommandSender sender, String string) 
	{
		sender.sendMessage("[" + ChatColor.AQUA + getConfig().getString("prefix")
				+ ChatColor.WHITE + "] " +ChatColor.RED+ string);
	}
    
    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }


}
