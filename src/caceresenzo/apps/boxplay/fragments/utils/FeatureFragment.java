package caceresenzo.apps.boxplay.fragments.utils;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import caceresenzo.apps.boxplay.R;

public class FeatureFragment extends Fragment {
	
	public FeatureFragment() {
		;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_feature, container, false);
		return view;
	}
	
}