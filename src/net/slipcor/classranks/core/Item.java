package net.slipcor.classranks.core;


import org.bukkit.Material;

/**
 * <pre>
 * Independent type for blocks and items
 * make it possible to separate the model from the minecraft server
 * property:
 *  String sRef;
 *  Integer iValue;
 * @author krglok
 *
 * </pre>
 */


public class Item 
{
   private Material sRef;
   private Integer iValue;
   
   public Item()
   {
	   sRef = Material.AIR;
	   iValue = 0;
   }
   
   public Item(Material itemRef, int value)
   {
	   this.sRef = itemRef;
	   this.iValue = value;
	   
   }
   
   public  Material ItemRef()
   {
	   return sRef;
   }
   
   public Integer value()
   {
//	   System.out.println(iValue);
	   return iValue;
   }
   
   public void setItemRef(Material itemRef)
   {
	   sRef = itemRef;
   }
   
   public void setValue(int value)
   {
	   iValue = value;
   }
   
   public void addValue(int value)
   {
	   iValue = iValue + value;
   }
   
}

