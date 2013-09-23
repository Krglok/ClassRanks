package net.slipcor.classranks.managers;

import java.util.logging.Level;

import net.slipcor.classranks.ClassRanks;

/*
 * Debug manager class
 * send messages to the server console, when active = true
 * active are set with config.debug_flag,  default value active = true !
 * for every message type is a separate method
 * i = level.INFO	, infoMessage
 * w = level.WARNING, warning MEssage
 * s = Level.SEVERE , errorMesaage
 * 
 * author: slipcor
 * 
 * version: v0.2.0.0 - 
 * 
 * history:
 *
 *     v0.2.0.0 - 
 */

public class DebugManager {
	public static boolean active;
	
	public DebugManager(boolean isActive) 
	{
		active = isActive;
	}
	
	/*
	 * info log
	 */
	public  void i(String s) {
		if (!active)
			return;
		ClassRanks.log(s, Level.INFO);
	}
	
	/*
	 * warning log
	 */
	public  void w(String s) {
		if (!active)
			return;
		ClassRanks.log(s, Level.WARNING);
	}
	
	/*
	 * severe log
	 */
	public  void s(String s) {
		if (!active)
			return;
		ClassRanks.log(s, Level.SEVERE);
	}
}
