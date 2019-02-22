package caceresenzo.apps.boxplay.helper.implementations;

import java.util.HashMap;
import java.util.Map;

import android.view.Menu;
import android.view.MenuItem;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.AbstractHelper;
import caceresenzo.apps.boxplay.helper.HelperManager;

public class MenuHelper extends AbstractHelper {
	
	/* Cache */
	private Map<MenuIdItem, MenuItem> menuIds;
	
	/* Constructor */
	public MenuHelper(BoxPlayApplication boxPlayApplication) {
		super(boxPlayApplication);
		
		this.menuIds = new HashMap<>();
	}
	
	@Override
	public void initialize(HelperManager helperManager) {
		super.initialize(helperManager);
		
		fillCache();
	}
	
	/** Clear and refill the cache. */
	public void recacheIds() {
		destroyCache();
		fillCache();
	}
	
	/** Fill the cache with default values. */
	private void fillCache() {
		/* Menu cache */
		menu(R.id.drawer_boxplay_user_profile, true);
		menu(R.id.drawer_boxplay_store_video, true);
		menu(R.id.drawer_boxplay_connect_feed);
		menu(R.id.drawer_boxplay_connect_friends);
		menu(R.id.drawer_boxplay_connect_chat);
		menu(R.id.drawer_boxplay_culture_searchngo);
		menu(R.id.drawer_boxplay_premium_adult);
		menu(R.id.drawer_boxplay_mylist_watchlater);
		menu(R.id.drawer_boxplay_mylist_subscriptions);
		menu(R.id.drawer_boxplay_other_settings);
		menu(R.id.drawer_boxplay_other_about);
	}
	
	/** Clear the cache. */
	private void destroyCache() {
		menuIds.clear();
	}
	
	/**
	 * Register a menu by its id.<br>
	 * Search icon will be disabled with this function.
	 * 
	 * @param menuItemId
	 *            Target menu id.
	 */
	private void menu(int menuItemId) {
		menu(menuItemId, false);
	}
	
	/**
	 * Register a menu by its id and tell if it should have the search icon enabled.
	 * 
	 * @param menuItemId
	 *            Target menu id.
	 * @param allowSearch
	 *            Weather or not the search icon will be enabled.
	 */
	private void menu(int menuItemId, boolean allowSearch) {
		menuIds.put(new MenuIdItem(menuItemId, allowSearch), null);
	}
	
	/**
	 * Get a {@link MenuIdItem} by its original {@link MenuItem} (supposed) id.
	 * 
	 * @param id
	 *            Target id.
	 * @return The corresponding {@link MenuIdItem}, or <code>null</code> if not found.
	 */
	public MenuIdItem getMenuIdItemById(int id) {
		for (MenuIdItem menuIdItem : menuIds.keySet()) {
			if (menuIdItem.getId() == id) {
				return menuIdItem;
			}
		}
		
		return null;
	}
	
	/** Unselect every item in the drawer from the register menu ids. */
	public void unselectAllMenu() {
		Menu menu = BoxPlayActivity.getBoxPlayActivity().getNavigationView().getMenu();
		
		for (MenuIdItem menuIdItem : menuIds.keySet()) {
			MenuItem item = menuIds.get(menuIdItem);
			
			if (item == null) {
				menuIds.put(menuIdItem, item = menu.findItem(menuIdItem.getId()));
			}
			
			if (item != null && item.isChecked()) {
				item.setChecked(false);
			}
		}
	}
	
	/**
	 * Update the search icon on the top right corner.<br>
	 * With the nextId, a corresponding {@link MenuIdItem} will provider information about enabling the icon or not.
	 * 
	 * @param nextId
	 *            Next menu id that is supposed to be display.
	 */
	public void updateSeachMenu(int nextId) {
		for (MenuIdItem menuIdItem : menuIds.keySet()) {
			if (menuIdItem.getId() == nextId) {
				try {
					BoxPlayActivity.getBoxPlayActivity().getOptionsMenu().findItem(R.id.menu_main_action_search).setVisible(menuIdItem.isSearchAllowed());
				} catch (Exception exception) {
					;
				}
				
				break;
			}
		}
	}
	
	/**
	 * Simple data holding class.
	 * 
	 * @author Enzo CACERES
	 */
	public static class MenuIdItem {
		
		/* Variables */
		private int id;
		private boolean allowSearch;
		
		/* Constructor */
		public MenuIdItem(int id, boolean allowSearch) {
			this.id = id;
			this.allowSearch = allowSearch;
		}
		
		/** @return Menu item's id. */
		public int getId() {
			return id;
		}
		
		/** @return Weather or not this item should have the search icon enabled on the right top cornerF. */
		public boolean isSearchAllowed() {
			return allowSearch;
		}
		
	}
	
}