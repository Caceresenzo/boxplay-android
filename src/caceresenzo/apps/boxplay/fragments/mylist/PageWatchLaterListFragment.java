package caceresenzo.apps.boxplay.fragments.mylist;

public class PageWatchLaterListFragment extends MyListPageFragment {
	
	@Override
	public void callFetch() {
		myListManager.getWatchLaterList().fetch(this);
	}
	
}