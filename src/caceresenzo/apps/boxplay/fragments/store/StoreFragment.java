package caceresenzo.apps.boxplay.fragments.store;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;

public class StoreFragment extends BaseTabLayoutFragment {
	
	public static final int PAGE_VIDEO = 0;
	public static final int PAGE_MUSIC = 1;
	
	@Override
	protected void initialize() {
		addFragment(new PageVideoStoreFragment(), R.string.boxplay_store_video_video);
		addFragment(new PageMusicStoreFragment(), R.string.boxplay_store_music_music);
	}
	
	@Override
	protected int getMenuItemIdByPageId(int pageId) {
		switch (pageId) {
			default:
			case PAGE_VIDEO: {
				return R.id.drawer_boxplay_store_video;
			}
			
			case PAGE_MUSIC: {
				return R.id.drawer_boxplay_store_music;
			}
		}
	}
	
	public StoreFragment withVideo() {
		return (StoreFragment) withPage(PAGE_VIDEO);
	}
	
	public StoreFragment withMusic() {
		return (StoreFragment) withPage(PAGE_MUSIC);
	}
	
	public static StoreFragment getStoreFragment() {
		return (StoreFragment) INSTANCE;
	}
	
}