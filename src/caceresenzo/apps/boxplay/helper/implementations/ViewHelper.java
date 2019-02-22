package caceresenzo.apps.boxplay.helper.implementations;

import android.support.v4.app.Fragment;
import caceresenzo.apps.boxplay.activities.BoxPlayActivity;
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
	
	/** @return Last {@link Fragment} supposed to have been displayed with the {@link BoxPlayActivity#showFragment(Fragment)} function. */
	public Fragment getLastFragment() {
		return lastFragment;
	}
	
	/**
	 * Set the "new" last fragment, the actually open.<br>
	 * This function is meant to be called by the {@link BoxPlayActivity#showFragment(Fragment)} function. Any other call can result in unexpected behavior.
	 * 
	 * @param lastFragment
	 *            New fragment that will be displayed.
	 */
	public void setLastFragment(Fragment lastFragment) {
		this.lastFragment = lastFragment;
	}
	
	/** @return Last menu id that have been used to display the actual {@link Fragment} that is supposed to have been displayed with the {@link BoxPlayActivity#showFragment(Fragment)} function. */
	public int getLastFragmentMenuItemId() {
		return lastFragmentMenuItemId;
	}
	
	/**
	 * Set the "new" last fragment menu id, the actually open.<br>
	 * This function is meant to be called by the {@link BoxPlayActivity#showFragment(Fragment)} function. Any other call can result in unexpected behavior.
	 * 
	 * @param lastFragmentMenuItemId
	 *            New menu id that have been used to display the actual fragment that will be displayed.
	 */
	public void setLastFragmentMenuItemId(int lastFragmentMenuItemId) {
		this.lastFragmentMenuItemId = lastFragmentMenuItemId;
	}
	
}