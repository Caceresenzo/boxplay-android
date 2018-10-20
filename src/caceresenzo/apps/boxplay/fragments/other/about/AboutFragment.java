package caceresenzo.apps.boxplay.fragments.other.about;

import android.support.design.widget.TabLayout;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.fragments.BaseTabLayoutFragment;

/**
 * About main fragment
 * 
 * @author Enzo CACERES
 */
public class AboutFragment extends BaseTabLayoutFragment {
	
	/* Constants */
	public static final int PAGE_ABOUT = 0;
	public static final int PAGE_HOSTING = 1;
	public static final int PAGE_CHANGELOG = 2;
	public static final int PAGE_LIBRARIES = 3;
	
	@Override
	protected void initialize() {
		addFragment(new PageAboutAboutFragment(), R.string.boxplay_other_about_about);
		addFragment(new PageAboutHostingFragment(), R.string.boxplay_other_about_hosting);
		addFragment(new PageAboutChangeLogFragment(), R.string.boxplay_other_about_changelog);
		addFragment(new PageAboutLibrariesFragment(), R.string.boxplay_other_about_libraries);
		
		setTabMode(TabLayout.MODE_SCROLLABLE);
	}
	
	@Override
	protected int getMenuItemIdByPageId(int pageId) {
		return R.id.drawer_boxplay_other_about;
	}
	
	/**
	 * Change the page to the {@link PageAboutAboutFragment} page
	 * 
	 * @return Itself
	 */
	public AboutFragment withAbout() {
		return (AboutFragment) withPage(PAGE_ABOUT);
	}
	
	/**
	 * Change the page to the {@link PageAboutHostingFragment} page
	 * 
	 * @return Itself
	 */
	public AboutFragment withHosting() {
		return (AboutFragment) withPage(PAGE_HOSTING);
	}
	
	/**
	 * Change the page to the {@link PageAboutChangeLogFragment} page
	 * 
	 * @return Itself
	 */
	public AboutFragment withChangeLog() {
		return (AboutFragment) withPage(PAGE_CHANGELOG);
	}
	
	/**
	 * Change the page to the {@link PageAboutLibrariesFragment} page
	 * 
	 * @return Itself
	 */
	public AboutFragment withLibraries() {
		return (AboutFragment) withPage(PAGE_LIBRARIES);
	}
	
	/**
	 * @return Saved instance
	 */
	public static AboutFragment getAboutFragment() {
		return (AboutFragment) INSTANCE;
	}
	
}