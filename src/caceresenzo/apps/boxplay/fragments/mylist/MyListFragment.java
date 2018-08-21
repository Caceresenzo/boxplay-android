package caceresenzo.apps.boxplay.fragments.mylist;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;

public class MyListFragment extends BaseTabLayoutFragment {
	
	public static final int PAGE_WATCHLATER = 0;
	
	@Override
	protected void initialize() {
		addFragment(new PageWatchLaterListFragment(), R.string.boxplay_mylist_watchlater_title);
	}
	
	@Override
	protected int getMenuItemIdByPageId(int pageId) {
		switch (pageId) {
			default:
			case PAGE_WATCHLATER: {
				return R.id.drawer_boxplay_mylist_watchlater;
			}
		}
	}
	
	public MyListFragment withWatchLater() {
		return (MyListFragment) withPage(PAGE_WATCHLATER);
	}
	
}