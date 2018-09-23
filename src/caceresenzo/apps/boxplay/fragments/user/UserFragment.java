package caceresenzo.apps.boxplay.fragments.user;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;
import caceresenzo.apps.boxplay.fragments.utils.PlaceholderFragment;

public class UserFragment extends BaseTabLayoutFragment {
	
	/* Constants */
	public static final int PAGE_PROFILE = 0;
	
	/* Instance */
	public static UserFragment INSTANCE;
	
	/* Constructor */
	public UserFragment() {
		super();
		
		INSTANCE = this;
	}
	
	@Override
	protected void initialize() {
		adapter.addFragment(new PlaceholderFragment(), R.string.boxplay_user_profile_profile);
	}
	
	@Override
	protected int getMenuItemIdByPageId(int pageId) {
		switch (pageId) {
			default:
			case PAGE_PROFILE: {
				return R.id.drawer_boxplay_user_profile;
			}
		}
	}
	
	public UserFragment withProfile() {
		return (UserFragment) withPage(PAGE_PROFILE);
	}
	
	public static UserFragment getUserFragment() {
		return INSTANCE;
	}
	
}