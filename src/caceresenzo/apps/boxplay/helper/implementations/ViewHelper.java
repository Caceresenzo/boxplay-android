package caceresenzo.apps.boxplay.helper.implementations;

import android.support.v4.app.Fragment;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.AbstractHelper;

public class ViewHelper extends AbstractHelper {
	
	/* Variables */
	private Fragment lastFragment;
	private int lastFragmentMenuItemId;
	
	/* Constructor */
	public ViewHelper(BoxPlayApplication boxPlayApplication) {
		super(boxPlayApplication);
	}
	
	public Fragment getLastFragment() {
		return lastFragment;
	}
	
	public void setLastFragment(Fragment lastFragment) {
		this.lastFragment = lastFragment;
	}
	
	public int getLastFragmentMenuItemId() {
		return lastFragmentMenuItemId;
	}
	
	public void setLastFragmentMenuItemId(int lastFragmentMenuItemId) {
		this.lastFragmentMenuItemId = lastFragmentMenuItemId;
	}
	
}