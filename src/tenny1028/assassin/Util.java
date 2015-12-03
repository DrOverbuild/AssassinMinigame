/*
 * Copyright (c) 2015. Jasper Reddin.
 * All Rights Reserved.
 */

package tenny1028.assassin;

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
}
