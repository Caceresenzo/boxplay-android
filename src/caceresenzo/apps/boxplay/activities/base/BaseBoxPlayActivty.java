package caceresenzo.apps.boxplay.activities.base;

import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.XManagers;

public abstract class BaseBoxPlayActivty extends AppCompatActivity {
	
	public static final int NO_VALUE = -1;

	protected static Fragment FRAGMENT_TO_OPEN = null;
	protected static int MENUITEM_ID_TO_SELECT = NO_VALUE;
	
	protected BoxPlayApplication boxPlayApplication;
	
	protected Handler handler;
	protected XManagers managers;
	protected ViewHelper helper;
	
	private boolean ready = false;
	
	protected CoordinatorLayout coordinatorLayout;
	
	public BaseBoxPlayActivty() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		
		this.handler = BoxPlayApplication.getHandler();
		this.managers = BoxPlayApplication.getManagers();
		this.helper = BoxPlayApplication.getViewHelper();
	}
	
	protected void initialize() {
		
	}
	
	/**
	 * Asking Activity to recreate without any specific fragment to open after recreation
	 */
	public void askRecreate() {
		askRecreate(null);
	}
	
	/**
	 * Asking Activity to recreate but with a specific fragment to open after recreation
	 * 
	 * If oldFrangent is null, default fragment will be open
	 * 
	 * @param oldFrangent
	 *            Target fragment
	 */
	public void askRecreate(Fragment oldFrangent) {
		FRAGMENT_TO_OPEN = oldFrangent;
		recreate();
	}
	
	public boolean isReady() {
		return ready;
	}
	
	protected void ready() {
		this.ready = true;
		
		BoxPlayApplication.attachActivity(this);
	}
	
	public CoordinatorLayout getCoordinatorLayout() {
		return coordinatorLayout;
	}
	
}