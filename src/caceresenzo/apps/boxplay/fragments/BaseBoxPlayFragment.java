package caceresenzo.apps.boxplay.fragments;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.implementations.ApplicationHelper;
import caceresenzo.apps.boxplay.helper.implementations.CacheHelper;
import caceresenzo.apps.boxplay.helper.implementations.ImageHelper;
import caceresenzo.apps.boxplay.helper.implementations.MenuHelper;
import caceresenzo.apps.boxplay.helper.implementations.ViewHelper;
import caceresenzo.apps.boxplay.managers.XManagers;

/**
 * Base class for any fragment for BoxPlay
 * 
 * @author Enzo CACERES
 */
public class BaseBoxPlayFragment extends Fragment {
	
	/* Tag */
	public static final String TAG = BaseBoxPlayFragment.class.getSimpleName();
	
	/* Managers */
	protected BoxPlayApplication boxPlayApplication;
	protected Handler handler;
	protected ApplicationHelper applicationHelper;
	protected CacheHelper cacheHelper;
	protected ImageHelper imageHelper;
	protected MenuHelper menuHelper;
	protected ViewHelper viewHelper;
	protected XManagers managers;
	
	/* Variables */
	protected boolean ready, destroyed;
	protected Context context;
	
	/* Constructor */
	public BaseBoxPlayFragment() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.handler = BoxPlayApplication.getHandler();
		this.applicationHelper = boxPlayApplication.getApplicationHelper();
		this.cacheHelper = boxPlayApplication.getCacheHelper();
		this.imageHelper = boxPlayApplication.getImageHelper();
		this.menuHelper = boxPlayApplication.getMenuHelper();
		this.viewHelper = boxPlayApplication.getViewHelper();
		this.managers = boxPlayApplication.getManagers();

		this.ready = false;
		this.destroyed = false;
	}
	
	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		
		this.context = context;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();

		ready = false;
		destroyed = true;
		context = null;
	}
	
	protected void ready() {
		ready = true;
	}
	
	public boolean isReady() {
		return ready;
	}
	
	public boolean isContextValid() {
		return !destroyed && context != null;
	}
	
}