/*
 * Copyright (c) 2015. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Created by jasper on 9/15/15.
 */
public class Util {
	public static boolean inventoryIsEmpty(Inventory i){
		for(ItemStack it : i.getContents())
		{
			if(it != null) return false;
		}
		return true;
	}

	public static int getAmountOfMaterial(Inventory i, Material material){
		int amount = 0;
		for(ItemStack itemStack : i.all(material).values()){
			amount+= itemStack.getAmount();
		}
		return amount;
	}
}
