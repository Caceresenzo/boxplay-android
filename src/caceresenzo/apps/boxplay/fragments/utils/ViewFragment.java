package caceresenzo.apps.boxplay.fragments.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import caceresenzo.apps.boxplay.R;

/**
 * Advanced Helping {@link Fragment} to hold any {@link View} in a {@link FrameLayout}
 * 
 * @author Enzo CACERES
 */
public class ViewFragment extends Fragment {
	
	/* Tag */
	public static final String TAG = ViewFragment.class.getSimpleName();
	
	/* Views */
	private FrameLayout containerFrameLayout;
	private View targetView;
	
	/* Variables */
	private boolean activityCreated, withScroll;
	
	/**
	 * Constructor, create a new instance with a {@link View} considered as null and scrolling on
	 */
	public ViewFragment() {
		this(null, true);
	}
	
	/**
	 * Constructor, create a new instance with a custom {@link View} and scrolling on
	 * 
	 * @param view
	 *            Target {@link View}
	 */
	public ViewFragment(View view) {
		this(view, true);
	}
	
	/**
	 * Constructor, create a new instance with a custom {@link View} and a custom scrolling state
	 * 
	 * @param view
	 *            Target {@link View}
	 * @param withScroll
	 *            Scrolling state
	 */
	public ViewFragment(View view, boolean withScroll) {
		this.targetView = view;
		this.withScroll = withScroll;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(withScroll ? R.layout.fragment_view : R.layout.fragment_view_noscroll, null, false);
		containerFrameLayout = (FrameLayout) view.findViewById(R.id.fragment_view_framelayout_container);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		activityCreated = true;
		
		applyView();
	}
	
	/**
	 * Get the actual {@link View}
	 * 
	 * @return Actual {@link View} in the {@link FrameLayout}
	 */
	public View getTargetView() {
		return targetView;
	}
	
	/**
	 * Set a custom {@link View}
	 * 
	 * @param targetView
	 *            New {@link View}
	 * @return Itself
	 */
	public ViewFragment withTargetView(View targetView) {
		this.targetView = targetView;
		
		if (activityCreated) {
			applyView();
		}
		
		return this;
	}
	
	/**
	 * Update the {@link Fragment}
	 */
	private void applyView() {
		if (targetView != null) {
			containerFrameLayout.removeAllViews();
			containerFrameLayout.addView(targetView);
		}
	}
	
}