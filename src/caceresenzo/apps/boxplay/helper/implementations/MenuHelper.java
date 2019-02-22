package caceresenzo.apps.boxplay.helper.implementations;

import java.util.HashMap;
import java.util.Map;

import android.view.Menu;
import android.view.MenuItem;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.AbstractHelper;

public class MenuHelper extends AbstractHelper {
	
	/* Cache */
	private static Map<MenuIdItem, MenuItem> drawerMenuIds = new HashMap<>();
	
	public MenuHelper(BoxPlayApplication boxPlayApplication) {
		super(boxPlayApplication);
	}
	
	/*
	 * Classes
	 */
	class MenuIdItem {
		private int id;
		private boolean allowSearch;
		
		public MenuIdItem(int id) {
			this(id, false);
		}
		
		public MenuIdItem(int id, boolean allowSearch) {
			this.id = id;
			this.allowSearch = allowSearch;
		}
		
		public int getId() {
			return id;
		}
		
		public boolean isSearchAllowed() {
			return allowSearch;
		}
	}
	
	public MenuIdItem getMenuIdItemById(int id) {
		for (MenuIdItem menuIdItem : drawerMenuIds.keySet()) {
			if (menuIdItem.getId() == id) {
				return menuIdItem;
			}
		}
		return null;
	}
	
	public void refreshMenuIdCache() {
		drawerMenuIds.clear();
		
		/* Menu cache */
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_user_profile, true), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_store_video, true), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_connect_feed), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_connect_friends), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_connect_chat), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_culture_searchngo), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_premium_adult), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_mylist_watchlater), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_mylist_subscriptions), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_other_settings), null);
		drawerMenuIds.put(new MenuIdItem(R.id.drawer_boxplay_other_about), null);
	}
	
	/*
	 * Menu Help
	 */
	public void unselectAllMenu() {
		Menu menu = BoxPlayActivity.getBoxPlayActivity().getNavigationView().getMenu();
		for (MenuIdItem menuIdItem : drawerMenuIds.keySet()) {
			MenuItem item = drawerMenuIds.get(menuIdItem);
			if (item == null) {
				drawerMenuIds.put(menuIdItem, item = menu.findItem(menuIdItem.getId()));
			}
			
			if (item != null && item.isChecked()) {
				item.setChecked(false);
			}
		}
	}
	
	public void updateSeachMenu(int nextId) {
		for (MenuIdItem menuIdItem : drawerMenuIds.keySet()) {
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
	
}