package net.slipcor.classranks.core;


public class Itemlist 
{
	private String itemName;
	private int amount;
	
	public Itemlist (String name, int amount)
	{
		this.setItemName(name);
		this.setAmount(amount);
	}

	public int getAmount() {
		return amount;
	}

	public void setAmount(int amount) {
		this.amount = amount;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}
}
