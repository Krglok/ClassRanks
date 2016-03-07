package net.slipcor.classranks.core;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * list of items, hold each item type unique
 * if item exists the value are added 
 *  
 * @author oduda
 *
 */
public class ItemList  extends  HashMap<Material, Item>  
{
/**
	 * 
	 */
	private static final long serialVersionUID = -3351533128792676311L;
	//	private Map<String, Integer> itemList;
	protected int itemCount;

	public ItemList()
	{
//		itemList = new HashMap<String, Integer>();
		itemCount = 0;
	}

//	public Map<String, Integer> getItemList() {
//		return itemList;
//	}
//
//	public void setItemList(Map<String, Integer> itemList) {
//		this.itemList = itemList;
//	}
	
	/**
	 * add item to the list
	 * if exist values are  add to exiting items
	 * @param item
	 */
	public boolean addItem(Item item)
	{
		if (this.containsKey(item.ItemRef()))
		{
			Item exist = this.get(item.ItemRef());
			exist.setValue(exist.value()+item.value());
		} else
		{
			this.put(item.ItemRef(), item);
		}
		itemCount = itemCount + item.value();
//		Item item = new Item(itemRef, iValue);
		return true;
	}
	
	/**
	 * add item to the list
	 * if exist values are  overwrite
	 * @param itemRef
	 * @param iValue
	 * @return added item or null
	 */
	public boolean addItem(Material itemRef, int iValue)
	{
		if (this.containsKey(itemRef))
		{
			Item exist = this.get(itemRef);
			exist.setValue(exist.value()+iValue);
		} else
		{
			this.put(itemRef, new Item(itemRef, iValue)); 
		}
		itemCount = itemCount + iValue;
//		Item item = new Item(itemRef, iValue);
		return true;
	}
	
	/**
	 * add value to the item in the list
	 * if not exist add item
	 * @param itemRef
	 * @param iValue
	 * @return
	 */
	public Item putItem(Material itemRef, int iValue)
	{
		Item item;
		if (this.containsKey(itemRef))
		{
			item = this.get(itemRef);
			item.setValue(item.value()+iValue);
			itemCount = itemCount + iValue;
			return item; 
		}
		item = new Item(itemRef,iValue);
		this.addItem(item);
		return item;
	}
	
	/**
	 * 
	 * @param itemRef
	 * @return
	 */
	public Item getItem(Material itemRef)
	{
		return this.get(itemRef);
	}
		
//	public Boolean isEmpty()
//	{
//		return itemList.isEmpty();
//	}

	/**
	 * add value to item value
	 * if item not exist in list it will be created
	 * @param itemRef
	 * @param value
	 */
	public void depositItem(Material itemRef, int iValue)
	{
		Item item;
		if (containsKey(itemRef))
		{
			item = this.get(itemRef);
//			System.out.println("deposit: "+iValue);
			item.setValue(item.value()+iValue);
//			put(itemRef, get(itemRef)+value);
		} else
		{
			this.addItem(itemRef, iValue);
		}
		itemCount = itemCount + iValue;
	}

	/**
	 * bucht value von item ab , 
	 * wenn bestand > 0
	 * @param itemRef
	 * @param value
	 * @return
	 */
	public Boolean withdrawItem(Material itemRef, int value)
	{
		int actual = 0;
		if(this.containsKey(itemRef))
		{
			actual = this.get(itemRef).value();
		}
		if ((actual-value) >= 0)
		{
			value = value * -1;
//			System.out.println("withdraw: "+value);
			this.depositItem(itemRef, (value));
			return true;
		}
		
		return false;
	}
	
	public int getValue(Material itemRef)
	{
		Item item = get(itemRef);
		if (item != null)
		{
			return item.value();
		} else
		{
			return 0;
		}
	}
	
	public void setValue(Material itemRef, int value)
	{
		putItem(itemRef, value);
	}
	
	

	public int getItemCount()
	{
		return itemCount;
	}

	/**
	 * calculate item count new form item values
	 */
	public void setItemCount()
	{
		itemCount = 0;
		for (Item item : this.values())
		{
			itemCount = itemCount + item.value();
		}
	}
	
	public ArrayList<Material> sortItems()
	{
		ArrayList<Material> sortedItems = new ArrayList<Material>();
		for (Material s : this.keySet())
		{
			sortedItems.add(s);
		}
		if (sortedItems.size() > 1)
		{
			Collections.sort
			(sortedItems);
		}
		return sortedItems;
	}

	public void addAll(ItemList newList)
	{
		for (Item item : newList.values())
		{
			put(item.ItemRef(), item);
		}
	}
	

	/**
	 * make a dowb count for required list by the amopunt
	 * 
	 */
	public void reduceRequired()
	{
		ArrayList<Material> deleteList = new ArrayList<Material>();
		for (Item item : this.values())
		{
			if (item.value() > 0)
			{
				item.setValue(item.value()-1);
			} else
			{
				deleteList.add(item.ItemRef());
			}
		}
		for (Material key : deleteList )
		{
			this.remove(key);
		}
	}
	
	public String asString()
	{
		int count = 0;
		String value ="";
		for (Material key : this.keySet())
		{
			if (count == 0)
			{
				value = key.name();
			} else
			{
				value = value + ", "+key.name() ;
			}
		}
		return value;
	}
	
	public void addList( ItemStack[] items)
	{
		if (items == null)
		{
			return;
		}
        for (int i=0;i<items.length;i++) 
        {
            if (items[i] != null) 
            {
            	Item item = this.getItem(items[i].getType());
            	if (item == null)
            	{
            		this.put(items[i].getType(), new Item(items[i].getType(),items[i].getAmount()));
            	} else
            	{
            		item.addValue(items[i].getAmount());
            	}
            }
        }

	}
	
}
