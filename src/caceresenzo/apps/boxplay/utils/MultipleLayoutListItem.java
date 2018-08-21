package caceresenzo.apps.boxplay.utils;

/**
 * Abstract class, used to extends Multiple Layout Recycler View Item
 * 
 * @author Enzo CACERES
 */
public abstract class MultipleLayoutListItem {
	
	/**
	 * Get actual type of the item view
	 * 
	 * @return Item unique type id
	 */
	public abstract int getType();
	
}