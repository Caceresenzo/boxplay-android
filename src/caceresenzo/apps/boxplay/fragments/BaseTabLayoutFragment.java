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
import caceresenzo.apps.boxplay.application.BoxPlayApplication;

/**
 * Base class for TabLayout-based fragment
 * 
 * @author Enzo CACERES
 */
public abstract class BaseTabLayoutFragment extends Fragment {
	
	private static final int OFFSCREEN_PAGE_LIMIT = 10;
	
	public static BaseTabLayoutFragment INSTANCE;
	
	protected BoxPlayApplication boxPlayApplication;
	protected BoxPlayApplication boxPlayActivity;
	
	protected TabLayout tabLayout;
	protected ViewPager viewPager;
	protected BaseViewPagerAdapter adapter;
	
	protected OnPageChangeListener onPageChangeListener;
	
	protected int onOpenPageId = 0, lastOpenPosition = 0;
	
	/**
	 * Create new instance of BaseTabLayoutFragment
	 */
	public BaseTabLayoutFragment() {
		INSTANCE = this;
		
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.boxPlayActivity = BoxPlayApplication.getBoxPlayApplication();
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
	 * Need to be overrided!
	 * 
	 * Now use the addFragment(Fragment, String) method to add pages
	 */
	protected abstract void initialize();
	
	/**
	 * Delegate function of adapter.addFragment(Fragment, String);
	 * 
	 * @param fragment
	 *            a fragment instance
	 * @param title
	 *            Corresponding title
	 */
	protected void addFragment(Fragment fragment, String title) {
		adapter.addFragment(fragment, title);
	}
	
	/**
	 * Delegate function of adapter.addFragment(Fragment, Context.getString(int));
	 * 
	 * @param fragment
	 *            a fragment instance
	 * @param titleRessource
	 *            Corresponding title string ressource id
	 */
	protected void addFragment(Fragment fragment, @StringRes int titleRessource) {
		adapter.addFragment(fragment, getString(titleRessource));
	}
	
	/**
	 * Change Tab behavior
	 * 
	 * Possible: TabLayout.MODE_FIXED or TabLayout.MODE_SCROLLABLE
	 * 
	 * @param mode
	 */
	protected void setTabMode(int mode) {
		tabLayout.setTabMode(mode);
	}
	
	/**
	 * Quickly change page
	 * 
	 * @param pageId
	 *            Open a page by its index
	 * @return Fragment instance
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
	 * Update drawer selection by lastOpenPosition
	 */
	private void updateDrawerSelection() {
		updateDrawerSelection(getMenuItemIdByPageId(lastOpenPosition));
	}
	
	/**
	 * Update drawer selection by MenuItem's id
	 * 
	 * @param menuItemId
	 */
	private void updateDrawerSelection(int menuItemId) {
		if (BoxPlayActivity.getBoxPlayActivity() != null) {
			updateDrawerSelection(BoxPlayActivity.getBoxPlayActivity().getNavigationView().getMenu().findItem(menuItemId));
		}
	}
	
	/**
	 * Update drawer selection by MenuItem instance directly
	 * 
	 * @param menuItem
	 */
	private void updateDrawerSelection(MenuItem menuItem) {
		BoxPlayApplication.getViewHelper().unselectAllMenu();
		menuItem.setChecked(true);
	}
	
	/**
	 * Get actual page's fragment
	 * 
	 * @return The actual subfragment open
	 */
	public Fragment getActualFragment() {
		return adapter.getItem(lastOpenPosition);
	}
	
	/**
	 * Get the last open fragment id
	 * 
	 * @return Last open position
	 */
	public int getLastOpenPosition() {
		return lastOpenPosition;
	}
	
	/**
	 * Get MenuItem's id by page position
	 * 
	 * Used to select item in the drawer
	 * 
	 * @param pageId
	 *            Actual oppened pageId/(supposed) index
	 * @return
	 */
	protected abstract int getMenuItemIdByPageId(int pageId);
	
}