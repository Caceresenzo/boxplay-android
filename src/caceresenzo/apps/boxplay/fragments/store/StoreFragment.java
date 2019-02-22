package caceresenzo.apps.boxplay.fragments.store;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;

public class StoreFragment extends BaseTabLayoutFragment {
	
	public static final int PAGE_VIDEO = 0;
	
	@Override
	protected void initialize() {
		addFragment(new PageVideoStoreFragment(), R.string.boxplay_store_video_video);
	}
	
	@Override
	protected int getMenuItemIdByPageId(int pageId) {
		switch (pageId) {
			default:
			case PAGE_VIDEO: {
				return R.id.drawer_boxplay_store_video;
			}
		}
	}
	
	public StoreFragment withVideo() {
		return (StoreFragment) withPage(PAGE_VIDEO);
	}
	
	public static StoreFragment getStoreFragment() {
		return (StoreFragment) INSTANCE;
	}
	
}