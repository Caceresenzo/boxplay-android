package caceresenzo.apps.boxplay.fragments;

import android.content.Context;
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
	protected Context context;
	
	/* Constructor */
	public BaseBoxPlayFragment() {
		this.boxPlayApplication = BoxPlayApplication.getBoxPlayApplication();
		this.handler = BoxPlayApplication.getHandler();
		this.viewHelper = BoxPlayApplication.getViewHelper();
		this.managers = BoxPlayApplication.getManagers();

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