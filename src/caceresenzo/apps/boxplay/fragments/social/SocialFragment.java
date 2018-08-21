package caceresenzo.apps.boxplay.fragments.social;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;
import caceresenzo.apps.boxplay.fragments.utils.PlaceholderFragment;

public class SocialFragment extends BaseTabLayoutFragment {
	
	public static final int PAGE_FEED = 0;
	public static final int PAGE_FRIEND = 1;
	public static final int PAGE_CHAT = 2;
	
	@Override
	protected void initialize() {
		adapter.addFragment(new PlaceholderFragment(), "FEEDS");
		adapter.addFragment(new PlaceholderFragment(), "FRIENDS");
		adapter.addFragment(new PlaceholderFragment(), "CHATS");
	}
	
	@Override
	protected int getMenuItemIdByPageId(int pageId) {
		switch (pageId) {
			default:
			case PAGE_FEED: {
				return R.id.drawer_boxplay_connect_feed;
			}
			case PAGE_FRIEND: {
				return R.id.drawer_boxplay_connect_friends;
			}
			case PAGE_CHAT: {
				return R.id.drawer_boxplay_connect_chat;
			}
		}
	}
	
	public SocialFragment withFeed() {
		return (SocialFragment) withPage(PAGE_FEED);
	}
	
	public SocialFragment withFriend() {
		return (SocialFragment) withPage(PAGE_FRIEND);
	}
	
	public SocialFragment withChat() {
		return (SocialFragment) withPage(PAGE_CHAT);
	}
	
	public static SocialFragment getSocialFragment() {
		return (SocialFragment) INSTANCE;
	}
	
}