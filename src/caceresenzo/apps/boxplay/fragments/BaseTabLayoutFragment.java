package caceresenzo.apps.boxplay.fragments;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import caceresenzo.apps.boxplay.R;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;

/**
 * Base class for TabLayout-based fragment
 * 
 * @author Enzo CACERES
 */
public abstract class BaseTabLayoutFragment extends BaseBoxPlayFragment {
	
	/* Shortcuts Constants */
	/** @see {@link TabLayout#MODE_FIXED} */
	public static final int MODE_FIXED = TabLayout.MODE_FIXED;
	/** @see {@link TabLayout#MODE_SCROLLABLE} */
	public static final int MODE_SCROLLABLE = TabLayout.MODE_SCROLLABLE;
	
	/* Constants */
	public static final int OFFSCREEN_PAGE_LIMIT = 10;
	
	/* Bundle Keys */
	public static final String BUNDLE_KEY_LAST_OPENED_TAB_ID = "last_opened_tab_id";
	
	/* Instance */
	public static BaseTabLayoutFragment INSTANCE;
	
	/* Views */
	protected TabLayout tabLayout;
	protected ViewPager viewPager;
	protected BaseViewPagerAdapter adapter;
	
	/* Listeners */
	protected OnPageChangeListener onPageChangeListener;
	
	/* Variables */
	protected int onOpenPageId = 0, lastOpenPosition = 0;
	
	/* Constructor */
	public BaseTabLayoutFragment() {
		super();
		
		INSTANCE = this;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.base_fragment_tablayout, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		if (savedInstanceState == null) {
			viewPager = (ViewPager) getView().findViewById(R.id.base_fragment_tablayout_viewpager_container);
			tabLayout = (TabLayout) getView().findViewById(R.id.base_fragment_tablayout_tablayout_container);
			
			initializeViewPager();
		}
		
		if (viewPager != null) {
			viewPager.setCurrentItem(onOpenPageId, true);
		}
	}
	
	@Override
	public void saveInstanceState(Bundle outState) {
		super.saveInstanceState(outState);
		
		outState.putInt(BUNDLE_KEY_LAST_OPENED_TAB_ID, getLastOpenPosition());
		
		/* Event propagation */
		for (Fragment fragment : adapter.getFragments()) {
			if (fragment instanceof BaseBoxPlayFragment) {
				((BaseBoxPlayFragment) fragment).saveInstanceState(outState);
			}
		}
	}
	
	@Override
	public void restoreInstanceState(Bundle savedInstanceState) {
		super.restoreInstanceState(savedInstanceState);
		
		int lastOpenTab = savedInstanceState.getInt(BUNDLE_KEY_LAST_OPENED_TAB_ID, NO_VALUE);
		if (lastOpenTab != NO_VALUE) {
			withPage(lastOpenTab);
		}
		
		/* Event propagation */
		for (Fragment fragment : adapter.getFragments()) {
			if (fragment instanceof BaseBoxPlayFragment) {
				((BaseBoxPlayFragment) fragment).restoreInstanceState(savedInstanceState);
			}
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		destroy();
	}
	
	/**
	 * Private, called if the {@link ViewPager} is null when {@link #onActivityCreated(Bundle)} is called.
	 */
	private void initializeViewPager() {
		adapter = new BaseViewPagerAdapter(getChildFragmentManager());
		
		initialize();
		
		viewPager.setAdapter(adapter);
		viewPager.setOffscreenPageLimit(OFFSCREEN_PAGE_LIMIT);
		
		viewPager.addOnPageChangeListener(onPageChangeListener = new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				if (position != lastOpenPosition) {
					lastOpenPosition = position;
					
					updateDrawerSelection();
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
				;
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				;
			}
		});
		
		tabLayout.setupWithViewPager(viewPager);
	}
	
	/**
	 * Need to be overrided!<br>
	 * Then use the {@link #addFragment(Fragment, int)} or {@link #addFragment(Fragment, String)} method to add pages.
	 */
	protected abstract void initialize();
	
	/**
	 * Override if you you need to have a destroyer.
	 */
	protected void destroy() {
		;
	}
	
	/**
	 * Delegate function of {@link BaseViewPagerAdapter#addFragment(Fragment, String)}.
	 * 
	 * @param fragment
	 *            A fragment instance.
	 * @param title
	 *            Corresponding title.
	 */
	protected void addFragment(Fragment fragment, String title) {
		adapter.addFragment(fragment, title);
	}
	
	/**
	 * Same as {@link #addFragment(Fragment, String)}.
	 * 
	 * @param fragment
	 *            A fragment instance.
	 * @param titleRessourceId
	 *            Corresponding title string ressource id.
	 * @see #addFragment(Fragment, String)
	 */
	protected void addFragment(Fragment fragment, @StringRes int titleRessourceId) {
		addFragment(fragment, getString(titleRessourceId));
	}
	
	/**
	 * Change TabLayout behavior.<br>
	 * Possible values:
	 * <ul>
	 * <li>{@link #MODE_FIXED}</li>
	 * <li>{@link #MODE_SCROLLABLE}</li>
	 * </ul>
	 * 
	 * @param mode
	 *            New bahavior.
	 */
	protected void setTabMode(int mode) {
		tabLayout.setTabMode(mode);
	}
	
	/**
	 * Quickly change page.
	 * 
	 * @param pageId
	 *            Open a page by its index.
	 * @return Itself.
	 */
	public BaseTabLayoutFragment withPage(int pageId) {
		if (viewPager == null) {
			this.onOpenPageId = pageId;
		} else {
			viewPager.setCurrentItem(pageId);
		}
		
		updateDrawerSelection(getMenuItemIdByPageId(pageId));
		
		return this;
	}
	
	/**
	 * Update drawer selection by {@link #lastOpenPosition}.
	 */
	private void updateDrawerSelection() {
		updateDrawerSelection(getMenuItemIdByPageId(lastOpenPosition));
	}
	
	/**
	 * Update drawer selection by {@link MenuItem}'s id.
	 * 
	 * @param menuItemId
	 *            Target menu id.
	 */
	private void updateDrawerSelection(int menuItemId) {
		if (BoxPlayActivity.getBoxPlayActivity() != null) {
			updateDrawerSelection(BoxPlayActivity.getBoxPlayActivity().getNavigationView().getMenu().findItem(menuItemId));
		}
	}
	
	/**
	 * Update drawer selection by {@link MenuItem} instance directly
	 * 
	 * @param menuItem
	 */
	private void updateDrawerSelection(MenuItem menuItem) {
		menuHelper.unselectAllMenu();
		menuItem.setChecked(true);
	}
	
	/**
	 * Get actual page's {@link Fragment}.
	 * 
	 * @return The actual displayed {@link Fragment}.
	 */
	public Fragment getActualFragment() {
		return adapter.getItem(lastOpenPosition);
	}
	
	/**
	 * Get the last open {@link Fragment} id.
	 * 
	 * @return Last open position.
	 */
	public int getLastOpenPosition() {
		return lastOpenPosition;
	}
	
	/**
	 * Get {@link MenuItem}'s id by page position.<br>
	 * Used to select item in the drawer.
	 * 
	 * @param pageId
	 *            Actual oppened pageId/(supposed) index.
	 * @return The menu item id corresponding to the page id.
	 */
	protected abstract int getMenuItemIdByPageId(int pageId);
	
}