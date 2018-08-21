package caceresenzo.apps.boxplay.utils;

/**
 * Allow a class to be Bindable
 *
 * @param <T>
 *            Target bind type
 * 
 * @author Enzo CACERES
 */
public interface Bindable<T extends MultipleLayoutListItem> {
	
	/**
	 * Bind the actual class
	 * 
	 * @param item
	 *            Item to be bind with
	 */
	void bind(T item);
	
}