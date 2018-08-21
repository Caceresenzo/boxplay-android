package caceresenzo.apps.boxplay.fragments.culture;

import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;
import caceresenzo.apps.boxplay.fragments.culture.searchngo.PageCultureSearchAndGoFragment;

public class CultureFragment extends BaseTabLayoutFragment {
	
	public static final int PAGE_SEARCHANDGO = 0;
	
	@Override
	protected void initialize() {
		addFragment(new PageCultureSearchAndGoFragment(), R.string.boxplay_culture_searchngo_title);
	}
	
	@Override
	protected int getMenuItemIdByPageId(int pageId) {
		switch (pageId) {
			default:
			case PAGE_SEARCHANDGO: {
				return R.id.drawer_boxplay_culture_searchngo;
			}
		}
	}
	
	/**
	 * Open the Search n' Go page of the culture fragment
	 * 
	 * @return Itself
	 */
	public CultureFragment withSearchAndGo() {
		return (CultureFragment) withPage(PAGE_SEARCHANDGO);
	}
	
	/**
	 * Get the main instance of this fragment
	 * 
	 * @return Last instance
	 */
	public static CultureFragment getCultureFragment() {
		return (CultureFragment) INSTANCE;
	}
	
}