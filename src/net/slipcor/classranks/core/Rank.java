package net.slipcor.classranks.core;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

/**
 * rank class
 * - rank parameter 
 * 
 * @version v0.3.0 
 * 
 * @author slipcor
 */
public class Rank {
	String sPermissionName;
	String sDisplayName;
	ChatColor clChat;
	String sClassRef;

	Itemlist[] items;
	private Double cost;
	int exp;

	/**
	 * create a rank instance
	 * 
	 * @param sPermName
	 *            the permission name to use
	 * @param sDispName
	 *            the display name
	 * @param cC
	 *            the name ChatColor
	 * @param crc
	 *            the class to add to
	 * @param isItems
	 *            the rank items cost
	 * @param dCost
	 *            the rank cost
	 * @param iExp
	 *            the rank experience cost
	 */
	public Rank(String sPermName, String sDispName, ChatColor cC, Class crc,
			Itemlist[] Items, double dCost, int iExp) 
	{
		this.sPermissionName = sPermName;
		this.sDisplayName = sDispName;
		this.clChat = cC;
		this.sClassRef = crc.name;
		this.items = Items;
		this.cost = dCost;
		this.exp = iExp;
	}

	/**
	 * hand over display name
	 * 
	 * @return the display name
	 */
	public String getDispName() {
		return this.sDisplayName;
	}

	/**
	 * hand over the color
	 * 
	 * @return the color
	 */
	public ChatColor getColor() {
		return this.clChat;
	}

	/**
	 * hand over the class
	 * 
	 * @return the class
	 */
	public String getSuperClass() {
		return this.sClassRef;
	}

	/**
	 * hand over the permission name
	 * 
	 * @return the permission name
	 */
	public String getPermName() {
		return sPermissionName;
	}

	/**
	 * update the display name
	 * 
	 * @param sDisplayName
	 *            the display to use
	 */
	public void setDispName(String sDisplayName) {
		this.sDisplayName = sDisplayName;
	}

	/**
	 * update the color
	 * 
	 * @param cColor
	 *            the color to use
	 */
	public void setColor(ChatColor cColor) {
		this.clChat = cColor;
	}

	public Double getCost() {
		return cost;
	}
	
	public ArrayList<String> getItems() {
		if (items == null) {
			return null;
		}
		ArrayList<String> result = new ArrayList<String>();
		
		for (int i = 0; i < items.length; i++) 
		{
			result.add(items[i].getItemName() + ":" +items[i].getAmount());
		}
		return result;
	}
	
	public int getExp() {
		return exp;
	}

//	public ItemStack[] getItemstacks() {
//		return items;
//	}

	public void debugPrint() {
		//return;
		/*
		 * 
		System.out.print("--"+sPermissionName+"--");
		System.out.print(sDisplayName);
		System.out.print(cColor+"color");
		if (items == null) {
			System.out.print("null");
		} else
			System.out.print(FormatManager.formatItemStacks(items));
		System.out.print(cost);
		System.out.print(exp);
		
		 */
		
	}
}
