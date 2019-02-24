package caceresenzo.apps.boxplay.fragments;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;

/**
 * Simple PageViewerAdapter for Fragment content.
 * 
 * @author Enzo CACERES
 */
public class BaseViewPagerAdapter extends FragmentStatePagerAdapter {
	
	/* Variables */
	private final List<Fragment> fragments;
	private final List<String> titles;
	private boolean clearing;
	
	/* Constructor */
	/**
	 * Create a new instance from {@link BoxPlayApplication}'s default {@link FragmentManager}.
	 */
	public BaseViewPagerAdapter(BaseBoxPlayFragment boxPlayFragment) {
		this(boxPlayFragment.getChildFragmentManager());
	}
	
	/* Constructor */
	/**
	 * Create a new instance from a specific {@link FragmentManager}.
	 * 
	 * @param manager
	 *            Specific {@link FragmentManager}.
	 */
	public BaseViewPagerAdapter(FragmentManager manager) {
		super(manager);
		
		fragments = new ArrayList<>();
		titles = new ArrayList<>();
	}
	
	@Override
	public Fragment getItem(int position) {
		return fragments.get(position);
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
		return fragments.size();
	}
	
	/**
	 * Add a fragment to the {@link ViewPager}.
	 * 
	 * @param fragment
	 *            New fragment.
	 */
	public void addFragment(Fragment fragment) {
		addFragment(fragment, "");
	}
	
	/**
	 * Add a fragment to the {@link ViewPager}.
	 * 
	 * @param fragment
	 *            New fragment.
	 * @param titleStringRessourceId
	 *            New title from {@link R.string}.
	 */
	public void addFragment(Fragment fragment, int titleStringRessourceId) {
		addFragment(fragment, BoxPlayApplication.getBoxPlayApplication().getString(titleStringRessourceId));
	}
	
	/**
	 * Add a fragment to the {@link ViewPager}.
	 * 
	 * @param fragment
	 *            New fragment.
	 * @param title
	 *            Fragment's title.
	 */
	public void addFragment(Fragment fragment, String title) {
		fragments.add(fragment);
		titles.add(title);
	}
	
	/**
	 * Replace a fragment by another fragment.
	 * 
	 * @param index
	 *            Old fragment index.
	 * @param newFragment
	 *            New fragment to replace the old one.
	 */
	public void updateFragment(int index, Fragment newFragment) {
		if (newFragment != null && (index >= 0 && index <= fragments.size())) {
			fragments.set(index, newFragment);
		}
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return titles.get(position);
	}
	
	/** @return An array of the currently added {@link Fragment} created from the local {@link List}. */
	public Fragment[] getFragments() {
		return fragments.toArray(new Fragment[fragments.size()]);
	}
	
	/**
	 * Remove all fragment of the {@link ViewPager}.
	 * 
	 * @param viewPager
	 *            Parent {@link ViewPager} used with this {@link BaseViewPagerAdapter}.
	 */
	public void clearFragments(ViewPager viewPager) {
		clearing = true;
		
		for (Fragment fragment : fragments) {
			viewPager.removeView(fragment.getView());
		}
		
		fragments.clear();
		titles.clear();
		
		notifyDataSetChanged();
		
		clearing = false;
	}
	
}