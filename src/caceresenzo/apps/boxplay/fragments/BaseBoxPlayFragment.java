package caceresenzo.apps.boxplay.fragments;

import android.os.Handler;
import android.support.v4.app.Fragment;
import caceresenzo.apps.boxplay.application.BoxPlayApplication;
import caceresenzo.apps.boxplay.helper.ViewHelper;
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
	protected ViewHelper viewHelper;
	protected XManagers managers;
	
	/* Variables */
	protected boolean ready, destroyed;
	
	/* Constructor */
	public BaseBoxPlayFragment() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.handler = BoxPlayApplication.getHandler();
		this.viewHelper = BoxPlayApplication.getViewHelper();
		this.managers = BoxPlayApplication.getManagers();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		destroyed = true;
	}
	
}