package net.slipcor.classranks;

import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
//import org.bukkit.Material;
import org.bukkit.command.CommandSender;
//import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
//import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;
import net.slipcor.classranks.commands.ClassAdminCommand;
import net.slipcor.classranks.commands.ClassCommand;
import net.slipcor.classranks.commands.RankdownCommand;
import net.slipcor.classranks.commands.RankupCommand;
import net.slipcor.classranks.handlers.*;
import net.slipcor.classranks.listeners.*;
import net.slipcor.classranks.managers.ClassManager;
import net.slipcor.classranks.managers.CommandManager;
import net.slipcor.classranks.managers.ConfigManager;
import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.register.payment.Method;

/**
 * main plugin class
 * 
 * @version v0.3.1
 * 
 * @author slipcor/krglok
 */

public class ClassRanks extends JavaPlugin {
	public final CommandManager cmdMgr = new CommandManager(this);
	private final CRServerListener serverListener = new CRServerListener(this,cmdMgr);
	public final DebugManager db = new DebugManager(this);
	public ConfigManager config = new ConfigManager(this);
	
	public boolean trackRanks = false;

	public Method method = null; // eConomy access
    public static Economy economy = null;
	public CRHandler perms; // Permissions access
	
	private Map<String, Object> classes;

	@Override
	public void onEnable() {

		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(serverListener, this);

		@SuppressWarnings("unused")
		ClassManager cm = new ClassManager(this);

		// register commands
		getCommand("class").setExecutor(new ClassCommand(this, cmdMgr));
		getCommand("classadmin").setExecutor(new ClassAdminCommand(this, cmdMgr));
		getCommand("rankup").setExecutor(new RankupCommand(this, cmdMgr));
		getCommand("rankdown").setExecutor(new RankdownCommand(this, cmdMgr));

		// load the config file
		config.load_config(); 

		// load Vault 
		if (pm.getPlugin("Vault") != null) {
			db.i("Vault found!");
			if (getConfig().getBoolean("vaultpermissions")) 
			{
				this.perms = new HandleVaultPerms(this);
			    log("ClassRank  Vault permissions plugin found", Level.INFO); // success!
			    
			    if (this.perms.setupPermissions() == false)
			    {
				    log("ClassRank  Vault permissions NOT initialized", Level.SEVERE); // success!
			    }
			}
			if (getConfig().getBoolean("vaulteconomy")) 
			{
				setupEconomy();
			}
		}
		
		// backup permissions
		if (this.perms == null || (this.perms != null && !this.perms.setupPermissions())) {
		
			if (pm.getPlugin("bPermissions") != null) {
				db.i("bPermissions found!");
				this.perms = new HandleBPerms(this);
			} else if (pm.getPlugin("PermissionsEx") != null) {
				db.i("PermissionsEX found!");
				this.perms = new HandlePEX(this);
			} else {
				db.i("No perms found, defaulting to SuperPermissions!");
				this.perms = new HandleSuperPerms(this);
			}
			this.perms.setupPermissions();
		}

		if(config.isUpdateCheck())
		{
		    log(" UpdateCheck", Level.INFO); // success!
			Tracker tracker = new Tracker(this);
			tracker.start();
			Update.updateCheck(this);
		}
		log("Version " + this.getDescription().getVersion() + " init ready", Level.INFO);
	}

	@Override
	public void onDisable() {
		Tracker.stop();
		log("disabled", Level.INFO);
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
	public void log(String message, Level level) {
		Bukkit.getLogger().log(level, "[ClassRanks] " + message);
	}

	/**
	 * send a message to a player
	 * 
	 * @param pPlayer
	 *            the player to message
	 * @param string
	 *            the string to send
	 */
	public void msg(Player pPlayer, String string) {
		pPlayer.sendMessage("[" + ChatColor.AQUA + getConfig().getString("prefix")
				+ ChatColor.WHITE + "] " + string);
		db.i("to " + pPlayer.getName() + ": " + string);
	}

	/**
	 * send a message to a commandsender
	 * 
	 * @param sender
	 *            the commandsender to message
	 * @param string
	 *            the string to send
	 */
	public void msg(CommandSender sender, String string) {
		sender.sendMessage("[" + ChatColor.AQUA + getConfig().getString("prefix")
				+ ChatColor.WHITE + "] " + string);
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
