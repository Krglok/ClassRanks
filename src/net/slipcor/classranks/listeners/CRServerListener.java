package net.slipcor.classranks.listeners;

import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;
import net.slipcor.classranks.Update;
import net.slipcor.classranks.managers.CommandManager;
//import net.slipcor.classranks.managers.DebugManager;
import net.slipcor.classranks.register.payment.Methods;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

/**
 * server listener class
 * 
 * -
 * 
 * @version v0.4.6
 * 
 * @author slipcor/krglog
 */

public class CRServerListener implements Listener {
	private final Methods methods;
	private final ClassRanks plugin;

	private final CommandManager cmdMgr;
//	private final DebugManager db;
	
    public CRServerListener(ClassRanks plugin, CommandManager classes) {
    	this.plugin = plugin;
    	this.methods = new Methods();
		cmdMgr = classes;
//		db = new DebugManager(plugin);
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();

		if (!player.isOp()) {
			return; // no OP => OUT
		}
		Update.message(player);
	}
	
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
    	if (event.isCancelled() || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
    		return; // event cancelled or no block clicked => OUT
    	}

    	plugin.db.i("right block click!");
		// we clicked a block
		if ((plugin.config.isSigncheck() == false) || (plugin.config.isSigncheck() == null)) 
		{
			return; // no sign usage => OUT
		}
		plugin.db.i("we want to check for sign usage!");
		// we want to allow sign usage
		Block bBlock = event.getClickedBlock();
		if (!(bBlock.getState() instanceof Sign)) {
			return; // no sign clicked => OUT
		}
		plugin.db.i("we clicked a sign!");
		// we clicked a sign!
		Sign s = (Sign) bBlock.getState();

		if (s.getLine(0).equals(plugin.config.getSigns()[0])) {
			plugin.db.i("parsing command " + s.getLine(1));
			String[] sArgs = {s.getLine(1)}; // parse the command!
			cmdMgr.parseCommand(event.getPlayer(), sArgs);
		} else {
			plugin.db.i("searching for rank commands");
			for (int i = 0 ; i <= 3; i++) {
				if (s.getLine(i).equals(plugin.config.getSigns()[1])) {
					plugin.db.i("rankup found, parsing...");
					String[] sArgs = {"rankup"};
					cmdMgr.parseCommand(event.getPlayer(), sArgs);
					return;
				} else if (s.getLine(i).equals(plugin.config.getSigns()[2])) {
					plugin.db.i("rankup found, parsing");
					String[] sArgs = {"rankdown"};
					cmdMgr.parseCommand(event.getPlayer(), sArgs);
					return;
				}
			}
			plugin.db.i("no command found!");
		}
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
    	if (ClassRanks.economy != null) {
    		return;
    	}
        // Check to see if the plugin thats being disabled is the one we are using
        if (this.methods != null && Methods.hasMethod()) {
            Boolean check = Methods.checkDisabled(event.getPlugin());

            if(check) {
                plugin.method = null;
                ClassRanks.log("</3 eConomy",Level.INFO);
            }
        }
    }

	@EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
    	if (ClassRanks.economy != null) {
    		return;
    	}
        // Check to see if we need a payment method
        if (!Methods.hasMethod()) {
            if(Methods.setMethod(Bukkit.getServer().getPluginManager())) {
                plugin.method = Methods.getMethod();
                ClassRanks.log("<3 " + plugin.method.getName() + " version: " + plugin.method.getVersion(),Level.INFO);
            }
        }
        // Autoupdate are not used
    }
}