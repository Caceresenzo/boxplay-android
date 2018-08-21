package caceresenzo.apps.boxplay.fragments;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

public class BaseViewPagerAdapter extends FragmentStatePagerAdapter {
	
	private final List<Fragment> fragmentList = new ArrayList<>();
	private final List<String> fragmentTitleList = new ArrayList<>();
	
	private boolean clearing = false;
	
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
	
	public void addFragment(Fragment fragment, String title) {
		fragmentList.add(fragment);
		fragmentTitleList.add(title);
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return fragmentTitleList.get(position);
	}
	
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