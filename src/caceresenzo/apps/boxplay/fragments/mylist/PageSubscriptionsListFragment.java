package caceresenzo.apps.boxplay.fragments.mylist;

import caceresenzo.apps.boxplay.managers.MyListManager.MyList;

public class PageSubscriptionsListFragment extends MyListPageFragment {
	
	@Override
	public MyList getMyListInstance() {
		return myListManager.getSubscriptionsMyList();
	}
	
}