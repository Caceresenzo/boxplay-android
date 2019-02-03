package caceresenzo.apps.boxplay.fragments.mylist;

import caceresenzo.apps.boxplay.managers.MyListManager.MyList;

public class PageWatchLaterListFragment extends MyListPageFragment {
	
	@Override
	public MyList getMyListInstance() {
		return myListManager.getWatchLaterMyList();
	}
	
}