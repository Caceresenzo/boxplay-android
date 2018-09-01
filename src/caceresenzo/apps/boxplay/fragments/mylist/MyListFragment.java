package caceresenzo.apps.boxplay.fragments.mylist;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;

public class MyListFragment extends BaseTabLayoutFragment {
	
	public static final int PAGE_WATCHLATER = 0;
	public static final int PAGE_SUBSCRIPTIONS = 1;
	
	@Override
	protected void initialize() {
		addFragment(new PageWatchLaterListFragment(), R.string.boxplay_mylist_watchlater_title);
		addFragment(new PageSubscriptionsListFragment(), R.string.boxplay_mylist_subscriptions_title);
	}
	
	@Override
	protected int getMenuItemIdByPageId(int pageId) {
		switch (pageId) {
			default:
			case PAGE_WATCHLATER: {
				return R.id.drawer_boxplay_mylist_watchlater;
			}
			
			case PAGE_SUBSCRIPTIONS: {
				return R.id.drawer_boxplay_mylist_subscriptions;
			}
		}
	}
	
	public MyListFragment withWatchLater() {
		return (MyListFragment) withPage(PAGE_WATCHLATER);
	}
	
	public MyListFragment withSubscriptions() {
		return (MyListFragment) withPage(PAGE_SUBSCRIPTIONS);
	}
	
}