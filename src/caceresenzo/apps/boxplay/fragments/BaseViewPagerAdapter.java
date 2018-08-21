package caceresenzo.apps.boxplay.fragments;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;

/**
 * Simple PageViewerAdapter for Fragment content
 * 
 * @author Enzo CACERES
 */
public class BaseViewPagerAdapter extends FragmentStatePagerAdapter {
	
	private final List<Fragment> fragmentList = new ArrayList<>();
	private final List<String> fragmentTitleList = new ArrayList<>();
	
	private boolean clearing = false;
	
	/**
	 * Constructor, create a new instance from {@link BoxPlayApplication}'s default {@link FragmentManager}
	 */
	public BaseViewPagerAdapter() {
		super(BoxPlayApplication.getBoxPlayApplication().getSupportFragmentManager());
	}
	
	/**
	 * Constructor, create a new instance from a specific {@link FragmentManager}
	 * 
	 * @param manager
	 *            Specific {@link FragmentManager}
	 */
	public BaseViewPagerAdapter(FragmentManager manager) {
		super(manager);
	}
	
	@Override
	public Fragment getItem(int position) {
		return fragmentList.get(position);
	}
	
	@Override
	public int getItemPosition(Object object) {
		if (clearing) {
			return POSITION_NONE;
		}
		
		return super.getItemPosition(object);
	}
	
	@Override
	public int getCount() {
		return fragmentList.size();
	}
	
	/**
	 * Add a fragment to the {@link ViewPager}
	 * 
	 * @param fragment
	 *            New fragment
	 */
	public void addFragment(Fragment fragment) {
		addFragment(fragment, "");
	}
	
	/**
	 * Add a fragment to the {@link ViewPager}
	 * 
	 * @param fragment
	 *            New fragment
	 * @param titleStringRessourceId
	 *            New title from {@link R.string}
	 */
	public void addFragment(Fragment fragment, int titleStringRessourceId) {
		fragmentList.add(fragment);
		fragmentTitleList.add(BoxPlayApplication.getBoxPlayApplication().getString(titleStringRessourceId));
	}
	
	/**
	 * Add a fragment to the {@link ViewPager}
	 * 
	 * @param fragment
	 *            New fragment
	 * @param title
	 *            New title
	 */
	public void addFragment(Fragment fragment, String title) {
		fragmentList.add(fragment);
		fragmentTitleList.add(title);
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return fragmentTitleList.get(position);
	}
	
	/**
	 * Remove all fragment of the {@link ViewPager}
	 * 
	 * @param viewPager
	 *            Parent {@link ViewPager} used with this {@link BaseViewPagerAdapter}
	 */
	public void clearFragments(ViewPager viewPager) {
		clearing = true;
		
		for (Fragment fragment : fragmentList) {
			viewPager.removeView(fragment.getView());
		}
		
		fragmentList.clear();
		fragmentTitleList.clear();
		
		notifyDataSetChanged();
		
		clearing = false;
	}
	
}