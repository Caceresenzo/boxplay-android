package caceresenzo.apps.boxplay.fragments.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import caceresenzo.apps.boxplay.R;

public class ViewFragment extends Fragment {
	
	public static final String TAG = ViewFragment.class.getSimpleName();
	
	private boolean activityCreated = false;
	
	private FrameLayout containerFrameLayout;
	private View targetView;
	private boolean withScroll;
	
	public ViewFragment() {
		this(null, true);
	}
	
	public ViewFragment(View view) {
		this(view, true);
	}
	
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
	
	public View getTargetView() {
		return targetView;
	}
	
	public void setTargetView(View targetView) {
		this.targetView = targetView;
		
		if (activityCreated) {
			applyView();
		}
	}
	
	private void applyView() {
		if (targetView != null) {
			containerFrameLayout.addView(targetView);
		}
	}
	
}