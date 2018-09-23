package caceresenzo.apps.boxplay.activities.base;

import android.content.Context;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.LocaleHelper;
import caceresenzo.apps.boxplay.helper.ViewHelper;
import caceresenzo.apps.boxplay.managers.XManagers;

/**
 * Base {@link AppCompatActivity} class for every other activity in BoxPlay, allow direct access to common managers like {@link XManagers}, {@link ViewHelper} and a {@link Handler}
 * 
 * @author Enzo CACERES
 */
public abstract class BaseBoxPlayActivty extends AppCompatActivity {
	
	/* Constants */
	public static final int NO_VALUE = -1;
	
	/* Static */
	protected static Fragment FRAGMENT_TO_OPEN = null;
	protected static int MENUITEM_ID_TO_SELECT = NO_VALUE;
	
	/* Managers */
	protected BoxPlayApplication boxPlayApplication;
	protected Handler handler;
	protected ViewHelper viewHelper;
	protected XManagers managers;
	
	/* Views */
	protected CoordinatorLayout coordinatorLayout;
	
	/* Variables */
	private boolean ready = false;
	
	/* Constructor */
	public BaseBoxPlayActivty() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.handler = BoxPlayApplication.getHandler();
		this.viewHelper = BoxPlayApplication.getViewHelper();
		this.managers = BoxPlayApplication.getManagers();
		
		initialize();
	}
	
	/**
	 * If you don't need to define any final variables, please use this function to initialize your system
	 */
	protected void initialize() {
		;
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(LocaleHelper.onAttach(base));
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